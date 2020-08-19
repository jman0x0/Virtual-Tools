package tools;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
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
            classes.put(current, classes.remove(old));
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

    public void load(String fileName) {
        try {
            if (!new File(fileName).exists()) {
                return;
            }
            final String data = Files.readString(Paths.get(fileName));
            final JSONObject object = new JSONObject(data);
            final JSONObject classes = object.getJSONObject("classes");
            Iterator<String> keys = classes.keys();
            while (keys.hasNext()) {
                final String key = keys.next();
                final JSONArray roster = classes.getJSONArray(key);
                final ArrayList<Student> cached = new ArrayList<>();
                cached.ensureCapacity(roster.length());
                for (int i = 0; i < roster.length(); ++i) {
                    cached.add(new Student(roster.getString(i)));
                }
                addClass(key);
                getClassroom(key).setStudents(cached);
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
                for (Student student : entry.getValue()) {
                    roster.put(student.toString());
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
