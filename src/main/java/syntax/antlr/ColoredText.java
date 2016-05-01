package syntax.antlr;

import com.sun.tools.javac.util.Pair;
import gui.state.ColoredString;
import org.apache.commons.collections4.list.TreeList;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by avyatkin on 01/05/16.
 */
public class ColoredText {
    public void setColoredLines(List<List<ColoredString>> coloredLines) {
        this.coloredLines = coloredLines;
    }

//    TODO: cache value
    public String getText() {
        return coloredLines.stream().
                map(strings -> String.join("", strings.stream().map(ColoredString::getContent).
                        collect(Collectors.toList()))).
                collect(Collectors.joining());
    }

//    TODO: cache value
    public String getLine(int n) {
        return coloredLines.get(n).stream().map(ColoredString::getContent).collect(Collectors.joining());
    }

    public List<ColoredString> getColoredLine(int n) {
        return coloredLines.get(n);
    }

    public List<List<ColoredString>> getColoredLines() {
        return coloredLines;
    }

    List<List<ColoredString>> coloredLines;

    public ColoredText() {
        coloredLines = new TreeList<>(new TreeList<>());
    }

    public ColoredText(List<List<ColoredString>> coloredStrings) {
        this.coloredLines = coloredStrings;
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
            remaining.subList(1, lastIndex).forEach(line -> linesIter.add(new TreeList<>(Arrays.asList())));
            linesIter.add(remaining);
        }
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
