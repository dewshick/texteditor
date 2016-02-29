import java.io.*;
import java.awt.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class SimpleFileEditor extends JPanel {

    JTextArea editableArea;

    private final JTextArea initEditableArea() {
        final Dimension TEXT_AREA_SIZE = new Dimension(40, 40);
        final Insets TEXT_AREA_MARGIN = new Insets(5,5,5,5);
        JTextArea editableArea = new JTextArea(TEXT_AREA_SIZE.height, TEXT_AREA_SIZE.width);
        editableArea.setMargin(TEXT_AREA_MARGIN);
        return editableArea;
    }

    JScrollPane editScrollPane;
    JFileChooser fc = new JFileChooser();

    JButton openButton;
    JButton saveButton;

    private final JButton openingButton() {
        JButton openButton = new JButton("Open a File...");
        openButton.addActionListener((event) -> {
            int returnVal = fc.showOpenDialog(SimpleFileEditor.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
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

    private final JButton savingButton() {
        JButton saveButton = new JButton("Save changes");
        saveButton.addActionListener((event) -> {
            int returnVal = fc.showSaveDialog(SimpleFileEditor.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
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
        openButton = openingButton();
        saveButton = savingButton();
        editScrollPane = new JScrollPane(editableArea);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.PAGE_START);
        add(editScrollPane, BorderLayout.CENTER);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SimpleFileEditor());
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            createAndShowGUI();
        });
    }
}