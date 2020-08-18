package tools;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;

public class Picker extends VBox {
    @FXML
    private Label picked;

    @FXML
    private ToggleButton present;

    private static Student ACTIVE = null;

    private static ArrayList<Student> SHUFFLED = new ArrayList<>();

    private final PrimaryController controller;

    public Picker(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "picker.fxml");
        controller.listenToClassChange((observableValue, s, t1) -> {
            restock();
        });
        controller.listenToOrderChange((observableValue, toggle, t1) -> {
            picked.setText(Utilities.reverseName(picked.getText()));
        });
    }

    @FXML
    protected void initialize() {
        if (ACTIVE != null) {
            setStudent(ACTIVE);
        }
        present.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            restock();
            pickStudent();
        });
    }

    @FXML
    private void pickStudent() {
        if (SHUFFLED.isEmpty()) {
            restock();
        }
        // Restocking might not be successful
        if (!SHUFFLED.isEmpty()) {
            final Student student = SHUFFLED.remove(SHUFFLED.size() - 1);
            setStudent(student);
        }
    }

    private void restock() {
        final String active = controller.getActiveClass();
        if (active == null) {
            return;
        }
        if (!present.isSelected()) {
            SHUFFLED = new ArrayList<>(Classes.CLASS_INFO.get(active));
        }
        else {
            SHUFFLED = Attendance.getRoster(controller, Attendance.Filter.PRESENT);
        }
        Collections.shuffle(SHUFFLED);
    }

    private void setStudent(Student student) {
        ACTIVE = student;

        if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
            picked.setText(student.getReversed());
        }
        else {
            picked.setText(student.toString());
        }
    }
}
