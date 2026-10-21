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
| org.slf4j | [slf4j-simple](http://www.slf4j.org)[8] | 2.0.17 | jar | [MIT](https://opensource.org/license/mit)[9] |

## [provided](#provided)

The following is a list of provided dependencies for this project. These dependencies are required to compile the application, but should be provided by default when using the library:

| GroupId | ArtifactId | Version | Classifier | Type | Licenses |
| --- | --- | --- | --- | --- | --- |
| javax.inject | [javax.inject](http://code.google.com/p/atinject/)[10] | 1 | - | jar | [The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[11] |
| network.ike | [ike-base-parent](https://ike.network/ike-base-parent/)[12] | 15 | site-theme | zip | [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-core](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-core/)[13] | 4.0.0-rc-5 | - | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.slf4j | [slf4j-api](http://www.slf4j.org)[8] | 2.0.17 | - | jar | [MIT](https://opensource.org/license/mit)[9] |

# Project Transitive Dependencies

The following is a list of transitive dependencies for this project. Transitive dependencies are the dependencies of the project dependencies.

## [compile](#compile_2)

The following is a list of compile dependencies for this project. These dependencies are required to compile and run the application:

| GroupId | ArtifactId | Version | Type | Licenses |
| --- | --- | --- | --- | --- |
| org.apache.maven | [maven-api-annotations](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-annotations/)[14] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-plugin](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-plugin/)[15] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-settings](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-settings/)[16] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-toolchain](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-toolchain/)[17] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-xml](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-xml/)[18] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |

## [test](#test_2)

The following is a list of test dependencies for this project. These dependencies are only required to compile and run unit tests for the application:

| GroupId | ArtifactId | Version | Type | Licenses |
| --- | --- | --- | --- | --- |
| org.apiguardian | [apiguardian-api](https://github.com/apiguardian-team/apiguardian)[19] | 1.1.2 | jar | [The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[11] |
| org.jspecify | [jspecify](http://jspecify.org/)[20] | 1.0.0 | jar | [The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[11] |
| org.junit.jupiter | [junit-jupiter-api](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.junit.jupiter | [junit-jupiter-engine](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.junit.jupiter | [junit-jupiter-params](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.junit.platform | [junit-platform-commons](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.junit.platform | [junit-platform-engine](https://junit.org/)[6] | 6.0.0 | jar | [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.opentest4j | [opentest4j](https://github.com/ota4j-team/opentest4j)[21] | 1.3.0 | jar | [The Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |

## [provided](#provided_2)

The following is a list of provided dependencies for this project. These dependencies are required to compile the application, but should be provided by default when using the library:

| GroupId | ArtifactId | Version | Type | Licenses |
| --- | --- | --- | --- | --- |
| com.fasterxml.woodstox | [woodstox-core](https://github.com/FasterXML/woodstox)[22] | 7.1.1 | jar | [The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[11] |
| com.google.code.gson | [gson](https://github.com/google/gson)[23] | 2.13.2 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-api-metadata](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-metadata/)[24] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-artifact](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-artifact/)[25] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-builder-support](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-builder-support/)[26] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-di](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-di/)[27] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-impl](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-impl/)[28] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-jline](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-jline/)[29] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-logging](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-logging/)[30] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-model](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-model/)[31] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-model-builder](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-model-builder/)[32] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-plugin-api](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-plugin-api/)[33] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-repository-metadata](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-repository-metadata/)[34] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-settings](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-settings/)[35] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-support](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-support/)[36] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-toolchain-model](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-toolchain-model/)[37] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven | [maven-xml](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-xml/)[38] | 4.0.0-rc-5 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven.resolver | [maven-resolver-api](https://maven.apache.org/resolver/maven-resolver-api/)[39] | 2.0.13 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven.resolver | [maven-resolver-connector-basic](https://maven.apache.org/resolver/maven-resolver-connector-basic/)[40] | 2.0.13 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven.resolver | [maven-resolver-impl](https://maven.apache.org/resolver/maven-resolver-impl/)[41] | 2.0.13 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven.resolver | [maven-resolver-named-locks](https://maven.apache.org/resolver/maven-resolver-named-locks/)[42] | 2.0.13 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven.resolver | [maven-resolver-spi](https://maven.apache.org/resolver/maven-resolver-spi/)[43] | 2.0.13 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.apache.maven.resolver | [maven-resolver-util](https://maven.apache.org/resolver/maven-resolver-util/)[44] | 2.0.13 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.codehaus.plexus | [plexus-classworlds](https://codehaus-plexus.github.io/plexus-classworlds/)[45] | 2.9.0 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.codehaus.plexus | [plexus-interpolation](https://codehaus-plexus.github.io/plexus-pom/plexus-interpolation/)[46] | 1.28 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.codehaus.plexus | [plexus-sec-dispatcher](https://codehaus-plexus.github.io/plexus-pom/plexus-sec-dispatcher/)[47] | 4.1.0 | jar | [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.codehaus.plexus | [plexus-utils](https://codehaus-plexus.github.io/plexus-utils/)[48] | 4.0.2 | jar | [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.codehaus.plexus | [plexus-xml](https://codehaus-plexus.github.io/plexus-xml/)[49] | 4.1.0 | jar | [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
| org.codehaus.woodstox | [stax2-api](http://github.com/FasterXML/stax2-api)[50] | 4.2.2 | jar | [The BSD 2-Clause License](http://www.opensource.org/licenses/bsd-license.php)[51] |
| org.eclipse.sisu | [org.eclipse.sisu.plexus](https://eclipse.dev/sisu/org.eclipse.sisu.plexus/)[52] | 0.9.0.M4 | jar | [Eclipse Public License, Version 2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
| org.jline | [jansi-core](https://github.com/jline/jline3/jansi-core)[53] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
| org.jline | [jline-builtins](https://github.com/jline/jline3/jline-builtins)[55] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
| org.jline | [jline-console](https://github.com/jline/jline3/jline-console)[56] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
| org.jline | [jline-console-ui](https://github.com/jline/jline3/jline-console-ui)[57] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
| org.jline | [jline-native](https://github.com/jline/jline3/jline-native)[58] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
| org.jline | [jline-reader](https://github.com/jline/jline3/jline-reader)[59] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
| org.jline | [jline-style](https://github.com/jline/jline3/jline-style)[60] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
| org.jline | [jline-terminal](https://github.com/jline/jline3/jline-terminal)[61] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
| org.jline | [jline-terminal-jni](https://github.com/jline/jline3/jline-terminal-jni)[62] | 3.30.6 | jar | [The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |

# Project Dependency Graph

## [Dependency Tree](#dependency-tree)

- network.ike.tooling:ike-workspace-extension:jar:15 ** 
  
  | IKE Workspace Extension |
  | --- |
  | **Description: **Maven 4 build extension that prunes non-existent <subprojects> entries from workspace POMs before model validation. Lets a fresh clone of an IKE workspace bootstrap with mvn ws:scaffold-init before any subproject directory is on disk. **URL: **[https://ike.network/ike-workspace-extension/](https://ike.network/ike-workspace-extension/)[63] **Project Licenses: **[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
  
    - org.apache.maven:maven-api-spi:jar:4.0.0-rc-5 (compile) ** 
      
      | Maven 4 API :: SPI |
      | --- |
      | **Description: **Maven 4 API - Maven SPI. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-spi/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-spi/)[5] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
      
          - org.apache.maven:maven-api-annotations:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: Meta annotations |
            | --- |
            | **Description: **Maven 4 API - Java meta annotations. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-annotations/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-annotations/)[14] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
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
                    | **Description: **Open Test Alliance for the JVM **URL: **[https://github.com/ota4j-team/opentest4j](https://github.com/ota4j-team/opentest4j)[21] **Project Licenses: **[The Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
                  - org.junit.platform:junit-platform-commons:jar:6.0.0 (test) ** 
                    
                    | JUnit Platform Commons |
                    | --- |
                    | **Description: **Module "junit-platform-commons" of JUnit **URL: **[https://junit.org/](https://junit.org/)[6] **Project Licenses: **[Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
                  - org.apiguardian:apiguardian-api:jar:1.1.2 (test) ** 
                    
                    | org.apiguardian:apiguardian-api |
                    | --- |
                    | **Description: **@API Guardian **URL: **[https://github.com/apiguardian-team/apiguardian](https://github.com/apiguardian-team/apiguardian)[19] **Project Licenses: **[The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[11] |
                  - org.jspecify:jspecify:jar:1.0.0 (test) ** 
                    
                    | JSpecify annotations |
                    | --- |
                    | **Description: **An artifact of well-named and well-specified annotations to power static analysis checks **URL: **[http://jspecify.org/](http://jspecify.org/)[20] **Project Licenses: **[The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[11] |
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
            | **Description: **Maven 4 API - Immutable Settings model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-settings/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-settings/)[16] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-api-toolchain:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: Toolchain |
            | --- |
            | **Description: **Maven 4 API - Immutable Toolchain model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-toolchain/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-toolchain/)[17] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-api-plugin:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: Plugin |
            | --- |
            | **Description: **Maven 4 API - Immutable Plugin model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-plugin/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-plugin/)[15] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-api-xml:jar:4.0.0-rc-5 (compile) ** 
            
            | Maven 4 API :: XML |
            | --- |
            | **Description: **Maven 4 API - Immutable XML. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-xml/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-xml/)[18] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
    - org.apache.maven:maven-api-model:jar:4.0.0-rc-5 (compile) ** 
      
      | Maven 4 API :: Model |
      | --- |
      | **Description: **Maven 4 API - Immutable Model for Maven POM (Project Object Model). **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-model/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-model/)[4] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
    - org.apache.maven:maven-api-di:jar:4.0.0-rc-5 (compile) ** 
      
      | Maven 4 API :: Dependency Injection |
      | --- |
      | **Description: **Maven 4 API - Dependency Injection **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-di/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-di/)[3] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
    - org.apache.maven:maven-core:jar:4.0.0-rc-5 (provided) ** 
      
      | Maven 4 Core |
      | --- |
      | **Description: **Maven Core classes. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-core/](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-core/)[13] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
      
          - org.apache.maven:maven-api-metadata:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven 4 API :: Repository Metadata |
            | --- |
            | **Description: **Maven 4 API - Immutable Repository Metadata model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-metadata/](https://maven.apache.org/ref/4.0.0-rc-5/api/maven-api-metadata/)[24] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-di:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven 4 Dependency Injection |
            | --- |
            | **Description: **Provides the implementation for the Dependency Injection mechanism in Maven **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-di/](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-di/)[27] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-impl:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven 4 API Implementation |
            | --- |
            | **Description: **Provides the implementation classes for the Maven API **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-impl/](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-impl/)[28] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
            
                  - org.apache.maven:maven-support:jar:4.0.0-rc-5 (provided) ** 
                    
                    | Maven 4 Model Support |
                    | --- |
                    | **Description: **Provides the Maven 4 Model Support **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-support/](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-support/)[36] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
                  - com.fasterxml.woodstox:woodstox-core:jar:7.1.1 (provided) ** 
                    
                    | Woodstox |
                    | --- |
                    | **Description: **Woodstox is a high-performance XML processor that implements Stax (JSR-173), SAX2 and Stax2 APIs **URL: **[https://github.com/FasterXML/woodstox](https://github.com/FasterXML/woodstox)[22] **Project Licenses: **[The Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[11] |
                  - org.codehaus.woodstox:stax2-api:jar:4.2.2 (provided) ** 
                    
                    | Stax2 API |
                    | --- |
                    | **Description: **Stax2 API is an extension to basic Stax 1.0 API that adds significant new functionality, such as full-featured bi-direction validation interface and high-performance Typed Access API. **URL: **[http://github.com/FasterXML/stax2-api](http://github.com/FasterXML/stax2-api)[50] **Project Licenses: **[The BSD 2-Clause License](http://www.opensource.org/licenses/bsd-license.php)[51] |
                  - org.apache.maven.resolver:maven-resolver-named-locks:jar:2.0.13 (provided) ** 
                    
                    | Maven Artifact Resolver Named Locks |
                    | --- |
                    | **Description: **A synchronization utility implementation using Named locks. **URL: **[https://maven.apache.org/resolver/maven-resolver-named-locks/](https://maven.apache.org/resolver/maven-resolver-named-locks/)[42] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
                  - org.apache.maven.resolver:maven-resolver-connector-basic:jar:2.0.13 (provided) ** 
                    
                    | Maven Artifact Resolver Connector Basic |
                    | --- |
                    | **Description: **A repository connector implementation for repositories using URI-based layouts. **URL: **[https://maven.apache.org/resolver/maven-resolver-connector-basic/](https://maven.apache.org/resolver/maven-resolver-connector-basic/)[40] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
                  - org.codehaus.plexus:plexus-sec-dispatcher:jar:4.1.0 (provided) ** 
                    
                    | Plexus Security Dispatcher Component |
                    | --- |
                    | **Description: **This library provides encryption/decryption functionality with pluggable ciphers and password providers **URL: **[https://codehaus-plexus.github.io/plexus-pom/plexus-sec-dispatcher/](https://codehaus-plexus.github.io/plexus-pom/plexus-sec-dispatcher/)[47] **Project Licenses: **[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-jline:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven 4 JLine integration |
            | --- |
            | **Description: **Provides the JLine integration in Maven **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-jline/](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-jline/)[29] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
            
                  - org.jline:jline-reader:jar:3.30.6 (provided) ** 
                    
                    | JLine Reader |
                    | --- |
                    | **Description: **JLine **URL: **[https://github.com/jline/jline3/jline-reader](https://github.com/jline/jline3/jline-reader)[59] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
                  - org.jline:jline-style:jar:3.30.6 (provided) ** 
                    
                    | JLine Style |
                    | --- |
                    | **Description: **JLine **URL: **[https://github.com/jline/jline3/jline-style](https://github.com/jline/jline3/jline-style)[60] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
                  - org.jline:jline-builtins:jar:3.30.6 (provided) ** 
                    
                    | JLine Builtins |
                    | --- |
                    | **Description: **JLine **URL: **[https://github.com/jline/jline3/jline-builtins](https://github.com/jline/jline3/jline-builtins)[55] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
                  - org.jline:jline-console:jar:3.30.6 (provided) ** 
                    
                    | JLine Console |
                    | --- |
                    | **Description: **JLine **URL: **[https://github.com/jline/jline3/jline-console](https://github.com/jline/jline3/jline-console)[56] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
                  - org.jline:jline-console-ui:jar:3.30.6 (provided) ** 
                    
                    | JLine Console UI |
                    | --- |
                    | **Description: **JLine **URL: **[https://github.com/jline/jline3/jline-console-ui](https://github.com/jline/jline3/jline-console-ui)[57] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
                  - org.jline:jline-terminal:jar:3.30.6 (provided) ** 
                    
                    | JLine Terminal |
                    | --- |
                    | **Description: **JLine **URL: **[https://github.com/jline/jline3/jline-terminal](https://github.com/jline/jline3/jline-terminal)[61] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
                    
                            - org.jline:jline-native:jar:3.30.6 (provided) ** 
                              
                              | JLine Native Library |
                              | --- |
                              | **Description: **JLine **URL: **[https://github.com/jline/jline3/jline-native](https://github.com/jline/jline3/jline-native)[58] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
                  - org.jline:jline-terminal-jni:jar:3.30.6 (provided) ** 
                    
                    | JLine JNI Terminal |
                    | --- |
                    | **Description: **JLine **URL: **[https://github.com/jline/jline3/jline-terminal-jni](https://github.com/jline/jline3/jline-terminal-jni)[62] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
                  - org.jline:jansi-core:jar:3.30.6 (provided) ** 
                    
                    | Jansi Core |
                    | --- |
                    | **Description: **JLine **URL: **[https://github.com/jline/jline3/jansi-core](https://github.com/jline/jline3/jansi-core)[53] **Project Licenses: **[The BSD License](https://opensource.org/licenses/BSD-3-Clause)[54] |
          - org.apache.maven:maven-logging:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven 4 Logging |
            | --- |
            | **Description: **Provides the Maven Logging infrastructure **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-logging/](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-logging/)[30] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-xml:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven 4 XML Implementation |
            | --- |
            | **Description: **Provides the implementation classes for the Maven XML **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-xml/](https://maven.apache.org/ref/4.0.0-rc-5/maven-impl-modules/maven-xml/)[38] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-artifact:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven Artifact |
            | --- |
            | **Description: **Maven is a software build management and comprehension tool. Based on the concept of a project object model: builds, dependency management, documentation creation, site publication, and distribution publication are all controlled from the declarative file. Maven can be extended by plugins to utilise a number of other development tools for reporting or the build process. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-artifact/](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-artifact/)[25] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-model:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven Model |
            | --- |
            | **Description: **Model for Maven POM (Project Object Model) **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-model/](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-model/)[31] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-model-builder:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven Model Builder (deprecated) |
            | --- |
            | **Description: **The effective model builder, with inheritance, profile activation, interpolation, ... **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-model-builder/](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-model-builder/)[32] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
            
                  - org.apache.maven:maven-builder-support:jar:4.0.0-rc-5 (provided) ** 
                    
                    | Maven Builder Support (deprecated) |
                    | --- |
                    | **Description: **Support for descriptor builders (model, setting, toolchains) **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-builder-support/](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-builder-support/)[26] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
                  - org.codehaus.plexus:plexus-interpolation:jar:1.28 (provided) ** 
                    
                    | Plexus Interpolation API |
                    | --- |
                    | **Description: **The Plexus project provides a full software stack for creating and executing software projects. **URL: **[https://codehaus-plexus.github.io/plexus-pom/plexus-interpolation/](https://codehaus-plexus.github.io/plexus-pom/plexus-interpolation/)[46] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
                  - org.codehaus.plexus:plexus-utils:jar:4.0.2 (provided) ** 
                    
                    | Plexus Common Utilities |
                    | --- |
                    | **Description: **A collection of various utility classes to ease working with strings, files, command lines and more. **URL: **[https://codehaus-plexus.github.io/plexus-utils/](https://codehaus-plexus.github.io/plexus-utils/)[48] **Project Licenses: **[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-plugin-api:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven 3 Plugin API |
            | --- |
            | **Description: **The API for Maven 3 plugins - Mojos - development. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-plugin-api/](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-plugin-api/)[33] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
            
                  - org.eclipse.sisu:org.eclipse.sisu.plexus:jar:0.9.0.M4 (provided) ** 
                    
                    | org.eclipse.sisu:org.eclipse.sisu.plexus |
                    | --- |
                    | **Description: **Plexus-JSR330 adapter; adds Plexus support to the Sisu-Inject container **URL: **[https://eclipse.dev/sisu/org.eclipse.sisu.plexus/](https://eclipse.dev/sisu/org.eclipse.sisu.plexus/)[52] **Project Licenses: **[Eclipse Public License, Version 2.0](https://www.eclipse.org/legal/epl-v20.html)[7] |
          - org.apache.maven:maven-repository-metadata:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven Repository Metadata Model |
            | --- |
            | **Description: **Per-directory local and remote repository metadata. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-repository-metadata/](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-repository-metadata/)[34] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-settings:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven Settings |
            | --- |
            | **Description: **Maven Settings model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-settings/](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-settings/)[35] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven:maven-toolchain-model:jar:4.0.0-rc-5 (provided) ** 
            
            | Maven Toolchain Model |
            | --- |
            | **Description: **Maven Toolchain model. **URL: **[https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-toolchain-model/](https://maven.apache.org/ref/4.0.0-rc-5/maven-compat-modules/maven-toolchain-model/)[37] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven.resolver:maven-resolver-api:jar:2.0.13 (provided) ** 
            
            | Maven Artifact Resolver API |
            | --- |
            | **Description: **The application programming interface for the repository system. **URL: **[https://maven.apache.org/resolver/maven-resolver-api/](https://maven.apache.org/resolver/maven-resolver-api/)[39] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven.resolver:maven-resolver-spi:jar:2.0.13 (provided) ** 
            
            | Maven Artifact Resolver SPI |
            | --- |
            | **Description: **The service provider interface for repository system implementations and repository connectors. **URL: **[https://maven.apache.org/resolver/maven-resolver-spi/](https://maven.apache.org/resolver/maven-resolver-spi/)[43] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
            
                  - com.google.code.gson:gson:jar:2.13.2 (provided) ** 
                    
                    | Gson |
                    | --- |
                    | **Description: **Gson JSON library **URL: **[https://github.com/google/gson](https://github.com/google/gson)[23] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven.resolver:maven-resolver-util:jar:2.0.13 (provided) ** 
            
            | Maven Artifact Resolver Utilities |
            | --- |
            | **Description: **A collection of utility classes to ease usage of the repository system. **URL: **[https://maven.apache.org/resolver/maven-resolver-util/](https://maven.apache.org/resolver/maven-resolver-util/)[44] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.apache.maven.resolver:maven-resolver-impl:jar:2.0.13 (provided) ** 
            
            | Maven Artifact Resolver Implementation |
            | --- |
            | **Description: **An implementation of the repository system. **URL: **[https://maven.apache.org/resolver/maven-resolver-impl/](https://maven.apache.org/resolver/maven-resolver-impl/)[41] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.codehaus.plexus:plexus-classworlds:jar:2.9.0 (provided) ** 
            
            | Plexus Classworlds |
            | --- |
            | **Description: **A class loader framework **URL: **[https://codehaus-plexus.github.io/plexus-classworlds/](https://codehaus-plexus.github.io/plexus-classworlds/)[45] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
          - org.codehaus.plexus:plexus-xml:jar:4.1.0 (provided) ** 
            
            | Plexus XML Utilities |
            | --- |
            | **Description: **A collection of various utility classes to ease working with XML. **URL: **[https://codehaus-plexus.github.io/plexus-xml/](https://codehaus-plexus.github.io/plexus-xml/)[49] **Project Licenses: **[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |
    - javax.inject:javax.inject:jar:1 (provided) ** 
      
      | javax.inject |
      | --- |
      | **Description: **The javax.inject API **URL: **[http://code.google.com/p/atinject/](http://code.google.com/p/atinject/)[10] **Project Licenses: **[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)[11] |
    - org.slf4j:slf4j-api:jar:2.0.17 (provided) ** 
      
      | SLF4J API Module |
      | --- |
      | **Description: **The slf4j API **URL: **[http://www.slf4j.org](http://www.slf4j.org)[8] **Project Licenses: **[MIT](https://opensource.org/license/mit)[9] |
    - org.slf4j:slf4j-simple:jar:2.0.17 (test) ** 
      
      | SLF4J Simple Provider |
      | --- |
      | **Description: **SLF4J Simple Provider **URL: **[http://www.slf4j.org](http://www.slf4j.org)[8] **Project Licenses: **[MIT](https://opensource.org/license/mit)[9] |
    - network.ike:ike-base-parent:zip:site-theme:15 (provided) ** 
      
      | IKE Base Parent |
      | --- |
      | **Description: **Tier 0 foundation parent for the IKE Network — the apex of the parent inheritance forest, inherited by ike-tooling, ike-docs, and ike-platform. Carries shared publishing metadata, GPG signing, and Maven Central publishing configuration. **URL: **[https://ike.network/ike-base-parent/](https://ike.network/ike-base-parent/)[12] **Project Licenses: **[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)[2] |

# Licenses

**Eclipse Public License, Version 2.0: **org.eclipse.sisu:org.eclipse.sisu.plexus

**The Apache License, Version 2.0: **JSpecify annotations, Woodstox, org.apiguardian:apiguardian-api, org.opentest4j:opentest4j

**The BSD License: **JLine Builtins, JLine Console, JLine Console UI, JLine JNI Terminal, JLine Native Library, JLine Reader, JLine Style, JLine Terminal, Jansi Core

**The BSD 2-Clause License: **Stax2 API

**Apache License, Version 2.0: **IKE Base Parent, IKE Workspace Extension, Plexus Common Utilities, Plexus Security Dispatcher Component

**Apache-2.0: **Gson, Maven 3 Plugin API, Maven 4 API :: Core, Maven 4 API :: Dependency Injection, Maven 4 API :: Meta annotations, Maven 4 API :: Model, Maven 4 API :: Plugin, Maven 4 API :: Repository Metadata, Maven 4 API :: SPI, Maven 4 API :: Settings, Maven 4 API :: Toolchain, Maven 4 API :: XML, Maven 4 API Implementation, Maven 4 Core, Maven 4 Dependency Injection, Maven 4 JLine integration, Maven 4 Logging, Maven 4 Model Support, Maven 4 XML Implementation, Maven Artifact, Maven Artifact Resolver API, Maven Artifact Resolver Connector Basic, Maven Artifact Resolver Implementation, Maven Artifact Resolver Named Locks, Maven Artifact Resolver SPI, Maven Artifact Resolver Utilities, Maven Builder Support (deprecated), Maven Model, Maven Model Builder (deprecated), Maven Repository Metadata Model, Maven Settings, Maven Toolchain Model, Plexus Classworlds, Plexus Interpolation API, Plexus XML Utilities

**Eclipse Public License v2.0: **JUnit Jupiter (Aggregator), JUnit Jupiter API, JUnit Jupiter Engine, JUnit Jupiter Params, JUnit Platform Commons, JUnit Platform Engine API

**MIT: **SLF4J API Module, SLF4J Simple Provider

**The Apache Software License, Version 2.0: **javax.inject

# Dependency File Details

| Total | Size | Entries | Classes | Packages | Java Version | Debug Information |
| --- | --- | --- | --- | --- | --- | --- |
| woodstox-core-7.1.1.jar | 1.6 MB | 1091 | 942 | 78 | 1.8 | Yes |
| gson-2.13.2.jar | 289.9 kB | 226 | - | - | - | - |
|    • Root | - | 224 | 203 | 9 | 1.8 | Yes |
|    • Versioned | - | 2 | 1 | 1 | 9 | No |
| javax.inject-1.jar | 2.5 kB | 8 | 6 | 1 | 1.5 | No |
| ike-base-parent-15-site-theme.zip | 3.4 kB | - | - | - | - | - |
| maven-api-annotations-4.0.0-rc-5.jar | 13.1 kB | 27 | 12 | 1 | 17 | Yes |
| maven-api-core-4.0.0-rc-5.jar | 218.3 kB | 257 | 237 | 7 | 17 | Yes |
| maven-api-di-4.0.0-rc-5.jar | 16.4 kB | 32 | 13 | 2 | 17 | Yes |
| maven-api-metadata-4.0.0-rc-5.jar | 41.8 kB | 45 | 30 | 1 | 17 | Yes |
| maven-api-model-4.0.0-rc-5.jar | 222.2 kB | 128 | 113 | 1 | 17 | Yes |
| maven-api-plugin-4.0.0-rc-5.jar | 82.1 kB | 77 | 60 | 2 | 17 | Yes |
| maven-api-settings-4.0.0-rc-5.jar | 84.7 kB | 67 | 52 | 1 | 17 | Yes |
| maven-api-spi-4.0.0-rc-5.jar | 15.2 kB | 30 | 14 | 1 | 17 | Yes |
| maven-api-toolchain-4.0.0-rc-5.jar | 41.5 kB | 45 | 30 | 1 | 17 | Yes |
| maven-api-xml-4.0.0-rc-5.jar | 36.5 kB | 42 | 27 | 1 | 17 | Yes |
| maven-artifact-4.0.0-rc-5.jar | 62.7 kB | 59 | 34 | 11 | 17 | Yes |
| maven-builder-support-4.0.0-rc-5.jar | 16 kB | 24 | 10 | 1 | 17 | Yes |
| maven-core-4.0.0-rc-5.jar | 873.9 kB | 555 | 474 | 54 | 17 | Yes |
| maven-di-4.0.0-rc-5.jar | 63.7 kB | 44 | 29 | 2 | 17 | Yes |
| maven-impl-4.0.0-rc-5.jar | 563.2 kB | 297 | 259 | 16 | 17 | Yes |
| maven-jline-4.0.0-rc-5.jar | 19.6 kB | 21 | 6 | 1 | 17 | Yes |
| maven-logging-4.0.0-rc-5.jar | 28.9 kB | 30 | 12 | 2 | 17 | Yes |
| maven-model-4.0.0-rc-5.jar | 217.1 kB | 105 | 88 | 3 | 17 | Yes |
| maven-model-builder-4.0.0-rc-5.jar | 248.2 kB | 188 | 149 | 19 | 17 | Yes |
| maven-plugin-api-4.0.0-rc-5.jar | 46.7 kB | 44 | 25 | 5 | 17 | Yes |
| maven-repository-metadata-4.0.0-rc-5.jar | 44.1 kB | 38 | 20 | 2 | 17 | Yes |
| maven-settings-4.0.0-rc-5.jar | 125.2 kB | 58 | 41 | 3 | 17 | Yes |
| maven-support-4.0.0-rc-5.jar | 299.4 kB | 81 | 55 | 6 | 17 | Yes |
| maven-toolchain-model-4.0.0-rc-5.jar | 36.3 kB | 35 | 18 | 2 | 17 | Yes |
| maven-xml-4.0.0-rc-5.jar | 51.8 kB | 47 | 30 | 1 | 17 | Yes |
| maven-resolver-api-2.0.13.jar | 175.3 kB | 176 | 152 | 13 | 1.8 | Yes |
| maven-resolver-connector-basic-2.0.13.jar | 41.2 kB | 32 | 15 | 1 | 1.8 | Yes |
| maven-resolver-impl-2.0.13.jar | 394.5 kB | 250 | 215 | 18 | 1.8 | Yes |
| maven-resolver-named-locks-2.0.13.jar | 44.1 kB | 42 | 24 | 3 | 1.8 | Yes |
| maven-resolver-spi-2.0.13.jar | 78.5 kB | 116 | 85 | 18 | 1.8 | Yes |
| maven-resolver-util-2.0.13.jar | 265.8 kB | 196 | 169 | 14 | 1.8 | Yes |
| apiguardian-api-1.1.2.jar | 6.8 kB | 9 | 3 | 2 | 1.6 | Yes |
| plexus-classworlds-2.9.0.jar | 54.1 kB | 51 | 36 | 5 | 1.8 | Yes |
| plexus-interpolation-1.28.jar | 87.1 kB | 80 | 63 | 7 | 1.8 | Yes |
| plexus-sec-dispatcher-4.1.0.jar | 78 kB | 59 | 38 | 7 | 17 | Yes |
| plexus-utils-4.0.2.jar | 192.5 kB | 128 | - | - | - | - |
|    • Root | - | 110 | 86 | 7 | 1.8 | Yes |
|    • Versioned | - | 6 | 1 | 1 | 9 | Yes |
|    • Versioned | - | 6 | 1 | 1 | 10 | Yes |
|    • Versioned | - | 6 | 1 | 1 | 11 | Yes |
| plexus-xml-4.1.0.jar | 90.7 kB | 43 | 24 | 2 | 17 | Yes |
| stax2-api-4.2.2.jar | 195.9 kB | 146 | 125 | 12 | 1.6 | Yes |
| org.eclipse.sisu.plexus-0.9.0.M4.jar | 215.8 kB | 204 | 167 | 20 | 1.8 | Yes |
| jansi-core-3.30.6.jar | 47 kB | 43 | 25 | 2 | 1.8 | Yes |
| jline-builtins-3.30.6.jar | 329.2 kB | 106 | 87 | 1 | 1.8 | Yes |
| jline-console-3.30.6.jar | 164.6 kB | 69 | 57 | 3 | 1.8 | Yes |
| jline-console-ui-3.30.6.jar | 72.4 kB | 71 | 54 | 5 | 1.8 | Yes |
| jline-native-3.30.6.jar | 190.2 kB | 70 | 18 | 1 | 1.8 | Yes |
| jline-reader-3.30.6.jar | 187.1 kB | 89 | 75 | 5 | 1.8 | Yes |
| jline-style-3.30.6.jar | 28.4 kB | 29 | 19 | 1 | 1.8 | Yes |
| jline-terminal-3.30.6.jar | 275.5 kB | 157 | 114 | 5 | 1.8 | Yes |
| jline-terminal-jni-3.30.6.jar | 50.1 kB | 44 | 15 | 6 | 1.8 | Yes |
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
| slf4j-api-2.0.17.jar | 69.9 kB | 71 | - | - | - | - |
|    • Root | - | 69 | 55 | 4 | 1.8 | Yes |
|    • Versioned | - | 2 | 1 | 1 | 9 | No |
| slf4j-simple-2.0.17.jar | 15.7 kB | 22 | - | - | - | - |
|    • Root | - | 20 | 6 | 1 | 1.8 | Yes |
|    • Versioned | - | 2 | 1 | 1 | 9 | No |
| 62 | 10.1 MB | 6961 | 5565 | 447 | 17 | 58 |
| compile: 9 | compile: 730 kB | compile: 705 | compile: 558 | compile: 17 | 17 | compile: 9 |
| provided: 43 | provided: 8 MB | provided: 5268 | provided: 4149 | provided: 377 | provided: 41 |
| test: 10 | test: 1.4 MB | test: 988 | test: 858 | test: 53 | 17 | test: 8 |
