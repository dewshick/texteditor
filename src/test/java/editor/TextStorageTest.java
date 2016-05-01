package editor;

import static org.junit.Assert.assertEquals;

import gui.state.EditorTextStorage;
import org.junit.Before;
import org.junit.Test;
import syntax.antlr.LexemeIndex;
import syntax.document.SupportedSyntax;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by avyatkin on 14/04/16.
 */

//TODO refactor all the combinatorics if there will be time for it
public class TextStorageTest {
    EditorTextStorage textStorage;
    LexemeIndex index;
    LexemeIndex anotherIndex;
    String text;

    @Before
    public void init() {
        text = "firstline endoffirstline\nsecondline endofsecondline\nthirdline endofthirdline";
        textStorage = new EditorTextStorage(SupportedSyntax.JAVA);
        textStorage.setText(text);
        index = new LexemeIndex(SupportedSyntax.JAVA, text);
        anotherIndex = new LexemeIndex(SupportedSyntax.JAVA, text);
    }

    List<String> pastedTemplates = Arrays.asList("\n", "\n\n", "\n\n\n\n\n\n\n", "\nhello", "\nyeah\n", "whatever", "what\never", "what\nso\never", "too\nmany\nnewlines\nyeah\n");

    private void pastingTest(Point coords) {
        pastedTemplates.forEach(str -> {
            init();
            pasteText(coords, str);
            assertStateIsCorrect(str);
        });
    }

    @Test
    public void addInBeginning() {
        pastingTest(new Point(0, 0));
    }

    @Test
    public void addOnStartOfMiddleLine() {
        pastingTest(new Point(0, 1));
    }

    @Test
    public void addOnStartOfLastLine() {
        pastingTest(new Point(0, 2));
    }

    @Test
    public void addOnEndOfFirstLine() {
        pastingTest(new Point("firstline endoffirstline".length(), 0));
    }

    @Test
    public void addOnEndOfMiddleLine() {
        pastingTest(new Point("secondline endofsecondline".length(), 1));
    }

    @Test
    public void addOnEndOfLastLine() {
        pastingTest(new Point("thirdline endofthirdline".length(), 2));
    }


    @Test
    public void addInMiddleOfFirstLine() {
        pastingTest(new Point("firstline".length(), 0));
    }

    @Test
    public void addInMiddleOfMiddleLine() {
        pastingTest(new Point("secondline".length(), 1));
    }

    @Test
    public void addInMiddleOfLastLine() {
        pastingTest(new Point("thirdline".length(), 2));
    }

    @Test
    public void removeFromBeginning() {
        deleteText(new Point(0, 0), "firstline".length());
        assertStateIsCorrect();
    }

    @Test
    public void removeFromStartOfMiddleLine() {
        deleteText(new Point(0, 1), "secondline".length());
        assertStateIsCorrect();
    }

    @Test
    public void removeFromStartOfLastLine() {
        deleteText(new Point(0, 2), "thirdline".length());
        assertStateIsCorrect();
    }

    @Test
    public void removeFromEndOfFirstLine() {
        deleteText(new Point("firstline ".length(), 0), "endoffirstline".length());
        assertStateIsCorrect();
    }

    @Test
    public void removeMultilineFromEndOfFirstLine() {
        deleteText(new Point("firstline ".length(), 0), "endoffirstline".length() + 1);
        assertStateIsCorrect();
    }

    @Test
    public void removeFromEndOfMiddleLine() {
        deleteText(new Point("secondline ".length(), 1), "endofsecondline".length());
        assertStateIsCorrect();
    }

    @Test
    public void removeMultilineFromEndOfMiddleLine() {
        deleteText(new Point("secondline ".length(), 1), "endofsecondline".length() + 1);
        assertStateIsCorrect();
    }

    @Test
    public void removeFromOnEndOfLastLine() {
        deleteText(new Point("thirdline ".length(), 0), "endofthirdline".length());
        assertStateIsCorrect();
    }

    @Test
    public void removeNewlineInBeginning() {
        pasteText(new Point(0,0), "\n");
        deleteText(new Point(0,1), 1);
        assertStateIsCorrect();
    }

    @Test
    public void removeNewlineInMiddle() {
        pasteText(new Point(0,1), "\n");
        deleteText(new Point(0, 2), 1);
        assertStateIsCorrect();
    }

    @Test
    public void rmNewlineFromEnd() {
        pasteText(new Point("thirdline endofthirdline".length(), 2), "\n");
        deleteText(new Point("thirdline endofthirdline".length(), 2), 1);
        assertStateIsCorrect();
    }

    @Test
    public void addNewlineInTheBeginning() {
        pasteText(new Point(0, 0), "\n");
        assertStateIsCorrect();
    }

    @Test
    public void addNewlineInTheMiddle() {
        pasteText(new Point(0, 1), "\n");
        pasteText(new Point(0, 1), "\n");
        assertStateIsCorrect();
    }

    @Test
    public void addNewlineInTehEnd() {
        pasteText(new Point("thirdline endofthirdline".length(), 2), "\n");
        assertStateIsCorrect();
    }

    @Test
    public void getTextTest() {
        assertEquals(text, textStorage.getText(textStorage.beginningOfText(), textStorage.endOfText()));
        String[] lines = text.split("\n");
        assertEquals(lines[0] + "\n", textStorage.getText(textStorage.beginningOfText(), new Point(0,1)));
        assertEquals(lines[0] +"\n" + lines[1] + "\n", textStorage.getText(textStorage.beginningOfText(), new Point(0,2)));
        assertEquals(lines[0].substring(1) + "\n", textStorage.getText(new Point(1,0), new Point(0,1)));
        assertEquals(lines[1].substring(1) + "\n", textStorage.getText(new Point(1,1), new Point(0, 2)));
        assertEquals(lines[1].substring(1) + "\n" + lines[2].substring(0,1), textStorage.getText(new Point(1,1), new Point(1, 2)));
    }

    private void pasteText(Point coords, String addedText) {
        int splitIndex = splitIndex(coords);
        text = text.substring(0, splitIndex) + addedText + text.substring(splitIndex);
        anotherIndex.addText(splitIndex, addedText);
        index.addText(textStorage.offsetFromCoords(coords), addedText);
        textStorage.addText(coords, addedText);
    }

    private void deleteText(Point coords, int length) {
        int splitIndex = splitIndex(coords);
        text = text.substring(0, splitIndex) + text.substring(splitIndex + length);
        anotherIndex.removeText(splitIndex, length);
        index.removeText(textStorage.offsetFromCoords(coords), length);
        textStorage.removeText(coords, length);
    }

    private int splitIndex(Point point) {
        List<Integer> newlineIndexes = new ArrayList<>();
        newlineIndexes.add(0);
        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) == '\n')
                newlineIndexes.add(i + 1);
        return newlineIndexes.get(point.y) + point.x;
    }

    private void assertStateIsCorrect() {
        assertStateIsCorrect("");
    }

    private void assertStateIsCorrect(String added) {
        String description = added.replace("\n", "\\n");
        assertEquals(description, text, textStorage.getText());
        assertEquals(description, text, index.getState().snd.getText());
        assertEquals(description, text, anotherIndex.getState().snd.getText());
    }
}
