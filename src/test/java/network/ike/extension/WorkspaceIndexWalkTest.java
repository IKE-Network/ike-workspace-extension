package network.ike.extension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * The member walk behind {@link WorkspaceIndex} (IKE-Network/ike-issues#1163):
 * build output, {@code .git} and nested repositories are pruned, never
 * traversed, and an entry that cannot be read never fails the scan.
 */
class WorkspaceIndexWalkTest {

    @TempDir
    Path root;

    private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    private PrintStream originalErr;
    private Path locked;

    @BeforeEach
    void captureStderr() throws IOException {
        originalErr = System.err;
        System.setErr(new PrintStream(stderr, true, StandardCharsets.UTF_8));
        Files.writeString(root.resolve("workspace.yaml"), """
                subprojects:
                  lib:
                    repo: https://example.invalid/lib.git
                    branch: main
                """, StandardCharsets.UTF_8);
        pom("lib/pom.xml", "com.test", "lib", "1.0.0-SNAPSHOT");
        pom("lib/core/pom.xml", "com.test", "lib-core", "1.0.0-SNAPSHOT");
    }

    @AfterEach
    void restore() throws IOException {
        System.setErr(originalErr);
        if (locked != null) {
            Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }

    @Test
    void anUnreadableBuildDirectoryIsPrunedWithoutAWarning() throws IOException {
        pom("lib/core/target/pom.xml", "com.test", "decoy", "9");
        lock("lib/core/target");

        WorkspaceIndex index = WorkspaceIndex.scan(root);

        assertNotNull(index.produced("com.test", "lib-core"));
        assertNull(index.produced("com.test", "decoy"));
        assertEquals("", stderr.toString(StandardCharsets.UTF_8));
    }

    @Test
    void anUnreadableDirectoryElsewhereIsWarnedAboutAndTheScanContinues() throws IOException {
        pom("lib/hidden/pom.xml", "com.test", "hidden", "1.0.0-SNAPSHOT");
        lock("lib/hidden");

        WorkspaceIndex index = WorkspaceIndex.scan(root);

        assertNotNull(index.produced("com.test", "lib"), "the rest of the member is indexed");
        assertNotNull(index.produced("com.test", "lib-core"));
        assertNull(index.produced("com.test", "hidden"));
        String warning = stderr.toString(StandardCharsets.UTF_8);
        assertTrue(warning.contains("cannot read " + root.resolve("lib/hidden")), warning);
    }

    @Test
    void theWalkNeverEntersPrunedSubtrees() throws IOException {
        pom("lib/core/target/classes/pom.xml", "com.test", "build-output", "9");
        pom("lib/.mvn/target/project-local-repo/x/pom.xml", "com.test", "project-local", "9");
        pom("lib/.git/pom.xml", "com.test", "git-internal", "9");
        pom("lib/vendored/pom.xml", "com.test", "nested-repo", "9");
        Files.createDirectories(root.resolve("lib/vendored/.git"));

        List<Path> poms = WorkspaceIndex.memberPoms(root.resolve("lib"));

        assertEquals(List.of(root.resolve("lib/core/pom.xml"), root.resolve("lib/pom.xml")),
                poms.stream().sorted().toList());
    }

    @Test
    void aModuleDirectoryNamedTargetWithoutASiblingPomIsIndexed() throws IOException {
        // modules/ holds no pom.xml, so modules/target is a module, not build output.
        pom("lib/modules/target/pom.xml", "com.test", "target-module", "1.0.0-SNAPSHOT");

        WorkspaceIndex index = WorkspaceIndex.scan(root);

        assertNotNull(index.produced("com.test", "target-module"));
    }

    @Test
    void theMemberRootIsNeverPruned() {
        assertFalse(WorkspaceIndex.pruned(root.resolve("lib"), root.resolve("lib")));
    }

    private void pom(String relative, String groupId, String artifactId, String version)
            throws IOException {
        Path file = root.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, """
                <?xml version="1.0"?>
                <project>
                    <groupId>%s</groupId>
                    <artifactId>%s</artifactId>
                    <version>%s</version>
                </project>
                """.formatted(groupId, artifactId, version), StandardCharsets.UTF_8);
    }

    private void lock(String relative) throws IOException {
        locked = root.resolve(relative);
        Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString("---------"));
        assumeFalse(Files.isReadable(locked), "running as a user who can read anything");
    }
}
