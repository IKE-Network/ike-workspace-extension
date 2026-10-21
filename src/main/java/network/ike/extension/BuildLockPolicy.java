package network.ike.extension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Which sessions take the build lock: every one, unless turned off or asking only for goals
 * that write nothing under the tree.
 *
 * <p>A read-only session is recognized by its goals alone — every goal on the command line
 * must be one of these. Anything else locks, including {@code clean}, which is the most
 * destructive writer of all.
 */
final class BuildLockPolicy {

    /** Plugin goals that read the tree and write nothing under it. */
    private static final Set<String> READ_ONLY_GOALS = Set.of(
            "dependency:tree", "dependency:list", "dependency:resolve", "dependency:resolve-plugins",
            "versions:display-dependency-updates", "versions:display-plugin-updates",
            "versions:display-property-updates");

    /** Lifecycle phases that write nothing under the tree. */
    private static final Set<String> READ_ONLY_PHASES = Set.of("validate");

    private BuildLockPolicy() {}

    /**
     * Why a session skips the lock, if it does.
     *
     * @param enabled the value of {@value BuildLock#ENABLED_PROPERTY}, or null
     * @param goals   the goals and phases on the command line
     * @return the reason to skip, or empty to lock
     */
    static Optional<String> skipReason(String enabled, List<String> goals) {
        if (enabled != null && enabled.strip().equalsIgnoreCase("false")) {
            return Optional.of("-D" + BuildLock.ENABLED_PROPERTY + "=false");
        }
        if (!goals.isEmpty() && goals.stream().allMatch(BuildLockPolicy::readOnly)) {
            return Optional.of("read-only goals " + String.join(" ", goals));
        }
        return Optional.empty();
    }

    static boolean readOnly(String goal) {
        if (READ_ONLY_PHASES.contains(goal) || READ_ONLY_GOALS.contains(goal)) {
            return true;
        }
        // Every help: goal only reports; the ws: drafts are read-only by contract.
        int colon = goal.indexOf(':');
        if (colon < 0) {
            return false;
        }
        String prefix = goal.substring(0, colon);
        return prefix.equals("help") || prefix.equals("ws") && goal.endsWith("-draft");
    }
}
