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

description = "This module is a library used to validate Smithy models, create filtered " +
        "projections of a model, and generate build artifacts."

extra["displayName"] = "Smithy :: Build"
extra["moduleName"] = "software.amazon.smithy.build"

dependencies {
    api(project(":smithy-utils"))
    api(project(":smithy-model"))

    // Allows testing of annotation processor
    testImplementation("com.google.testing.compile:compile-testing:0.21.0")
}
