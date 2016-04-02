import static org.junit.Assert.assertEquals;
import languages.BracketIndex;
import languages.LexerWrapper;
import org.junit.Test;

/**
 * Created by avyatkin on 02/04/16.
 */
public class BracketIndexTest {
    private BracketIndex bracketIndex(String code) {
        return new BracketIndex("'('", "')'", LexerWrapper.javaLexer(code));
    }

    BracketIndex correctBrackets = bracketIndex("()");
    BracketIndex openBracketsMismatch = bracketIndex("(()");
    BracketIndex closedBracketsMismatch = bracketIndex("())");
    BracketIndex closedBracketsMismatchRecovery = bracketIndex("())()");
    BracketIndex openBracketsMismatchRecovery = bracketIndex("()(()");

    @Test public void caretBeforeCorrectBracketsTest() {
        assertEquals("{workingBraces=[0, 1], brokenBraces=[]}", correctBrackets.getHighlighting(0).toString());
    }

    @Test public void caretBetweenCorrectBracketsTest() {
        assertEquals("{workingBraces=[], brokenBraces=[]}", correctBrackets.getHighlighting(1).toString());
    }

    @Test public void caretAfterCorrectBracketsTest() {
        assertEquals("{workingBraces=[0, 1], brokenBraces=[]}", correctBrackets.getHighlighting(2).toString());
    }


    @Test public void openBracketsMismatchTest() {
        assertEquals("{workingBraces=[], brokenBraces=[0]}", openBracketsMismatch.getHighlighting(0).toString());
    }

    @Test public void openBracketsRecoveryTest() {
        assertEquals("{workingBraces=[1, 2], brokenBraces=[]}", openBracketsMismatch.getHighlighting(1).toString());
        assertEquals("{workingBraces=[], brokenBraces=[0]}", openBracketsMismatch.getHighlighting(0).toString());
    }

    @Test public void closedBracketsMismatchTest() {
        assertEquals("{workingBraces=[0, 1], brokenBraces=[]}", closedBracketsMismatch.getHighlighting(2).toString());
        assertEquals("{workingBraces=[], brokenBraces=[2]}", closedBracketsMismatch.getHighlighting(3).toString());
    }

    @Test public void closedBracketsMismatchRecoveryInTheMiddleTest() {
        assertEquals("{workingBraces=[3, 4], brokenBraces=[2]}", closedBracketsMismatchRecovery.getHighlighting(3).toString());
    }

    @Test public void openBracketsMismatchRecoveryTest() {
        assertEquals("{workingBraces=[3, 4], brokenBraces=[]}", openBracketsMismatchRecovery.getHighlighting(3).toString());
        assertEquals("{workingBraces=[0, 1], brokenBraces=[2]}", openBracketsMismatchRecovery.getHighlighting(2).toString());
    }

    @Test public void outOfBoundsTest() {
        assertEquals("{workingBraces=[], brokenBraces=[]}", correctBrackets.getHighlighting(10).toString());
    }
}
