package network.ike.extension;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * A separate build process for {@link BuildLockTest}: takes a tree's lock, says how it came
 * out on standard output, and holds the lock until its standard input closes.
 *
 * <p>Arguments: the tree's root, the wait timeout in milliseconds.
 */
public final class BuildLockProbe {

    private BuildLockProbe() {}

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        Duration timeout = Duration.ofMillis(Long.parseLong(args[1]));
        BuildLock.Holder holder = BuildLock.Holder.current(root, List.of("compile"), List.of("probe"));
        BuildLock.Outcome outcome = BuildLock.acquire(holder, timeout, Duration.ofMillis(200),
                line -> System.out.println("WAITING " + line));
        String result = switch (outcome) {
            case BuildLock.Acquired acquired -> "ACQUIRED waited=" + acquired.waited().toMillis();
            case BuildLock.Nested nested -> "NESTED " + nested.reason();
            case BuildLock.TimedOut timedOut -> "TIMEDOUT " + BuildLock.timedOutMessage(timedOut);
            case BuildLock.Unavailable unavailable -> "UNAVAILABLE " + unavailable.reason();
        };
        System.out.println(result);
        System.out.flush();
        if (outcome instanceof BuildLock.Acquired acquired) {
            while (System.in.read() >= 0) {
                // Hold until the test closes our input.
            }
            acquired.handle().close();
        }
    }
}
