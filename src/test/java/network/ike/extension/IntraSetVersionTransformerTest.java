package network.ike.extension;

import org.apache.maven.api.model.Dependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The resolution rule as a pure decision matrix
 * (IKE-Network/ike-issues#1019): build mode always binds the reactor's
 * current version; release mode binds reactor-current for members the
 * version pass de-qualified and the recorded released version for
 * everyone else; explicit versions and foreign coordinates pass
 * through untouched.
 */
class IntraSetVersionTransformerTest {

    @TempDir
    Path root;

    /**
     * A minimal set: {@code changed} was version-passed to 3.0.0;
     * {@code bystander} sits at its development version with a
     * recorded 1.4.0 release; {@code fresh} has never released.
     */
    private WorkspaceIndex index() throws IOException {
        Files.writeString(root.resolve("workspace.yaml"), """
                subprojects:
                  changed:
                    branch: main
                  bystander:
                    branch: main
                  fresh:
                    branch: main
                """, StandardCharsets.UTF_8);
        pom("changed", "3.0.0");
        pom("bystander", "1.5.0-SNAPSHOT");
        pom("fresh", "0.1.0-SNAPSHOT");
        Path releases = Files.createDirectories(root.resolve("releases"));
        Files.writeString(releases.resolve("release-c1.yaml"), """
                members:
                  bystander:
                    version: "1.4.0"
                    recorded: "2026-08-14"
                """, StandardCharsets.UTF_8);
        return WorkspaceIndex.scan(root);
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

    private static Dependency dep(String artifactId, String version) {
        Dependency.Builder builder = Dependency.newBuilder()
                .groupId("com.test").artifactId(artifactId);
        if (version != null) {
            builder.version(version);
        }
        return builder.build();
    }

    @Test
    void buildModeBindsTheReactorCurrentVersion() throws IOException {
        WorkspaceIndex index = index();
        assertEquals("1.5.0-SNAPSHOT", IntraSetVersionTransformer.resolve(
                dep("bystander", null), index, false));
        assertEquals("0.1.0-SNAPSHOT", IntraSetVersionTransformer.resolve(
                dep("fresh", null), index, false));
    }

    @Test
    void releaseModeSplitsByWhatTheVersionPassDid() throws IOException {
        WorkspaceIndex index = index();
        // Version-passed member: releasing — bind reactor-current.
        assertEquals("3.0.0", IntraSetVersionTransformer.resolve(
                dep("changed", null), index, true));
        // Development-version member: unchanged — bind its release.
        assertEquals("1.4.0", IntraSetVersionTransformer.resolve(
                dep("bystander", null), index, true));
    }

    @Test
    void neverReleasedBystanderIsLeftForThePreflightToRefuse()
            throws IOException {
        WorkspaceIndex index = index();
        assertNull(IntraSetVersionTransformer.resolve(
                dep("fresh", null), index, true));
    }


    @Test
    void siblingIdiomBindsThroughTheUniqueArtifactId() throws IOException {
        WorkspaceIndex index = index();
        Dependency sibling = Dependency.newBuilder()
                .groupId("${project.groupId}").artifactId("bystander").build();
        assertEquals("1.5.0-SNAPSHOT", IntraSetVersionTransformer.resolve(
                sibling, index, false));
    }

    @Test
    void explicitVersionsAndForeignCoordinatesPassThrough()
            throws IOException {
        WorkspaceIndex index = index();
        // A deliberate released pin stays exactly as declared.
        assertNull(IntraSetVersionTransformer.resolve(
                dep("bystander", "1.3.0"), index, false));
        // A property expression is an explicit version, not a blank.
        assertNull(IntraSetVersionTransformer.resolve(
                dep("changed", "${some.version}"), index, true));
        // A coordinate the set does not produce is never touched.
        assertNull(IntraSetVersionTransformer.resolve(
                dep("external-thing", null), index, false));
    }
}
