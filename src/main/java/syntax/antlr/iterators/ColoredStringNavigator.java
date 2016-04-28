package syntax.antlr.iterators;

import gui.state.ColoredString;
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

    public ColoredStringNavigator(List<List<ColoredString>> coloredLines) {
        this.linesIterator = coloredLines.listIterator();
        this.coloredLines = coloredLines;
        lastLineIterAction = LastIteratorAction.NOTHING;
        nextLine();
    }

    public boolean hasNext() {
        return currentLineIterator.hasNext() || linesIterator.hasNext();
    }

    public ColoredString next() {
        if (currentLineIterator.hasNext())
            return currentLineIterator.next();
        else if (hasNext()) {
            nextLine();
            return currentLineIterator.next(); //always at least one coloredstring on line
        }
        else throw new RuntimeException("No next element!");
    }

    public boolean hasPrevious() {
        return currentLineIterator.hasPrevious() || linesIterator.hasPrevious();
    }

    public ColoredString previous() {
        if (currentLineIterator.hasPrevious())
            return currentLineIterator.previous();
        else if (hasPrevious()) {
            previousLine();
            return currentLineIterator.previous(); //always at least one coloredstring on line
        }
        else throw new RuntimeException("No next element!");
    }

    private void nextLine() {
        if (lastLineIterAction == LastIteratorAction.PREVIOUS) linesIterator.next();
        currentLineIterator = linesIterator.next().listIterator();
        lastLineIterAction = LastIteratorAction.NEXT;
    }

    private void previousLine() {
        if (lastLineIterAction == LastIteratorAction.NEXT) linesIterator.previous();
        List<ColoredString> previousLine = linesIterator.previous();
        currentLineIterator = previousLine.listIterator(previousLine.size());
        lastLineIterAction = LastIteratorAction.PREVIOUS;
    }

    public void beforePoint(Point p) {
        goToLine(p.y);
        int offset = 0;
        while (offset <= p.x && currentLineIterator.hasNext())
            offset += currentLineIterator.next().getSize();
        if (currentLineIterator.hasPrevious())
            currentLineIterator.previous();
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