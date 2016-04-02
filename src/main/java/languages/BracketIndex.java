package languages;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

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

        Stack<Token> braces = new Stack<>();
        for(Token currentToken : lexer.tokens()) {
            String tokenType = lexer.getTokenType(currentToken);
            if (tokenType.equals(openToken)) {
                braces.push(currentToken);
            } else if (tokenType.equals(closeToken)) {
                int caretHighlightPosition = currentToken.getStopIndex() + 1;
                if (braces.empty()) { addBracesToIndex(brokenBracesIndex, caretHighlightPosition, currentToken); }
                else {
                    Token openingBrace = braces.pop();
                    addBracesToIndex(correctBracesIndex, caretHighlightPosition, openingBrace, currentToken);
                    addBracesToIndex(correctBracesIndex, openingBrace.getStartIndex(), openingBrace, currentToken);
                }
            }
        }
        while (!braces.empty()) {
            Token currentToken = braces.pop();
            addBracesToIndex(brokenBracesIndex, currentToken.getStartIndex(), currentToken);
        }
    }

    private void addBracesToIndex(Map<Integer, List<Integer>> index, int position, Token ... braces) {
        List<Integer> presentBraces = index.getOrDefault(position, new ArrayList<>());
        for (Token brace : braces) { presentBraces.add(brace.getStartIndex()); }
        index.put(position, presentBraces);
    }

    public BracketHighlighting getHighlighting(int caretPosition) {
        return new BracketHighlighting(correctBracesIndex.get(caretPosition), brokenBracesIndex.get(caretPosition));
    }

    HashMap<Integer, List<Integer>> brokenBracesIndex;
    HashMap<Integer, List<Integer>> correctBracesIndex;

    public class BracketHighlighting {
        @Override
        public String toString() {
            return "{" + "workingBraces=" + workingBraces + ", brokenBraces=" + brokenBraces + '}';
        }

        public BracketHighlighting(List<Integer> workingBraces, List<Integer> brokenBraces) {
            if (workingBraces == null)
                workingBraces = new ArrayList<>(0);
            if (brokenBraces == null)
                brokenBraces = new ArrayList<>(0);

            this.workingBraces = workingBraces;
            this.brokenBraces = brokenBraces;
        }

        List<Integer> workingBraces;
        List<Integer> brokenBraces;
    }

}
