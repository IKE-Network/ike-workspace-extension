package network.ike.extension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The tree's build lock, between real processes: one holder, waiters told who, crashes never wedge. */
class BuildLockTest {

    @TempDir
    Path tree;

    private final List<Probe> probes = new ArrayList<>();

    @AfterEach
    void stopProbes() {
        probes.forEach(probe -> probe.process.destroyForcibly());
    }

    /** A probe process and the lines it has printed. */
    private record Probe(Process process, BlockingQueue<String> lines) {

        String next(Duration within) throws InterruptedException {
            String line = lines.poll(within.toMillis(), TimeUnit.MILLISECONDS);
            assertNotNull(line, "the probe said nothing within " + within);
            return line;
        }

        /** The next line that is not a waiting notice, collecting the notices. */
        String result(Duration within, List<String> notices) throws InterruptedException {
            Instant deadline = Instant.now().plus(within);
            while (true) {
                String line = next(Duration.between(Instant.now(), deadline));
                if (!line.startsWith("WAITING ")) {
                    return line;
                }
                notices.add(line);
            }
        }

        void finish() throws IOException, InterruptedException {
            process.getOutputStream().close();
            assertTrue(process.waitFor(10, TimeUnit.SECONDS));
        }
    }

    private Probe probe(Duration timeout) throws IOException {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"),
                BuildLockProbe.class.getName(), tree.toString(), Long.toString(timeout.toMillis()))
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();
        BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        Thread reader = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    lines.add(line);
                }
            } catch (IOException ignored) {
                // The probe ended.
            }
        });
        reader.setDaemon(true);
        reader.start();
        Probe probe = new Probe(process, lines);
        probes.add(probe);
        return probe;
    }

    @Test
    void aSecondBuildWaitsNamingTheHolderAndProceedsWhenItFinishes() throws Exception {
        Probe first = probe(Duration.ofSeconds(5));
        assertTrue(first.next(Duration.ofSeconds(20)).startsWith("ACQUIRED"));

        Probe second = probe(Duration.ofSeconds(30));
        List<String> notices = new ArrayList<>();
        String early = second.lines.poll(10, TimeUnit.SECONDS);
        assertNotNull(early, "the waiting build says so");
        assertTrue(early.startsWith("WAITING "), early);
        notices.add(early);
        assertTrue(early.contains("pid " + first.process.pid()), "names the holder: " + early);
        assertTrue(early.contains("compile -pl probe"), "says what the holder builds: " + early);

        // Let the second wait a while before the first finishes.
        Thread.sleep(1200);
        first.finish();
        String result = second.result(Duration.ofSeconds(20), notices);
        assertTrue(result.startsWith("ACQUIRED"), result);
        long waited = Long.parseLong(result.substring("ACQUIRED waited=".length()));
        assertTrue(waited >= 1000, "waited " + waited + " ms");
        second.finish();
    }

    @Test
    void aBuildThatWaitsTooLongFailsNamingTheHolder() throws Exception {
        Probe first = probe(Duration.ofSeconds(5));
        assertTrue(first.next(Duration.ofSeconds(20)).startsWith("ACQUIRED"));

        Probe second = probe(Duration.ofMillis(800));
        String result = second.result(Duration.ofSeconds(20), new ArrayList<>());
        assertTrue(result.startsWith("TIMEDOUT"), result);
        assertTrue(result.contains("pid " + first.process.pid()), result);
        assertTrue(result.contains(BuildLock.TIMEOUT_PROPERTY), result);
        first.finish();
    }

    @Test
    void aKilledHolderNeverWedgesTheTree() throws Exception {
        Probe first = probe(Duration.ofSeconds(5));
        assertTrue(first.next(Duration.ofSeconds(20)).startsWith("ACQUIRED"));
        first.process.destroyForcibly().waitFor(10, TimeUnit.SECONDS);

        Probe second = probe(Duration.ofSeconds(5));
        String result = second.result(Duration.ofSeconds(20), new ArrayList<>());
        assertTrue(result.startsWith("ACQUIRED"), result);
        second.finish();
    }

    @Test
    void aBuildStartedByTheHolderRunsNestedInsteadOfDeadlocking() throws Exception {
        BuildLock.Outcome outcome = BuildLock.acquire(
                BuildLock.Holder.current(tree, List.of("install"), List.of()),
                Duration.ofSeconds(1), Duration.ofSeconds(1), line -> {});
        BuildLock.Acquired acquired = assertInstanceOf(BuildLock.Acquired.class, outcome);
        try {
            // This JVM is the child's parent: it holds the lock the child finds.
            Probe child = probe(Duration.ofSeconds(5));
            String result = child.result(Duration.ofSeconds(20), new ArrayList<>());
            assertTrue(result.startsWith("NESTED"), result);
            assertTrue(result.contains(Long.toString(ProcessHandle.current().pid())), result);
        } finally {
            acquired.handle().close();
        }
    }

    @Test
    void aSecondSessionInTheHoldingJvmIsNested() {
        BuildLock.Outcome outcome = BuildLock.acquire(
                BuildLock.Holder.current(tree, List.of("verify"), List.of()),
                Duration.ofSeconds(1), Duration.ofSeconds(1), line -> {});
        BuildLock.Acquired acquired = assertInstanceOf(BuildLock.Acquired.class, outcome);
        try {
            assertEquals(Optional.of(acquired.handle().holder()), BuildLock.heldHere(tree));
            BuildLock.Outcome second = BuildLock.acquire(
                    BuildLock.Holder.current(tree, List.of("verify"), List.of()),
                    Duration.ofSeconds(1), Duration.ofSeconds(1), line -> {});
            assertInstanceOf(BuildLock.Nested.class, second);
        } finally {
            acquired.handle().close();
        }
        assertEquals(Optional.empty(), BuildLock.heldHere(tree));
        BuildLock.Outcome again = BuildLock.acquire(
                BuildLock.Holder.current(tree, List.of("verify"), List.of()),
                Duration.ofSeconds(1), Duration.ofSeconds(1), line -> {});
        BuildLock.Acquired reacquired = assertInstanceOf(BuildLock.Acquired.class, again, "released on close");
        reacquired.handle().close();
    }

    @Test
    void theHolderRecordRoundTrips() {
        BuildLock.Holder holder = new BuildLock.Holder(42, "graphlet", Instant.parse("2026-10-21T12:00:00Z"),
                "compile -am", "komet/kview", tree);
        assertEquals(Optional.of(holder), BuildLock.Holder.parse(holder.render()));
        assertEquals(Optional.empty(), BuildLock.Holder.parse("pid=4"));
    }

    @Test
    void timeoutsParseInTheUsualSpellings() {
        Duration fallback = BuildLock.DEFAULT_TIMEOUT;
        assertEquals(Duration.ofMinutes(45), BuildLock.parseDuration("PT45M", fallback));
        assertEquals(Duration.ofSeconds(90), BuildLock.parseDuration("90s", fallback));
        assertEquals(Duration.ofMinutes(30), BuildLock.parseDuration("30m", fallback));
        assertEquals(Duration.ofHours(2), BuildLock.parseDuration("2h", fallback));
        assertEquals(Duration.ofMinutes(10), BuildLock.parseDuration("10", fallback));
        assertEquals(fallback, BuildLock.parseDuration("soon", fallback));
        assertEquals(fallback, BuildLock.parseDuration(null, fallback));
    }

    @Test
    void readOnlyGoalsAndTheSwitchSkipTheLock() {
        assertTrue(BuildLockPolicy.skipReason("false", List.of("install")).isPresent());
        assertTrue(BuildLockPolicy.skipReason(null, List.of("validate")).isPresent());
        assertTrue(BuildLockPolicy.skipReason(null, List.of("help:effective-pom")).isPresent());
        assertTrue(BuildLockPolicy.skipReason(null, List.of("ws:feature-start-sibling-draft")).isPresent());
        assertTrue(BuildLockPolicy.skipReason(null, List.of("dependency:tree", "validate")).isPresent());
        assertTrue(BuildLockPolicy.skipReason(null, List.of("clean")).isEmpty(), "clean writes");
        assertTrue(BuildLockPolicy.skipReason(null, List.of("validate", "compile")).isEmpty());
        assertTrue(BuildLockPolicy.skipReason(null, List.of("ws:feature-start-sibling-publish")).isEmpty());
        assertTrue(BuildLockPolicy.skipReason(null, List.of()).isEmpty(), "default goals lock");
        assertTrue(BuildLockPolicy.skipReason("true", List.of("compile")).isEmpty());
    }
}
