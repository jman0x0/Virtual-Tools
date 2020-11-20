package tools;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;

public class LoadFiles extends BorderPane implements SubWindow {
    @FXML
    private Button dropBox;

    @FXML
    private Button cancel;

    @FXML
    private TextField field;

    private File[] files;

    private boolean multiple;

    private static String lastDirectory = System.getProperty("user.home");

    @FXML
    private void initialize() {
        Draggable.initialize(dropBox);
        Platform.runLater(() -> cancel.requestFocus());
        dropBox.setOnDragDropped(this::dragDropped);
    }

    public LoadFiles(boolean multiple) {
        this.multiple = multiple;
        Utilities.loadController(this, "load_files.fxml");
    }

    public File[] getFiles() {
        return files;
    }

    @Override
    public Scene buildScene() {
        return new Scene(this);
    }

    @Override
    public String getTitle() {
        return "Virtual Tools - Load Files";
    }

    @Override
    public double getWindowWidth() {
        return 480;
    }

    @Override
    public double getWindowHeight() {
        return 320;
    }

    private boolean isValid() {
        if (files != null) {
            for (var file : files) {
                if (!file.exists()) {
                    return false;
                }
            }
        }
        return files != null && files.length > 0;
    }

    @FXML
    private void confirm() {
        if (isValid()) {
            App.STAGE_STACK.peek().close();
        }
    }

    @FXML
    private void cancel() {
        files = null;
        App.STAGE_STACK.peek().close();
    }

    @FXML
    private void chooseFiles() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Attendance Sheets");
        fileChooser.setInitialDirectory(new File(lastDirectory));
        if (!multiple) {
            final var chosen = fileChooser.showOpenDialog(App.STAGE_STACK.peek());
            if (chosen != null) {
                setFiles(toFileArray(chosen));
            }
        }
        else {
            final var chosen = fileChooser.showOpenMultipleDialog(App.STAGE_STACK.peek());
            if (chosen != null && chosen.size() > 0) {
                setFiles(chosen.toArray(File[]::new));
                lastDirectory = chosen.get(0).getParent();
            }
        }
    }

    private void setFiles(File[] files) {
        this.files = files;

        final StringBuilder builder = new StringBuilder();
        for (var file : files) {
            builder.append('\"');
            builder.append(file.getName());
            builder.append('\"').append(' ');
        }
        field.setText(builder.toString());
    }

    private void dragDropped(DragEvent drag) {
        final Dragboard dragboard = drag.getDragboard();
        final boolean success = dragboard.hasFiles();
        if (success) {
            if (multiple) {
                setFiles(dragboard.getFiles().toArray(File[]::new));
            }
            else {
                setFiles(toFileArray(dragboard.getFiles().get(0)));
            }
        }
        drag.setDropCompleted(success);
        drag.consume();
    }

    private static File[] toFileArray(File file) {
        final File[] array = new File[1];
        array[0] = file;
        return array;
    }
}
