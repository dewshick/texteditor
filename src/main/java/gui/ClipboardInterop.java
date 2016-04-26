package gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by avyatkin on 22/04/16.
 */
public class ClipboardInterop {
    static Optional<String> paste() {
//            are plain retries suitable for this?
        int retries = 100;
        for (int i = 0; i < retries; i++) {
            try {
                return Optional.of((String) defaultClipboard().getData(DataFlavor.stringFlavor));
            } catch (UnsupportedFlavorException e) {
                return Optional.empty();
            } catch (IOException | IllegalStateException e) {}
        }
        return Optional.empty();
    }

    static void copy(String str) {
        int retries = 100;
        for (int i = 0; i < retries; i++) {
            try {
                defaultClipboard().setContents(new StringSelection(str), null);
            } catch (IllegalStateException e) {}
        }
    }

    static Clipboard defaultClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }
}
