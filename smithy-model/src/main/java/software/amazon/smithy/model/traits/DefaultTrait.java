/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.smithy.model.traits;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;

/**
 * Indicates that a shape is defaulted, meaning a zero value is used when one is not provided.
 */
public final class DefaultTrait implements Trait {

    public static final ShapeId ID = ShapeId.from("smithy.api#default");
    private final Node value;

    public DefaultTrait(Node value) {
        this.value = value;
    }

    @Override
    public ShapeId toShapeId() {
        return ID;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return value.getSourceLocation();
    }

    @Override
    public Node toNode() {
        return value;
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(ID);
        }

        @Override
        public Trait createTrait(ShapeId target, Node value) {
            return new DefaultTrait(value);
        }
    }
}
