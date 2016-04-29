package syntax.document;

import gui.state.ColoredString;
import gui.state.EditorTextStorage;
import org.apache.commons.collections4.list.TreeList;
import syntax.antlr.Lexeme;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by avyatkin on 23/04/16.
 */
public class SyntaxColoring {

    List<SyntaxColorRule> colorRules;
    Color defaultColor;

    public SyntaxColoring(List<SyntaxColorRule> colorRules1, Color defaultColor1) {
        colorRules = colorRules1;
        defaultColor = defaultColor1;
    }

    public List<ColoredString> splitInColoredLines(Lexeme lexeme) {
        int lexemePartIndex = 0;
        List<ColoredString> result = new TreeList<>();
        Color lexemeColor = getColor(lexeme);
        for (String lexemePart : EditorTextStorage.buildLinesList(lexeme.getText(), true)) {
            result.add(new ColoredString(lexemePart, lexemeColor, lexemePartIndex, lexeme.getType()));
            lexemePartIndex++;
        }
        return result;
    }

    public Color getColor(Lexeme lexeme) {
        Color tokenColor = defaultColor;
        for (SyntaxColorRule rule : colorRules) {
            Optional<Color> maybeColor = rule.getColor(lexeme.getType());
            if (maybeColor.isPresent()) {
                tokenColor = maybeColor.get();
                break;
            }
        }
        return tokenColor;
    }
}
