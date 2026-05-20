# IKE Workspace Extension

[![Maven Central](https://img.shields.io/maven-central/v/network.ike.tooling/ike-workspace-extension)](https://central.sonatype.com/artifact/network.ike.tooling/ike-workspace-extension)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Documentation](https://img.shields.io/badge/docs-ike.network%2Fike--workspace--extension-blue)](https://ike.network/ike-workspace-extension/)
[![IKE Network](https://img.shields.io/badge/IKE-Network-green)](https://ike.network/)

Maven 4 build extension that prunes non-existent `<subprojects>`
entries from IKE workspace POMs before model validation. Lets a
fresh clone of any IKE workspace bootstrap with `mvn
ws:scaffold-init` even when no subproject directory is on disk yet.

## What it does

When Maven 4 reads a workspace POM declaring six subprojects but
the working tree only has the workspace root (because the
operator just ran `git clone` and has not yet populated
subprojects), Maven's model validator fatally aborts the build
with *"Child subproject X does not exist"* — before any goal can
run. That makes the documented bootstrap (`git clone && cd && mvn
ws:scaffold-init`) impossible. See
[`IKE-Network/ike-issues#459`](https://github.com/IKE-Network/ike-issues/issues/459).

This extension implements
`org.apache.maven.api.spi.ModelTransformer.transformFileModel`,
which fires **before** profile activation and subproject
existence validation. For any POM whose directory has a sibling
`workspace.yaml` (the "this is a workspace POM" marker), the
transformer:

1. Removes `<subprojects>` entries whose `<sub>/pom.xml` does not
   exist on disk.
2. Removes file-activated profiles whose `<file><exists>` target
   is missing — so any `-P?with-X` flag on the command line
   silently no-ops on the missing profile name.

Every other POM passes through untouched.

## Stability commitment

This artifact does exactly one thing and never grows. The
dependency surface stays at the Maven 4 API jars (compile scope,
loaded into the extension classloader). No `ike-*` runtime
dependencies, no business logic, no knowledge of IKE conventions
beyond *"sibling `workspace.yaml` marks a workspace POM."*

Inherits from
[`network.ike:ike-base-parent`](https://github.com/IKE-Network/ike-base-parent)
(Tier 0 — publishing metadata, GPG signing, Maven Central
deployment). Deliberately **does not** inherit from
`network.ike.platform:ike-parent` (Tier 2 — build conventions and
plugin management for the ike-platform reactor): coupling there
would re-introduce the version churn the standalone repo exists to
avoid.

Versioned independently of the
[`ike-platform`](https://github.com/IKE-Network/ike-platform)
release cadence. If we ever need different behavior, we add a
separate extension rather than evolving this one.

## Wiring

Declared in a workspace's `.mvn/extensions.xml`:

```xml
<extensions>
    <extension>
        <groupId>network.ike.tooling</groupId>
        <artifactId>ike-workspace-extension</artifactId>
        <version>1</version>
    </extension>
</extensions>
```

The [`ike-platform`](https://github.com/IKE-Network/ike-platform)
workspace plugin's `ws:scaffold-init` writes this entry into new
workspaces automatically; `ws:scaffold-publish` keeps it in sync
on existing workspaces. The version is sourced from a property
declared in `ike-parent` — operators do not maintain it by hand.

## Build

```bash
mvn clean install
```

## Release Cascade

This artifact stands **outside** the IKE release cascade by
design. Its contract is intentionally small and stable; coupling
it to the cascade would re-introduce the version churn we are
trying to eliminate. It is published independently when (rarely)
its behavior contract changes.

The IKE cascade
([`ike-tooling`](https://github.com/IKE-Network/ike-tooling) →
[`ike-docs`](https://github.com/IKE-Network/ike-docs) →
[`ike-platform`](https://github.com/IKE-Network/ike-platform) →
consumers) does not depend on this artifact at build time. It is
consumed at workspace **runtime** via `.mvn/extensions.xml`, so a
cascade release does not have to wait for or trigger an extension
release.

## Links

- **Documentation:** [`https://ike.network/ike-workspace-extension/`](https://ike.network/ike-workspace-extension/)
- **Productionization tracking:** [`IKE-Network/ike-issues#460`](https://github.com/IKE-Network/ike-issues/issues/460)
- **Underlying defect:** [`IKE-Network/ike-issues#459`](https://github.com/IKE-Network/ike-issues/issues/459)
- **Issues:** [`IKE-Network/ike-issues`](https://github.com/IKE-Network/ike-issues) (cross-project tracker)
- **Source:** [`IKE-Network/ike-workspace-extension`](https://github.com/IKE-Network/ike-workspace-extension)
