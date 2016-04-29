package syntax.antlr.iterators;

import gui.state.ColoredString;
import com.sun.tools.javac.util.Pair;
import org.apache.commons.collections4.list.TreeList;
import syntax.antlr.Lexeme;
import syntax.document.SyntaxColoring;

import java.awt.*;
import java.util.*;
import java.util.List;

import static syntax.EditorUtil.*;

/**
 * Created by avyatkin on 28/04/16.
 */
public class LexemesIterator implements ListIterator<Lexeme> {
    ColoredStringNavigator navigator;
    Optional<Lexeme> lastReturned;
    LastIteratorAction lastAction;
    SyntaxColoring coloring;

    public LexemesIterator(TreeList<List<ColoredString>> coloredLines, SyntaxColoring syntx) {
        coloring = syntx;
        lastAction = LastIteratorAction.NOTHING;
        navigator = new ColoredStringNavigator(coloredLines);
    }

//    public static Pair<LexemesIterator, Integer> beforeFirstAffectedLexeme(
//            List<List<ColoredString>> coloredLines,
//            Point affectedLexemePoint,
//            SyntaxColoring syntx) {
//        LexemesIterator iter = new LexemesIterator(coloredLines, syntx);
//        int offset = iter.getBeforeAffectedLexeme(affectedLexemePoint);
//        return new Pair<>(iter, offset);
//    }

    public int getBeforeAffectedLexeme(Point affectedLexemePoint) {
        int initialOffset = navigator.beforePoint(affectedLexemePoint);

        if (navigator.hasNext()) {
            ColoredString currentString = navigator.peekNext();
            while (currentString.getIndexInLexeme() > 0) {
                currentString = navigator.previous();
                initialOffset += currentString.getSize();
            }
        }
        return initialOffset;
    }

    @Override
    public boolean hasNext() {
        return navigator.hasNext();
    }

    @Override
    public Lexeme next() {
        java.util.List<ColoredString> lexemeParts = new ArrayList<>();
        if (navigator.hasNext()) {
            lastAction = LastIteratorAction.NEXT;
            ColoredString current = navigator.next();
            lexemeParts.add(current);
            if (current.getIndexInLexeme() != 0) throw new RuntimeException("Inconsistent state");

            while (navigator.hasNext()) {
                ColoredString next = navigator.next();
                if (current.getIndexInLexeme() + 1 != next.getIndexInLexeme()) {
                    navigator.previous();
                    break;
                } else {
                    lexemeParts.add(next);
                    current = next;
                }
            }
            return lexemeFromStrings(lexemeParts);
        } else throw new RuntimeException("No next element!");
    }

    @Override
    public boolean hasPrevious() {
        return navigator.hasPrevious();
    }

    @Override
    public Lexeme previous() {
        java.util.List<ColoredString> lexemeParts = new ArrayList<>();
        if (navigator.hasPrevious()) {
            lastAction = LastIteratorAction.PREVIOUS;
            ColoredString current = navigator.previous();
            lexemeParts.add(current);
            while (navigator.hasPrevious() && current.getIndexInLexeme() != 0) {
                current = navigator.previous();
                lexemeParts.add(current);
            }
            Collections.reverse(lexemeParts);
            return lexemeFromStrings(lexemeParts);
        } else throw new RuntimeException("No previous element!");
    }

    @Override
    public int nextIndex() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public int previousIndex() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void remove() {
        if (lastAction == LastIteratorAction.PREVIOUS) {
            Point start = new Point(navigator.linesIterator.nextIndex(), navigator.currentLineIterator.nextIndex());
            ColoredString currentLexeme = navigator.next();
            while (navigator.hasNext() &&
                    navigator.peekNext().getIndexInLexeme() == currentLexeme.getIndexInLexeme() + 1)
                currentLexeme = navigator.next();
            Point end = new Point(navigator.linesIterator.nextIndex(), navigator.currentLineIterator.nextIndex());

        } else if (lastAction == LastIteratorAction.NEXT) {
            navigator.currentLineIterator.next();
        } else throw new RuntimeException("Cannot remove element, because removal direction is unknown.");

        lastAction = LastIteratorAction.NOTHING;
    }

    @Override
    public void set(Lexeme lexeme) {
        throw new RuntimeException("Not implemented!");
    }

//    TODO: move to navigator
    @Override
    public void add(Lexeme lexeme) {
        List<ColoredString> colored = coloring.splitInColoredLines(lexeme);

        if (colored.size() == 1) {
            navigator.currentLineIterator.add(colored.get(0));
        } else {
            List<ColoredString> movedDown = new TreeList<>();
            while (navigator.currentLineIterator.hasNext()) {
                movedDown.add(navigator.currentLineIterator.next());
                navigator.currentLineIterator.remove();
            }

            TreeList<ColoredString> newLine = new TreeList<>();
            int index = 0;
            for (ColoredString line : colored) {
                navigator.currentLineIterator.add(line);
                if (index != colored.size() - 1) {
                    navigator.linesIterator.add(newLine);
                    navigator.currentLineIterator = newLine.listIterator();
                    newLine = new TreeList<>();
                }
                index++;
            }
            movedDown.forEach(navigator.currentLineIterator::add);
        }
    }

    private Lexeme lexemeFromStrings(List<ColoredString> lexemeParts) {
        String lexemeText = join(lexemeParts, ColoredString::getContent, "");
        String type = lexemeParts.get(0).getType();
        return new Lexeme(type, lexemeText);
    }

    public LexemesIterator copy() {
        throw new RuntimeException("not implemented, but should be");
    }
}