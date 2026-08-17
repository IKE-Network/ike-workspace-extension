package network.ike.extension;

import org.apache.maven.api.di.Named;
import org.apache.maven.api.di.Singleton;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.spi.ModelTransformer;
import org.apache.maven.api.spi.ModelTransformerException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maven 4 build extension that resolves intra-working-set dependency
 * versions at file-model time — before profile activation, model
 * validation, and, decisively, before the reactor sorter runs
 * (IKE-Network/ike-issues#1019).
 *
 * <p><b>Why.</b> A working set deliberately makes its BOM a living
 * member, so an intra-set dependency declared without a version has no
 * version at model-building time: the BOM import that would supply it
 * degrades when the BOM is co-built (Maven 4's
 * {@code bom-import-from-reactor} model problem). The reactor sorter
 * matches dependencies against reactor members by full identity, so no
 * edge is created, modules sort arbitrarily, and resolution succeeds or
 * fails by luck of the local repository — masked until the first build
 * that needs a version existing nowhere yet, which is exactly what a
 * release mission creates.
 *
 * <p><b>The rule</b> (settled 2026-08-16, recorded in the workspace
 * plugin's site as {@code workspace-version-resolution.adoc}):
 *
 * <ul>
 *   <li><b>Build</b> — every intra-set dependency binds to the
 *       reactor's current version: always align with snapshots.</li>
 *   <li><b>Release</b> (signaled by
 *       {@code -Dike.workspace.release=true}) — a member whose POM
 *       carries a development version is unchanged this mission: its
 *       consumers bind to its most recently <em>released</em> version,
 *       from the mission records. A member at a non-development version
 *       has been version-passed — it is releasing — and consumers bind
 *       to that reactor-current value.</li>
 * </ul>
 *
 * <p>The changed set is therefore read from what the release mission's
 * version pass materialized on disk, and the not-releasing versions
 * from {@code releases/release-<mission>.yaml} — the goal computes, the
 * extension applies. Dependencies that already carry a version (usual
 * for anything external, and for deliberate released pins) pass through
 * untouched; so does every POM outside a working set.
 *
 * <p>Every binding is printed once per build — resolution is never
 * silent.
 */
@Named("ike-workspace-intra-set-versions")
@Singleton
public class IntraSetVersionTransformer implements ModelTransformer {

    /** Release-mode signal, set by the release mission's reactor builds. */
    static final String RELEASE_MODE_PROPERTY = "ike.workspace.release";

    private static final Map<Path, WorkspaceIndex> INDEX_CACHE =
            new ConcurrentHashMap<>();
    private static final Map<String, Boolean> PRINTED =
            new ConcurrentHashMap<>();

    /** Creates the transformer. DI-only; not for direct construction. */
    public IntraSetVersionTransformer() {}

    /**
     * Bind versionless intra-set dependencies per the resolution rule.
     * No-op for POMs outside a working set.
     *
     * @param model the file-stage parsed Maven model
     * @return the model with intra-set versions bound — same instance
     *         when nothing needed binding
     * @throws ModelTransformerException never thrown; declared for SPI
     *                                   conformance
     */
    @Override
    public Model transformFileModel(Model model)
            throws ModelTransformerException {
        Path pomPath = model.getPomFile();
        if (pomPath == null) {
            return model;
        }
        Path workspaceRoot = findWorkspaceRoot(pomPath.getParent());
        if (workspaceRoot == null) {
            return model;
        }
        List<Dependency> dependencies = model.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return model;
        }

        WorkspaceIndex index = INDEX_CACHE.computeIfAbsent(
                workspaceRoot, WorkspaceIndex::scan);
        boolean releaseMode =
                Boolean.getBoolean(RELEASE_MODE_PROPERTY);

        List<Dependency> bound = new ArrayList<>(dependencies.size());
        boolean changed = false;
        for (Dependency dependency : dependencies) {
            String resolution = resolve(dependency, index, releaseMode);
            if (resolution == null) {
                bound.add(dependency);
                continue;
            }
            bound.add(dependency.withVersion(resolution));
            changed = true;
            printOnce(dependency.getGroupId() + ":"
                    + dependency.getArtifactId() + " -> " + resolution
                    + (releaseMode ? " [release]" : " [build]"));
        }
        return changed ? model.withDependencies(bound) : model;
    }

    /**
     * The version a versionless intra-set dependency binds to, or
     * {@code null} when this transformer has nothing to say — the
     * dependency already has a version, or the working set does not
     * produce the coordinate.
     *
     * @param dependency  the dependency under consideration
     * @param index       the working set's artifact index
     * @param releaseMode whether a release mission signaled release mode
     * @return the version to bind, or {@code null} to pass through
     */
    static String resolve(Dependency dependency, WorkspaceIndex index,
                          boolean releaseMode) {
        String version = dependency.getVersion();
        if (version != null && !version.isBlank()) {
            return null;
        }
        String groupId = dependency.getGroupId();
        WorkspaceIndex.Produced produced;
        if (groupId == null || groupId.contains("${")) {
            // The ${project.groupId} sibling idiom: the groupId is an
            // uninterpolated expression at file-model time, so identity
            // comes from the artifactId — and only when exactly one
            // member produces it. The expression itself stays in the
            // model untouched; only the version is bound.
            produced = index.producedByUniqueArtifactId(
                    dependency.getArtifactId());
        } else {
            produced = index.produced(groupId, dependency.getArtifactId());
        }
        if (produced == null) {
            return null;
        }
        if (!releaseMode) {
            return produced.currentVersion();
        }
        boolean development =
                produced.currentVersion().endsWith("-SNAPSHOT");
        if (!development) {
            // Version-passed this mission: the member is releasing.
            return produced.currentVersion();
        }
        String released = index.releasedVersion(produced.member());
        if (released == null) {
            // Never released and not in this mission's release set: the
            // release preflights own this refusal; binding the snapshot
            // here would hide it inside a deployed POM.
            System.err.println("[ike-workspace-extension] release mode: "
                    + dependency.getGroupId() + ":"
                    + dependency.getArtifactId() + " is produced by "
                    + produced.member() + ", which has never released —"
                    + " leaving the dependency unbound");
            return null;
        }
        return released;
    }

    /**
     * The nearest ancestor directory holding a {@code workspace.yaml},
     * or {@code null} when the POM is outside any working set.
     *
     * @param start the POM's directory
     * @return the workspace root, or {@code null}
     */
    private static Path findWorkspaceRoot(Path start) {
        Path dir = start;
        // A working set is shallow: root/member/…/module. Eight levels
        // is generous; the bound exists so repository-cache POMs with
        // deep paths cost nothing.
        for (int i = 0; i < 8 && dir != null; i++) {
            if (Files.exists(dir.resolve("workspace.yaml"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static void printOnce(String line) {
        if (PRINTED.putIfAbsent(line, Boolean.TRUE) == null) {
            System.err.println("[ike-workspace-extension] " + line);
        }
    }
}
