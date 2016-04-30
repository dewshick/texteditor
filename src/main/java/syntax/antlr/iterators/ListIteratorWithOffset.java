package syntax.antlr.iterators;

import syntax.antlr.Lexeme;
import syntax.antlr.LexemeWithOffset;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Created by avyatkin on 27/04/16.
 */
public class ListIteratorWithOffset implements ListIterator<LexemeWithOffset>, Iterator<LexemeWithOffset> {
    LastIteratorAction lastAction;
    Optional<LexemeWithOffset> lastReturned;

    public ListIteratorWithOffset(LexemesIterator literator) {
        listIterator = literator;
        lastAction = LastIteratorAction.NOTHING;
        offset = 0;
        lastReturned = Optional.empty();
    }

    public ListIteratorWithOffset copy() {
        ListIteratorWithOffset result = new ListIteratorWithOffset(listIterator.copy());
        result.lastAction = lastAction;
        result.offset = offset;
        result.lastReturned = lastReturned;
        return result;
    }

    public LexemesIterator getListIterator() {
        return listIterator;
    }

    public void getBeforeAffectedLexeme(int offset) {
        while (hasNext() &&
                (!lastReturned.isPresent() ||
                        lastReturned.get().getOffset() + lastReturned.get().getLexeme().getSize() < offset))
            next();

        while (hasPrevious() && (!lastReturned.isPresent() || lastReturned.get().getOffset() > offset))
            previous();
    }

    LexemesIterator listIterator;
    int offset;

    @Override
    public boolean hasNext() {
        return listIterator.hasNext();
    }

    @Override
    public LexemeWithOffset next() {
        if (listIterator.hasNext()) {
            lastAction = LastIteratorAction.NEXT;
            Lexeme next = listIterator.next();
            LexemeWithOffset result = new LexemeWithOffset(next, offset);
            offset += next.getSize();
            lastReturned = Optional.of(result);
            return result;
        }
//            todo: need proper exception here
        throw new RuntimeException("No next!");
    }

    @Override
    public boolean hasPrevious() {
        return listIterator.hasPrevious();
    }

    @Override
    public LexemeWithOffset previous() {
        if (listIterator.hasPrevious()) {
            lastAction = LastIteratorAction.PREVIOUS;
            Lexeme previous = listIterator.previous();
            offset -= previous.getSize();
            LexemeWithOffset result = new LexemeWithOffset(previous, offset);
            lastReturned = Optional.of(result);
            return new LexemeWithOffset(previous, offset);
        }
        throw new RuntimeException("No previous!");
    }

    @Override
    public int nextIndex() {
        return listIterator.nextIndex();
    }

    @Override
    public int previousIndex() {
        return listIterator.previousIndex();
    }

    @Override
    public void remove() {
        listIterator.remove();
        if (lastAction.equals(LastIteratorAction.NEXT))
            offset -= lastReturned.get().getLexeme().getSize();
        lastAction = LastIteratorAction.NOTHING;
    }

    @Override
    public void set(LexemeWithOffset lexeme) {
//            listIterator.set(lexeme);
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void add(LexemeWithOffset lexeme) {
        listIterator.add(lexeme.getLexeme());
    }
}
