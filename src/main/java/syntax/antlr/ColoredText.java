package syntax.antlr;

import com.sun.tools.javac.util.Pair;
import gui.state.ColoredString;
import gui.state.EditorTextStorage;
import org.apache.commons.collections4.list.TreeList;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.IntStream;

/**
 * Created by avyatkin on 01/05/16.
 */
public class ColoredText {
    public String getText() {
        return text;
    }

    public String getLine(int n) {
        return lines.get(n);
    }

    public Dimension getRelativeSize() {
        return relativeSize;
    }

    String text;
    List<String> lines;
    Dimension relativeSize;

    private void rebuildText() {
        StringBuilder textBuilder = new StringBuilder();
        for (List<ColoredString> strs : coloredLines)
            for (ColoredString str: strs)
                textBuilder.append(str.getContent());
        text = textBuilder.toString();
        lines = EditorTextStorage.buildLinesList(text, true);
        int maxWidth = 0;
        for(String str: lines)
            if (str.length() > maxWidth)
                maxWidth = str.length();
        relativeSize = new Dimension(maxWidth ,linesCount() + 1);
    }

    public List<ColoredString> getColoredLine(int n) {
        return coloredLines.get(n);
    }

    public List<List<ColoredString>> getColoredLines() {
        return coloredLines;
    }

    List<List<ColoredString>> coloredLines;

    public ColoredText(List<List<ColoredString>> coloredStrings) {
        this.coloredLines = coloredStrings;
        rebuildText();
    }

    public void addText(Point position, String text) {
        List<ColoredString> newLines = ColoredString.stubStrings(text);
        Pair<ListIterator<List<ColoredString>>, ListIterator<ColoredString>> iters = splitAtCoords(position);
        ListIterator<List<ColoredString>> linesIter = iters.fst;
        ListIterator<ColoredString> lineIter = iters.snd;

        if (newLines.size() == 1)
            lineIter.add(newLines.get(0));
        else {
            int lastIndex = newLines.size() - 1;
            ColoredString first = newLines.get(0);
            ColoredString last = newLines.get(lastIndex);
            lineIter.add(first);
            List<ColoredString> remaining = new TreeList<>();
            remaining.add(last);
            while (lineIter.hasNext()) {
                remaining.add(lineIter.next());
                lineIter.remove();
            }
            newLines.subList(1, lastIndex).forEach(line -> linesIter.add(new TreeList<>(Arrays.asList(line))));
            linesIter.add(remaining);
        }
        rebuildText();
    }

    public void removeText(Point start, Point end) {
        ListIterator<ColoredString> firstLineIter = splitAtCoords(start).snd;
        List<ColoredString> updatedLine = new TreeList<>();
        while (firstLineIter.hasPrevious()) updatedLine.add(firstLineIter.previous());
        Collections.reverse(updatedLine);

        Pair<ListIterator<List<ColoredString>>, ListIterator<ColoredString>> iters = splitAtCoords(end);
        ListIterator<ColoredString> lastLineIter = iters.snd;
        while (lastLineIter.hasNext()) updatedLine.add(lastLineIter.next());

        ListIterator<List<ColoredString>> linesIterator = iters.fst;
        IntStream.rangeClosed(start.y, end.y).forEach(i -> { linesIterator.previous(); linesIterator.remove(); });
        iters.fst.add(updatedLine);
        rebuildText();
    }

    public String getText(Point start, Point end) {
        StringBuilder result = new StringBuilder();
        if (start.y == end.y)
            result.append(getLine(start.y).substring(start.x, end.x));
        else {
            result.append(getLine(start.y).substring(start.x));
            for (int i = start.y + 1; i < end.y; i++) result.append(getLine(i));
            result.append(getLine(end.y).substring(0, end.x));
        }
        return result.toString();
    }

    private Pair<ListIterator<List<ColoredString>>, ListIterator<ColoredString>> splitAtCoords(Point coords) {
        ListIterator<List<ColoredString>> linesIter = coloredLines.listIterator(coords.y);
        if (linesIter.hasNext()) {
            List<ColoredString> updatedLine = linesIter.next();
            ListIterator<ColoredString> updatedLineIter = updatedLine.listIterator();
            if (!updatedLineIter.hasNext()) return new Pair<>(linesIter, updatedLineIter);

            int offset = 0;
            ColoredString current = updatedLineIter.next();

            while (updatedLineIter.hasNext() && current.getContent().length() + offset < coords.x) {
                offset += current.getContent().length();
                current = updatedLineIter.next();
            }

            int splitCoords = coords.x - offset;
            if (current.getContent().length() <= splitCoords)
                return new Pair<>(linesIter, updatedLineIter);

            updatedLineIter.remove();
            for (ColoredString str: current.splitAt(splitCoords))
                updatedLineIter.add(str);
            updatedLineIter.previous();
            return new Pair<>(linesIter, updatedLineIter);
        } else {
            List<ColoredString> newLine = new TreeList<>();
            linesIter.add(newLine);
            return new Pair<>(linesIter, newLine.listIterator());
        }
    }

    public int lastLineIndex() { return coloredLines.size() - 1; }

    public int linesCount() { return coloredLines.size(); }
}
