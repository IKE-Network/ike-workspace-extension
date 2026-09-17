package network.ike.extension;

import org.apache.maven.api.model.Build;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Plugin;
import org.apache.maven.api.model.PluginExecution;
import org.apache.maven.api.model.PluginManagement;
import org.apache.maven.api.model.Profile;
import org.apache.maven.api.xml.XmlNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * How far the resolution rule reaches (IKE-Network/ike-issues#1019): a
 * coordinate naming a working-set member binds the same way whether it
 * is a project dependency, a dependency of a plugin, or an
 * {@code <artifactItem>} in that plugin's configuration.
 *
 * <p>These are the sites that used to require a hand-written
 * {@code <member>.version} property — the pins a sibling reactor's
 * version rewrite leaves stale.
 */
class PluginSiteBindingTest {

    private static final String DEPENDENCY_PLUGIN = "maven-dependency-plugin";

    @TempDir
    Path root;

    /**
     * A minimal set: {@code staged} is a member at a development
     * version with a recorded 4 release; {@code alsoStaged} likewise.
     * Neither is version-passed, so release mode binds the recorded
     * release and build mode binds the reactor's snapshot.
     */
    private void workspace() throws IOException {
        Files.writeString(root.resolve("workspace.yaml"), """
                subprojects:
                  staged:
                    branch: main
                  alsoStaged:
                    branch: main
                """, StandardCharsets.UTF_8);
        pom("staged", "5-SNAPSHOT");
        pom("alsoStaged", "9-SNAPSHOT");
        Path releases = Files.createDirectories(root.resolve("releases"));
        Files.writeString(releases.resolve("release-c1.yaml"), """
                members:
                  staged:
                    version: "4"
                    recorded: "2026-08-14"
                  alsoStaged:
                    version: "8"
                    recorded: "2026-08-14"
                """, StandardCharsets.UTF_8);
    }

    private void pom(String member, String version) throws IOException {
        Path dir = Files.createDirectories(root.resolve(member));
        Files.writeString(dir.resolve("pom.xml"), """
                <?xml version="1.0"?>
                <project>
                    <groupId>com.test</groupId>
                    <artifactId>%s</artifactId>
                    <version>%s</version>
                </project>
                """.formatted(member, version), StandardCharsets.UTF_8);
    }

    // ── model fixtures ──────────────────────────────────────────────

    private Model consumer(Build build) {
        return Model.newBuilder()
                .pomFile(root.resolve("consumer").resolve("pom.xml"))
                .groupId("com.test").artifactId("consumer").version("1-SNAPSHOT")
                .build(build)
                .build();
    }

    private static Dependency dep(String groupId, String artifactId,
                                  String version) {
        Dependency.Builder builder = Dependency.newBuilder()
                .groupId(groupId).artifactId(artifactId);
        if (version != null) {
            builder.version(version);
        }
        return builder.build();
    }

    private static XmlNode element(String name, String value) {
        return XmlNode.newInstance(name, value);
    }

    private static XmlNode item(String element, String groupId,
                                String artifactId, String version) {
        List<XmlNode> children = new ArrayList<>();
        children.add(element("groupId", groupId));
        children.add(element("artifactId", artifactId));
        if (version != null) {
            children.add(element("version", version));
        }
        return XmlNode.newInstance(element, children);
    }

    private static XmlNode staging(XmlNode... items) {
        return XmlNode.newInstance("configuration",
                List.of(XmlNode.newInstance("artifactItems", List.of(items))));
    }

    private static Plugin.Builder dependencyPlugin() {
        return Plugin.newBuilder()
                .groupId("org.apache.maven.plugins")
                .artifactId(DEPENDENCY_PLUGIN);
    }

    // ── reading the result back ─────────────────────────────────────

    private static Plugin onlyPlugin(Model model) {
        return model.getBuild().getPlugins().get(0);
    }

    /** The version written on the first {@code <artifactItem>}. */
    private static String stagedVersion(XmlNode configuration, int index) {
        XmlNode item = configuration.getChild("artifactItems")
                .getChildren().get(index);
        XmlNode version = item.getChild("version");
        return version == null ? null : version.getValue();
    }

    private Model transform(Model model) {
        return new IntraSetVersionTransformer().transformFileModel(model);
    }

    // ── plugin dependencies ─────────────────────────────────────────

    @Test
    void bindsAPluginsOwnDependencies() throws IOException {
        workspace();
        Model bound = transform(consumer(Build.newBuilder()
                .plugins(List.of(dependencyPlugin()
                        .dependencies(List.of(dep("com.test", "staged", null)))
                        .build()))
                .build()));

        assertEquals("5-SNAPSHOT", onlyPlugin(bound)
                .getDependencies().get(0).getVersion());
    }

    @Test
    void bindsPluginDependenciesUnderPluginManagement() throws IOException {
        workspace();
        Model bound = transform(consumer(Build.newBuilder()
                .pluginManagement(PluginManagement.newBuilder()
                        .plugins(List.of(dependencyPlugin()
                                .dependencies(List.of(
                                        dep("com.test", "staged", null)))
                                .build()))
                        .build())
                .build()));

        assertEquals("5-SNAPSHOT", bound.getBuild().getPluginManagement()
                .getPlugins().get(0).getDependencies().get(0).getVersion());
    }

    @Test
    void bindsPluginDependenciesInsideAProfile() throws IOException {
        workspace();
        Model model = Model.newBuilder()
                .pomFile(root.resolve("consumer").resolve("pom.xml"))
                .groupId("com.test").artifactId("consumer").version("1-SNAPSHOT")
                .profiles(List.of(Profile.newBuilder()
                        .id("image")
                        .build(Build.newBuilder()
                                .plugins(List.of(dependencyPlugin()
                                        .dependencies(List.of(
                                                dep("com.test", "staged", null)))
                                        .build()))
                                .build())
                        .build()))
                .build();

        Model bound = transform(model);

        assertEquals("5-SNAPSHOT", bound.getProfiles().get(0).getBuild()
                .getPlugins().get(0).getDependencies().get(0).getVersion());
    }

    @Test
    void releaseModeBindsPluginDependenciesToTheRecordedRelease()
            throws IOException {
        workspace();
        System.setProperty(
                IntraSetVersionTransformer.RELEASE_MODE_PROPERTY, "true");
        try {
            Model bound = transform(consumer(Build.newBuilder()
                    .plugins(List.of(dependencyPlugin()
                            .dependencies(List.of(
                                    dep("com.test", "staged", null)))
                            .build()))
                    .build()));

            // The member is not version-passed this mission, so a
            // released consumer POM must not carry its snapshot.
            assertEquals("4", onlyPlugin(bound)
                    .getDependencies().get(0).getVersion());
        } finally {
            System.clearProperty(
                    IntraSetVersionTransformer.RELEASE_MODE_PROPERTY);
        }
    }

    // ── artifactItems ───────────────────────────────────────────────

    @Test
    void bindsArtifactItemsInPluginConfiguration() throws IOException {
        workspace();
        Model bound = transform(consumer(Build.newBuilder()
                .plugins(List.of(dependencyPlugin()
                        .configuration(staging(
                                item("artifactItem", "com.test", "staged", null),
                                item("artifactItem", "com.test", "alsoStaged",
                                        null)))
                        .build()))
                .build()));

        XmlNode configuration = onlyPlugin(bound).getConfiguration();
        assertEquals("5-SNAPSHOT", stagedVersion(configuration, 0));
        assertEquals("9-SNAPSHOT", stagedVersion(configuration, 1));
    }

    @Test
    void bindsArtifactItemsInAnExecutionsConfiguration() throws IOException {
        workspace();
        Model bound = transform(consumer(Build.newBuilder()
                .plugins(List.of(dependencyPlugin()
                        .executions(List.of(PluginExecution.newBuilder()
                                .id("fetch-standard-application-extensions")
                                .configuration(staging(item("artifactItem",
                                        "com.test", "staged", null)))
                                .build()))
                        .build()))
                .build()));

        assertEquals("5-SNAPSHOT", stagedVersion(onlyPlugin(bound)
                .getExecutions().get(0).getConfiguration(), 0));
    }

    @Test
    void leavesADeliberatePinAlone() throws IOException {
        workspace();
        Model bound = transform(consumer(Build.newBuilder()
                .plugins(List.of(dependencyPlugin()
                        .configuration(staging(item("artifactItem",
                                "com.test", "staged", "2")))
                        .build()))
                .build()));

        assertEquals("2", stagedVersion(onlyPlugin(bound).getConfiguration(), 0));
    }

    @Test
    void leavesForeignCoordinatesAlone() throws IOException {
        workspace();
        Model model = consumer(Build.newBuilder()
                .plugins(List.of(dependencyPlugin()
                        .dependencies(List.of(dep("org.other", "thing", null)))
                        .configuration(staging(item("artifactItem",
                                "org.other", "thing", null)))
                        .build()))
                .build());

        Model bound = transform(model);

        // Nothing to say: the very instance Maven handed over.
        assertSame(model, bound);
        assertNull(stagedVersion(onlyPlugin(bound).getConfiguration(), 0));
    }

    @Test
    void ignoresCoordinatesOutsideAnArtifactItem() throws IOException {
        workspace();
        // Other plugins model a groupId/artifactId pair with no version
        // field at all; inserting one would fail on an unknown parameter.
        Model model = consumer(Build.newBuilder()
                .plugins(List.of(dependencyPlugin()
                        .configuration(XmlNode.newInstance("configuration",
                                List.of(item("bannedDependency",
                                        "com.test", "staged", null))))
                        .build()))
                .build());

        assertSame(model, transform(model));
    }

    @Test
    void leavesPomsOutsideAWorkingSetAlone() throws IOException {
        // No workspace.yaml written: nothing above the POM is a set.
        Model model = consumer(Build.newBuilder()
                .plugins(List.of(dependencyPlugin()
                        .dependencies(List.of(dep("com.test", "staged", null)))
                        .build()))
                .build());

        assertSame(model, transform(model));
    }
}
