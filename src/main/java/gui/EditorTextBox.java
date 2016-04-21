package gui;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Optional;

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

        int xOffset = clip.x;
        int yOffset = fontHeight();

//        TODO: render exact sublist of lines instead of iterating over whole list(faster and cleaner)
        for (String line : textStorage.getLines()) {
            if (yOffset >= clip.y) g.drawString(stringInBounds(line, clip), xOffset, yOffset);
            yOffset += fontHeight();
            if (yOffset > clip.height + clip.y) break;
        }
        caret.renderCaret(g);
    }

    /**
     * Focus
     */

    private void addFocusRelatedListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { requestFocusInWindow(); }
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
                                    caret.relativePosition = selection.startPoint();
                                else if (caretDir.getKeyCode() == KeyEvent.VK_RIGHT)
                                    caret.relativePosition = selection.endPoint();
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

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('c'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!selection.isEmpty()) {
                    ClipboardInterop.copy(textStorage.getText(selection.startPoint(), selection.endPoint()));
                    repaint();
                }
            }
        });

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('v'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<String> pastedText = ClipboardInterop.paste();
                if (pastedText.isPresent()) {
                    selection.removeTextUnderSelection();
                    textStorage.addText(caret.relativePosition, pastedText.get());
                    repaint();
                }
            }
        });



        bindKeyToAction(KeyEvent.VK_DELETE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (caret.relativePosition.equals(textStorage.endOfText()) && selection.isEmpty())
                    return;

                if (selection.isEmpty()) textStorage.removeText(caret.positionAfterCaret(), 1);
                else selection.removeTextUnderSelection();
                repaint();
            }
        });

        bindKeyToAction(KeyEvent.VK_BACK_SPACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (caret.relativePosition.equals(textStorage.beginningOfText()) && selection.isEmpty())
                    return;
                if (selection.isEmpty()) {
                    caret.move(CaretDirection.LEFT);
                    textStorage.removeText(caret.positionAfterCaret(), 1);
                } else selection.removeTextUnderSelection();
                repaint();
            }
        });

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('i'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                caret.switchInsertMode();
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey() || ignoredKeys.indexOf(e.getExtendedKeyCode()) != -1) return;
                if (!getFont().canDisplay(e.getKeyChar())) return;
                if (e.isControlDown()) return;
                if (!selection.isEmpty()) selection.removeTextUnderSelection();
                else if (caret.insertMode &&
                        e.getKeyChar() != '\n' &&
                        textStorage.getLines().get(caret.relativePosition.y).length() > caret.relativePosition.x)
                    textStorage.removeText(caret.positionAfterCaret(), 1);
                textStorage.addText(caret.relativePosition, e.getKeyChar() + "");
                caret.move(CaretDirection.RIGHT);
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    setCaretToMousePosition(e);
                    selection.extendSelection();
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            //TODO: selection if mouse moved in pressed state
            @Override
            public void mouseClicked(MouseEvent e) {
                setCaretToMousePosition(e);
                if (e.isShiftDown()) selection.extendSelection();
                else selection.dropSelection();
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isShiftDown()) return;
                setCaretToMousePosition(e);
                selection.dropSelection();
                repaint();
            }
        });
    }

    private void setCaretToMousePosition(MouseEvent e) {
        Point clickCoords = e.getPoint();
        Point absoluteCoords = new Point(clickCoords.x + (fontWidth()/2), clickCoords.y);
        Point relativeCoords = new Point(absoluteCoords.x / fontWidth(), absoluteCoords.y / fontHeight());
        caret.setRelativePosition(getTextStorage().closestCaretPosition(relativeCoords));
    }

    private void bindKeyToAction(int key, Action action) { bindKeyToAction(key, 0, action); }

    private void bindKeyToAction(int key, int modifier, Action action) {
        String keyName = KeyEvent.getKeyText(key) + KeyEvent.getModifiersExText(modifier);
        getInputMap().put(KeyStroke.getKeyStroke(key, modifier),  keyName);
        getActionMap().put(keyName, action);
    }


    class Caret {
        Caret() {
            relativePosition = new Point(0, 0);
            insertMode = false;
        }

        private Point getAbsolutePosition() { return absoluteCoords(relativePosition); }

        public void setRelativePosition(Point relativePosition) { this.relativePosition = relativePosition; }

        private Point relativePosition;

        private boolean insertMode;

        public void switchInsertMode() {
            insertMode = !insertMode;
        }

        Rectangle caretRect() {
            int width = insertMode ? fontWidth() : 2;
            return new Rectangle(getAbsolutePosition().x, getAbsolutePosition().y, width, fontHeight());
        }

        void renderCaret(Graphics g) {
//            TODO: SPECIFIC COLOR FOR CARET!
//            TODO: SPECIFIC STATE MACHINE FOR COLORS/DEFAULT COLOR/WHATEVER TO MAINTAIN CORRECT STATE
//            TODO: SEPARATE RENDERING AND HANDLING RELATIVE/ABSOLUTE COORDS FROM VIEW LOGIC
//            TODO: RENDER ONLY DIFF
            Color c = g.getColor();
            g.setColor(Color.black);
            g.fillRect(caretRect().x, caretRect().y, caretRect().width, caretRect().height);
            if (insertMode) {
                String relevantLine = textStorage.getLines().get(relativePosition.y);
                if (relevantLine.length() <= relativePosition.x) return;
                g.setColor(Color.green);
                Point textEnd = new Point(relativePosition.x + 1, relativePosition.y);
                String text = textStorage.getText(relativePosition, textEnd);
                g.setFont(FONT);
                g.drawString(text, relativePosition.x * fontWidth(), (relativePosition.y+1) * fontHeight());
            }
            g.setColor(c);
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
            this.initialPoint = point;
            this.edgePoint = point;
        }

        public void dropSelection() { dropSelection(caret.relativePosition); }

        public void extendSelection() { edgePoint = caret.relativePosition; }

        public void switchCaretsWithCurrent() {
            edgePoint = initialPoint;
            initialPoint = caret.relativePosition;
        }

        public boolean isEmpty() { return initialPoint.equals(edgePoint); }

        public Point startPoint() { return isInitialAfterEdge() ? edgePoint : initialPoint; }

        public Point endPoint() { return isInitialAfterEdge() ? initialPoint : edgePoint; }

        private boolean isInitialAfterEdge() {
            return initialPoint.y > edgePoint.y || (initialPoint.y == edgePoint.y && initialPoint.x > edgePoint.x);
        }

        private void removeTextUnderSelection() {
            textStorage.removeText(this);
            dropSelection(startPoint());
            caret.relativePosition = startPoint();
        }

        private Point initialPoint;
        private Point edgePoint;
    }

    static class ClipboardInterop {
        static Optional<String> paste() {
//            are plain retries suitable for this?
            int retries = 100;
            for (int i = 0; i < retries; i++) {
                try {
                    return Optional.of((String) defaultClipboard().getData(DataFlavor.stringFlavor));
                } catch (UnsupportedFlavorException e) {
                    return Optional.empty();
                } catch (IOException | IllegalStateException e) {}
            }
            return Optional.empty();
        }

        static void copy(String str) {
            int retries = 100;
            for (int i = 0; i < retries; i++) {
                try {
                    defaultClipboard().setContents(new StringSelection(str), null);
                } catch (IllegalStateException e) {}
            }
        }

        static Clipboard defaultClipboard() {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        }
    }
}
