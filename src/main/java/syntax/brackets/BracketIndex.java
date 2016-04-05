package syntax.brackets;
import org.antlr.v4.runtime.Token;
import syntax.antlr.Lexeme;
import syntax.antlr.LexerWrapper;
import syntax.document.SupportedSyntax;

import java.util.*;

/**
 * Created by avyatkin on 01/04/16.
 */
// currently we highlight only single brackets
// get rid of boxed lists since it's overkill for memory
public class BracketIndex {
    public BracketIndex(String openToken, String closeToken, LexerWrapper lexer) {
        brokenBracesIndex = new HashMap<>();
        correctBracesIndex = new HashMap<>();

        Stack<Lexeme> braces = new Stack<>();
        for(Lexeme lexeme : lexer.lexemes()) {
            String tokenType = lexeme.getType();
            if (tokenType.equals(openToken)) {
                braces.push(lexeme);
            } else if (tokenType.equals(closeToken)) {
                int caretHighlightPosition = lexeme.getStopIndex() + 1;
                if (braces.empty()) { addBracesToIndex(brokenBracesIndex, caretHighlightPosition, lexeme); }
                else {
                    Lexeme openingBrace = braces.pop();
                    addBracesToIndex(correctBracesIndex, caretHighlightPosition, openingBrace, lexeme);
                    addBracesToIndex(correctBracesIndex, openingBrace.getOffset(), openingBrace, lexeme);
                }
            }
        }
        braces.forEach(brace -> addBracesToIndex(brokenBracesIndex, brace.getOffset(), brace));
    }

    private void addBracesToIndex(Map<Integer, List<Integer>> index, int position, Lexeme ... braces) {
        List<Integer> presentBraces = index.getOrDefault(position, new ArrayList<>());
        for (Lexeme brace : braces) { presentBraces.add(brace.getOffset()); }
        index.put(position, presentBraces);
    }

    public BracketHighlighting getHighlighting(int caretPosition) {
        return new BracketHighlighting(correctBracesIndex.get(caretPosition), brokenBracesIndex.get(caretPosition));
    }

    HashMap<Integer, List<Integer>> brokenBracesIndex;
    HashMap<Integer, List<Integer>> correctBracesIndex;

    public static BracketIndex forJavaBraces(String input) {
        return new BracketIndex("'('", "')'", new LexerWrapper(SupportedSyntax.JAVA, input));
    }
}

