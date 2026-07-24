package gdd;

import gdd.sprite.Boss;
import gdd.sprite.BossBullet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class BossTest extends JPanel implements ActionListener {

    private final Boss boss;
    private final List<BossBullet> bossBullets;
    private final Timer timer;

    public BossTest() {

        boss = new Boss(280, 40);
        bossBullets = new ArrayList<>();

        setPreferredSize(
                new Dimension(
                        Global.BOARD_WIDTH,
                        Global.BOARD_HEIGHT
                )
        );

        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new BossTestKeyAdapter());

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (!boss.isDefeated()) {

            boss.act();

            List<BossBullet> firedBullets = boss.shoot();
            bossBullets.addAll(firedBullets);
        }

        for (BossBullet bullet : bossBullets) {

            if (bullet.isVisible()) {
                bullet.act();
            }
        }

        bossBullets.removeIf(
                bullet -> !bullet.isVisible()
        );

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        if (boss.isVisible()) {
            graphics.drawImage(
                    boss.getImage(),
                    boss.getX(),
                    boss.getY(),
                    this
            );
        }

        for (BossBullet bullet : bossBullets) {

            if (bullet.isVisible()) {
                graphics.drawImage(
                        bullet.getImage(),
                        bullet.getX(),
                        bullet.getY(),
                        this
                );
            }
        }

        drawHealthBar(graphics);

        graphics.setColor(Color.WHITE);
        graphics.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        graphics.drawString(
                "Press SPACE to damage the boss",
                20,
                Global.BOARD_HEIGHT - 60
        );

        graphics.drawString(
                "Current phase: " + boss.getPhase(),
                20,
                Global.BOARD_HEIGHT - 35
        );

        if (boss.isDefeated()) {

            graphics.setColor(Color.GREEN);
            graphics.setFont(
                    new Font("Arial", Font.BOLD, 36)
            );

            graphics.drawString(
                    "BOSS DEFEATED!",
                    200,
                    Global.BOARD_HEIGHT / 2
            );
        }
    }

    private void drawHealthBar(Graphics graphics) {

        int barX = 150;
        int barY = 15;
        int barWidth = 400;
        int barHeight = 20;

        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(
                barX,
                barY,
                barWidth,
                barHeight
        );

        int currentHealthWidth =
                (int) (
                        barWidth
                                * ((double) boss.getHealth()
                                / boss.getMaxHealth())
                );

        graphics.setColor(Color.RED);
        graphics.fillRect(
                barX,
                barY,
                currentHealthWidth,
                barHeight
        );

        graphics.setColor(Color.WHITE);
        graphics.drawRect(
                barX,
                barY,
                barWidth,
                barHeight
        );

        graphics.drawString(
                "FINAL BOSS: "
                        + boss.getHealth()
                        + "/"
                        + boss.getMaxHealth(),
                barX + 120,
                barY + 16
        );
    }

    private class BossTestKeyAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent event) {

            if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                boss.takeDamage(5);
            }
        }
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {

            JFrame frame = new JFrame("Final Boss Test");

            frame.add(new BossTest());
            frame.pack();

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}