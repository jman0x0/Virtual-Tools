package tools;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class App extends Application {
    public static final Stack<Stage> STAGE_STACK = new Stack<>();

    @Override
    public void start(Stage primaryStage) throws IOException {
        STAGE_STACK.push(primaryStage);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        final Parent root = loader.load();
        primaryStage.setTitle("Virtual Tools");
        primaryStage.getIcons().addAll(new Image(getClass().getResourceAsStream("tools.png")));
        primaryStage.setScene(new Scene(root, 870, 580));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch();
    }
}
