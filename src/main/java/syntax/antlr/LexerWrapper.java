package syntax.antlr;

import org.apache.commons.io.input.ReaderInputStream;
import syntax.antlr.ecmascript.ECMAScriptLexer;
import syntax.antlr.java.JavaLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import syntax.document.SupportedSyntax;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.util.*;
import java.util.stream.StreamSupport;

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
        List<Lexeme> newLexemes = new ArrayList<>();
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

        while (oldLexemeIterator.hasNext()) { oldLexemeIterator.next(); oldLexemeIterator.remove(); }
        for(Token t : updateLexer.getAllTokens()) {
            Lexeme l = lexemeFromToken(t);
            l.shift(updatedLexemeOffset);
            newLexemes.add(l);
        }
        newLexemes.forEach(l -> oldLexemeIterator.add(l));
//        for (Token token = updateLexer.nextToken(); token.getType() != Token.EOF; token = updateLexer.nextToken()) {
//            Lexeme newLexeme = lexemeFromToken(token);
//            newLexeme.setOffset(newLexeme.getOffset() + updatedLexemeOffset);
//
//            if (oldLexemeIterator.hasNext()) {
//                Lexeme oldLexeme = oldLexemeIterator.next();
//                oldLexeme.setOffset(oldLexeme.getOffset() + newText.length());
//
//                while (newLexeme.getOffset() > oldLexeme.getOffset()) {
//                    oldLexemeIterator.remove();
//                    oldLexeme = oldLexemeIterator.next();
//                    oldLexeme.setOffset(oldLexeme.getOffset() + newText.length());
//                }
//                if (oldLexeme.equals(newLexeme)) {
//                    break;
//                } else newLexemes.add(newLexeme);
//            } else newLexemes.add(newLexeme);
//        }
//        if (oldLexemeIterator.hasPrevious()) oldLexemeIterator.previous();
//        newLexemes.forEach((l) -> oldLexemeIterator.add(l));
//        if (oldLexemeIterator.hasNext()) oldLexemeIterator.next();
//        oldLexemeIterator.forEachRemaining((l) -> l.setOffset(l.getOffset() + newText.length()));
        text = text.substring(0, offset) + newText + text.substring(offset);
        return newLexemes;
    }

    public void removeText(int offset, int length) {
//        ListIterator<Lexeme> oldLexemeIterator = beforeFirstAffectedLexeme(offset);
//        if (oldLexemeIterator.hasNext()) {
//            Lexeme updatedLexeme = oldLexemeIterator.next();
//        } else if (oldLexemeIterator.hasPrevious()) {
//            Lexeme previous = oldLexemeIterator.previous();
//            int newLexemesOffset = previous.getOffset() + previous.getSize();
//
//        } else {
//
//        }


//        String
        init(syntax, text.substring(0, offset) + text.substring(offset + length));
    }


    private ListIterator<Lexeme> beforeFirstAffectedLexeme(int offset) {
        ListIterator<Lexeme> iterator = lexemes.listIterator();
        Lexeme currentLexeme;
        while (iterator.hasNext()) {
            currentLexeme = iterator.next();
            if (currentLexeme.getOffset() + currentLexeme.getDistanceToNextToken() > offset) {
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
}
