package iterators;

import gui.state.ColoredLinesList;
import gui.view.EditorColors;
import org.junit.Before;
import org.junit.Test;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import syntax.antlr.Lexeme;
import syntax.antlr.iterators.LexemesIterator;
import syntax.document.SupportedSyntax;
import syntax.document.SyntaxColoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by avyatkin on 28/04/16.
 */
public class LexemesIteratorTest {
    Lexeme oneLinedLexeme = new Lexeme("short", "one line lexeme");
    Lexeme twoLinedLexeme = new Lexeme("moderate", "one average multiline\n lexeme");
    Lexeme threeLinedLexeme = new Lexeme("long", "one long\nlong multiline\n lexeme");
    List<Lexeme> availableLexemes = Arrays.asList(oneLinedLexeme, twoLinedLexeme, threeLinedLexeme);

    LexemesIterator iterator;
    ColoredLinesList lines;

    @Before
    public void init() {
    }

    private void testAddingLexemes(List<Lexeme> lexemes) {
        lines = new ColoredLinesList(EditorColors.forSyntax(SupportedSyntax.JAVA));
        for (Lexeme l : lexemes) lines.add(l);
        iterator = lines.lexemesIterator();
        List<Lexeme> result = new ArrayList<>();
        while (iterator.hasNext()) result.add(iterator.next());
        assertEquals(lexemes.toString(), result.toString());
        List<Lexeme> backwardTraversal = new ArrayList<>();
        while (iterator.hasPrevious()) backwardTraversal.add(iterator.previous());
        Collections.reverse(backwardTraversal);
        assertEquals(lexemes.toString(), backwardTraversal.toString());
    }

    @Test
    public void addingLexemes() {
        ICombinatoricsVector<Lexeme> originalVector = Factory.createVector(availableLexemes);
        IntStream.range(1, 4).forEach(lexemesCount -> {
            Generator<Lexeme> gen = Factory.createPermutationWithRepetitionGenerator(originalVector, lexemesCount);
            for (ICombinatoricsVector<Lexeme> perm : gen)
                testAddingLexemes(perm.getVector());
        });

    }

    public void addLexemeInTheBeginning() {

    }

    public void addLexemeInTheMiddle() {

    }

    public void addLexemeInTheEnd() {

    }

    public void removePreviousLexeme() {

    }

    public void removeNextLexeme() {

    }
}
