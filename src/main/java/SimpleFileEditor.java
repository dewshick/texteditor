import gui.EditorComponent;
import syntax.document.SupportedSyntax;

import java.io.*;
import java.awt.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class SimpleFileEditor extends JPanel {

    EditorComponent editableArea;

    private static final EditorComponent initEditableArea() {
        EditorComponent editableArea = new EditorComponent(SupportedSyntax.ECMASCRIPT);
        return editableArea;
    }

    JScrollPane editScrollPane;
    JFileChooser fc;

    JButton openButton;
    JButton saveButton;

    private static final JButton openingButton(SimpleFileEditor fileEditor, JFileChooser fileChooser, EditorComponent editableArea) {
        JButton openButton = new JButton("Open a File...");
        openButton.addActionListener((event) -> {
            int returnVal = fileChooser.showOpenDialog(fileEditor);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                fileEditor.setAppropriateSyntax(file);
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
                    editableArea.repaint();
                } catch (FileNotFoundException e) {
                    editableArea.setText(e.getMessage());
                }

            }
        });
        return openButton;
    }

    private void setAppropriateSyntax(File f) {
        if (f.getName().endsWith(".js"))
            editableArea.changeSyntax(SupportedSyntax.ECMASCRIPT);
        else
            editableArea.changeSyntax(SupportedSyntax.JAVA);
    }

    private static final JButton savingButton(SimpleFileEditor fileEditor, JFileChooser fileChooser, EditorComponent editableArea) {
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
        editScrollPane = new JScrollPane(editableArea);
        editableArea.setScrollPane(editScrollPane);

        saveButton = savingButton(this, fc, editableArea);
        openButton = openingButton(this, fc, editableArea);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.PAGE_START);
        add(editScrollPane, BorderLayout.CENTER);
//        editableArea.setText(readDefaultFile());
    }

    private String readDefaultFile() {
//        String path = "/Users/avyatkin/Desktop/big_src.java";
//        String path = "/Users/avyatkin/Desktop/ajax.js";
        String path = "/Users/avyatkin/Desktop/jquery-1.12.2.js";
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