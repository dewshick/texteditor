import org.junit.Before;
import org.junit.Test;
import syntax.antlr.Lexeme;
import syntax.antlr.LexerWrapper;
import syntax.document.SupportedSyntax;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by avyatkin on 04/04/16.
 */
public class IncrementalHighlightingTest {
    static final String CODE =
            "/* whatever */\n" +
            "//whatever()\n" +
            "public String test() { return \"1 + 1\"; }";

    private LexerWrapper lexemeIndex(String code) { return new LexerWrapper(SupportedSyntax.JAVA, code); }

    @Before public void init() { initialIndex = lexemeIndex(CODE); emptyIndex = lexemeIndex(""); }

    LexerWrapper initialIndex;
    LexerWrapper emptyIndex;

    @Test public void isInitialized() { assertEquals(
            "[{offset=0, distanceToNextToken=14, revision=0, type='MultiLineComment', text='/* whatever */', size=14}, {offset=14, distanceToNextToken=1, revision=0, type='LineTerminator', text='\n" +
                    "', size=1}, {offset=15, distanceToNextToken=12, revision=0, type='SingleLineComment', text='//whatever()', size=12}, {offset=27, distanceToNextToken=1, revision=0, type='LineTerminator', text='\n" +
                    "', size=1}, {offset=28, distanceToNextToken=6, revision=0, type='Public', text='public', size=6}, {offset=34, distanceToNextToken=1, revision=0, type='WhiteSpaces', text=' ', size=1}, {offset=35, distanceToNextToken=6, revision=0, type='Identifier', text='String', size=6}, {offset=41, distanceToNextToken=1, revision=0, type='WhiteSpaces', text=' ', size=1}, {offset=42, distanceToNextToken=4, revision=0, type='Identifier', text='test', size=4}, {offset=46, distanceToNextToken=1, revision=0, type=''('', text='(', size=1}, {offset=47, distanceToNextToken=1, revision=0, type='')'', text=')', size=1}, {offset=48, distanceToNextToken=1, revision=0, type='WhiteSpaces', text=' ', size=1}, {offset=49, distanceToNextToken=1, revision=0, type=''{'', text='{', size=1}, {offset=50, distanceToNextToken=1, revision=0, type='WhiteSpaces', text=' ', size=1}, {offset=51, distanceToNextToken=6, revision=0, type=''return'', text='return', size=6}, {offset=57, distanceToNextToken=1, revision=0, type='WhiteSpaces', text=' ', size=1}, {offset=58, distanceToNextToken=7, revision=0, type='StringLiteral', text='\"1 + 1\"', size=7}, {offset=65, distanceToNextToken=1, revision=0, type='';'', text=';', size=1}, {offset=66, distanceToNextToken=1, revision=0, type='WhiteSpaces', text=' ', size=1}, {offset=67, distanceToNextToken=1, revision=0, type=''}'', text='}', size=1}]",
            initialIndex.lexemes().toString()); }

    @Test public void isInitializedFromEmptyString() { assertEquals("[]", lexemeIndex("").lexemes().toString()); }

    private void checkIfTextPastedCorrectly(int position, String addedText) {
        initialIndex.addText(position, addedText);
        String newText = CODE.substring(0,position) + addedText + CODE.substring(position);
        assertLexemesAreCorrect(newText, initialIndex);
    }

    private void checkIfTypedTextIsLexedCorrectly(int position, String addedText) {
        int charPosition = position;
        for (Character c : addedText.toCharArray()) {
            initialIndex.addText(charPosition, c.toString());
            charPosition++;
        }
        String newText = CODE.substring(0,position) + addedText + CODE.substring(position);
        assertLexemesAreCorrect(newText, initialIndex);
    }

    @Test public void lexemeAddedInBeginning() { checkIfTextPastedCorrectly(0, "public"); }
    @Test public void lexemeAddedInMiddle() { checkIfTextPastedCorrectly(CODE.indexOf("String"), "static"); }
    @Test public void lexemeAddedInEnd() { checkIfTextPastedCorrectly(CODE.length(), "static"); }
    @Test public void lexemeModifiedViaAddingText() { checkIfTextPastedCorrectly(CODE.indexOf("*/"), "static"); }
    @Test public void lexemeChangedViaAddingText() { checkIfTextPastedCorrectly(CODE.indexOf("public"), "super"); }
    @Test public void commentingTest() { checkIfTextPastedCorrectly(CODE.indexOf("public"), "//"); }
    @Test public void emptyTextAdded() { checkIfTextPastedCorrectly(0, ""); }

    @Test public void lexemeTypedInBeginning() { checkIfTypedTextIsLexedCorrectly(0, "public"); }
    @Test public void lexemeTypedInMiddle() { checkIfTypedTextIsLexedCorrectly(CODE.indexOf("String"), "static"); }
    @Test public void lexemeTypedInEnd() { checkIfTypedTextIsLexedCorrectly(CODE.length(), "static"); }
    @Test public void lexemeTypedAndModifiedViaAddingText() { checkIfTypedTextIsLexedCorrectly(CODE.indexOf("*/"), "static"); }
    @Test public void lexemeTypedAndChangedViaAddingText() { checkIfTypedTextIsLexedCorrectly(CODE.indexOf("public"), "super"); }
    @Test public void commentingTypedTest() { checkIfTypedTextIsLexedCorrectly(CODE.indexOf("public"), "//"); }

    private void checkIfTextDeletedCorrectly(int position, int length) {
        initialIndex.removeText(position, length);
        String newText = CODE.substring(0,position) + CODE.substring(length + position);
        assertLexemesAreCorrect(newText, initialIndex);
    }

    private void checkIfTextBackspacedCorrectly(int position, int length) {
        for(int caretPosition = position + length - 1; caretPosition >= position; caretPosition--)
            initialIndex.removeText(caretPosition, 1);

        String newText = CODE.substring(0,position) + CODE.substring(length + position);
        assertLexemesAreCorrect(newText, initialIndex);
    }

    @Test public void lexemeDeletedFromBeginning() { checkIfTextDeletedCorrectly(CODE.indexOf("/*"), 2); }
    @Test public void uncommentingViaDelete() { checkIfTextDeletedCorrectly(CODE.indexOf("//"), "//".length()); }
    @Test public void completeRemovalViaDelete() { checkIfTextDeletedCorrectly(0, CODE.length()); }
    @Test public void lexemeAddedViaRemovalViaDelete() { checkIfTextDeletedCorrectly(CODE.indexOf("\""), 1); }
    @Test public void lexemeChangedViaRemovalViaDelete() { checkIfTextDeletedCorrectly(CODE.indexOf("return"), 1); }
    @Test public void lexemeRemovedViaDelete() { checkIfTextDeletedCorrectly(CODE.indexOf("return"), "return".length()); }
    @Test public void emptyTextRemovedCorrectly() { checkIfTextDeletedCorrectly(0, 0); }

    @Test public void lexemeRemovedFromBeginning() { checkIfTextBackspacedCorrectly(CODE.indexOf("/*"), 2); }
    @Test public void uncommenting() { checkIfTextBackspacedCorrectly(CODE.indexOf("//"), "//".length()); }
    @Test public void completeRemoval() { checkIfTextBackspacedCorrectly(0, CODE.length()); }
    @Test public void lexemeAddedViaRemoval() { checkIfTextBackspacedCorrectly(CODE.indexOf("\""), 1); }
    @Test public void lexemeChangedViaRemoval() { checkIfTextBackspacedCorrectly(CODE.indexOf("return"), 1); }
    @Test public void lexemeRemovedViaRemoval() { checkIfTextBackspacedCorrectly(CODE.indexOf("return"), "return".length()); }

    @Test public void emptyTextRemovedFromEmptyDocument() {
        emptyIndex.removeText(0,0);
        assertLexemesAreCorrect("", emptyIndex);
    }
    @Test public void emptyTextAddedToEmptyDocument() {  }

    @Test
    public void allCodeChangeTest() {
        String newCode = "public static int whatever() {\n}\n";
        initialIndex.removeText(0, CODE.length());
        initialIndex.addText(0, newCode);
        assertLexemesAreCorrect(newCode, initialIndex);
    }

    @Test
    public void allCodeTypedInTest() {
        String newCode = "public static int whatever() {\n}\n";
        initialIndex.removeText(0, CODE.length());
        for(int i = 0; i < newCode.length(); i++)
            initialIndex.addText(i, newCode.charAt(i) + "");
        assertLexemesAreCorrect(newCode, initialIndex);
    }

    private void assertLexemesAreCorrect(String expectedCodeToLex, LexerWrapper actualL) {
        List<Lexeme> expected = lexemeIndex(expectedCodeToLex).lexemes();
        List<Lexeme> actual = actualL.lexemes();
        Function<List<Lexeme>, String> codeFromLexemes = ls -> ls.stream().map(Lexeme::getText).collect(Collectors.joining());
        assertEquals(codeFromLexemes.apply(expected), codeFromLexemes.apply(actual));
        assertEquals(expected, actual);
    }
}
