package tools;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public interface SubWindow {
    default void display(Stage parent) {
        final Stage stage = buildStage(parent);
        final Scene scene = buildScene();
        App.STAGE_STACK.push(stage);
        stage.setTitle(getTitle());
        stage.setScene(scene);
        stage.showAndWait();
        App.STAGE_STACK.pop();
    }

    private Stage buildStage(Stage parent) {
        final Stage stage = new Stage();
        final double width = getWindowWidth();
        final double height = getWindowHeight();
        final double minWidth = getMinimumWidth();
        final double minHeight = getMinimumHeight();

        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parent);
        stage.setWidth(width);
        stage.setMinWidth(minWidth);
        stage.setHeight(height);
        stage.setMinHeight(minHeight);
        stage.getIcons().addAll(parent.getIcons());
        return stage;
    }

    Scene buildScene();

    String getTitle();

    double getWindowWidth();

    double getWindowHeight();

    default double getMinimumWidth() {
        return getWindowWidth();
    }

    default double getMinimumHeight() {
        return getWindowHeight();
    }
}
