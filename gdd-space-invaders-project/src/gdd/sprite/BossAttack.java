package gdd.sprite;

import java.util.ArrayList;
import java.util.List;

public class BossAttack {

    private int cooldown;

    public List<BossBullet> update(Boss boss, Player player) {
        List<BossBullet> bullets = new ArrayList<>();
        if (!boss.canAttack() || cooldown-- > 0) {
            return bullets;
        }

        int originX = boss.getX() + Boss.WIDTH / 2;
        int originY = boss.getY() + Boss.HEIGHT - 20;

        if (boss.getPhase() == BossPhase.PHASE_TWO) {
            bullets.add(new BossBullet(originX, originY, -2.2, 4.2));
            bullets.add(new BossBullet(originX, originY, 0, 4.8));
            bullets.add(new BossBullet(originX, originY, 2.2, 4.2));
            cooldown = 35;
        } else {
            double deltaX = player.getX() - originX;
            double deltaY = Math.max(1, player.getY() - originY);
            double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            bullets.add(new BossBullet(
                    originX, originY, deltaX / length * 4, deltaY / length * 4));
            cooldown = 55;
        }

        return bullets;
    }
}
