---
date_published: 1980-01-31
date_modified: 1980-01-31
canonical_url: https://github.com/IKE-Network/ike-workspace-extension/index.html
---

# IKE Workspace Extension

[https://central.sonatype.com/artifact/network.ike.tooling/ike-workspace-extension](https://central.sonatype.com/artifact/network.ike.tooling/ike-workspace-extension)[1]

`network.ike.tooling:ike-workspace-extension` is a Maven 4 build extension that prunes non-existent `<subprojects>` entries from IKE workspace POMs before model validation. Lets a fresh clone of any IKE workspace bootstrap with `mvn ws:scaffold-init` even when no subproject directory is on disk yet.

## [#what-it-does](#what-it-does)What it does

When Maven 4 reads a workspace POM declaring several subprojects but the working tree only has the workspace root (because the operator just ran `git clone` and has not yet populated subprojects), Maven’s model validator fatally aborts the build with **"Child subproject X does not exist"** — before any goal can run. That makes the documented bootstrap (`git clone && cd && mvn ws:scaffold-init`) impossible. See [IKE-Network/ike-issues#459](https://github.com/IKE-Network/ike-issues/issues/459)[2].

This extension implements `org.apache.maven.api.spi.ModelTransformer.transformFileModel`, which fires **before** profile activation and subproject existence validation. For any POM whose directory has a sibling `workspace.yaml` (the "this is a workspace POM" marker), the transformer:

1. Removes `<subprojects>` entries whose `<sub>/pom.xml` does not exist on disk.
2. Removes file-activated profiles whose `<file><exists>` target is missing — so any `-P?with-X` flag on the command line silently no-ops on the missing profile name.

Every other POM passes through untouched.

## [#stability-commitment](#stability-commitment)Stability commitment

This artifact does exactly one thing and never grows. The dependency surface stays at the Maven 4 API jars (compile scope, loaded into the extension classloader). No `ike-*` runtime dependencies, no business logic, no knowledge of IKE conventions beyond *"sibling `workspace.yaml` marks a workspace POM."*

Versioned independently of the [ike-platform](https://github.com/IKE-Network/ike-platform)[3] release cadence. If we ever need different behavior, we add a separate extension rather than evolving this one.

## [#wiring](#wiring)Wiring

Declared in a workspace’s `.mvn/extensions.xml`:

```
<extensions>
    <extension>
        <groupId>network.ike.tooling</groupId>
        <artifactId>ike-workspace-extension</artifactId>
        <version>1</version>
    </extension>
</extensions>
```

The ike-platform workspace plugin’s `ws:scaffold-init` writes this entry into new workspaces automatically; `ws:scaffold-publish` keeps it in sync on existing workspaces. The version is sourced from a property declared in `ike-parent` — operators do not maintain it by hand.

## [#coordinates](#coordinates)Coordinates

```
<dependency>
    <groupId>network.ike.tooling</groupId>
    <artifactId>ike-workspace-extension</artifactId>
    <version>1</version>
</dependency>
```

Available from Maven Central.

## [#tracking](#tracking)Tracking

- [IKE-Network/ike-issues#460](https://github.com/IKE-Network/ike-issues/issues/460)[4] — productionization plan
- [IKE-Network/ike-issues#459](https://github.com/IKE-Network/ike-issues/issues/459)[2] — underlying defect this extension fixes
