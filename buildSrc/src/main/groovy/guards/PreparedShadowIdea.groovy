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


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class PreparedShadowIdea implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.with {
            repositories {
                flatDir {
                    dirs project.tasks[PreparedShadow.JAR_TASK].destinationDir
                }
            }
            idea.module {
                def jarTask = tasks[PreparedShadow.JAR_TASK]
                String artifactName = jarTask.archiveName as String
                String extension = jarTask.extension as String
                if ( artifactName.endsWith(extension) ) {
                    artifactName = artifactName.substring(0, artifactName.length() - extension.length() - 1)
                }
                scopes.COMPILE.plus += [
                            configurations.detachedConfiguration(
                                    dependencies.create(name: artifactName))
                ]
            }
        }
    }
}
