package tools;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class AnimatedImageView extends ImageView {
    private String name;

    private int count;

    public void setPathway(String name) {
        this.name = name;
    }

    public void play() {
        final FadeTransition frameAnimator = new FadeTransition();
        frameAnimator.setDuration(Duration.seconds(1.8));
        frameAnimator.setFromValue(1.0);
        frameAnimator.setToValue(74.0);
        frameAnimator.currentTimeProperty().addListener((observable, old, current) -> {
            final int start = 1;
            final var end = 74;
            final var duration = frameAnimator.getDuration();
            final double v = current.toSeconds() / duration.toSeconds();
            final long frame = (long)Math.floor(start + v * (end - start));
            final Image image = new Image(getClass().getResourceAsStream(name + frame + ".png"));
            this.setImage(image);
        });
        frameAnimator.setNode(this);
        frameAnimator.play();
    }
}
