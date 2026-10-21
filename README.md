# IKE Workspace Extension

[![Maven Central](https://img.shields.io/maven-central/v/network.ike.tooling/ike-workspace-extension)](https://central.sonatype.com/artifact/network.ike.tooling/ike-workspace-extension)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Documentation](https://img.shields.io/badge/docs-ike.network%2Fike--workspace--extension-blue)](https://ike.network/ike-workspace-extension/)
[![IKE Network](https://img.shields.io/badge/IKE-Network-green)](https://ike.network/)

Maven 4 build extension that keeps an IKE working set building as
one tree. It does the work that has to happen inside Maven's core,
before any goal can run or around the whole build session:

- **Missing subprojects.** It prunes `<subprojects>` entries that are
  not on disk yet, so a fresh clone bootstraps with
  `mvn ws:scaffold-init`.
- **Intra-set versions.** It binds every dependency on a working-set
  member to the reactor's version, so the reactor sorts and resolves
  members as one set (`IKE-Network/ike-issues#1019`).
- **Member caches.** A member built from its own root discards its
  stale project-local repository (`IKE-Network/ike-issues#1265`).
- **Build lock and diagnosis.** One build per tree at a time, and a
  diagnosis when a sibling module goes missing mid-build
  (`IKE-Network/ike-issues#1314`).

## Missing subprojects

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

## Intra-set versions

A working set makes its BOM a living member, so an intra-set
dependency declared without a version has none while the model is
built, and Maven's reactor sorter, which matches members by full
identity, creates no edge for it. The extension binds each such
coordinate at file-model time, before the sorter runs: in a build, to
the reactor's current version; in a release
(`-Dike.workspace.release=true`), to the member's last released
version unless the release mission is releasing that member too, read
from `releases/release-<mission>.yaml`. A dependency that already
carries a version passes through, and so does every POM outside a
working set.

## Member caches

Maven 4 resolves a reactor sibling outside the build from the
project-local repository under the nearest `.mvn` root. Every member
of a working set has one so it can build alone, so a member built from
its own root served siblings from a cache its own last build wrote.
On the first model read of such a build, the extension discards that
member's cache, so siblings resolve from the local repository, which
the working set's builds keep current. `-Dike.workspace.memberCache=keep`
keeps it.

## Build lock and missing-module diagnosis

Every working-set build takes an exclusive OS file lock on
`<root>/.mvn/target/build.lock` when project discovery starts and
holds it until the session ends. A second build of the same tree
waits, saying every 30 seconds who holds the lock (pid, host, start
time, goals and `-pl`), and fails after `-Dike.build.lock.timeout`
(default `30m`; `PT45M`, `90s`, `2h` also work) with the holder
named. A member built from its own root locks its working set's
tree. `-Dike.build.lock=false` skips the lock, and so do sessions
whose goals are all read-only (`validate`, `help:*`,
`dependency:tree`/`list`/`resolve`, `ws:*-draft`).

Only the outer Maven session locks: a Maven started by the holder
(`exec`, the invoker, a `ws:` goal running `mvn`) finds its ancestor
in the lock file and proceeds unlocked. The OS releases the lock
when the holder exits, Ctrl-C and crashes included, so a dead build
never wedges the tree. IntelliJ's Maven sync neither takes nor waits
for the lock; IDE builds take it only when delegated to Maven.

When a compile fails with javac `module not found: X`, or a test
fork with `NoClassDefFoundError`/`ClassNotFoundException` for a
reactor sibling's class, the extension logs a diagnosis (and saves it
under `.mvn/target/ike-diagnostics/`): the sibling's `target/classes`,
whether the class or `module-info.class` is there and when it was
written relative to the build's start and to this session's own
compile of the sibling, the file the dependency resolved to (reactor
class directory, packaged jar, project-local repository, or local
repository), and other processes working in the tree. It tells an
outside writer apart from Maven resolving or ordering the sibling
wrongly (MNG-8096).

## Scope

A behavior belongs in this extension only when it must run inside
Maven's core: on the file model before validation and reactor
sorting, or around the build session. Anything a goal can do belongs
in the workspace plugin instead.

The dependency surface stays small. The Maven 4 API jars are at
compile scope, loaded into the extension classloader. `maven-core`
is provided, for the session-level build lock and diagnosis. There
are no `ike-*` runtime dependencies. The IKE conventions it reads
are the working set's own files: `workspace.yaml` and its member
list, the release mission records under `releases/`, and the
reactor root's `.mvn/target`.

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
release cadence.

## Wiring

Declared in a workspace's `.mvn/extensions.xml`:

```xml
<extensions>
    <extension>
        <groupId>network.ike.tooling</groupId>
        <artifactId>ike-workspace-extension</artifactId>
        <version>15</version>
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

The IKE cascade
([`ike-tooling`](https://github.com/IKE-Network/ike-tooling) →
[`ike-docs`](https://github.com/IKE-Network/ike-docs) →
[`ike-platform`](https://github.com/IKE-Network/ike-platform) →
consumers) does not depend on this artifact at build time. It is
consumed at workspace **runtime** via `.mvn/extensions.xml`.

A release of this extension does start one cascade step. On TeamCity,
the ike-platform release is triggered when an extension release
succeeds. Its "align upstream cascade" commit writes the new
extension version into `ike-parent`. A working set then takes it up
when it moves to that `ike-parent` and runs `ws:scaffold-publish`.

## Links

- **Documentation:** [`https://ike.network/ike-workspace-extension/`](https://ike.network/ike-workspace-extension/)
- **Productionization tracking:** [`IKE-Network/ike-issues#460`](https://github.com/IKE-Network/ike-issues/issues/460)
- **Underlying defect:** [`IKE-Network/ike-issues#459`](https://github.com/IKE-Network/ike-issues/issues/459)
- **Issues:** [`IKE-Network/ike-issues`](https://github.com/IKE-Network/ike-issues) (cross-project tracker)
- **Source:** [`IKE-Network/ike-workspace-extension`](https://github.com/IKE-Network/ike-workspace-extension)
