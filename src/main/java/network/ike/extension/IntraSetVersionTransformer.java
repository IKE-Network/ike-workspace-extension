package network.ike.extension;

import org.apache.maven.api.di.Named;
import org.apache.maven.api.di.Singleton;
import org.apache.maven.api.model.Build;
import org.apache.maven.api.model.BuildBase;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Plugin;
import org.apache.maven.api.model.PluginExecution;
import org.apache.maven.api.model.PluginManagement;
import org.apache.maven.api.model.Profile;
import org.apache.maven.api.spi.ModelTransformer;
import org.apache.maven.api.spi.ModelTransformerException;
import org.apache.maven.api.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <p><b>Where the rule reaches</b> (widened 2026-08-20). A coordinate
 * naming a working-set member is the same fact wherever it is written,
 * so the rule is applied at every site in the file model that names one:
 *
 * <ul>
 *   <li>project {@code <dependencies>}, and a profile's;</li>
 *   <li>a plugin's own {@code <dependencies>}, under {@code <build>} and
 *       {@code <pluginManagement>} alike — these are ordinary
 *       {@link Dependency} objects that dependencyManagement does
 *       <em>not</em> govern, so before this they could only be
 *       hand-pinned, and the reactor edge a staged plugin jar needs
 *       existed only as long as someone kept the pin current;</li>
 *   <li>{@code <artifactItem>} entries in plugin configuration — see
 *       {@link ArtifactItems}.</li>
 * </ul>
 *
 * <p>The payoff is that a member's version stops being duplicated into
 * consumer POMs as {@code <member>.version} properties, which is what
 * went stale: a hand-maintained pin does not survive a sibling reactor's
 * version rewrite, and ws:align's bundle-edge derivation re-points it
 * one generation back (ike-issues#1027). Nothing to maintain, nothing
 * to re-point.
 *
 * <p>{@code <dependencyManagement>} is not a binding site. Nothing has
 * needed it — entries there carry explicit versions by convention, and
 * an explicit version passes through here anyway. The one case worth
 * revisiting is a member BOM's own import
 * ({@code <type>pom</type><scope>import</scope>}), which must track the
 * reactor line and is still hand-pinned; widening to cover it is a
 * separate decision, not an oversight.
 *
 * <p>Resolution is never silent: each working set is announced once per
 * build at info, and every binding is logged once at debug ({@code -X}
 * lists them). Maven relays anything an extension writes to standard
 * error as a warning, so the log is the channel.
 */
@Named("ike-workspace-intra-set-versions")
@Singleton
public class IntraSetVersionTransformer implements ModelTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(IntraSetVersionTransformer.class);

    /** Release-mode signal, set by the release mission's reactor builds. */
    static final String RELEASE_MODE_PROPERTY = "ike.workspace.release";

    /** Site labels, appended to the one-line binding report. */
    private static final String PROJECT = "";
    private static final String PLUGIN_DEPENDENCY = " (plugin dependency)";
    private static final String ARTIFACT_ITEM = " (artifactItem)";

    private static final Map<Path, WorkspaceIndex> INDEX_CACHE =
            new ConcurrentHashMap<>();
    private static final Map<String, Boolean> PRINTED =
            new ConcurrentHashMap<>();
    private static final Map<Path, Boolean> ANNOUNCED =
            new ConcurrentHashMap<>();

    /** Creates the transformer. DI-only; not for direct construction. */
    public IntraSetVersionTransformer() {}

    /**
     * Bind versionless intra-set coordinates per the resolution rule, at
     * every site the model carries them. No-op for POMs outside a
     * working set.
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

        WorkspaceIndex index = INDEX_CACHE.computeIfAbsent(
                workspaceRoot, WorkspaceIndex::scan);
        if (ANNOUNCED.putIfAbsent(workspaceRoot, Boolean.TRUE) == null) {
            LOG.info("[ike-workspace-extension] binding intra-set coordinates of {}"
                    + " to the reactor's versions (each binding is listed at debug)",
                    workspaceRoot.getFileName());
        }
        Binding binding = new Binding(index,
                Boolean.getBoolean(RELEASE_MODE_PROPERTY));

        Model bound = model;
        List<Dependency> dependencies =
                binding.dependencies(model.getDependencies(), PROJECT);
        if (dependencies != null) {
            bound = bound.withDependencies(dependencies);
        }
        Build build = bound.getBuild();
        if (build != null) {
            // The cast holds: BuildBase's with* methods are overridden
            // covariantly by Build, so rebuilding a Build yields a Build.
            BuildBase reboundBuild = binding.build(build);
            if (reboundBuild != null) {
                bound = bound.withBuild((Build) reboundBuild);
            }
        }
        List<Profile> profiles = binding.profiles(bound.getProfiles());
        if (profiles != null) {
            bound = bound.withProfiles(profiles);
        }
        return bound;
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
        return resolve(dependency.getGroupId(), dependency.getArtifactId(),
                index, releaseMode);
    }

    /**
     * The rule itself, over a bare coordinate — shared by every binding
     * site, since a plugin dependency and an {@code <artifactItem>} name
     * a member exactly the way a project dependency does. Callers have
     * already established that no version is written.
     *
     * @param groupId     the coordinate's groupId, possibly an
     *                    uninterpolated expression or absent
     * @param artifactId  the coordinate's artifactId
     * @param index       the working set's artifact index
     * @param releaseMode whether a release mission signaled release mode
     * @return the version to bind, or {@code null} to pass through
     */
    static String resolve(String groupId, String artifactId,
                          WorkspaceIndex index, boolean releaseMode) {
        if (artifactId == null || artifactId.isBlank()) {
            return null;
        }
        WorkspaceIndex.Produced produced;
        if (groupId == null || groupId.isBlank() || groupId.contains("${")) {
            // The ${project.groupId} sibling idiom: the groupId is an
            // uninterpolated expression at file-model time, so identity
            // comes from the artifactId — and only when exactly one
            // member produces it. The expression itself stays in the
            // model untouched; only the version is bound.
            produced = index.producedByUniqueArtifactId(artifactId);
        } else {
            produced = index.produced(groupId, artifactId);
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
            LOG.warn("[ike-workspace-extension] release mode: {}:{} is produced by {},"
                    + " which has never released — leaving the dependency unbound",
                    groupId, artifactId, produced.member());
            return null;
        }
        return released;
    }

    /**
     * One build's worth of binding: the index and mode, plus the
     * traversal of every site that can name a member. Each method
     * returns {@code null} when it changed nothing, so an untouched
     * model is returned as the very instance Maven handed over.
     */
    private static final class Binding implements ArtifactItems.Resolver {

        private final WorkspaceIndex index;
        private final boolean releaseMode;

        Binding(WorkspaceIndex index, boolean releaseMode) {
            this.index = index;
            this.releaseMode = releaseMode;
        }

        /**
         * {@inheritDoc}
         *
         * <p>The {@code <artifactItem>} site, reached from
         * {@link ArtifactItems}.
         */
        @Override
        public String version(String groupId, String artifactId) {
            String resolution =
                    resolve(groupId, artifactId, index, releaseMode);
            if (resolution != null) {
                report(groupId, artifactId, resolution, ARTIFACT_ITEM);
            }
            return resolution;
        }

        /**
         * Bind a dependency list.
         *
         * @param declared the declared dependencies, possibly null
         * @param site     the site label for the binding report
         * @return the bound list, or {@code null} when unchanged
         */
        List<Dependency> dependencies(List<Dependency> declared, String site) {
            if (declared == null || declared.isEmpty()) {
                return null;
            }
            List<Dependency> bound = new ArrayList<>(declared.size());
            boolean changed = false;
            for (Dependency dependency : declared) {
                String resolution = resolve(dependency, index, releaseMode);
                if (resolution == null) {
                    bound.add(dependency);
                    continue;
                }
                bound.add(dependency.withVersion(resolution));
                changed = true;
                report(dependency.getGroupId(), dependency.getArtifactId(),
                        resolution, site);
            }
            return changed ? bound : null;
        }

        /**
         * Bind a build section — its plugins and its plugin management.
         *
         * @param build the build section, {@code <build>} or a profile's
         * @return the bound section, or {@code null} when unchanged
         */
        BuildBase build(BuildBase build) {
            BuildBase bound = build;
            List<Plugin> plugins = plugins(build.getPlugins());
            if (plugins != null) {
                bound = bound.withPlugins(plugins);
            }
            PluginManagement management = build.getPluginManagement();
            if (management != null) {
                List<Plugin> managed = plugins(management.getPlugins());
                if (managed != null) {
                    bound = bound.withPluginManagement(
                            management.withPlugins(managed));
                }
            }
            return bound == build ? null : bound;
        }

        /**
         * Bind each plugin's own dependencies and its configuration —
         * plugin level and per execution.
         *
         * @param declared the declared plugins, possibly null
         * @return the bound list, or {@code null} when unchanged
         */
        List<Plugin> plugins(List<Plugin> declared) {
            if (declared == null || declared.isEmpty()) {
                return null;
            }
            List<Plugin> bound = new ArrayList<>(declared.size());
            boolean changed = false;
            for (Plugin plugin : declared) {
                Plugin rebound = plugin;
                List<Dependency> dependencies = dependencies(
                        plugin.getDependencies(), PLUGIN_DEPENDENCY);
                if (dependencies != null) {
                    rebound = rebound.withDependencies(dependencies);
                }
                XmlNode configuration =
                        ArtifactItems.bind(plugin.getConfiguration(), this);
                if (configuration != null) {
                    rebound = rebound.withConfiguration(configuration);
                }
                List<PluginExecution> executions =
                        executions(plugin.getExecutions());
                if (executions != null) {
                    rebound = rebound.withExecutions(executions);
                }
                if (rebound != plugin) {
                    changed = true;
                }
                bound.add(rebound);
            }
            return changed ? bound : null;
        }

        /**
         * Bind the configuration of each execution — an execution may
         * carry its own {@code <artifactItems>}.
         *
         * @param declared the declared executions, possibly null
         * @return the bound list, or {@code null} when unchanged
         */
        List<PluginExecution> executions(List<PluginExecution> declared) {
            if (declared == null || declared.isEmpty()) {
                return null;
            }
            List<PluginExecution> bound = new ArrayList<>(declared.size());
            boolean changed = false;
            for (PluginExecution execution : declared) {
                XmlNode configuration = ArtifactItems.bind(
                        execution.getConfiguration(), this);
                if (configuration == null) {
                    bound.add(execution);
                    continue;
                }
                bound.add(execution.withConfiguration(configuration));
                changed = true;
            }
            return changed ? bound : null;
        }

        /**
         * Bind each profile's dependencies and build section. A profile
         * is not activated yet at file-model time, so binding it is not
         * a decision about whether it applies — only about what it says
         * if it does.
         *
         * @param declared the declared profiles, possibly null
         * @return the bound list, or {@code null} when unchanged
         */
        List<Profile> profiles(List<Profile> declared) {
            if (declared == null || declared.isEmpty()) {
                return null;
            }
            List<Profile> bound = new ArrayList<>(declared.size());
            boolean changed = false;
            for (Profile profile : declared) {
                Profile rebound = profile;
                List<Dependency> dependencies =
                        dependencies(profile.getDependencies(), PROJECT);
                if (dependencies != null) {
                    rebound = rebound.withDependencies(dependencies);
                }
                BuildBase build = profile.getBuild();
                if (build != null) {
                    BuildBase reboundBuild = build(build);
                    if (reboundBuild != null) {
                        rebound = rebound.withBuild(reboundBuild);
                    }
                }
                if (rebound != profile) {
                    changed = true;
                }
                bound.add(rebound);
            }
            return changed ? bound : null;
        }

        private void report(String groupId, String artifactId,
                            String resolution, String site) {
            printOnce(groupId + ":" + artifactId + " -> " + resolution
                    + (releaseMode ? " [release]" : " [build]") + site);
        }
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
            if (Manifests.exists(dir)) {
                return dir;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static void printOnce(String line) {
        if (PRINTED.putIfAbsent(line, Boolean.TRUE) == null) {
            LOG.debug("[ike-workspace-extension] {}", line);
        }
    }
}
