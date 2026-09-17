package network.ike.extension;

import org.apache.maven.api.xml.XmlNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The one binding site that is not a {@link
 * org.apache.maven.api.model.Dependency}: {@code <artifactItem>} entries
 * inside a plugin's configuration (IKE-Network/ike-issues#1019).
 *
 * <p><b>Why this needs its own pass.</b> maven-dependency-plugin's
 * {@code copy} and {@code unpack} goals take their coordinates as
 * <em>configuration parameters</em>, not dependencies — untyped
 * {@link XmlNode} at file-model time. Maven never resolves them as
 * dependencies, and the plugin's own version fill-in reads only the
 * project's {@code <dependencies>} and {@code <dependencyManagement>}
 * (verified against maven-dependency-plugin 3.9.0), so a plugin
 * dependency declaring the same coordinate does not supply a version
 * here. That is why staging lists ended up carrying hand-written
 * {@code <member>.version} pins — the pins a sibling reactor's version
 * rewrite leaves behind.
 *
 * <p><b>What is bound.</b> Only elements literally named
 * {@code artifactItem}, and only when they carry an {@code artifactId},
 * carry no {@code version}, and name a coordinate the working set
 * produces. The narrowness is deliberate: plugin configuration is
 * schema-free, and other plugins model a groupId/artifactId pair with no
 * version field at all, where inserting {@code <version>} would fail the
 * build on an unknown parameter. Widening past {@code artifactItem} is a
 * decision to take on evidence, not by default.
 *
 * <p>Rebuilt nodes keep their attributes and input location, so
 * configuration merging ({@code combine.children}, {@code combine.self})
 * and error reporting behave exactly as they did before binding.
 */
final class ArtifactItems {

    /** The maven-dependency-plugin element this pass recognizes. */
    private static final String ITEM = "artifactItem";

    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String VERSION = "version";

    private ArtifactItems() {}

    /**
     * Supplies the version for a coordinate an {@code <artifactItem>}
     * names — the resolution rule, from wherever it lives.
     */
    @FunctionalInterface
    interface Resolver {

        /**
         * The version to bind for a versionless coordinate.
         *
         * @param groupId    the item's groupId, possibly an
         *                   uninterpolated expression or absent
         * @param artifactId the item's artifactId
         * @return the version to bind, or {@code null} to pass through
         */
        String version(String groupId, String artifactId);
    }

    /**
     * Bind every versionless intra-set {@code <artifactItem>} reachable
     * from a plugin's (or execution's) configuration.
     *
     * @param configuration the configuration node, possibly null
     * @param resolver      the resolution rule
     * @return the rebound configuration, or {@code null} when nothing
     *         needed binding — including when there is no configuration
     */
    static XmlNode bind(XmlNode configuration, Resolver resolver) {
        return configuration == null ? null : walk(configuration, resolver);
    }

    /**
     * Depth-first rebind. Returns {@code null} all the way up when no
     * descendant changed, so an untouched configuration is left as the
     * instance Maven parsed.
     *
     * @param node     the node under consideration
     * @param resolver the resolution rule
     * @return the rebound node, or {@code null} when unchanged
     */
    private static XmlNode walk(XmlNode node, Resolver resolver) {
        if (ITEM.equals(node.getName())) {
            return bindItem(node, resolver);
        }
        List<XmlNode> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            return null;
        }
        List<XmlNode> bound = new ArrayList<>(children.size());
        boolean changed = false;
        for (XmlNode child : children) {
            XmlNode rebound = walk(child, resolver);
            if (rebound == null) {
                bound.add(child);
                continue;
            }
            bound.add(rebound);
            changed = true;
        }
        return changed ? withChildren(node, bound) : null;
    }

    /**
     * Append a {@code <version>} to one {@code <artifactItem>} when the
     * rule has something to say about it. An item that already carries a
     * version is a deliberate pin and passes through — the same courtesy
     * a versioned dependency gets.
     *
     * @param item     the {@code <artifactItem>} node
     * @param resolver the resolution rule
     * @return the rebound item, or {@code null} when unchanged
     */
    private static XmlNode bindItem(XmlNode item, Resolver resolver) {
        if (text(item, VERSION) != null) {
            return null;
        }
        String artifactId = text(item, ARTIFACT_ID);
        if (artifactId == null) {
            return null;
        }
        String resolution = resolver.version(text(item, GROUP_ID), artifactId);
        if (resolution == null) {
            return null;
        }
        List<XmlNode> children = new ArrayList<>(item.getChildren());
        children.add(XmlNode.newInstance(VERSION, resolution));
        return withChildren(item, children);
    }

    /**
     * The text of a named child, or {@code null} when the child is
     * absent or empty.
     *
     * @param node      the parent node
     * @param childName the child element name
     * @return the child's text, or {@code null}
     */
    private static String text(XmlNode node, String childName) {
        XmlNode child = node.getChild(childName);
        if (child == null) {
            return null;
        }
        String value = child.getValue();
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * The same node with a new child list — everything else preserved.
     *
     * @param node     the node to rebuild
     * @param children the replacement children
     * @return the rebuilt node
     */
    private static XmlNode withChildren(XmlNode node, List<XmlNode> children) {
        return XmlNode.newInstance(node.getName(), node.getValue(),
                node.getAttributes(), children, node.getInputLocation());
    }
}
