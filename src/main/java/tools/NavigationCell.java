package tools;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;


class NavigationCell extends ListCell<NavigationData> {
    private final ImageView imageView = new ImageView();

    private final ObservableValue<Boolean> classesOnly;

    public NavigationCell(ObservableValue<Boolean> classesOnly) {
        this.classesOnly = classesOnly;
    }

    @Override
    protected void updateItem(NavigationData data, boolean empty) {
        super.updateItem(data, empty);
        final boolean filter = classesOnly.getValue() && !empty;
        setDisable(filter && !data.getName().equalsIgnoreCase("CLASSES"));
        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else {
            imageView.setImage(data.getImage());
            setGraphic(imageView);
            setText(data.getName());
        }
    }
}
