package gui.state.lastaction;

import syntax.antlr.Lexeme;
import syntax.antlr.LexemeIndex;

/**
 * Created by avyatkin on 02/05/16.
 */
public interface RecentAction {
    public boolean canBeCombined(RecentAction action);
    public void combineWith(RecentAction action);
    public LexemeIndex apply(LexemeIndex index);
}
