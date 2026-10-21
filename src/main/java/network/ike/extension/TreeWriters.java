package network.ike.extension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Other processes working in a tree right now, as far as is cheap to know: a command line that
 * names the tree, a working directory inside it, or an IntelliJ build process.
 *
 * <p>Two process listings, each bounded to a few seconds — {@code ps} for command lines and
 * {@code lsof -d cwd} for working directories. Which files a process has open under
 * {@code target/} would need a recursive {@code lsof} over the tree, far too slow for a
 * failure path. This process, its ancestors and its descendants (test forks) are left out.
 * Unix only; elsewhere, or when a listing fails, the list is just shorter.
 */
final class TreeWriters {

    private static final long TIMEOUT_SECONDS = 3;
    private static final String INTELLIJ_BUILD = "org.jetbrains.jps.cmdline.BuildMain";

    private TreeWriters() {}

    /** One line per process working in {@code root}, other than this build. */
    static List<String> in(Path root) {
        String tree = root.toAbsolutePath().normalize().toString();
        Set<Long> ours = ours();
        Map<Long, String> found = new LinkedHashMap<>();
        for (String line : run("ps", "-Aww", "-o", "pid=,etime=,command=")) {
            String[] parts = line.strip().split("\\s+", 3);
            if (parts.length < 3) {
                continue;
            }
            long pid;
            try {
                pid = Long.parseLong(parts[0]);
            } catch (NumberFormatException e) {
                continue;
            }
            String command = parts[2];
            if (ours.contains(pid)) {
                continue;
            }
            boolean intellijBuild = command.contains(INTELLIJ_BUILD);
            if (command.contains(tree) || intellijBuild) {
                found.put(pid, "pid " + pid + " (running " + parts[1] + ")"
                        + (intellijBuild ? " IntelliJ build process" : "") + ": " + abbreviate(command));
            }
        }
        long pid = -1;
        for (String line : run("lsof", "-nP", "-a", "-d", "cwd", "-Fpn")) {
            if (line.startsWith("p")) {
                try {
                    pid = Long.parseLong(line.substring(1));
                } catch (NumberFormatException e) {
                    pid = -1;
                }
            } else if (line.startsWith("n") && pid > 0 && !ours.contains(pid)) {
                String cwd = line.substring(1);
                if (cwd.equals(tree) || cwd.startsWith(tree + "/")) {
                    String prior = found.get(pid);
                    found.put(pid, (prior == null ? "pid " + pid + " (" + command(pid) + ")" : prior)
                            + " [cwd " + cwd + "]");
                }
            }
        }
        return new ArrayList<>(found.values());
    }

    private static Set<Long> ours() {
        Set<Long> pids = new HashSet<>();
        ProcessHandle self = ProcessHandle.current();
        pids.add(self.pid());
        self.parent().ifPresent(parent -> {
            for (ProcessHandle p = parent; p != null; p = p.parent().orElse(null)) {
                pids.add(p.pid());
            }
        });
        self.descendants().forEach(child -> pids.add(child.pid()));
        return pids;
    }

    private static String command(long pid) {
        return ProcessHandle.of(pid)
                .flatMap(handle -> handle.info().command())
                .map(TreeWriters::abbreviate)
                .orElse("command unknown");
    }

    private static String abbreviate(String command) {
        return command.length() <= 300 ? command : command.substring(0, 300) + "…";
    }

    private static List<String> run(String... command) {
        Path out = null;
        try {
            // Output goes to a file so a hung listing cannot block the read past the timeout.
            out = Files.createTempFile("ike-tree-writers", ".txt");
            // Run from / so the listing's own working directory is never inside the tree.
            Process process = new ProcessBuilder(command)
                    .directory(Path.of("/").toFile())
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .redirectOutput(out.toFile())
                    .start();
            process.getOutputStream().close();
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return List.of();
            }
            return Files.readString(out, StandardCharsets.UTF_8).lines().toList();
        } catch (IOException | RuntimeException e) {
            return List.of();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        } finally {
            if (out != null) {
                try {
                    Files.deleteIfExists(out);
                } catch (IOException ignored) {
                    // A temp file left behind.
                }
            }
        }
    }
}
