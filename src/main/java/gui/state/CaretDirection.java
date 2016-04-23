package gui.state;

import java.awt.event.KeyEvent;

/**
 * Created by avyatkin on 22/04/16.
 */
public enum CaretDirection {
    UP, DOWN, LEFT, RIGHT;

    public int getKeyCode() {
        switch (this) {
            case UP: return KeyEvent.VK_UP;
            case DOWN: return KeyEvent.VK_DOWN;
            case LEFT: return KeyEvent.VK_LEFT;
            case RIGHT: return KeyEvent.VK_RIGHT;
        }
        throw new RuntimeException("Missing caret direction!");
    }
}
