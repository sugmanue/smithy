package software.amazon.smithy.model.loader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.ExpectationNotMetException;

final class Lexer {

    private final String filename;
    private final String idl;
    private final Tokenizer tokens;
    private Token peeked;
    private final Deque<String> states = new ArrayDeque<>();
    private final List<SyntaxError> syntaxErrors = new ArrayList<>();

    Lexer(String filename, String idl) {
        this.filename = filename;
        this.idl = idl;
        this.tokens = new Tokenizer(idl);
    }

    public Statement parse(String name, Supplier<Statement> parser) {
        Statement result = null;
        states.push(name);
        try {
            result = parser.get();
        } catch (IllegalStateException | ExpectationNotMetException e) {
            // TODO ?
        }
        states.pop();
        return result;
    }

    public Token next() {
        Token result = peek();
        peeked = null;
        return result;
    }

    public Token peek() {
        if (peeked == null) {
            peeked = tokens.hasNext() ? tokens.next() : Token.EOF;
        }

        return peeked;
    }

    public Lexer skipWs() {
        while (peek().getType().isWhitespace()) {
            next();
        }
        return this;
    }

    public Lexer skipSpaces() {
        while (peek().getType() == Token.Type.SPACE) {
            next();
        }
        return this;
    }

    public boolean eof() {
        return !tokens.hasNext();
    }

    public Token expect(Token.Type... types) {
        for (Token.Type type : types) {
            if (peek().getType() == type) {
                return next();
            }
        }
        String message = "Expected " + Arrays.toString(types) + ", but found " + peek();
        addSyntaxError(peek(), message);
        throw new IllegalStateException("Expected " + Arrays.toString(types) + ", but found " + peek());
    }

    public Token expect(String exactIdentifier) {
        Token token = expect(Token.Type.IDENTIFIER);
        if (token.getLexeme(idl).equals(exactIdentifier)) {
            return token;
        }
        throw new IllegalStateException("Expected `" + exactIdentifier + "`, but found " + token);
    }

    public Token expectOrInject(Token.Type type) {
        return expectOrInject(type, (t, badPeek) -> {
            return new Token(t, badPeek.getPosition(), 1, badPeek.getLine(), badPeek.getColumn());
        });
    }

    public Token expectOrInject(Token.Type type, BiFunction<Token.Type, Token, Token> mapper) {
        if (peek().getType() == type) {
            return next();
        }

        Token badPeek = peek();
        addSyntaxError(badPeek, "Expected " + type + ", but found " + badPeek.getType());
        Token result = mapper.apply(type, badPeek);
        peeked = result;
        next();
        peeked = badPeek;
        return result;
    }

    public String getLexeme(Token token) {
        return idl.substring(token.getPosition(), token.getPosition() + token.getSpan());
    }

    public List<SyntaxError> getSyntaxErrors() {
        return syntaxErrors;
    }

    public Lexer addSyntaxError(SyntaxError error) {
        syntaxErrors.add(error);
        return this;
    }

    public Lexer addSyntaxError(Token token, String message) {
        return addSyntaxError(new SyntaxError(token, message, String.join(" < ", states)));
    }

    public SourceLocation getLocation() {
        Token peek = peek();
        return new SourceLocation(filename, peek.getLine(), peek.getColumn());
    }
}
