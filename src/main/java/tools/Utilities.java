package tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
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
}
