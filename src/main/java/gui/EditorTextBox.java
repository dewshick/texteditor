package gui;

import javax.swing.*;
import javax.swing.text.Document;
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

    public EditorTextStorage getTextStorage() {
        return textStorage;
    }

    EditorTextStorage textStorage;

    public EditorTextBox(Document doc) {
        textStorage = new EditorTextStorage();
        editable = true;
        caret = new Caret();
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

    private Point absoluteCoords(Point point) { return new Point(point.x * fontWidth(), point.y * fontHeight()); }

    public Dimension getPreferredSize() {
//        TODO compute incrementally
        Dimension relativeSize = textStorage.relativeSize();
        return new Dimension(relativeSize.width * fontWidth(), relativeSize.height * fontHeight());
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(FONT);
        g.setColor(Color.black);

        caret.renderCaret(g);
        Rectangle clip = g.getClipBounds();
        g.setColor(Color.black);

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
            @Override public void mouseClicked(MouseEvent e) { requestFocusInWindow(); }
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
                caretDir -> bindKeyToAction(caretDir.getKeyCode(), new AbstractAction() {
                    @Override public void actionPerformed(ActionEvent e) { caret.move(caretDir); }
                }));

        bindKeyToAction(KeyEvent.VK_DELETE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!caret.relativePosition.equals(textStorage.endOfText())) {
                    textStorage.removeText(caret.positionAfterCaret(), 1);
                    repaint();
                }
            }
        });

        bindKeyToAction(KeyEvent.VK_BACK_SPACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!caret.relativePosition.equals(textStorage.beginningOfText())) {
                    caret.move(CaretDirection.LEFT);
                    textStorage.removeText(caret.positionAfterCaret(), 1);
                    repaint();
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey() || ignoredKeys.indexOf(e.getExtendedKeyCode()) != -1) return;
                textStorage.addText(caret.relativePosition, e.getKeyChar() + "");
                caret.move(CaretDirection.RIGHT);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Point clickCoords = e.getPoint();
                Point absoluteCoords = new Point(clickCoords.x + (fontWidth()/2), clickCoords.y);
                Point relativeCoords = new Point(absoluteCoords.x / fontWidth(), absoluteCoords.y / fontHeight());
                caret.setRelativePosition(getTextStorage().closestRealCoord(relativeCoords));
                repaint();
            }
        });
    }

    private void bindKeyToAction(int key, Action action) {
        String keyName = KeyEvent.getKeyText(key);
        getInputMap().put(KeyStroke.getKeyStroke(key, 0),  keyName);
        getActionMap().put(keyName, action);
    }


    class Caret {
        Caret() { relativePosition = new Point(0, 0); }

        private Point getAbsolutePosition() { return absoluteCoords(relativePosition); }

        public void setRelativePosition(Point relativePosition) { this.relativePosition = relativePosition; }

        private Point relativePosition;

        Rectangle caretRect() {
            int yCoord = getAbsolutePosition().y + (fontHeight() - fontMetrics().getAscent());
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
                repaint();
            }
        }
    }

    /**
     *
     */
}
