package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Shot extends Sprite {

    private static final int H_SPACE = 68;
    private static final int V_SPACE = 9;

    private static final int BULLET_X = 248;
    private static final int BULLET_Y = 10;
    private static final int BULLET_WIDTH = 8;
    private static final int BULLET_HEIGHT = 10;
    private static final int SCALE = 3;

    private int velocityX;
    private int velocityY;
    private int hitWidth;
    private int hitHeight;
    private final boolean laser;

    public Shot() {
        this.laser = false;
    }

    public Shot(int x, int y) {
        this(x, y, 0, 20, 0, false);
    }

    public Shot(int x, int y, int offsetY) {
        this(x, y, offsetY, 20, 0, false);
    }

    public Shot(
            int x,
            int y,
            int offsetY,
            int velocityX,
            int velocityY
    ) {
        this(x, y, offsetY, velocityX, velocityY, false);
    }

    public static Shot createLaser(
            int x,
            int y,
            int offsetY,
            int velocityY
    ) {
        return new Shot(x, y, offsetY, 28, velocityY, true);
    }

    private Shot(
            int x,
            int y,
            int offsetY,
            int velocityX,
            int velocityY,
            boolean laser
    ) {
        this.laser = laser;
        this.velocityX = velocityX;
        this.velocityY = velocityY;

        if (laser) {
            loadLaserImage();
        } else {
            loadBulletImage();
        }

        setX(x + H_SPACE);
        setY(y + V_SPACE + offsetY);
    }

    private void loadBulletImage() {
        try {
            BufferedImage sheet = ImageIO.read(
                    new File(
                            "gdd-space-invaders-project/src/images/spites.png"
                    )
            );

            BufferedImage bullet = sheet.getSubimage(
                    BULLET_X,
                    BULLET_Y,
                    BULLET_WIDTH,
                    BULLET_HEIGHT
            );

            Image image = bullet.getScaledInstance(
                    BULLET_WIDTH * SCALE,
                    BULLET_HEIGHT * SCALE,
                    Image.SCALE_FAST
            );

            setImage(image);
            hitWidth = BULLET_WIDTH * SCALE;
            hitHeight = BULLET_HEIGHT * SCALE;

        } catch (IOException exception) {
            System.err.println(
                    "Could not load bullet sprite: "
                            + exception.getMessage()
            );
            loadFallbackBullet();
        }
    }

    private void loadFallbackBullet() {
        BufferedImage fallback =
                new BufferedImage(18, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = fallback.createGraphics();
        graphics.setColor(Color.YELLOW);
        graphics.fillRect(0, 0, 18, 8);
        graphics.dispose();

        setImage(fallback);
        hitWidth = fallback.getWidth();
        hitHeight = fallback.getHeight();
    }

    private void loadLaserImage() {
        BufferedImage beam =
                new BufferedImage(54, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = beam.createGraphics();

        graphics.setColor(new Color(180, 255, 255, 150));
        graphics.fillRect(0, 0, beam.getWidth(), beam.getHeight());

        graphics.setColor(Color.CYAN);
        graphics.fillRect(0, 2, beam.getWidth(), 4);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 3, beam.getWidth(), 2);

        graphics.dispose();

        setImage(beam);
        hitWidth = beam.getWidth();
        hitHeight = beam.getHeight();
    }

    public void act() {
        x += velocityX;
        y += velocityY;

        if (x > BOARD_WIDTH
                || x + hitWidth < 0
                || y > BOARD_HEIGHT
                || y + hitHeight < 0) {
            die();
        }
    }

    public int getHitWidth() {
        return hitWidth;
    }

    public int getHitHeight() {
        return hitHeight;
    }

    public boolean isLaser() {
        return laser;
    }
}