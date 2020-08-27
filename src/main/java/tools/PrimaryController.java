package tools;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import tools.json.JSONArray;
import tools.json.JSONException;
import tools.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private DatePicker datePicker;

    @FXML
    protected void initialize() {
        navigationList.setCellFactory(cell -> new NavigationCell());

        navigationList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadPane(newValue.getName());
        });

        order.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
        });

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        datePicker.valueProperty().addListener((observable, old, date) -> {
            if (old != null) {
                save("configuration.json", old.format(formatter));
            }
            if (date != null) {
                load("configuration.json", date.format(formatter));
                if (navigationList != null && !navigationList.getSelectionModel().isEmpty()) {
                    loadPane(navigationList.getSelectionModel().getSelectedItem().getName());
                }
            }
        });
        App.STAGE_STACK.peek().setOnCloseRequest(windowEvent -> {
            save("configuration.json", datePicker.getValue().format(formatter));
        });
        datePicker.setValue(LocalDate.now());
        if (classes.isEmpty()) {
            loadLastRoster("configuration.json");
        }
        navigationList.getSelectionModel().select(0);
    }

    @FXML
    public void displayHelp(@SuppressWarnings("unnused") ActionEvent actionEvent) {
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

    public void loadPane(String pane) {
        final var classroom = getActiveClassroom();

        switch (pane.toUpperCase()) {
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
            case "NOTES":
                borderPane.setCenter(new Notes(this));
                break;
        }
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

    public void load(String fileName, String date) {
        try {
            clearClassroom();
            if (!new File(fileName).exists()) {
                return;
            }
            final String contents = Files.readString(Paths.get(fileName));
            final JSONObject object = new JSONObject(contents);
            if (object.has(date)) {
                loadClassObject(object.getJSONObject(date), false);
            }
        }
        catch (JSONException | IOException jse) {
            jse.printStackTrace();
        }
    }


    public void save(String fileName, String date) {
        FileWriter writer = null;
        try {
            final JSONObject root = fetchRoot(fileName);
            final JSONObject selection = new JSONObject();
            final JSONObject classData = new JSONObject();
            for (var entry : classes.entrySet()) {
                final JSONArray roster = new JSONArray();
                final AttendanceSheet attendance = entry.getValue().getAttendanceSheet();
                final Notebook notebook = entry.getValue().getNotebook();
                for (Student student : entry.getValue()) {
                    final JSONObject data = new JSONObject();
                    final boolean present = attendance.get(student).getValue();
                    data.put("name", student.toString());
                    if (present) {
                        data.put("present", true);
                    }
                    if (!notebook.getNote(student).isEmpty()) {
                        data.put("note", notebook.getNote(student));
                    }
                    roster.put(data);
                }
                classData.put(entry.getKey(), roster);
            }
            selection.put("classes", classData);
            if (classes.isEmpty()) {
                root.remove(date);
            }
            else {
                root.put(date, selection);
            }
            writer = new FileWriter(new File(Paths.get(fileName).toUri()));
            writer.write(root.toString());
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

    private void loadLastRoster(String fileName) {
        try {
            if (!new File(fileName).exists()) {
                return;
            }
            final String contents = Files.readString(Paths.get(fileName));
            final JSONObject object = new JSONObject(contents);
            Iterator<String> dates = object.keys();
            String access = null;
            Date previous = null;
            final Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
            while (dates.hasNext()) {
                final String key = dates.next();
                final Date date = new SimpleDateFormat("MM/dd/yyyy").parse(key);
                if (previous == null || date.after(previous) && date.before(today)) {
                    previous = date;
                    access = key;
                }
            }
            if (access != null) {
                loadClassObject(object.getJSONObject(access), true);
            }
        }
        catch (IOException | ParseException ioe) {
            ioe.printStackTrace();
        }
    }

    private JSONObject fetchRoot(String fileName) {
        try {
            return new JSONObject(Files.readString(Paths.get(fileName)));
        }
        catch (IOException ignored) {
            return new JSONObject();
        }
    }

    private void loadClassObject(JSONObject selected, boolean rosterOnly) throws JSONException {
        final JSONObject classes = selected.getJSONObject("classes");
        Iterator<String> keys = classes.keys();
        while (keys.hasNext()) {
            final String subject = keys.next();
            final JSONArray roster = classes.getJSONArray(subject);
            final Classroom classroom = new Classroom(subject);
            final AttendanceSheet attendance = classroom.getAttendanceSheet();
            final Notebook notebook = classroom.getNotebook();
            for (int i = 0; i < roster.length(); ++i) {
                final JSONObject data = roster.getJSONObject(i);
                final String name = data.getString("name");

                final Student student = new Student(name);
                if (!rosterOnly && data.has("note")) {
                    notebook.setNote(student, data.getString("note"));
                }
                classroom.add(student);
                if (!rosterOnly && data.has("present")) {
                    attendance.mark(student, data.getBoolean("present"));
                }
            }
            addClass(subject, classroom);
        }
    }
}
