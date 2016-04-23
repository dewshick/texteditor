package gui.state;

import gui.view.EditorColors;
import org.apache.commons.collections4.list.TreeList;
import syntax.antlr.LexemeIndex;
import syntax.document.SupportedSyntax;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by avyatkin on 16/04/16.
 * stores text
 * all operations are done in relative coords (number of char in line/number of line)
 */
public class EditorTextStorage {
    List<String> lines;

    LexemeIndex index;
    SupportedSyntax syntax;


    public EditorTextStorage(SupportedSyntax syntax) {
        lines = new TreeList<>();
        lines.add("");
        index = new LexemeIndex(syntax, "");
        this.syntax = syntax;
    }

    public String getText() { return String.join("\n", lines); }

    public void setText(String text) {
        this.lines = buildLinesList(text);
        index = new LexemeIndex(syntax, text);
    }

//    TODO: use 2d int rectangle to return displayed area
//    TODO: should be package-local
    public List<String> getLines() { return lines; }

    public List<ColoredString> getColoredLine(int line) {
        return index.getColoredLine(line);
    }

    /**
     * Edit text
     */

    public void addText(Point position, String text) {
        ListIterator<String> iter = lines.listIterator(position.y);
        List<String> newLines;
        if (iter.hasNext()) {
            String updatedLine = iter.next();
            newLines = buildLinesList(updatedLine.substring(0, position.x) + text + updatedLine.substring(position.x));
            iter.remove();
        } else
            newLines = buildLinesList(text);
        newLines.forEach(iter::add);

        index.addText(getText(beginningOfText(), position).length(), text);
    }

    public void removeText(Point position, int length) {
        removeText(position, horizontalMove(position, length));
    }

    private void removeText(Point start, Point end) {
        index.removeText(getText(beginningOfText(), start).length(), getText(start, end).length());
        String updated = lines.get(start.y).substring(0, start.x) + lines.get(end.y).substring(end.x);
        ListIterator<String> iter = lines.listIterator(start.y);
        for (int i = start.y; i <= end.y; i++) {
            iter.next();
            iter.remove();
        }
        iter.add(updated);
    }

    public void removeText(EditorState.Selection selection) {
        Point start = selection.startPoint();
        Point end = selection.endPoint();
        removeText(start, end);
    }

//    iterator code is almost the same for all the strings so maybe there's way to reuse it?
//    to avoid complex testing/rewriting all the time
    public String getText(Point start, Point end) {
        StringBuilder result = new StringBuilder();
        if (start.y == end.y)
            result.append(lines.get(start.y).substring(start.x, end.x));
        else {
            result.append(lines.get(start.y).substring(start.x));
            for (int i = start.y + 1; i < end.y; i++) {
                result.append('\n');
                result.append(lines.get(i));
            }
            result.append("\n" + lines.get(end.y).substring(0, end.x));
        }
        return result.toString();
    }

    /**
     * Various operations related to relative coords
     */

    public int lastLineIndex() { return lines.size() - 1; }

    public Point beginningOfText() { return new Point(0,0); }

    public Point endOfText() { return new Point(lines.get(lastLineIndex()).length(), lastLineIndex()); }

    public Dimension relativeSize() {
        return new Dimension(lines.stream().map(String::length).reduce(0, Integer::max), lines.size());
    }

    public Point verticalMove(Point position, int direction) {
        int newY = Math.min(Math.max(position.y + direction, 0), lastLineIndex());
        if (newY == position.y) return position;
        int newX = Math.min(lines.get(newY).length(), position.x);
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
        String currentLine = lines.get(position.y);
        int newY = position.y;
        int newX = position.x + direction;

        if (newX > currentLine.length()) {
            if (newY >= lastLineIndex()) return position;
            else return new Point(0, position.y + 1);
        } else if (newX < 0) {
            if (newY == 0) return position;
            else {
                newY--;
                return new Point(lines.get(newY).length(), newY);
            }
        }
        return new Point(newX, newY);
    }

    public Point closestCaretPosition(Point point) {
        if (point.y < 0) return beginningOfText();
        else if (point.y >= lines.size()) return endOfText();
        else return new Point(Math.min(point.x, lines.get(point.y).length()), point.y);
    }

//    correct string-split
    public static List<String> buildLinesList(String str) {
        int initialIndex = 0;
        List<String> result = new ArrayList<>();
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '\n') {
                result.add(str.substring(initialIndex, i));
                initialIndex = i+1;
            }
        result.add(str.substring(initialIndex));
        return result;
    }
}
