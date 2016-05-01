package gui.state.view;

import java.awt.*;

/**
 * Created by avyatkin on 02/05/16.
 */
public class SelectionState {
    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public SelectionState(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    Point start;
    Point end;

    public boolean isSelectionEmpty() {
        return start.equals(end);
    }
}