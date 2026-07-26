package gdd.sprite;

import static gdd.Global.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 * A tougher enemy that survives multiple hits before dying.
 * Flashes ("twinkles") briefly whenever it takes a hit that doesn't kill it,
 * and when finally destroyed it should be replaced by 3 regular Alien1s
 * (handled by the caller, e.g. Scene1, via getSpawnX()/getSpawnY() after
 * hit() returns true).
 */
public class MiniBoss extends Enemy {

    private int health = MINIBOSS_HEALTH;

    private Image normalImage;
    private Image flashImage;
    private int flashTimer = 0;

    // How many frames each on/off blink lasts within a flash
    private static final int BLINK_RATE = 1;

    public MiniBoss(int x, int y) {
        super(x, y);
        initMiniBoss(x, y);
    }

    private void initMiniBoss(int x, int y) {

        this.x = x;
        this.y = y;

        Image loadedImage = null;

        try {
            var ii = new ImageIcon(IMG_MINIBOSS);

            if (ii.getIconWidth() > 0 && ii.getIconHeight() > 0) {
                // Scale miniBoss to be about 3x the size of a regular alien
                // Regular alien: 24x24 (ALIEN_WIDTH * SCALE_FACTOR = 12 * SCALE_FACTOR)
                // MiniBoss: scale to ~72x60 pixels for balanced gameplay
                int scaledWidth = ALIEN_WIDTH * 3;
                int scaledHeight = ALIEN_HEIGHT * 3;
                
                // Use a more robust scaling approach to avoid color model issues
                loadedImage = ii.getImage().getScaledInstance(
                        scaledWidth,
                        scaledHeight,
                        Image.SCALE_DEFAULT);
            } else {
                System.err.println("MiniBoss: could not load image at \"" + IMG_MINIBOSS
                        + "\" (check the path/filename/case). Falling back to the regular enemy sprite.");
            }
        } catch (Exception e) {
            System.err.println("MiniBoss: error loading image \"" + IMG_MINIBOSS + "\": " + e);
        }

        // this.image already holds the regular Enemy sprite set by super(x, y);
        // reuse it as a fallback so a bad miniBoss.png never prevents the spawn.
        normalImage = (loadedImage != null) ? loadedImage : this.image;
        flashImage = createFlashImage(normalImage);
        setImage(normalImage);
        setVisible(true);
    }

    // Builds a bright whited-out version of the sprite to use as the "hit" flash frame
    private Image createFlashImage(Image source) {

        int w = source.getWidth(null);
        int h = source.getHeight(null);

        if (w <= 0 || h <= 0) {
            return source;
        }

        var buffered = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffered.createGraphics();
        g2d.drawImage(source, 0, 0, null);
        // Make the flash brighter for better visibility of hits
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.9f));
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();

        return buffered;
    }

    @Override
    public void act(int direction) {

        // Side-scrolling mini boss: enter from the right and travel left.
        this.x -= 1;

        tickFlash();
    }

    private void tickFlash() {

        if (flashTimer <= 0) {
            return;
        }

        flashTimer--;

        boolean blinkOn = (flashTimer / BLINK_RATE) % 2 == 0;
        setImage(blinkOn ? flashImage : normalImage);

        if (flashTimer == 0) {
            setImage(normalImage);
        }
    }

    /**
     * Registers a shot hitting the mini boss.
     *
     * @return true if this hit destroyed the mini boss (health reached 0),
     *         false if it survived (and is now twinkling).
     */
    public boolean hit() {

        health--;

        if (health <= 0) {
            return true;
        }

        flashTimer = MINIBOSS_FLASH_DURATION;
        return false;
    }

    public int getHealth() {
        return health;
    }

    // Actual on-screen size of the sprite, for hit-box purposes -
    // the boss image may be bigger than the standard ALIEN_WIDTH/HEIGHT
    public int getSpriteWidth() {
        return (this.image != null) ? this.image.getWidth(null) : ALIEN_WIDTH;
    }

    public int getSpriteHeight() {
        return (this.image != null) ? this.image.getHeight(null) : ALIEN_HEIGHT;
    }
}