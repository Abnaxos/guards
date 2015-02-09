/*
 * Copyright 2015 Raffael Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package guards

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Publishable implements Plugin<Project> {

    @Override
    void apply(Project target) {
        target.with {
            apply plugin:'maven'
            apply plugin:'signing'
//            apply plugin:'com.bmuschko.nexus'

//            sourcesJar {
//            }
//            afterEvaluate {
//                sourcesJar {
//                    if ( project.plugins.findPlugin(PreparedShadow) ) {
//                        dependsOn preparedShadowSourceJar
//                        from zipTree(preparedShadowSourceJar.archivePath)
//                    }
//                    configurations.included.allDependencies.each { dep ->
//                        dependsOn dep.dependencyProject.sourcesJar
//                        from zipTree(dep.dependencyProject.sourcesJar.archivePath)
//                    }
//                }
//            }

//            signing {
//                sign configurations.archives
//            }
//
//            publishing {
//                publications {
//                    maven(MavenPublication) {
//                        artifact jar
//                        artifact sourceJar
//
//                        pom.packaging = 'jar'
//
////                        pom.project {
////                            licenses {
////                                license {
////                                    name 'MIT License'
////                                }
////                            }
////                        }
//                    }
//                }
//            }

            uploadArchives {
                repositories.mavenDeployer {
                    beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }

                    repository(url: "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
                        authentication(userName: project.properties['ossrhUsername'],
                                       password: project.properties['ossrhPassword'])
                    }

                    snapshotRepository(url: "https://oss.sonatype.org/content/repositories/snapshots/") {
                        authentication(userName: project.properties['ossrhUsername'],
                                       password: project.properties['ossrhPassword'])
                    }
                }
            }

        }
    }
}
