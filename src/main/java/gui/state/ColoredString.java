package gui.state;

import gui.view.EditorColors;
import syntax.document.SupportedSyntax;

import java.util.ArrayList;
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
    String type;

    public String getType() {
        return type;
    }

    public ColoredString(String content, Color color, String type) {
        this.content = content;
        this.color = color;
        this.type = type;
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

    @Deprecated
    public List<ColoredString> splitByLines() {
        return EditorTextStorage.buildLinesList(content, true).stream().
                map(str -> new ColoredString(str, color, type)).collect(Collectors.toList());
    }

    public List<ColoredString> splitAt(int at) {
        ArrayList<ColoredString> results = new ArrayList<>(2);
        results.add(new ColoredString(content.substring(0, at), color, type));
        results.add(new ColoredString(content.substring(at), color, type));
        return results;
    }

    public static List<ColoredString> stubStrings(String text) {
        return new ColoredString(text, EditorColors.TEXT, SupportedSyntax.STUB_ID).splitByLines();
    }
}
