package software.amazon.smithy.model.loader;

public final class SyntaxError {
    private final Token token;
    private final String message;
    private final String context;

    public SyntaxError(Token token, String message, String context) {
        this.token = token;
        this.context = context;
        this.message = message;
    }

    public Token getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "SyntaxError{" +
               "token=" + token +
               ", message='" + message + '\'' +
               ", context='" + context + '\'' +
               '}';
    }
}
