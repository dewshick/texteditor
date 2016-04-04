package syntax.antlr;

import syntax.antlr.ecmascript.ECMAScriptLexer;
import syntax.antlr.java.JavaLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by avyatkin on 01/04/16.
 */
public class LexerWrapper {
    public String getTokenType(Token t) {
        return lexer.getVocabulary().getDisplayName(t.getType());
    }

    Lexer lexer;

    private LexerWrapper(Lexer l) {
        lexer = l;
        l.reset();
        List <? extends Token> tokens = l.getAllTokens();
        lexemes = new LinkedList<>();

        Iterator<? extends Token> tokenIterator = tokens.iterator();
        if (tokenIterator.hasNext()) {
            Token currentToken = tokenIterator.next();
            Token nextToken;
            while (tokenIterator.hasNext()) {
                nextToken = tokenIterator.next();
                lexemes.add(lexemeFromConsecutiveTokens(currentToken, nextToken));
                currentToken = nextToken;
            }
            lexemes.add(lexemeFromConsecutiveTokens(currentToken, null));
        }
    }

    private List<Lexeme> lexemes;

    public List<Lexeme> lexemes() { return lexemes; }

    private Lexeme lexemeFromConsecutiveTokens(Token current, Token next) {
        int offset = current.getStartIndex();
        int distanceToNextToken = next == null ? 0 : next.getStartIndex() - offset;
        String tokenType = getTokenType(current);
        return new Lexeme(offset, distanceToNextToken, current.getText().length(), tokenType);
    }

    public void addText(int offset, String text) { }

    public void removeText(int offset, int length) { }

//  Factory methods

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
