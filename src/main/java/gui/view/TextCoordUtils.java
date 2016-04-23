package gui.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by avyatkin on 22/04/16.
 */

/**
 * Util class to convert between relative (character in text) & absolute(pixel) coords
 */

public class TextCoordUtils {
    public TextCoordUtils(JComponent component) {
        fontMetrics = component.getFontMetrics(FONT);
    }

    public static final Font FONT = new Font("Monospaced", Font.BOLD, 12);

    private FontMetrics fontMetrics;

    public FontMetrics fontMetrics() { return fontMetrics; }

    public int fontWidth() { return fontMetrics().charWidth(' '); }

    public int fontHeight() { return fontMetrics().getHeight(); }

    public Point relativeMousePosition(MouseEvent e) {
        Point clickCoords = e.getPoint();
        Point absoluteCoords = new Point(clickCoords.x + (fontWidth()/2), clickCoords.y);
        Point relativeCoords = new Point(absoluteCoords.x / fontWidth(), absoluteCoords.y / fontHeight());
        return relativeCoords;
    }

    public Rectangle relativeRectangle(Rectangle rect) {
        return new Rectangle(
                rect.x / fontWidth(),
                rect.y/ fontHeight(),
                rect.width / fontWidth(),
                rect.height / fontHeight());
    }

    public Point absoluteCoords(Point point) {
        return new Point(point.x * fontWidth(),
                point.y * fontHeight() + (fontHeight() - fontMetrics().getAscent()));
    }

    public Rectangle absoluteRectangle(Rectangle rect) {
        Point absoluteCoords = absoluteCoords(new Point(rect.x, rect.y));
        return new Rectangle(absoluteCoords.x, absoluteCoords.y, rect.width * fontWidth(), rect.height * fontHeight());
    }

    public String stringInRelativeBounds(String str, Rectangle bounds) {
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

    public Point absoluteTextCoords(Point relativePosition) {
        return new Point(relativePosition.x * fontWidth(),
                (relativePosition.y+1) * fontHeight());
    }

    public Dimension absoluteDimension(Dimension relativeSize) {
        return new Dimension(relativeSize.width * fontWidth(), relativeSize.height * fontHeight());
    }
}
