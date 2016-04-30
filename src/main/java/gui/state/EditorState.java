package gui.state;

import syntax.document.SupportedSyntax;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by avyatkin on 22/04/16.
 */
public class EditorState {
    public EditorState(SupportedSyntax syntax) {
        textStorage = new EditorTextStorage(syntax);
        caret = new Caret();
        selection = new Selection();
    }

    EditorTextStorage textStorage;

    Caret caret;

    Selection selection;

    @Deprecated
    public EditorTextStorage getTextStorage() {
        return textStorage;
    }

    @Deprecated
    public Caret getCaret() {
        return caret;
    }

    @Deprecated
    public Selection getSelection() {
        return selection;
    }

    public void changeSyntax(SupportedSyntax syntax) {
        textStorage.changeSyntax(syntax);
    }

    public void paste(String text) {
        selection.removeTextUnderSelection();
        textStorage.addText(caret.relativePosition, text);
        caret.setRelativePosition(textStorage.horizontalMove(caret.relativePosition, text.length()), false);
    }

    public void type(char typed) {
        if (!selection.isEmpty()) selection.removeTextUnderSelection();
        else if (caret.insertMode &&
                typed != '\n' &&
                textStorage.getLines().get(caret.relativePosition.y).length() > caret.relativePosition.x)
            textStorage.removeText(caret.positionAfterCaret(), 1);
        textStorage.addText(caret.relativePosition, typed + "");
        caret.move(CaretDirection.RIGHT, false);
    }

    public void moveCaret(Point coords, boolean extendSelection) {
        caret.setRelativePosition(textStorage.closestCaretPosition(coords), extendSelection);
    }

    public void moveCaret(CaretDirection direction, boolean extendSelection) {
        if (extendSelection) {
            caret.move(direction, true);
        } else {
            if (!selection.isEmpty()) {
                if (direction.getKeyCode() == KeyEvent.VK_LEFT)
                    caret.setRelativePosition(selection.startPoint(), false);
                else if (direction.getKeyCode() == KeyEvent.VK_RIGHT)
                    caret.setRelativePosition(selection.endPoint(), false);
                else caret.move(direction, false);
                selection.dropSelection();

            } else {
                caret.move(direction, false);
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
            caret.move(CaretDirection.LEFT, false);
            textStorage.removeText(caret.positionAfterCaret(), 1);
        } else selection.removeTextUnderSelection();
    }

    public Optional<String> getSelectedText() {
        if (selection.isEmpty()) return Optional.empty();
        return Optional.of(textStorage.getText(selection.startPoint(), selection.endPoint()));
    }

    public void pageUp(Rectangle relativeVisible) {
        moveCaretToClosestPosition(new Point(relativeVisible.x, relativeVisible.y - relativeVisible.height));
    }

    public void pageDown(Rectangle relativeVisible) {
        moveCaretToClosestPosition(new Point(relativeVisible.x, relativeVisible.y + 2 * relativeVisible.height));
    }

    public void goToBeginning() {
        moveCaretToClosestPosition(textStorage.beginningOfText());
    }

    public void goToEnd() {
        moveCaretToClosestPosition(textStorage.endOfText());
    }

    private void moveCaretToClosestPosition(Point toMoveTo) {
        caret.setRelativePosition(textStorage.closestCaretPosition(toMoveTo), false);
    }


//    handled by pageUp/pageDown
//    public void moveScreen(boolean down, boolean tillEdge) { }

    //    TODO: NOT PUBLIC
    public class Caret {
        public static final long CARET_BLINK_TIME = 500;
        public static final long CARET_PERSIST_TIME = 1000;

        Caret() {
            lastMoveTimestamp = System.currentTimeMillis();
            relativePosition = new Point(0, 0);
            insertMode = false;
        }

        public void setRelativePosition(Point relativePosition, boolean extendSelection) {
            this.relativePosition = relativePosition;
            this.lastMoveTimestamp = System.currentTimeMillis();
            if (!extendSelection) selection.dropSelection();
        }

        public boolean shouldBeRendered() {
            if (insertMode) return true;
            long timeDiff = System.currentTimeMillis() - lastMoveTimestamp;
            return timeDiff < CARET_PERSIST_TIME || (((timeDiff - CARET_PERSIST_TIME) / CARET_BLINK_TIME) % 2 == 0);
        }

        private long lastMoveTimestamp;

        private Point relativePosition;

        //    should be used in renderer
        @Deprecated
        public boolean isInInsertMode() {
            return insertMode;
        }

        //    should be used in renderer
        @Deprecated
        public Point getRelativePosition() {
            return relativePosition;
        }

        private boolean insertMode;

        public void switchInsertMode() {
            insertMode = !insertMode;
        }

        Point positionAfterCaret() {
            return relativePosition;
        }

        void move(CaretDirection direction, boolean extendSelection) {
            Point updatedPosition = (Point) relativePosition.clone();
            switch (direction) {
                case UP:
                    updatedPosition = textStorage.verticalMove(updatedPosition, -1);
                    break;
                case DOWN:
                    updatedPosition = textStorage.verticalMove(updatedPosition, 1);
                    break;
                case LEFT:
                    updatedPosition = textStorage.horizontalMove(updatedPosition, -1);
                    break;
                case RIGHT:
                    updatedPosition = textStorage.horizontalMove(updatedPosition, 1);
                    break;
            }
            setRelativePosition(updatedPosition, extendSelection);
        }
    }

    public class Selection {
        public Selection() {
            dropSelection();
        }

        public void dropSelection() {
            this.initialPoint = caret.getRelativePosition();
        }

        public boolean isEmpty() {
            return initialPoint.equals(caret.relativePosition);
        }

        public Point startPoint() {
            return isCaretAfterEdge() ? caret.relativePosition : initialPoint;
        }

        public Point endPoint() {
            return isCaretAfterEdge() ? initialPoint : caret.relativePosition;
        }

        private boolean isCaretAfterEdge() {
            return initialPoint.y > caret.relativePosition.y ||
                    (initialPoint.y == caret.relativePosition.y && initialPoint.x > caret.relativePosition.x);
        }

        private void removeTextUnderSelection() {
            textStorage.removeText(this);
            caret.setRelativePosition(startPoint(), false);
        }

        private Point initialPoint;
    }
}
