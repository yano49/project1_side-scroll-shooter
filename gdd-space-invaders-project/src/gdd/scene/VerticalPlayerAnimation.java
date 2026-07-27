package gdd.scene;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Scene 2 view of the player, loaded from the upward-facing blue ship frames.
 * This keeps the side-scrolling animation in Player.java unchanged.
 */
final class VerticalPlayerAnimation {

    private static final String SPRITE_SHEET =
            "gdd-space-invaders-project/src/images/spites.png";
    private static final int START_X = 152;
    private static final int START_Y = 0;
    private static final int FRAME_WIDTH = 16;
    private static final int FRAME_HEIGHT = 24;
    private static final int FRAME_GAP = 24;
    private static final int SCALE = 3;
    private static final int FRAME_DELAY = 8;

    private final Image[] frames = new Image[4];
    private int frame;
    private int tick;

    VerticalPlayerAnimation() {
        try {
            BufferedImage sheet = ImageIO.read(new File(SPRITE_SHEET));
            for (int i = 0; i < frames.length; i++) {
                BufferedImage source = sheet.getSubimage(
                        START_X + i * FRAME_GAP,
                        START_Y,
                        FRAME_WIDTH,
                        FRAME_HEIGHT
                );
                frames[i] = source.getScaledInstance(
                        FRAME_WIDTH * SCALE,
                        FRAME_HEIGHT * SCALE,
                        Image.SCALE_FAST
                );
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "Could not load vertical player frames from " + SPRITE_SHEET,
                    exception
            );
        }
    }

    void update() {
        tick++;
        if (tick >= FRAME_DELAY) {
            tick = 0;
            frame = (frame + 1) % frames.length;
        }
    }

    Image getImage() {
        return frames[frame];
    }
}
