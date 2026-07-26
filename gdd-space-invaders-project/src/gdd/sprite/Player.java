package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player extends Sprite {

    private static final int START_X = 135;
    private static final int START_Y = 540;

    // ===============================
    // Sprite Animation Settings
    // ===============================

    private BufferedImage spriteSheet;
    private BufferedImage[] frames = new BufferedImage[4];

    private int currentFrame = 0;
    private int animationTick = 0;

    // Adjust ONLY these values
    private static final int START_X_FRAME = 24;
    private static final int START_Y_FRAME = 8;

    // Size of one frame
    private static final int FRAME_WIDTH = 24;
    private static final int FRAME_HEIGHT = 16;

    // Distance between two ship frames
    private static final int GAP = 32;

    // Display size
    private static final int SCALE = 3;

    private int width = FRAME_WIDTH * SCALE;

    // ===============================
    // Player Stats
    // ===============================

    private int currentSpeed = 2;

    private int weaponLevel = 1;
    private final int MAX_WEAPON_LEVEL = 4;

    private int speedLevel = 1;
    private final int MAX_SPEED_LEVEL = 2;

    private int dy;

    private boolean poweredUp = false;

    private long powerUpStartTime = 0;

    private void loadBluePlane() {

        frames[0] = spriteSheet.getSubimage(
                START_X_FRAME,
                START_Y_FRAME,
                FRAME_WIDTH,
                FRAME_HEIGHT);
    
        frames[1] = spriteSheet.getSubimage(
                START_X_FRAME + GAP,
                START_Y_FRAME,
                FRAME_WIDTH,
                FRAME_HEIGHT);
    
        frames[2] = spriteSheet.getSubimage(
                START_X_FRAME,
                START_Y_FRAME,
                FRAME_WIDTH,
                FRAME_HEIGHT);
    
        frames[3] = spriteSheet.getSubimage(
                START_X_FRAME + GAP,
                START_Y_FRAME,
                FRAME_WIDTH,
                FRAME_HEIGHT);
    }

    private void loadOrangePlane() {

        int orangeY = START_Y_FRAME + 32;
    
        frames[0] = spriteSheet.getSubimage(
                START_X_FRAME,
                orangeY,
                FRAME_WIDTH,
                FRAME_HEIGHT);
    
        frames[1] = spriteSheet.getSubimage(
                START_X_FRAME + GAP,
                orangeY,
                FRAME_WIDTH,
                FRAME_HEIGHT);
    
        frames[2] = spriteSheet.getSubimage(
                START_X_FRAME,
                orangeY,
                FRAME_WIDTH,
                FRAME_HEIGHT);
     
        frames[3] = spriteSheet.getSubimage(
                START_X_FRAME + GAP,
                orangeY,
                FRAME_WIDTH,
                FRAME_HEIGHT);
    }

    public Player() {
        initPlayer();
    }

    private void initPlayer() {

        try {

            spriteSheet = ImageIO.read(new File("gdd-space-invaders-project/src/images/spites.png"));

            loadBluePlane();

            setImage(frames[0].getScaledInstance(
                FRAME_WIDTH * SCALE,
                FRAME_HEIGHT * SCALE,
            Image.SCALE_FAST));

        } catch (IOException e) {
            e.printStackTrace();
        }

        setX(START_X);
        setY(START_Y);
    }

    @Override
    public void act() {

        y += dy;

        if (y < 0)
            y = 0;
        
        if (y > BOARD_HEIGHT - FRAME_HEIGHT * SCALE)
            y = BOARD_HEIGHT - FRAME_HEIGHT * SCALE;

        animationTick++;

        if (animationTick >= 8) {

            animationTick = 0;

            currentFrame++;

            if (currentFrame >= frames.length)
                currentFrame = 0;

            setImage(frames[currentFrame].getScaledInstance(
                    FRAME_WIDTH * SCALE,
                    FRAME_HEIGHT * SCALE,
                    Image.SCALE_FAST));
        }

        if (poweredUp &&
            System.currentTimeMillis() - powerUpStartTime >= 60000) {
        
            poweredUp = false;
        
            loadBluePlane();
        
            currentFrame = 0;
        
            setImage(frames[currentFrame].getScaledInstance(
                    FRAME_WIDTH * SCALE,
                    FRAME_HEIGHT * SCALE,
                    Image.SCALE_FAST));
        }

    }

    // ===============================
    // Speed
    // ===============================

    public int getSpeed() {
        return currentSpeed;
    }

    public int setSpeed(int speed) {

        if (speed < 1)
            speed = 1;

        currentSpeed = speed;

        return currentSpeed;
    }

    // ===============================
    // Weapon Upgrade
    // ===============================

    public int getWeaponLevel() {
        return weaponLevel;
    }

    public void upgradeWeapon() {

        if (weaponLevel < MAX_WEAPON_LEVEL)
            weaponLevel++;
    }

    // ===============================
    // Speed Upgrade
    // ===============================

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void upgradeSpeed() {

        if (speedLevel < MAX_SPEED_LEVEL) {

            speedLevel++;
            currentSpeed++;
        }
    }

    // ===============================
    // Keyboard
    // ===============================

    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();
    
        if (key == KeyEvent.VK_UP)
            dy = -currentSpeed;
    
        if (key == KeyEvent.VK_DOWN)
            dy = currentSpeed;
    }

    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();
    
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN)
            dy = 0;
    }

    public void activatePowerUp() {

        poweredUp = true;
        powerUpStartTime = System.currentTimeMillis();
    
        loadOrangePlane();
    
        currentFrame = 0;
    
        setImage(frames[currentFrame].getScaledInstance(
                FRAME_WIDTH * SCALE,
                FRAME_HEIGHT * SCALE,
                Image.SCALE_FAST));
    }
}