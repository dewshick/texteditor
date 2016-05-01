package gui.state;

import gui.view.EditorColors;
import org.apache.commons.collections4.list.TreeList;
import syntax.antlr.ColoredText;
import syntax.antlr.LexemeIndex;
import syntax.brackets.BracketHighlighting;
import syntax.document.SupportedSyntax;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.IntStream;

/**
 * Created by avyatkin on 16/04/16.
 * stores text
 * all operations are done in relative coords (number of char in line/number of line)
 */
public class EditorTextStorage {
//    List<String> lines;
    ColoredText lines;

    LexemeIndex index;

    public SupportedSyntax getSyntax() {
        return syntax;
    }

    SupportedSyntax syntax;

    public EditorTextStorage(SupportedSyntax syntax) {
        lines = new ColoredText();
        lines.addText(new Point(0, 0), "");
        index = new LexemeIndex(syntax, "");
        this.syntax = syntax;
    }

    public String getText() { return lines.getText(); }

    public void setText(String text) {
        index = new LexemeIndex(syntax, text);
        this.lines.setColoredLines(index.getColoredLines());
    }

    public BracketHighlighting getBracketHighlighting(Point caret) {
        return index.getHighlighting(caret);
    }

//    TODO: use 2d int rectangle to return displayed area
//    TODO: should be package-local


    public List<ColoredString> getColoredLine(int line) {
//        return index.getColoredLine(line);
        return lines.getColoredLine(line);
    }

    /**
     * Edit text
     */

    public void addText(Point position, String text) {
        lines.addText(position, text);
    }

    public void removeText(Point position, int length) {
        removeText(position, horizontalMove(position, length));
    }

    private void removeText(Point start, Point end) {
        lines.removeText(start, end);
    }

    public void removeText(EditorState.Selection selection) {
        Point start = selection.startPoint();
        Point end = selection.endPoint();
        removeText(start, end);
    }

//    iterator code is almost the same for all the strings so maybe there's way to reuse it?
//    to avoid complex testing/rewriting all the time
    public String getText(Point start, Point end) {
        return lines.getText(start, end);
    }

    /**
     * Various operations related to relative coords
     */

    public int lastLineIndex() { return lines.lastLineIndex(); }

    public Point beginningOfText() { return new Point(0,0); }

//    TODO remove this weird method and fix logic
    public int lineLength(int n) {
        String lastLine = lines.getLine(n);
        int length = lastLine.length();
        if (lastLine.endsWith("\n")) length -= 1;
        return length;
    }

    public Point endOfText() { return new Point(lineLength(lastLineIndex()), lastLineIndex()); }

    public Dimension relativeSize() {
        int width = IntStream.rangeClosed(0, lines.lastLineIndex()).map(this::lineLength).reduce(0, Integer::max);
        return new Dimension(width, lines.linesCount());
    }

    public Point verticalMove(Point position, int direction) {
        int newY = Math.min(Math.max(position.y + direction, 0), lastLineIndex());
        if (newY == position.y) return position;
        int newX = Math.min(lineLength(newY), position.x);
        return new Point(newX, newY);
    }

//    TODO: this can be easily optimized
    public Point horizontalMove(Point position, int distance) {
        int direction = distance > 0 ? 1 : -1;
        int length = Math.abs(distance);
        for (;length > 0;length--) position = horizontalStep(position, direction);
        return position;
    }

//    moves only on 1 position
    private Point horizontalStep(Point position, int direction) {
        int currentLineLength = lineLength(position.y);
        int newY = position.y;
        int newX = position.x + direction;

        if (newX > currentLineLength) {
            if (newY >= lastLineIndex()) return position;
            else return new Point(0, position.y + 1);
        } else if (newX < 0) {
            if (newY == 0) return position;
            else {
                newY--;
                return new Point(lineLength(newY), newY);
            }
        }
        return new Point(newX, newY);
    }

    public Point closestCaretPosition(Point point) {
        if (point.y < 0) return beginningOfText();
        else if (point.y >= lines.linesCount()) return endOfText();
        else return new Point(Math.min(point.x, lineLength(point.y)), point.y);
    }

//    correct string-split
    public static List<String> buildLinesList(String str, boolean keepNewlines) {
        int initialIndex = 0;
        List<String> result = new ArrayList<>();
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '\n') {
                result.add(str.substring(initialIndex, keepNewlines ? i+1 : i));
                initialIndex = i+1;
            }
        result.add(str.substring(initialIndex));
        return result;
    }

    public void changeSyntax(SupportedSyntax syntax) {
        index = new LexemeIndex(syntax, getText());
        this.syntax = syntax;
    }
}
