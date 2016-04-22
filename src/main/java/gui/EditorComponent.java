package gui;

import gui.state.CaretDirection;
import gui.state.EditorState;
import gui.state.EditorTextStorage;

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
public class EditorComponent extends JComponent implements Scrollable {
    boolean editable;

    public void setText(String text) { state.getTextStorage().setText(text); }
    public String getText() { return state.getTextStorage().getText(); }

    EditorState state;

    CaretRenderer caretRenderer;

    public EditorComponent(Document doc) {
        state = new EditorState();
        editable = true;
        caretRenderer = new CaretRenderer();
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
        Dimension relativeSize = state.getTextStorage().relativeSize();
        return new Dimension(relativeSize.width * fontWidth(), relativeSize.height * fontHeight());
    }

//    TODO: render all this in separate specified class
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
        for (String line : state.getTextStorage().getLines()) {
            if (yOffset >= clip.y) g.drawString(stringInBounds(line, clip), xOffset, yOffset);
            yOffset += fontHeight();
            if (yOffset > clip.height + clip.y) break;
        }
        caretRenderer.renderCaret(g);
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


    List<Integer> ignoredKeys = Arrays.asList(KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE);


    private void addCaretRelatedActions() {
        Arrays.asList(CaretDirection.values()).forEach(
                caretDir -> {
                    bindKeyToAction(caretDir.getKeyCode(), new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            caretRenderer.updateCaret(caretDir, false);
                        }
                    });

                    bindKeyToAction(caretDir.getKeyCode(), KeyEvent.SHIFT_DOWN_MASK, new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            caretRenderer.updateCaret(caretDir, true);
                        }
                    });
                });

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('c'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<String> selectedText = state.getSelectedText();
                if (selectedText.isPresent()) ClipboardInterop.copy(selectedText.get());
                repaint();
            }
        });

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('v'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<String> pastedText = ClipboardInterop.paste();
                if (pastedText.isPresent()) {
                    state.paste(pastedText.get());
                    repaint();
                }
            }
        });



        bindKeyToAction(KeyEvent.VK_DELETE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.delete();
                repaint();
            }
        });

        bindKeyToAction(KeyEvent.VK_BACK_SPACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.backspace();
                repaint();
            }
        });

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('i'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.switchCaretMode();
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey() || ignoredKeys.indexOf(e.getExtendedKeyCode()) != -1) return;
                if (!getFont().canDisplay(e.getKeyChar())) return;
                if (e.isControlDown()) return;
                state.type(e.getKeyChar());
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    caretRenderer.updateCaret(getRelativeMousePosition(e), true);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            //TODO: selection if mouse moved in pressed state
            @Override
            public void mouseClicked(MouseEvent e) {
                caretRenderer.updateCaret(getRelativeMousePosition(e), e.isShiftDown());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isShiftDown()) return;
                caretRenderer.updateCaret(getRelativeMousePosition(e), false);
            }
        });
    }

    private Point getRelativeMousePosition(MouseEvent e) {
        Point clickCoords = e.getPoint();
        Point absoluteCoords = new Point(clickCoords.x + (fontWidth()/2), clickCoords.y);
        Point relativeCoords = new Point(absoluteCoords.x / fontWidth(), absoluteCoords.y / fontHeight());
        return relativeCoords;
    }

    private void bindKeyToAction(int key, Action action) { bindKeyToAction(key, 0, action); }

    private void bindKeyToAction(int key, int modifier, Action action) {
        String keyName = KeyEvent.getKeyText(key) + KeyEvent.getModifiersExText(modifier);
        getInputMap().put(KeyStroke.getKeyStroke(key, modifier),  keyName);
        getActionMap().put(keyName, action);
    }

    /**
     * all coords are relative to text
     */

    @Deprecated
    class CaretRenderer {
        public CaretRenderer() {
            caret = state.getCaret();
        }

        EditorState.Caret caret;

        private Point getAbsolutePosition() { return absoluteCoords(caret.getRelativePosition()); }

        Rectangle caretRect() {
            int width = caret.isInInsertMode() ? fontWidth() : 2;
            return new Rectangle(getAbsolutePosition().x, getAbsolutePosition().y, width, fontHeight());
        }

        public void updateCaret(Point coords, boolean extendSelection) {
            Point oldCoords = caret.getRelativePosition();
            state.moveCaret(coords, extendSelection);
            if (!oldCoords.equals(caret.getRelativePosition())) {
                scrollRectToVisible(caretRect());
                repaint();
            }
        }


        public void updateCaret(CaretDirection direction, boolean extendSelection) {
            Point oldCoords = caret.getRelativePosition();
            state.moveCaret(direction, extendSelection);
            if (!oldCoords.equals(caret.getRelativePosition())) {
                scrollRectToVisible(caretRect());
                repaint();
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
                g.setFont(FONT);
                g.drawString(text, relativePosition.x * fontWidth(), (relativePosition.y+1) * fontHeight());
            }
            g.setColor(c);
        }


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
