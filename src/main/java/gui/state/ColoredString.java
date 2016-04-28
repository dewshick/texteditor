package gui.state;

import org.apache.commons.collections4.list.TreeList;
import syntax.antlr.Lexeme;
import syntax.document.SyntaxColoring;

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

    public static List<ColoredString> splitLexeme(Lexeme lexeme, SyntaxColoring coloring) {
        int lexemePartIndex = 0;
        List<ColoredString> result = new TreeList<>();
        Color lexemeColor = coloring.getColor(lexeme);
        for (String lexemePart : EditorTextStorage.buildLinesList(lexeme.getText(), true)) {
            result.add(new ColoredString(lexemePart, lexemeColor, lexemePartIndex));
            lexemePartIndex++;
        }
        return result;
    }
}
