package syntax.antlr.iterators;

import gui.state.ColoredString;
import org.apache.commons.collections4.list.TreeList;

import java.awt.*;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by avyatkin on 28/04/16.
 */
//    can easily become inconsistent, so is intended for quick usage
public class ColoredStringNavigator {
    ListIterator<List<ColoredString>> linesIterator;
    ListIterator<ColoredString> currentLineIterator;
    List<List<ColoredString>> coloredLines;
    LastIteratorAction lastLineIterAction;

    public ColoredStringNavigator(TreeList<List<ColoredString>> coloredLines) {
        if (coloredLines.isEmpty()) coloredLines.add(new TreeList<>());
        this.linesIterator = coloredLines.listIterator();
        this.coloredLines = coloredLines;
        lastLineIterAction = LastIteratorAction.NOTHING;
        nextLine();
    }

    public boolean hasNext() {
        boolean result = currentLineIterator.hasNext() || linesIterator.hasNext();
        if (lastLineIterAction == LastIteratorAction.PREVIOUS) {
            linesIterator.next();
            result = currentLineIterator.hasNext() || linesIterator.hasNext();
            linesIterator.previous();
        }
        return result;
    }

    public ColoredString next() {
        if (currentLineIterator.hasNext())
            return currentLineIterator.next();
        else if (hasNext()) {
            nextLine();
            return currentLineIterator.next(); //always at least one colored string on line
        }
        else throw new RuntimeException("No next element!");
    }

    public boolean hasPrevious() {
        boolean result = currentLineIterator.hasPrevious() || linesIterator.hasPrevious();
        if (lastLineIterAction == LastIteratorAction.NEXT) {
            linesIterator.previous();
            result = currentLineIterator.hasPrevious() || linesIterator.hasPrevious();
            linesIterator.next();
        }
        return result;
    }

    public ColoredString previous() {
        if (currentLineIterator.hasPrevious())
            return currentLineIterator.previous();
        else if (hasPrevious()) {
            previousLine();
            return currentLineIterator.previous(); //always at least one colored string on line
        }
        else throw new RuntimeException("No next element!");
    }

    void nextLine() {
        if (lastLineIterAction == LastIteratorAction.PREVIOUS) linesIterator.next();
        currentLineIterator = linesIterator.next().listIterator();
        lastLineIterAction = LastIteratorAction.NEXT;
    }

    void previousLine() {
        if (lastLineIterAction == LastIteratorAction.NEXT) linesIterator.previous();
        List<ColoredString> previousLine = linesIterator.previous();
        currentLineIterator = previousLine.listIterator(previousLine.size());
        lastLineIterAction = LastIteratorAction.PREVIOUS;
    }

    public int beforePoint(Point p) {
        goToLine(p.y);
        int offset = 0;
        while (offset <= p.x && currentLineIterator.hasNext())
            offset += currentLineIterator.next().getSize();
        if (currentLineIterator.hasPrevious()) {
            ColoredString previousString = currentLineIterator.previous();
            offset -= previousString.getSize();
        }
        return p.x - offset;
    }

    private void goToLine(int line) {
        linesIterator = coloredLines.listIterator(line);
        nextLine();
    }

    public ColoredString peekNext() {
        next();
        return previous();
    }
}