package syntax.antlr;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Created by avyatkin on 27/04/16.
 */
class ListIteratorWithOffset implements ListIterator<LexemeWithOffset> {
    enum LastAction { NOTHING, NEXT, PREVIOUS }

    LastAction lastAction;
    Optional<LexemeWithOffset> lastReturned;

    public ListIteratorWithOffset(List<Lexeme> lexemes) {
        listIterator = lexemes.listIterator();
        lastAction = LastAction.NOTHING;
        offset = 0;
        lastReturned = Optional.empty();
    }

    public ListIterator<Lexeme> getListIterator() {
        return listIterator;
    }

    ListIterator<Lexeme> listIterator;
    int offset;

    @Override
    public boolean hasNext() {
        return listIterator.hasNext();
    }

    @Override
    public LexemeWithOffset next() {
        if (listIterator.hasNext()) {
            lastAction = LastAction.NEXT;
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
            lastAction = LastAction.PREVIOUS;
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
        if (lastAction.equals(lastAction.NEXT))
            offset -= lastReturned.get().getLexeme().getSize();
        lastAction = LastAction.NOTHING;
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
