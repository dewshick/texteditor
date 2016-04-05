package syntax.antlr;

import org.apache.commons.io.input.ReaderInputStream;
import syntax.antlr.ecmascript.ECMAScriptLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import syntax.document.SupportedSyntax;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;

/**
 * Created by avyatkin on 01/04/16.
 */
public class LexerWrapper {
    public String getTokenType(Token t) {
        return lexer.getVocabulary().getDisplayName(t.getType());
    }

    Lexer lexer;
    String text;
    SupportedSyntax syntax;

    public LexerWrapper(SupportedSyntax syntax, String code) {
        init(syntax, code);
    }

    private void init(SupportedSyntax syntax, String code) {
        this.syntax = syntax;
        this.text = code;
        lexer = lexerByInputStream(new ANTLRInputStream(code));
        lexemes = new LinkedList<>();
        lexer.getAllTokens().forEach(t -> lexemes.add(lexemeFromToken(t)));
    }

    private List<Lexeme> lexemes;

    public List<Lexeme> lexemes() {
        return lexemes;
    }

    private Lexeme lexemeFromToken(Token current) {
        int offset = current.getStartIndex();
        int distanceToNextToken = current.getText().length();
        String tokenType = getTokenType(current);
        return new Lexeme(offset, distanceToNextToken, distanceToNextToken, tokenType, current.getText());
    }

//    we have only stateless lexers here so we do not need to remember any modes whatsoever
    public List<Lexeme> addText(int offset, String newText) {
        text = text.substring(0, offset) + newText + text.substring(offset);
        ListIterator<Lexeme> oldLexemeIterator = beforeFirstAffectedLexeme(offset);
        String extendedText = newText;
        int updatedLexemeOffset = 0;
        if (oldLexemeIterator.hasNext()) {
            Lexeme updatedLexeme = oldLexemeIterator.next(); oldLexemeIterator.remove();
            String oldText = updatedLexeme.getText();
            updatedLexemeOffset = updatedLexeme.getOffset();
            int relativeOffset = offset - updatedLexemeOffset;
            extendedText = oldText.substring(0, relativeOffset) + newText + oldText.substring(relativeOffset);
        } else if (oldLexemeIterator.hasPrevious()) {
            Lexeme lastLexeme = oldLexemeIterator.previous();
            updatedLexemeOffset = lastLexeme.getOffset() + lastLexeme.getDistanceToNextToken();
            oldLexemeIterator.next();
        }

        Lexer updateLexer = updatedCodeLexer(extendedText, lexemes.listIterator(oldLexemeIterator.nextIndex()));

        return new IncrementalRetokenizer(updateLexer,
                oldLexemeIterator,
                updatedLexemeOffset,
                newText.length()).syncLexemes();
    }

    public void removeText(int offset, int length) {
        init(syntax, text.substring(0, offset) + text.substring(offset + length));
    }


    private ListIterator<Lexeme> beforeFirstAffectedLexeme(int offset) {
        ListIterator<Lexeme> iterator = lexemes.listIterator();
        Lexeme currentLexeme;
        while (iterator.hasNext()) {
            currentLexeme = iterator.next();
            if (currentLexeme.getOffset() + currentLexeme.getDistanceToNextToken() >= offset) {
                iterator.previous();
                break;
            }
        }
        return iterator;
    }

    private Lexer updatedCodeLexer(String init, Iterator<Lexeme> todo) {
        Enumeration readersEnum = new Enumeration<InputStream>() {
            @Override
            public boolean hasMoreElements() {
                return todo.hasNext();
            }

            @Override
            public InputStream nextElement() {
                return stringInputStream(todo.next().getText());
            }
        };

        SequenceInputStream inputStream = new SequenceInputStream(readersEnum);
        inputStream = new SequenceInputStream(stringInputStream(init), inputStream);
        try {
            return lexerByInputStream(new ANTLRInputStream(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e); //impossible
        }
    }

    private InputStream stringInputStream(String input) {
        return new ReaderInputStream(new StringReader(input));
    }

    private Lexer lexerByInputStream(ANTLRInputStream inputStream) {
        switch (syntax) {
            case ECMASCRIPT:
                return new ECMAScriptLexer(inputStream);
            case JAVA:
                return new ECMAScriptLexer(inputStream);
        }
        throw new RuntimeException("Unknown syntax type: " + syntax); //impossible
    }

    class IncrementalRetokenizer {
        IncrementalRetokenizer(Lexer lex, ListIterator<Lexeme> iter, int newOffset, int oldOffset) {
            updateLexer = lex;
            oldLexemesIterator = iter;
            newLexemeOffset = newOffset;
            oldLexemeOffset = oldOffset;
            lexemesConverged = false;
            nextOldLexeme();
            nextUpdatedLexeme();
        }

        private Lexer updateLexer;
        private ListIterator<Lexeme> oldLexemesIterator;
        private int newLexemeOffset;
        private int oldLexemeOffset;
        private boolean lexemesConverged;
        private Optional<Lexeme> oldLexeme;
        private Optional<Lexeme> newLexeme;

        public List<Lexeme> syncLexemes() {
            List<Lexeme> newLexemes = new ArrayList<>();

            while (oldLexeme.isPresent() && newLexeme.isPresent()) {
                while (oldLexeme.get().getOffset() < newLexeme.get().getOffset())
                    nextOldLexeme();
                if (oldLexeme.get().equals(newLexeme.get())) {
                    newLexemes.add(newLexeme.get());
                    lexemesConverged = true;
                    break;
                } else {
                    newLexemes.add(newLexeme.get());
                    nextUpdatedLexeme();
                }
            }

            while (oldLexemesIterator.hasPrevious()) {
                Lexeme previousLexeme = oldLexemesIterator.previous();
                if (previousLexeme.getOffset() >= newLexemeOffset)
                    oldLexemesIterator.remove();
                else {
                    oldLexemesIterator.next();
                    break;
                }
            }

            if (!lexemesConverged) {
                if (oldLexeme.isPresent()) {
                    while (oldLexemesIterator.hasNext()) {
                        oldLexemesIterator.next();
                        oldLexemesIterator.remove();
                    }
                } else {
                    while (newLexeme.isPresent()) {
                        newLexemes.add(newLexeme.get());
                        nextUpdatedLexeme();
                    }
                }
            }
            newLexemes.forEach(oldLexemesIterator::add);
            oldLexemesIterator.forEachRemaining(lexeme -> lexeme.shift(oldLexemeOffset));

            return newLexemes;
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
    }
}
