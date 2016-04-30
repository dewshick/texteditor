package iterators;

import gui.state.ColoredLinesStorage;
import gui.view.EditorColors;
import org.junit.Test;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import syntax.antlr.Lexeme;
import syntax.antlr.iterators.LexemesIterator;
import syntax.document.SupportedSyntax;

import java.util.*;
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

    ColoredLinesStorage lines;

    private void initWithLexemes(List<Lexeme> lexemes) {
        lines = lexemesStorage(lexemes);
    }

    private ColoredLinesStorage lexemesStorage(List<Lexeme> lexemes) {
        ColoredLinesStorage st = new ColoredLinesStorage(EditorColors.forSyntax(SupportedSyntax.JAVA));
        for (Lexeme l : lexemes) st.add(l);
        return st;
    }

    private void testInitializingWithLexemes(List<Lexeme> lexemes) {
        initWithLexemes(lexemes);
        assertEquals(lexemes.toString(), lexemesAfterForwardTraversal().toString());

        assertEquals(lexemes.toString(), lexemesAfterBackwardTraversal(0).toString());
        assertEquals(lexemes.toString(), lexemesAfterBackwardTraversal(1).toString());
    }

    private List<Lexeme> lexemesAfterForwardTraversal() {
        LexemesIterator iterator = lines.lexemesIterator();
        List<Lexeme> result = new ArrayList<>();
        while (iterator.hasPrevious()) iterator.previous();
        while (iterator.hasNext()) result.add(iterator.next());
        return result;
    }

    private List<Lexeme> lexemesAfterBackwardTraversal(int traversals) {
        LexemesIterator iterator = lines.lexemesIterator();
        List<Lexeme> backwardTraversal = new ArrayList<>();
        while (iterator.hasNext()) iterator.next();

        IntStream.range(1, traversals).forEach(i -> {
            while (iterator.hasPrevious()) iterator.previous();
            while (iterator.hasNext()) iterator.next();
        });
        while (iterator.hasPrevious()) backwardTraversal.add(iterator.previous());
        Collections.reverse(backwardTraversal);
        return backwardTraversal;
    }

    private void testAddingLexemeInSpecificPlace(List<Lexeme> lexemes) {
        for (Lexeme l : availableLexemes) {
            IntStream.rangeClosed(0, lexemes.size()).forEach(lexemePlace -> {
                initWithLexemes(lexemes);
                LexemesIterator iter = lines.lexemesIterator();
                IntStream.rangeClosed(0, lexemePlace-1).forEach(j -> iter.next());
                iter.add(l);

                List<Lexeme> expected = new ArrayList<>(lexemes);
                expected.add(lexemePlace, l);

                List<Lexeme> actual = lexemesAfterForwardTraversal();
                assertStorageUpdatedCorrectly(lines, expected);
//                assertEquals(expected.toString(), actual.toString());
            });
        }
    }

    private void testRemovingLexemeBackwards(List<Lexeme> lexemes) {
        IntStream.rangeClosed(1, lexemes.size()).forEach(indexToRm -> {
            List<Lexeme> expectedList = new ArrayList<>(lexemes);
            initWithLexemes(expectedList);
            LexemesIterator actualIter = lines.lexemesIterator();
            ListIterator<Lexeme> expectedIter = expectedList.listIterator();
            while (actualIter.hasNext()) { actualIter.next(); expectedIter.next(); }

            IntStream.rangeClosed(1, indexToRm).forEach(j -> {
                expectedIter.previous();
                actualIter.previous();
            });
            actualIter.remove(); expectedIter.remove();

            assertStorageUpdatedCorrectly(lines, expectedList);
//            assertEquals(expectedList, lexemesAfterForwardTraversal());
        });
    }

    private void testRemovingLexemeForward(List<Lexeme> lexemes) {
        IntStream.rangeClosed(1, lexemes.size()).forEach(indexToRm -> {
            List<Lexeme> expectedList = new ArrayList<>(lexemes);
            initWithLexemes(expectedList);
            LexemesIterator actualIter = lines.lexemesIterator();
            ListIterator<Lexeme> expectdIter = expectedList.listIterator();
            IntStream.rangeClosed(1, indexToRm).forEach(j -> { actualIter.next(); expectdIter.next();});
            expectdIter.remove();
            actualIter.remove();

            assertStorageUpdatedCorrectly(lines, expectedList);
//            assertEquals(expectedList, lexemesAfterForwardTraversal());
        });
    }

    private List<List<Lexeme>> lexemePermutations(int n) {
        ICombinatoricsVector<Lexeme> originalVector = Factory.createVector(availableLexemes);
        Generator<Lexeme> gen = Factory.createPermutationWithRepetitionGenerator(originalVector, n);
        ArrayList<List<Lexeme>> result = new ArrayList<>();
        for (ICombinatoricsVector<Lexeme> perm : gen)
            result.add(perm.getVector());
        return result;
    }

    private void assertStorageUpdatedCorrectly(ColoredLinesStorage updated, List<Lexeme> src) {
        assertEquals(lexemesStorage(src).getColoredLines().toString(), updated.getColoredLines().toString());
    }

    private void testForwardRemoval(List<Lexeme> lexemes) {
        List<Lexeme> expectedList = new ArrayList<>(lexemes);
        initWithLexemes(expectedList);
        LexemesIterator iter = lines.lexemesIterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        assertStorageUpdatedCorrectly(lines, new ArrayList<>());
    }

    private void testBackwardRemoval(List<Lexeme> lexemes) {
        List<Lexeme> expectedList = new ArrayList<>(lexemes);
        initWithLexemes(expectedList);
        LexemesIterator iter = lines.lexemesIterator();
        while (iter.hasNext()) iter.next();
        while (iter.hasPrevious()) {
            iter.previous();
            iter.remove();
        }
        assertStorageUpdatedCorrectly(lines, new ArrayList<>());
    }

    @Test
    public void initializing() {
        IntStream.rangeClosed(1, 3).forEach(i -> lexemePermutations(i).forEach(this::testInitializingWithLexemes));
    }

//    actually we should compare internal structures of lineslists instead of comparing them with lexeme lists
//    TODO after fixing all this tests
    @Test
    public void addingLexemes() {
        lexemePermutations(3).forEach(this::testAddingLexemeInSpecificPlace);
    }

    @Test
    public void removePreviousLexeme() {
        lexemePermutations(3).forEach(this::testRemovingLexemeBackwards);
    }

    @Test
    public void removeNextLexeme() {
        lexemePermutations(3).forEach(this::testRemovingLexemeForward);
    }

    @Test
    public void removeAllLexemesForward() {
        lexemePermutations(3).forEach(this::testForwardRemoval);
    }

    @Test
    public void removeAllLexemesBackward() {
        lexemePermutations(3).forEach(this::testBackwardRemoval);
    }
}
