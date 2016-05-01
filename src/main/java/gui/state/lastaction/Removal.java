package gui.state.lastaction;

import syntax.antlr.LexemeIndex;

/**
 * Created by avyatkin on 02/05/16.
 */
public class Removal implements RecentAction {

    private int offset;
    private int length;

    public Removal(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }


    @Override
    public boolean canBeCombined(RecentAction action) {
        if (action instanceof Removal) {
            Removal rm = (Removal) action;
            return combinedAsBackspace(rm) || combinedAsDelete(rm);
        }
        return false;
    }

    @Override
    public void combineWith(RecentAction action) {
        Removal rm = (Removal) action;
        if (combinedAsBackspace(rm)) combineAsBackspace(rm);
        else if (combinedAsDelete(rm)) combineAsDelete(rm);
        else throw new IllegalArgumentException("Actions can't be combined!");
    }

    @Override
    public LexemeIndex apply(LexemeIndex index) {
        return index.removeText(offset, length);
    }

    private boolean combinedAsBackspace(Removal action) {
        return offset - length == action.offset;
    }

    private boolean combinedAsDelete(Removal action) {
        return offset == action.offset;
    }

    private void combineAsBackspace(Removal action) {
        offset = action.offset;
        length += action.length;
    }

    private void combineAsDelete(Removal action) {
        length += action.length;
    }
}
