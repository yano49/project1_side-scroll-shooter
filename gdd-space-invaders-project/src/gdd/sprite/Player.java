package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player extends Sprite {

    private static final int START_X = 135;
    private static final int START_Y = 540;

    private static final int START_X_FRAME = 24;
    private static final int START_Y_FRAME = 8;
    private static final int FRAME_WIDTH = 24;
    private static final int FRAME_HEIGHT = 16;
    private static final int GAP = 32;
    private static final int SCALE = 3;

    private static final int BASE_SPEED = 2;
    private static final int MAX_WEAPON_LEVEL = 4;
    private static final int MAX_SPEED_LEVEL = 2;
    private static final int SUPER_SPEED = 6;
    private static final long DEFAULT_SUPER_POWER_DURATION_MS = 10_000L;

    private BufferedImage spriteSheet;
    private final BufferedImage[] frames = new BufferedImage[4];

    private int currentFrame;
    private int animationTick;

    private int dx;
    private int dy;

    private boolean movingUp;
    private boolean movingDown;
    private boolean movingLeft;
    private boolean movingRight;

    private int currentSpeed = BASE_SPEED;
    private int weaponLevel = 1;
    private int speedLevel = 1;

    private boolean superPowered;
    private boolean invincible;
    private boolean crashAttackEnabled;
    private long superPowerEndTime;

    private int savedSpeed;
    private int savedWeaponLevel;
    private int savedSpeedLevel;

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        try {
            spriteSheet = ImageIO.read(
                    new File("gdd-space-invaders-project/src/images/spites.png")
            );

            loadBluePlane();
            updateDisplayedFrame();
        } catch (IOException exception) {
            System.err.println(
                    "Could not load player sprite sheet: "
                            + exception.getMessage()
            );
        }

        setX(START_X);
        setY(START_Y);
    }

    private void loadBluePlane() {
        loadPlaneFrames(START_Y_FRAME);
    }

    private void loadOrangePlane() {
        /*
         * Correct orange 1P side-facing row.
         */
        loadPlaneFrames(32);
    }

    private void loadPlaneFrames(int rowY) {
        if (spriteSheet == null) {
            return;
        }

        frames[0] = spriteSheet.getSubimage(
                START_X_FRAME,
                rowY,
                FRAME_WIDTH,
                FRAME_HEIGHT
        );

        frames[1] = spriteSheet.getSubimage(
                START_X_FRAME + GAP,
                rowY,
                FRAME_WIDTH,
                FRAME_HEIGHT
        );

        frames[2] = frames[0];
        frames[3] = frames[1];
    }

    @Override
    public void act() {
        updateMovementDirection();

        x += dx;
        y += dy;

        int maximumX = BOARD_WIDTH - getSpriteWidth();
        int maximumY = BOARD_HEIGHT - getSpriteHeight();

        x = Math.max(0, Math.min(x, maximumX));
        y = Math.max(0, Math.min(y, maximumY));

        updateAnimation();
        updateSuperPowerStatus();
    }

    private void updateMovementDirection() {
        dx = 0;
        dy = 0;

        if (movingLeft && !movingRight) {
            dx = -currentSpeed;
        } else if (movingRight && !movingLeft) {
            dx = currentSpeed;
        }

        if (movingUp && !movingDown) {
            dy = -currentSpeed;
        } else if (movingDown && !movingUp) {
            dy = currentSpeed;
        }
    }

    private void updateAnimation() {
        animationTick++;

        if (animationTick < 8) {
            return;
        }

        animationTick = 0;
        currentFrame = (currentFrame + 1) % frames.length;
        updateDisplayedFrame();
    }

    private void updateDisplayedFrame() {
        if (frames[currentFrame] == null) {
            return;
        }

        setImage(
                frames[currentFrame].getScaledInstance(
                        FRAME_WIDTH * SCALE,
                        FRAME_HEIGHT * SCALE,
                        Image.SCALE_FAST
                )
        );
    }

    public void activateSuperPowerUp() {
        activateSuperPowerUp(DEFAULT_SUPER_POWER_DURATION_MS);
    }

    public void activateSuperPowerUp(long durationMilliseconds) {
        long duration = Math.max(1_000L, durationMilliseconds);

        /*
         * Save the real player state only on the first activation.
         * Collecting another power-up while active extends the timer without
         * overwriting the original state that must later be restored.
         */
        if (!superPowered) {
            savedSpeed = currentSpeed;
            savedWeaponLevel = weaponLevel;
            savedSpeedLevel = speedLevel;
        }

        superPowered = true;
        invincible = true;
        crashAttackEnabled = true;

        weaponLevel = MAX_WEAPON_LEVEL;
        speedLevel = MAX_SPEED_LEVEL;
        currentSpeed = Math.max(currentSpeed, SUPER_SPEED);

        superPowerEndTime = System.currentTimeMillis() + duration;

        loadOrangePlane();
        currentFrame = 0;
        updateDisplayedFrame();
    }

    private void updateSuperPowerStatus() {
        if (!superPowered) {
            return;
        }

        if (System.currentTimeMillis() < superPowerEndTime) {
            return;
        }

        deactivateSuperPowerUp();
    }

    public void deactivateSuperPowerUp() {
        if (!superPowered) {
            return;
        }

        superPowered = false;
        invincible = false;
        crashAttackEnabled = false;
        superPowerEndTime = 0L;

        currentSpeed = savedSpeed;
        weaponLevel = savedWeaponLevel;
        speedLevel = savedSpeedLevel;

        /*
         * Movement direction is recalculated every frame, so changing back
         * to the saved speed happens immediately without sticking.
         */
        dx = 0;
        dy = 0;

        loadBluePlane();
        currentFrame = 0;
        updateDisplayedFrame();
    }

    public boolean isSuperPowered() {
        return superPowered;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public boolean canCrashEnemies() {
        return crashAttackEnabled;
    }

    public long getSuperPowerRemainingMillis() {
        if (!superPowered) {
            return 0L;
        }

        return Math.max(
                0L,
                superPowerEndTime - System.currentTimeMillis()
        );
    }

    /**
     * Applies permanent score-based upgrades.
     *
     * 0-49 points: weapon 1, speed 2
     * 50-149 points: weapon 2, speed 3
     * 150-249 points: weapon 3, speed 3
     * 250+ points: weapon 4, speed 4
     */
    public void applyScoreProgression(int score) {
        int targetWeaponLevel = 1;
        int targetSpeedLevel = 1;
        int targetSpeed = BASE_SPEED;

        if (score >= 250) {
            targetWeaponLevel = 4;
            targetSpeedLevel = 2;
            targetSpeed = 4;
        } else if (score >= 150) {
            targetWeaponLevel = 3;
            targetSpeedLevel = 1;
            targetSpeed = 3;
        } else if (score >= 50) {
            targetWeaponLevel = 2;
            targetSpeedLevel = 1;
            targetSpeed = 3;
        }

        /*
         * During the temporary orange power-up, preserve the permanent
         * progression in the saved values so it is restored correctly.
         */
        if (superPowered) {
            savedWeaponLevel = targetWeaponLevel;
            savedSpeedLevel = targetSpeedLevel;
            savedSpeed = targetSpeed;
            return;
        }

        weaponLevel = targetWeaponLevel;
        speedLevel = targetSpeedLevel;
        currentSpeed = targetSpeed;
    }

    public int getSpeed() {
        return currentSpeed;
    }

    public int setSpeed(int speed) {
        currentSpeed = Math.max(1, speed);
        return currentSpeed;
    }

    public int getWeaponLevel() {
        return weaponLevel;
    }

    public void setWeaponLevel(int level) {
        if (superPowered) {
            return;
        }

        weaponLevel = Math.max(1, Math.min(level, MAX_WEAPON_LEVEL));
    }

    public void upgradeWeapon() {
        if (!superPowered && weaponLevel < MAX_WEAPON_LEVEL) {
            weaponLevel++;
        }
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void setSpeedLevel(int level) {
        if (superPowered) {
            return;
        }

        speedLevel = Math.max(1, Math.min(level, MAX_SPEED_LEVEL));
        currentSpeed = BASE_SPEED + (speedLevel - 1) * 2;
    }

    public void upgradeSpeed() {
        if (!superPowered && speedLevel < MAX_SPEED_LEVEL) {
            speedLevel++;
            currentSpeed += 2;
        }
    }

    public int getSpriteWidth() {
        return FRAME_WIDTH * SCALE;
    }

    public int getSpriteHeight() {
        return FRAME_HEIGHT * SCALE;
    }

    public void keyPressed(KeyEvent event) {
        int key = event.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            movingUp = true;
        }

        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            movingDown = true;
        }

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            movingLeft = true;
        }

        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            movingRight = true;
        }
    }

    public void keyReleased(KeyEvent event) {
        int key = event.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            movingUp = false;
        }

        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            movingDown = false;
        }

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            movingLeft = false;
        }

        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            movingRight = false;
        }
    }

    /* Backward-compatible name used by older code. */
    public void activatePowerUp() {
        activateSuperPowerUp();
    }
}