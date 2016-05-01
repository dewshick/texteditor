package gui.state.view;


import com.sun.tools.javac.util.Pair;
import gui.state.ColoredString;
import syntax.brackets.BracketHighlighting;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Created by avyatkin on 01/05/16.
 */
public class StateView {
    private Dimension relativeSize;




    public StateView(CaretState caretState,
                     Map<Integer,List<ColoredString>> displayedLines,
                     SelectionState selection,
                     boolean available,
                     BracketHighlighting bracketHighlighting,
                     Dimension relativeS) {
        this.caretState = caretState;
        this.displayedLines = displayedLines;
        selectionState = selection;
        this.available = available;
        relativeSize = relativeS;
        this.bracketHighlighting = bracketHighlighting;
    }

    public CaretState getCaretState() {
        return caretState;
    }

    public Map<Integer,List<ColoredString>> getDisplayedLines() {
        return displayedLines;
    }

    public Dimension getRelativeSize() {
        return relativeSize;
    }

    private CaretState caretState;

    public SelectionState getSelectionState() {
        return selectionState;
    }

    private SelectionState selectionState;

    private Map<Integer,List<ColoredString>> displayedLines;


    public BracketHighlighting getBracketHighlighting() {
        return bracketHighlighting;
    }

    private BracketHighlighting bracketHighlighting;

    public boolean isAvailable() {
        return available;
    }

    boolean available;

    public String getText(Point relativeCharCoords, Point textEnd) {
        return "";
    }
}
