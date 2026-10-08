package network.ike.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The project-local repository of a working-set member built from its own root.
 *
 * <p>Maven 4 resolves a reactor sibling that is not in the build from the project-local
 * repository at {@code <root>/.mvn/target/project-local-repo}, where the root is the nearest
 * {@code .mvn} directory. Every member of a working set carries its own {@code .mvn} so it
 * builds standalone, so a member built from its own root serves siblings from its own cache,
 * written by the last build rooted there, and never from the working set's build or the
 * newer install in the local repository; Maven's freshness check compares the cache with the
 * sibling's {@code target/classes} only. Seen 2026-10-08: a module of tinkar-core compiled
 * against a {@code common} from ten days earlier (IKE-Network/ike-issues#1265).
 *
 * <p>So, when the session's root directory is a member nested inside a working set, this
 * discards that member's project-local repository before anything resolves from it, and says
 * so. Resolution then falls back to the local repository, which the working set's build keeps
 * current, and Maven regenerates the cache from the modules this build packages.
 * {@code -D}{@value #KEEP_PROPERTY}{@code =keep} keeps the cache.
 */
final class MemberCache {

    private static final Logger LOG = LoggerFactory.getLogger(MemberCache.class);

    /** The system property that keeps a member's cache: {@code -Dike.workspace.memberCache=keep}. */
    static final String KEEP_PROPERTY = "ike.workspace.memberCache";

    /** The cache, relative to a root directory, as Maven 4 names it. */
    static final Path PROJECT_LOCAL_REPO = Path.of(".mvn", "target", "project-local-repo");

    private MemberCache() {}

    /** What was found for a root directory: the working set it is a member of, and its cache if any. */
    record Member(String name, Path workspaceRoot, Path cache) {}

    /**
     * The working set a root directory is a member of, if it is one: an ancestor directory
     * with a manifest that lists the root directory's name.
     *
     * @param rootDirectory the build's root directory, the nearest {@code .mvn} directory
     * @return the member, or empty if the root directory is a working-set root itself or no
     *         enclosing manifest lists it
     */
    static Optional<Member> memberOf(Path rootDirectory) {
        Path root = rootDirectory.toAbsolutePath().normalize();
        if (Manifests.exists(root)) {
            return Optional.empty();
        }
        for (Path ancestor = root.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
            Path manifest = Manifests.in(ancestor);
            if (manifest == null) {
                continue;
            }
            Path relative = ancestor.relativize(root);
            if (relative.getNameCount() == 1
                    && WorkspaceIndex.manifestMembers(manifest).contains(relative.toString())) {
                return Optional.of(new Member(relative.toString(), ancestor, root.resolve(PROJECT_LOCAL_REPO)));
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Discards the project-local repository of a root directory that is a member built from
     * its own root inside a working set, unless {@value #KEEP_PROPERTY} says to keep it.
     *
     * @param rootDirectory the build's root directory
     * @return the member whose cache was discarded, or empty when nothing was
     */
    static Optional<Member> discard(Path rootDirectory) {
        Optional<Member> member = memberOf(rootDirectory);
        if (member.isEmpty() || !Files.isDirectory(member.get().cache())) {
            return Optional.empty();
        }
        Member found = member.get();
        if ("keep".equalsIgnoreCase(System.getProperty(KEEP_PROPERTY))) {
            LOG.warn("[ike-workspace-extension] {} is built from its own root inside the working set {}: "
                    + "siblings outside this build resolve from its own project-local repository {}, "
                    + "kept by -D{}=keep (IKE-Network/ike-issues#1265)",
                    found.name(), found.workspaceRoot(), found.cache(), KEEP_PROPERTY);
            return Optional.empty();
        }
        String age = lastWritten(found.cache()).map(Instant::toString).orElse("unknown");
        try {
            deleteTree(found.cache());
        } catch (IOException e) {
            LOG.warn("[ike-workspace-extension] could not discard {}: {}", found.cache(), e.getMessage());
            return Optional.empty();
        }
        LOG.warn("[ike-workspace-extension] {} is built from its own root inside the working set {}; "
                + "its project-local repository {} (last written {}) was discarded so siblings outside "
                + "this build resolve from the local repository, which the working set's build keeps "
                + "current, not from a cache this root last wrote. Build from {} with "
                + "`mvn -pl {}/<module> ...` instead; -D{}=keep keeps the cache "
                + "(IKE-Network/ike-issues#1265).",
                found.name(), found.workspaceRoot(), found.cache(), age, found.workspaceRoot(),
                found.name(), KEEP_PROPERTY);
        return member;
    }

    private static Optional<Instant> lastWritten(Path cache) {
        try (Stream<Path> files = Files.walk(cache)) {
            return files.filter(Files::isRegularFile)
                    .map(file -> {
                        try {
                            return Files.getLastModifiedTime(file).toInstant();
                        } catch (IOException e) {
                            return Instant.EPOCH;
                        }
                    })
                    .max(Comparator.naturalOrder());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static void deleteTree(Path root) throws IOException {
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(root)) {
            paths = walk.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path path : paths) {
            Files.delete(path);
        }
    }
}
