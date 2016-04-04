package syntax.antlr;

import syntax.antlr.ecmascript.ECMAScriptLexer;
import syntax.antlr.java.JavaLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import syntax.document.SupportedSyntax;

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
    String text;
    SupportedSyntax syntax;

    public LexerWrapper(SupportedSyntax syntax, String code) {
        init(syntax, code);
    }

    private void init(SupportedSyntax syntax, String code) {
        this.syntax = syntax;
        this.text = code;
        CharStream cs = new ANTLRInputStream(code);

        switch (syntax) {
            case ECMASCRIPT: lexer = new ECMAScriptLexer(cs); break;
            case JAVA: lexer = new ECMAScriptLexer(cs); break;
        }

        List <? extends Token> tokens = lexer.getAllTokens();
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
        return new Lexeme(offset, distanceToNextToken, current.getText().length(), tokenType, current.getText());
    }

    public void addText(int offset, String newText) {
        init(syntax, text.substring(0, offset) + newText + text.substring(offset));
    }

    public void removeText(int offset, int length) {
        init(syntax, text.substring(0, offset) + text.substring(offset + length));
    }

//  Factory methods

    public static LexerWrapper javaLexer(String input) { return new LexerWrapper(SupportedSyntax.JAVA, input); }
    public static LexerWrapper jsLexer(String input) { return new LexerWrapper(SupportedSyntax.ECMASCRIPT, input); }
}
