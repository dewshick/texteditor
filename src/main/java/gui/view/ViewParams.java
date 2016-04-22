package gui.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by avyatkin on 22/04/16.
 */
public class ViewParams {
    public ViewParams(JComponent component) {
        fontMetrics = component.getFontMetrics(FONT);
    }

    public static final Font FONT = new Font("Monospaced", Font.BOLD, 12);

    private FontMetrics fontMetrics;

    public FontMetrics fontMetrics() { return fontMetrics; }

    public int fontWidth() { return fontMetrics().charWidth(' '); }

    public int fontHeight() { return fontMetrics().getHeight(); }

    public Point getRelativeMousePosition(MouseEvent e) {
        Point clickCoords = e.getPoint();
        Point absoluteCoords = new Point(clickCoords.x + (fontWidth()/2), clickCoords.y);
        Point relativeCoords = new Point(absoluteCoords.x / fontWidth(), absoluteCoords.y / fontHeight());
        return relativeCoords;
    }
}
