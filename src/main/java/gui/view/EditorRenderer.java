package gui.view;

import gui.state.ColoredString;
import gui.state.view.CaretState;
import gui.state.view.SelectionState;
import gui.state.view.StateView;
import syntax.brackets.BracketHighlighting;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by avyatkin on 22/04/16.
 */
public class EditorRenderer {
    StateView state;
    StateView oldState;

    TextCoordUtils utils;

    public void setState(StateView state) {
        oldState = this.state;
        this.state = state;
    }

    public CaretRenderer getCaretRenderer() {
        return caretRenderer;
    }

    CaretRenderer caretRenderer;

    public EditorRenderer(StateView state1, TextCoordUtils params1) {
        state = state1;
        utils = params1;
        caretRenderer = new CaretRenderer();
    }

    private List<Rectangle> selectionInBounds(Rectangle bounds) {
        List<Rectangle> result = new ArrayList<>();
        Dimension size = getPreferredSize();
        SelectionState selection = state.getSelectionState();
        if (selection.isSelectionEmpty()) return result;

        if (selection.getStart().y == selection.getEnd().y) {
            Rectangle selectionRect = utils.absoluteRectangle(selection.getStart(), selection.getEnd().x - selection.getStart().x, 1);
            result.add(selectionRect);
        } else {
            result.add(utils.absoluteRectangle(selection.getStart(), size.width, 1));
            for (int i = selection.getStart().y + 1; i < selection.getEnd().y; i++)
                result.add(utils.absoluteRectangle(new Rectangle(0, i, size.width, 1)));
            result.add(utils.absoluteRectangle(new Rectangle(0, selection.getEnd().y, selection.getEnd().x, 1)));
        }
        return result.stream().filter(bounds::intersects).map(bounds::intersection).collect(Collectors.toList());
    }

    //    TODO: intersect updated area with relative rectangle and render diff
    public void paintState(Graphics g) {
        g.setFont(TextCoordUtils.FONT);

        Rectangle clip = g.getClipBounds();
        fillRectWithColor(g, clip, EditorColors.BACKGROUND);

        if (!state.isAvailable()) {
            drawStringWithColor(g, "Loading, please wait...", new Point(0, 0), EditorColors.COMMENT);
            return;
        }

        selectionInBounds(clip).forEach(rect -> fillRectWithColor(g, rect, EditorColors.SELECTION));


//        Rectangle bounds = utils.relativeRectangle(clip);
//        TODO: render exact sublist of lines instead of iterating over whole list(faster and cleaner)
//        yOffset = bounds.y * utils.fontHeight();
//        int yIndex = bounds.y;
//        List<String> lines = state.getTextStorage().getLines().subList(bounds.y, bounds.y + bounds.height);


        for (Map.Entry<Integer, List<ColoredString>> lineWithIndex : state.getDisplayedLines().entrySet()) {
            int offset = 0;
            int yCoord = lineWithIndex.getKey();
            List<ColoredString> coloredLines = lineWithIndex.getValue();
            for (ColoredString str : coloredLines) {
                drawStringWithColor(g, str.getContent(), new Point(offset, yCoord), str.getColor());
                offset += str.getContent().length();
            }
        }
        caretRenderer.renderCaret(g);
    }

    public Dimension getPreferredSize() {
        //        TODO compute incrementally
        return utils.absoluteDimension(state.getRelativeSize());
    }

    @Deprecated
    public class CaretRenderer {
        public CaretState caret() {
            return state.getCaretState();
        }

        static final int CARET_WIDTH = 2;

        public Rectangle caretRect() {
            Rectangle result = utils.absoluteTile(caret().getCoords());
            if (!caret().isInInsertMode()) result.width = CARET_WIDTH;
            return result;
        }

        public void renderCaret(Graphics g) {
            renderBracketHighlighting(g);

            if (caret().isInInsertMode()) {
                renderCharOnBackground(g, caret().getCoords(), EditorColors.TEXT_OVER_INSERT_CARET, EditorColors.INSERT_CARET);
            } else {
                if (!caret().isShouldBeRendered()) return;
                fillRectWithColor(g, caretRect(), EditorColors.CARET);
            }

        }

        private void renderBracketHighlighting(Graphics g) {
            BracketHighlighting highlighting = state.getBracketHighlighting();
            highlighting.getBrokenBraces().forEach(coords ->
                    renderCharOnBackground(g, coords, EditorColors.TEXT_OVER_BROKEN_BRACKET, EditorColors.BRACKET_BACKGROUND));
            highlighting.getWorkingBraces().forEach(coords ->
                    renderCharOnBackground(g, coords, EditorColors.TEXT_OVER_WORKING_BRACKET, EditorColors.BRACKET_BACKGROUND));
        }
    }

    private void renderCharOnBackground(Graphics g, Point relativeCharCoords, Color color, Color background) {
        fillRectWithColor(g, utils.absoluteTile(relativeCharCoords), background);

        if (g.getClipBounds().intersects(utils.absoluteTile(relativeCharCoords))) return;

        Point textEnd = new Point(relativeCharCoords.x + 1, relativeCharCoords.y);
        String text = state.getText(relativeCharCoords, textEnd);
        drawStringWithColor(g, text, relativeCharCoords, color);
    }

    private void fillRectWithColor(Graphics g, Rectangle rect, Color clr) {
        g.setColor(clr);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    private void drawStringWithColor(Graphics g, String str, Point relativeCoords, Color color) {
        g.setFont(TextCoordUtils.FONT);
        g.setColor(color);
        Point absoluteCoords = utils.absoluteTextCoords(relativeCoords);
        g.drawString(str, absoluteCoords.x, absoluteCoords.y);
    }
}
