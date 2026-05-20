package network.ike.extension;

import org.apache.maven.api.di.Named;
import org.apache.maven.api.di.Singleton;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Profile;
import org.apache.maven.api.spi.ModelTransformer;
import org.apache.maven.api.spi.ModelTransformerException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Maven 4 build extension that prunes a workspace POM's
 * {@code <subprojects>} and file-activated profiles whose targets
 * are missing from disk, <em>before</em> Maven's model validator
 * gets a chance to fail on "Child subproject X does not exist".
 *
 * <p>Without this extension, a fresh {@code git clone} of an IKE
 * workspace cannot run {@code mvn ws:scaffold-init} (or any goal)
 * because the workspace POM declares subprojects that have not yet
 * been cloned into place. Maven 4 reads the POM, sees the declared
 * subproject directories, validates them, and fails before any
 * goal can execute. The {@code <activation><file><exists>} guard
 * that earlier workspaces used to gate profile activation does not
 * survive an explicit {@code -P?with-X} CLI flag — and the
 * managed {@code -P?} block in {@code .mvn/maven.config} (added so
 * IntelliJ would see the profiles activated, per IKE-Network/
 * ike-issues#276) force-activates them regardless. The chicken-
 * and-egg in IKE-Network/ike-issues#459.
 *
 * <p>This transformer hooks into Maven 4's
 * {@link ModelTransformer#transformFileModel(Model)} SPI, which
 * fires <em>before</em> profile activation and subproject existence
 * validation. Two pruning passes are applied to any POM whose
 * directory contains a sibling {@code workspace.yaml} (the
 * "this is a workspace POM" marker):
 *
 * <ol>
 *   <li>Top-level {@code <subprojects>}: each entry whose
 *       {@code <subproject-dir>/pom.xml} does not exist on disk is
 *       removed. This lets workspace POMs declare all subprojects
 *       unconditionally at top level, with no {@code with-*}
 *       profile machinery.</li>
 *   <li>File-activated profiles: each profile whose
 *       {@code <activation><file><exists>} target does not exist is
 *       removed from the model entirely. Profile activation runs
 *       after this transformer, sees no such profile, and merges
 *       nothing. Any matching {@code -P?with-X} flag silently no-
 *       ops on a missing profile name. This keeps the transformer
 *       backward-compatible with legacy workspaces still using the
 *       profile pattern, before migration.</li>
 * </ol>
 *
 * <p>Every other POM (subproject POMs, parent POMs, external
 * consumers) passes through untouched.
 *
 * <p>The extension is registered in the workspace's
 * {@code .mvn/extensions.xml} and loaded by Maven at startup. Its
 * version is managed by {@code ws:scaffold-publish} on the IKE
 * platform side; this artifact itself stays at version {@code 1}
 * unless its behavior contract changes.
 */
@Named("ike-workspace-subproject-prune")
@Singleton
public class SubprojectPruneTransformer implements ModelTransformer {

    /** Creates the transformer. DI-only; not for direct construction. */
    public SubprojectPruneTransformer() {}

    /**
     * Prune missing subprojects and missing-target profiles from the
     * file-stage model. No-op for any POM that lacks a sibling
     * {@code workspace.yaml}.
     *
     * @param model the file-stage parsed Maven model
     * @return the (possibly pruned) model — same instance when
     *         nothing was pruned
     * @throws ModelTransformerException never thrown; declared for
     *                                    SPI conformance
     */
    @Override
    public Model transformFileModel(Model model) throws ModelTransformerException {
        Path pomPath = model.getPomFile();
        if (pomPath == null) {
            return model;
        }
        Path projectDir = pomPath.getParent();
        if (projectDir == null) {
            return model;
        }
        if (!Files.exists(projectDir.resolve("workspace.yaml"))) {
            return model;
        }

        Model result = model;
        result = pruneTopLevelSubprojects(result, projectDir);
        result = pruneMissingTargetProfiles(result, projectDir);
        return result;
    }

    private static Model pruneTopLevelSubprojects(Model model, Path projectDir) {
        List<String> subs = model.getSubprojects();
        if (subs == null || subs.isEmpty()) {
            return model;
        }
        List<String> kept = new ArrayList<>(subs.size());
        List<String> dropped = new ArrayList<>();
        for (String sub : subs) {
            if (Files.exists(projectDir.resolve(sub).resolve("pom.xml"))) {
                kept.add(sub);
            } else {
                dropped.add(sub);
            }
        }
        if (dropped.isEmpty()) {
            return model;
        }
        System.err.println("[ike-workspace-extension] pruned missing <subprojects>: "
                + dropped);
        return model.withSubprojects(kept);
    }

    private static Model pruneMissingTargetProfiles(Model model, Path projectDir) {
        List<Profile> profiles = model.getProfiles();
        if (profiles == null || profiles.isEmpty()) {
            return model;
        }
        List<Profile> kept = new ArrayList<>(profiles.size());
        List<String> dropped = new ArrayList<>();
        for (Profile p : profiles) {
            if (hasMissingFileActivation(p, projectDir)) {
                dropped.add(p.getId());
            } else {
                kept.add(p);
            }
        }
        if (dropped.isEmpty()) {
            return model;
        }
        System.err.println("[ike-workspace-extension] stripped profiles with"
                + " missing <file><exists> targets: " + dropped);
        return model.withProfiles(kept);
    }

    private static boolean hasMissingFileActivation(Profile p, Path projectDir) {
        if (p.getActivation() == null || p.getActivation().getFile() == null) {
            return false;
        }
        String exists = p.getActivation().getFile().getExists();
        if (exists == null || exists.isBlank()) {
            return false;
        }
        String resolved = exists.replace("${project.basedir}", projectDir.toString());
        return !Files.exists(Path.of(resolved));
    }
}
