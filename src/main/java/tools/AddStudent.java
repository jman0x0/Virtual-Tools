package tools;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class AddStudent implements SubWindow {
    @FXML
    private VBox root;

    @FXML
    private TextField fullName;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private RadioButton firstLast;

    @FXML
    private RadioButton lastFirst;

    @FXML
    private ToggleGroup format;

    private Student student;

    @FXML
    private void initialize() {
        format.selectedToggleProperty().addListener(observable -> updateNameFields());
        fullName.textProperty().addListener(observable -> updateNameFields());
    }

    @Override
    public Scene buildScene() {
        return new Scene(root);
    }

    @Override
    public String getTitle() {
        return "Virtual Tools - Add Student";
    }

    @Override
    public double getWindowWidth() {
        return 400;
    }

    @Override
    public double getWindowHeight() {
        return 225;
    }

    public Student getStudent() {
        return student;
    }

    @FXML
    public void close() {
        App.STAGE_STACK.peek().close();
    }

    @FXML
    public void confirm() {
        try {
            if (!fullName.getText().isEmpty()) {
                final String text = fullName.getText();
                final Toggle toggled = format.getSelectedToggle();
                final boolean reverse = toggled == firstLast;
                final String name = reverse ? Utilities.reverseName(text) : text;
                student = new Student(name);
                close();
            }
        }
        catch (IllegalArgumentException ignored) {

        }
    }

    private void updateNameFields() {
        try {
            final String name = fullName.getText();
            final Student student = new Student(name);
            final String[] nameParts = student.getFirstLast().split(",");
            final Toggle toggled = format.getSelectedToggle();
            if (toggled == firstLast) {
                Utilities.reverseArray(nameParts);
            }
            if (nameParts.length > 1) {
                firstName.setText(nameParts[0].trim());
                lastName.setText(nameParts[1].trim());
            }
            else {
                firstName.setText(null);
                lastName.setText(null);
            }
        }
        catch (IllegalArgumentException ignored) {

        }
    }
}
