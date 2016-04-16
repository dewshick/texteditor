package gui;

import org.apache.commons.collections4.list.TreeList;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by avyatkin on 06/04/16.
 */
public class EditorTextBox extends JComponent implements Scrollable {
    String text;
    List<String> lines;
    boolean editable;
    Caret caret;

    public EditorTextBox(Document doc) {
        lines = new TreeList<>();
        lines.add("");
        editable = true;
        caret = new Caret();
        updatePreferredSize();
        grabFocus();
        setDoubleBuffered(true);
        addFocusRelatedListeners();
        addCaretMovementsActions();
    }

    public String getText() { return String.join("\n", lines); }

    public void setText(String text) {
        this.lines = buildLinesList(text);
        updatePreferredSize();
    }

    private List<String> buildLinesList(String str) {
        List<String> result = new TreeList<>(Arrays.asList(str.split("\n")));
        if (str.length() > 0 && str.charAt(str.length() -1) == '\n')
            result.add("");
        return result;
    }

    public void setEditable(boolean editable) { this.editable = editable; }

    /**
     * Edit text
     */

    public void addText(Point position, String text) {
        ListIterator<String> iter = lines.listIterator(position.y);
        List<String> newLines;
        if (iter.hasNext()) {
            String updatedLine = iter.next();
            newLines = buildLinesList(updatedLine.substring(0, position.x) + text + updatedLine.substring(position.x));
            iter.remove();
        } else
            newLines = buildLinesList(text);
        newLines.forEach(iter::add);
    }

    public void removeText(Point position, int length) {
        ListIterator<String> iter = lines.listIterator(position.y);
        StringBuilder affectedString = new StringBuilder();
        if (iter.hasNext()) {
            affectedString.append(iter.next());
            iter.remove();
            while (iter.hasNext() && position.x + length > affectedString.length()) {
                affectedString.append("\n").append(iter.next());
                iter.remove();
            }
        }
        String old = affectedString.toString();
        String updated = old.substring(0, position.x) + old.substring(position.x + length);
        buildLinesList(updated).forEach(iter::add);
    }

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

    private Point absoluteCoords(Point point) { return new Point(point.x * fontWidth(), point.y * fontHeight()); }

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
        for (String line : lines) {
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

    private int lastLine() { return lines.size() - 1; }

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

    private void addCaretMovementsActions() {
        Arrays.asList(CaretDirection.values()).forEach(
                caretDir -> bindKeyToAction(caretDir.getKeyCode(), new AbstractAction() {
                    @Override public void actionPerformed(ActionEvent e) { caret.move(caretDir); }
                }));

        bindKeyToAction(KeyEvent.VK_ENTER, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!caret.relativePosition.equals(new Point(lines.get(lastLine()).length(),lastLine()))) {
                    removeText(caret.positionAfterCaret(), 1);
                    repaint();
                }
            }
        });

        bindKeyToAction(KeyEvent.VK_BACK_SPACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!caret.relativePosition.equals(new Point(0,0))) {
                    caret.move(CaretDirection.LEFT);
                    removeText(caret.positionAfterCaret(), 1);
                    repaint();
                }
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

        Point positionBeforeCaret() { return horizontalMove(relativePosition, -1); }

        void renderCaret(Graphics g) {
            g.fillRect(caretRect().x, caretRect().y, caretRect().width, caretRect().height);
        }

        Point positionAfterCaret() { return relativePosition; }

        void move(CaretDirection direction) {
            Point updatedPosition = (Point) relativePosition.clone();
            switch (direction) {
                case UP:
                    updatedPosition = verticalMove(updatedPosition, -1); break;
                case DOWN:
                    updatedPosition = verticalMove(updatedPosition, 1); break;
                case LEFT:
                    updatedPosition = horizontalMove(updatedPosition, -1); break;
                case RIGHT:
                    updatedPosition = horizontalMove(updatedPosition, 1); break;
            }
            if (updatedPosition != relativePosition) {
                setRelativePosition(updatedPosition);
                scrollRectToVisible(caretRect());
                repaint();
            }
        }

        private Point verticalMove(Point position, int direction) {
            int newY = Math.min(Math.max(position.y + direction, 0), lastLine());
            if (newY == position.y) return position;
            int newX = Math.min(lines.get(newY).length(), position.x);
            return new Point(newX, newY);
        }

        private Point horizontalMove(Point position, int direction) {
            String currentLine = lines.get(position.y);
            int newY = position.y;
            int newX = position.x + direction;
            if (newX > currentLine.length()) {
                if (newY >= lastLine()) return position;
                else return new Point(0, position.y + 1);
            } else if (newX < 0) {
                if (newY == 0) return position;
                else {
                    newY--;
                    return new Point(lines.get(newY).length(), newY);
                }
            }
            return new Point(newX, newY);
        }
    }

    /**
     *
     */
}
