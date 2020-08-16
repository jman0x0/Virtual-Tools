package tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

public class Configuration {

    public static void load(String fileName) {
        try {
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
                Classes.CLASS_INFO.put(key, cached);
            }
        }
        catch (IOException ioe) {
            //ioe.printStackTrace();
        }
    }

    public static void save(String fileName) {
        FileWriter writer = null;
        try {
            final JSONObject object = new JSONObject();
            final JSONObject classes = new JSONObject();
            for (var entry : Classes.CLASS_INFO.entrySet()) {
                final JSONArray roster = new JSONArray();
                for (var student : entry.getValue()) {
                    roster.put(student.toString());
                }
                classes.put(entry.getKey(), roster);
            }
            object.put("classes", classes);
            writer = new FileWriter(new File(Paths.get(fileName).toUri()));
            writer.write(object.toString());
        }
        catch (IOException ioe) {
            //ioe.printStackTrace();
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
