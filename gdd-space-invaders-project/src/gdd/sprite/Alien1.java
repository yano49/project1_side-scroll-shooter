package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

/**
 * Normal alien with straight, zigzag, or slow tracking movement.
 */
public class Alien1 extends Enemy {

    public enum MovementType {
        STRAIGHT,
        ZIGZAG,
        SLOW_TRACKING
    }

    private static final int TOP_MARGIN = 70;
    private static final int BOTTOM_MARGIN = 30;

    private Bomb bomb;
    private final MovementType movementType;
    private final int movementSpeed;
    private final int startingY;
    private double zigzagAngle;

    public Alien1(int x, int y) {
        this(x, y, MovementType.STRAIGHT, 2);
    }

    public Alien1(
            int x,
            int y,
            MovementType movementType,
            int movementSpeed
    ) {
        super(x, y);

        this.movementType =
                movementType == null
                        ? MovementType.STRAIGHT
                        : movementType;

        this.movementSpeed = Math.max(1, movementSpeed);
        this.startingY = y;

        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {
        this.x = x;
        this.y = y;

        bomb = new Bomb(x, y);

        ImageIcon icon = new ImageIcon(IMG_ENEMY);

        var scaledImage = icon.getImage().getScaledInstance(
                icon.getIconWidth() * SCALE_FACTOR,
                icon.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH
        );

        setImage(scaledImage);
    }

    @Override
    public void act(int direction) {
        x -= movementSpeed;
    }

    public void act(Player player) {
        switch (movementType) {
            case ZIGZAG:
                updateZigzag();
                break;

            case SLOW_TRACKING:
                updateSlowTracking(player);
                break;

            case STRAIGHT:
            default:
                x -= movementSpeed;
                break;
        }

        clampVerticalPosition();
    }

    private void updateZigzag() {
        x -= movementSpeed;

        zigzagAngle += 0.09;
        y = startingY + (int) Math.round(
                Math.sin(zigzagAngle) * 55
        );
    }

    private void updateSlowTracking(Player player) {
        /*
         * Fixed speed 1 so this alien is not faster than the miniboss.
         */
        x -= 1;

        if (player == null) {
            return;
        }

        int alienCenterY = y + ALIEN_HEIGHT / 2;
        int playerCenterY =
                player.getY() + player.getSpriteHeight() / 2;

        if (alienCenterY < playerCenterY - 5) {
            y += 1;
        } else if (alienCenterY > playerCenterY + 5) {
            y -= 1;
        }
    }

    private void clampVerticalPosition() {
        int maximumY =
                BOARD_HEIGHT - ALIEN_HEIGHT - BOTTOM_MARGIN;

        y = Math.max(TOP_MARGIN, Math.min(y, maximumY));
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public class Bomb extends Sprite {

        private boolean destroyed;

        public Bomb(int x, int y) {
            initBomb(x, y);
        }

        private void initBomb(int x, int y) {
            setDestroyed(true);

            this.x = x;
            this.y = y;

            var bombImg = "src/images/bomb.png";
            var icon = new ImageIcon(bombImg);
            setImage(icon.getImage());
        }

        public void setDestroyed(boolean destroyed) {
            this.destroyed = destroyed;
        }

        public boolean isDestroyed() {
            return destroyed;
        }
    }
}