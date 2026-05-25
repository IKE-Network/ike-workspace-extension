---
date_published: 1980-01-31
date_modified: 1980-01-31
canonical_url: https://ike.network/ike-workspace-extension/release-notes.html
---

# Release Notes

## [ike-tooling v209](#ike-tooling-v209)

### [Internal](#internal)

- Unify visual theme across Maven site, JaCoCo, and Javadoc (currently three different themes per project) ([#518](https://github.com/IKE-Network/ike-issues/issues/518)[1])

## [ike-tooling v208](#ike-tooling-v208)

### [Internal](#internal_2)

- Configure maven-javadoc-plugin <links> for cross-module references across foundation apidocs ([#517](https://github.com/IKE-Network/ike-issues/issues/517)[2])

## [ike-base-parent v10](#ike-base-parent-v10)

### [Internal](#internal_3)

- Release ike-base-parent v10 to propagate ike-java-support v1→v2 canonical pin ([#519](https://github.com/IKE-Network/ike-issues/issues/519)[3])

## [ike-tooling v207](#ike-tooling-v207)

### [Internal](#internal_4)

- Clean up stale release-cascade.yaml content (drop unread version-property data; update X.version comments) ([#516](https://github.com/IKE-Network/ike-issues/issues/516)[4])
- Publish Javadoc on ike-tooling and ike-java-support Maven sites ([#513](https://github.com/IKE-Network/ike-issues/issues/513)[5])

## [ike-java-support v2](#ike-java-support-v2)

### [Internal](#internal_5)

- ike-java-support is missing src/main/cascade/release-cascade.yaml ([#515](https://github.com/IKE-Network/ike-issues/issues/515)[6])

## [ike-tooling v206](#ike-tooling-v206)

### [Internal](#internal_6)

- Landing page polish: Kroki dependency diagram + complete site/README for new foundation members ([#511](https://github.com/IKE-Network/ike-issues/issues/511)[7])

## [ike-tooling v198](#ike-tooling-v198)

### [Internal](#internal_7)

- Async Maven Central deploy with sentinel-file status tracking ([#484](https://github.com/IKE-Network/ike-issues/issues/484)[8])

## [ike-tooling v196](#ike-tooling-v196)

### [Internal](#internal_8)

- Nexus-first two-phase deploy with retries in ike:release-publish ([#482](https://github.com/IKE-Network/ike-issues/issues/482)[9])

## [ike-tooling v185](#ike-tooling-v185)

### [Internal](#internal_9)

- Consolidate the AsciiDoc doc-rendering pipeline into ike-doc-maven-plugin ([#437](https://github.com/IKE-Network/ike-issues/issues/437)[10])
- Add Central-required POM metadata (developers, scm); fix stale reactor comment ([#434](https://github.com/IKE-Network/ike-issues/issues/434)[11])
- Re-pin koncept-asciidoc-extension to network.ike.docs groupId ([#432](https://github.com/IKE-Network/ike-issues/issues/432)[12])

## [ike-platform v68](#ike-platform-v68)

### [Internal](#internal_10)

- ws:scaffold-publish report: show parent-cascade from→to and post-run uncommitted state ([#431](https://github.com/IKE-Network/ike-issues/issues/431)[13])

## [ike-tooling v183](#ike-tooling-v183)

### [Internal](#internal_11)

- URL-mode cascade resolver — assemble the release cascade without local sibling checkouts ([#429](https://github.com/IKE-Network/ike-issues/issues/429)[14])
- Fail ike:release-publish on preflight warnings by default; add ike.release.ignoreWarnings ([#428](https://github.com/IKE-Network/ike-issues/issues/428)[15])

## [ike-tooling v182](#ike-tooling-v182)

### [Internal](#internal_12)

- Decentralize the release cascade: per-project manifests, loosely coupled ([#420](https://github.com/IKE-Network/ike-issues/issues/420)[16])
- Complete the ike:-tier release-cascade capability (executor, alignment, terminal marker, POM wiring) ([#419](https://github.com/IKE-Network/ike-issues/issues/419)[17])

## [ike-tooling v180](#ike-tooling-v180)

### [Fixes](#fixes)

- Foundation-drift report mislabels 'ahead' projects as 'behind'; no direction, no explanation ([#412](https://github.com/IKE-Network/ike-issues/issues/412)[18])

### [Internal](#internal_13)

- Developer environment setup guide in ike-build-standards + scaffold-enforced README link ([#410](https://github.com/IKE-Network/ike-issues/issues/410)[19])

## [ike-platform v58](#ike-platform-v58)

### [Fixes](#fixes_2)

- workspace.yaml: ws:checkpoint emits duplicate sha keys instead of replacing ([#387](https://github.com/IKE-Network/ike-issues/issues/387)[20])

### [Enhancements](#enhancements)

- Document checkpoint vs release issue handling: checkpoints report, releases close ([#394](https://github.com/IKE-Network/ike-issues/issues/394)[21])
- Collapse 15 workspace goals into 3 scaffold-* goals via convergence pattern ([#393](https://github.com/IKE-Network/ike-issues/issues/393)[22])

## [ike-tooling v175](#ike-tooling-v175)

### [Fixes](#fixes_3)

- IKE-WORKSPACE.md references archived network.ike.pipeline pluginGroup ([#389](https://github.com/IKE-Network/ike-issues/issues/389)[23])

### [Enhancements](#enhancements_2)

- ike:* site lifecycle convergence: 7 goals → 2 (site-draft / site-publish) ([#398](https://github.com/IKE-Network/ike-issues/issues/398)[24])
- Standards: every standards-change issue must include a Documentation Impact section ([#396](https://github.com/IKE-Network/ike-issues/issues/396)[25])
- Release preflight: verify gh permissions and pending-release label setup for issue-management workflow ([#392](https://github.com/IKE-Network/ike-issues/issues/392)[26])
- Release process should remove pending-release label from resolved issues ([#390](https://github.com/IKE-Network/ike-issues/issues/390)[27])
- Build standards: add commit-message issue-association standard ([#388](https://github.com/IKE-Network/ike-issues/issues/388)[28])

## [ike-pipeline 111](#ike-pipeline-111)

### [Internal](#internal_14)

- ike-pipeline: port to ike-tooling 127 — SubprojectType removal ([#228](https://github.com/IKE-Network/ike-issues/issues/228)[29])
