package tools;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Stack;

public class App extends Application {
    private static Scene scene;
    public static final Stack<Stage> STAGE_STACK = new Stack<>();

    @Override
    public void start(Stage primaryStage) throws IOException {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        final Parent root = loader.load();
        primaryStage.setTitle("Virtual Tools");
        primaryStage.getIcons().addAll(new Image(getClass().getResourceAsStream("tools.png")));
        scene = new Scene(root, 795, 530);
        STAGE_STACK.push(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        Configuration.load("configuration.json");
        launch();
        Configuration.save("configuration.json");
    }

}