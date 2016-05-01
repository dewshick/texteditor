import gui.EditorComponent;
import syntax.document.SupportedSyntax;

import java.io.*;
import java.awt.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class SimpleCodeEditor extends JPanel {

    EditorComponent editableArea;

    private static final EditorComponent initEditableArea() {
        EditorComponent editableArea = new EditorComponent(SupportedSyntax.ECMASCRIPT);
        return editableArea;
    }

    JScrollPane editScrollPane;
    JFileChooser fc;

    JButton openButton;
    JButton saveButton;

    private static final JButton openingButton(SimpleCodeEditor fileEditor, JFileChooser fileChooser, EditorComponent editableArea) {
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

    private static final JButton savingButton(SimpleCodeEditor fileEditor, JFileChooser fileChooser, EditorComponent editableArea) {
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
        });
        return saveButton;

    }

    public SimpleCodeEditor() {
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
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Obvious idea");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SimpleCodeEditor());
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpleCodeEditor::createAndShowGUI);
    }
}