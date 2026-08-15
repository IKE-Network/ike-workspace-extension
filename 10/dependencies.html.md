---
date_published: 1980-01-31
date_modified: 1980-01-31
canonical_url: https://ike.network/ike-workspace-extension/dependencies.html
---

# Project Dependencies

## [compile](#compile)

The following is a list of compile dependencies for this project. These dependencies are required to compile and run the application:

| GroupId | ArtifactId | Version | Type | Licenses |
| --- | --- | --- | --- | --- |
| org.apache.maven | [maven-api-core](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-core/)[1] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-di](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-di/)[3] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-model](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-model/)[4] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-spi](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-spi/)[5] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |

## [test](#test)

The following is a list of test dependencies for this project. These dependencies are only required to compile and run unit tests for the application:

| GroupId | ArtifactId | Version | Type | Licenses |
| --- | --- | --- | --- | --- |
| org.junit.jupiter | [junit-jupiter](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |

## [provided](#provided)

The following is a list of provided dependencies for this project. These dependencies are required to compile the application, but should be provided by default when using the library:

| GroupId | ArtifactId | Version | Classifier | Type | Licenses |
| --- | --- | --- | --- | --- | --- |
| network.ike | [ike-base-parent](https://ike.network/ike-base-parent/)[8] | 15 | site-theme | zip | [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |

# Project Transitive Dependencies

The following is a list of transitive dependencies for this project. Transitive dependencies are the dependencies of the project dependencies.

## [compile](#compile_2)

The following is a list of compile dependencies for this project. These dependencies are required to compile and run the application:

| GroupId | ArtifactId | Version | Type | Licenses |
| --- | --- | --- | --- | --- |
| org.apache.maven | [maven-api-annotations](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-annotations/)[9] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-plugin](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-plugin/)[10] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-settings](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-settings/)[11] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-toolchain](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-toolchain/)[12] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-xml](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-xml/)[13] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |

## [test](#test_2)

The following is a list of test dependencies for this project. These dependencies are only required to compile and run unit tests for the application:

| GroupId | ArtifactId | Version | Type | Licenses |
| --- | --- | --- | --- | --- |
| org.apiguardian | [apiguardian-api](https://github.com/apiguardian-team/apiguardian)[14] | 1.1.2 | jar | [The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[15] |
| org.jspecify | [jspecify](http://jspecify.org/)[16] | 1.0.0 | jar | [The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[15] |
| org.junit.jupiter | [junit-jupiter-api](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.junit.jupiter | [junit-jupiter-engine](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.junit.jupiter | [junit-jupiter-params](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.junit.platform | [junit-platform-commons](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.junit.platform | [junit-platform-engine](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.opentest4j | [opentest4j](https://github.com/ota4j-team/opentest4j)[17] | 1.3.0 | jar | [The Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |

# Project Dependency Graph

## [Dependency Tree](#dependency-tree)

- network.ike.tooling:ike-workspace-extension:jar:10 ** 
  
  | IKE Workspace Extension |
  | --- |
  | **Description: **Maven 4 build extension that prunes non-existent <subprojects> entries from workspace POMs before model validation. Lets a fresh clone of an IKE workspace bootstrap with mvn ws:scaffold-init before any subproject directory is on disk. **URL: **[https://ike.network/ike-workspace-extension/](https://ike.network/ike-workspace-extension/)[18] **Project Licenses: **[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
  
    - org.apache.maven:maven-api-spi:jar:4.0.0-rc-5 (compile) ** 
      
      | Maven 4 API :: SPI |
      | --- |
      | **Description: **Maven 4 API - Maven SPI. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-spi/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-spi/)[5] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
      
          - org.apache.maven:maven-api-annotations:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: Meta annotations |
            | --- |
            | **Description: **Maven 4 API - Java meta annotations. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-annotations/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-annotations/)[9] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
    - org.junit.jupiter:junit-jupiter:jar:6.0.0 (test) ** 
      
      | JUnit Jupiter (Aggregator) |
      | --- |
      | **Description: **Module "junit-jupiter" of JUnit **URL: **[https://junit.org/](https://junit.org/)[6] **Project Licenses: **[Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
      
          - org.junit.jupiter:junit-jupiter-api:jar:6.0.0 (test) ** 
            
            | JUnit Jupiter API |
            | --- |
            | **Description: **Module "junit-jupiter-api" of JUnit **URL: **[https://junit.org/](https://junit.org/)[6] **Project Licenses: **[Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
            
                  - org.opentest4j:opentest4j:jar:1.3.0 (test) ** 
                    
                    | org.opentest4j:opentest4j |
                    | --- |
                    | **Description: **Open Test Alliance for the JVM **URL: **[https://github.com/ota4j-team/opentest4j](https://github.com/ota4j-team/opentest4j)[17] **Project Licenses: **[The Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
                  - org.junit.platform:junit-platform-commons:jar:6.0.0 (test) ** 
                    
                    | JUnit Platform Commons |
                    | --- |
                    | **Description: **Module "junit-platform-commons" of JUnit **URL: **[https://junit.org/](https://junit.org/)[6] **Project Licenses: **[Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
                  - org.apiguardian:apiguardian-api:jar:1.1.2 (test) ** 
                    
                    | org.apiguardian:apiguardian-api |
                    | --- |
                    | **Description: **@API Guardian **URL: **[https://github.com/apiguardian-team/apiguardian](https://github.com/apiguardian-team/apiguardian)[14] **Project Licenses: **[The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[15] |
                  - org.jspecify:jspecify:jar:1.0.0 (test) ** 
                    
                    | JSpecify annotations |
                    | --- |
                    | **Description: **An artifact of well-named and well-specified annotations to power static analysis checks **URL: **[http://jspecify.org/](http://jspecify.org/)[16] **Project Licenses: **[The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[15] |
          - org.junit.jupiter:junit-jupiter-params:jar:6.0.0 (test) ** 
            
            | JUnit Jupiter Params |
            | --- |
            | **Description: **Module "junit-jupiter-params" of JUnit **URL: **[https://junit.org/](https://junit.org/)[6] **Project Licenses: **[Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
          - org.junit.jupiter:junit-jupiter-engine:jar:6.0.0 (test) ** 
            
            | JUnit Jupiter Engine |
            | --- |
            | **Description: **Module "junit-jupiter-engine" of JUnit **URL: **[https://junit.org/](https://junit.org/)[6] **Project Licenses: **[Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
            
                  - org.junit.platform:junit-platform-engine:jar:6.0.0 (test) ** 
                    
                    | JUnit Platform Engine API |
                    | --- |
                    | **Description: **Module "junit-platform-engine" of JUnit **URL: **[https://junit.org/](https://junit.org/)[6] **Project Licenses: **[Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
    - org.apache.maven:maven-api-core:jar:4.0.0-rc-5 (compile) ** 
      
      | Maven 4 API :: Core |
      | --- |
      | **Description: **Maven 4 API - Maven Core API **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-core/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-core/)[1] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
      
          - org.apache.maven:maven-api-settings:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: Settings |
            | --- |
            | **Description: **Maven 4 API - Immutable Settings model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-settings/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-settings/)[11] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-api-toolchain:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: Toolchain |
            | --- |
            | **Description: **Maven 4 API - Immutable Toolchain model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-toolchain/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-toolchain/)[12] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-api-plugin:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: Plugin |
            | --- |
            | **Description: **Maven 4 API - Immutable Plugin model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-plugin/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-plugin/)[10] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-api-xml:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: XML |
            | --- |
            | **Description: **Maven 4 API - Immutable XML. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-xml/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-xml/)[13] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
    - org.apache.maven:maven-api-model:jar:4.0.0-rc-5 (compile) ** 
      
      | Maven 4 API :: Model |
      | --- |
      | **Description: **Maven 4 API - Immutable Model for Maven POM (Project Object Model). **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-model/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-model/)[4] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
    - org.apache.maven:maven-api-di:jar:4.0.0-rc-5 (compile) ** 
      
      | Maven 4 API :: Dependency Injection |
      | --- |
      | **Description: **Maven 4 API - Dependency Injection **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-di/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-di/)[3] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
    - network.ike:ike-base-parent:zip:site-theme:15 (provided) ** 
      
      | IKE Base Parent |
      | --- |
      | **Description: **Tier 0 foundation parent for the IKE Network — the apex of the parent inheritance forest, inherited by ike-tooling, ike-docs, and ike-platform. Carries shared publishing metadata, GPG signing, and Maven Central publishing configuration. **URL: **[https://ike.network/ike-base-parent/](https://ike.network/ike-base-parent/)[8] **Project Licenses: **[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |

# Licenses

**The Apache License, Version 2.0: **JSpecify annotations, org.apiguardian:apiguardian-api, org.opentest4j:opentest4j

**Apache License, Version 2.0: **IKE Base Parent, IKE Workspace Extension

**Apache-2.0: **Maven 4 API :: Core, Maven 4 API :: Dependency Injection, Maven 4 API :: Meta annotations, Maven 4 API :: Model, Maven 4 API :: Plugin, Maven 4 API :: SPI, Maven 4 API :: Settings, Maven 4 API :: Toolchain, Maven 4 API :: XML

**Eclipse Public License v2.0: **JUnit Jupiter (Aggregator), JUnit Jupiter API, JUnit Jupiter Engine, JUnit Jupiter Params, JUnit Platform Commons, JUnit Platform Engine API

# Dependency File Details

| Total | Size | Entries | Classes | Packages | Java Version | Debug Information |
| --- | --- | --- | --- | --- | --- | --- |
| ike-base-parent-15-site-theme.zip | 3.4 kB | - | - | - | - | - |
| maven-api-annotations-4.0.0-rc-5.jar | 13.1 kB | 27 | 12 | 1 | 17 | Yes |
| maven-api-core-4.0.0-rc-5.jar | 218.3 kB | 257 | 237 | 7 | 17 | Yes |
| maven-api-di-4.0.0-rc-5.jar | 16.4 kB | 32 | 13 | 2 | 17 | Yes |
| maven-api-model-4.0.0-rc-5.jar | 222.2 kB | 128 | 113 | 1 | 17 | Yes |
| maven-api-plugin-4.0.0-rc-5.jar | 82.1 kB | 77 | 60 | 2 | 17 | Yes |
| maven-api-settings-4.0.0-rc-5.jar | 84.7 kB | 67 | 52 | 1 | 17 | Yes |
| maven-api-spi-4.0.0-rc-5.jar | 15.2 kB | 30 | 14 | 1 | 17 | Yes |
| maven-api-toolchain-4.0.0-rc-5.jar | 41.5 kB | 45 | 30 | 1 | 17 | Yes |
| maven-api-xml-4.0.0-rc-5.jar | 36.5 kB | 42 | 27 | 1 | 17 | Yes |
| apiguardian-api-1.1.2.jar | 6.8 kB | 9 | 3 | 2 | 1.6 | Yes |
| jspecify-1.0.0.jar | 3.8 kB | 14 | - | - | - | - |
|    • Root | - | 10 | 4 | 1 | 1.8 | No |
|    • Versioned | - | 4 | 1 | 1 | 9 | No |
| junit-jupiter-6.0.0.jar | 6.4 kB | 5 | 1 | 1 | 17 | No |
| junit-jupiter-api-6.0.0.jar | 249.9 kB | 224 | 208 | 9 | 17 | Yes |
| junit-jupiter-engine-6.0.0.jar | 353.7 kB | 188 | 171 | 9 | 17 | Yes |
| junit-jupiter-params-6.0.0.jar | 293.7 kB | 215 | 194 | 9 | 17 | Yes |
| junit-platform-commons-6.0.0.jar | 171.1 kB | 103 | 87 | 10 | 17 | Yes |
| junit-platform-engine-6.0.0.jar | 277.6 kB | 193 | 175 | 9 | 17 | Yes |
| opentest4j-1.3.0.jar | 14.3 kB | 15 | 9 | 2 | 1.6 | Yes |
| 19 | 2.1 MB | 1671 | 1410 | 69 | 17 | 16 |
| compile: 9 | compile: 730 kB | compile: 705 | compile: 558 | compile: 17 | 17 | compile: 9 |
| provided: 1 | provided: 3.4 kB | - | - | - | - |
| test: 9 | test: 1.4 MB | test: 966 | test: 852 | test: 52 | 17 | test: 7 |
