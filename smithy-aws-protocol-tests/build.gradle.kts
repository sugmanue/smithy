/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

plugins {
    id("software.amazon.smithy.gradle.smithy-jar")
}

description = "Defines protocol tests for AWS HTTP protocols."

extra["displayName"] = "Smithy :: AWS :: Protocol Tests"
extra["moduleName"] = "software.amazon.smithy.aws.protocoltests"

dependencies {
    implementation(project(path = ":smithy-cli", configuration = "shadow"))
    implementation(project(":smithy-protocol-test-traits"))
    implementation(project(":smithy-protocol-traits"))
    implementation(project(":smithy-aws-traits"))
    api(project(":smithy-validation-model"))
}

tasks.sourcesJar {
    dependsOn("smithyJarStaging")
}

smithy {
    format.set(false)
    smithyBuildConfigs.set(project.files())
}
