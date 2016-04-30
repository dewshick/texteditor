import static org.junit.Assert.assertEquals;

import syntax.brackets.BracketHighlighting;
import syntax.brackets.BracketIndex;
import syntax.antlr.LexemeIndex;
import org.junit.Test;
import syntax.document.SupportedSyntax;

import java.awt.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by avyatkin on 02/04/16.
 */
public class BracketIndexTest {
    private BracketIndex bracketIndex(String code) {
        return new BracketIndex(SupportedSyntax.JAVA, new LexemeIndex(SupportedSyntax.JAVA, code));
    }

    BracketIndex correctBrackets = bracketIndex("()");
    BracketIndex openBracketsMismatch = bracketIndex("(()");
    BracketIndex closedBracketsMismatch = bracketIndex("())");
    BracketIndex closedBracketsMismatchRecovery = bracketIndex("())()");
    BracketIndex openBracketsMismatchRecovery = bracketIndex("()(()");

    private String getHighlighting(BracketIndex index, int coord) {
        BracketHighlighting result = index.getHighlighting(new Point(coord, 0));
        String workingBraces = result.getWorkingBraces().stream().map(p -> p.x + "").collect(Collectors.joining(", "));
        String brokenBraces = result.getBrokenBraces().stream().map(p -> p.x + "").collect(Collectors.joining(", "));
        return  "{workingBraces=[" + workingBraces + "], brokenBraces=[" + brokenBraces + "]}";
    }

    @Test public void caretBeforeCorrectBracketsTest() {
        assertEquals("{workingBraces=[0, 1], brokenBraces=[]}", getHighlighting(correctBrackets, 0));
    }

    @Test public void caretBetweenCorrectBracketsTest() {
        assertEquals("{workingBraces=[], brokenBraces=[]}", getHighlighting(correctBrackets, 1));
    }

    @Test public void caretAfterCorrectBracketsTest() {
        assertEquals("{workingBraces=[0, 1], brokenBraces=[]}", getHighlighting(correctBrackets, 2));
    }


    @Test public void openBracketsMismatchTest() {
        assertEquals("{workingBraces=[], brokenBraces=[0]}", getHighlighting(openBracketsMismatch, 0));
    }

    @Test public void openBracketsRecoveryTest() {
        assertEquals("{workingBraces=[1, 2], brokenBraces=[]}", getHighlighting(openBracketsMismatch, 1));
        assertEquals("{workingBraces=[], brokenBraces=[0]}", getHighlighting(openBracketsMismatch, 0));
    }

    @Test public void closedBracketsMismatchTest() {
        assertEquals("{workingBraces=[0, 1], brokenBraces=[]}", getHighlighting(closedBracketsMismatch, 2));
        assertEquals("{workingBraces=[], brokenBraces=[2]}", getHighlighting(closedBracketsMismatch, 3));
    }

    @Test public void closedBracketsMismatchRecoveryInTheMiddleTest() {
        assertEquals("{workingBraces=[3, 4], brokenBraces=[2]}", getHighlighting(closedBracketsMismatchRecovery, 3));
    }

    @Test public void openBracketsMismatchRecoveryTest() {
        assertEquals("{workingBraces=[3, 4], brokenBraces=[]}", getHighlighting(openBracketsMismatchRecovery, 3));
        assertEquals("{workingBraces=[0, 1], brokenBraces=[2]}", getHighlighting(openBracketsMismatchRecovery, 2));
    }

    @Test public void outOfBoundsTest() {
        assertEquals("{workingBraces=[], brokenBraces=[]}", getHighlighting(correctBrackets, 10));
    }
}
