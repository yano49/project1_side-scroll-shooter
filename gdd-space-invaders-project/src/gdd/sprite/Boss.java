package gdd.sprite;

import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.IMG_BOSS;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Boss extends Sprite {

    public static final int WIDTH = 120;
    public static final int HEIGHT = 120;
    public static final int MAX_HEALTH = 30;
    private static final int FIGHT_Y = 45;

    private int health = MAX_HEALTH;
    private int horizontalSpeed = 2;
    private BossPhase phase = BossPhase.ENTERING;

    public Boss() {
        x = (BOARD_WIDTH - WIDTH) / 2;
        y = -HEIGHT;

        Image image = new ImageIcon(IMG_BOSS).getImage();
        setImage(image.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH));
    }

    @Override
    public void act() {
        if (phase == BossPhase.DEFEATED) {
            return;
        }

        if (phase == BossPhase.ENTERING) {
            y += 2;
            if (y >= FIGHT_Y) {
                y = FIGHT_Y;
                phase = BossPhase.PHASE_ONE;
            }
            return;
        }

        x += horizontalSpeed;
        if (x <= 0 || x + WIDTH >= BOARD_WIDTH) {
            x = Math.max(0, Math.min(x, BOARD_WIDTH - WIDTH));
            horizontalSpeed = -horizontalSpeed;
        }
    }

    public void takeDamage(int damage) {
        if (phase == BossPhase.ENTERING || phase == BossPhase.DEFEATED) {
            return;
        }

        health = Math.max(0, health - damage);
        if (health == 0) {
            phase = BossPhase.DEFEATED;
            setDying(true);
        } else if (health <= MAX_HEALTH / 2) {
            phase = BossPhase.PHASE_TWO;
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
        return phase == BossPhase.PHASE_ONE || phase == BossPhase.PHASE_TWO;
    }
}
