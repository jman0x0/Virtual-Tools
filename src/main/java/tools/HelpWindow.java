package tools;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class HelpWindow extends BorderPane implements SubWindow {
    public HelpWindow() {
        Utilities.loadController(this, "help.fxml");
    }

    @Override
    public Scene buildScene() {
        return new Scene(this);
    }

    @Override
    public String getTitle() {
        return "Virtual Tools - " + "Help";
    }

    @Override
    public double getWindowWidth() {
        return 540;
    }

    @Override
    public double getWindowHeight() {
        return 270;
    }

    @FXML
    private void close() {
        App.STAGE_STACK.peek().close();
    }
}
