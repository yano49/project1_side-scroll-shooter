package gdd.sprite;

import static gdd.Global.ALIEN_HEIGHT;
import static gdd.Global.ALIEN_WIDTH;
import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.IMG_MINIBOSS;
import static gdd.Global.MINIBOSS_FLASH_DURATION;
import static gdd.Global.MINIBOSS_HEALTH;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 * Scene 1 miniboss AI owned by Member 2.
 *
 * Behavior:
 * - Enters from beyond the right edge.
 * - Stays inside the board after entering.
 * - Circles near the player and periodically charges at the player.
 * - Never disappears because it reached a screen edge.
 * - Remains active until its health reaches zero.
 */
public class MiniBoss extends Enemy {

    private enum AiState {
        ENTERING,
        ORBITING,
        CHARGING,
        RECOVERING
    }

    private static final int SPRITE_WIDTH = ALIEN_WIDTH * 3;
    private static final int SPRITE_HEIGHT = ALIEN_HEIGHT * 3;

    private static final int TOP_MARGIN = 60;
    private static final int BOTTOM_MARGIN = 20;
    private static final int RIGHT_MARGIN = 18;

    private static final int ENTRY_SPEED = 1;
    private static final int NORMAL_SPEED = 1;
    private static final int FAST_SPEED = 2;
    private static final int CHARGE_SPEED_BONUS = 1;

    private static final int ORBIT_DISTANCE_X = 170;
    private static final int ORBIT_DISTANCE_Y = 105;
    private static final int ORBIT_FRAMES_BEFORE_CHARGE = 150;
    private static final int CHARGE_FRAMES = 75;
    private static final int RECOVERY_FRAMES = 55;

    private int health = MINIBOSS_HEALTH;
    private int flashTimer;
    private int stateTimer;
    private double orbitAngle;

    private AiState aiState = AiState.ENTERING;
    private Image normalImage;
    private Image flashImage;

    public MiniBoss(int x, int y) {
        super(x, y);
        initMiniBoss(x, y);
    }

    private void initMiniBoss(int requestedX, int requestedY) {
        /* Always begin outside the right edge for side-scroll gameplay. */
        x = Math.max(BOARD_WIDTH + 20, requestedX);
        y = clampY(requestedY);

        ImageIcon icon = new ImageIcon(IMG_MINIBOSS);

        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
            normalImage = icon.getImage().getScaledInstance(
                    SPRITE_WIDTH,
                    SPRITE_HEIGHT,
                    Image.SCALE_SMOOTH
            );
        } else {
            System.err.println(
                    "MiniBoss image could not be loaded: " + IMG_MINIBOSS
            );
            normalImage = image;
        }

        flashImage = createFlashImage(normalImage);
        setImage(normalImage);
        setVisible(true);
    }

    /**
     * Main AI method Scene1 should call each update.
     */
    public void act(Player player, boolean fastMode) {
        if (!isVisible() || isDying() || player == null) {
            return;
        }

        int speed = fastMode ? FAST_SPEED : NORMAL_SPEED;

        switch (aiState) {
            case ENTERING:
                updateEntering(speed);
                break;

            case ORBITING:
                updateOrbiting(player, speed);
                break;

            case CHARGING:
                updateCharging(player, speed + CHARGE_SPEED_BONUS);
                break;

            case RECOVERING:
                updateRecovering(player, speed);
                break;

            default:
                break;
        }

        keepInsideBoard();
        tickFlash();
    }

    private void updateEntering(int speed) {
        int targetX = BOARD_WIDTH - SPRITE_WIDTH - 60;
        x -= Math.max(ENTRY_SPEED, speed);

        if (x <= targetX) {
            x = targetX;
            aiState = AiState.ORBITING;
            stateTimer = 0;
        }
    }

    private void updateOrbiting(Player player, int speed) {
        stateTimer++;
        orbitAngle += 0.020 * speed;

        int playerCenterX = player.getX() + player.getSpriteWidth() / 2;
        int playerCenterY = player.getY() + player.getSpriteHeight() / 2;

        int targetX = playerCenterX
                + ORBIT_DISTANCE_X
                + (int) (Math.cos(orbitAngle) * 70);

        int targetY = playerCenterY
                + (int) (Math.sin(orbitAngle) * ORBIT_DISTANCE_Y);

        moveToward(targetX, targetY, speed);

        if (stateTimer >= ORBIT_FRAMES_BEFORE_CHARGE) {
            aiState = AiState.CHARGING;
            stateTimer = 0;
        }
    }

    private void updateCharging(Player player, int speed) {
        stateTimer++;

        int targetX = player.getX();
        int targetY = player.getY();
        moveToward(targetX, targetY, speed);

        if (stateTimer >= CHARGE_FRAMES) {
            aiState = AiState.RECOVERING;
            stateTimer = 0;
        }
    }

    private void updateRecovering(Player player, int speed) {
        stateTimer++;

        int safeX = BOARD_WIDTH - SPRITE_WIDTH - 70;
        int safeY = player.getY() - SPRITE_HEIGHT;
        moveToward(safeX, safeY, speed);

        if (stateTimer >= RECOVERY_FRAMES) {
            aiState = AiState.ORBITING;
            stateTimer = 0;
        }
    }

    private void moveToward(int targetX, int targetY, int speed) {
        int differenceX = targetX - x;
        int differenceY = targetY - y;
        double distance = Math.hypot(differenceX, differenceY);

        if (distance < 1.0) {
            return;
        }

        x += (int) Math.round(speed * differenceX / distance);
        y += (int) Math.round(speed * differenceY / distance);
    }

    private void keepInsideBoard() {
        int maximumX = BOARD_WIDTH - SPRITE_WIDTH - RIGHT_MARGIN;
        int maximumY = BOARD_HEIGHT - SPRITE_HEIGHT - BOTTOM_MARGIN;

        x = Math.max(0, Math.min(x, maximumX));
        y = Math.max(TOP_MARGIN, Math.min(y, maximumY));
    }

    private int clampY(int requestedY) {
        int maximumY = BOARD_HEIGHT - SPRITE_HEIGHT - BOTTOM_MARGIN;
        return Math.max(TOP_MARGIN, Math.min(requestedY, maximumY));
    }

    private Image createFlashImage(Image source) {
        if (source == null) {
            return null;
        }

        int width = source.getWidth(null);
        int height = source.getHeight(null);

        if (width <= 0 || height <= 0) {
            return source;
        }

        BufferedImage bufferedImage = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.9f)
        );
        graphics.setColor(Color.YELLOW);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        return bufferedImage;
    }

    private void tickFlash() {
        if (flashTimer <= 0) {
            return;
        }

        flashTimer--;
        setImage(flashTimer % 4 < 2 ? flashImage : normalImage);

        if (flashTimer == 0) {
            setImage(normalImage);
        }
    }

    /**
     * Backward-compatible fallback. Scene1 should normally call
     * act(player, fastMode), not this method.
     */
    @Override
    public void act(int direction) {
        tickFlash();
    }

    public boolean hit() {
        if (health <= 0) {
            return true;
        }

        health--;

        if (health <= 0) {
            health = 0;
            setDying(true);
            return true;
        }

        flashTimer = MINIBOSS_FLASH_DURATION;
        return false;
    }

    public int getHealth() {
        return health;
    }

    public int getSpriteWidth() {
        return SPRITE_WIDTH;
    }

    public int getSpriteHeight() {
        return SPRITE_HEIGHT;
    }
}