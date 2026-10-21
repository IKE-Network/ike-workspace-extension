package network.ike.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The missing-module diagnosis, synthesized from fake failures in a fake tree. */
class MissingModuleDiagnosisTest {

    private static final String SPINED = "dev.ikm.tinkar.provider.spinedarray";

    @TempDir
    Path tree;

    private final Instant start = Instant.now().minusSeconds(120);

    private Instant at(int seconds) {
        return start.plusSeconds(seconds);
    }

    private MissingModuleDiagnosis.Module module(String name, String moduleName,
            Optional<MissingModuleDiagnosis.Window> compiled) throws IOException {
        Path basedir = tree.resolve(name);
        Path sources = basedir.resolve("src/main/java");
        Files.createDirectories(sources);
        Files.createDirectories(basedir.resolve("target/classes"));
        if (moduleName != null) {
            Files.writeString(sources.resolve("module-info.java"),
                    "/* header */\nmodule " + moduleName + " {\n}\n", StandardCharsets.UTF_8);
        }
        return new MissingModuleDiagnosis.Module("dev.ikm.tinkar:" + name, name, basedir,
                basedir.resolve("target"), basedir.resolve("target/classes"), List.of(sources), true, compiled);
    }

    private static void writeModuleInfo(MissingModuleDiagnosis.Module module, String name, Instant written)
            throws IOException {
        Path file = module.outputDirectory().resolve("module-info.class");
        Files.write(file, moduleInfoBytes(name));
        Files.setLastModifiedTime(file, FileTime.from(written));
    }

    /** A real module-info.class, so the diagnosis reads names the way it does in a build. */
    private static byte[] moduleInfoBytes(String name) {
        // The JDK has no public descriptor writer; compile one instead.
        try {
            Path dir = Files.createTempDirectory("module-info");
            Path source = dir.resolve("module-info.java");
            Files.writeString(source, "module " + name + " {}\n");
            int status = javax.tools.ToolProvider.getSystemJavaCompiler()
                    .run(null, null, null, "-d", dir.toString(), source.toString());
            assertEquals(0, status);
            return Files.readAllBytes(dir.resolve("module-info.class"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private MissingModuleDiagnosis.Input input(MissingModuleDiagnosis.Module failing, Instant failingStart,
            String mojo, List<MissingModuleDiagnosis.Missing> missing, List<MissingModuleDiagnosis.Module> modules,
            List<MissingModuleDiagnosis.Resolved> resolved) {
        return new MissingModuleDiagnosis.Input(failing, failingStart, mojo, missing, modules, resolved,
                Optional.of(tree.resolve("m2")), start, "build lock held by this session (pid 1)",
                List.of("pid 777 (running 05:00) IntelliJ build process: java org.jetbrains.jps.cmdline.BuildMain"));
    }

    private static String text(List<String> lines) {
        return String.join("\n", lines);
    }

    @Test
    void javacModuleNotFoundIsReadFromTheCompilerFailure() {
        String failure = "Compilation failure\n/x/test-fixtures/src/main/java/module-info.java:[4,30] module not found: "
                + SPINED + "\n/x/module-info.java:[5,30] module not found: dev.ikm.tinkar.common\n";
        assertEquals(List.of(new MissingModuleDiagnosis.MissingModule(SPINED),
                        new MissingModuleDiagnosis.MissingModule("dev.ikm.tinkar.common")),
                MissingModuleDiagnosis.modulesNotFound(failure));
    }

    @Test
    void aClassDirectoryEmptiedAfterThisSessionCompiledItNamesAnOutsideWriter() throws IOException {
        MissingModuleDiagnosis.Module spined = module("spinedarray-provider", SPINED,
                Optional.of(new MissingModuleDiagnosis.Window(at(10), at(14))));
        MissingModuleDiagnosis.Module fixtures = module("test-fixtures", "dev.ikm.tinkar.fixtures", Optional.empty());
        // module-info.class absent now: someone removed it after this session wrote it.
        List<String> lines = MissingModuleDiagnosis.render(input(fixtures, at(30), "compiler:compile",
                List.of(new MissingModuleDiagnosis.MissingModule(SPINED)), List.of(spined, fixtures),
                List.of(new MissingModuleDiagnosis.Resolved(spined.id(), spined.outputDirectory()))));
        String text = text(lines);
        assertTrue(text.contains("javac: module not found: " + SPINED), text);
        assertTrue(text.contains("reactor module spinedarray-provider (dev.ikm.tinkar:spinedarray-provider), in this build"), text);
        assertTrue(text.contains("module-info.class absent now"), text);
        assertTrue(text.contains("this session compiled it +10s into the build to +14s into the build"), text);
        assertTrue(text.contains("=> gone since this session compiled it: removed by another writer"), text);
        assertTrue(text.contains("(the reactor class directory)"), text);
        assertTrue(text.contains("IntelliJ build process"), text);
        assertTrue(!text.contains("MNG-8096"), "not an ordering problem: " + text);
    }

    @Test
    void aClassDirectoryRewrittenOutsideTheCompileWindowNamesAnOutsideWriter() throws IOException {
        MissingModuleDiagnosis.Module spined = module("spinedarray-provider", SPINED,
                Optional.of(new MissingModuleDiagnosis.Window(at(10), at(14))));
        writeModuleInfo(spined, SPINED, at(25));
        MissingModuleDiagnosis.Module fixtures = module("test-fixtures", null, Optional.empty());
        String text = text(MissingModuleDiagnosis.render(input(fixtures, at(30), "compiler:compile",
                List.of(new MissingModuleDiagnosis.MissingModule(SPINED)), List.of(spined, fixtures),
                List.of(new MissingModuleDiagnosis.Resolved(spined.id(), spined.outputDirectory())))));
        assertTrue(text.contains("module-info.class present, written "), text);
        assertTrue(text.contains("(+25s into the build)"), text);
        assertTrue(text.contains("=> written during this build, outside this session's compile of it: another writer"), text);
        assertTrue(text.contains("provided now by dev.ikm.tinkar:spinedarray-provider"), text);
    }

    @Test
    void aCompileThatStartedBeforeItsDependencyFinishedNamesMavensOrdering() throws IOException {
        MissingModuleDiagnosis.Module spined = module("spinedarray-provider", SPINED,
                Optional.of(new MissingModuleDiagnosis.Window(at(10), at(14))));
        writeModuleInfo(spined, SPINED, at(13));
        MissingModuleDiagnosis.Module fixtures = module("test-fixtures", null, Optional.empty());
        String text = text(MissingModuleDiagnosis.render(input(fixtures, at(11), "compiler:compile",
                List.of(new MissingModuleDiagnosis.MissingModule(SPINED)), List.of(spined, fixtures),
                List.of(new MissingModuleDiagnosis.Resolved(spined.id(), spined.outputDirectory())))));
        assertTrue(text.contains("test-fixtures started before this session finished compiling spinedarray-provider: "
                + "Maven's ordering (MNG-8096-style)"), text);
        assertTrue(!text.contains("another writer"), text);
    }

    @Test
    void aSiblingInTheBuildResolvedFromTheProjectLocalRepositoryNamesMavensResolution() throws IOException {
        MissingModuleDiagnosis.Module spined = module("spinedarray-provider", SPINED,
                Optional.of(new MissingModuleDiagnosis.Window(at(10), at(14))));
        writeModuleInfo(spined, SPINED, at(12));
        MissingModuleDiagnosis.Module fixtures = module("test-fixtures", null, Optional.empty());
        Path cached = tree.resolve(".mvn/target/project-local-repo/dev/ikm/tinkar/spinedarray-provider/1-SNAPSHOT/"
                + "spinedarray-provider-1-SNAPSHOT.jar");
        Files.createDirectories(cached.getParent());
        Files.write(cached, new byte[0]);
        String text = text(MissingModuleDiagnosis.render(input(fixtures, at(30), "compiler:compile",
                List.of(new MissingModuleDiagnosis.MissingModule(SPINED)), List.of(spined, fixtures),
                List.of(new MissingModuleDiagnosis.Resolved(spined.id(), cached)))));
        assertTrue(text.contains("(the project-local repository)"), text);
        assertTrue(text.contains("=> resolved outside the reactor although the module is in this build (MNG-8096-style)"), text);
        assertTrue(text.contains("no resolved dependency of test-fixtures provides module " + SPINED), text);
    }

    @Test
    void aTestForkMissingASiblingClassIsTracedToItsOwner() throws IOException {
        MissingModuleDiagnosis.Module common = module("common", "dev.ikm.tinkar.common",
                Optional.of(new MissingModuleDiagnosis.Window(at(5), at(8))));
        Path source = common.sourceRoots().get(0).resolve("dev/ikm/tinkar/common/Stamp.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "package dev.ikm.tinkar.common; class Stamp {}");
        Path classFile = common.outputDirectory().resolve("dev/ikm/tinkar/common/Stamp$Builder.class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, new byte[0]);
        Files.setLastModifiedTime(classFile, FileTime.from(at(40)));
        MissingModuleDiagnosis.Module kview = module("kview", null, Optional.empty());
        Path reports = kview.buildDirectory().resolve("surefire-reports");
        Files.createDirectories(reports);
        Path report = reports.resolve("dev.ikm.komet.kview.ViewTest.txt");
        Files.writeString(report, "Tests run: 3, Failures: 0, Errors: 1\n"
                + "java.lang.NoClassDefFoundError: dev/ikm/tinkar/common/Stamp$Builder\n\tat ...\n");
        Path old = reports.resolve("old.txt");
        Files.writeString(old, "java.lang.NoClassDefFoundError: dev/ikm/Stale\n");
        Files.setLastModifiedTime(old, FileTime.from(start.minusSeconds(3600)));

        List<MissingModuleDiagnosis.Missing> missing = MissingModuleDiagnosis.missingClassesInReports(
                List.of(reports, kview.buildDirectory().resolve("failsafe-reports")), start, 5);
        assertEquals(List.of(new MissingModuleDiagnosis.MissingClass("dev.ikm.tinkar.common.Stamp$Builder",
                Optional.of(report))), missing, "only this session's reports count");

        String text = text(MissingModuleDiagnosis.render(input(kview, at(50), "surefire:test", missing,
                List.of(common, kview),
                List.of(new MissingModuleDiagnosis.Resolved(common.id(), tree.resolve("m2/dev/ikm/common.jar"))))));
        assertTrue(text.contains("test fork: class not found: dev.ikm.tinkar.common.Stamp$Builder "
                + "(in dev.ikm.komet.kview.ViewTest.txt)"), text);
        assertTrue(text.contains("reactor module common (dev.ikm.tinkar:common), in this build"), text);
        assertTrue(text.contains("=> written during this build, outside this session's compile of it: another writer"), text);
        assertTrue(text.contains("(the local repository)"), text);
    }

    @Test
    void aClassDirectoryAnIncrementalCompileLeftAloneIsNotBlamedOnAWriter() throws IOException {
        MissingModuleDiagnosis.Module spined = module("spinedarray-provider", SPINED,
                Optional.of(new MissingModuleDiagnosis.Window(at(10), at(14))));
        writeModuleInfo(spined, SPINED, start.minusSeconds(600));
        MissingModuleDiagnosis.Module fixtures = module("test-fixtures", null, Optional.empty());
        String text = text(MissingModuleDiagnosis.render(input(fixtures, at(30), "compiler:compile",
                List.of(new MissingModuleDiagnosis.MissingModule(SPINED)), List.of(spined, fixtures),
                List.of(new MissingModuleDiagnosis.Resolved(spined.id(), spined.outputDirectory())))));
        assertTrue(text.contains("=> unchanged since before this build: its compile found nothing stale"), text);
        assertTrue(!text.contains("another writer"), text);
    }

    @Test
    void aClassNoReactorModuleOwnsSaysSo() throws IOException {
        MissingModuleDiagnosis.Module kview = module("kview", null, Optional.empty());
        String text = text(MissingModuleDiagnosis.render(input(kview, at(50), "surefire:test",
                List.of(new MissingModuleDiagnosis.MissingClass("org.example.Gone", Optional.empty())),
                List.of(kview), List.of())));
        assertTrue(text.contains("no reactor module owns org.example.Gone"), text);
    }
}
