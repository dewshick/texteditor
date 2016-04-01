package languages.java;

import languages.java.antlr.JavaLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;

import javax.swing.text.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by avyatkin on 15/03/16.
 */
public class JavaDocument extends DefaultStyledDocument {
    static final HashSet<String> keywords = new HashSet<>(Arrays.asList("'abstract'","'assert'","'boolean'","'break'","'byte'","'case'","'catch'","'char'","'class'","'const'","'continue'","'default'","'do'","'double'","'else'","'enum'","'extends'","'final'","'finally'","'float'","'for'","'if'","'goto'","'implements'","'import'","'instanceof'","'int'","'interface'","'long'","'native'","'new'","'package'","'private'","'protected'","'public'","'return'","'short'","'static'","'strictfp'","'super'","'switch'","'synchronized'","'this'","'throw'","'throws'","'transient'","'try'","'void'","'volatile'","'while'"));

    static final StyleContext cont = StyleContext.getDefaultStyleContext();

    static final AttributeSet keywordAttr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.RED);

    static final AttributeSet defaultAttr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLACK);

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
        CharStream cs = new ANTLRInputStream(text);
        JavaLexer lexer = new JavaLexer(cs);

        for (Token t : lexer.getAllTokens()) {
            String tokenName = JavaLexer.VOCABULARY.getDisplayName(t.getType());

            AttributeSet tokenAttr = defaultAttr;
            if (keywords.contains(tokenName))
                tokenAttr = keywordAttr;
            setCharacterAttributes(t.getStartIndex(), t.getText().length(), tokenAttr, false);
        }
    }
}
