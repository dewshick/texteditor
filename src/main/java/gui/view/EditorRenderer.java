package gui.view;

import gui.state.ColoredString;
import gui.state.EditorState;
import gui.state.EditorTextStorage;
import syntax.brackets.BracketHighlighting;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by avyatkin on 22/04/16.
 */
public class EditorRenderer {
    EditorState state;
    TextCoordUtils utils;

    public CaretRenderer getCaretRenderer() {
        return caretRenderer;
    }

    CaretRenderer caretRenderer;

    public EditorRenderer(EditorState state1, TextCoordUtils params1) {
        state = state1;
        utils = params1;
        caretRenderer = new CaretRenderer();
    }

//    TODO: do we need to pass rectangle?
    private List<Rectangle> selectionInBounds(Rectangle bounds) {
        List<Rectangle> result = new ArrayList<>();
        Dimension size = getPreferredSize();
        EditorState.Selection selection = state.getSelection();

        if (selection.isEmpty()) return result;
        if (selection.startPoint().y == selection.endPoint().y) {
            result.add(utils.absoluteRectangle(new Rectangle(selection.startPoint().x, selection.startPoint().y, selection.endPoint().x - selection.startPoint().x, 1)));
        } else {
            result.add(utils.absoluteRectangle(new Rectangle(selection.startPoint().x, selection.startPoint().y, size.width, 1)));
            for (int i = selection.startPoint().y + 1; i < selection.endPoint().y; i++)
                result.add(utils.absoluteRectangle(new Rectangle(0, i, size.width, 1)));
            result.add(utils.absoluteRectangle(new Rectangle(0, selection.endPoint().y, selection.endPoint().x, 1)));
        }
        return result;
    }

    //    TODO: render all this in separate specified class
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

        int yOffset = utils.fontHeight();
        int yCoord = 0;

//        Rectangle bounds = utils.relativeRectangle(clip);
//        TODO: render exact sublist of lines instead of iterating over whole list(faster and cleaner)
//        yOffset = bounds.y * utils.fontHeight();
//        int yIndex = bounds.y;
//        List<String> lines = state.getTextStorage().getLines().subList(bounds.y, bounds.y + bounds.height);

        for (String line : state.getTextStorage().getLines()) {
            if (yOffset >= clip.y) { //g.drawString(utils.stringInRelativeBounds(line, clip), xOffset, yOffset);
                int offset = 0;
                for(ColoredString str :  state.getTextStorage().getColoredLine(yCoord)) {
                    drawStringWithColor(g, str.getContent(), new Point(offset, yCoord), str.getColor());
                    offset += str.getContent().length();
                }
            }
            yOffset += utils.fontHeight();
            yCoord++;
            if (yOffset > clip.height + clip.y) break;
        }
        caretRenderer.renderCaret(g);
    }

    public Dimension getPreferredSize() {
        //        TODO compute incrementally
        return utils.absoluteDimension(state.getTextStorage().relativeSize());
    }

    @Deprecated
    public class CaretRenderer {
        public CaretRenderer() {
            caret = state.getCaret();
        }

        EditorState.Caret caret;

        static final int CARET_WIDTH = 2;

        public Rectangle caretRect() {
            Rectangle result = utils.absoluteTile(caret.getRelativePosition());
            if (!caret.isInInsertMode()) result.width = CARET_WIDTH;
            return result;
        }

        public void renderCaret(Graphics g) {
//            TODO: RENDER ONLY DIFF
            renderBracketHighlighting(g);

            if (caret.isInInsertMode()) {
                renderCharOnBackground(g, caret.getRelativePosition(), EditorColors.TEXT_OVER_INSERT_CARET, EditorColors.INSERT_CARET);
            } else {
                if (!caret.shouldBeRendered()) return;
                fillRectWithColor(g, caretRect(), EditorColors.CARET);
            }

        }

        private void renderBracketHighlighting(Graphics g) {
            BracketHighlighting highlighting = state.getTextStorage().getBracketHighlighting(caret.getRelativePosition());
            highlighting.getBrokenBraces().forEach(coords ->
                    renderCharOnBackground(g, coords, EditorColors.TEXT_OVER_BROKEN_BRACKET, EditorColors.BRACKET_BACKGROUND));
            highlighting.getWorkingBraces().forEach(coords ->
                    renderCharOnBackground(g, coords, EditorColors.TEXT_OVER_WORKING_BRACKET, EditorColors.BRACKET_BACKGROUND));
        }
    }

    private void renderCharOnBackground(Graphics g, Point relativeCharCoords, Color color, Color background) {
        fillRectWithColor(g, utils.absoluteTile(relativeCharCoords), background);
        String relevantLine = state.getTextStorage().getLines().get(relativeCharCoords.y);
        if (relevantLine.length() <= relativeCharCoords.x) return;
        Point textEnd = new Point(relativeCharCoords.x + 1, relativeCharCoords.y);
        String text = state.getTextStorage().getText(relativeCharCoords, textEnd);
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
