package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.IMG_SCENE2_BACKGROUND;
import gdd.sprite.Boss;
import gdd.sprite.BossAttack;
import gdd.sprite.BossBullet;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene2 extends JPanel {

    private static final int PLAYER_MARGIN = 8;
    private static final int SHOT_SPEED = 14;
    private static final int MAX_SHOTS = 8;
    private static final String SCENE2_AUDIO =
            "gdd-space-invaders-project/src/audio/scene1.wav";

    private final Game game;
    private final Timer timer = new Timer(1000 / 60, new GameCycle());
    private final Image background =
            new ImageIcon(IMG_SCENE2_BACKGROUND).getImage();
    private final List<Shot> shots = new ArrayList<>();
    private final List<BossBullet> bossBullets = new ArrayList<>();

    private Player player;
    private VerticalPlayerAnimation verticalPlane;
    private Boss boss;
    private BossAttack bossAttack;
    private AudioPlayer audioPlayer;
    private int backgroundOffset;
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private boolean gameOver;

    public Scene2(Game game) {
        this.game = game;
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new Controls());
    }

    public void start() {
        stopAudio();
        player = new Player();
        verticalPlane = new VerticalPlayerAnimation();
        player.setImage(verticalPlane.getImage());
        player.setX((BOARD_WIDTH - player.getImage().getWidth(null)) / 2);
        player.setY(BOARD_HEIGHT - player.getImage().getHeight(null) - 55);
        boss = new Boss(true);
        bossAttack = new BossAttack();
        shots.clear();
        bossBullets.clear();
        backgroundOffset = 0;
        gameOver = false;
        resetKeys();
        timer.start();
        startAudio();
        requestFocusInWindow();
    }

    public void stop() {
        timer.stop();
        stopAudio();
        resetKeys();
    }

    private void startAudio() {
        try {
            audioPlayer = new AudioPlayer(SCENE2_AUDIO);
            audioPlayer.play();
        } catch (Exception exception) {
            audioPlayer = null;
            System.err.println(
                    "Could not start Scene 2 audio: "
                            + exception.getMessage()
            );
        }
    }

    private void stopAudio() {
        if (audioPlayer == null) {
            return;
        }
        try {
            audioPlayer.stop();
        } catch (Exception exception) {
            System.err.println(
                    "Could not stop Scene 2 audio: "
                            + exception.getMessage()
            );
        } finally {
            audioPlayer = null;
        }
    }

    private void update() {
        backgroundOffset = (backgroundOffset + 2) % BOARD_HEIGHT;
        updatePlayer();

        if (boss.isVisible()) {
            boss.act();
            bossBullets.addAll(bossAttack.update(boss, player));
        }

        updateShots();
        updateBossBullets();
    }

    private void updatePlayer() {
        player.act();
        verticalPlane.update();
        player.setImage(verticalPlane.getImage());
        int speed = player.getSpeed();
        int nextX = player.getX() + (right ? speed : 0) - (left ? speed : 0);
        int nextY = player.getY() + (down ? speed : 0) - (up ? speed : 0);
        int playerWidth = player.getImage().getWidth(null);
        int playerHeight = player.getImage().getHeight(null);
        player.setX(Math.max(
                PLAYER_MARGIN,
                Math.min(nextX, BOARD_WIDTH - playerWidth - PLAYER_MARGIN)
        ));
        player.setY(Math.max(
                BOARD_HEIGHT / 3,
                Math.min(nextY, BOARD_HEIGHT - playerHeight - PLAYER_MARGIN)
        ));
    }

    private void updateShots() {
        List<Shot> removed = new ArrayList<>();
        for (Shot shot : shots) {
            if (!shot.isVisible()) {
                removed.add(shot);
                continue;
            }

            shot.setY(shot.getY() - SHOT_SPEED);
            if (shot.getY() + shot.getImage().getHeight(null) < 0) {
                shot.die();
                removed.add(shot);
                continue;
            }

            if (boss.isVisible() && shot.collidesWith(boss)) {
                boss.takeDamage(1);
                shot.die();
                removed.add(shot);
                if (boss.isDying()) {
                    boss.die();
                    bossBullets.clear();
                    stop();
                    game.loadEndingScene();
                    return;
                }
            }
        }
        shots.removeAll(removed);
    }

    private void updateBossBullets() {
        List<BossBullet> removed = new ArrayList<>();
        for (BossBullet bullet : bossBullets) {
            if (!bullet.isVisible()) {
                removed.add(bullet);
                continue;
            }
            bullet.act();
            if (bullet.collidesWith(player)) {
                bullet.die();
                removed.add(bullet);
                gameOver = true;
                timer.stop();
                stopAudio();
            }
        }
        bossBullets.removeAll(removed);
    }

    private void fire() {
        if (gameOver || shots.size() >= MAX_SHOTS) {
            return;
        }

        int weaponLevel = Math.max(1, Math.min(4, player.getWeaponLevel()));
        int[] offsets = switch (weaponLevel) {
            case 2 -> new int[]{-12, 12};
            case 3 -> new int[]{-22, 0, 22};
            case 4 -> new int[]{-33, -11, 11, 33};
            default -> new int[]{0};
        };

        for (int offset : offsets) {
            if (shots.size() >= MAX_SHOTS) {
                break;
            }
            Shot shot = new Shot(player.getX(), player.getY());
            shot.setImage(createVerticalShotImage(shot.getImage()));
            int shotWidth = shot.getImage().getWidth(null);
            shot.setX(
                    player.getX()
                            + player.getImage().getWidth(null) / 2
                            - shotWidth / 2
                            + offset
            );
            shot.setY(player.getY() - shot.getImage().getHeight(null));
            shots.add(shot);
        }
    }

    private Image createVerticalShotImage(Image sideShot) {
        int sourceWidth = sideShot.getWidth(null);
        int sourceHeight = sideShot.getHeight(null);
        BufferedImage verticalShot = new BufferedImage(
                sourceHeight,
                sourceWidth,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = verticalShot.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );
        graphics.translate(0, sourceWidth);
        graphics.rotate(-Math.PI / 2.0);
        graphics.drawImage(sideShot, 0, 0, this);
        graphics.dispose();
        return verticalShot;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        drawBackground(graphics);

        if (boss != null && boss.isVisible()) {
            graphics.drawImage(boss.getImage(), boss.getX(), boss.getY(), this);
            drawBossHealth(graphics);
        }

        for (Shot shot : shots) {
            if (shot.isVisible()) {
                graphics.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
        for (BossBullet bullet : bossBullets) {
            if (bullet.isVisible()) {
                graphics.drawImage(
                        bullet.getImage(), bullet.getX(), bullet.getY(), this
                );
            }
        }
        if (player != null && player.isVisible()) {
            graphics.drawImage(
                    player.getImage(), player.getX(), player.getY(), this
            );
        }

        if (gameOver) {
            drawGameOver(graphics);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics graphics) {
        if (background.getWidth(null) > 0) {
            graphics.drawImage(
                    background, 0, backgroundOffset - BOARD_HEIGHT,
                    BOARD_WIDTH, BOARD_HEIGHT, this
            );
            graphics.drawImage(
                    background, 0, backgroundOffset,
                    BOARD_WIDTH, BOARD_HEIGHT, this
            );
        }
    }

    private void drawBossHealth(Graphics graphics) {
        int width = 360;
        int x = (BOARD_WIDTH - width) / 2;
        int healthWidth = width * boss.getHealth() / Boss.MAX_HEALTH;
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(x, 18, width, 16);
        graphics.setColor(Color.RED);
        graphics.fillRect(x, 18, healthWidth, 16);
        graphics.setColor(Color.WHITE);
        graphics.drawRect(x, 18, width, 16);
        graphics.drawString("FINAL BOSS", x, 15);
    }

    private void drawGameOver(Graphics graphics) {
        graphics.setColor(new Color(0, 0, 0, 190));
        graphics.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Helvetica", Font.BOLD, 34));
        String text = "MISSION FAILED";
        int x = (BOARD_WIDTH - graphics.getFontMetrics().stringWidth(text)) / 2;
        graphics.drawString(text, x, BOARD_HEIGHT / 2);
        graphics.setFont(graphics.getFont().deriveFont(18f));
        String retry = "Press R to retry";
        x = (BOARD_WIDTH - graphics.getFontMetrics().stringWidth(retry)) / 2;
        graphics.drawString(retry, x, BOARD_HEIGHT / 2 + 42);
    }

    private void resetKeys() {
        up = down = left = right = false;
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            update();
            repaint();
        }
    }

    private class Controls extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> up = true;
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> down = true;
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> left = true;
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right = true;
                case KeyEvent.VK_SPACE -> fire();
                case KeyEvent.VK_R -> {
                    if (gameOver) {
                        game.loadScene1();
                    }
                }
                default -> {
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> up = false;
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> down = false;
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> left = false;
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right = false;
                default -> {
                }
            }
        }
    }
}
