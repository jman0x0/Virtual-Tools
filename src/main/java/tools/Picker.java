package tools;


import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Stack;

public class Picker extends StackPane implements Refreshable {
    @FXML
    private ImageView hat;

    @FXML
    private Label picked;

    @FXML
    private CheckBox present;

    @FXML
    private CheckBox animation;

    @FXML
    private AnimatedImageView gifView;

    @FXML
    private Spinner<Integer> macroMagnitude;

    @FXML
    private Label notification;

    @FXML
    private Button add;

    @FXML
    private Button minus;

    private final ControllerState state = new ControllerState();

    private Student student = null;

    private Stack<Student> shuffle = new Stack<>();

    private final PrimaryController controller;

    private NoteViewer noteViewer;

    private Region noteModel;

    private boolean animating = false;

    public Picker(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "picker.fxml");
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("note_viewer.fxml"));
        try {
            noteModel = loader.load();
            noteModel.setManaged(false);
            noteViewer = loader.getController();
            super.getChildren().add(noteModel);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        widthProperty().addListener((observableValue, number, t1) -> {
            noteViewer.setOffset(t1.doubleValue());
        });
        heightProperty().addListener((observableValue, number, t1) -> {
            noteModel.resize(250, t1.doubleValue());
        });
        add.setDisable(true);
        minus.setDisable(true);
    }

    @FXML
    protected void initialize() {
        present.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            restock();
        });
        gifView.setPathway("confetti/confetti-");
    }

    @FXML
    private void pickStudent() {
        if (animating) {
            return;
        }
        animating = true;
        if (!animation.isSelected()) {
            finalizePick();
        }
        else {
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
                finish.setOnFinished(action -> finalizePick());
            });
        }
    }

    private void finalizePick() {
        if (animation.isSelected()) {
            gifView.play();
            gifView.setScaleX(1.34);
            gifView.setLayoutX(hat.getLayoutX() - hat.getImage().getWidth());
            gifView.setLayoutY(-hat.getImage().getHeight() + 40);
        }
        setStudent(fetchStudent());
        animating = false;
    }

    private Student fetchStudent() {
        if (shuffle.isEmpty() && !restock()) {
            return null;
        }
        final Classroom classroom = controller.getActiveClassroom();
        final AttendanceSheet attendance = classroom.getAttendanceSheet();
        final boolean presentOnly = present.isSelected();
        while (!shuffle.isEmpty()) {
            final Student student = shuffle.pop();

            if (!presentOnly || attendance.get(student).getValue()) {
                return student;
            }
        }
        return null;
    }

    private boolean restock() {
        final Classroom classroom = controller.getActiveClassroom();
        if (!present.isSelected()) {
            shuffle.addAll(classroom.getStudents());
        }
        else {
            shuffle.addAll(classroom.getAttendanceSheet().getRoster(AttendanceSheet.Filter.PRESENT));
        }
        Collections.shuffle(shuffle);
        return !shuffle.isEmpty();
    }

    private void setStudent(Student student) {
        this.student = student;
        add.setDisable(student == null);
        minus.setDisable(student == null);
        if (student == null) {
            picked.setText("*NO STUDENT*");
        }
        else if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
            picked.setText(student.getFirstLast());
        }
        else {
            picked.setText(student.getLastFirst());
        }

        final Classroom classroom = controller.getActiveClassroom();
        if (noteViewer != null) {
            noteViewer.setStudentAndBook(this.student, classroom.getNotebook());
        }
        notification.setText(null);
    }

    public void addPoint(Integer value) {
        if (student == null) {
            return;
        }
        noteViewer.creditStudent(student, value);
        if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
            notification.setText(value + " point to " + student.getFirstLast());
        }
        else {
            notification.setText(value + " point to " + student.getLastFirst());
        }
    }

    @FXML
    private void noteStudent() {
        if (student == null) {
            return;
        }
        final Notebook notebook = controller.getActiveClassroom().getNotebook();
        noteViewer.setStudentAndBook(student, notebook);
        noteViewer.show();
    }

    @FXML
    private void increment() {
        addPoint(macroMagnitude.getValue());
    }

    @FXML
    private void decrement() {
        addPoint(-macroMagnitude.getValue());
    }

    @Override
    public void classChanged(Classroom classroom) {
        restock();
    }

    @Override
    public void orderChanged(PrimaryController.Order order) {
        picked.setText(Utilities.reverseName(picked.getText()));
    }

    @Override
    public void dateChanged(LocalDate date) {

    }

    @Override
    public ControllerState getControllerState() {
        return state;
    }
}
