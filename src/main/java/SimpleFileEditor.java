import gui.EditorTextBox;
import syntax.document.CodeDocumentFactory;
import java.io.*;
import java.awt.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.text.Highlighter;

public class SimpleFileEditor extends JPanel {

    EditorTextBox editableArea;

    private static final EditorTextBox initEditableArea() {
        final Insets TEXT_AREA_MARGIN = new Insets(5,5,5,5);
        EditorTextBox editableArea = new EditorTextBox(CodeDocumentFactory.forJs());
//        editableArea.setMargin(TEXT_AREA_MARGIN);
        return editableArea;
    }

    JScrollPane editScrollPane;
    JFileChooser fc;

    JButton openButton;
    JButton saveButton;

    private static final JButton openingButton(SimpleFileEditor fileEditor, JFileChooser fileChooser, EditorTextBox editableArea) {
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
                    editableArea.getTextStorage().setText(text.toString());
                } catch (FileNotFoundException e) {
                    editableArea.getTextStorage().setText(e.getMessage());
                    editableArea.setEditable(false);
                }

            }
        });
        return openButton;
    }

    private static final JButton savingButton(SimpleFileEditor fileEditor, JFileChooser fileChooser, EditorTextBox editableArea) {
        JButton saveButton = new JButton("Save changes");
        saveButton.addActionListener((event) -> {
            int returnVal = fileChooser.showSaveDialog(fileEditor);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    PrintWriter writer = new PrintWriter(file);
                    writer.write(editableArea.getTextStorage().getText());
                    writer.flush();
                } catch (FileNotFoundException e) {
                    editableArea.getTextStorage().setText(e.getMessage());
                    editableArea.setEditable(false);
                }
            }
//            editableArea.setCaretPosition(editableArea.getDocument().getLength());
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
        editableArea.getTextStorage().setText(readDefaultFile());
//        editableArea.addCaretListener(caretEvent -> {
//            CodeDocument codeDocument = (CodeDocument)editableArea.getDocument();
//            BracketHighlighting highlighting = codeDocument.getBracketHighlighting(caretEvent.getDot());
//
//            Highlighter.HighlightPainter errorBracketsPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
//            Highlighter.HighlightPainter correctBracketsPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
//
//            Highlighter highlighter = editableArea.getHighlighter();
//            highlighter.removeAllHighlights();
//            try {
//                for (int workingBrace : highlighting.getWorkingBraces())
//                    highlighter.addHighlight(workingBrace, workingBrace + 1, correctBracketsPainter);
//                for (int brokenBrace : highlighting.getBrokenBraces())
//                    highlighter.addHighlight(brokenBrace, brokenBrace + 1, errorBracketsPainter);
//            } catch (BadLocationException e1) {
//                e1.printStackTrace();
//            }
//        });
    }

    private String readDefaultFile() {
        String path = "/Users/avyatkin/Desktop/small_src.java";
//        String path = "/Users/avyatkin/Desktop/ajax.js";
//        String path = "/Users/avyatkin/Desktop/jquery-1.12.2.js";
//        String path = "/Users/avyatkin/Code/fun/oracle_swing_tutorial/src/main/java/syntax/document/CodeDocumentFactory.java";
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