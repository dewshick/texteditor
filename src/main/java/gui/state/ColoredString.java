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
    String type;

    public ColoredString(String content, Color color, int indexInLexeme, String type) {
        this.type = type;
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

    public int getSize() { return content.length(); }

    public int getIndexInLexeme() {
        return indexInLexeme;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ColoredString{" +
                "content='" + content + '\'' +
                ", color=" + color +
                ", indexInLexeme=" + indexInLexeme +
                ", type='" + type + '\'' +
                '}';
    }
}
