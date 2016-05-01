package gui.state.lastaction;

import syntax.antlr.LexemeIndex;

/**
 * Created by avyatkin on 02/05/16.
 */
public class Addition implements RecentAction {
    public Addition(int offset, String str) {
        this.offset = offset;
        content = str;
    }

    int offset;
    String content;

    @Override
    public boolean canBeCombined(RecentAction action) {
        if (action instanceof Addition) {
            return ((Addition) action).offset == offset + content.length();
        }
        return false;
    }

    @Override
    public void combineWith(RecentAction action) {
        content += ((Addition) action).content;
    }

    @Override
    public LexemeIndex apply(LexemeIndex index) {
        return index.addText(offset, content);
    }
}
