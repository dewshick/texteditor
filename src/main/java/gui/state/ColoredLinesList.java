package gui.state;


import com.sun.tools.javac.util.Pair;
import org.apache.commons.collections4.list.TreeList;
import syntax.antlr.Lexeme;
import syntax.antlr.iterators.LexemesIterator;
import syntax.antlr.iterators.ListIteratorWithOffset;
import syntax.document.SyntaxColoring;

import java.awt.*;
import java.util.List;

/**
* Created by avyatkin on 25/04/16.
*/
public class ColoredLinesList {
    public TreeList<List<ColoredString>> getColoredLines() {
        return coloredLines;
    }

    TreeList<List<ColoredString>> coloredLines;
    SyntaxColoring coloring;
    LexemesIterator addingIterator;

    public ColoredLinesList(SyntaxColoring coloring) {
        coloredLines = new TreeList<>();
        this.coloring = coloring;
        addingIterator = new LexemesIterator(coloredLines, coloring);
    }

    public void add(Lexeme l) {
        addingIterator.add(l);
    }

    public boolean isEmpty() {
        return coloredLines.isEmpty();
    }

    public List<ColoredString> getColoredLine(int line) {
        return coloredLines.get(line);
    }

    public Pair<Integer, ListIteratorWithOffset> beforeFirstAffectedLexeme(Point changesPoint) {
        LexemesIterator iter = new LexemesIterator(coloredLines, coloring);
        int offset = iter.getBeforeAffectedLexeme(changesPoint);
        return new Pair<>(offset, new ListIteratorWithOffset(iter));
    }

    public LexemesIterator lexemesIterator() {
        return new LexemesIterator(coloredLines, coloring);
    }
}
