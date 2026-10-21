package network.ike.extension;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * What a build knew about a reactor sibling at the moment a module could not find it.
 *
 * <p><b>Why.</b> Two failures of parallel working-set builds that passed on rerun — a
 * {@code NoClassDefFoundError} for a built sibling's class in a test fork (2026-10-12) and a
 * javac {@code module not found} for a declared dependency (2026-10-21) — have two candidate
 * causes that leave the same message: another writer (IntelliJ, another session's build)
 * deleting or rewriting the sibling's {@code target/classes} mid-build, or Maven 4 handing the
 * failing module the sibling from the wrong place or too early (MNG-8096, MNG-8028). This
 * renders, for the sibling, the evidence that tells them apart:
 *
 * <ul>
 *   <li>its {@code target/classes} and whether the class or {@code module-info.class} is there,
 *       with its last write relative to the build's start and to this session's own compile of
 *       the sibling — a write outside that window is another writer's;</li>
 *   <li>the file the failing module's dependency resolved to, classified as the reactor class
 *       directory, a packaged jar, the project-local repository, or the local repository;</li>
 *   <li>whether the failing compile started before this session finished compiling the sibling
 *       — Maven's ordering, not an outside writer;</li>
 *   <li>the tree's build-lock holder and other processes working in the tree.</li>
 * </ul>
 *
 * <p>Pure: the session spy adapts Maven's objects into the records here, so the rendering is
 * testable from a fake failure.
 */
final class MissingModuleDiagnosis {

    /** A write this close to a compile window's edge counts as inside it. */
    static final Duration SLACK = Duration.ofSeconds(2);

    private static final Pattern MODULE_NOT_FOUND = Pattern.compile("module not found: ([\\w.]+)");
    private static final Pattern MISSING_CLASS = Pattern.compile(
            "(?:NoClassDefFoundError|ClassNotFoundException)(?::|\\s+)\\s*(?:Could not initialize class\\s+)?"
                    + "([A-Za-z_$][\\w$]*(?:[./][A-Za-z_$][\\w$]*)+)");
    private static final Pattern MODULE_DECLARATION = Pattern.compile(
            "(?m)^\\s*(?:@[\\w.]+(?:\\([^)]*\\))?\\s*)*(?:open\\s+)?module\\s+([\\w.]+)\\s*\\{");

    private MissingModuleDiagnosis() {}

    /** When this session compiled a module. */
    record Window(Instant start, Instant end) {
        boolean contains(Instant time) {
            return !time.isBefore(start.minus(SLACK)) && !time.isAfter(end.plus(SLACK));
        }
    }

    /**
     * A reactor project, as the session saw it.
     *
     * @param id              {@code groupId:artifactId}
     * @param label           how to name it in a log line
     * @param basedir         its directory
     * @param buildDirectory  its {@code target}
     * @param outputDirectory its {@code target/classes}
     * @param sourceRoots     its main source roots
     * @param inBuild         whether this session builds it, or only knows it
     * @param compiled        when this session compiled it, if it did
     */
    record Module(String id, String label, Path basedir, Path buildDirectory, Path outputDirectory,
            List<Path> sourceRoots, boolean inBuild, Optional<Window> compiled) {}

    /** A dependency of the failing module and the file it resolved to. */
    record Resolved(String id, Path file) {}

    /** What could not be found. */
    sealed interface Missing permits MissingModule, MissingClass {
        String name();
    }

    /** javac: {@code module not found: name}. */
    record MissingModule(String name) implements Missing {}

    /** A test fork: {@code NoClassDefFoundError} or {@code ClassNotFoundException} for a class. */
    record MissingClass(String name, Optional<Path> report) implements Missing {}

    /** Where a resolved file lives. */
    enum Origin {
        REACTOR_CLASSES("the reactor class directory"),
        REACTOR_PACKAGE("the reactor module's packaged jar"),
        PROJECT_LOCAL_REPOSITORY("the project-local repository"),
        LOCAL_REPOSITORY("the local repository"),
        ELSEWHERE("outside the reactor and the repositories");

        final String description;

        Origin(String description) {
            this.description = description;
        }
    }

    /**
     * Everything one diagnosis reads.
     *
     * @param failing      the module that failed
     * @param failingStart when the failing mojo started
     * @param mojo         the failing mojo, {@code plugin:goal}
     * @param missing      what it could not find
     * @param modules      every reactor project the session knows
     * @param resolved     the failing module's dependencies and their files
     * @param localRepository the local repository's base, if known
     * @param buildStart   when the session started
     * @param lock         one line on the tree's build lock
     * @param writers      other processes working in the tree
     */
    record Input(Module failing, Instant failingStart, String mojo, List<Missing> missing, List<Module> modules,
            List<Resolved> resolved, Optional<Path> localRepository, Instant buildStart, String lock,
            List<String> writers) {}

    // ---- detection ------------------------------------------------------------------------

    /** The module names javac reported missing in a failure's text. */
    static List<Missing> modulesNotFound(String text) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = MODULE_NOT_FOUND.matcher(text == null ? "" : text);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names.stream().<Missing>map(MissingModule::new).toList();
    }

    /** The classes a text reports missing, as binary names. */
    static Set<String> missingClasses(String text) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = MISSING_CLASS.matcher(text == null ? "" : text);
        while (matcher.find()) {
            names.add(matcher.group(1).replace('/', '.'));
        }
        return names;
    }

    /**
     * The classes test reports written since {@code since} name as missing.
     *
     * @param reportDirectories {@code target/surefire-reports} and the like
     * @param since             the session's start; older reports are another build's
     * @param limit             at most this many classes
     */
    static List<Missing> missingClassesInReports(List<Path> reportDirectories, Instant since, int limit) {
        List<Missing> found = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Path directory : reportDirectories) {
            if (!Files.isDirectory(directory)) {
                continue;
            }
            List<Path> reports;
            try (Stream<Path> files = Files.list(directory)) {
                reports = files.filter(Files::isRegularFile)
                        .filter(file -> modifiedSince(file, since))
                        .sorted()
                        .toList();
            } catch (IOException e) {
                continue;
            }
            for (Path report : reports) {
                String text;
                try {
                    text = Files.readString(report, StandardCharsets.UTF_8);
                } catch (IOException | RuntimeException e) {
                    continue;
                }
                for (String name : missingClasses(text)) {
                    if (seen.add(name)) {
                        found.add(new MissingClass(name, Optional.of(report)));
                        if (found.size() >= limit) {
                            return found;
                        }
                    }
                }
            }
        }
        return found;
    }

    // ---- facts ----------------------------------------------------------------------------

    /** The JPMS name a reactor module declares, from its compiled descriptor or its source. */
    static Optional<String> moduleName(Module module) {
        Path descriptor = module.outputDirectory().resolve("module-info.class");
        if (Files.isRegularFile(descriptor)) {
            try (InputStream in = Files.newInputStream(descriptor)) {
                return Optional.of(ModuleDescriptor.read(in).name());
            } catch (IOException | RuntimeException e) {
                // Fall through to the source.
            }
        }
        for (Path root : module.sourceRoots()) {
            Path source = root.resolve("module-info.java");
            if (Files.isRegularFile(source)) {
                try {
                    Matcher matcher = MODULE_DECLARATION.matcher(
                            stripComments(Files.readString(source, StandardCharsets.UTF_8)));
                    if (matcher.find()) {
                        return Optional.of(matcher.group(1));
                    }
                } catch (IOException | RuntimeException e) {
                    // Unreadable: no name.
                }
            }
        }
        return Optional.empty();
    }

    /** The reactor module a binary class name belongs to, by its class file or its source. */
    static Optional<Module> owner(String className, List<Module> modules) {
        String path = className.replace('.', '/');
        int nested = path.indexOf('$');
        String outer = nested < 0 ? path : path.substring(0, nested);
        for (Module module : modules) {
            if (Files.isRegularFile(module.outputDirectory().resolve(path + ".class"))) {
                return Optional.of(module);
            }
        }
        for (Module module : modules) {
            for (Path root : module.sourceRoots()) {
                if (Files.isRegularFile(root.resolve(outer + ".java"))) {
                    return Optional.of(module);
                }
            }
        }
        return Optional.empty();
    }

    /** Where a resolved file lives. */
    static Origin origin(Path file, List<Module> modules, Optional<Path> localRepository) {
        Path normal = file.toAbsolutePath().normalize();
        for (Module module : modules) {
            if (normal.equals(module.outputDirectory().toAbsolutePath().normalize())) {
                return Origin.REACTOR_CLASSES;
            }
        }
        for (Module module : modules) {
            if (normal.startsWith(module.buildDirectory().toAbsolutePath().normalize())) {
                return Origin.REACTOR_PACKAGE;
            }
        }
        if (normal.toString().contains("/" + MemberCache.PROJECT_LOCAL_REPO + "/")) {
            return Origin.PROJECT_LOCAL_REPOSITORY;
        }
        if (localRepository.isPresent() && normal.startsWith(localRepository.get().toAbsolutePath().normalize())) {
            return Origin.LOCAL_REPOSITORY;
        }
        return Origin.ELSEWHERE;
    }

    /** The JPMS name a resolved file provides, explicit or automatic. */
    static Optional<String> providedModule(Path file) {
        try {
            if (Files.isDirectory(file)) {
                Path descriptor = file.resolve("module-info.class");
                if (!Files.isRegularFile(descriptor)) {
                    return Optional.empty();
                }
                try (InputStream in = Files.newInputStream(descriptor)) {
                    return Optional.of(ModuleDescriptor.read(in).name());
                }
            }
            if (Files.isRegularFile(file)) {
                return ModuleFinder.of(file).findAll().stream()
                        .map(ModuleReference::descriptor)
                        .map(ModuleDescriptor::name)
                        .findFirst();
            }
        } catch (IOException | RuntimeException e) {
            // Unreadable or not a module.
        }
        return Optional.empty();
    }

    // ---- rendering ------------------------------------------------------------------------

    /** The diagnosis, one log line per element. */
    static List<String> render(Input input) {
        List<String> lines = new ArrayList<>();
        Instant start = input.buildStart();
        lines.add("[ike-workspace-extension] missing-module diagnosis for " + input.failing().label()
                + " (" + input.mojo() + ", started " + relative(input.failingStart(), start) + ")");
        lines.add("  build started " + start + "; " + input.lock());
        for (Missing missing : input.missing()) {
            switch (missing) {
                case MissingModule module -> renderModule(lines, input, module);
                case MissingClass type -> renderClass(lines, input, type);
            }
        }
        if (input.writers().isEmpty()) {
            lines.add("  other processes working in the tree: none found");
        } else {
            lines.add("  other processes working in the tree:");
            input.writers().forEach(writer -> lines.add("    " + writer));
        }
        return lines;
    }

    private static void renderModule(List<String> lines, Input input, MissingModule missing) {
        lines.add("  javac: module not found: " + missing.name());
        Optional<Module> declaring = input.modules().stream()
                .filter(module -> moduleName(module).filter(missing.name()::equals).isPresent())
                .findFirst();
        if (declaring.isEmpty()) {
            lines.add("    no reactor module declares " + missing.name());
        } else {
            Module module = declaring.get();
            describeSibling(lines, input, module, "module-info.class");
            describeResolution(lines, input, module);
        }
        List<String> providers = input.resolved().stream()
                .filter(dependency -> providedModule(dependency.file()).filter(missing.name()::equals).isPresent())
                .map(dependency -> dependency.id() + " -> " + dependency.file())
                .toList();
        lines.add(providers.isEmpty()
                ? "    no resolved dependency of " + input.failing().label() + " provides module " + missing.name()
                        + " now"
                : "    provided now by " + String.join("; ", providers));
    }

    private static void renderClass(List<String> lines, Input input, MissingClass missing) {
        lines.add("  test fork: class not found: " + missing.name()
                + missing.report().map(report -> " (in " + report.getFileName() + ")").orElse(""));
        Optional<Module> owner = owner(missing.name(), input.modules());
        if (owner.isEmpty()) {
            lines.add("    no reactor module owns " + missing.name());
            return;
        }
        if (owner.get().id().equals(input.failing().id())) {
            lines.add("    owned by the failing module itself");
        }
        describeSibling(lines, input, owner.get(), missing.name().replace('.', '/') + ".class");
        describeResolution(lines, input, owner.get());
    }

    private static void describeSibling(List<String> lines, Input input, Module module, String entry) {
        Instant start = input.buildStart();
        lines.add("    reactor module " + module.label() + " (" + module.id() + ")"
                + (module.inBuild() ? ", in this build" : ", not in this build"));
        Path classes = module.outputDirectory();
        lines.add("    target/classes " + classes + (Files.isDirectory(classes) ? "" : " (absent)"));
        Path file = classes.resolve(entry);
        Optional<Instant> written = lastModified(file);
        if (written.isPresent()) {
            lines.add("    " + entry + " present, written " + written.get() + " (" + relative(written.get(), start) + ")");
        } else {
            lines.add("    " + entry + " absent now");
        }
        if (module.compiled().isEmpty()) {
            lines.add("    this session has not compiled " + module.label()
                    + (written.filter(time -> time.isAfter(start)).isPresent()
                            ? "; the class directory was written during this build by someone else"
                            : ""));
        } else {
            Window window = module.compiled().get();
            lines.add("    this session compiled it " + relative(window.start(), start) + " to "
                    + relative(window.end(), start));
            if (written.isEmpty()) {
                lines.add("    => gone since this session compiled it: removed by another writer");
            } else if (written.get().isBefore(start.minus(SLACK))) {
                lines.add("    => unchanged since before this build: its compile found nothing stale");
            } else if (!window.contains(written.get())) {
                lines.add("    => written during this build, outside this session's compile of it: another writer");
            } else {
                lines.add("    => consistent with this session's own compile of it");
            }
            if (input.failingStart().isBefore(window.end())) {
                lines.add("    => " + input.failing().label() + " started before this session finished compiling "
                        + module.label() + ": Maven's ordering (MNG-8096-style), not an outside writer");
            }
        }
    }

    private static void describeResolution(List<String> lines, Input input, Module module) {
        Optional<Resolved> resolved = input.resolved().stream()
                .filter(dependency -> dependency.id().equals(module.id()))
                .findFirst();
        if (resolved.isEmpty()) {
            lines.add("    not among the resolved dependencies of " + input.failing().label());
            return;
        }
        Path file = resolved.get().file();
        Origin origin = origin(file, input.modules(), input.localRepository());
        lines.add("    resolved for " + input.failing().label() + " to " + file + " (" + origin.description + ")");
        if (origin == Origin.REACTOR_CLASSES
                && !file.toAbsolutePath().normalize().equals(module.outputDirectory().toAbsolutePath().normalize())) {
            lines.add("    => that is another module's class directory");
        }
        if (origin == Origin.PROJECT_LOCAL_REPOSITORY || origin == Origin.LOCAL_REPOSITORY) {
            lastModified(file).ifPresent(time -> lines.add("    that file was written " + time
                    + " (" + relative(time, input.buildStart()) + ")"));
            if (module.inBuild()) {
                lines.add("    => resolved outside the reactor although the module is in this build (MNG-8096-style)");
            }
        }
    }

    // ---- helpers --------------------------------------------------------------------------

    static String relative(Instant time, Instant start) {
        Duration delta = Duration.between(start, time);
        return delta.isNegative()
                ? Durations.brief(delta.negated()) + " before the build started"
                : "+" + Durations.brief(delta) + " into the build";
    }

    private static Optional<Instant> lastModified(Path file) {
        try {
            return Optional.of(Files.getLastModifiedTime(file).toInstant());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static boolean modifiedSince(Path file, Instant since) {
        return lastModified(file).filter(time -> !time.isBefore(since.minus(SLACK))).isPresent();
    }

    private static String stripComments(String source) {
        return source.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");
    }
}
