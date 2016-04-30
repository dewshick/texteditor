package syntax.brackets;
import com.sun.tools.javac.util.Pair;
import gui.state.ColoredString;
import syntax.antlr.Lexeme;
import syntax.antlr.LexemeIndex;
import syntax.document.SupportedSyntax;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by avyatkin on 01/04/16.
 */
// currently we highlight only single brackets
// get rid of boxed lists since it's overkill for memory
public class BracketIndex {
    public BracketIndex(SupportedSyntax syntax, LexemeIndex lexer) {
        List<Pair<String, String>> bracketPairs = syntax.bracketPairs();
        brokenBracesForCaret = new HashMap<>();
        correctBracesForCaret = new HashMap<>();

        HashMap<Pair<String, String>, Stack<Point>> bracesStacks = new HashMap<>();
        for (Pair<String, String> brackets : bracketPairs)
            bracesStacks.put(brackets, new Stack<>());

        int yCoord = 0;
        for(List<ColoredString> coloredStrings : lexer.getColoredLines()) {
            int xCoord = 0;
            for (ColoredString str : coloredStrings) {
                Point currentCoords = new Point(xCoord, yCoord);
                for (Pair<String, String> brackets : bracketPairs) {

                    Stack<Point> currentStack = bracesStacks.get(brackets);
                    String tokenType = str.getType();
                    if (tokenType.equals(brackets.fst)) {
                        currentStack.push(currentCoords);
                        break;
                    } else if (tokenType.equals(brackets.snd)) {
                        Point nextCaretCoords = new Point(xCoord + 1, yCoord);
                        if (currentStack.empty()) {
                            addBracesToIndex(brokenBracesForCaret, nextCaretCoords, currentCoords);
                        } else {
                            Point openingBrace = currentStack.pop();
                            addBracesToIndex(correctBracesForCaret, nextCaretCoords, openingBrace, currentCoords);
                            addBracesToIndex(correctBracesForCaret, openingBrace, openingBrace, currentCoords);
                        }
                        break;
                    }
                }
                xCoord += str.getContent().length();
            }
            yCoord++;
        }
        for(Stack<Point> braces: bracesStacks.values())
            braces.forEach(brace -> addBracesToIndex(brokenBracesForCaret, brace, brace));
    }

    private void addBracesToIndex(Map<Point, List<Point>> index, Point position, Point ... braces) {
        List<Point> presentBraces = index.getOrDefault(position, new ArrayList<>());
        for (Point brace : braces) { presentBraces.add(brace); }
        index.put(position, presentBraces);
    }

    public BracketHighlighting getHighlighting(Point caretPosition) {
        return new BracketHighlighting(correctBracesForCaret.get(caretPosition), brokenBracesForCaret.get(caretPosition));
    }

    HashMap<Point, List<Point>> brokenBracesForCaret;
    HashMap<Point, List<Point>> correctBracesForCaret;

    class ColoredStringWithCoords {
        ColoredString string;

        public Point getCoords() {
            return coords;
        }

        public ColoredString getString() {
            return string;
        }

        Point coords;

        public ColoredStringWithCoords(ColoredString string, Point coords) {
            this.string = string;
            this.coords = coords;
        }
    }
}

