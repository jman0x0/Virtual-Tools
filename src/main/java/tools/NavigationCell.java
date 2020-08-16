package tools;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;


class NavigationCell extends ListCell<NavigationData> {
    private final ImageView imageView = new ImageView();

    @Override
    protected void updateItem(NavigationData data, boolean empty) {
        super.updateItem(data, empty);
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
