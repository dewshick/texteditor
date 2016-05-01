package gui.state;

import com.sun.tools.javac.util.Pair;
import gui.state.lastaction.ExpirableAction;
import syntax.antlr.ColoredText;
import syntax.antlr.LexemeIndex;
import syntax.brackets.BracketIndex;
import syntax.document.SupportedSyntax;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by avyatkin on 02/05/16.
 */
public class RelexingLauncher {
    CompletableFuture<LexemeIndex> relexer;
    Optional<ExpirableAction> recentAction;

    public RelexingLauncher(SupportedSyntax syntax, String text) {
        init(syntax, text);
    }

    public synchronized void reinitialize(SupportedSyntax syntax, String text) {
        interrupt();
        init(syntax, text);
    }

    private void init(SupportedSyntax syntax, String text) {
        relexer = CompletableFuture.completedFuture(new LexemeIndex(syntax, text));
        recentAction = Optional.empty();
    }

    public synchronized void tick() {
        if (recentAction.isPresent() && recentAction.get().isExpired())
            runCurrentAction();
    }

    public synchronized boolean isDone() {
        return !recentAction.isPresent() && relexer.isDone();
    }

    public synchronized Pair<BracketIndex, ColoredText> getState() {
        runCurrentAction();
        try {
            return relexer.thenApplyAsync(LexemeIndex::getState).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addText(int offset, String text) {
        addAction(ExpirableAction.ofAddition(offset, text));
    }

    public synchronized void removeText(int offset, int length) {
        addAction(ExpirableAction.ofRemoval(offset, length));
    }

    private void interrupt() {
        relexer.cancel(true);
        recentAction = Optional.empty();
    }

    private void addAction(ExpirableAction action) {
        if (recentAction.isPresent()) {
            ExpirableAction recent = recentAction.get();
            if (recent.canBeCombined(action))
                recent.combineWith(action);
            else {
                runCurrentAction();
                recentAction = Optional.of(action);
            }
        } else recentAction = Optional.of(action);
    }

    private void runCurrentAction() {
        if (recentAction.isPresent()) {
            relexer = relexer.thenApplyAsync(recentAction.get()::apply);
            recentAction = Optional.empty();
        }
    }

}
