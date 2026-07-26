package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.IMG_BOSS_BULLET;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class BossBullet extends Sprite {

    /*
     * The original boss-bullet image points downward.
     * A square canvas gives enough room to rotate it toward any direction.
     */
    private static final int BULLET_SIZE = 30;
    private static final int SOURCE_WIDTH = 18;
    private static final int SOURCE_HEIGHT = 28;

    private final double velocityX;
    private final double velocityY;

    private double preciseX;
    private double preciseY;

    public BossBullet(
            int x,
            int y,
            double velocityX,
            double velocityY
    ) {
        this.x = x;
        this.y = y;
        preciseX = x;
        preciseY = y;

        this.velocityX = velocityX;
        this.velocityY = velocityY;

        setImage(createDirectionalImage(velocityX, velocityY));
    }

    private Image createDirectionalImage(
            double velocityX,
            double velocityY
    ) {
        Image originalImage =
                new ImageIcon(IMG_BOSS_BULLET).getImage();

        BufferedImage canvas = new BufferedImage(
                BULLET_SIZE,
                BULLET_SIZE,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = canvas.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        /*
         * The source sprite points down.
         * atan2 gives the bullet's actual movement angle.
         * Subtracting PI / 2 changes the down-facing sprite into that angle.
         */
        double movementAngle = Math.atan2(velocityY, velocityX);
        double rotationAngle = movementAngle - Math.PI / 2.0;

        AffineTransform transform = new AffineTransform();
        transform.translate(BULLET_SIZE / 2.0, BULLET_SIZE / 2.0);
        transform.rotate(rotationAngle);
        transform.translate(
                -SOURCE_WIDTH / 2.0,
                -SOURCE_HEIGHT / 2.0
        );
        transform.scale(
                SOURCE_WIDTH / (double) originalImage.getWidth(null),
                SOURCE_HEIGHT / (double) originalImage.getHeight(null)
        );

        graphics.drawImage(originalImage, transform, null);
        graphics.dispose();

        return canvas;
    }

    @Override
    public void act() {
        preciseX += velocityX;
        preciseY += velocityY;

        x = (int) preciseX;
        y = (int) preciseY;

        if (x < -BULLET_SIZE
                || x > BOARD_WIDTH + BULLET_SIZE
                || y < -BULLET_SIZE
                || y > BOARD_HEIGHT + BULLET_SIZE) {
            die();
        }
    }
}