package gdd.sprite;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Shot extends Sprite {

    // ===== Bullet spawn position relative to the player =====
    // Adjust these two values if the bullet is not exactly at the nose.
    private static final int H_SPACE = 60;
    private static final int V_SPACE = 13;

    // ===== Bullet sprite coordinates on spites.png =====
    // Change these if you want another bullet sprite.
    private static final int BULLET_X = 248;
    private static final int BULLET_Y = 10;
    private static final int BULLET_WIDTH = 8;
    private static final int BULLET_HEIGHT = 10;

    private static final int SCALE = 3;

    public Shot() {
    }

    // Single shot
    public Shot(int x, int y) {
        initShot(x, y, 0);
    }

    // Multi-shot (offset is now vertical)
    public Shot(int x, int y, int offsetY) {
        initShot(x, y, offsetY);
    }

    private void initShot(int x, int y, int offsetY) {

        try {

            BufferedImage sheet = ImageIO.read(
                    new File("gdd-space-invaders-project/src/images/spites.png"));

            BufferedImage bullet = sheet.getSubimage(
                    BULLET_X,
                    BULLET_Y,
                    BULLET_WIDTH,
                    BULLET_HEIGHT);

            Image img = bullet.getScaledInstance(
                    BULLET_WIDTH * SCALE,
                    BULLET_HEIGHT * SCALE,
                    Image.SCALE_FAST);

            setImage(img);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Spawn from the plane's nose.
        setX(x + H_SPACE);

        // Vertical offset for multi-shot.
        setY(y + V_SPACE + offsetY);
    }
}