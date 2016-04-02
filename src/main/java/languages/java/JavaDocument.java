package languages.java;

import jdk.nashorn.internal.parser.Lexer;
import languages.LexerWrapper;
import languages.java.antlr.JavaLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;

import javax.swing.text.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by avyatkin on 15/03/16.
 */

// TODO: use common CodeDocument and pass rule-based engine for tokens' color in java & js
public class JavaDocument extends DefaultStyledDocument {
    static final HashSet<String> keywords = new HashSet<>(Arrays.asList("'abstract'","'assert'","'boolean'","'break'","'byte'","'case'","'catch'","'char'","'class'","'const'","'continue'","'default'","'do'","'double'","'else'","'enum'","'extends'","'final'","'finally'","'float'","'for'","'if'","'goto'","'implements'","'import'","'instanceof'","'int'","'interface'","'long'","'native'","'new'","'package'","'private'","'protected'","'public'","'return'","'short'","'static'","'strictfp'","'super'","'switch'","'synchronized'","'this'","'throw'","'throws'","'transient'","'try'","'void'","'volatile'","'while'"));

    static final StyleContext cont = StyleContext.getDefaultStyleContext();

    static final AttributeSet keywordAttr = colorAttr(Color.BLUE);
    static final AttributeSet identifierAttr = colorAttr(Color.orange);
    static final AttributeSet commentAttr = colorAttr(Color.GRAY);
    static final AttributeSet defaultAttr = colorAttr(Color.BLACK);

    private static final AttributeSet colorAttr(Color color) {
        StyleContext cont = StyleContext.getDefaultStyleContext();
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, color);
    }

    public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        colorizeText();
    }

    public void remove (int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        colorizeText();
    }

    private void colorizeText() throws BadLocationException {
        String text = getText(0, getLength());
        LexerWrapper lexer = LexerWrapper.javaLexer(text);

        for (Token t : lexer.tokens()) {
            String tokenName = lexer.getTokenType(t);
            AttributeSet tokenAttr = defaultAttr;
            if (keywords.contains(tokenName))
                tokenAttr = keywordAttr;
            else if (tokenName.equals("Identifier"))
                tokenAttr = identifierAttr;
            else if (Arrays.asList("LINE_COMMENT", "COMMENT").contains(tokenName))
                tokenAttr = commentAttr;

            setCharacterAttributes(t.getStartIndex(), t.getText().length(), tokenAttr, false);
        }
    }
}
