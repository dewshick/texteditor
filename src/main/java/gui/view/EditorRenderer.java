package gui.view;

import gui.state.ColoredString;
import gui.state.EditorState;
import gui.state.EditorTextStorage;
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

        selectionInBounds(clip).forEach(rect -> fillRectWithColor(g, rect, EditorColors.SELECTION));

        int xOffset = clip.x;
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
            Point coords = utils.absoluteCoords(caret.getRelativePosition());
            int width = caret.isInInsertMode() ? utils.fontWidth() : CARET_WIDTH;
            return new Rectangle(coords.x, coords.y, width, utils.fontHeight());
        }

//            TODO: protected
        public void renderCaret(Graphics g) {
//            TODO: SPECIFIC STATE MACHINE FOR COLORS/DEFAULT COLOR/WHATEVER TO MAINTAIN CORRECT STATE
//            TODO: SEPARATE RENDERING AND HANDLING RELATIVE/ABSOLUTE COORDS FROM VIEW LOGIC
//            TODO: RENDER ONLY DIFF
            if (!caret.shouldBeRendered()) return;
            Color caretColor = caret.isInInsertMode() ? EditorColors.INSERT_CARET : EditorColors.CARET;
            fillRectWithColor(g, caretRect(), caretColor);
            Point relativePosition = caret.getRelativePosition();
            EditorTextStorage textStorage = state.getTextStorage();

            if (caret.isInInsertMode()) {
                String relevantLine = textStorage.getLines().get(relativePosition.y);
                if (relevantLine.length() <= relativePosition.x) return;
                Point textEnd = new Point(relativePosition.x + 1, relativePosition.y);
                String text = textStorage.getText(relativePosition, textEnd);
                drawStringWithColor(g, text, relativePosition, EditorColors.TEXT_OVER_INSERT_CARET);
            }
        }
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
