package gui.state;

import java.util.List;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.stream.Collectors;

/**
 * Created by avyatkin on 23/04/16.
 */
public class ColoredString {
    String content;
    Color color;

    public ColoredString(String content, Color color) {
        this.content = content;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getContent() {
        return content;
    }

    public boolean containsNewlines() {
        return content.contains("\n");
    }

    public List<ColoredString> splitByLines() {
        return EditorTextStorage.buildLinesList(content).stream().
                map(str -> new ColoredString(str, color)).collect(Collectors.toList());
    }
}