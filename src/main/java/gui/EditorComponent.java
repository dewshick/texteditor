package gui;

import gui.state.CaretDirection;
import gui.state.EditorState;
import gui.view.EditorRenderer;
import gui.view.TextCoordUtils;
import syntax.document.SupportedSyntax;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Timer;

/**
 * Created by avyatkin on 06/04/16.
 */
public class EditorComponent extends JComponent implements Scrollable {

    public void setText(String text) {
        state.setText(text, this);
    }

    public String getText() { return state.getTextStorage().getText(); }

    EditorState state;
    TextCoordUtils coordUtils;
    EditorRenderer renderer;

//    TODO deal somehow with scroll pane update and remove this ugly workaround
    public void setScrollPane(JScrollPane pane) {
        scrollPane = pane;
    }

    JScrollPane scrollPane;

    public EditorComponent(SupportedSyntax syntax) {
        state = new EditorState(syntax);
        coordUtils = new TextCoordUtils(this);
        renderer = new EditorRenderer(state, coordUtils);
        setDoubleBuffered(true);
        addFocusRelatedListeners();
        addCaretRelatedActions();
        addCaretBlink();
    }

    public void changeSyntax(SupportedSyntax syntax) {
        state.changeSyntax(syntax);
    }

    /**
     * Scrollable implementation
     */

    @Override
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getValueForOrientation(orientation, coordUtils.fontWidth(), coordUtils.fontHeight());
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
            case SwingConstants.HORIZONTAL: return horizontal;
            case SwingConstants.VERTICAL: return vertical;
            default: throw new IllegalArgumentException("Unexpected orientation: " + orientation);
        }
    }

    /**
     * Font-related metrics & computations
     * For monospaced fixed font
     */

    public Dimension getPreferredSize() {
        Dimension rendererPreferredSize = renderer.getPreferredSize();
        Dimension minimalDimension = new Dimension(500, 300);
        Rectangle visibleRect = getVisibleRect();

        return new Dimension(
                Collections.max(Arrays.asList(visibleRect.width, rendererPreferredSize.width, minimalDimension.width)),
                Collections.max(Arrays.asList(visibleRect.height, rendererPreferredSize.height, minimalDimension.width)));
    }

    /**
     * Focus
     */

    private void addFocusRelatedListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!state.isAvailable()) return;
                requestFocusInWindow();
            }
        });
    }

    List<Integer> ignoredKeys = Arrays.asList(KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE);


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.paintState(g);
    }

    public void updateViewWithScroll() {
        scrollPane.revalidate();
        scrollPane.repaint();
        repaint();
    }

    public void updateView(boolean caretMoved) {
        if (caretMoved) scrollRectToVisible(renderer.getCaretRenderer().caretRect());
        repaint();
    }

//     TODO: accept rectangle in relative coords with diff(recieved from state after state change) to be able to repaint only diff
//     TODO: state will return rectangles to repaint
    public void updateView(List<Rectangle> rectangles, boolean caretMoved) {
        if (caretMoved && !getVisibleRect().contains(renderer.getCaretRenderer().caretRect()))
            repaint();
        else {
            for (Rectangle rect : rectangles) repaint(coordUtils.absoluteRectangle(rect));
        }
    }

    private void addCaretRelatedActions() {
        Arrays.asList(CaretDirection.values()).forEach(
                caretDir -> {
                    bindKeyToAction(caretDir.getKeyCode(), new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            state.moveCaret(caretDir, false);
                            updateView(true);
                        }
                    });

                    bindKeyToAction(caretDir.getKeyCode(), KeyEvent.SHIFT_DOWN_MASK, new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            state.moveCaret(caretDir, true);
                            updateView(true);
                        }
                    });
                });

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('c'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<String> selectedText = state.getSelectedText();
                if (selectedText.isPresent()) ClipboardInterop.copy(selectedText.get());
                updateView(false);
            }
        });

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('v'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<String> pastedText = ClipboardInterop.paste();
                if (pastedText.isPresent()) {
                    state.paste(pastedText.get());
                    updateView(true);
                }
            }
        });

        bindKeyToAction(KeyEvent.VK_DELETE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.delete();
                updateView(true);
            }
        });

        bindKeyToAction(KeyEvent.VK_BACK_SPACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.backspace();
                updateView(true);
            }
        });

        bindKeyToAction(KeyEvent.getExtendedKeyCodeForChar('i'), KeyEvent.CTRL_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.switchCaretMode();
                updateView(false);
            }
        });

        bindKeyToAction(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.pageUp(coordUtils.relativeRectangle(getVisibleRect()));
                updateView(true);
            }
        });

        bindKeyToAction(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.pageDown(coordUtils.relativeRectangle(getVisibleRect()));
                updateView(true);
            }
        });

        bindKeyToAction(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.goToBeginning();
                updateView(true);
            }
        });

        bindKeyToAction(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.goToEnd();
                updateView(true);
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey() || ignoredKeys.indexOf(e.getExtendedKeyCode()) != -1) return;
                if (!getFont().canDisplay(e.getKeyChar())) return;
                if (e.isControlDown()) return;
                state.type(e.getKeyChar());
                updateView(true);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    state.moveCaret(coordUtils.relativeMousePosition(e), true);
                    updateView(true);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            //TODO: selection if mouse moved in pressed state
            @Override
            public void mouseClicked(MouseEvent e) {
                state.moveCaret(coordUtils.relativeMousePosition(e), e.isShiftDown());
                updateView(true);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isShiftDown()) return;
                state.moveCaret(coordUtils.relativeMousePosition(e), false);
                updateView(true);
            }
        });
    }

    private void addCaretBlink() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (!state.getCaret().isInInsertMode())
                        repaint(renderer.getCaretRenderer().caretRect());
                    state.getTextStorage().syncIfPossible();
                });
            }
        },0, EditorState.Caret.CARET_BLINK_TIME);
    }

    private void bindKeyToAction(int key, Action action) { bindKeyToAction(key, 0, action); }

    private void bindKeyToAction(int key, int modifier, Action action) {
        String keyName = KeyEvent.getKeyText(key) + KeyEvent.getModifiersExText(modifier);
        getInputMap().put(KeyStroke.getKeyStroke(key, modifier),  keyName);
        getActionMap().put(keyName, action);
    }
}
