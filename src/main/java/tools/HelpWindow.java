package tools;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class HelpWindow extends BorderPane implements SubWindow {
    @FXML
    private TabPane tabPane;

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
        return 560;
    }

    @Override
    public double getWindowHeight() {
        return 315 ;
    }

    @FXML
    private void initialize() {
        final var tabs = tabPane.getTabs();

        for (Tab tab : tabs) {
            final Node node = tab.contentProperty().get();
            if (node instanceof Parent) {
                final Parent parent = (Parent)node;

                for (var child : parent.getChildrenUnmodifiable()) {
                    if (child instanceof Region) {
                        ((Region)child).maxWidthProperty().bind(tabPane.widthProperty());
                    }
                }
            }

        }
    }

    @FXML
    private void close() {
        App.STAGE_STACK.peek().close();
    }
}
