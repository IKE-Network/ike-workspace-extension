package network.ike.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Coverage for {@link WorkspaceIndex} against a filesystem fixture
 * shaped like a real working set (IKE-Network/ike-issues#1019):
 * members with sub-modules that inherit their version from a parent
 * block, a {@code target/} decoy, a nested-repository trap, and mission
 * records where a later record supersedes an earlier one.
 */
class WorkspaceIndexTest {

    @TempDir
    Path root;

    private WorkspaceIndex scanned() throws IOException {
        Files.writeString(root.resolve("workspace.yaml"), """
                subprojects:
                  core-lib:
                    repo: https://example.invalid/core-lib.git
                    branch: main
                # a column-zero comment between members must not end
                # the list — the real manifest carries these
                  app:
                    repo: https://example.invalid/app.git
                    branch: main
                """, StandardCharsets.UTF_8);

        // core-lib: a root POM and a sub-module that inherits its
        // version through its parent block — the owl-extension shape.
        write("core-lib/pom.xml", """
                <?xml version="1.0"?>
                <project>
                    <groupId>com.test</groupId>
                    <artifactId>core-lib</artifactId>
                    <version>2.5.0-SNAPSHOT</version>
                </project>
                """);
        write("core-lib/ext/pom.xml", """
                <?xml version="1.0"?>
                <project>
                    <parent>
                        <groupId>com.test.ext</groupId>
                        <artifactId>ext-parent</artifactId>
                        <version>2.5.0-SNAPSHOT</version>
                    </parent>
                    <artifactId>owl-ext</artifactId>
                </project>
                """);
        // A Maven 4.1 inference module: empty <parent/>, no version,
        // own groupId — the owl-extension shape that broke the sort.
        write("core-lib/inferred/pom.xml", """
                <?xml version="1.0"?>
                <project>
                    <modelVersion>4.1.0</modelVersion>
                    <parent/>
                    <groupId>com.test.inferred</groupId>
                    <artifactId>inferred-ext</artifactId>
                </project>
                """);
        // Decoys: a target/ copy and a nested repository.
        write("core-lib/target/pom.xml", """
                <?xml version="1.0"?>
                <project>
                    <groupId>com.test</groupId>
                    <artifactId>decoy-target</artifactId>
                    <version>9.9.9</version>
                </project>
                """);
        write("core-lib/vendored/pom.xml", """
                <?xml version="1.0"?>
                <project>
                    <groupId>com.test</groupId>
                    <artifactId>decoy-nested-repo</artifactId>
                    <version>9.9.9</version>
                </project>
                """);
        Files.createDirectories(root.resolve("core-lib/vendored/.git"));

        write("app/pom.xml", """
                <?xml version="1.0"?>
                <project>
                    <groupId>com.test</groupId>
                    <artifactId>app</artifactId>
                    <version>1.1.0-SNAPSHOT</version>
                </project>
                """);

        // Two mission records; the later one supersedes for core-lib.
        write("releases/release-mission-1.yaml", """
                members:
                  core-lib:
                    version: "2.3.0"
                    tag: "2.3.0"
                    recorded: "2026-08-01"
                  app:
                    version: "1.0.0"
                    tag: "1.0.0"
                    recorded: "2026-08-01"
                """);
        write("releases/release-mission-2.yaml", """
                members:
                  core-lib:
                    version: "2.4.0"
                    tag: "2.4.0"
                    recorded: "2026-08-14"
                """);
        return WorkspaceIndex.scan(root);
    }

    private void write(String rel, String content) throws IOException {
        Path p = root.resolve(rel);
        Files.createDirectories(p.getParent());
        Files.writeString(p, content, StandardCharsets.UTF_8);
    }

    @Test
    void producesEveryModuleIncludingParentVersionInheritors()
            throws IOException {
        WorkspaceIndex index = scanned();
        assertEquals("core-lib",
                index.produced("com.test", "core-lib").member());
        assertEquals("2.5.0-SNAPSHOT",
                index.produced("com.test", "core-lib").currentVersion());
        // The sub-module inherits groupId and version from its parent
        // block — both must resolve.
        assertEquals("core-lib",
                index.produced("com.test.ext", "owl-ext").member());
        assertEquals("2.5.0-SNAPSHOT",
                index.produced("com.test.ext", "owl-ext").currentVersion());
        // app is declared AFTER the column-zero comment: its presence
        // proves the comment did not end the member list.
        assertEquals("app", index.produced("com.test", "app").member());
    }


    @Test
    void inferenceModulesInheritVersionFromTheDirectoryAncestor()
            throws IOException {
        WorkspaceIndex index = scanned();
        assertEquals("core-lib",
                index.produced("com.test.inferred", "inferred-ext").member());
        assertEquals("2.5.0-SNAPSHOT", index.produced(
                "com.test.inferred", "inferred-ext").currentVersion());
    }

    @Test
    void decoysDoNotEnterTheIndex() throws IOException {
        WorkspaceIndex index = scanned();
        assertNull(index.produced("com.test", "decoy-target"));
        assertNull(index.produced("com.test", "decoy-nested-repo"));
        assertNull(index.produced("com.test", "not-produced-here"));
    }

    @Test
    void latestRecordWinsPerMember() throws IOException {
        WorkspaceIndex index = scanned();
        assertEquals("2.4.0", index.releasedVersion("core-lib"));
        // app appears only in the first record.
        assertEquals("1.0.0", index.releasedVersion("app"));
        assertNull(index.releasedVersion("never-released"));
    }

    /**
     * Two missions recorded on the same date — mission 4 ran past midnight
     * with mission 3's date and every bystander bound one generation
     * stale (ike-issues#1026). The record's numeric mission suffix (the
     * monotonic root version counter) breaks the tie.
     */
    @Test
    void sameDateRecordsResolveToTheHigherCycleNumber()
            throws IOException {
        scanned();
        write("releases/release-mission-3.yaml", """
                members:
                  core-lib:
                    version: "2.5.0"
                    tag: "2.5.0"
                    recorded: "2026-08-15"
                """);
        write("releases/release-mission-4.yaml", """
                members:
                  core-lib:
                    version: "2.6.0"
                    tag: "2.6.0"
                    recorded: "2026-08-15"
                """);
        WorkspaceIndex index = WorkspaceIndex.scan(root);
        assertEquals("2.6.0", index.releasedVersion("core-lib"));
    }

    /**
     * The manifest is read under either name: a working set the scaffold
     * has upgraded carries {@code working-set.yaml}, one it has not still
     * carries {@code workspace.yaml} (ike-issues#1054).
     */
    @Test
    void manifestIsReadUnderEitherName() throws IOException {
        String manifest = """
                subprojects:
                  core-lib:
                    repo: https://example.invalid/core-lib.git
                """;
        Files.writeString(root.resolve("working-set.yaml"), manifest,
                StandardCharsets.UTF_8);
        assertEquals(java.util.List.of("core-lib"),
                WorkspaceIndex.manifestMembers(Manifests.in(root)));

        Files.delete(root.resolve("working-set.yaml"));
        Files.writeString(root.resolve("workspace.yaml"), manifest,
                StandardCharsets.UTF_8);
        assertEquals(java.util.List.of("core-lib"),
                WorkspaceIndex.manifestMembers(Manifests.in(root)));
    }

    /** Neither name present is simply not a working-set root. */
    @Test
    void noManifestIsNoMembers() {
        assertNull(Manifests.in(root));
        assertEquals(java.util.List.of(),
                WorkspaceIndex.manifestMembers(Manifests.in(root)));
    }
}
