package syntax.document;

import syntax.antlr.LexerWrapper;
import syntax.brackets.BracketIndex;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Created by avyatkin on 02/04/16.
 */

public class CodeDocumentFactory {

    static final String[] JAVA_KEYWORDS = {"'abstract'","'assert'","'boolean'","'break'","'byte'","'case'","'catch'","'char'","'class'","'const'","'continue'","'default'","'do'","'double'","'else'","'enum'","'extends'","'final'","'finally'","'float'","'for'","'if'","'goto'","'implements'","'import'","'instanceof'","'int'","'interface'","'long'","'native'","'new'","'package'","'private'","'protected'","'public'","'return'","'short'","'static'","'strictfp'","'super'","'switch'","'synchronized'","'this'","'throw'","'throws'","'transient'","'try'","'void'","'volatile'","'while'"};
    public static CodeDocument forJava() {
        return documentForSpecificLanguage(
                JAVA_KEYWORDS,
                "Identifier",
                new String[]{ "LINE_COMMENT", "COMMENT" },
                new String[][]{ { "'('", "')'" }, { "'{'", "'}'" }, {"'['", "']'"} },
                LexerWrapper::jsLexer);
    }

//    for some reason, function keyword in js is recognized as identifier
    static final String[] JS_KEYWORDS = {"'break'","'do'","'instanceof'","'typeof'","'case'","'else'","'new'","'var'","'catch'","'finally'","'return'","'void'","'continue'","'for'","'switch'","'while'","'debugger'","'function'","'this'","'with'","'default'","'if'","'throw'","'delete'","'in'","'try'","'class'","'enum'","'extends'","'super'","'const'","'export'","'import'","'implements'","'let'","'private'","'public'","'interface'","'package'","'protected'","'static'","'yield'"};
    public static CodeDocument forJs() {
        return documentForSpecificLanguage(
                JS_KEYWORDS,
                "Identifier",
                new String[]{ "MultiLineComment", "SingleLineComment" },
                new String[][]{ { "'('", "')'" }, { "'{'", "'}'" }, {"'['", "']'"} },
                LexerWrapper::jsLexer);
    }

    private static CodeDocument documentForSpecificLanguage(String[] keywordTokens,
                                                            String identifier,
                                                            String[] commentTokens,
                                                            String[][] bracketPairs,
                                                            Function<String,LexerWrapper> lexerFactory) {
        List<SyntaxColorRule> colorRules = Arrays.asList(
                new SyntaxColorRule(Color.BLUE, keywordTokens),
                new SyntaxColorRule(Color.ORANGE, identifier),
                new SyntaxColorRule(Color.LIGHT_GRAY, commentTokens));
        List<Function<String, BracketIndex>> bracketIndexFactories = new ArrayList<>();
        for (String[] bracketTokenPair : bracketPairs)
            bracketIndexFactories.add(code ->
                    new BracketIndex(bracketTokenPair[0],bracketTokenPair[1], lexerFactory.apply(code)));
        return new CodeDocument(colorRules, lexerFactory, bracketIndexFactories);
    }
}
