package tools;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HelpWindow extends VBox implements SubWindow {
    @FXML
    private TabPane tabPane;

    public HelpWindow() {
        Utilities.loadController(this, "help.fxml");
    }

    @Override
    public Scene buildScene() {
        return new Scene(this);
    }

    @Override
    public String getTitle() {
        return "Virtual Tools - " + "Help";
    }

    @Override
    public double getWindowWidth() {
        return 560;
    }

    @Override
    public double getWindowHeight() {
        return 315 ;
    }

    @FXML
    private void initialize() {
        final var tabs = tabPane.getTabs();

        for (Tab tab : tabs) {
            final Node node = tab.contentProperty().get();
            if (node instanceof Parent) {
                final Parent parent = (Parent)node;

                for (var child : parent.getChildrenUnmodifiable()) {
                    if (child instanceof Region) {
                        ((Region)child).maxWidthProperty().bind(tabPane.widthProperty());
                    }
                }
            }

        }
    }

    @FXML
    private void openEmail() {
        Desktop desktop;
        try {
            if (Desktop.isDesktopSupported()
                    && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
                URI mailto = new URI("mailto:jman0x0@gmail.com?subject=Hello%20World");
                desktop.mail(mailto);
            }
        } catch (IOException | URISyntaxException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    private void openSourcePage() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/jman0x0/Virtual-Tools/tree/master"));
        } catch (IOException | URISyntaxException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    private void openDonationPage() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=4KY8JM4V6TLTE&currency_code=USD&source=url"));
        } catch (IOException | URISyntaxException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    private void close() {
        App.STAGE_STACK.peek().close();
    }
}
