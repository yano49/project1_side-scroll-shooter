package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.IMG_BOSS_BULLET;
import java.awt.Image;
import javax.swing.ImageIcon;

public class BossBullet extends Sprite {

    private static final int WIDTH = 18;
    private static final int HEIGHT = 28;
    private final double velocityX;
    private final double velocityY;
    private double preciseX;
    private double preciseY;

    public BossBullet(int x, int y, double velocityX, double velocityY) {
        this.x = x;
        this.y = y;
        preciseX = x;
        preciseY = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;

        Image image = new ImageIcon(IMG_BOSS_BULLET).getImage();
        setImage(image.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH));
    }

    @Override
    public void act() {
        preciseX += velocityX;
        preciseY += velocityY;
        x = (int) preciseX;
        y = (int) preciseY;

        if (y > BOARD_HEIGHT || x < -WIDTH || x > BOARD_WIDTH) {
            die();
        }
    }
}
