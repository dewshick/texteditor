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
    LexerWrapper initialIndex;
    String code;

    private LexerWrapper lexemeIndex(String code) { return new LexerWrapper(SupportedSyntax.JAVA, code); }

    @Before public void init() {
        code = "/* whatever */\n" +
                "//whatever()\n" +
                "public String test() { return \"1 + 1\"; }";
        initialIndex = lexemeIndex(code);
    }

    private void initWithCode(String initCode) {
        code = initCode;
        initialIndex = lexemeIndex(code);
    }

    private void _addToCode(int offset, String addedText) {
        code = code.substring(0, offset) + addedText + code.substring(offset);
    }

    private void _removeFromCode(int offset, int length) {
        code = code.substring(0, offset) + code.substring(length + offset);
    }

    private void paste(int offset, String addedText) {
        _addToCode(offset, addedText);
        initialIndex.addText(offset, addedText);
    }

    private void type(int offset, String typedText) {
        _addToCode(offset, typedText);
        int charPosition = offset;
        for (Character c : typedText.toCharArray()) {
            initialIndex.addText(charPosition, c.toString());
            charPosition++;
        }
    }

    private void delete(int offset, int length) {
        _removeFromCode(offset, length);
        initialIndex.removeText(offset, length);
    }

    private void backspace(int offset, int length) {
        _removeFromCode(offset, length);
        for(int caretPosition = offset + length - 1; caretPosition >= offset; caretPosition--)
            initialIndex.removeText(caretPosition, 1);
    }

    private void assertLexemesAreCorrect() {
        List<Lexeme> expected = lexemeIndex(code).lexemes();
        List<Lexeme> actual = initialIndex.lexemes();
        Function<List<Lexeme>, String> codeFromLexemes = ls -> ls.stream().map(Lexeme::getText).collect(Collectors.joining());
        assertEquals(codeFromLexemes.apply(expected), codeFromLexemes.apply(actual));
        assertEquals(expected, actual);
    }

    private void checkIfPastedTextIsLexedCorrectly(int offset, String addedText) {
        paste(offset, addedText);
        assertLexemesAreCorrect();
    }

    private void checkIfTypedTextIsLexedCorrectly(int offset, String addedText) {
        type(offset, addedText);
        assertLexemesAreCorrect();
    }

    private void checkIfTextDeletedCorrectly(int offset, int length) {
        delete(offset, length);
        assertLexemesAreCorrect();
    }

    private void checkIfTextBackspacedCorrectly(int offset, int length) {
        backspace(offset, length);
        assertLexemesAreCorrect();
    }

    @Test public void initialized() {
        assertEquals(
                "[{offset=0, distanceToNextToken=14, revision=0, type='COMMENT', text='/* whatever */', size=14}, {offset=14, distanceToNextToken=1, revision=0, type='WS', text='\n" +
                        "', size=1}, {offset=15, distanceToNextToken=12, revision=0, type='LINE_COMMENT', text='//whatever()', size=12}, {offset=27, distanceToNextToken=1, revision=0, type='WS', text='\n" +
                        "', size=1}, {offset=28, distanceToNextToken=6, revision=0, type=''public'', text='public', size=6}, {offset=34, distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {offset=35, distanceToNextToken=6, revision=0, type='Identifier', text='String', size=6}, {offset=41, distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {offset=42, distanceToNextToken=4, revision=0, type='Identifier', text='test', size=4}, {offset=46, distanceToNextToken=1, revision=0, type=''('', text='(', size=1}, {offset=47, distanceToNextToken=1, revision=0, type='')'', text=')', size=1}, {offset=48, distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {offset=49, distanceToNextToken=1, revision=0, type=''{'', text='{', size=1}, {offset=50, distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {offset=51, distanceToNextToken=6, revision=0, type=''return'', text='return', size=6}, {offset=57, distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {offset=58, distanceToNextToken=7, revision=0, type='StringLiteral', text='\"1 + 1\"', size=7}, {offset=65, distanceToNextToken=1, revision=0, type='';'', text=';', size=1}, {offset=66, distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {offset=67, distanceToNextToken=1, revision=0, type=''}'', text='}', size=1}]",
                initialIndex.lexemes().toString());
    }
    @Test public void initializedFromEmptyString() {
        initWithCode("");
        assertLexemesAreCorrect();
    }

    @Test public void changeLexemeOnTheEnd() {
        initWithCode("public");
        paste(code.length(), "a");
        assertLexemesAreCorrect();
    }

    @Test public void lexemeAddedInBeginning() { checkIfPastedTextIsLexedCorrectly(0, "public"); }
    @Test public void lexemeAddedInMiddle() { checkIfPastedTextIsLexedCorrectly(code.indexOf("String"), "static"); }
    @Test public void lexemeAddedInEnd() { checkIfPastedTextIsLexedCorrectly(code.length(), "static"); }
    @Test public void lexemeModifiedViaAddingText() { checkIfPastedTextIsLexedCorrectly(code.indexOf("*/"), "static"); }
    @Test public void lexemeChangedViaAddingText() { checkIfPastedTextIsLexedCorrectly(code.indexOf("public"), "super"); }
    @Test public void commentingTest() { checkIfPastedTextIsLexedCorrectly(code.indexOf("public"), "//"); }
    @Test public void emptyTextAdded() { checkIfPastedTextIsLexedCorrectly(0, ""); }
    @Test public void multilineCommentingTest() {
        type(code.indexOf("public"), "/*");
        type(code.indexOf(";") + 1, "*/");
        assertLexemesAreCorrect();
    }

    @Test public void lexemeTypedInBeginning() { checkIfTypedTextIsLexedCorrectly(0, "public"); }
    @Test public void lexemeTypedInMiddle() { checkIfTypedTextIsLexedCorrectly(code.indexOf("String"), "static"); }
    @Test public void lexemeTypedInEnd() { checkIfTypedTextIsLexedCorrectly(code.length(), "static"); }
    @Test public void lexemeTypedAndModifiedViaAddingText() { checkIfTypedTextIsLexedCorrectly(code.indexOf("*/"), "static"); }
    @Test public void lexemeTypedAndChangedViaAddingText() { checkIfTypedTextIsLexedCorrectly(code.indexOf("public"), "super"); }
    @Test public void commentingTypedTest() { checkIfTypedTextIsLexedCorrectly(code.indexOf("public"), "//"); }
    @Test public void multilineCommentingTypedTest() {
        type(code.indexOf("public"), "/*");
        type(code.indexOf(";") + 1, "*/");
        assertLexemesAreCorrect();
    }

    @Test public void lexemeDeletedFromBeginning() { checkIfTextDeletedCorrectly(code.indexOf("/*"), 2); }
    @Test public void uncommentingViaDelete() { checkIfTextDeletedCorrectly(code.indexOf("//"), "//".length()); }
    @Test public void completeRemovalViaDelete() { checkIfTextDeletedCorrectly(0, code.length()); }
    @Test public void lexemeAddedViaRemovalViaDelete() { checkIfTextDeletedCorrectly(code.indexOf("\""), 1); }
    @Test public void lexemeChangedViaRemovalViaDelete() { checkIfTextDeletedCorrectly(code.indexOf("return"), 1); }
    @Test public void lexemeRemovedViaDelete() { checkIfTextDeletedCorrectly(code.indexOf("return"), "return".length()); }
    @Test public void emptyTextRemovedCorrectly() { checkIfTextDeletedCorrectly(0, 0); }
    @Test public void multilineCommentDeletion() {
        delete(code.indexOf("/*"), 2);
        delete(code.indexOf("*/"), 2);
        assertLexemesAreCorrect();
    }

    @Test public void lexemeRemovedFromBeginning() { checkIfTextBackspacedCorrectly(code.indexOf("/*"), 2); }
    @Test public void uncommenting() { checkIfTextBackspacedCorrectly(code.indexOf("//"), "//".length()); }
    @Test public void completeRemoval() { checkIfTextBackspacedCorrectly(0, code.length()); }
    @Test public void lexemeAddedViaRemoval() { checkIfTextBackspacedCorrectly(code.indexOf("\""), 1); }
    @Test public void lexemeChangedViaRemoval() { checkIfTextBackspacedCorrectly(code.indexOf("return"), 1); }
    @Test public void lexemeRemovedViaRemoval() { checkIfTextBackspacedCorrectly(code.indexOf("return"), "return".length()); }
    @Test public void multilineCommentRemoval() {
        backspace(code.indexOf("*/"), 2);
        backspace(code.indexOf("/*"), 2);
        assertLexemesAreCorrect();
    }

    @Test public void emptyTextRemovedFromEmptyDocument() {
        initWithCode("");
        delete(0, 0);
        assertLexemesAreCorrect();
    }
    @Test public void emptyTextAddedToEmptyDocument() {
        initWithCode("");
        paste(0, "");
        assertLexemesAreCorrect();
    }

    @Test
    public void allCodeChangeTest() {
        String newCode = "public static int whatever() {\n}\n";
        delete(0, code.length());
        paste(0, newCode);
        assertLexemesAreCorrect();
    }

    @Test
    public void allCodeTypedInTest() {
        String newCode = "public static int whatever() {\n}\n";
        backspace(0, code.length());
        type(0, newCode);
        assertLexemesAreCorrect();
    }
}
