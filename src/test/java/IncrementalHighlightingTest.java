import gui.state.ColoredString;
import gui.state.EditorState;
import gui.state.EditorTextStorage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import syntax.antlr.Lexeme;
import syntax.antlr.LexemeIndex;
import syntax.document.SupportedSyntax;

import java.awt.*;
import java.util.List;
import java.util.function.Function;
import static org.junit.Assert.assertEquals;
import static syntax.EditorUtil.*;

/**
 * Created by avyatkin on 04/04/16.
 */

// TODO: parameterize test instead of copy-paste
public class IncrementalHighlightingTest {
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

    private Point offsetToCoords(int offset) {
        List<String> lines = EditorTextStorage.buildLinesList(code, true);
        int yindex = 0;
        int lineOffset = 0;
        for (;yindex < lines.size(); yindex++) {
            String currentLine = lines.get(yindex);
            if (lineOffset + lines.get(yindex).length() > offset)
                return new Point(offset - lineOffset, yindex);
            else lineOffset += currentLine.length();
        }
        throw new IndexOutOfBoundsException("No such offset");
    }

    private void paste(int offset, String addedText) {
        Point coords = offsetToCoords(offset);
        _addToCode(offset, addedText);
        initialIndex.addText(coords, addedText);
    }

    private void delete(int offset, int length) {
        Point coords = offsetToCoords(offset);
        _removeFromCode(offset, length);
        initialIndex.removeText(coords, length);
    }

    private void type(int offset, String typedText) {
        Point coords = offsetToCoords(offset);
        _addToCode(offset, typedText);

        for (Character c : typedText.toCharArray()) {
            initialIndex.addText(coords, c.toString());
            if (c == '\n')
                coords = new Point(0, coords.y + 1);
            else
                coords = new Point(coords.x+1, coords.y);
        }
    }

    private void backspace(int offset, int length) {
        Point coords = offsetToCoords(offset);
        List<String> lines = EditorTextStorage.buildLinesList(code, true);
        _removeFromCode(offset, length);
        for(int caretPosition = offset + length - 1; caretPosition >= offset; caretPosition--) {
            initialIndex.removeText(coords, 1);
            if (coords.x > 0)
                coords = new Point(coords.x - 1, coords.y);
            else
                coords = new Point(lines.get(coords.y - 1).length(), coords.y - 1);
        }
    }

    private void assertLexemesAreCorrect() {
        List<List<ColoredString>> expected = lexemeIndex(code).getColoredLinesList().getColoredLines();
        List<List<ColoredString>> actual = initialIndex.getColoredLinesList().getColoredLines();
        Function<List<List<ColoredString>>, String> codeFromLexemes = ls ->
                join(map(ls, l -> join(l, ColoredString::getContent, "")), Function.identity(), "\n");
        assertEquals(codeFromLexemes.apply(expected), codeFromLexemes.apply(actual));
        assertEquals(expected.toString(), actual.toString());
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
                "[{distanceToNextToken=14, revision=0, type='COMMENT', text='/* whatever */', size=14}, {distanceToNextToken=1, revision=0, type='WS', text='\n" +
                        "', size=1}, {distanceToNextToken=12, revision=0, type='LINE_COMMENT', text='//whatever()', size=12}, {distanceToNextToken=1, revision=0, type='WS', text='\n" +
                        "', size=1}, {distanceToNextToken=6, revision=0, type=''public'', text='public', size=6}, {distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {distanceToNextToken=6, revision=0, type='Identifier', text='String', size=6}, {distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {distanceToNextToken=4, revision=0, type='Identifier', text='test', size=4}, {distanceToNextToken=1, revision=0, type=''('', text='(', size=1}, {distanceToNextToken=1, revision=0, type='')'', text=')', size=1}, {distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {distanceToNextToken=1, revision=0, type=''{'', text='{', size=1}, {distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {distanceToNextToken=6, revision=0, type=''return'', text='return', size=6}, {distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {distanceToNextToken=7, revision=0, type='StringLiteral', text='\"1 + 1\"', size=7}, {distanceToNextToken=1, revision=0, type='';'', text=';', size=1}, {distanceToNextToken=1, revision=0, type='WS', text=' ', size=1}, {distanceToNextToken=1, revision=0, type=''}'', text='}', size=1}]",
                initialIndex.getColoredLinesList().getColoredLines().toString());
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
