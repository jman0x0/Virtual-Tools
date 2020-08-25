package tools;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class Notes extends VBox {
    private final PrimaryController controller;

    public Notes(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "notes.fxml");
    }

    @FXML
    private void initialize() {

    }
}
