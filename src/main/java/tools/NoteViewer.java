package tools;

import javafx.animation.FadeTransition;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NoteViewer {
    @FXML
    private Region root;

    @FXML
    private TextArea noteArea;

    @FXML
    private TitledPane content;

    private Pane pane = new Pane();

    private Duration duration = Duration.seconds(1.0);

    private FadeTransition transition = new FadeTransition(duration, pane);

    private boolean animating = false;

    private boolean showing = false;

    private double offset;

    private Student student;

    private Notebook notebook;

    @FXML
    private void initialize() {
        root.setVisible(false);
        noteArea.textProperty().addListener(this::updateNotebook);
    }

    public NoteViewer() {
        transition.setOnFinished(fn -> {
            animating = false;
            root.setVisible(showing);
        });
        transition.currentTimeProperty().addListener(this::updatePosition);
    }

    @FXML
    public void show() {
        if (animating) {
            return;
        }
        root.setVisible(true);
        showing = true;
        animating = true;
        transition.setFromValue(0.0);
        transition.setToValue(-root.getWidth());
        transition.play();
    }

    @FXML
    public void hide() {
        if (animating) {
            return;
        }
        showing = false;
        animating = true;
        transition.setFromValue(-root.getWidth());
        transition.setToValue(0.0);
        transition.play();
    }

    public void updatePosition(Observable observable, Duration old, Duration current) {
        final double delta = current.toSeconds() / duration.toSeconds();
        final double ratio = (delta == 1.0) ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * delta);
        final double range = transition.getToValue() - transition.getFromValue();
        root.setLayoutX(transition.getFromValue() + ratio * range + offset);
    }

    public void setStudentAndBook(Student student, Notebook notebook) {
        this.student = student;
        this.notebook = notebook;
        updateNotes();
    }

    public void setOffset(double offset) {
        final double extra = offset - this.offset;
        this.offset = offset;
        root.setLayoutX(root.getLayoutX() + extra);
    }

    private void updateNotes() {
        if (notebook != null && student != null) {
            noteArea.setText(notebook.getNote(student));;
        }
    }

    private void updateNotebook(Observable observable, String old, String current) {
        if (notebook != null || student != null) {
            notebook.setNote(student, current);
        }
    }

    @FXML
    private void credit(ActionEvent actionEvent) {
        if (notebook != null || student != null) {
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd@hh:mm a").format(Calendar.getInstance().getTime());
            noteArea.setText(noteArea.getText() + '\n' + "+1 Credit " + timeStamp);
        }
    }
}
