package syntax.antlr;

import syntax.antlr.ecmascript.ECMAScriptLexer;
import syntax.antlr.java.JavaLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * Created by avyatkin on 01/04/16.
 */
public class LexerWrapper {
    public String getTokenType(Token t) {
        return lexer.getVocabulary().getDisplayName(t.getType());
    }

    Lexer lexer;
    List<? extends Token> tokens;

    private LexerWrapper(Lexer l) {
        lexer = l;
        tokens = l.getAllTokens();
    }

    public List<? extends Token> tokens() {
        return tokens;
    }

    public static LexerWrapper javaLexer(String input) {
        CharStream cs = new ANTLRInputStream(input);
        Lexer lexer = new JavaLexer(cs);
        return new LexerWrapper(lexer);
    }

    public static LexerWrapper jsLexer(String input) {
        CharStream cs = new ANTLRInputStream(input);
        Lexer lexer = new ECMAScriptLexer(cs);
        return new LexerWrapper(lexer);
    }
}
