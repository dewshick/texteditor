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
public class EditorTextBox extends JComponent // implements Scrollable, Accessible
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

    private static Font codeFont = new Font("Monospaced", Font.BOLD | Font.ITALIC, 36);

    public void paint(Graphics g) {
        super.paintComponent(g);
        Rectangle clip = g.getClipBounds();
        int xOffset = 0;
        int yOffset = 0;
        for (String line: lines) {
            g.drawString(line, xOffset, yOffset);
            yOffset += 20;
//            xOffset += 10;
            if (xOffset > clip.height)
                break;
        }

    }

    public Dimension getPreferredSize() {
        return new Dimension(400,400);
    }
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}

//class TestFrame extends JFrame {
//    public static void main( String args[] ) {
//        JPanel buttonPanel = new JPanel();
//        buttonPanel.add(new JButton("11"));
//        buttonPanel.add(new JButton("12"));
//
//        TestFrame mainFrame = new TestFrame();
//        mainFrame.add(new EditorTextBox());
//        mainFrame.pack();
//        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        mainFrame.setVisible( true );
//    }
//}