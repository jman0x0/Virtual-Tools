package tools;


import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Picker extends StackPane {
    @FXML
    private ImageView hat;

    @FXML
    private Label picked;

    @FXML
    private CheckBox present;

    @FXML
    private AnimatedImageView gifView;

    private static Student ACTIVE = null;

    private static ArrayList<Student> SHUFFLED = new ArrayList<>();

    private final PrimaryController controller;

    private NoteViewer noteViewer;

    private boolean animating = false;

    public Picker(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "picker.fxml");
        controller.listenToClassChange((observableValue, s, t1) -> {
            restock();
        });
        controller.listenToOrderChange((observableValue, toggle, t1) -> {
            picked.setText(Utilities.reverseName(picked.getText()));
        });
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("note_viewer.fxml"));

        try {
            noteViewer = loader.load();
            noteViewer.setManaged(false);
            noteViewer.resize(250, 450);
            super.getChildren().add(noteViewer);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        widthProperty().addListener((observableValue, number, t1) -> {
            noteViewer.setOffset(t1.doubleValue());
        });
        heightProperty().addListener((observableValue, number, t1) -> {
            noteViewer.resize(noteViewer.getWidth(), t1.doubleValue());
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
        gifView.setPathway("confetti/confetti-");

    }

    @FXML
    private void pickStudent() {
        if (animating) {
            return;
        }
        if (SHUFFLED.isEmpty()) {
            restock();
        }
        animating = true;
        final int cycles = 10;
        final double angle = 30.0;
        final Duration duration = Duration.seconds(.25);
        final RotateTransition rotate = new RotateTransition();
        rotate.setFromAngle(-angle);
        rotate.setToAngle(angle);
        rotate.setDuration(duration);
        rotate.setAutoReverse(true);
        rotate.setCycleCount(cycles);
        rotate.setNode(hat);
        final ScaleTransition scale = new ScaleTransition();
        scale.setFromY(.95);
        scale.setInterpolator(new Interpolator() {
            @Override
            protected double curve(double t) {
                return -0.5 * (Math.cos(t * Math.PI) - 1);
            }
        });
        scale.setToY(1.05);
        scale.setFromX(.9);
        scale.setToX(1.1);
        scale.setDuration(duration);
        scale.setAutoReverse(true);
        scale.setCycleCount(cycles);
        scale.setNode(hat);

        rotate.play();
        scale.play();
        rotate.setOnFinished(actionEvent -> {
            final RotateTransition finish = new RotateTransition();
            finish.setFromAngle(-angle);
            finish.setToAngle(0);
            finish.setDuration(duration.divide(2));
            finish.setNode(hat);
            finish.play();
            finish.setOnFinished(this::finalizePick);
        });

    }

    private void finalizePick(ActionEvent actionEvent) {
        gifView.play();
        gifView.setTranslateY(-240);
        if (!SHUFFLED.isEmpty()) {
            final Student student = SHUFFLED.remove(SHUFFLED.size() - 1);
            setStudent(student);
        }
        animating = false;
    }

    private void restock() {
        final var classroom = controller.getActiveClassroom();
        if (classroom == null) {
            return;
        }
        if (!present.isSelected()) {
            SHUFFLED = new ArrayList<>(classroom.getStudents());
        }
        else {
            SHUFFLED = classroom.getAttendanceSheet().getRoster(AttendanceSheet.Filter.PRESENT);
        }
        Collections.shuffle(SHUFFLED);
    }

    private void setStudent(Student student) {
        ACTIVE = student;

        if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
            picked.setText(student.getFirstLast());
        }
        else {
            picked.setText(student.getLastFirst());
        }
    }

    @FXML
    private void noteStudent() {
        noteViewer.show();
    }
}
