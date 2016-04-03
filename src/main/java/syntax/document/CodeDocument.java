package syntax.document;

import org.antlr.v4.runtime.Token;
import syntax.antlr.LexerWrapper;
import syntax.brackets.BracketHighlighting;
import syntax.brackets.BracketIndex;

import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by avyatkin on 01/04/16.
 */
public class CodeDocument extends DefaultStyledDocument {
    public CodeDocument(List<SyntaxColorRule> colorRules,
                        Function<String, LexerWrapper> lexerFactory,
                        List<Function<String, BracketIndex>> bracketIndexFactories) {
        this.colorRules = colorRules;
        this.bracketIndexFactories = bracketIndexFactories;
        this.lexerFactory = lexerFactory;
        bracketIndexes = bracketIndexFactories.stream().map(f -> f.apply(allText())).collect(Collectors.toList());
    }

    Function<String, LexerWrapper> lexerFactory;
    List<SyntaxColorRule> colorRules;
    List<Function<String, BracketIndex>> bracketIndexFactories;
    List<BracketIndex> bracketIndexes;

    public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        colorizeText();
    }

    public void remove (int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        colorizeText();
    }

    private static final Color DEFAULT_TOKEN_COLOR = Color.BLACK;

    private void colorizeText() {
        rebuildBracketIndexes();
        LexerWrapper lexer = lexerFactory.apply(allText());
        // todo: use existing coloring & maybe paint tokens only for what's currently displayed
        for (Token t : lexer.tokens()) {
            Color tokenColor = DEFAULT_TOKEN_COLOR;
            String tokenType = lexer.getTokenType(t);
            for (SyntaxColorRule rule : colorRules) {
                Optional<Color> maybeColor = rule.getColor(tokenType);
                if (maybeColor.isPresent()) {
                    tokenColor = maybeColor.get();
                    break;
                }
            }
            setCharacterAttributes(t.getStartIndex(), t.getText().length(), colorAttr(tokenColor), false);
        }
    }

//  todo: use single bracket highlighting index instead of all this dances around lists
    public BracketHighlighting getBracketHighlighting(int caretPosition) {
        return bracketIndexes.stream().map(idx -> idx.getHighlighting(caretPosition)).
                reduce((hl1, hl2) -> {
                    hl1.getBrokenBraces().addAll(hl2.getBrokenBraces());
                    hl1.getWorkingBraces().addAll(hl2.getWorkingBraces());
                    return hl1;
                }).get();
    }

    private static final AttributeSet colorAttr(Color color) {
        StyleContext cont = StyleContext.getDefaultStyleContext();
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, color);
    }

//  todo: use single bracket highlighting index instead of all this dances around lists
//  todo: use rebuild in index instead of factories stuff
    private void rebuildBracketIndexes() {
        bracketIndexes = bracketIndexFactories.stream().
                map(f -> f.apply(allText())).collect(Collectors.toList());
    }

    private String allText() {
        try {
            return getText(0, getLength());
        } catch (BadLocationException e) {
            return "";
        }
    }
}
