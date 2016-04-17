package gui;

import org.w3c.dom.css.Rect;

import javax.swing.*;
import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

/**
 * Created by avyatkin on 06/04/16.
 */
public class EditorTextBox extends JComponent implements Scrollable {
    boolean editable;
    Caret caret;

    Selection selection;

    public EditorTextStorage getTextStorage() { return textStorage; }

    EditorTextStorage textStorage;

    public EditorTextBox(Document doc) {
        textStorage = new EditorTextStorage();
        editable = true;
        caret = new Caret();
        selection = new Selection();
        grabFocus();
        setDoubleBuffered(true);
        addFocusRelatedListeners();
        addCaretRelatedActions();
    }

    public void setEditable(boolean editable) { this.editable = editable; }

    /**
     * Scrollable implementation
     */

    @Override
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getValueForOrientation(orientation, fontWidth(), fontHeight());
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getValueForOrientation(orientation, visibleRect.width, visibleRect.height);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() { return false; }

    @Override
    public boolean getScrollableTracksViewportHeight() { return false; }

    private int getValueForOrientation(int orientation, int horizontal, int vertical) {
        switch (orientation) {
            case SwingConstants.HORIZONTAL:
                return horizontal;
            case SwingConstants.VERTICAL:
                return vertical;
            default:
                throw new IllegalArgumentException("Unexpected orientation: " + orientation);
        }
    }

    /**
     * Font-related metrics & computations
     * For monospaced fixed font
     */

    private static Font FONT = new Font("Monospaced", Font.BOLD, 12);

    private FontMetrics fontMetrics() { return getFontMetrics(FONT); }

    private int fontWidth() { return fontMetrics().charWidth(' '); }

    private int fontHeight() { return fontMetrics().getHeight(); }

    private String stringInBounds(String str, Rectangle bounds) {
        int minX = bounds.x;
        int maxX = bounds.x + bounds.width;
        try {
            int fistCharIndex = minX / fontWidth();
            int lastCharIndex = (int) Math.ceil(maxX / (float) fontWidth());
            return str.substring(Math.min(fistCharIndex, str.length()), Math.min(lastCharIndex, str.length()));
        } catch (ArithmeticException e) {
            return str;
        }
    }

//    TODO: do we need to pass rectangle?
    private List<Rectangle> selectionInBounds(Rectangle bounds) {
        List<Rectangle> result = new ArrayList<>();
        Dimension size = getPreferredSize();
        if (selection.isEmpty()) return result;
        if (selection.startEdge().y == selection.endEdge().y) {
            result.add(absoluteRectangle(new Rectangle(selection.startEdge().x, selection.startEdge().y, selection.endEdge().x - selection.startEdge().x, 1)));
        } else {
            result.add(absoluteRectangle(new Rectangle(selection.startEdge().x, selection.startEdge().y, size.width, 1)));
            for (int i = selection.startEdge().y + 1; i < selection.endEdge().y; i++)
                result.add(absoluteRectangle(new Rectangle(0, i, size.width, 1)));
            result.add(absoluteRectangle(new Rectangle(0, selection.endEdge().y, selection.endEdge().x, 1)));
        }
        return result;
    }

    private Point absoluteCoords(Point point) {
        return new Point(point.x * fontWidth(),
                point.y * fontHeight() + (fontHeight() - fontMetrics().getAscent()));
    }

    private Rectangle absoluteRectangle(Rectangle rect) {
        Point absoluteCoords = absoluteCoords(new Point(rect.x, rect.y));
        return new Rectangle(absoluteCoords.x, absoluteCoords.y, rect.width * fontWidth(), rect.height * fontHeight());
    }

    public Dimension getPreferredSize() {
//        TODO compute incrementally
        Dimension relativeSize = textStorage.relativeSize();
        return new Dimension(relativeSize.width * fontWidth(), relativeSize.height * fontHeight());
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(FONT);
        g.setColor(Color.black);

        Rectangle clip = g.getClipBounds();
        g.setColor(Color.blue);
        selectionInBounds(clip).forEach(rect -> g.fillRect(rect.x, rect.y, rect.width, rect.height));
        g.setColor(Color.black);

        caret.renderCaret(g);

        int xOffset = clip.x;
        int yOffset = fontHeight();

//        TODO: render exact sublist of lines instead of iterating over whole list(faster and cleaner)
        for (String line : textStorage.getLines()) {
            if (yOffset >= clip.y) g.drawString(stringInBounds(line, clip), xOffset, yOffset);
            yOffset += fontHeight();
            if (yOffset > clip.height + clip.y) break;
        }
    }

    /**
     * Focus
     */

    private void addFocusRelatedListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    /**
     * Caret
     */

    enum CaretDirection {
        UP, DOWN, LEFT, RIGHT;

        int getKeyCode() { return getCorrectOne(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT); }

        private <T> T getCorrectOne(T up, T down, T left, T right) {
            switch (this) {
                case UP: return up;
                case DOWN: return down;
                case LEFT: return left;
                case RIGHT: return right;
            }
            throw new RuntimeException("Missing caret direction!");
        }
    }

    List<Integer> ignoredKeys = Arrays.asList(KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE);


    private void addCaretRelatedActions() {
        Arrays.asList(CaretDirection.values()).forEach(
                caretDir -> {
                    bindKeyToAction(caretDir.getKeyCode(), new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (!selection.isEmpty()) {
                                if (caretDir.getKeyCode() == KeyEvent.VK_LEFT)
                                    caret.relativePosition = selection.startEdge();
                                else if (caretDir.getKeyCode() == KeyEvent.VK_RIGHT)
                                    caret.relativePosition = selection.endEdge();
                                else caret.move(caretDir);

                            } else caret.move(caretDir);

                            selection.dropSelection();
                            repaint();
                        }
                    });

                    bindKeyToAction(caretDir.getKeyCode(), KeyEvent.SHIFT_DOWN_MASK, new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            caret.move(caretDir);
                            selection.extendSelection();
                            repaint();
                        }
                    });
                });

        bindKeyToAction(KeyEvent.VK_DELETE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!caret.relativePosition.equals(textStorage.endOfText())) {
                    if (selection.isEmpty()) textStorage.removeText(caret.positionAfterCaret(), 1);
                    else selection.removeTextUnderSelection();
                    repaint();
                }
            }
        });

        bindKeyToAction(KeyEvent.VK_BACK_SPACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!caret.relativePosition.equals(textStorage.beginningOfText())) {
                    if (selection.isEmpty()) {
                        caret.move(CaretDirection.LEFT);
                        textStorage.removeText(caret.positionAfterCaret(), 1);
                    } else selection.removeTextUnderSelection();
                    repaint();
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey() || ignoredKeys.indexOf(e.getExtendedKeyCode()) != -1) return;
                if (!selection.isEmpty()) selection.removeTextUnderSelection();
                textStorage.addText(caret.relativePosition, e.getKeyChar() + "");
                caret.move(CaretDirection.RIGHT);
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            //TODO: selection if mouse moved in pressed state
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Point clickCoords = e.getPoint();
                Point absoluteCoords = new Point(clickCoords.x + (fontWidth()/2), clickCoords.y);
                Point relativeCoords = new Point(absoluteCoords.x / fontWidth(), absoluteCoords.y / fontHeight());
                caret.setRelativePosition(getTextStorage().closestCaretPosition(relativeCoords));
                if (e.isShiftDown()) selection.extendSelection();
                else selection.dropSelection();
                repaint();
            }
        });
    }

    private void bindKeyToAction(int key, Action action) {
        bindKeyToAction(key, 0, action);
    }

    private void bindKeyToAction(int key, int modifier, Action action) {
        String keyName = KeyEvent.getKeyText(key) + KeyEvent.getModifiersExText(modifier);
        getInputMap().put(KeyStroke.getKeyStroke(key, modifier),  keyName);
        getActionMap().put(keyName, action);
    }


    class Caret {
        Caret() { relativePosition = new Point(0, 0); }

        private Point getAbsolutePosition() { return absoluteCoords(relativePosition); }

        public void setRelativePosition(Point relativePosition) { this.relativePosition = relativePosition; }

        private Point relativePosition;

        Rectangle caretRect() {
            int yCoord = getAbsolutePosition().y;
            return new Rectangle(getAbsolutePosition().x, yCoord, 2, fontHeight());
        }

        void renderCaret(Graphics g) {
            g.fillRect(caretRect().x, caretRect().y, caretRect().width, caretRect().height);
        }

        Point positionAfterCaret() { return relativePosition; }

        void move(CaretDirection direction) {
            Point updatedPosition = (Point) relativePosition.clone();
            switch (direction) {
                case UP:
                    updatedPosition = textStorage.verticalMove(updatedPosition, -1); break;
                case DOWN:
                    updatedPosition = textStorage.verticalMove(updatedPosition, 1); break;
                case LEFT:
                    updatedPosition = textStorage.horizontalMove(updatedPosition, -1); break;
                case RIGHT:
                    updatedPosition = textStorage.horizontalMove(updatedPosition, 1); break;
            }
            if (updatedPosition != relativePosition) {
                setRelativePosition(updatedPosition);
                scrollRectToVisible(caretRect());
            }
        }
    }

    /**
     * all coords are relative to text
     */

    class Selection {
        public Selection() { dropSelection(); }

        public void dropSelection(Point point) {
            this.initialCaret = point;
            this.edgeCaret = point;
        }

        public void dropSelection() { dropSelection(caret.relativePosition); }

        public void extendSelection() { edgeCaret = caret.relativePosition; }

        public boolean isEmpty() { return initialCaret.equals(edgeCaret); }

        public Point startEdge() { return isInitialAfterEdge() ? edgeCaret : initialCaret; }

        public Point endEdge() { return isInitialAfterEdge() ? initialCaret : edgeCaret; }

        private boolean isInitialAfterEdge() {
            return initialCaret.y > edgeCaret.y || (initialCaret.y == edgeCaret.y && initialCaret.x > edgeCaret.x);
        }

        private void removeTextUnderSelection() {
            textStorage.removeText(startEdge(), endEdge());
            dropSelection(startEdge());
            caret.relativePosition = startEdge();
        }

        private Point initialCaret;
        private Point edgeCaret;
    }
}
