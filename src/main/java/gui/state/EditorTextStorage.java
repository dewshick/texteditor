package gui.state;

import com.sun.tools.javac.util.Pair;
import syntax.antlr.ColoredText;
import syntax.antlr.LexemeIndex;
import syntax.brackets.BracketHighlighting;
import syntax.brackets.BracketIndex;
import syntax.document.SupportedSyntax;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Created by avyatkin on 16/04/16.
 * stores text
 * all operations are done in relative coords (number of char in line/number of line)
 */
public class EditorTextStorage {
    public synchronized void setLines(ColoredText lines) {
        this.lines = lines;
    }

    //    List<String> lines;
    ColoredText lines;

    CompletableFuture<LexemeIndex> index;

    public synchronized SupportedSyntax getSyntax() {
        return syntax;
    }

    SupportedSyntax syntax;
    BracketIndex highlighting;

    boolean sync;

    public EditorTextStorage(SupportedSyntax syntax) {
        index = CompletableFuture.completedFuture(new LexemeIndex(syntax, ""));
        forceSync();
        this.syntax = syntax;
    }

    public synchronized String getText() { return lines.getText(); }

    private synchronized void forceSync() {
        try {
            if (sync) return;
            Pair<BracketIndex, ColoredText> state = index.get().getState();
            highlighting = state.fst;
            lines = state.snd;
            sync = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void syncIfPossible() {
        if (index.isDone()) forceSync();
    }

//    TODO: move async logic here
    public synchronized void setText(String text) {
        sync = false;
        index = CompletableFuture.completedFuture(new LexemeIndex(syntax, text));
        forceSync();
    }

    public synchronized BracketHighlighting getBracketHighlighting(Point caret) {
        if (sync) return highlighting.getHighlighting(caret);
        return BracketHighlighting.emptyHighlighting();
    }

//    TODO: use 2d int rectangle to return displayed area
//    TODO: should be package-local


    public synchronized List<ColoredString> getColoredLine(int line) {
        return lines.getColoredLine(line);
    }

    /**
     * Edit text
     */

    public synchronized int offsetFromCoords(Point position) {
        return getText(beginningOfText(), position).length();
    }

    public synchronized EditorTextStorage addText(Point position, String text) {
        sync = false;
        int offset = offsetFromCoords(position);
        lines.addText(position, text);
        index = index.thenApplyAsync(lexemes -> lexemes.addText(offset, text));
        return this;
    }

    public synchronized void removeText(Point position, int length) {
        removeText(position, horizontalMove(position, length));
    }

    private synchronized void removeText(Point start, Point end) {
        sync = false;
        int startOffset = offsetFromCoords(start);
        int endOffset = offsetFromCoords(end);
        lines.removeText(start, end);
        index = index.thenApplyAsync(lexemes -> lexemes.removeText(startOffset, endOffset - startOffset));
    }

    public synchronized void removeText(EditorState.Selection selection) {
        Point start = selection.startPoint();
        Point end = selection.endPoint();
        removeText(start, end);
    }

//    iterator code is almost the same for all the strings so maybe there's way to reuse it?
//    to avoid complex testing/rewriting all the time
    public synchronized String getText(Point start, Point end) {
        return lines.getText(start, end);
    }

    /**
     * Various operations related to relative coords
     */

    public synchronized int lastLineIndex() { return lines.lastLineIndex(); }

    public synchronized Point beginningOfText() { return new Point(0,0); }

//    TODO remove this weird method and fix logic
    public synchronized int lineLength(int n) {
        String lastLine = lines.getLine(n);
        int length = lastLine.length();
        if (lastLine.endsWith("\n")) length -= 1;
        return length;
    }

    public synchronized Point endOfText() { return new Point(lineLength(lastLineIndex()), lastLineIndex()); }

    public synchronized Dimension relativeSize() {
        int width = IntStream.rangeClosed(0, lines.lastLineIndex()).map(this::lineLength).reduce(0, Integer::max);
        return new Dimension(width, lines.linesCount());
    }

    public synchronized Point verticalMove(Point position, int direction) {
        int newY = Math.min(Math.max(position.y + direction, 0), lastLineIndex());
        if (newY == position.y) return position;
        int newX = Math.min(lineLength(newY), position.x);
        return new Point(newX, newY);
    }

//    TODO: this can be easily optimized
    public synchronized Point horizontalMove(Point position, int distance) {
        int direction = distance > 0 ? 1 : -1;
        int length = Math.abs(distance);
        for (;length > 0;length--) position = horizontalStep(position, direction);
        return position;
    }

//    moves only on 1 position
    private synchronized Point horizontalStep(Point position, int direction) {
        int currentLineLength = lineLength(position.y);
        int newY = position.y;
        int newX = position.x + direction;

        if (newX > currentLineLength) {
            if (newY >= lastLineIndex()) return position;
            else return new Point(0, position.y + 1);
        } else if (newX < 0) {
            if (newY == 0) return position;
            else {
                newY--;
                return new Point(lineLength(newY), newY);
            }
        }
        return new Point(newX, newY);
    }

    public synchronized Point closestCaretPosition(Point point) {
        if (point.y < 0) return beginningOfText();
        else if (point.y >= lines.linesCount()) return endOfText();
        else return new Point(Math.min(point.x, lineLength(point.y)), point.y);
    }

//    correct string-split
    public synchronized static List<String> buildLinesList(String str, boolean keepNewlines) {
        int initialIndex = 0;
        List<String> result = new ArrayList<>();
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '\n') {
                result.add(str.substring(initialIndex, keepNewlines ? i+1 : i));
                initialIndex = i+1;
            }
        result.add(str.substring(initialIndex));
        return result;
    }

    public synchronized EditorTextStorage changeSyntax(SupportedSyntax syntax) {
        index = CompletableFuture.completedFuture(new LexemeIndex(syntax, getText()));
        this.syntax = syntax;
        return this;
    }
}
