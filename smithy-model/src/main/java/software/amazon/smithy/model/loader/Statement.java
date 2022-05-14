package software.amazon.smithy.model.loader;

import java.util.Set;
import java.util.function.Function;
import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.AbstractShapeBuilder;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ShapeType;
import software.amazon.smithy.model.shapes.ToShapeId;
import software.amazon.smithy.model.traits.Trait;

interface Statement extends FromSourceLocation {

    void resolveForwardReferences(Set<ShapeId> ids, Function<ShapeId, ShapeType> typeProvider);

    interface Visitor<T> {
        default T control(Control statement) {
            return onDefault(statement);
        }

        default T metadata(Metadata statement) {
            return onDefault(statement);
        }

        default T namespace(Namespace statement) {
            return onDefault(statement);
        }

        default T applyTrait(ApplyTrait statement) {
            return onDefault(statement);
        }

        default T applyResolvedTrait(ApplyResolvedTrait statement) {
            return onDefault(statement);
        }

        default T defineShape(DefineShape statement) {
            return onDefault(statement);
        }

        T onDefault(Statement statement);
    }

    abstract class AbstractStatement implements Statement {
        private final SourceLocation sourceLocation;

        public AbstractStatement(FromSourceLocation sourceLocation) {
            this.sourceLocation = sourceLocation.getSourceLocation();
        }

        @Override
        public final SourceLocation getSourceLocation() {
            return sourceLocation;
        }

        @Override
        public void resolveForwardReferences(Set<ShapeId> ids, Function<ShapeId, ShapeType> typeProvider) {
            // does nothing by default
        }
    }

    final class Control extends AbstractStatement {
        private final String name;
        private final Node value;

        public Control(SourceLocation sourceLocation, String name, Node value) {
            super(sourceLocation);
            this.name = name;
            this.value = value;
        }

        public String name() {
            return name;
        }

        public Node value() {
            return value;
        }
    }

    final class Metadata extends AbstractStatement {
        private final String name;
        private final Node value;

        public Metadata(SourceLocation sourceLocation, String name, Node value) {
            super(sourceLocation);
            this.name = name;
            this.value = value;
        }

        public String name() {
            return name;
        }

        public Node value() {
            return value;
        }
    }

    class Namespace extends AbstractStatement {
        private final String message;

        public Namespace(SourceLocation sourceLocation, String message) {
            super(sourceLocation);
            this.message = message;
        }

        public String message() {
            return message;
        }
    }

    class ApplyTrait extends AbstractStatement implements ToShapeId {
        private final ShapeId shapeId;
        private final Node value;
        private final boolean isAnnotation;

        public ApplyTrait(SourceLocation sourceLocation, ShapeId shapeId, Node value, boolean isAnnotation) {
            super(sourceLocation);
            this.shapeId = shapeId;
            this.value = value;
            this.isAnnotation = isAnnotation;
        }

        @Override
        public ShapeId toShapeId() {
            return shapeId;
        }

        public Node value() {
            return value;
        }

        public boolean isAnnotation() {
            return isAnnotation;
        }
    }

    class ApplyResolvedTrait extends AbstractStatement implements ToShapeId {
        private final Trait trait;

        public ApplyResolvedTrait(Trait trait) {
            super(trait.getSourceLocation());
            this.trait = trait;
        }

        @Override
        public ShapeId toShapeId() {
            return trait.toShapeId();
        }

        public Trait trait() {
            return trait;
        }
    }

    class DefineShape extends AbstractStatement implements ToShapeId {
        private final AbstractShapeBuilder<?, ?> builder;

        public DefineShape(AbstractShapeBuilder<?, ?> builder) {
            super(builder.getSourceLocation());
            this.builder = builder;
        }

        @Override
        public ShapeId toShapeId() {
            return builder.getId();
        }

        public ShapeType shapeType() {
            return builder.getShapeType();
        }

        public AbstractShapeBuilder<?, ?> builder() {
            return builder;
        }
    }
}
