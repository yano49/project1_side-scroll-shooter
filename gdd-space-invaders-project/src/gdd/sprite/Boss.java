package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.IMG_BOSS;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Boss extends Sprite {

    public static final int WIDTH = 120;
    public static final int HEIGHT = 120;
    public static final int MAX_HEALTH = 30;

    private static final int ENTRY_SPEED = 2;
    private static final int FIGHT_X = BOARD_WIDTH - WIDTH - 45;
    private static final int FIGHT_Y = 55;
    private static final int TOP_LIMIT = 70;
    private static final int BOTTOM_LIMIT = BOARD_HEIGHT - HEIGHT - 30;

    private int health = MAX_HEALTH;
    private int verticalSpeed = 2;
    private int horizontalSpeed = 2;
    private final boolean verticalFight;
    private BossPhase phase = BossPhase.ENTERING;

    public Boss() {
        this(false);
    }

    public Boss(boolean verticalFight) {
        this.verticalFight = verticalFight;

        if (verticalFight) {
            x = (BOARD_WIDTH - WIDTH) / 2;
            y = -HEIGHT;
        } else {
            x = BOARD_WIDTH + 20;
            y = Math.max(
                    TOP_LIMIT,
                    Math.min((BOARD_HEIGHT - HEIGHT) / 2, BOTTOM_LIMIT)
            );
        }

        Image image = new ImageIcon(IMG_BOSS).getImage();

        setImage(
                image.getScaledInstance(
                        WIDTH,
                        HEIGHT,
                        Image.SCALE_SMOOTH
                )
        );
    }

    @Override
    public void act() {
        if (phase == BossPhase.DEFEATED) {
            return;
        }

        // Enter from the right and move left into the fight position.
        if (phase == BossPhase.ENTERING) {
            if (verticalFight) {
                y += ENTRY_SPEED;
                if (y >= FIGHT_Y) {
                    y = FIGHT_Y;
                    phase = BossPhase.PHASE_ONE;
                }
            } else {
                x -= ENTRY_SPEED;
                if (x <= FIGHT_X) {
                    x = FIGHT_X;
                    phase = BossPhase.PHASE_ONE;
                }
            }
            return;
        }

        if (verticalFight) {
            x += horizontalSpeed;
            if (x <= 0 || x + WIDTH >= BOARD_WIDTH) {
                x = Math.max(0, Math.min(x, BOARD_WIDTH - WIDTH));
                horizontalSpeed = -horizontalSpeed;
            }
        } else {
            y += verticalSpeed;
            if (y <= TOP_LIMIT || y >= BOTTOM_LIMIT) {
                y = Math.max(TOP_LIMIT, Math.min(y, BOTTOM_LIMIT));
                verticalSpeed = -verticalSpeed;
            }
        }
    }

    public void takeDamage(int damage) {
        if (phase == BossPhase.ENTERING
                || phase == BossPhase.DEFEATED) {
            return;
        }

        health = Math.max(0, health - damage);

        if (health == 0) {
            phase = BossPhase.DEFEATED;
            setDying(true);
        } else if (health <= MAX_HEALTH / 2) {
            phase = BossPhase.PHASE_TWO;
            verticalSpeed = verticalSpeed < 0 ? -3 : 3;
            horizontalSpeed = horizontalSpeed < 0 ? -3 : 3;
        }
    }

    public int getHealth() {
        return health;
    }

    public BossPhase getPhase() {
        return phase;
    }

    public boolean canAttack() {
        return phase == BossPhase.PHASE_ONE
                || phase == BossPhase.PHASE_TWO;
    }

    public boolean isVerticalFight() {
        return verticalFight;
    }
}
