package gui;

import org.apache.commons.collections4.list.TreeList;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by avyatkin on 06/04/16.
 */
public class EditorTextBox extends JComponent implements Scrollable, Accessible
{
    String text;
    List<String> lines;
    boolean editable;
    Caret caret;

    public EditorTextBox(Document doc) {
        text = "";
        lines = new TreeList<>();
        lines.add("");
        editable = true;
        caret = new Caret();
        updatePreferredSize();
        grabFocus();
        setDoubleBuffered(true);
        addFocusRelatedListeners();
        addCaretMovementsListeners();
    }

    public String getText() { return text; }

    public void setText(String text) {
        this.text = text;
        this.lines = new TreeList<>(Arrays.asList(text.split("\n")));
        updatePreferredSize();
    }

    public boolean isEditable() { return editable; }

    public void setEditable(boolean editable) { this.editable = editable; }

    /**
     * Scrollable implementation
     */

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getValueForOrientation(orientation, fontWidth(), fontHeight());
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getValueForOrientation(orientation, visibleRect.width, visibleRect.height);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

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

    private int stringWidth(String str) { return str.length() * fontWidth(); }

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

    private Point absoluteCoords(Point point) {
        return new Point(point.x * fontWidth(), point.y * fontHeight());
    }

    private void updatePreferredSize() {
//        TODO: do not scan all the every time, store them in heap or hash by their length or something
        int width = lines.stream().map(this::stringWidth).reduce(0, Integer::max);
        int height = lines.size() * fontHeight();
        setPreferredSize(new Dimension(width, height));
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
        for (String line: lines) {
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

    private int lastLine() { return lines.size() - 1; }

    enum CaretDirection { UP, DOWN, LEFT, RIGHT }

    private void addCaretMovementsListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        caret.move(CaretDirection.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                        caret.move(CaretDirection.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                        caret.move(CaretDirection.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                        caret.move(CaretDirection.RIGHT);
                        break;
                }
            }
        });
    }

    class Caret {
        Caret() { relativePosition = new Point(0, 0); }

        private Point getAbsolutePosition() { return absoluteCoords(relativePosition); }

        public void setRelativePosition(Point relativePosition) { this.relativePosition = relativePosition; }

        Point relativePosition;

        Rectangle caretRect() {
            int yCoord = getAbsolutePosition().y + (fontHeight() - fontMetrics().getAscent());
            return new Rectangle(getAbsolutePosition().x, yCoord, 2, fontHeight());
        }

        void renderCaret(Graphics g) {
            g.fillRect(caretRect().x, caretRect().y, caretRect().width, caretRect().height);
        }

        void move(CaretDirection direction) {
            Point previousPosition = (Point)relativePosition.clone();
            switch (direction) {
                case UP: verticalMove(-1);
                    break;
                case DOWN: verticalMove(1);
                    break;
                case LEFT: horizontalMove(-1);
                    break;
                case RIGHT: horizontalMove(1);
                    break;
            }
            if (previousPosition != relativePosition) repaint();
        }

        private void verticalMove(int direction) {
            int newY = Math.min(Math.max(relativePosition.y + direction, 0), lastLine());
            if (newY == relativePosition.y) return;
            int newX = Math.min(lines.get(newY).length(), relativePosition.x);
            setRelativePosition(new Point(newX, newY));
        }

        private void horizontalMove(int direction) {
            String currentLine = lines.get(relativePosition.y);
            int newX = Math.min(Math.max(0, relativePosition.x+ direction), currentLine.length());
            if (newX != relativePosition.x)
                setRelativePosition(new Point(newX, relativePosition.y));
        }
    }

    /**
     *
     */
}
