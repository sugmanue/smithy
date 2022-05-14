package software.amazon.smithy.model.loader;

import java.util.Iterator;
import java.util.NoSuchElementException;
import software.amazon.smithy.utils.SimpleParser;

// TODO: This class would be public and provide tokens. Lexer would not.
public class Tokenizer implements Iterator<Token> {

    private final SimpleParser parser;
    private int offsetStart = 0;
    private int lineStart = 1;
    private int columnStart = 1;

    public Tokenizer(String idl) {
        this(new SimpleParser(idl));
    }

    private Tokenizer(SimpleParser parser) {
        this.parser = parser;
    }

    @Override
    public boolean hasNext() {
        return parser.peek() != SimpleParser.EOF;
    }

    @Override
    public Token next() {
        offsetStart = parser.position();
        lineStart = parser.line();
        columnStart = parser.column();

        int c = parser.peek();

        if (c == SimpleParser.EOF) {
            throw new NoSuchElementException("Token stream reached EOF");
        }

        switch (c) {
            case ' ':
            case '\t':
                return tokenizeSpace();
            case '\r':
            case '\n':
                return tokenizeBr();
            case ',':
                return singleCharToken(Token.Type.COMMA);
            case '@':
                return singleCharToken(Token.Type.AT);
            case '$':
                return singleCharToken(Token.Type.DOLLAR);
            case '.':
                return singleCharToken(Token.Type.DOT);
            case ':':
                return singleCharToken(Token.Type.COLON);
            case '{':
                return singleCharToken(Token.Type.LBRACE);
            case '}':
                return singleCharToken(Token.Type.RBRACE);
            case '[':
                return singleCharToken(Token.Type.LBRACKET);
            case ']':
                return singleCharToken(Token.Type.RBRACKET);
            case '(':
                return singleCharToken(Token.Type.LPAREN);
            case ')':
                return singleCharToken(Token.Type.RPAREN);
            case '#':
                return singleCharToken(Token.Type.POUND);
            case '=':
                return singleCharToken(Token.Type.EQUAL);
            case '"':
                return parseString();
            case '/':
                return parseComment();
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return parseNumber((char) c);
            default:
                if (ParserUtils.isIdentifierStart((char) c)) {
                    return parseIdentifier();
                } else {
                    return singleCharToken(Token.Type.ERROR);
                }
        }
    }

    public int getPosition() {
        return parser.position();
    }

    public int getLine() {
        return parser.line();
    }

    public int getColumn() {
        return parser.column();
    }

    private Token singleCharToken(Token.Type type) {
        parser.skip();
        return new Token(type, offsetStart, computeSpan(), lineStart, columnStart);
    }

    private Token tokenizeBr() {
        parser.consumeUntilNoLongerMatches(c -> c == '\n' || c == '\r');
        return new Token(Token.Type.BR, offsetStart, computeSpan(), lineStart, columnStart);
    }

    private Token tokenizeSpace() {
        parser.consumeUntilNoLongerMatches(c -> c == ' ' || c == '\t');
        return new Token(Token.Type.SPACE, offsetStart, computeSpan(), lineStart, columnStart);
    }

    private Token parseComment() {
        // first "/".
        parser.expect('/');
        // required, second "/".
        if (parser.peek() != '/') {
            return singleCharToken(Token.Type.ERROR);
        }
        parser.expect('/');

        Token.Type type = Token.Type.COMMENT;

        // Three "///" is a documentation comment.
        if (parser.peek() == '/') {
            parser.expect('/');
            type = Token.Type.DOC_COMMENT;
        }

        do {
            parser.skip();
        } while (!parser.eof() && parser.line() == lineStart);

        return new Token(type, offsetStart, computeSpan(), lineStart, columnStart);
    }

    int computeSpan() {
        return parser.position() - offsetStart;
    }

    // TODO: Fix how this parses numbers and handles errors.
    private Token parseNumber(char start) {
        // skip the first number or "-".
        parser.skip();
        // Consume the digits that follow.
        readDigits();

        // "-" must be followed by at least one digit.
        if (start == '-' && computeSpan() == 1) {
            return singleCharToken(Token.Type.ERROR);
        }

        if (parser.peek() == '.') {
            parser.expect('.');
            int startOfDecimal = computeSpan();
            readDigits();
            // Fractional needs at least one digit after.
            if (computeSpan() == startOfDecimal) {
                return singleCharToken(Token.Type.ERROR);
            }
        }

        if (parser.peek() == 'e') {
            parser.expect('e');
            if (parser.peek() == '+' || parser.peek() == '-') {
                parser.skip();
            }
            int startOfExponent = computeSpan();
            readDigits();
            // Exponent needs at least one digit after.
            if (computeSpan() == startOfExponent) {
                return singleCharToken(Token.Type.ERROR);
            }
        }

        return new Token(Token.Type.NUMBER, offsetStart, computeSpan(), lineStart, columnStart);
    }

    private void readDigits() {
        parser.consumeUntilNoLongerMatches(ParserUtils::isDigit);
    }

    private Token parseIdentifier() {
        parser.consumeUntilNoLongerMatches(ParserUtils::isValidIdentifierCharacter);
        return new Token(Token.Type.IDENTIFIER, offsetStart, computeSpan(), lineStart, columnStart);
    }

    private Token parseString() {
        return Token.EOF; // TODO parse strings ("" and """)
    }
}
