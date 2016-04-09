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
public class EditorTextBox extends JComponent //implements Scrollable // , Accessible
{
    String text;
    List<String> lines;

    boolean editable;

    public EditorTextBox(Document doc) {
        text = "";
        lines = new TreeList<>();
        editable = true;
    }

    public String getText() { return text; }

    public void setText(String text) {
        this.text = text;
        this.lines = new TreeList<>(Arrays.asList(text.split("\n")));
    }

    public boolean isEditable() { return editable; }

    public void setEditable(boolean editable) { this.editable = editable; }

//    private static Font codeFont = new Font("Monospaced", Font.BOLD | Font.ITALIC, 36);

    public void paint(Graphics g) {
        super.paintComponent(g);
        Rectangle clip = g.getClipBounds();
        int xOffset = 0;
        int yOffset = 0;
        for (String line: lines) {
            g.drawString(line, xOffset, yOffset);
            yOffset += 20;
            if (xOffset > clip.height)
                break;
        }

    }


    static final Dimension DEFAULT_SIZE = new Dimension(400,400);

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d = (d == null) ? DEFAULT_SIZE : d;
        return d;
    }


//    @Override
//    public Dimension getPreferredScrollableViewportSize() {
//        return null;
//    }
//
//    @Override
//    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
//        return 0;
//    }
//
//    @Override
//    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
//        return 0;
//    }
//
//    @Override
//    public boolean getScrollableTracksViewportWidth() {
//        return false;
//    }
//
//    @Override
//    public boolean getScrollableTracksViewportHeight() {
//        return false;
//    }
}
