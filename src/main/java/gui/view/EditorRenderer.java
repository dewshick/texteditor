package gui.view;

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
    ViewUtils utils;

    public CaretRenderer getCaretRenderer() {
        return caretRenderer;
    }

    CaretRenderer caretRenderer;

    public EditorRenderer(EditorState state1, ViewUtils params1) {
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
        g.setFont(ViewUtils.FONT);

        Rectangle clip = g.getClipBounds();
        g.setColor(EditorColors.BACKGROUND);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(EditorColors.SELECTION);
        selectionInBounds(clip).forEach(rect -> g.fillRect(rect.x, rect.y, rect.width, rect.height));
        g.setColor(EditorColors.TEXT);

        int xOffset = clip.x;
        int yOffset = utils.fontHeight();

//        TODO: render exact sublist of lines instead of iterating over whole list(faster and cleaner)
        for (String line : state.getTextStorage().getLines()) {
            if (yOffset >= clip.y) g.drawString(utils.stringInRelativeBounds(line, clip), xOffset, yOffset);
            yOffset += utils.fontHeight();
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
//            TODO: SPECIFIC COLOR FOR CARET!
//            TODO: SPECIFIC STATE MACHINE FOR COLORS/DEFAULT COLOR/WHATEVER TO MAINTAIN CORRECT STATE
//            TODO: SEPARATE RENDERING AND HANDLING RELATIVE/ABSOLUTE COORDS FROM VIEW LOGIC
//            TODO: RENDER ONLY DIFF

            g.setColor(caret.isInInsertMode() ? EditorColors.INSERT_CARET : EditorColors.CARET);

            g.fillRect(caretRect().x, caretRect().y, caretRect().width, caretRect().height);
            Point relativePosition = caret.getRelativePosition();
            EditorTextStorage textStorage = state.getTextStorage();

            if (caret.isInInsertMode()) {
                String relevantLine = textStorage.getLines().get(relativePosition.y);
                if (relevantLine.length() <= relativePosition.x) return;
                g.setColor(EditorColors.TEXT_OVER_INSERT_CARET);
                Point textEnd = new Point(relativePosition.x + 1, relativePosition.y);
                String text = textStorage.getText(relativePosition, textEnd);
                g.setFont(ViewUtils.FONT);
                Point textCoords = utils.absoluteTextCoords(relativePosition);
                g.drawString(text, textCoords.x, textCoords.y);
            }
        }


    }
}
