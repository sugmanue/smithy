/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

description = "Provides a parse tree and formatter for Smithy models."

extra["displayName"] = "Smithy :: Syntax"
extra["moduleName"] = "software.amazon.smithy.syntax"

dependencies {
    api(project(":smithy-utils"))
    api(project(":smithy-model"))
    implementation("com.opencastsoftware:prettier4j:0.1.1")

    // This is needed to export these as dependencies since we aren't shading them.
    shadow(project(":smithy-model"))
    shadow(project(":smithy-utils"))
}

tasks {
    shadowJar {
        // Replace the normal JAR with the shaded JAR. We don't want to publish a JAR that isn't shaded.
        archiveClassifier.set("")

        mergeServiceFiles()

        // Shade and relocate prettier4j.
        relocate("com.opencastsoftware.prettier4j", "software.amazon.smithy.syntax.shaded.prettier4j")

        // Despite the "shadow" configuration under dependencies, we unfortunately need to also list here that
        // smithy-model and smithy-utils aren't shaded. These are normal dependencies that we want consumers to resolve.
        dependencies {
            exclude(project(":smithy-utils"))
            exclude(project(":smithy-model"))
        }
    }

    jar {
        finalizedBy(shadowJar)
    }
}