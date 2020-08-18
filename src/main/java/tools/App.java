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
//    Map<String, ObservableValue<Boolean>> map = new HashMap<>();
//
//    public static void main(String[] args) {
//        Application.launch(args);
//    }
//
//    @Override
//    public void start(Stage stage) {
//        // Populate the map with ListView items as its keys and
//        // their selected state as the value
//        map.put("Apple", new SimpleBooleanProperty(false));
//        map.put("Banana", new SimpleBooleanProperty(false));
//        map.put("Donut", new SimpleBooleanProperty(false));
//        map.put("Hash Brown", new SimpleBooleanProperty(false));
//
//        ListView<String> breakfasts = new ListView<>();
//        breakfasts.setPrefSize(200, 120);
//        breakfasts.setEditable(true);
//        breakfasts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//        // Add all keys from the map as items to the ListView
//        breakfasts.getItems().addAll(map.keySet());
//
//        // Create a Callback object
//        Callback<String, ObservableValue<Boolean>> itemToBoolean = (String item) -> map.get(item);
//
//        // Set the cell factory to my CheckBoxListCell implementation
//        breakfasts.setCellFactory(lv -> new CheckBoxListCell<>(itemToBoolean));
//
//        Button printBtn = new Button("Print Selection");
//        printBtn.setOnAction(e -> printSelection());
//
//        VBox root = new VBox(new Label("Breakfasts:"), breakfasts, printBtn);
//        Scene scene = new Scene(root);
//        stage.setScene(scene);
//        stage.setTitle("Using ListView Cell Factory");
//        stage.show();
//    }
//
//    public void printSelection() {
//        System.out.println("Selected items: ");
//        for(String key: map.keySet()) {
//            ObservableValue<Boolean> value = map.get(key);
//            if (value.getValue()) {
//                System.out.println(key);
//            }
//        }
//
//        System.out.println();
//    }
//
//    public class MyCell extends CheckBoxListCell<String> {
//        public MyCell(Callback<String, ObservableValue<Boolean>> getSelectedProperty){
//            super(getSelectedProperty);
//        }
//
//        @Override
//        public void updateItem(String item, boolean empty) {
//            super.updateItem(item, empty);
//            // I would expect the following to work
//            setStyle("-fx-background-color: yellow;");
//        }
//    }
}