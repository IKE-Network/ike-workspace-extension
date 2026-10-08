package network.ike.extension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The member's project-local repository: found and discarded only for a member built from its own root. */
class MemberCacheTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void clearProperty() {
        System.clearProperty(MemberCache.KEEP_PROPERTY);
    }

    private Path workspace(String... members) throws IOException {
        Path ws = tempDir.resolve("ws");
        Files.createDirectories(ws);
        StringBuilder manifest = new StringBuilder("schema-version: \"1.0\"\nsubprojects:\n");
        for (String member : members) {
            manifest.append("  ").append(member).append(":\n    url: https://example.invalid/").append(member).append("\n");
            Files.createDirectories(ws.resolve(member));
        }
        Files.writeString(ws.resolve(Manifests.MANIFEST_FILE), manifest.toString(), StandardCharsets.UTF_8);
        return ws;
    }

    private static Path cacheIn(Path memberRoot) throws IOException {
        Path cache = memberRoot.resolve(MemberCache.PROJECT_LOCAL_REPO);
        Files.createDirectories(cache.resolve("group/artifact/1-SNAPSHOT"));
        Files.writeString(cache.resolve("group/artifact/1-SNAPSHOT/artifact-1-SNAPSHOT.jar"), "stale", StandardCharsets.UTF_8);
        return cache;
    }

    @Test
    void aMemberBuiltFromItsOwnRootHasItsCacheDiscarded() throws IOException {
        Path ws = workspace("alpha", "beta");
        Path cache = cacheIn(ws.resolve("alpha"));
        Optional<MemberCache.Member> discarded = MemberCache.discard(ws.resolve("alpha"));
        assertTrue(discarded.isPresent());
        assertEquals("alpha", discarded.get().name());
        assertEquals(ws, discarded.get().workspaceRoot());
        assertFalse(Files.exists(cache), "the cache is gone");
        assertTrue(Files.isDirectory(ws.resolve("alpha/.mvn")), "only the cache is removed");
    }

    @Test
    void theWorkingSetRootKeepsItsCache() throws IOException {
        Path ws = workspace("alpha");
        Path cache = cacheIn(ws);
        assertTrue(MemberCache.discard(ws).isEmpty());
        assertTrue(Files.exists(cache));
    }

    @Test
    void aDirectoryTheManifestDoesNotListIsNotAMember() throws IOException {
        Path ws = workspace("alpha");
        Path stranger = ws.resolve("gamma");
        Path cache = cacheIn(stranger);
        assertTrue(MemberCache.memberOf(stranger).isEmpty());
        assertTrue(MemberCache.discard(stranger).isEmpty());
        assertTrue(Files.exists(cache));
    }

    @Test
    void aNestedDirectoryOfAMemberIsNotTheMember() throws IOException {
        Path ws = workspace("alpha");
        Path module = ws.resolve("alpha/module");
        Path cache = cacheIn(module);
        assertTrue(MemberCache.discard(module).isEmpty());
        assertTrue(Files.exists(cache));
    }

    @Test
    void aMemberWithoutACacheIsLeftAlone() throws IOException {
        Path ws = workspace("alpha");
        assertTrue(MemberCache.memberOf(ws.resolve("alpha")).isPresent());
        assertTrue(MemberCache.discard(ws.resolve("alpha")).isEmpty());
    }

    @Test
    void thePropertyKeepsTheCache() throws IOException {
        Path ws = workspace("alpha");
        Path cache = cacheIn(ws.resolve("alpha"));
        System.setProperty(MemberCache.KEEP_PROPERTY, "keep");
        assertTrue(MemberCache.discard(ws.resolve("alpha")).isEmpty());
        assertTrue(Files.exists(cache));
    }

    @Test
    void theTransformerActsOnceAndPassesTheModelThrough() throws Exception {
        Path ws = workspace("alpha");
        Path cache = cacheIn(ws.resolve("alpha"));
        String previous = System.getProperty(MemberCacheTransformer.ROOT_DIRECTORY_PROPERTY);
        System.setProperty(MemberCacheTransformer.ROOT_DIRECTORY_PROPERTY, ws.resolve("alpha").toString());
        try {
            MemberCacheTransformer transformer = new MemberCacheTransformer();
            org.apache.maven.api.model.Model model = org.apache.maven.api.model.Model.newBuilder().artifactId("x").build();
            assertEquals(model, transformer.transformFileModel(model));
            assertFalse(Files.exists(cache));
            cacheIn(ws.resolve("alpha"));
            transformer.transformFileModel(model);
            assertTrue(Files.exists(cache), "once per session");
        } finally {
            if (previous == null) {
                System.clearProperty(MemberCacheTransformer.ROOT_DIRECTORY_PROPERTY);
            } else {
                System.setProperty(MemberCacheTransformer.ROOT_DIRECTORY_PROPERTY, previous);
            }
        }
    }
}
