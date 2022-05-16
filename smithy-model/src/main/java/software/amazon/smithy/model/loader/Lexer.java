package software.amazon.smithy.model.loader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.BiPredicate;
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

    public boolean isPeek(String exactIdentifier) {
        Token token = peek();
        return peek().getType() == Token.Type.IDENTIFIER && getLexeme(token).equals(exactIdentifier);
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

    /**
     * Expects that the next token is one of the given types, and returns the
     * consumed token.
     *
     * @param types Types to expect.
     * @return Returns the parsed type.
     * @throws IllegalStateException if the token is unexpected.
     */
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

    /**
     * Expects a token of a specific type, and if not found, injects a synthetic
     * token of the given type, adding a syntax error.
     *
     * @param type Type of token to expect or inject.
     * @return Returns the expected or injected token.
     */
    public Token expectOrInject(Token.Type type) {
        if (peek().getType() == type) {
            return next();
        }

        Token badPeek = peek();
        addSyntaxError(badPeek, "Expected " + type + ", but found " + badPeek.getType());
        Token result = new Token(type, badPeek.getPosition(), 1, badPeek.getLine(), badPeek.getColumn());
        peeked = result;
        next();
        peeked = badPeek;
        return result;
    }

    /**
     * Expects a specific type of token, and then if that token is not found,
     * continues to skip tokens while {@code skipWhile} returns true for the
     * next peeked token.
     *
     * <p>This method performs a kind of error recovery that is used to skip
     * over statements until it recognizes that a new statement is defined.
     * If tokens are skipped, a syntax error is added.
     *
     * @param skipWhile Predicate that accepts the next consumed token and the peeked token after it.
     *                  This method returns true to skip the consumed token.
     * @param types Types of token to expect.
     * @return Returns the next consumed token to continue parsing from.
     * @see #expectOrSkipStatement(Token.Type...)
     */
    public Token expectOrSkip(BiPredicate<Token, Token> skipWhile, Token.Type... types) {
        try {
            return expect(types);
        } catch (IllegalStateException e) {
            Token result;
            do {
                result = next();
            } while (skipWhile.test(result, peek()));
            return result;
        }
    }

    /**
     * Expects the next token to be one of the given types, and if not, skips
     * over all tokens until a "}" is encountered or a newline is encountered
     * and the column of the following token is at 1, indicating a new
     * statement.
     *
     * <p>For example, this method would be useful to skip over half-defined
     * shape definition that don't yet provide a name. (e.g., "string ").
     * You could skip over tokens until you see a newline and are back to
     * column 0.
     *
     * @param types Types of token to expect.
     * @return Returns the next consumed token to continue parsing from.
     */
    public Token expectOrSkipStatement(Token.Type... types) {
        return expectOrSkip((token1, token2) -> {
            return token1.getType() == Token.Type.RBRACE
                   || (token1.getType() == Token.Type.BR && token2.getColumn() == 1);
        }, types);
    }

    /**
     * Get the string contents for a token from the source IDL.
     *
     * @param token Token to get the contents of.
     * @return Returns the string lexeme.
     */
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

    /**
     * Get the SourceLocation of the current lexer position.
     *
     * @return Returns the source location of the current position.
     */
    public SourceLocation getLocation() {
        Token peek = peek();
        return new SourceLocation(filename, peek.getLine(), peek.getColumn());
    }
}
