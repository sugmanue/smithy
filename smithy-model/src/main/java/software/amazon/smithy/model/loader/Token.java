package software.amazon.smithy.model.loader;

public final class Token {

    public static final Token EOF = new Token(Token.Type.EOF, 0, 0, 1, 1);

    public enum Type {
        SPACE,
        BR,
        COMMA,
        COMMENT,
        DOC_COMMENT,
        AT,
        STRING,
        TEXT_BLOCK,
        COLON,
        IDENTIFIER,
        DOT,
        POUND,
        DOLLAR,
        NUMBER,
        LBRACE,
        RBRACE,
        LBRACKET,
        RBRACKET,
        LPAREN,
        RPAREN,
        EQUAL,
        EOF,
        ERROR;

        public boolean isWhitespace() {
            switch (this) {
                case BR:
                case SPACE:
                case COMMA:
                case COMMENT:
                case DOC_COMMENT:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isStringValue() {
            switch (this) {
                case STRING:
                case TEXT_BLOCK:
                    return true;
                default:
                    return false;
            }
        }
    }

    private final Token.Type type;
    private final int position;
    private final int span;
    private final int line;
    private final int column;
    private boolean synthetic;

    public Token(Token.Type type, int position, int span, int line, int column) {
        this.type = type;
        this.position = position;
        this.span = span;
        this.line = line;
        this.column = column;
    }

    public Token.Type getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public int getSpan() {
        return span;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public CharSequence getLexeme(CharSequence source) {
        return source.subSequence(position, position + span);
    }

    @Override
    public String toString() {
        return "Token{"
               + "type='" + type + "',"
               + "line=" + getLine() + ", "
               + "column=" + getColumn() + ", "
               + "span=" + getSpan() + '}';
    }
}
