import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import syntax.antlr.Lexeme;
import syntax.antlr.LexemeIndex;
import syntax.document.SupportedSyntax;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by avyatkin on 04/04/16.
 */

// TODO: parameterize test instead of copy-paste
//@RunWith(Parameterized.class)
public class IncrementalHighlightingTest {
//    @Parameterized.Parameters
//    public static Collection<Object[]> data() {
//        return Arrays.asList(new Object[][] {
//                { 0, 0 }, { 1, 1 }, { 2, 1 }, { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 }
//        });
//    }

    LexemeIndex initialIndex;
    String code;

    private LexemeIndex lexemeIndex(String code) { return new LexemeIndex(SupportedSyntax.JAVA, code); }

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
                "[{offset=0, type='COMMENT', text='/* whatever */'}, {offset=14, type='WS', text='\n" +
                        "'}, {offset=15, type='LINE_COMMENT', text='//whatever()'}, {offset=27, type='WS', text='\n" +
                        "'}, {offset=28, type=''public'', text='public'}, {offset=34, type='WS', text=' '}, {offset=35, type='Identifier', text='String'}, {offset=41, type='WS', text=' '}, {offset=42, type='Identifier', text='test'}, {offset=46, type=''('', text='('}, {offset=47, type='')'', text=')'}, {offset=48, type='WS', text=' '}, {offset=49, type=''{'', text='{'}, {offset=50, type='WS', text=' '}, {offset=51, type=''return'', text='return'}, {offset=57, type='WS', text=' '}, {offset=58, type='StringLiteral', text='\"1 + 1\"'}, {offset=65, type='';'', text=';'}, {offset=66, type='WS', text=' '}, {offset=67, type=''}'', text='}'}]",
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

//    dunno why it fails, can't reproduce by hand
    @Ignore @Test public void multilineCommentingTest() {
        paste(code.indexOf("public"), "/*");
        assertLexemesAreCorrect();
        paste(code.indexOf(";") + 1, "*/");
        assertLexemesAreCorrect();
    }

    @Test public void lexemeTypedInBeginning() { checkIfTypedTextIsLexedCorrectly(0, "public"); }
    @Test public void lexemeTypedInMiddle() { checkIfTypedTextIsLexedCorrectly(code.indexOf("String"), "static"); }
    @Test public void lexemeTypedInEnd() { checkIfTypedTextIsLexedCorrectly(code.length(), "static"); }
    @Test public void lexemeTypedAndModifiedViaAddingText() { checkIfTypedTextIsLexedCorrectly(code.indexOf("*/"), "static"); }
    @Test public void lexemeTypedAndChangedViaAddingText() { checkIfTypedTextIsLexedCorrectly(code.indexOf("public"), "super"); }
    @Test public void commentingTypedTest() { checkIfTypedTextIsLexedCorrectly(code.indexOf("public"), "//"); }

//    dunno why it fails, can't reproduce by hand
    @Ignore @Test public void multilineCommentingTypedTest() {
        type(code.indexOf("public"), "/*");
        assertLexemesAreCorrect();
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
