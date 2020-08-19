package tools;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Attendance extends VBox {
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

    private AttendanceSheet sheet;

    private static final ArrayList<String> NAME_LIST = new ArrayList<>();

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

        if (!NAME_LIST.isEmpty()) {
            updateNames();
        }
        display.setEditable(true);
        final Callback<Student, ObservableValue<Boolean>> itemToBoolean = (Student student) -> {
            return sheet == null ? null : sheet.get(student);
        };
        controller.listenToClassChange((observableValue, old, current) -> {
            sheet = controller.getActiveClassroom().getAttendanceSheet();
            updateNames();
        });
        controller.listenToOrderChange((observableValue, old, current) -> {
            final HashMap<Student, String[]> names = new HashMap<>();
            for (var student : students) {
                if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                    names.put(student, Utilities.extractWords(student.getFirstLast()));
                }
                else {
                    names.put(student, Utilities.extractWords(student.getLastFirst()));
                }
            }
            Collections.sort(students, new Comparator<Student>() {
                @Override
                public int compare(Student lhs, Student rhs) {
                    return Arrays.compare(names.get(lhs), names.get(rhs));
                }
            });
        });
        display.setCellFactory(factory -> new StudentCell(itemToBoolean, controller));
        updateNames();
    }

    public Attendance(PrimaryController controller, AttendanceSheet sheet) {
        this.controller = controller;
        this.sheet = sheet;
        Utilities.loadController(this, "attendance.fxml");
    }

    public void updateAttendanceSheet() {
        final var setting = AttendanceSheet.Filter.fromToggleGroup(filter, complete, present, absent);
        final var roster  = sheet.getRoster(AttendanceSheet.Filter.COMPLETE);
        for (String fullName : NAME_LIST) {
            final String[] split = Utilities.extractWords(fullName);
            processName(split, roster, setting);
        }
    }

    public void loadAttendanceSheet(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            NAME_LIST.clear();

            while (reader.ready()) {
                final String line = reader.readLine();
                if (line.isEmpty()) {
                    continue;
                }
                final String name = line.substring(0, line.indexOf(','));
                if (name.contains("(")) {
                    continue;
                }
                NAME_LIST.add(name);
            }
            loaded.setText(file.getPath());
            updateAttendanceSheet();
            updateNames();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public void attachListeners() {
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

    public void updateNames() {
        if (sheet == null) {
            return;
        }
        clearDisplay();
        final var setting = AttendanceSheet.Filter.fromToggleGroup(filter, complete, present, absent);
        final var roster = sheet.getRoster(setting);
        if (roster != null) {
            students.addAll(roster);
        }
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
}
