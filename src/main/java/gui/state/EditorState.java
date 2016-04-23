package gui.state;

import java.util.List;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by avyatkin on 22/04/16.
 */
public class EditorState {
    public EditorState() {
        textStorage = new EditorTextStorage();
        caret = new Caret();
        selection = new Selection();
    }

    EditorTextStorage textStorage;

    Caret caret;

    Selection selection;

    @Deprecated
    public EditorTextStorage getTextStorage() { return textStorage; }

    @Deprecated
    public Caret getCaret() { return caret; }

    @Deprecated
    public Selection getSelection() { return selection; }

    public void paste(String text) {
        selection.removeTextUnderSelection();
        textStorage.addText(caret.relativePosition, text);
    }

    public void type(char typed) {
        if (!selection.isEmpty()) selection.removeTextUnderSelection();
        else if (caret.insertMode &&
                typed != '\n' &&
                textStorage.getLines().get(caret.relativePosition.y).length() > caret.relativePosition.x)
            textStorage.removeText(caret.positionAfterCaret(), 1);
        textStorage.addText(caret.relativePosition, typed + "");
        caret.move(CaretDirection.RIGHT);
    }

    public void moveCaret(Point coords, boolean extendSelection) {
        caret.setRelativePosition(textStorage.closestCaretPosition(coords));
        if (extendSelection) selection.extendSelection();
        else selection.dropSelection();
    }

    public void moveCaret(CaretDirection direction, boolean extendSelection) {
        if (extendSelection) {
            caret.move(direction);
            selection.extendSelection();
        } else {
            if (!selection.isEmpty()) {
                if (direction.getKeyCode() == KeyEvent.VK_LEFT)
                    caret.relativePosition = selection.startPoint();
                else if (direction.getKeyCode() == KeyEvent.VK_RIGHT)
                    caret.relativePosition = selection.endPoint();
                else caret.move(direction);
                selection.dropSelection();

            } else {
                caret.move(direction);
            }
        }
    }

    public void switchCaretMode() {
        caret.switchInsertMode();
    }

    public void delete() {
        if (caret.relativePosition.equals(textStorage.endOfText()) && selection.isEmpty())
            return;

        if (selection.isEmpty()) textStorage.removeText(caret.positionAfterCaret(), 1);
        else selection.removeTextUnderSelection();
    }

    public void backspace() {
        if (caret.relativePosition.equals(textStorage.beginningOfText()) && selection.isEmpty())
            return;
        if (selection.isEmpty()) {
            caret.move(CaretDirection.LEFT);
            textStorage.removeText(caret.positionAfterCaret(), 1);
        } else selection.removeTextUnderSelection();
    }

    public Optional<String> getSelectedText() {
        if (selection.isEmpty()) return Optional.empty();
        return Optional.of(textStorage.getText(selection.startPoint(), selection.endPoint()));
    }

//    handled by pageUp/pageDown
//    public void moveScreen(boolean down, boolean tillEdge) { }

//    TODO: NOT PUBLIC
    public class Caret {
        Caret() {
            relativePosition = new Point(0, 0);
            insertMode = false;
        }

        public void setRelativePosition(Point relativePosition) { this.relativePosition = relativePosition; }

        private Point relativePosition;

//    should be used in renderer
    @Deprecated
    public boolean isInInsertMode() { return insertMode; }

//    should be used in renderer
    @Deprecated
    public Point getRelativePosition() { return relativePosition; }

    private boolean insertMode;

        public void switchInsertMode() {
            insertMode = !insertMode;
        }

        Point positionAfterCaret() { return relativePosition; }

        void move(CaretDirection direction) {
            Point updatedPosition = (Point) relativePosition.clone();
            switch (direction) {
                case UP:
                    updatedPosition = textStorage.verticalMove(updatedPosition, -1); break;
                case DOWN:
                    updatedPosition = textStorage.verticalMove(updatedPosition, 1); break;
                case LEFT:
                    updatedPosition = textStorage.horizontalMove(updatedPosition, -1); break;
                case RIGHT:
                    updatedPosition = textStorage.horizontalMove(updatedPosition, 1); break;
            }
            setRelativePosition(updatedPosition);
        }
    }

    public class Selection {
        public Selection() { dropSelection(); }

        public void dropSelection(Point point) {
            this.initialPoint = point;
            this.edgePoint = point;
        }

        public void dropSelection() { dropSelection(caret.relativePosition); }

        public void extendSelection() { edgePoint = caret.relativePosition; }

        public void switchCaretsWithCurrent() {
            edgePoint = initialPoint;
            initialPoint = caret.relativePosition;
        }

        public boolean isEmpty() { return initialPoint.equals(edgePoint); }

        public Point startPoint() { return isInitialAfterEdge() ? edgePoint : initialPoint; }

        public Point endPoint() { return isInitialAfterEdge() ? initialPoint : edgePoint; }

        private boolean isInitialAfterEdge() {
            return initialPoint.y > edgePoint.y || (initialPoint.y == edgePoint.y && initialPoint.x > edgePoint.x);
        }

        private void removeTextUnderSelection() {
            textStorage.removeText(this);
            dropSelection(startPoint());
            caret.relativePosition = startPoint();
        }

        private Point initialPoint;
        private Point edgePoint;
    }
}
