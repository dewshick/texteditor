package gui.view;

import syntax.document.SupportedSyntax;
import syntax.document.SyntaxColorRule;
import syntax.document.SyntaxColoring;
import java.awt.*;
import java.util.*;

/**
 * Created by avyatkin on 23/04/16.
 */
public class EditorColors {
    public static final Color BACKGROUND = Color.WHITE;
    public static final Color CARET = Color.BLACK;
    public static final Color INSERT_CARET = Color.BLACK;
    public static final Color TEXT_OVER_INSERT_CARET = Color.GREEN;
    public static final Color SELECTION = Color.GREEN;

    public static final Color TEXT_OVER_BROKEN_BRACKET = Color.RED;
    public static final Color TEXT_OVER_WORKING_BRACKET = Color.YELLOW;
    public static final Color BRACKET_BACKGROUND = Color.CYAN;

    public static final Color TEXT = Color.BLACK;
    public static final Color KEYWORD = Color.BLUE;
    public static final Color IDENTIFIER = Color.ORANGE;
    public static final Color COMMENT = Color.LIGHT_GRAY;

    public static SyntaxColoring forSyntax(SupportedSyntax syntax) {
        return coloringForLanguage(syntax.keywords(), syntax.identifier(), syntax.comments());
    }


    private static SyntaxColoring coloringForLanguage(String[] keywordTokens,
                                                            String identifier,
                                                            String[] commentTokens) {
        java.util.List<SyntaxColorRule> colorRules = Arrays.asList(
                new SyntaxColorRule(KEYWORD, keywordTokens),
                new SyntaxColorRule(IDENTIFIER, identifier),
                new SyntaxColorRule(COMMENT, commentTokens));
        return new SyntaxColoring(colorRules, TEXT);
    }
}
