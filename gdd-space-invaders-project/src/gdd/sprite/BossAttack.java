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

        /*
         * Fire from the boss's left side because the player is positioned
         * to the left in a side-scrolling shooter.
         */
        int originX = boss.getX() + 5;
        int originY = boss.getY() + Boss.HEIGHT / 2;

        if (boss.getPhase() == BossPhase.PHASE_TWO) {
            /*
             * Three-way spread travelling mainly from right to left.
             */
            bullets.add(
                    new BossBullet(originX, originY, -4.2, -2.2)
            );
            bullets.add(
                    new BossBullet(originX, originY, -4.8, 0)
            );
            bullets.add(
                    new BossBullet(originX, originY, -4.2, 2.2)
            );

            cooldown = 35;
        } else {
            /*
             * Phase one directly aims at the player's center.
             */
            double targetX = player.getX();
            double targetY = player.getY();

            double deltaX = targetX - originX;
            double deltaY = targetY - originY;
            double length = Math.sqrt(
                    deltaX * deltaX + deltaY * deltaY
            );

            if (length == 0) {
                length = 1;
            }

            double speed = 4.5;

            bullets.add(
                    new BossBullet(
                            originX,
                            originY,
                            deltaX / length * speed,
                            deltaY / length * speed
                    )
            );

            cooldown = 55;
        }

        return bullets;
    }
}