package gui.state.view;

import java.awt.*;

/**
 * Created by avyatkin on 02/05/16.
 */
public class CaretState {
    public boolean isShouldBeRendered() {
        return shouldBeRendered;
    }

    public boolean isInInsertMode() {
        return isInInsertMode;
    }

    public Point getCoords() {
        return coords;
    }

    public CaretState(boolean shouldBeRendered, boolean isInInsertMode, Point coords) {
        this.shouldBeRendered = shouldBeRendered;
        this.isInInsertMode = isInInsertMode;
        this.coords = coords;
    }

    private boolean shouldBeRendered;
    private boolean isInInsertMode;
    private Point coords;
}