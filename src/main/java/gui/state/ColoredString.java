package gui.state;

import org.apache.commons.collections4.list.TreeList;

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
    int indexInLexeme;

    public ColoredString(String content, Color color) {
        this(content, color, 0);
    }

    private ColoredString(String content, Color color, int indexInLexeme) {
        this.content = content;
        this.color = color;
        this.indexInLexeme = indexInLexeme;
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
        int lexemePartIndex = 0;
        List<ColoredString> result = new TreeList<>();
        for (String lexemePart : EditorTextStorage.buildLinesList(content, true)) {
            result.add(new ColoredString(lexemePart, color, lexemePartIndex));
            lexemePartIndex++;
        }
        return result;
    }
}
