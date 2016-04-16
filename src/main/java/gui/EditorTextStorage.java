package gui;

import org.apache.commons.collections4.list.TreeList;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by avyatkin on 16/04/16.
 * stores text
 * all operations are done in relative coords (number of char in line/number of line)
 */
public class EditorTextStorage {
    List<String> lines;

    public EditorTextStorage() {
        lines = new TreeList<>();
        lines.add("");
    }

    public String getText() { return String.join("\n", lines); }

    public void setText(String text) { this.lines = buildLinesList(text); }

//    TODO: use 2d int rectangle to return displayed area
    List<String> getLines() { return lines; }

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
    }

    public void removeText(Point position, int length) {
        ListIterator<String> iter = lines.listIterator(position.y);
        StringBuilder affectedString = new StringBuilder();
        if (iter.hasNext()) {
            affectedString.append(iter.next());
            iter.remove();
            while (iter.hasNext() && position.x + length > affectedString.length()) {
                affectedString.append("\n").append(iter.next());
                iter.remove();
            }
        }
        String old = affectedString.toString();
        String updated = old.substring(0, position.x) + old.substring(position.x + length);
        buildLinesList(updated).forEach(iter::add);
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

    public Point horizontalMove(Point position, int direction) {
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

    public Point closestRealCoord(Point point) {
        if (point.y < 0) return beginningOfText();
        else if (point.y >= lines.size()) return endOfText();
        else return new Point(Math.min(point.x, lines.get(point.y).length()), point.y);
    }

//    correct string-split
    private static List<String> buildLinesList(String str) {
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
