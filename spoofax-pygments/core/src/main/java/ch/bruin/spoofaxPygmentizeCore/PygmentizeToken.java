package ch.bruin.spoofaxPygmentizeCore;

import org.spoofax.jsglr.client.imploder.IToken;

import static org.spoofax.jsglr.client.imploder.IToken.*;

public class PygmentizeToken {
    private int index;
    private String token;
    private String value;

    public PygmentizeToken(int index, String token, String value) {
        this.index = index;
        this.token = token;
        this.value = value;
    }

    public PygmentizeToken(IToken token) {
        this(token.getStartOffset(), tokenKind(token.getKind()), token.toString());
    }

    private static String tokenKind(int kind) {
        // from pygments.token import Comment, Error, Keyword, Literal, Name, Number, Operator, Other, Punctuation, String, Token
        switch (kind) {
            default:
            case TK_UNKNOWN:
            case TK_RESERVED:
            case TK_NO_TOKEN_KIND:
                return "Token.Text";
            case TK_IDENTIFIER:
                return "Token.Name";
            case TK_NUMBER:
                return "Token.Number";
            case TK_STRING:
                return "Token.String";
            case TK_KEYWORD:
                return "Token.Keyword";
            case TK_OPERATOR:
                return "Token.Operator";
            case TK_VAR:
                return "Token.Name.Variable";
            case TK_LAYOUT:
                return "Token.Comment";
            case TK_EOF:
                return "Token.Generic";
            case TK_ERROR:
            case TK_ERROR_KEYWORD:
            case TK_ERROR_LAYOUT:
            case TK_ERROR_EOF_UNEXPECTED:
                return "Token.Error";
            case TK_ESCAPE_OPERATOR:
                return "Operator.Word";
        }
    }

    @Override
    public String toString() {
        return "(" + index + ", " + token + ", '''" + value.replaceAll("\\\\", "\\\\\\\\") + "''')";
    }
}
