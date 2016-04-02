import languages.BracketIndex;
import languages.java.JavaDocument;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.io.*;
import java.awt.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class SimpleFileEditor extends JPanel {

    JTextPane editableArea;

    private static final JTextPane initEditableArea() {
        final Dimension TEXT_AREA_SIZE = new Dimension(40, 40);
        final Insets TEXT_AREA_MARGIN = new Insets(5,5,5,5);
        JTextPane editableArea = new JTextPane(new JavaDocument());
        editableArea.setMargin(TEXT_AREA_MARGIN);
        return editableArea;
    }

    JScrollPane editScrollPane;
    JFileChooser fc;

    JButton openButton;
    JButton saveButton;

    private static final JButton openingButton(SimpleFileEditor fileEditor, JFileChooser fileChooser, JTextPane editableArea) {
        JButton openButton = new JButton("Open a File...");
        openButton.addActionListener((event) -> {
            int returnVal = fileChooser.showOpenDialog(fileEditor);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    Scanner scanner = new Scanner(file);
                    scanner.useDelimiter("\\n");
                    StringBuilder text = new StringBuilder();
                    while (scanner.hasNext()) {
                        text.append(scanner.next());
                        if (scanner.hasNext())
                            text.append("\n");
                    }
                    editableArea.setText(text.toString());
                } catch (FileNotFoundException e) {
                    editableArea.setText(e.getMessage());
                    editableArea.setEditable(false);
                }

            }
        });
        return openButton;
    }

    private static final JButton savingButton(SimpleFileEditor fileEditor, JFileChooser fileChooser, JTextPane editableArea) {
        JButton saveButton = new JButton("Save changes");
        saveButton.addActionListener((event) -> {
            int returnVal = fileChooser.showSaveDialog(fileEditor);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    PrintWriter writer = new PrintWriter(file);
                    writer.write(editableArea.getText());
                    writer.flush();
                } catch (FileNotFoundException e) {
                    editableArea.setText(e.getMessage());
                    editableArea.setEditable(false);
                }
            }
            editableArea.setCaretPosition(editableArea.getDocument().getLength());
        });
        return saveButton;

    }

    public SimpleFileEditor() {
        super(new BorderLayout());
        fc = new JFileChooser();
        editableArea = initEditableArea();
        saveButton = savingButton(this, fc, editableArea);
        openButton = openingButton(this, fc, editableArea);
        editScrollPane = new JScrollPane(editableArea);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.PAGE_START);
        add(editScrollPane, BorderLayout.CENTER);
        editableArea.setText(readDefaultFile());

        editableArea.addCaretListener(caretEvent -> {
            JavaDocument javaDocument = (JavaDocument)editableArea.getDocument();
            BracketIndex.BracketHighlighting highlighting = javaDocument.getBracketHighlighting(caretEvent.getDot());

            Highlighter.HighlightPainter errorBracketsPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            Highlighter.HighlightPainter correctBracketsPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

            Highlighter highlighter = editableArea.getHighlighter();
            highlighter.removeAllHighlights();
            try {
                for (int workingBrace : highlighting.getWorkingBraces())
                    highlighter.addHighlight(workingBrace, workingBrace + 1, correctBracketsPainter);
                for (int brokenBrace : highlighting.getBrokenBraces())
                    highlighter.addHighlight(brokenBrace, brokenBrace + 1, errorBracketsPainter);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        });
    }

    private String readDefaultFile() {
        String path = "/Users/avyatkin/Code/fun/oracle_swing_tutorial/src/main/java/languages/java/JavaDocument.java";
        try {
            Scanner scanner = new Scanner(new File(path));
            scanner.useDelimiter("\\n");
            StringBuilder text = new StringBuilder();
            while (scanner.hasNext()) {
                text.append(scanner.next());
                if (scanner.hasNext())
                    text.append("\n");
            }
            return text.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SimpleFileEditor());
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
}