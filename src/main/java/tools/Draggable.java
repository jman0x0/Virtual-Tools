package tools;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

public class Draggable {
    private static final String DRAGGABLE_CLASS = "draggable";
    private static final PseudoClass ARMED = PseudoClass.getPseudoClass("armed");

    public static void initialize(Node node) {
        node.getStyleClass().add(DRAGGABLE_CLASS);

        node.setOnDragOver((dragEvent) -> {
            Draggable.dragOver(dragEvent, node);
        });
        node.setOnDragEntered((dragEvent) -> {
            Draggable.dragEntered(dragEvent, node);
        });
        node.setOnDragExited((dragEvent) -> {
            Draggable.dragExited(dragEvent, node);
        });
    }

    public static void dragOver(DragEvent drag, Node node) {
        if (drag.getGestureSource() != node && drag.getDragboard().hasFiles()) {
            drag.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        drag.consume();
    }

    public static void dragEntered(DragEvent drag, Node node) {
        if (drag.getGestureSource() != node && drag.getDragboard().hasFiles()) {
            node.pseudoClassStateChanged(ARMED, true);
        }
        drag.consume();
    }

    public static void dragExited(DragEvent drag, Node node) {
        node.pseudoClassStateChanged(ARMED, false);
        drag.consume();
    }
}
