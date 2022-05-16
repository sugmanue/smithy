package software.amazon.smithy.model.loader;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;

interface StatementParser {

    Statement parse(Lexer lexer);

    final class ControlParser implements StatementParser {
        @Override
        public Statement parse(Lexer lexer) {
            if (lexer.peek().getType() != Token.Type.DOLLAR) {
                return null;
            }

            return lexer.parse("control", () -> {
                SourceLocation location = lexer.getLocation();
                lexer.expect(Token.Type.DOLLAR);
                lexer.skipWs();
                String name = lexer.getLexeme(lexer.expect(Token.Type.IDENTIFIER, Token.Type.STRING));
                lexer.skipWs();
                lexer.expectOrInject(Token.Type.COLON);
                lexer.skipWs();
                Node value = null; // TODO parse node
                lexer.skipSpaces();
                lexer.expectOrInject(Token.Type.BR);
                return new Statement.Control(location, name, value);
            });
        }
    }

    final class MetadataParser implements StatementParser {
        @Override
        public Statement parse(Lexer lexer) {
            if (!lexer.isPeek("metadata")) {
                return null;
            }

            return lexer.parse("metadata", () -> {
                SourceLocation location = lexer.getLocation();
                lexer.next();
                lexer.skipWs();
                String name = lexer.getLexeme(lexer.expect(Token.Type.IDENTIFIER, Token.Type.STRING));
                lexer.skipWs();
                lexer.expectOrInject(Token.Type.EQUAL);
                lexer.skipWs();
                Node value = null; // TODO Parse node
                lexer.skipSpaces();
                lexer.expectOrInject(Token.Type.BR);
                return new Statement.Metadata(location, name, value);
            });
        }
    }

    final class NamespaceParser implements StatementParser {
        @Override
        public Statement parse(Lexer lexer) {
            if (!lexer.isPeek("namespace")) {
                return null;
            }

            return lexer.parse("namespace", () -> {
                SourceLocation location = lexer.getLocation();
                lexer.next();
                lexer.skipWs();
                String namespace = lexer.getLexeme(lexer.expect(Token.Type.IDENTIFIER, Token.Type.STRING));
                lexer.skipSpaces();
                lexer.expectOrInject(Token.Type.BR);
                return new Statement.Namespace(location, namespace);
            });
        }
    }
}
