package syntax.document;

import syntax.LexerWrapper;
import syntax.SyntaxColorRule;
import syntax.brackets.BracketIndex;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Created by avyatkin on 02/04/16.
 */
public class CodeDocumentFactory {
    static final String[] JAVA_KEYWORDS = {"'abstract'","'assert'","'boolean'","'break'","'byte'","'case'","'catch'","'char'","'class'","'const'","'continue'","'default'","'do'","'double'","'else'","'enum'","'extends'","'final'","'finally'","'float'","'for'","'if'","'goto'","'implements'","'import'","'instanceof'","'int'","'interface'","'long'","'native'","'new'","'package'","'private'","'protected'","'public'","'return'","'short'","'static'","'strictfp'","'super'","'switch'","'synchronized'","'this'","'throw'","'throws'","'transient'","'try'","'void'","'volatile'","'while'"};

    public static CodeDocument forJava() {
        List<SyntaxColorRule> colorRules = Arrays.asList(
            new SyntaxColorRule(Color.BLUE, JAVA_KEYWORDS),
            new SyntaxColorRule(Color.ORANGE, "Identifier"),
            new SyntaxColorRule(Color.GRAY, "LINE_COMMENT", "COMMENT"));

        List<Function<String, BracketIndex>> indexFactories = Arrays.asList(
            code -> new BracketIndex("'('", "')'", LexerWrapper.javaLexer(code)),
            code -> new BracketIndex("'{'", "'}'", LexerWrapper.javaLexer(code))
        );

        return new CodeDocument(colorRules, indexFactories);
    }
//
//    public static CodeDocument forJs() {
//        new CodeDocument();
//    }
}
