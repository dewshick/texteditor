import org.junit.Before;
import org.junit.Test;
import syntax.antlr.LexerWrapper;

import static org.junit.Assert.assertEquals;

/**
 * Created by avyatkin on 04/04/16.
 */
public class IncrementalHighlightingTest {
    static final String CODE =
            "/* whatever */\n" +
            "//whatever()\n" +
            "public String test() { return \"1 + 1\"; }";

    private LexerWrapper lexemeIndex(String code) { return LexerWrapper.javaLexer(code); }

    @Before public void init() { initialIndex = lexemeIndex(CODE); }

    LexerWrapper initialIndex;

    @Test public void isInitialized() { assertEquals(
            "[{offset=0, distanceToNextToken=14, revision=0, type='MultiLineComment', size=14}, " +
                    "{offset=14, distanceToNextToken=1, revision=0, type='LineTerminator', size=1}, " +
                    "{offset=15, distanceToNextToken=12, revision=0, type='SingleLineComment', size=12}, " +
                    "{offset=27, distanceToNextToken=1, revision=0, type='LineTerminator', size=1}, " +
                    "{offset=28, distanceToNextToken=6, revision=0, type='Public', size=6}, " +
                    "{offset=34, distanceToNextToken=1, revision=0, type='WhiteSpaces', size=1}, " +
                    "{offset=35, distanceToNextToken=6, revision=0, type='Identifier', size=6}, " +
                    "{offset=41, distanceToNextToken=1, revision=0, type='WhiteSpaces', size=1}, " +
                    "{offset=42, distanceToNextToken=4, revision=0, type='Identifier', size=4}, " +
                    "{offset=46, distanceToNextToken=1, revision=0, type=''('', size=1}, " +
                    "{offset=47, distanceToNextToken=1, revision=0, type='')'', size=1}, " +
                    "{offset=48, distanceToNextToken=1, revision=0, type='WhiteSpaces', size=1}, " +
                    "{offset=49, distanceToNextToken=1, revision=0, type=''{'', size=1}, " +
                    "{offset=50, distanceToNextToken=1, revision=0, type='WhiteSpaces', size=1}, " +
                    "{offset=51, distanceToNextToken=6, revision=0, type=''return'', size=6}, " +
                    "{offset=57, distanceToNextToken=1, revision=0, type='WhiteSpaces', size=1}, " +
                    "{offset=58, distanceToNextToken=7, revision=0, type='StringLiteral', size=7}, " +
                    "{offset=65, distanceToNextToken=1, revision=0, type='';'', size=1}, " +
                    "{offset=66, distanceToNextToken=1, revision=0, type='WhiteSpaces', size=1}, " +
                    "{offset=67, distanceToNextToken=0, revision=0, type=''}'', size=1}]",
            initialIndex.lexemes().toString()); }

    @Test public void isInitializedFromEmptyString() { assertEquals("[]", lexemeIndex("").lexemes().toString()); }

    private void checkIfTextAddedCorrectly(int position, String addedText) {
        initialIndex.addText(position, addedText);
        String newText = CODE.substring(0,position) + addedText + CODE.substring(position);
        assertEquals(initialIndex.lexemes(), lexemeIndex(newText).lexemes());
    }

    @Test public void lexemeAddedInBeginning() { checkIfTextAddedCorrectly(0, "public"); }
    @Test public void lexemeAddedInMiddle() { checkIfTextAddedCorrectly(CODE.indexOf("String"), "static"); }
    @Test public void lexemeAddedInEnd() { checkIfTextAddedCorrectly(CODE.length()-1, "static"); }
    @Test public void lexemeModifiedViaAddingText() { checkIfTextAddedCorrectly(CODE.indexOf("*/"), "static"); }
    @Test public void lexemeChangedViaAddingText() { checkIfTextAddedCorrectly(CODE.indexOf("public"), "super"); }
    @Test public void commentingTest() { checkIfTextAddedCorrectly(CODE.indexOf("public"), "//"); }

    private void checkIfTextRemovedCorrectly(int position, int length) {
        initialIndex.removeText(position, length);
        String newText = CODE.substring(0,position) + CODE.substring(length + position);
        assertEquals(initialIndex.lexemes(), lexemeIndex(newText).lexemes());
    }

    @Test public void lexemeRemovedFromBeginning() { checkIfTextRemovedCorrectly(CODE.indexOf("/*"), 2); }
    @Test public void uncommenting() { checkIfTextRemovedCorrectly(CODE.indexOf("//"), "//".length()); }
    @Test public void completeRemoval() { checkIfTextRemovedCorrectly(0, CODE.length()); }
    @Test public void lexemeAddedViaRemoval() { checkIfTextRemovedCorrectly(CODE.indexOf("\""), 1); }
    @Test public void lexemeChangedViaRemoval() { checkIfTextRemovedCorrectly(CODE.indexOf("return"), 1); }
    @Test public void lexemeRemovedViaRemoval() { checkIfTextRemovedCorrectly(CODE.indexOf("return"), "return".length()); }

    @Test
    public void allCodeChangeTest() {
        String newCode = "public static int whatever() {\n}\n";
        initialIndex.removeText(0, CODE.length());
        initialIndex.addText(0, newCode);
        assertEquals(initialIndex.lexemes(), lexemeIndex(newCode).lexemes());
    }
}
