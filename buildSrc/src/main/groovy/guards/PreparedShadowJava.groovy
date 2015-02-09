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
class PreparedShadowJava implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.with {
            compileJava.dependsOn PreparedShadow.JAR_TASK
            compileTestJava.dependsOn PreparedShadow.JAR_TASK

            tasks.jar {
                dependsOn PreparedShadow.JAR_TASK
                from zipTree(tasks[PreparedShadow.JAR_TASK].archivePath)
            }
            tasks.sourcesJar {
                dependsOn PreparedShadow.SOURCE_JAR_TASK
                from zipTree(tasks[PreparedShadow.SOURCE_JAR_TASK].archivePath)
            }
        }
    }
}
