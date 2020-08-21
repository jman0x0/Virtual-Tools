package tools;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PrimaryController {
    enum Order {
        FIRST_LAST,
        LAST_FIRST
    }

    private Map<String, Classroom> classes = new LinkedHashMap<>();

    @FXML
    private ToggleGroup order;

    @FXML
    private ToggleButton firstLast;

    @FXML
    private ToggleButton lastFirst;

    @FXML
    public BorderPane borderPane;

    @FXML
    private ListView<NavigationData> navigationList;

    @FXML
    private ComboBox<String> classChoices;

    @FXML
    protected void initialize() {
        navigationList.setCellFactory(cell -> new NavigationCell());

        navigationList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final var classroom = getActiveClassroom();

            switch (newValue.getName().toUpperCase()) {
                case "CLASSES":
                    borderPane.setCenter(new Classes(this));
                    break;
                case "PICKER":
                    borderPane.setCenter(new Picker(this));
                    break;
                case "GROUPER":
                    borderPane.setCenter(new Group(this));
                    break;
                case "ATTENDANCE":
                    borderPane.setCenter(new Attendance(this, classroom == null ? null : classroom.getAttendanceSheet()));
                    break;
            }
        });

        order.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
        });
        load("configuration.json");
        App.STAGE_STACK.peek().setOnCloseRequest(windowEvent -> {
            save("configuration.json");
        });
        navigationList.getSelectionModel().select(0);
    }

    @FXML
    public void displayHelp(ActionEvent actionEvent) {
       HelpWindow help = new HelpWindow();
       help.display(App.STAGE_STACK.peek());
    }

    public Set<String> getClassSet() {
        return classes.keySet();
    }

    public Classroom addClass(String name, Classroom classroom) {
        classChoices.getItems().add(name);

        if (classChoices.getSelectionModel().isEmpty()) {
            classChoices.getSelectionModel().select(0);
        }
        return classes.put(name, classroom);
    }

    public Classroom addClass(String name) {
        classChoices.getItems().add(name);

        if (classChoices.getSelectionModel().isEmpty()) {
            classChoices.getSelectionModel().select(0);
        }
        return classes.put(name, new Classroom(name));
    }

    public void setDisplay(Node node) {
        borderPane.setCenter(node);
    }

    public void listenToClassChange(ChangeListener<? super String> listener) {
        classChoices.valueProperty().addListener(listener);
    }

    public void listenToOrderChange(ChangeListener<? super Toggle> listener) {
        order.selectedToggleProperty().addListener(listener);
    }

    public Order getOrder() {
        final var ordering = order.getSelectedToggle();

        if (ordering == firstLast) {
            return Order.FIRST_LAST;
        }
        else {
            return Order.LAST_FIRST;
        }
    }

    public void removeClass(String name) {
        classChoices.getItems().remove(name);
        classes.remove(name);
        if (!classChoices.getItems().isEmpty()) {
            classChoices.getSelectionModel().select(0);
        }
    }

    public void updateClass(String old, String current) {
        final int index = classChoices.getItems().indexOf(old);
        if (index >= 0) {
            classChoices.getItems().set(index, current);
            final var removed = classes.remove(old);
            classes.put(current, removed);
        }
    }

    public String getActiveClass() {
        final int index = classChoices.getSelectionModel().getSelectedIndex();

        if (index >= 0) {
            return classChoices.getItems().get(index);
        }
        return null;
    }

    public Classroom getClassroom(String name) {
        return classes.get(name);
    }

    public Classroom getActiveClassroom() {
        return classes.get(getActiveClass());
    }

    public void clearClassroom() {
        classes.clear();
        classChoices.getItems().clear();
    }

    public Set<Map.Entry<String, Classroom>> getClassroomEntries() {
        return classes.entrySet();
    }

    public void load(String fileName) {
        try {
            if (!new File(fileName).exists()) {
                return;
            }
            final String contents = Files.readString(Paths.get(fileName));
            final JSONObject object = new JSONObject(contents);
            final JSONObject classes = object.getJSONObject("classes");
            Iterator<String> keys = classes.keys();
            while (keys.hasNext()) {
                final String subject = keys.next();
                final JSONArray roster = classes.getJSONArray(subject);
                final Classroom classroom = new Classroom(subject);
                final AttendanceSheet attendance = classroom.getAttendanceSheet();
                for (int i = 0; i < roster.length(); ++i) {
                    final JSONObject data = roster.getJSONObject(i);
                    final String name = data.getString("name");
                    final boolean present = data.getBoolean("present");
                    final Student student = new Student(name);
                    classroom.add(student);
                    attendance.mark(student, present);
                }
                addClass(subject, classroom);
            }
        }
        catch (JSONException | IOException jse) {
            jse.printStackTrace();
        }
    }

    public void save(String fileName) {
        FileWriter writer = null;
        try {
            final JSONObject object = new JSONObject();
            final JSONObject classData = new JSONObject();
            for (var entry : classes.entrySet()) {
                final JSONArray roster = new JSONArray();
                final  var attendance = entry.getValue().getAttendanceSheet();
                for (Student student : entry.getValue()) {
                    final JSONObject data = new JSONObject();
                    data.put("name", student.toString());
                    data.put("present", attendance.get(student).getValue());
                    roster.put(data);
                }
                classData.put(entry.getKey(), roster);
            }
            object.put("classes", classData);
            writer = new FileWriter(new File(Paths.get(fileName).toUri()));
            writer.write(object.toString());
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}
