package gui.state.lastaction;

import syntax.antlr.LexemeIndex;

/**
 * Created by avyatkin on 02/05/16.
 */
public class ExpirableAction {
    static final long OUTDATE_PERIOD = 100;

    volatile long creationDate;

    private RecentAction content;

    public ExpirableAction(RecentAction content) {
        creationDate = System.currentTimeMillis();
        this.content = content;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationDate > OUTDATE_PERIOD;
    }

    public boolean canBeCombined(ExpirableAction action) {
        return content.canBeCombined(action.content);
    }

    public void combineWith(ExpirableAction newContent) {
        content.combineWith(newContent.content);
        creationDate = System.currentTimeMillis();
    }

    public LexemeIndex apply(LexemeIndex index) {
        creationDate -= OUTDATE_PERIOD;
        return content.apply(index);
    }

    public static ExpirableAction ofAddition(int offset, String text) {
        return new ExpirableAction(new Addition(offset, text));
    }

    public static ExpirableAction ofRemoval(int offset, int length) {
        return new ExpirableAction(new Removal(offset, length));
    }
}