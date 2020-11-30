package tools;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.util.Stack;

public class App extends Application {
    public static final Stack<Stage> STAGE_STACK = new Stack<>();
    private static PrimaryController primary = null;
    @Override
    public void start(Stage primaryStage) throws IOException {
        STAGE_STACK.push(primaryStage);
        final double minimumSize = 670.0;
        primaryStage.setMinHeight(minimumSize);
        primaryStage.setMinWidth(minimumSize);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        final Parent root = loader.load();
        primaryStage.setTitle("Virtual Tools");
        primaryStage.getIcons().addAll(new Image(getClass().getResourceAsStream("tools.png")));
        primaryStage.setScene(new Scene(root, 900, 0));
        primary = loader.getController();
        primaryStage.show();
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // ignore ... this is catastrophic
            }
        }
    }

    public static void main(String[] args) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("error_log.txt");
            //System.setErr(new PrintStream(fos));
        }
        catch (FileNotFoundException ignored) {

        }
        finally {
            launch();
            closeQuietly(fos);
            if (primary != null) {
                primary.shutdown();
            }
        }
    }
}
