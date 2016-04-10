package gui;

import org.apache.commons.collections4.list.TreeList;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by avyatkin on 06/04/16.
 */
public class EditorTextBox extends JComponent implements Scrollable // , Accessible
{
    String text;
    List<String> lines;
    boolean editable;

    public EditorTextBox(Document doc) {
        text = "";
        lines = new TreeList<>();
        editable = true;
        updatePreferredSize();
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

    private void updatePreferredSize() {
        int width = lines.stream().map(this::stringWidth).reduce(0, Integer::max);
        int height = lines.size() * fontHeight();
        setPreferredSize(new Dimension(width, height));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(FONT);
        g.setColor(Color.blue);
        g.fillRect(0,0,100,100);
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
     *
     *
     */
}
