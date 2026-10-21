package network.ike.extension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The session-level half of the extension: takes the tree's {@link BuildLock} when project
 * discovery starts and releases it when the session ends, and renders a
 * {@link MissingModuleDiagnosis} when a compile or a test fork cannot find a reactor sibling.
 *
 * <p>An {@code EventSpy} rather than a lifecycle participant alone, because the spy sees
 * project discovery start — before any model is read, so before {@link MemberCache} writes —
 * and sees each mojo start and fail. It cannot fail the build: a lock that timed out is turned
 * into a build failure by {@link BuildLockGate}. Nothing here ever throws out of
 * {@link #onEvent(Object)}; a diagnosis that fails degrades to one warning.
 *
 * <p>Registered through the hand-written sisu index at {@code META-INF/sisu/javax.inject.Named}.
 */
@Named("ike-workspace-build-session")
@Singleton
public class BuildSessionSpy implements EventSpy {

    private static final Logger LOG = LoggerFactory.getLogger(BuildSessionSpy.class);

    private static final String COMPILER = "maven-compiler-plugin";
    private static final List<String> TEST_RUNNERS = List.of("maven-surefire-plugin", "maven-failsafe-plugin");
    private static final List<String> REPORT_DIRECTORIES = List.of("surefire-reports", "failsafe-reports");
    private static final int MAX_CLASSES = 5;

    /** The session's lock state; one outer session per JVM at a time. */
    private static volatile State state = State.NONE;

    private final Map<String, Instant> compileStarted = new ConcurrentHashMap<>();
    private final Map<String, MissingModuleDiagnosis.Window> compiled = new ConcurrentHashMap<>();
    private final Map<String, Instant> mojoStarted = new ConcurrentHashMap<>();

    /** Creates the spy; instantiated by the Maven container via the sisu index, never directly. */
    public BuildSessionSpy() {}

    /** The lock state of the current session, as seen by {@link BuildLockGate}. */
    record State(Path root, BuildLock.Outcome outcome, String skipped, Instant buildStart, Object session) {
        static final State NONE = new State(null, null, null, Instant.now(), null);

        String describe() {
            if (skipped != null) {
                return "build lock skipped (" + skipped + ")";
            }
            if (outcome == null) {
                return "build lock not taken";
            }
            return switch (outcome) {
                case BuildLock.Acquired acquired -> "build lock " + acquired.handle().lockFile()
                        + " held by this session (pid " + acquired.handle().holder().pid() + ")";
                case BuildLock.Nested nested -> "build lock not taken: nested build, " + nested.reason();
                case BuildLock.TimedOut timedOut -> "build lock timed out";
                case BuildLock.Unavailable unavailable -> "build lock unavailable: " + unavailable.reason();
            };
        }
    }

    static State state() {
        return state;
    }

    @Override
    public void init(Context context) {
        // Everything needed arrives with the session's events.
    }

    @Override
    public void onEvent(Object event) {
        try {
            if (event instanceof ExecutionEvent execution) {
                onExecutionEvent(execution);
            }
        } catch (RuntimeException | LinkageError e) {
            LOG.warn("[ike-workspace-extension] session event handling failed: {}", e.toString());
        }
    }

    @Override
    public void close() {
        release();
    }

    private void onExecutionEvent(ExecutionEvent event) {
        switch (event.getType()) {
            case ProjectDiscoveryStarted -> lock(event.getSession());
            case MojoStarted -> {
                String project = key(event.getProject());
                Instant now = Instant.now();
                mojoStarted.put(project, now);
                if (isCompile(event.getMojoExecution())) {
                    compileStarted.put(project, now);
                }
            }
            case MojoSucceeded -> finishCompile(event);
            case MojoFailed -> {
                finishCompile(event);
                diagnose(event);
            }
            case SessionEnded -> release(event.getSession());
            default -> {
                // Nothing else concerns the lock or the diagnosis.
            }
        }
    }

    // ---- the lock -------------------------------------------------------------------------

    private void lock(MavenSession session) {
        if (session == null) {
            return;
        }
        if (state.outcome() instanceof BuildLock.Acquired && state.session() != session) {
            // A session nested in this JVM (an in-process invoker) runs under the outer build's lock;
            // the outer session's state stays, so the nested session's end cannot release it.
            LOG.info("[ike-workspace-extension] build lock not taken: nested build (this JVM holds it)");
            return;
        }
        MavenExecutionRequest request = session.getRequest();
        File directory = request.getMultiModuleProjectDirectory();
        if (directory == null) {
            return;
        }
        Path root = lockRoot(directory.toPath());
        Instant now = Instant.now();
        String enabled = property(session, BuildLock.ENABLED_PROPERTY);
        Optional<String> skip = BuildLockPolicy.skipReason(enabled, request.getGoals());
        if (skip.isPresent()) {
            LOG.debug("[ike-workspace-extension] build lock skipped: {}", skip.get());
            state = new State(root, null, skip.get(), now, session);
            return;
        }
        Duration timeout = BuildLock.parseDuration(property(session, BuildLock.TIMEOUT_PROPERTY),
                BuildLock.DEFAULT_TIMEOUT);
        Duration notice = BuildLock.parseDuration(property(session, BuildLock.NOTICE_PROPERTY),
                BuildLock.DEFAULT_NOTICE);
        List<String> projects = request.getSelectedProjects() == null ? List.of() : request.getSelectedProjects();
        List<String> goals = new ArrayList<>(request.getGoals());
        if (request.getMakeBehavior() != null) {
            goals.add(switch (request.getMakeBehavior()) {
                case MavenExecutionRequest.REACTOR_MAKE_UPSTREAM -> "-am";
                case MavenExecutionRequest.REACTOR_MAKE_DOWNSTREAM -> "-amd";
                case MavenExecutionRequest.REACTOR_MAKE_BOTH -> "-am -amd";
                default -> "";
            });
        }
        BuildLock.Holder holder = BuildLock.Holder.current(root,
                goals.stream().filter(goal -> !goal.isBlank()).toList(), projects);
        BuildLock.Outcome outcome = BuildLock.acquire(holder, timeout, notice, LOG::warn);
        Instant buildStart = Instant.now();
        switch (outcome) {
            case BuildLock.Acquired acquired -> {
                if (acquired.waited().compareTo(Duration.ofSeconds(1)) >= 0) {
                    LOG.warn("[ike-workspace-extension] build lock {} taken after waiting {}",
                            acquired.handle().lockFile(), Durations.brief(acquired.waited()));
                } else {
                    LOG.info("[ike-workspace-extension] build lock {} taken", acquired.handle().lockFile());
                }
            }
            case BuildLock.Nested nested ->
                    LOG.info("[ike-workspace-extension] build lock not taken: nested build ({})", nested.reason());
            case BuildLock.TimedOut timedOut -> {
                // BuildLockGate fails the build with the holder named; saying it here too doubles it.
            }
            case BuildLock.Unavailable unavailable ->
                    LOG.warn("[ike-workspace-extension] build lock {} unavailable, building unlocked: {}",
                            unavailable.lockFile(), unavailable.reason());
        }
        state = new State(root, outcome, null, buildStart, session);
    }

    /** A member built from its own root locks its working set's tree, whose target directories it writes. */
    static Path lockRoot(Path rootDirectory) {
        return MemberCache.memberOf(rootDirectory)
                .map(MemberCache.Member::workspaceRoot)
                .orElse(rootDirectory.toAbsolutePath().normalize());
    }

    /** Releases the lock if {@code session} took it. */
    static void release(Object session) {
        if (state.session() == session) {
            release();
        }
    }

    /** Releases the lock whichever session took it: the JVM's Maven run is over. */
    static void release() {
        State current = state;
        if (current.outcome() instanceof BuildLock.Acquired acquired
                && BuildLock.heldHere(current.root()).isPresent()) {
            acquired.handle().close();
            LOG.debug("[ike-workspace-extension] build lock {} released", acquired.handle().lockFile());
        }
    }

    private static String property(MavenSession session, String name) {
        String value = session.getUserProperties().getProperty(name);
        return value != null ? value : session.getSystemProperties().getProperty(name);
    }

    // ---- the diagnosis --------------------------------------------------------------------

    private void finishCompile(ExecutionEvent event) {
        if (!isCompile(event.getMojoExecution())) {
            return;
        }
        String project = key(event.getProject());
        Instant start = compileStarted.remove(project);
        if (start != null) {
            compiled.put(project, new MissingModuleDiagnosis.Window(start, Instant.now()));
        }
    }

    private void diagnose(ExecutionEvent event) {
        MojoExecution mojo = event.getMojoExecution();
        MavenProject project = event.getProject();
        MavenSession session = event.getSession();
        if (mojo == null || project == null || session == null) {
            return;
        }
        String plugin = mojo.getPlugin().getArtifactId();
        Instant buildStart = state.buildStart();
        List<MissingModuleDiagnosis.Missing> missing;
        if (COMPILER.equals(plugin)) {
            missing = MissingModuleDiagnosis.modulesNotFound(failureText(event.getException()));
        } else if (TEST_RUNNERS.contains(plugin)) {
            Path target = Path.of(project.getBuild().getDirectory());
            List<MissingModuleDiagnosis.Missing> found = new ArrayList<>(
                    MissingModuleDiagnosis.missingClassesInReports(
                            REPORT_DIRECTORIES.stream().map(target::resolve).toList(), buildStart, MAX_CLASSES));
            for (String name : MissingModuleDiagnosis.missingClasses(failureText(event.getException()))) {
                if (found.size() < MAX_CLASSES && found.stream().noneMatch(m -> m.name().equals(name))) {
                    found.add(new MissingModuleDiagnosis.MissingClass(name, Optional.empty()));
                }
            }
            missing = found;
        } else {
            return;
        }
        if (missing.isEmpty()) {
            return;
        }
        try {
            List<MavenProject> building = session.getProjects();
            List<MavenProject> all = session.getAllProjects() != null ? session.getAllProjects() : building;
            List<MissingModuleDiagnosis.Module> modules = all.stream()
                    .map(p -> module(p, building.contains(p)))
                    .toList();
            List<MissingModuleDiagnosis.Resolved> resolved = new ArrayList<>();
            for (Artifact artifact : project.getArtifacts()) {
                if (artifact.getFile() != null) {
                    resolved.add(new MissingModuleDiagnosis.Resolved(
                            artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact.getFile().toPath()));
                }
            }
            Optional<Path> localRepository = Optional.ofNullable(session.getRepositorySession())
                    .map(repositorySession -> repositorySession.getLocalRepository())
                    .map(local -> local.getBasePath());
            Path root = state.root() != null ? state.root()
                    : lockRoot(session.getRequest().getMultiModuleProjectDirectory().toPath());
            MissingModuleDiagnosis.Input input = new MissingModuleDiagnosis.Input(
                    module(project, true),
                    mojoStarted.getOrDefault(key(project), Instant.now()),
                    plugin.replace("maven-", "").replace("-plugin", "") + ":" + mojo.getGoal(),
                    missing, modules, resolved, localRepository, buildStart, state.describe(),
                    TreeWriters.in(root));
            List<String> lines = MissingModuleDiagnosis.render(input);
            lines.forEach(LOG::warn);
            write(root, project.getArtifactId(), lines).ifPresent(file ->
                    LOG.warn("[ike-workspace-extension] diagnosis saved to {}", file));
        } catch (RuntimeException | LinkageError e) {
            LOG.warn("[ike-workspace-extension] missing-module diagnosis failed: {}", e.toString());
        }
    }

    private MissingModuleDiagnosis.Module module(MavenProject project, boolean inBuild) {
        Path basedir = project.getBasedir().toPath();
        String build = project.getBuild().getDirectory();
        String output = project.getBuild().getOutputDirectory();
        Path buildDirectory = build == null ? basedir.resolve("target") : Path.of(build);
        Path outputDirectory = output == null ? buildDirectory.resolve("classes") : Path.of(output);
        List<Path> sources = project.getCompileSourceRoots() == null ? List.of()
                : project.getCompileSourceRoots().stream().map(Path::of).toList();
        return new MissingModuleDiagnosis.Module(key(project), project.getArtifactId(), basedir, buildDirectory, outputDirectory,
                sources, inBuild, Optional.ofNullable(compiled.get(key(project))));
    }

    private static Optional<Path> write(Path root, String artifactId, List<String> lines) {
        try {
            Path directory = root.resolve(".mvn/target/ike-diagnostics");
            Files.createDirectories(directory);
            Path file = directory.resolve("missing-module-" + artifactId + "-" + System.currentTimeMillis() + ".txt");
            Files.write(file, lines, StandardCharsets.UTF_8);
            return Optional.of(file);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /** Everything a failure says, long messages included: compiler errors live there. */
    static String failureText(Throwable failure) {
        StringBuilder text = new StringBuilder();
        for (Throwable t = failure; t != null; t = t.getCause() == t ? null : t.getCause()) {
            text.append(t).append('\n');
            if (t instanceof AbstractMojoExecutionException mojoFailure && mojoFailure.getLongMessage() != null) {
                text.append(mojoFailure.getLongMessage()).append('\n');
            }
        }
        return text.toString();
    }

    /** The main compile, which writes {@code target/classes}; test compiles write elsewhere. */
    private static boolean isCompile(MojoExecution mojo) {
        return mojo != null && COMPILER.equals(mojo.getPlugin().getArtifactId()) && "compile".equals(mojo.getGoal());
    }

    private static String key(MavenProject project) {
        return project == null ? "unknown" : project.getGroupId() + ":" + project.getArtifactId();
    }
}
