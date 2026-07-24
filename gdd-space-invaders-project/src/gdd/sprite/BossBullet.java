package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class BossBullet extends Sprite {

    private static final String IMAGE_PATH =
            "gdd-space-invaders-project/src/images/boss-bullet.png";

    private final int dy;

    public BossBullet(int x, int y, int dx, int dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;

        setImage(loadBulletImage());
    }

    private Image loadBulletImage() {

        ImageIcon icon = new ImageIcon(IMAGE_PATH);

        if (icon.getIconWidth() > 0) {
            return icon.getImage().getScaledInstance(
                    14,
                    28,
                    Image.SCALE_SMOOTH
            );
        }

        // Temporary image if boss-bullet.png cannot be found
        BufferedImage fallback =
                new BufferedImage(14, 28, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = fallback.createGraphics();

        graphics.setColor(Color.RED);
        graphics.fillOval(0, 0, 14, 28);

        graphics.dispose();

        return fallback;
    }

    @Override
    public void act() {

        x += dx;
        y += dy;

        int bulletWidth = getImage().getWidth(null);

        boolean outsideBottom = y > BOARD_HEIGHT;
        boolean outsideLeft = x < -bulletWidth;
        boolean outsideRight = x > BOARD_WIDTH;

        if (outsideBottom || outsideLeft || outsideRight) {
            die();
        }
    }
}