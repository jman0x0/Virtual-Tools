package tools;

import javafx.animation.FadeTransition;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NoteViewer extends VBox {
    @FXML
    private TextArea noteArea;

    @FXML
    private TitledPane content;

    private Pane pane = new Pane();

    private Duration duration = Duration.seconds(1.0);

    private FadeTransition transition = new FadeTransition(duration, pane);

    private boolean animating = false;

    private double offset;

    private Student student;

    private Notebook notebook;

    @FXML
    private void initialize() {
        noteArea.textProperty().addListener(this::updateNotebook);
        heightProperty().addListener((observableValue, number, t1) -> {
            content.resize(getWidth(), t1.doubleValue());
        });
    }

    public NoteViewer() {
        transition.setOnFinished(fn -> animating = false);
        transition.currentTimeProperty().addListener(this::updatePosition);
    }

    @FXML
    public void show() {
        if (animating) {
            return;
        }
        animating = true;
        transition.setFromValue(0.0);
        transition.setToValue(-getWidth());
        transition.play();
    }

    @FXML
    public void hide() {
        System.out.println(getLayoutX());
        if (animating) {
            return;
        }
        animating = true;
        transition.setToValue(0.0);
        transition.setFromValue(-getWidth());
        transition.play();
    }

    public void updatePosition(Observable observable, Duration old, Duration current) {
        final double t = current.toSeconds() / duration.toSeconds();
        final double ratio = (t == 1.0) ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * t);
        setLayoutX(ratio * transition.getToValue() + offset);
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setNotebook(Notebook notebook) {
        this.notebook = notebook;
    }

    public void setOffset(double horizontal) {
        this.offset = horizontal;
    }

    private void updateNotebook(Observable observable, String old, String current) {
        if (notebook == null || student == null) {
            return;
        }
        notebook.setNote(student, current);
    }
}
