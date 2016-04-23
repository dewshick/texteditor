package gui;

import gui.state.CaretDirection;
import gui.state.EditorState;
import gui.view.EditorRenderer;
import gui.view.ViewUtils;
import javax.swing.*;
import javax.swing.text.Document;
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
    ViewUtils viewParams;
    EditorRenderer renderer;

    public EditorComponent(Document doc) {
        state = new EditorState();
        viewParams = new ViewUtils(this);
        renderer = new EditorRenderer(state, viewParams);

        editable = true;

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
        return getValueForOrientation(orientation, viewParams.fontWidth(), viewParams.fontHeight());
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

    public Dimension getPreferredSize() {
        return renderer.getPreferredSize();
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


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.paintState(g);
    }

    public void handleCaretMovement(boolean caretMoved) {
        if (caretMoved) {
            scrollRectToVisible(renderer.getCaretRenderer().caretRect());
            repaint();
        }
    }

    private void addCaretRelatedActions() {
        Arrays.asList(CaretDirection.values()).forEach(
                caretDir -> {
                    bindKeyToAction(caretDir.getKeyCode(), new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            handleCaretMovement(state.moveCaret(caretDir, false));
                        }
                    });

                    bindKeyToAction(caretDir.getKeyCode(), KeyEvent.SHIFT_DOWN_MASK, new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            handleCaretMovement(state.moveCaret(caretDir, true));
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
                    handleCaretMovement(state.moveCaret(viewParams.relativeMousePosition(e), true));
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            //TODO: selection if mouse moved in pressed state
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCaretMovement(state.moveCaret(viewParams.relativeMousePosition(e), e.isShiftDown()));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isShiftDown()) return;
                handleCaretMovement(state.moveCaret(viewParams.relativeMousePosition(e), false));
            }
        });
    }

    private void bindKeyToAction(int key, Action action) { bindKeyToAction(key, 0, action); }

    private void bindKeyToAction(int key, int modifier, Action action) {
        String keyName = KeyEvent.getKeyText(key) + KeyEvent.getModifiersExText(modifier);
        getInputMap().put(KeyStroke.getKeyStroke(key, modifier),  keyName);
        getActionMap().put(keyName, action);
    }
}
