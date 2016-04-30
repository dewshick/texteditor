package syntax.antlr;

import com.sun.tools.javac.util.Pair;
import gui.state.ColoredLinesStorage;
import gui.state.ColoredString;
import gui.view.EditorColors;
import org.apache.commons.io.input.ReaderInputStream;
import syntax.antlr.generated.ecmascript.ECMAScriptLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import syntax.antlr.iterators.ListIteratorWithOffset;
import syntax.antlr.generated.java.JavaLexer;
import syntax.document.SupportedSyntax;
import syntax.document.SyntaxColoring;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import static syntax.EditorUtil.*;

/**
 * Created by avyatkin on 01/04/16.
 */
public class LexemeIndex {
    public String getTokenType(Token t) { return lexer.getVocabulary().getDisplayName(t.getType()); }

    Lexer lexer;
    SupportedSyntax syntax;

    public ColoredLinesStorage getColoredLinesList() {
        return coloredLinesList;
    }

    ColoredLinesStorage coloredLinesList;
    SyntaxColoring coloring;

    public LexemeIndex(SupportedSyntax syntax, String code) {
        this.syntax = syntax;
        coloring = EditorColors.forSyntax(syntax);
        lexer = lexerByInputStream(new ANTLRInputStream(code));

        coloredLinesList = new ColoredLinesStorage(coloring);

        lexer.getAllTokens().forEach(t -> coloredLinesList.add(lexemeFromToken(t).getLexeme()));
    }

    public List<ColoredString> getColoredLine(int line) {
        return coloredLinesList.getColoredLine(line);
    }

    private LexemeWithOffset lexemeFromToken(Token current) {
        int offset = current.getStartIndex();
        int distanceToNextToken = current.getText().length();
        String tokenType = getTokenType(current);
        return new LexemeWithOffset(new Lexeme(tokenType, current.getText()),offset);
    }

//    we have only stateless lexers here so we do not need to remember any modes whatsoever
// TODO: accept Point coords instead of offset
    public List<Lexeme> addText(Point changesPoint, String newText) {
        Pair<Integer, ListIteratorWithOffset> iteratorWithOffset = coloredLinesList.beforeFirstAffectedLexeme(changesPoint);
        ListIteratorWithOffset oldLexemeIterator = iteratorWithOffset.snd;
        int relativeLexemeOffset = iteratorWithOffset.fst;

        String extendedText = newText;
        int updatedLexemeOffset = 0;
        if (oldLexemeIterator.hasNext()) {
            LexemeWithOffset updatedLexeme = oldLexemeIterator.next();
            String oldText = updatedLexeme.getLexeme().getText();
            updatedLexemeOffset = updatedLexeme.getOffset();
            int relativeOffset = relativeLexemeOffset;
            extendedText = oldText.substring(0, relativeOffset) + newText + oldText.substring(relativeOffset);
        }
        List<LexemeWithOffset> result = new IncrementalRetokenizer(extendedText,
                oldLexemeIterator,
                updatedLexemeOffset,
                newText.length()).syncLexemes();
        return map(result, LexemeWithOffset::getLexeme);
    }

// TODO: accept Point coords instead of offset
    public List<Lexeme> removeText(Point changesPoint, int length) {
        Pair<Integer, ListIteratorWithOffset> iteratorWithOffset = coloredLinesList.beforeFirstAffectedLexeme(changesPoint);
        ListIteratorWithOffset oldLexemeIterator = iteratorWithOffset.snd;
        int relativeLexemeOffset = iteratorWithOffset.fst;

        if (oldLexemeIterator.hasNext()) {
            StringBuilder modifiedTextBuilder = new StringBuilder();
            LexemeWithOffset affected = oldLexemeIterator.next();
            int newLexemesOffset = affected.getOffset();
            int removedZoneOffset = relativeLexemeOffset;
            int absoluteRemovalOffset = affected.getOffset() + relativeLexemeOffset + length;

            while (affected.getOffset() <= absoluteRemovalOffset) {
                modifiedTextBuilder.append(affected.getLexeme().getText());
                if (oldLexemeIterator.hasNext()) affected = oldLexemeIterator.next(); else break;
            }
            if (affected.getOffset() > absoluteRemovalOffset) oldLexemeIterator.previous();
            String modifiedText = modifiedTextBuilder.toString();
            modifiedText = modifiedText.substring(0, removedZoneOffset) + modifiedText.substring(removedZoneOffset + length);
            List<LexemeWithOffset> result = new IncrementalRetokenizer(modifiedText, oldLexemeIterator, newLexemesOffset, -length).syncLexemes();
            return map(result, LexemeWithOffset::getLexeme);
        } else {
            return new LinkedList<>();
        }
    }

// TODO: accept Point coords instead of offset

    private InputStream stringInputStream(String input) {
        return new ReaderInputStream(new StringReader(input));
    }

    private Lexer lexerByInputStream(ANTLRInputStream inputStream) {
        switch (syntax) {
            case ECMASCRIPT:
                return new ECMAScriptLexer(inputStream);
            case JAVA:
                return new JavaLexer(inputStream);
        }
        throw new RuntimeException("Unknown syntax type: " + syntax); //impossible
    }

    class IncrementalRetokenizer {
        IncrementalRetokenizer(String extendedText, ListIteratorWithOffset oldLexemesIter, int newOffset, int oldOffset) {
            updateLexer = updatedCodeLexer(extendedText, oldLexemesIter.getListIterator().copy());
            oldLexemesIterator = oldLexemesIter;
            newLexemeOffset = newOffset;
            oldLexemeOffset = oldOffset;
            lexemesConverged = false;
            nextOldLexeme();
            nextUpdatedLexeme();
            newLexemes = new ArrayList<>();
        }

        private Lexer updateLexer;
        private ListIteratorWithOffset oldLexemesIterator;
        private int newLexemeOffset;
        private int oldLexemeOffset;
        private boolean lexemesConverged;
        private Optional<LexemeWithOffset> oldLexeme;
        private Optional<LexemeWithOffset> newLexeme;
        List<LexemeWithOffset> newLexemes;

        public List<LexemeWithOffset> syncLexemes() {
            while (oldLexeme.isPresent() && newLexeme.isPresent()) {
                while (oldLexeme.get().getOffset() < newLexeme.get().getOffset())
                    nextOldLexeme();
                newLexemes.add(newLexeme.get());
                if (oldLexeme.get().getOffset() == newLexeme.get().getOffset() &&
                        oldLexeme.get().getLexeme().equals(newLexeme.get().getLexeme())) {
                    lexemesConverged = true;
                    break;
                } else nextUpdatedLexeme();
            }
            removeOverlappingOldLexemesBeforeIterator();

            if (!lexemesConverged) {
                removeOldLexemesAfterIterator();
                processRemainingNewLexemes();
            }
            newLexemes.forEach(oldLexemesIterator::add);
            return newLexemes;
        }

        private void removeOverlappingOldLexemesBeforeIterator() {
            while (oldLexemesIterator.hasPrevious()) {
                LexemeWithOffset previousLexeme = oldLexemesIterator.previous();
                if (previousLexeme.getOffset() >= newLexemeOffset)
                    oldLexemesIterator.remove();
                else {
                    oldLexemesIterator.next();
                    break;
                }
            }
        }

        private void processRemainingNewLexemes() {
            while (newLexeme.isPresent()) {
                newLexemes.add(newLexeme.get());
                nextUpdatedLexeme();
            }
        }

        private void removeOldLexemesAfterIterator() {
            while (oldLexemesIterator.hasNext()) {
                oldLexemesIterator.next();
                oldLexemesIterator.remove();
            }
        }

        private void nextUpdatedLexeme() {
            Token t = updateLexer.nextToken();
            newLexeme = t.getType() == Token.EOF ?
                    Optional.empty() : Optional.of(lexemeFromToken(t).shift(newLexemeOffset));
        }

        private void nextOldLexeme() {
            oldLexeme = oldLexemesIterator.hasNext() ?
                    Optional.of(oldLexemesIterator.next().shift(oldLexemeOffset)) : Optional.empty();
        }

        private Lexer updatedCodeLexer(String init, Iterator<Lexeme> todo) {
            Enumeration readersEnum = new Enumeration<InputStream>() {
                @Override
                public boolean hasMoreElements() { return todo.hasNext(); }

                @Override
                public InputStream nextElement() { return stringInputStream(todo.next().getText()); }
            };

            SequenceInputStream inputStream = new SequenceInputStream(readersEnum);
            inputStream = new SequenceInputStream(stringInputStream(init), inputStream);
            try {
                return lexerByInputStream(new ANTLRInputStream(inputStream));
            } catch (IOException e) {
                throw new RuntimeException(e); //impossible
            }
        }
    }
}
