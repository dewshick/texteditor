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

import java.lang.reflect.Array;
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

    ColoredLinesList lines;


    private void initWithLexemes(List<Lexeme> lexemes) {
        lines = new ColoredLinesList(EditorColors.forSyntax(SupportedSyntax.JAVA));
        for (Lexeme l : lexemes) lines.add(l);
    }

    private void testAddingLexemes(List<Lexeme> lexemes) {
        initWithLexemes(lexemes);
        assertEquals(lexemes.toString(), lexemesAfterForwardTraversal().toString());

        assertEquals(lexemes.toString(), lexemesAfterBackwardTraversal().toString());
    }

    private List<Lexeme> lexemesAfterForwardTraversal() {
        LexemesIterator iterator = lines.lexemesIterator();
        List<Lexeme> result = new ArrayList<>();
        while (iterator.hasPrevious()) iterator.previous();
        while (iterator.hasNext()) result.add(iterator.next());
        return result;
    }

    private List<Lexeme> lexemesAfterBackwardTraversal() {
        LexemesIterator iterator = lines.lexemesIterator();
        List<Lexeme> backwardTraversal = new ArrayList<>();

        while (iterator.hasPrevious()) backwardTraversal.add(iterator.previous());
        Collections.reverse(backwardTraversal);
        return backwardTraversal;
    }

    private void testAddingLexemeInSpecificPlace(List<Lexeme> lexemes) {
        for (Lexeme l : availableLexemes) {
            IntStream.rangeClosed(0, lexemes.size()).forEach(i -> {
                initWithLexemes(lexemes);
                LexemesIterator iter = lines.lexemesIterator();
                IntStream.rangeClosed(0, i-1).forEach(j -> iter.next());
                iter.add(l);

                List<Lexeme> expected = new ArrayList<>(lexemes);
                expected.add(i, l);

                List<Lexeme> actual = lexemesAfterForwardTraversal();
                assertEquals(expected.toString(), actual.toString());
            });
        }
    }

    private void testRemovingLexemeBackwards(List<Lexeme> lexemes) {

    }

    private void testRemovingLexemeForward(List<Lexeme> lexemes) {

    }

    private List<List<Lexeme>> lexemePermutations(int n) {
        ICombinatoricsVector<Lexeme> originalVector = Factory.createVector(availableLexemes);
        Generator<Lexeme> gen = Factory.createPermutationWithRepetitionGenerator(originalVector, n);
        ArrayList<List<Lexeme>> result = new ArrayList<>();
        for (ICombinatoricsVector<Lexeme> perm : gen)
            result.add(perm.getVector());
        return result;
    }

    @Test
    public void initializingWithLexemes() {
        IntStream.rangeClosed(1, 3).forEach(i -> lexemePermutations(i).forEach(this::testAddingLexemes));
    }

    @Test
    public void addingLexemes() {
        lexemePermutations(3).forEach(this::testAddingLexemeInSpecificPlace);
    }

    @Test
    public void removePreviousLexeme() {

    }

    @Test
    public void removeNextLexeme() {

    }
}
