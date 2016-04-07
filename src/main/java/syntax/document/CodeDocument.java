package syntax.document;

import syntax.antlr.Lexeme;
import syntax.antlr.LexemeIndex;
import syntax.brackets.BracketIndex;

import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by avyatkin on 01/04/16.
 */
public class CodeDocument extends DefaultStyledDocument {
    public CodeDocument(List<SyntaxColorRule> colorRules,
                        LexemeIndex lexer,
                        List<Function<String, BracketIndex>> bracketIndexFactories) {
        this.colorRules = colorRules;
        this.bracketIndexFactories = bracketIndexFactories;
        this.lexer= lexer;
//        bracketIndexes = bracketIndexFactories.stream().map(f -> f.apply(allText())).collect(Collectors.toList());
    }

    LexemeIndex lexer;
    List<SyntaxColorRule> colorRules;
    List<Function<String, BracketIndex>> bracketIndexFactories;
//    List<BracketIndex> bracketIndexes;

    public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        colorizeText(lexer.addText(offset, str));
    }

    public void remove (int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        colorizeText(lexer.removeText(offs, len));
    }

    private static final Color DEFAULT_TOKEN_COLOR = Color.BLACK;

    private void colorizeText(List<Lexeme> updatedLexemes) {
        rebuildBracketIndexes();
        // todo: use existing coloring & maybe paint tokens only for what's currently displayed
        for (Lexeme lexeme : updatedLexemes) {
            Color tokenColor = DEFAULT_TOKEN_COLOR;
            for (SyntaxColorRule rule : colorRules) {
                Optional<Color> maybeColor = rule.getColor(lexeme.getType());
                if (maybeColor.isPresent()) {
                    tokenColor = maybeColor.get();
                    break;
                }
            }
            setCharacterAttributes(lexeme.getOffset(), lexeme.getSize(), colorAttr(tokenColor), false);
        }
    }

//  todo: use single bracket highlighting index instead of all this dances around lists
//    public BracketHighlighting getBracketHighlighting(int caretPosition) {
//        return bracketIndexes.stream().map(idx -> idx.getHighlighting(caretPosition)).
//                reduce((hl1, hl2) -> {
//                    hl1.getBrokenBraces().addAll(hl2.getBrokenBraces());
//                    hl1.getWorkingBraces().addAll(hl2.getWorkingBraces());
//                    return hl1;
//                }).get();
//    }

    private static final AttributeSet colorAttr(Color color) {
        StyleContext cont = StyleContext.getDefaultStyleContext();
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, color);
    }

//  todo: use single bracket highlighting index instead of all this dances around lists
//  todo: use rebuild in index instead of factories stuff
    private void rebuildBracketIndexes() {
//        bracketIndexes = bracketIndexFactories.stream().
//                map(f -> f.apply(allText())).collect(Collectors.toList());
    }

    private String allText() {
        try {
            return getText(0, getLength());
        } catch (BadLocationException e) {
            return "";
        }
    }
}
