package network.ike.extension;

import org.apache.maven.api.di.Named;
import org.apache.maven.api.di.Singleton;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.spi.ModelTransformer;
import org.apache.maven.api.spi.ModelTransformerException;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Discards a member's own project-local repository when the member is built from its own
 * root inside a working set ({@link MemberCache}), once per session, on the first model read.
 * Models are read before anything resolves from the cache, so the first transformation is
 * early enough; the model itself passes through unchanged.
 *
 * <p>The root directory is the one the launcher names in
 * {@code maven.multiModuleProjectDirectory}, the nearest {@code .mvn} directory, which is also
 * where Maven 4 keeps the cache.
 */
@Named("ike-workspace-member-cache")
@Singleton
public class MemberCacheTransformer implements ModelTransformer {

    /** The system property the Maven launcher sets to the build's root directory. */
    static final String ROOT_DIRECTORY_PROPERTY = "maven.multiModuleProjectDirectory";

    private final AtomicBoolean done = new AtomicBoolean();

    public MemberCacheTransformer() {}

    @Override
    public Model transformFileModel(Model model) throws ModelTransformerException {
        if (done.compareAndSet(false, true)) {
            String root = System.getProperty(ROOT_DIRECTORY_PROPERTY);
            if (root != null && !root.isBlank()) {
                MemberCache.discard(Path.of(root));
            }
        }
        return model;
    }
}
