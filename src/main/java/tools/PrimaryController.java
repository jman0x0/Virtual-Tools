package tools;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrimaryController {
    public enum Order {
        FIRST_LAST,
        LAST_FIRST
    }

    private final Map<String, Classroom> classes = new LinkedHashMap<>();

    private final String CONFIG_FILE = "configuration.json";

    private final HashMap<String, Node> PANES = new HashMap<>();

    private final BooleanProperty classesOnly = new SimpleBooleanProperty(true);

    private final AtomicBoolean downloading = new AtomicBoolean(false);

    private ScheduledExecutorService updater = Executors.newSingleThreadScheduledExecutor();

    private long start = 0;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    private ToggleGroup order;

    @FXML
    private ToggleButton firstLast;

    @FXML
    private ToggleButton lastFirst;

    @FXML
    public SplitPane splitPane;

    @FXML
    public StackPane content;

    @FXML
    private ListView<NavigationData> navigationList;

    @FXML
    private ComboBox<String> classChoices;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button updateButton;

    @FXML
    private ProgressBar updateProgress;

    @FXML
    protected void initialize() {
        pollUpdates();
        PANES.put("CLASSES", new Classes(this));
        PANES.put("PICKER", new Picker(this));
        PANES.put("GROUPER", new Group(this));
        PANES.put("ATTENDANCE", new Attendance(this));
        PANES.put("NOTES", new Notes(this));
        navigationList.setCellFactory(cell -> new NavigationCell(classesOnly));

        navigationList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadPane(newValue.getName());
        });
        order.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
            else if (getContent() instanceof Refreshable) {
                ((Refreshable) getContent()).orderChanged(getOrder());
            }
        });
        classChoices.valueProperty().addListener((observable, old, current) -> {
            if (getContent() instanceof Refreshable) {
                ((Refreshable) getContent()).classChanged(classes.get(current));
            }
        });
        classesOnly.bind(classChoices.valueProperty().isNull());
        classesOnly.addListener((observableValue, old, filter) -> {
            if (filter) {
                navigationList.getSelectionModel().select(0);
            }
            navigationList.refresh();
        });

        datePicker.valueProperty().addListener((observable, old, date) -> {
            if (old != null) {
                save("configuration.json", old.format(formatter));
            }
            if (date != null) {
                load("configuration.json", date.format(formatter));
                refreshContent();
            }
        });
        App.STAGE_STACK.peek().setOnCloseRequest(windowEvent -> {
            if (downloading.get()) {
                final String title = "Close";
                final String header = "Download in progress...";
                final String content = "Would you like to stop downloading the latest version of VirtualTools and update later?";
                if (Utilities.showAlert(title, header, content, "Confirm").get() == ButtonType.CANCEL) {
                    windowEvent.consume();
                    return;
                }
            }
            save(CONFIG_FILE, datePicker.getValue().format(formatter));
        });
        datePicker.setValue(LocalDate.now());
        if (classes.isEmpty()) {
            loadLastRoster(CONFIG_FILE);
        }
        navigationList.getSelectionModel().select(0);
        splitPane.widthProperty().addListener((observableValue, old, current) -> {
            final double ratio = old.doubleValue() / current.doubleValue();
            final double divider = splitPane.getDividerPositions()[0];
            splitPane.setDividerPosition(0, divider * ratio);
        });
    }

    private void pollUpdates() {
        final long delay = 16;

        updater.scheduleAtFixedRate(() -> {
            final long HOUR_MILLIS = 1000 * 60 * 60;
            if (!Thread.interrupted()) {
                if (downloading.get()) {
                    Platform.runLater(() -> {
                        updateProgress.setVisible(true);
                    });
                    final String output = "latest.zip";
                    if (Updater.download(updateProgress, output)) {
                        Platform.runLater(() -> {
                            updateButton.setText("INSTALL");
                        });
                    }
                    else {
                        System.err.println("ERROR: Couldn't download the latest version.");
                    }
                    downloading.set(false);
                } else {
                    final long currentTime = System.currentTimeMillis();
                    if (currentTime - start > HOUR_MILLIS) {
                        final boolean available = Updater.updateAvailable();
                        Platform.runLater(() -> {
                            updateButton.setDisable(!available);
                        });
                        start = currentTime;
                    }
                }
            }
        }, 0, delay, TimeUnit.MILLISECONDS);
    }

    @FXML
    public void displayHelp(@SuppressWarnings("unnused") ActionEvent actionEvent) {
        final HelpWindow help = new HelpWindow();
        help.display(App.STAGE_STACK.peek());
    }

    public Set<String> getClassSet() {
        return classes.keySet();
    }

    public LocalDate getActiveDate() {
        return datePicker.getValue();
    }

    public Classroom addClass(String name, Classroom classroom) {
        classChoices.getItems().add(name);

        final var added = classes.put(name, classroom);
        if (classChoices.getSelectionModel().isEmpty()) {
            classChoices.getSelectionModel().select(0);
        }
        return added;
    }

    public void loadPane(String pane) {
        setContent(PANES.get(pane.toUpperCase()));
        refreshContent();
    }

    public Classroom addClass(String name) {
        return addClass(name, new Classroom(name));
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
                for (var classroom : loadClassrooms(object.getJSONObject(date), false)) {
                    addClass(classroom.getName(), classroom);
                }
            }
        }
        catch (JSONException | IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public ArrayList<Classroom> getClassroomsInRange(LocalDate begin, LocalDate end) {
        final String fileName = CONFIG_FILE;
        final ArrayList<Classroom> classrooms = new ArrayList<>();
        final Date active = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        final Date dateBegin = Date.from(begin.atStartOfDay(ZoneId.systemDefault()).toInstant());
        final Date dateEnd = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (dateBegin.compareTo(active) >= 0 && dateEnd.compareTo(active) <= 0) {
            for (var entry : classes.entrySet()) {
                classrooms.add(entry.getValue());
            }
        }
        try {
            if (new File(fileName).exists()) {
                final String contents = Files.readString(Paths.get(fileName));
                final JSONObject object = new JSONObject(contents);
                Iterator<String> dates = object.keys();
                final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                while (dates.hasNext()) {
                    final String key = dates.next();
                    final Date date = formatter.parse(key);
                    if (date.compareTo(active) != 0 && date.compareTo(dateBegin) >= 0 && date.compareTo(dateEnd) <= 0) {
                        classrooms.addAll(loadClassrooms(object.getJSONObject(key), false));
                    }
                }
            }
        }
        catch (IOException | ParseException ioe) {
            ioe.printStackTrace();
        }
        return classrooms;
    }

    public void shutdown() {
        updater.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!updater.awaitTermination(1, TimeUnit.SECONDS)) {
                updater.shutdownNow();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
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
        catch (JSONException | IOException ioe) {
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

    private void refreshContent() {
        if (getContent() instanceof Refreshable) {
            ((Refreshable) getContent()).refresh(this);
        }
    }

    private void setContent(Node node) {
        content.getChildren().clear();
        content.getChildren().add(node);
    }

    private Node getContent() {
        return content.getChildren().isEmpty() ? null : content.getChildren().get(0);
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
                for (var classroom : loadClassrooms(object.getJSONObject(access), true)) {
                    addClass(classroom.getName(), classroom);
                }
            }
        }
        catch (JSONException | IOException | ParseException ioe) {
            ioe.printStackTrace();
        }
    }

    private static JSONObject fetchRoot(String fileName) {
        try {
            return new JSONObject(Files.readString(Paths.get(fileName)));
        }
        catch (IOException | JSONException ignored) {
            return new JSONObject();
        }
    }

    private static ArrayList<Classroom> loadClassrooms(JSONObject selected, boolean rosterOnly) throws JSONException {
        final ArrayList<Classroom> classrooms = new ArrayList<>();
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
            classrooms.add(classroom);
        }
        return classrooms;
    }

    @FXML
    private void update() {
        if (updateButton.getText().equalsIgnoreCase("UPDATE")) {
            downloading.set(true);
        }
        else {
            final String title = "Install";
            final String header = "Install New Version";
            final String content = "The program must be closed in order to continue installing. Would you like to save your data and temporarily close the program to install?";
            if (Utilities.showAlert(title, header, content, "Confirm").get() != ButtonType.CANCEL) {
                save(CONFIG_FILE, datePicker.getValue().format(formatter));
                App.STAGE_STACK.peek().setOnCloseRequest(null);
                try {
                    Runtime.getRuntime().exec("cmd /c start VirtualToolsUpdater.exe latest.zip unpacked jlink_vtools_jre");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                App.STAGE_STACK.peek().close();
            }
        }
    }
}
