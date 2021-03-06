package syntax.antlr;

import com.sun.tools.javac.util.Pair;
import gui.state.ColoredString;
import gui.view.EditorColors;
import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.io.input.ReaderInputStream;
import syntax.antlr.ecmascript.ECMAScriptLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import syntax.antlr.java.JavaLexer;
import syntax.brackets.BracketHighlighting;
import syntax.brackets.BracketIndex;
import syntax.document.SupportedSyntax;
import syntax.document.SyntaxColoring;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.util.function.Function;

/**
 * Created by avyatkin on 01/04/16.
 */
public class LexemeIndex {
    public String getTokenType(Token t) { return lexer.getVocabulary().getDisplayName(t.getType()); }

    Lexer lexer;
    SupportedSyntax syntax;

    public LexemeIndex(SupportedSyntax syntax, String code) {
        this.syntax = syntax;
        lexer = lexerByInputStream(new ANTLRInputStream(code));
        lexemes = new LinkedList<>();
        lexer.getAllTokens().forEach(t -> lexemes.add(lexemeFromToken(t)));
    }

    private List<Lexeme> lexemes;

    @Deprecated
    public List<Lexeme> lexemes() { return lexemes; }

    public Pair<BracketIndex, ColoredText> getState() {
        ColoredText text = coloredText();
        BracketIndex index = new BracketIndex(syntax, text);
        return new Pair<>(index, coloredText());
    }

    private ColoredText coloredText() {
        SyntaxColoring coloring = EditorColors.forSyntax(syntax);

        List<List<ColoredString>> result = new TreeList<>();
        List<ColoredString> initialLine = new TreeList<>();
        for (Lexeme lexeme: lexemes) {
            ColoredString colored = coloring.colorizeLexeme(lexeme);
            if (colored.containsNewlines()) {
                int index = 0;
                List<ColoredString> splitted = colored.splitByLines();
                for (ColoredString line : colored.splitByLines()) {
                    initialLine.add(line);
                    if (index != splitted.size() - 1) {
                        result.add(initialLine);
                        initialLine = new TreeList<>();
                    }
                    index++;
                }
            } else initialLine.add(colored);
        }
        result.add(initialLine);
        return new ColoredText(result);
    }

    private Lexeme lexemeFromToken(Token current) {
        int offset = current.getStartIndex();
        int distanceToNextToken = current.getText().length();
        String tokenType = getTokenType(current);
        return new Lexeme(offset, tokenType, current.getText());
    }

//    we have only stateless lexers here so we do not need to remember any modes whatsoever
    public LexemeIndex addText(int offset, String newText) {
        ListIterator<Lexeme> oldLexemeIterator = beforeFirstAffectedLexeme(offset);
        String extendedText = newText;
        int updatedLexemeOffset = 0;
        if (oldLexemeIterator.hasNext()) {
            Lexeme updatedLexeme = oldLexemeIterator.next();
            String oldText = updatedLexeme.getText();
            updatedLexemeOffset = updatedLexeme.getOffset();
            int relativeOffset = offset - updatedLexemeOffset;
            extendedText = oldText.substring(0, relativeOffset) + newText + oldText.substring(relativeOffset);
        }
        List<Lexeme> result = new IncrementalRetokenizer(extendedText,
                oldLexemeIterator,
                updatedLexemeOffset,
                newText.length()).syncLexemes();
        return this;
    }

    public LexemeIndex removeText(int offset, int length) {
        ListIterator<Lexeme> oldLexemeIterator = beforeFirstAffectedLexeme(offset);
        if (oldLexemeIterator.hasNext()) {
            StringBuilder modifiedTextBuilder = new StringBuilder();
            Lexeme affected = oldLexemeIterator.next();
            int newLexemesOffset = affected.getOffset();
            int removedZoneOffset = offset - affected.getOffset();
            while (affected.getOffset() <= offset + length) {
                modifiedTextBuilder.append(affected.getText());
                if (oldLexemeIterator.hasNext()) affected = oldLexemeIterator.next(); else break;
            }
            if (affected.getOffset() > offset + length) oldLexemeIterator.previous();
            String modifiedText = modifiedTextBuilder.toString();
            modifiedText = modifiedText.substring(0, removedZoneOffset) + modifiedText.substring(removedZoneOffset + length);
            List<Lexeme> result = new IncrementalRetokenizer(modifiedText, oldLexemeIterator, newLexemesOffset, -length).syncLexemes();
        }
        return this;
    }


    private ListIterator<Lexeme> beforeFirstAffectedLexeme(int offset) {
        ListIterator<Lexeme> iterator = lexemes.listIterator();
        if (lexemes.isEmpty()) return iterator;
        Function<Lexeme, Integer> lexemeEnd = lx -> lx.getOffset() + lx.getSize();
        Lexeme currentLexeme = iterator.next();
        while (iterator.hasNext() && lexemeEnd.apply(currentLexeme) < offset) currentLexeme = iterator.next();
        iterator.previous();
        return iterator;
    }

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

    private class IncrementalRetokenizer {
        IncrementalRetokenizer(String extendedText, ListIterator<Lexeme> oldLexemesIter, int newOffset, int oldOffset) {
            updateLexer = updatedCodeLexer(extendedText, lexemes.listIterator(oldLexemesIter.nextIndex()));
            oldLexemesIterator = oldLexemesIter;
            newLexemeOffset = newOffset;
            oldLexemeOffset = oldOffset;
            lexemesConverged = false;
            nextOldLexeme();
            nextUpdatedLexeme();
            newLexemes = new ArrayList<>();
        }

        private Lexer updateLexer;
        private ListIterator<Lexeme> oldLexemesIterator;
        private int newLexemeOffset;
        private int oldLexemeOffset;
        private boolean lexemesConverged;
        private Optional<Lexeme> oldLexeme;
        private Optional<Lexeme> newLexeme;
        List<Lexeme> newLexemes;

        public List<Lexeme> syncLexemes() {
            while (oldLexeme.isPresent() && newLexeme.isPresent()) {
                while (oldLexeme.get().getOffset() < newLexeme.get().getOffset())
                    nextOldLexeme();
                newLexemes.add(newLexeme.get());
                if (oldLexeme.get().equals(newLexeme.get())) {
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
            oldLexemesIterator.forEachRemaining(lexeme -> lexeme.shift(oldLexemeOffset));

            return newLexemes;
        }

        private void removeOverlappingOldLexemesBeforeIterator() {
            while (oldLexemesIterator.hasPrevious()) {
                Lexeme previousLexeme = oldLexemesIterator.previous();
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
