package gdd.sprite;

import static gdd.Global.BOARD_WIDTH;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

public class Boss extends Sprite {

    private static final String IMAGE_PATH =
            "gdd-space-invaders-project/src/images/boss.png";

    private static final int BOSS_WIDTH = 140;
    private static final int BOSS_HEIGHT = 100;

    private final int maxHealth = 60;

    private int health = maxHealth;
    private int movementDirection = 1;
    private int movementSpeed = 3;
    private int shootingCooldown = 0;

    private BossPhase phase = BossPhase.PHASE_ONE;
    private BossAttack attack = BossAttack.SINGLE_SHOT;

    public Boss(int x, int y) {

        this.x = x;
        this.y = y;

        setImage(loadBossImage());
    }

    private Image loadBossImage() {

        ImageIcon icon = new ImageIcon(IMAGE_PATH);

        if (icon.getIconWidth() > 0) {
            return icon.getImage().getScaledInstance(
                    BOSS_WIDTH,
                    BOSS_HEIGHT,
                    Image.SCALE_SMOOTH
            );
        }

        // Temporary image if boss.png cannot be found
        BufferedImage fallback =
                new BufferedImage(
                        BOSS_WIDTH,
                        BOSS_HEIGHT,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D graphics = fallback.createGraphics();

        graphics.setColor(Color.MAGENTA);
        graphics.fillRoundRect(
                0,
                0,
                BOSS_WIDTH,
                BOSS_HEIGHT,
                30,
                30
        );

        graphics.setColor(Color.WHITE);
        graphics.fillOval(30, 25, 20, 20);
        graphics.fillOval(90, 25, 20, 20);

        graphics.dispose();

        return fallback;
    }

    @Override
    public void act() {

        if (!isVisible()) {
            return;
        }

        x += movementSpeed * movementDirection;

        int imageWidth = getImage().getWidth(null);

        if (x <= 0) {
            x = 0;
            movementDirection = 1;
        }

        if (x + imageWidth >= BOARD_WIDTH) {
            x = BOARD_WIDTH - imageWidth;
            movementDirection = -1;
        }

        if (shootingCooldown > 0) {
            shootingCooldown--;
        }
    }

    public List<BossBullet> shoot() {

        List<BossBullet> newBullets = new ArrayList<>();

        if (!isVisible() || shootingCooldown > 0) {
            return newBullets;
        }

        int bossCentreX =
                x + getImage().getWidth(null) / 2;

        int bulletStartY =
                y + getImage().getHeight(null);

        switch (attack) {

            case SINGLE_SHOT:

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                0,
                                7
                        )
                );

                shootingCooldown = 55;
                break;

            case TRIPLE_SPREAD:

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                -3,
                                7
                        )
                );

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                0,
                                8
                        )
                );

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                3,
                                7
                        )
                );

                shootingCooldown = 40;
                break;

            case FIVE_WAY_SPREAD:

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                -5,
                                7
                        )
                );

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                -2,
                                8
                        )
                );

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                0,
                                9
                        )
                );

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                2,
                                8
                        )
                );

                newBullets.add(
                        new BossBullet(
                                bossCentreX - 7,
                                bulletStartY,
                                5,
                                7
                        )
                );

                shootingCooldown = 25;
                break;

            default:
                break;
        }

        return newBullets;
    }

    public void takeDamage(int damage) {

        if (!isVisible() || damage <= 0) {
            return;
        }

        health -= damage;

        if (health < 0) {
            health = 0;
        }

        updatePhase();

        if (health == 0) {
            die();
        }
    }

    private void updatePhase() {

        if (health <= maxHealth / 3) {

            phase = BossPhase.PHASE_THREE;
            attack = BossAttack.FIVE_WAY_SPREAD;
            movementSpeed = 5;

        } else if (health <= (maxHealth * 2) / 3) {

            phase = BossPhase.PHASE_TWO;
            attack = BossAttack.TRIPLE_SPREAD;
            movementSpeed = 4;

        } else {

            phase = BossPhase.PHASE_ONE;
            attack = BossAttack.SINGLE_SHOT;
            movementSpeed = 3;
        }
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public BossPhase getPhase() {
        return phase;
    }

    public BossAttack getAttack() {
        return attack;
    }

    public boolean isDefeated() {
        return health <= 0;
    }
}