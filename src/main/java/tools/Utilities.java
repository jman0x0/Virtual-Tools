package tools;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {
    public static void loadController(Node node, String pathway) {
        final FXMLLoader fxmlLoader = new FXMLLoader(Utilities.class.getResource(pathway));
        fxmlLoader.setRoot(node);
        fxmlLoader.setController(node);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static String reverseName(String name) {
        final var lastFirst = name.split(",");
        if (lastFirst.length != 2) {
            return name;
        }
        final var first = Arrays.asList(lastFirst[1].split("\\W"));
        final var last = Arrays.asList(lastFirst[0].split("\\W"));
        Collections.reverse(first);
        Collections.reverse(last);
        return String.join(" ", first).stripTrailing() + ", " + String.join(" ", last);
    }

    public static <E> void reverseArray(E[] array) {
        for(int i = 0; i < array.length / 2; i++) {
            var temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    public static String[] extractWords(String string) {
        final Pattern pattern = Pattern.compile("(\\w+)");
        final Matcher matcher = pattern.matcher(string);
        final ArrayList<String> words = new ArrayList<>();

        while (matcher.find()) {
            words.add(matcher.group(0));
        }
        return words.toArray(String[]::new);
    }

    public static void sortStudents(List<Student> students, PrimaryController.Order order) {
        final HashMap<Student, String[]> names = new HashMap<>();
        for (var student : students) {
            if (order == PrimaryController.Order.FIRST_LAST) {
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
    }

    public static <NodeType, Controller> javafx.util.Pair<NodeType, Controller> loadFXML(String pathway) {
        final FXMLLoader loader = new FXMLLoader(Utilities.class.getResource(pathway));
        NodeType model = null;
        Controller controller = null;

        try {
            model = loader.load();
            controller = loader.getController();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return new javafx.util.Pair<>(model, controller);
    }

    public static Optional<ButtonType> showAlert(String title, String header, String content, String actionName) {
        final ButtonType action = new ButtonType(actionName, ButtonBar.ButtonData.OK_DONE);
        final Alert alert = new Alert(Alert.AlertType.WARNING, content, action, ButtonType.CANCEL);
        final Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
        stage.getIcons().addAll(App.STAGE_STACK.peek().getIcons());
        alert.setTitle("Virtual Tools - " + title);
        alert.setHeaderText(header);
        alert.setResizable(false);
        alert.setWidth(390);
        alert.setHeight(260);
        Platform.runLater(() -> alert.getDialogPane().lookupButton(ButtonType.CANCEL).requestFocus());
        return alert.showAndWait();
    }

    public static String getTimeStamp() {
        final DateFormat date = new SimpleDateFormat("MM/dd/yyyy@hh:mm a");
        return date.format(Calendar.getInstance().getTime());
    }

    public static String getCreditStamp(int credit, boolean newline) {
        final String timeStamp = Utilities.getTimeStamp();
        final String separator = newline ? "" : "\n";
        final String sign = credit >= 0 ? "+" : "";
        return separator + sign + credit + " Credit " + timeStamp;
    }
}
