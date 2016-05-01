package syntax.document;

import com.sun.tools.javac.util.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * Created by avyatkin on 02/04/16.
 */
public enum SupportedSyntax {
    JAVA, ECMASCRIPT;

    public static final String STUB_ID = "stub";

    public String[] keywords() {
        return oneOf(
                new String[]{"'abstract'","'assert'","'boolean'","'break'","'byte'","'case'","'catch'","'char'","'class'","'const'","'continue'","'default'","'do'","'double'","'else'","'enum'","'extends'","'final'","'finally'","'float'","'for'","'if'","'goto'","'implements'","'import'","'instanceof'","'int'","'interface'","'long'","'native'","'new'","'package'","'private'","'protected'","'public'","'return'","'short'","'static'","'strictfp'","'super'","'switch'","'synchronized'","'this'","'throw'","'throws'","'transient'","'try'","'void'","'volatile'","'while'"},
                new String[]{"'break'","'do'","'instanceof'","'typeof'","'case'","'else'","'new'","'var'","'catch'","'finally'","'return'","'void'","'continue'","'for'","'switch'","'while'","'debugger'","'function'","'this'","'with'","'default'","'if'","'throw'","'delete'","'in'","'try'","'class'","'enum'","'extends'","'super'","'const'","'export'","'import'","'implements'","'let'","'private'","'public'","'interface'","'package'","'protected'","'static'","'yield'"}
        );
    }

    public String identifier() {
        return "Identifier";
    }

    public String[] comments() {
        return oneOf(
                new String[]{ "LINE_COMMENT", "COMMENT", "BROKEN_COMMENT" },
                new String[]{ "MultiLineComment", "SingleLineComment", "BrokenMultiLineComment" }
        );
    }

    public List<Pair<String, String>> bracketPairs() {
        return Arrays.asList(new Pair<>("'('", "')'" ), new Pair<>( "'['", "']'" ), new Pair<>( "'{'", "'}'"));
    }

    private <T> T oneOf(T forJava, T forJs) {
        switch (this) {
            case JAVA:
                return forJava;
            case ECMASCRIPT:
                return forJs;
        }
        throw new IllegalArgumentException(
                "Unexpected argument: " + this + " should be " + JAVA + " or " + ECMASCRIPT);
    }
}
