package gui.view;

import gui.EditorComponent;
import gui.state.CaretDirection;
import gui.state.EditorState;
import gui.state.EditorTextStorage;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by avyatkin on 22/04/16.
 */
public class EditorRenderer {
    EditorState state;
    ViewParams params;

    public CaretRenderer getCaretRenderer() {
        return caretRenderer;
    }

    CaretRenderer caretRenderer;

    public EditorRenderer(EditorState state1, ViewParams params1) {
        state = state1;
        params = params1;
        caretRenderer = new CaretRenderer();
    }

//    TODO: do we need to pass rectangle?
//    TODO: move rendering in separate class
    private List<Rectangle> selectionInBounds(Rectangle bounds) {
        List<Rectangle> result = new ArrayList<>();

        Dimension size = getPreferredSize();

        EditorState.Selection selection = state.getSelection();

        if (selection.isEmpty()) return result;
        if (selection.startPoint().y == selection.endPoint().y) {
            result.add(absoluteRectangle(new Rectangle(selection.startPoint().x, selection.startPoint().y, selection.endPoint().x - selection.startPoint().x, 1)));
        } else {
            result.add(absoluteRectangle(new Rectangle(selection.startPoint().x, selection.startPoint().y, size.width, 1)));
            for (int i = selection.startPoint().y + 1; i < selection.endPoint().y; i++)
                result.add(absoluteRectangle(new Rectangle(0, i, size.width, 1)));
            result.add(absoluteRectangle(new Rectangle(0, selection.endPoint().y, selection.endPoint().x, 1)));
        }
        return result;
    }

    private String stringInBounds(String str, Rectangle bounds) {
        int minX = bounds.x;
        int maxX = bounds.x + bounds.width;
        try {
            int fistCharIndex = minX / params.fontWidth();
            int lastCharIndex = (int) Math.ceil(maxX / (float) params.fontWidth());
            return str.substring(Math.min(fistCharIndex, str.length()), Math.min(lastCharIndex, str.length()));
        } catch (ArithmeticException e) {
            return str;
        }
    }

    //    TODO: render all this in separate specified class
    public void paintComponent(EditorComponent component, Graphics g) {
        g.setFont(params.FONT);
        g.setColor(Color.black);

        Rectangle clip = g.getClipBounds();
        g.setColor(Color.blue);
        selectionInBounds(clip).forEach(rect -> g.fillRect(rect.x, rect.y, rect.width, rect.height));
        g.setColor(Color.black);

        int xOffset = clip.x;
        int yOffset = params.fontHeight();

//        TODO: render exact sublist of lines instead of iterating over whole list(faster and cleaner)
        for (String line : state.getTextStorage().getLines()) {
            if (yOffset >= clip.y) g.drawString(stringInBounds(line, clip), xOffset, yOffset);
            yOffset += params.fontHeight();
            if (yOffset > clip.height + clip.y) break;
        }
        caretRenderer.renderCaret(g);
    }

    private Point absoluteCoords(Point point) {
        return new Point(point.x * params.fontWidth(),
                point.y * params.fontHeight() + (params.fontHeight() - params.fontMetrics().getAscent()));
    }

    private Rectangle absoluteRectangle(Rectangle rect) {
        Point absoluteCoords = absoluteCoords(new Point(rect.x, rect.y));
        return new Rectangle(absoluteCoords.x, absoluteCoords.y, rect.width * params.fontWidth(), rect.height * params.fontHeight());
    }

    public Dimension getPreferredSize() {
        //        TODO compute incrementally
        Dimension relativeSize = state.getTextStorage().relativeSize();
        return new Dimension(relativeSize.width * params.fontWidth(), relativeSize.height * params.fontHeight());
    }

    @Deprecated
    public class CaretRenderer {
        public CaretRenderer() {
            caret = state.getCaret();
        }

        EditorState.Caret caret;

        private Point getAbsolutePosition() { return absoluteCoords(caret.getRelativePosition()); }

        Rectangle caretRect() {
            int width = caret.isInInsertMode() ? params.fontWidth() : 2;
            return new Rectangle(getAbsolutePosition().x, getAbsolutePosition().y, width, params.fontHeight());
        }

        public void updateCaret(EditorComponent component, Point coords, boolean extendSelection) {
            Point oldCoords = caret.getRelativePosition();
            state.moveCaret(coords, extendSelection);
            if (!oldCoords.equals(caret.getRelativePosition())) {
                component.scrollRectToVisible(caretRect());
                component.repaint();
            }
        }


        public void updateCaret(EditorComponent component, CaretDirection direction, boolean extendSelection) {
            Point oldCoords = caret.getRelativePosition();
            state.moveCaret(direction, extendSelection);
            if (!oldCoords.equals(caret.getRelativePosition())) {
                component.scrollRectToVisible(caretRect());
                component.repaint();
            }
        }

        //        TODO: protected
        public void renderCaret(Graphics g) {
//            TODO: SPECIFIC COLOR FOR CARET!
//            TODO: SPECIFIC STATE MACHINE FOR COLORS/DEFAULT COLOR/WHATEVER TO MAINTAIN CORRECT STATE
//            TODO: SEPARATE RENDERING AND HANDLING RELATIVE/ABSOLUTE COORDS FROM VIEW LOGIC
//            TODO: RENDER ONLY DIFF
            Color c = g.getColor();
            g.setColor(Color.black);
            g.fillRect(caretRect().x, caretRect().y, caretRect().width, caretRect().height);
            Point relativePosition = caret.getRelativePosition();
            EditorTextStorage textStorage = state.getTextStorage();
            if (caret.isInInsertMode()) {
                String relevantLine = textStorage.getLines().get(relativePosition.y);
                if (relevantLine.length() <= relativePosition.x) return;
                g.setColor(Color.green);
                Point textEnd = new Point(relativePosition.x + 1, relativePosition.y);
                String text = textStorage.getText(relativePosition, textEnd);
                g.setFont(params.FONT);
                g.drawString(text, relativePosition.x * params.fontWidth(), (relativePosition.y+1) * params.fontHeight());
            }
            g.setColor(c);
        }


    }
}
