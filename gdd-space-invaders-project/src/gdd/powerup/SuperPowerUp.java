package gdd.powerup;

import static gdd.Global.BOARD_WIDTH;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Orange collectible from the top-left of the 1P row in spites.png.
 */
public class SuperPowerUp extends PowerUp {

    public static final long EFFECT_DURATION_MS = 10_000L;

    /*
     * Keep it in the game for 25 seconds so the player has enough time
     * to notice and collect it during testing.
     */
    public static final long WORLD_LIFETIME_MS = 25_000L;

    public static final int DRAW_WIDTH = 32;
    public static final int DRAW_HEIGHT = 32;

    private static final int SPRITE_X = 0;
    private static final int SPRITE_Y = 8;
    private static final int SPRITE_WIDTH = 16;
    private static final int SPRITE_HEIGHT = 16;

    private static final int MOVE_SPEED = 3;

    private final long spawnTime;

    public SuperPowerUp(int x, int y) {
        super(x, y);
        spawnTime = System.currentTimeMillis();
        setImage(loadOrangeOrb());
        setVisible(true);
    }

    private Image loadOrangeOrb() {
        BufferedImage spriteSheet = readSpriteSheet();

        if (spriteSheet != null
                && SPRITE_X + SPRITE_WIDTH <= spriteSheet.getWidth()
                && SPRITE_Y + SPRITE_HEIGHT <= spriteSheet.getHeight()) {

            BufferedImage orangeOrb = spriteSheet.getSubimage(
                    SPRITE_X,
                    SPRITE_Y,
                    SPRITE_WIDTH,
                    SPRITE_HEIGHT
            );

            BufferedImage scaled = new BufferedImage(
                    DRAW_WIDTH,
                    DRAW_HEIGHT,
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
            );
            graphics.drawImage(
                    orangeOrb,
                    0,
                    0,
                    DRAW_WIDTH,
                    DRAW_HEIGHT,
                    null
            );
            graphics.dispose();

            return scaled;
        }

        System.err.println(
                "Could not load orange orb from spites.png; using fallback icon."
        );
        return createFallbackIcon();
    }

    private BufferedImage readSpriteSheet() {
        String[] possiblePaths = {
            "gdd-space-invaders-project/src/images/spites.png",
            "src/images/spites.png",
            "images/spites.png"
        };

        for (String path : possiblePaths) {
            File file = new File(path);

            if (!file.isFile()) {
                continue;
            }

            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    return image;
                }
            } catch (IOException exception) {
                System.err.println(
                        "Failed reading " + path + ": "
                                + exception.getMessage()
                );
            }
        }

        try {
            var resource = SuperPowerUp.class.getResource(
                    "/images/spites.png"
            );

            if (resource != null) {
                return ImageIO.read(resource);
            }
        } catch (IOException exception) {
            System.err.println(
                    "Failed reading sprite resource: "
                            + exception.getMessage()
            );
        }

        return null;
    }

    private Image createFallbackIcon() {
        BufferedImage fallback = new BufferedImage(
                DRAW_WIDTH,
                DRAW_HEIGHT,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = fallback.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillOval(4, 2, DRAW_WIDTH - 8, DRAW_HEIGHT - 4);
        graphics.setColor(Color.ORANGE);
        graphics.fillOval(10, 7, DRAW_WIDTH - 20, DRAW_HEIGHT - 14);
        graphics.setColor(Color.RED);
        graphics.fillOval(17, 12, DRAW_WIDTH - 34, DRAW_HEIGHT - 24);
        graphics.dispose();

        return fallback;
    }

    @Override
    public void act() {
        x -= MOVE_SPEED;

        boolean expired = System.currentTimeMillis() - spawnTime
                >= WORLD_LIFETIME_MS;

        if (expired || x + DRAW_WIDTH < 0 || x > BOARD_WIDTH + 250) {
            die();
        }
    }

    @Override
    public void upgrade(Player player) {
        if (player == null || !isVisible()) {
            return;
        }

        player.activateSuperPowerUp(EFFECT_DURATION_MS);
        die();
    }
}