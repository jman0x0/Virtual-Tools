package tools;

import javafx.beans.NamedArg;
import javafx.scene.image.Image;

public class NavigationData {
    private final String name;
    private final Image image;

    public NavigationData(@NamedArg("value") String value) {
        final String[] fields = value.split(",");
        this.name  = fields[0].trim();
        this.image = new Image(getClass().getResourceAsStream(fields[1].trim()));
    }

    public String getName() {
        return name;
    }

    public Image getImage() {
        return image;
    }
}
