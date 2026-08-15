package network.ike.extension;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * What a working set knows about the artifacts it produces — the
 * lookup behind intra-set version resolution
 * (IKE-Network/ike-issues#1019).
 *
 * <p>Two maps, built in one scan of the working set and cached per
 * workspace root for the life of the build JVM:
 *
 * <ul>
 *   <li><b>produced artifacts</b> — {@code groupId:artifactId} of every
 *       POM under every member directory (the member's root and all its
 *       sub-modules), to the producing member's name and the version
 *       that POM currently carries;</li>
 *   <li><b>released versions</b> — member name to its most recently
 *       released version, read from the cycle records in
 *       {@code releases/release-<cycle>.yaml}, choosing by the row's
 *       recorded date.</li>
 * </ul>
 *
 * <p>Reading uses the JDK's StAX parser — the raw elements only
 * (groupId, artifactId, version, and the parent block's as fallback),
 * never interpolation — because at file-model time nothing is
 * interpolated anyway, and the extension must not grow dependencies.
 */
final class WorkspaceIndex {

    /**
     * One produced artifact.
     *
     * @param member         the producing member's directory name
     * @param currentVersion the version its POM carries right now
     */
    record Produced(String member, String currentVersion) {}

    private final Map<String, Produced> producedByGa;
    private final Map<String, String> releasedByMember;
    private final Map<String, Produced> producedByUniqueArtifactId;

    private WorkspaceIndex(Map<String, Produced> producedByGa,
                           Map<String, String> releasedByMember) {
        this.producedByGa = producedByGa;
        this.releasedByMember = releasedByMember;
        Map<String, Produced> unique = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<String, Produced> entry : producedByGa.entrySet()) {
            String artifactId = entry.getKey()
                    .substring(entry.getKey().indexOf(':') + 1);
            counts.merge(artifactId, 1, Integer::sum);
            unique.put(artifactId, entry.getValue());
        }
        counts.forEach((artifactId, count) -> {
            if (count > 1) {
                unique.remove(artifactId);
            }
        });
        this.producedByUniqueArtifactId = unique;
    }

    /**
     * The producer of an artifactId that exactly one member produces —
     * the lookup behind the {@code ${project.groupId}} dependency idiom,
     * whose groupId is an uninterpolated expression at file-model time.
     * Ambiguity returns {@code null}: binding by artifactId alone when
     * two members produce the same name would be a guess.
     *
     * @param artifactId the dependency's artifactId
     * @return the unique producer, or {@code null}
     */
    Produced producedByUniqueArtifactId(String artifactId) {
        return producedByUniqueArtifactId.get(artifactId);
    }

    /**
     * The producer of a coordinate, or {@code null} when the working
     * set does not produce it.
     *
     * @param groupId    the dependency's groupId
     * @param artifactId the dependency's artifactId
     * @return the producing member and current version, or {@code null}
     */
    Produced produced(String groupId, String artifactId) {
        return producedByGa.get(groupId + ":" + artifactId);
    }

    /**
     * A member's most recently released version, or {@code null} when
     * no cycle record names it.
     *
     * @param member the member directory name
     * @return the released version, or {@code null}
     */
    String releasedVersion(String member) {
        return releasedByMember.get(member);
    }

    /**
     * Build the index for a workspace root: parse the manifest's member
     * list, scan each member's POM tree, and read the cycle records.
     *
     * @param root the directory holding {@code workspace.yaml}
     * @return the index; empty maps when the manifest is unreadable
     */
    static WorkspaceIndex scan(Path root) {
        Map<String, Produced> produced = new HashMap<>();
        for (String member : manifestMembers(root.resolve("workspace.yaml"))) {
            Path memberDir = root.resolve(member);
            if (!Files.isDirectory(memberDir)) {
                continue;
            }
            try (Stream<Path> tree = Files.walk(memberDir)) {
                // Depth-sorted so a directory's POM resolves before its
                // children's: Maven 4.1 models may declare an empty
                // <parent/> and omit groupId and version entirely,
                // inheriting both from the POM in the parent directory
                // (parent inference). The index performs the same
                // inference, or inference-style modules — the exact
                // shape that broke the reactor sort — never enter it.
                List<Path> poms = tree
                        .filter(p -> p.getFileName().toString().equals("pom.xml"))
                        .filter(p -> !p.toString().contains("/target/"))
                        .filter(p -> !crossesRepositoryBoundary(memberDir, p))
                        .sorted(java.util.Comparator.comparingInt(
                                Path::getNameCount))
                        .toList();
                Map<Path, Coordinates> resolvedByDir = new HashMap<>();
                for (Path pom : poms) {
                    Coordinates raw = readCoordinates(pom);
                    if (raw == null || raw.artifactId() == null) {
                        continue;
                    }
                    Coordinates inherited = nearestAncestor(
                            resolvedByDir, memberDir, pom.getParent());
                    String groupId = raw.groupId() != null
                            ? raw.groupId()
                            : inherited != null ? inherited.groupId() : null;
                    String version = raw.version() != null
                            ? raw.version()
                            : inherited != null ? inherited.version() : null;
                    Coordinates resolved = new Coordinates(
                            groupId, raw.artifactId(), version);
                    resolvedByDir.put(pom.getParent(), resolved);
                    if (groupId != null && version != null) {
                        produced.putIfAbsent(groupId + ":" + raw.artifactId(),
                                new Produced(member, version));
                    }
                }
            } catch (IOException e) {
                System.err.println("[ike-workspace-extension] cannot scan "
                        + memberDir + ": " + e.getMessage());
            }
        }
        return new WorkspaceIndex(produced, releasedVersions(root));
    }


    /**
     * The resolved coordinates of the nearest ancestor POM within the
     * member — the anchor Maven 4.1 parent inference inherits from.
     *
     * @param resolvedByDir directory → already-resolved coordinates
     * @param memberDir     the member's root, the walk's upper bound
     * @param start         the POM's directory
     * @return the nearest resolved ancestor, or {@code null}
     */
    private static Coordinates nearestAncestor(
            Map<Path, Coordinates> resolvedByDir, Path memberDir, Path start) {
        Path dir = start.getParent();
        while (dir != null && dir.startsWith(memberDir)) {
            Coordinates c = resolvedByDir.get(dir);
            if (c != null) {
                return c;
            }
            dir = dir.getParent();
        }
        return null;
    }

    /**
     * The raw identity of one POM.
     *
     * @param groupId    own or parent groupId
     * @param artifactId own artifactId
     * @param version    own or parent version
     */
    record Coordinates(String groupId, String artifactId, String version) {}

    /**
     * Read a POM's raw coordinates with the JDK StAX parser: the
     * project-level groupId/artifactId/version, falling back to the
     * parent block's groupId and version — the same inheritance a raw
     * model has before any building.
     *
     * @param pom the POM file
     * @return the coordinates, or {@code null} when unreadable
     */
    static Coordinates readCoordinates(Path pom) {
        String g = null, a = null, v = null, pg = null, pv = null;
        try (InputStream in = Files.newInputStream(pom)) {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
                    false);
            XMLStreamReader r = factory.createXMLStreamReader(in);
            int depth = 0;
            boolean inParent = false;
            String element = null;
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    depth++;
                    element = r.getLocalName();
                    if (depth == 2 && "parent".equals(element)) {
                        inParent = true;
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if (depth == 2 && "parent".equals(r.getLocalName())) {
                        inParent = false;
                    }
                    depth--;
                    element = null;
                } else if (event == XMLStreamConstants.CHARACTERS
                        && element != null && !r.isWhiteSpace()) {
                    String text = r.getText().trim();
                    if (depth == 2) {
                        switch (element) {
                            case "groupId" -> g = text;
                            case "artifactId" -> a = text;
                            case "version" -> v = text;
                            default -> { }
                        }
                    } else if (depth == 3 && inParent) {
                        switch (element) {
                            case "groupId" -> pg = text;
                            case "version" -> pv = text;
                            default -> { }
                        }
                    }
                    // Everything needed lives before <dependencies>;
                    // stop as soon as identity is complete.
                    if (a != null && (g != null || pg != null)
                            && (v != null || pv != null)
                            && depth == 1) {
                        break;
                    }
                }
            }
            r.close();
        } catch (Exception e) {
            return null;
        }
        return new Coordinates(g != null ? g : pg, a, v != null ? v : pv);
    }

    /**
     * The manifest's member names: keys nested one level under
     * {@code subprojects:}. A deliberate line parse — the extension
     * carries no YAML dependency, and the manifest's shape is ours.
     *
     * @param manifest the workspace.yaml path
     * @return member names in declaration order; empty when unreadable
     */
    static List<String> manifestMembers(Path manifest) {
        try {
            List<String> lines = Files.readAllLines(manifest,
                    StandardCharsets.UTF_8);
            java.util.ArrayList<String> members = new java.util.ArrayList<>();
            boolean inSubprojects = false;
            for (String line : lines) {
                if (line.startsWith("subprojects:")) {
                    inSubprojects = true;
                    continue;
                }
                if (inSubprojects) {
                    // Comment lines appear at any indent — including
                    // column zero between members — and are never the
                    // end of the list.
                    if (line.strip().startsWith("#")) {
                        continue;
                    }
                    if (!line.startsWith("  ") && !line.isBlank()) {
                        break;
                    }
                    if (line.matches("^  [A-Za-z0-9][^:\\s]*:\\s*$")) {
                        members.add(line.strip().replace(":", ""));
                    }
                }
            }
            return members;
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Member → most recently released version, across every cycle
     * record, latest row per member by recorded date.
     *
     * @param root the workspace root
     * @return the map; empty when no records exist
     */
    static Map<String, String> releasedVersions(Path root) {
        Map<String, String> version = new HashMap<>();
        Map<String, String> recordedDate = new HashMap<>();
        Path releases = root.resolve("releases");
        if (!Files.isDirectory(releases)) {
            return version;
        }
        try (Stream<Path> files = Files.list(releases)) {
            files.filter(p -> p.getFileName().toString().startsWith("release-"))
                    .filter(p -> p.getFileName().toString().endsWith(".yaml"))
                    .forEach(record -> readRecord(record, version, recordedDate));
        } catch (IOException e) {
            System.err.println("[ike-workspace-extension] cannot list "
                    + releases + ": " + e.getMessage());
        }
        return version;
    }

    private static void readRecord(Path record, Map<String, String> version,
                                   Map<String, String> recordedDate) {
        try {
            String member = null, v = null;
            for (String line : Files.readAllLines(record,
                    StandardCharsets.UTF_8)) {
                if (line.matches("^  [^\\s].*:\\s*$")) {
                    member = line.strip().replace(":", "");
                    v = null;
                } else if (member != null
                        && line.matches("^    version:.*")) {
                    v = line.replaceAll("^    version:\\s*\"?([^\"]*)\"?\\s*$",
                            "$1");
                } else if (member != null && v != null
                        && line.matches("^    recorded:.*")) {
                    String date = line.replaceAll(
                            "^    recorded:\\s*\"?([^\"]*)\"?\\s*$", "$1");
                    String prior = recordedDate.get(member);
                    if (prior == null || date.compareTo(prior) > 0) {
                        recordedDate.put(member, date);
                        version.put(member, v);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ike-workspace-extension] cannot read "
                    + record + ": " + e.getMessage());
        }
    }

    /**
     * Whether a POM lies inside a repository nested under the member —
     * the same boundary rule the release cycle's version pass applies.
     *
     * @param memberDir the member being scanned
     * @param pom       a POM beneath it
     * @return {@code true} when a directory between the two is itself
     *         a repository
     */
    private static boolean crossesRepositoryBoundary(Path memberDir, Path pom) {
        Path dir = pom.getParent();
        while (dir != null && !dir.equals(memberDir)) {
            if (Files.exists(dir.resolve(".git"))) {
                return true;
            }
            dir = dir.getParent();
        }
        return false;
    }
}
