package tools;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;


public class Attendance extends VBox implements Refreshable {
    @FXML
    private ObservableList<Student> students;

    @FXML
    private ListView<Student> display;

    @FXML
    private Button loadBox;

    @FXML
    private TextField loaded;

    @FXML
    private ToggleGroup filter;

    @FXML
    private ToggleButton present;

    @FXML
    private ToggleButton complete;

    @FXML
    private ToggleButton absent;

    private final ControllerState state = new ControllerState();

    private AttendanceSheet sheet;

    private PrimaryController controller;

    @FXML
    protected void initialize() {
        filter.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
            else {
                updateNames();
            }
        });

        display.setEditable(true);
        final Callback<Student, ObservableValue<Boolean>> itemToBoolean = (Student student) -> {
            return sheet == null ? null : sheet.get(student);
        };
        display.setCellFactory(factory -> new StudentCell(itemToBoolean, controller));
    }

    public Attendance(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "attendance.fxml");
    }

    public void updateAttendanceSheet(ArrayList<String> fullNames) {
        final var setting = AttendanceSheet.Filter.fromToggleGroup(filter, complete, present, absent);
        final var roster  = sheet.getRoster(AttendanceSheet.Filter.COMPLETE);
        for (String fullName : fullNames) {
            final String[] split = Utilities.extractWords(fullName);
            processName(split, roster, setting);
        }
    }

    public void loadAttendanceSheet(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            final ArrayList<String> fullNames = new ArrayList<>();
            while (reader.ready()) {
                final String line = reader.readLine();
                if (line.isEmpty()) {
                    continue;
                }
                final String name = line.substring(0, line.indexOf(','));
                if (name.contains("(")) {
                    continue;
                }
                fullNames.add(name);
            }
            loaded.setText(file.getPath());
            updateAttendanceSheet(fullNames);
            updateNames();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void listenToSheet() {
        sheet.setOnStudentMarked((student, checked) -> {
            final var setting = AttendanceSheet.Filter.fromToggleGroup(filter, complete, present, absent);
            if (checked && setting == AttendanceSheet.Filter.ABSENT || !checked && setting == AttendanceSheet.Filter.PRESENT) {
                students.remove(student);
            }
        });
    }

    @FXML
    public void loadSheet() {
        final LoadFiles loader = new LoadFiles(false);
        loader.display(App.STAGE_STACK.peek());
        final File[] chosen = loader.getFiles();

        if (chosen != null && chosen.length > 0) {
            loadAttendanceSheet(chosen[0]);
        }
    }

    @FXML
    public void clearAll() {
        sheet.clear();
        updateNames();
    }

    @FXML
    public void checkAll() {
        sheet.checkAll();
        updateNames();
    }

    public void updateNames() {
        clearDisplay();
        final var setting = AttendanceSheet.Filter.fromToggleGroup(filter, complete, present, absent);
        final var roster = sheet.getRoster(setting);
        students.addAll(roster);
    }

    public void clearDisplay() {
        students.clear();
    }

    public void processName(String[] split, ArrayList<Student> roster, AttendanceSheet.Filter filter) {
        final String[] reversed = Arrays.copyOf(split, split.length);
        Utilities.reverseArray(reversed);

        for (var student : roster) {
            if (matchStudent(split, student) || matchStudent(reversed, student)) {
                sheet.get(student).set(true);
                return;
            }
        }
    }

    public static boolean matchStudent(String[] split, Student student) {
        int active = 0;
        for (; active < split.length; ++active) {
            if (!student.firstMatches(split[active])) {
                break;
            }
        }
        if (active >= split.length) {
            return false;
        }
        while (active < split.length) {
            if (!student.lastMatches(split[active++])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void processChanges(EnumSet<ControllerState.Change> changes) {
        final Classroom classroom = controller.getActiveClassroom();
        sheet = classroom.getAttendanceSheet();
        listenToSheet();
        updateNames();
    }

    @Override
    public void classChanged(Classroom classroom) {
        sheet = classroom.getAttendanceSheet();
        listenToSheet();
        updateNames();
    }

    @Override
    public void orderChanged(PrimaryController.Order order) {
        Utilities.sortStudents(students, controller.getOrder());
    }

    @Override
    public void dateChanged(LocalDate date) {

    }

    @Override
    public ControllerState getControllerState() {
        return state;
    }
}
