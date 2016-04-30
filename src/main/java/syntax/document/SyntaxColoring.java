package syntax.document;

import gui.state.ColoredString;
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

    public List<ColoredString> colorizeLexemes(List<Lexeme> lexemes) {
        return lexemes.stream().map(this::colorizeLexeme).collect(Collectors.toList());
    }

    public ColoredString colorizeLexeme(Lexeme lexeme) {
        return new ColoredString(lexeme.getText(), getColor(lexeme), lexeme.getType());
    }

    Color getColor(Lexeme lexeme) {
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
