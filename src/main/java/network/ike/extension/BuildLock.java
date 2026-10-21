package network.ike.extension;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * One build at a time per tree: an exclusive OS file lock on
 * {@code <root>/.mvn/target/build.lock}, held from project discovery to the end of the
 * session.
 *
 * <p><b>Why.</b> A working set is shared by several writers — IntelliJ, and more than one
 * Claude session — and a parallel build that starts while another is writing the same
 * {@code target/} directories fails once and passes on rerun: a {@code NoClassDefFoundError}
 * for a built sibling's classes (2026-10-12), a javac {@code module not found} for a declared
 * dependency (2026-10-21). Neither reproduced in an idle tree. The lock serializes the Maven
 * writers so the next such failure can only be an outside writer or Maven itself, which
 * {@link MissingModuleDiagnosis} then tells apart.
 *
 * <p><b>Who locks.</b> Only the outer Maven session of a tree. A Maven started by a build that
 * holds the lock — {@code exec}, the invoker, a {@code ws:} goal running {@code mvn} — finds its
 * own ancestor named in the lock file and proceeds unlocked; so does a second session in the
 * JVM already holding it. Test forks never load the extension. A crashed holder never wedges
 * the tree: the OS releases the lock with the process, Ctrl-C included, and the file's stale
 * contents are only read while the lock is actually held by someone.
 *
 * <p><b>The file.</b> The holder writes what it is — pid, host, start time, goals and
 * {@code -pl} — into the locked file, so a waiter can say who it waits for. The holder never
 * opens the file a second time: POSIX record locks belong to the process, and closing any
 * descriptor of the file would release them.
 *
 * <p>{@code -D}{@value #ENABLED_PROPERTY}{@code =false} turns the lock off;
 * {@code -D}{@value #TIMEOUT_PROPERTY} bounds the wait (ISO-8601 such as {@code PT45M}, or
 * {@code 90s}, {@code 30m}, {@code 2h}; a bare number is minutes).
 */
final class BuildLock {

    /** {@code -Dike.build.lock=false} turns the lock off. */
    static final String ENABLED_PROPERTY = "ike.build.lock";

    /** How long to wait for another build before failing. */
    static final String TIMEOUT_PROPERTY = "ike.build.lock.timeout";

    /** How often a waiting build says it is still waiting. */
    static final String NOTICE_PROPERTY = "ike.build.lock.notice";

    static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(30);
    static final Duration DEFAULT_NOTICE = Duration.ofSeconds(30);

    /** The lock file, relative to the tree's root. */
    static final Path LOCK_FILE = Path.of(".mvn", "target", "build.lock");

    private static final Duration POLL = Duration.ofMillis(250);

    /** Locks this JVM holds, by lock file: a second session here is nested, not a rival. */
    private static final Map<Path, Held> HELD = new ConcurrentHashMap<>();

    private BuildLock() {}

    /** What a build is, as the lock file records it. */
    record Holder(long pid, String host, Instant started, String goals, String projects, Path root) {

        /** This process, about to build {@code root}. */
        static Holder current(Path root, List<String> goals, List<String> projects) {
            return new Holder(ProcessHandle.current().pid(), hostName(), Instant.now(),
                    String.join(" ", goals), String.join(",", projects), root);
        }

        String render() {
            Map<String, String> lines = new LinkedHashMap<>();
            lines.put("pid", Long.toString(pid));
            lines.put("host", host);
            lines.put("started", started.toString());
            lines.put("goals", goals);
            lines.put("projects", projects);
            lines.put("root", root.toString());
            StringBuilder text = new StringBuilder();
            lines.forEach((key, value) -> text.append(key).append('=').append(value).append('\n'));
            return text.toString();
        }

        static Optional<Holder> parse(String text) {
            Map<String, String> fields = new LinkedHashMap<>();
            for (String line : text.split("\\R")) {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    fields.put(line.substring(0, eq), line.substring(eq + 1));
                }
            }
            try {
                return Optional.of(new Holder(
                        Long.parseLong(fields.get("pid")),
                        fields.getOrDefault("host", "?"),
                        Instant.parse(fields.get("started")),
                        fields.getOrDefault("goals", ""),
                        fields.getOrDefault("projects", ""),
                        Path.of(fields.getOrDefault("root", "?"))));
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }

        /** One line: who, where, since when, doing what. */
        String describe(Instant now) {
            StringBuilder text = new StringBuilder();
            text.append("pid ").append(pid).append(" on ").append(host)
                    .append(", started ").append(started)
                    .append(" (").append(Durations.brief(Duration.between(started, now))).append(" ago)");
            if (!goals.isBlank()) {
                text.append(": ").append(goals);
            }
            if (!projects.isBlank()) {
                text.append(" -pl ").append(projects);
            }
            return text.toString();
        }
    }

    /** How a session came out of {@link #acquire}. */
    sealed interface Outcome permits Acquired, Nested, TimedOut, Unavailable {}

    /** This session holds the lock; close the handle at session end. */
    record Acquired(Handle handle, Duration waited) implements Outcome {}

    /** The lock is held by this JVM or an ancestor process; this session runs inside that build. */
    record Nested(String reason) implements Outcome {}

    /** Another build held the lock for the whole timeout. */
    record TimedOut(Path lockFile, Optional<Holder> holder, Duration waited) implements Outcome {}

    /** The lock could not be taken for a reason other than another build; the session proceeds. */
    record Unavailable(Path lockFile, String reason) implements Outcome {}

    /** The held lock. Closing it releases the lock; idempotent. */
    static final class Handle implements AutoCloseable {
        private final Path lockFile;
        private final Holder holder;

        private Handle(Path lockFile, Holder holder) {
            this.lockFile = lockFile;
            this.holder = holder;
        }

        Path lockFile() {
            return lockFile;
        }

        Holder holder() {
            return holder;
        }

        @Override
        public void close() {
            Held held = HELD.remove(lockFile);
            if (held == null) {
                return;
            }
            try {
                held.lock.release();
            } catch (IOException ignored) {
                // The channel close below releases it anyway.
            }
            try {
                held.channel.close();
            } catch (IOException ignored) {
                // Process exit releases it in the end.
            }
        }
    }

    private record Held(FileChannel channel, FileLock lock, Holder holder) {}

    /** The lock file of a tree. */
    static Path lockFile(Path root) {
        return root.toAbsolutePath().normalize().resolve(LOCK_FILE);
    }

    /** The holder this JVM has recorded for a tree, if it holds the tree's lock. */
    static Optional<Holder> heldHere(Path root) {
        Held held = HELD.get(lockFile(root));
        return held == null ? Optional.empty() : Optional.of(held.holder);
    }

    /**
     * Takes the tree's lock, waiting for another build up to {@code timeout}.
     *
     * @param holder  what this build is; its root names the tree
     * @param timeout how long to wait for another build
     * @param notice  how often to say it is still waiting
     * @param log     where waiting messages go
     * @return how it came out; never throws for a held lock
     */
    static Outcome acquire(Holder holder, Duration timeout, Duration notice, Consumer<String> log) {
        Path lockFile = lockFile(holder.root());
        if (HELD.containsKey(lockFile)) {
            return new Nested("this JVM already holds " + lockFile);
        }
        FileChannel channel;
        try {
            Files.createDirectories(lockFile.getParent());
            channel = FileChannel.open(lockFile, StandardOpenOption.CREATE,
                    StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            return new Unavailable(lockFile, e.toString());
        }
        Instant start = Instant.now();
        Instant deadline = start.plus(timeout);
        Instant nextNotice = start;
        Optional<Holder> lastHolder = Optional.empty();
        try {
            while (true) {
                FileLock lock;
                try {
                    lock = channel.tryLock();
                } catch (OverlappingFileLockException e) {
                    channel.close();
                    return new Nested("this JVM already holds " + lockFile);
                }
                if (lock != null) {
                    Holder recorded = holder;
                    write(channel, recorded);
                    HELD.put(lockFile, new Held(channel, lock, recorded));
                    return new Acquired(new Handle(lockFile, recorded), Duration.between(start, Instant.now()));
                }
                Optional<Holder> current = read(lockFile);
                if (current.isPresent()) {
                    lastHolder = current;
                    if (isAncestor(current.get().pid())) {
                        channel.close();
                        return new Nested("lock held by ancestor process " + current.get().pid());
                    }
                }
                Instant now = Instant.now();
                if (!now.isBefore(deadline)) {
                    channel.close();
                    return new TimedOut(lockFile, lastHolder, Duration.between(start, now));
                }
                if (!now.isBefore(nextNotice)) {
                    log.accept(waitingMessage(lockFile, lastHolder, Duration.between(start, now), timeout, now));
                    nextNotice = now.plus(notice);
                }
                Thread.sleep(POLL.toMillis());
            }
        } catch (IOException e) {
            closeQuietly(channel);
            return new Unavailable(lockFile, e.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            closeQuietly(channel);
            return new Unavailable(lockFile, "interrupted while waiting");
        }
    }

    static String waitingMessage(Path lockFile, Optional<Holder> holder, Duration waited, Duration timeout,
            Instant now) {
        return "[ike-workspace-extension] another build holds this tree's build lock "
                + lockFile + ": " + holder.map(h -> h.describe(now)).orElse("holder not yet recorded")
                + "; waited " + Durations.brief(waited) + " of " + Durations.brief(timeout)
                + " (-D" + TIMEOUT_PROPERTY + "; -D" + ENABLED_PROPERTY + "=false skips the lock)";
    }

    static String timedOutMessage(TimedOut timedOut) {
        return "[ike-workspace-extension] gave up after " + Durations.brief(timedOut.waited())
                + " waiting for this tree's build lock " + timedOut.lockFile() + ", held by "
                + timedOut.holder().map(h -> h.describe(Instant.now())).orElse("an unrecorded holder")
                + ". Wait for that build, raise -D" + TIMEOUT_PROPERTY + ", or skip the lock with -D"
                + ENABLED_PROPERTY + "=false.";
    }

    private static void write(FileChannel channel, Holder holder) throws IOException {
        byte[] bytes = holder.render().getBytes(StandardCharsets.UTF_8);
        channel.truncate(0);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long position = 0;
        while (buffer.hasRemaining()) {
            position += channel.write(buffer, position);
        }
        channel.force(false);
    }

    /** Reads another process's record. Never call for a lock this JVM holds (see the class note). */
    static Optional<Holder> read(Path lockFile) {
        try {
            return Holder.parse(Files.readString(lockFile, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    /** Whether a pid is this process's parent, grandparent, and so on. */
    static boolean isAncestor(long pid) {
        Optional<ProcessHandle> parent = ProcessHandle.current().parent();
        while (parent.isPresent()) {
            if (parent.get().pid() == pid) {
                return true;
            }
            parent = parent.get().parent();
        }
        return false;
    }

    /** Parses {@value #TIMEOUT_PROPERTY}-style durations. */
    static Duration parseDuration(String text, Duration fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }
        String value = text.strip().toLowerCase(Locale.ROOT);
        try {
            if (value.startsWith("p")) {
                return Duration.parse(value.toUpperCase(Locale.ROOT));
            }
            char unit = value.charAt(value.length() - 1);
            if (Character.isDigit(unit)) {
                return Duration.ofMinutes(Long.parseLong(value));
            }
            long amount = Long.parseLong(value.substring(0, value.length() - 1).strip());
            return switch (unit) {
                case 's' -> Duration.ofSeconds(amount);
                case 'm' -> Duration.ofMinutes(amount);
                case 'h' -> Duration.ofHours(amount);
                default -> fallback;
            };
        } catch (DateTimeParseException | NumberFormatException e) {
            return fallback;
        }
    }

    private static String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            String env = System.getenv("HOSTNAME");
            return env == null ? "unknown-host" : env;
        }
    }

    private static void closeQuietly(FileChannel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
            // Nothing held.
        }
    }
}
