package network.ike.extension;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The working set's manifest, under whichever name it carries.
 *
 * <p>The file is {@code working-set.yaml}; a working set the scaffold has
 * not upgraded yet still carries {@code workspace.yaml}, read forever
 * because manifests written before the rename live in tagged trees
 * (IKE-Network/ike-issues#1054).
 *
 * <p>The names are spelled here rather than borrowed from
 * {@code network.ike.workspace.WorkingSetResolver}: this extension loads
 * into Maven's core before any project is built and deliberately depends
 * on nothing but the Maven API, so it cannot reach the workspace model.
 * Keep the two in step.
 */
final class Manifests {

    /** The manifest file name. */
    static final String MANIFEST_FILE = "working-set.yaml";

    /** The manifest's former name, still read. */
    static final String LEGACY_MANIFEST_FILE = "workspace.yaml";

    private Manifests() {}

    /**
     * The manifest a directory holds, current name preferred.
     *
     * @param dir the directory to look in
     * @return the manifest path, or {@code null} when the directory holds
     *         neither
     */
    static Path in(Path dir) {
        Path current = dir.resolve(MANIFEST_FILE);
        if (Files.exists(current)) {
            return current;
        }
        Path legacy = dir.resolve(LEGACY_MANIFEST_FILE);
        return Files.exists(legacy) ? legacy : null;
    }

    /**
     * Whether a directory is a working-set root.
     *
     * @param dir the directory to test
     * @return {@code true} when it holds a manifest under either name
     */
    static boolean exists(Path dir) {
        return in(dir) != null;
    }
}
