package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Boss;
import gdd.sprite.BossAttack;
import gdd.sprite.BossBullet;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.MiniBoss;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {

    private static final int BACKGROUND_SCROLL_SPEED = 1;
    private static final int BOSS_SPAWN_FRAME = 650;
    private static final int MAX_PLAYER_SHOTS = 8;
    private static final int ENEMY_SPAWN_X = BOARD_WIDTH + 20;

    private final Game game;
    private final HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private final Random randomizer = new Random();

    private int frame;
    private int direction;
    private int deaths;
    private int backgroundX;

    private boolean inGame;
    private boolean bossSpawned;
    private String message;

    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private List<BossBullet> bossBullets;

    private Player player;
    private Boss boss;
    private BossAttack bossAttack;

    private Image backgroundImage;
    private Timer timer;
    private AudioPlayer audioPlayer;

    public Scene1(Game game) {
        this.game = game;
        loadSpawnDetails();
    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setBackground(Color.BLACK);

        loadBackgroundImage();
        gameInit();
        initAudio();

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        requestFocusInWindow();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }

        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
            } catch (Exception exception) {
                System.err.println(
                        "Error stopping Scene1 audio: "
                                + exception.getMessage()
                );
            }
        }
    }

    private void gameInit() {
        frame = 0;
        direction = -1;
        deaths = 0;
        backgroundX = 0;

        inGame = true;
        bossSpawned = false;
        message = "Game Over";

        powerups = new ArrayList<>();
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        bossBullets = new ArrayList<>();

        player = new Player();
        boss = null;
        bossAttack = new BossAttack();
    }

    private void loadBackgroundImage() {
        ImageIcon backgroundIcon = new ImageIcon(IMG_BACKGROUND);

        if (backgroundIcon.getIconWidth() <= 0
                || backgroundIcon.getIconHeight() <= 0) {
            backgroundImage = null;
            System.err.println(
                    "Could not load background image: "
                            + IMG_BACKGROUND
            );
            return;
        }

        backgroundImage = backgroundIcon.getImage();
    }

    private void initAudio() {
        try {
            String filePath =
                    "gdd-space-invaders-project/src/audio/scene1.wav";

            audioPlayer = new AudioPlayer(filePath);
            audioPlayer.play();
        } catch (Exception exception) {
            audioPlayer = null;
            System.err.println(
                    "Error initializing Scene1 audio: "
                            + exception.getMessage()
            );
        }
    }

    private void loadSpawnDetails() {
        spawnMap.clear();

        spawnMap.put(
                50,
                new SpawnDetails("PowerUp-SpeedUp", 100, 0)
        );

        spawnMap.put(
                200,
                new SpawnDetails("Alien1", 200, 0)
        );

        spawnMap.put(
                300,
                new SpawnDetails("Alien1", 300, 0)
        );

        spawnMap.put(
                400,
                new SpawnDetails("Alien1", 400, 0)
        );

        spawnMap.put(
                401,
                new SpawnDetails("Alien1", 450, 0)
        );

        spawnMap.put(
                402,
                new SpawnDetails("Alien1", 500, 0)
        );

        spawnMap.put(
                403,
                new SpawnDetails("Alien1", 550, 0)
        );

        spawnMap.put(
                500,
                new SpawnDetails("Alien1", 100, 0)
        );

        spawnMap.put(
                501,
                new SpawnDetails("Alien1", 150, 0)
        );

        spawnMap.put(
                502,
                new SpawnDetails("Alien1", 200, 0)
        );

        spawnMap.put(
                503,
                new SpawnDetails("Alien1", 350, 0)
        );

        spawnMap.put(
                600,
                new SpawnDetails("MiniBoss", 300, 0)
        );
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        doDrawing(graphics);
    }

    private void doDrawing(Graphics graphics) {
        drawScrollingBackground(graphics);

        if (inGame) {
            drawPowerUps(graphics);
            drawAliens(graphics);
            drawBoss(graphics);
            drawBossBullets(graphics);
            drawPlayer(graphics);
            drawShots(graphics);
            drawExplosions(graphics);
            drawDashboard(graphics);
        } else {
            gameOver(graphics);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawScrollingBackground(Graphics graphics) {
        if (backgroundImage == null) {
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
            return;
        }

        graphics.drawImage(
                backgroundImage,
                backgroundX,
                0,
                BOARD_WIDTH,
                BOARD_HEIGHT,
                this
        );

        graphics.drawImage(
                backgroundImage,
                backgroundX + BOARD_WIDTH,
                0,
                BOARD_WIDTH,
                BOARD_HEIGHT,
                this
        );
    }

    private void drawPowerUps(Graphics graphics) {
        for (PowerUp powerUp : powerups) {
            if (powerUp.isVisible()) {
                graphics.drawImage(
                        powerUp.getImage(),
                        powerUp.getX(),
                        powerUp.getY(),
                        this
                );
            }

            if (powerUp.isDying()) {
                powerUp.die();
            }
        }
    }

    private void drawAliens(Graphics graphics) {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                graphics.drawImage(
                        enemy.getImage(),
                        enemy.getX(),
                        enemy.getY(),
                        this
                );
            }

            if (enemy.isDying()) {
                enemy.die();
            }
        }
    }

    private void drawPlayer(Graphics graphics) {
        if (player != null && player.isVisible()) {
            graphics.drawImage(
                    player.getImage(),
                    player.getX(),
                    player.getY(),
                    this
            );
        }

        if (player != null && player.isDying()) {
            player.die();
            finishGame("Game Over");
        }
    }

    private void drawShots(Graphics graphics) {
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                graphics.drawImage(
                        shot.getImage(),
                        shot.getX(),
                        shot.getY(),
                        this
                );
            }
        }
    }

    private void drawExplosions(Graphics graphics) {
        List<Explosion> explosionsToRemove = new ArrayList<>();

        for (Explosion explosion : explosions) {
            if (!explosion.isVisible()) {
                explosionsToRemove.add(explosion);
                continue;
            }

            graphics.drawImage(
                    explosion.getImage(),
                    explosion.getX(),
                    explosion.getY(),
                    this
            );

            explosion.visibleCountDown();

            if (!explosion.isVisible()) {
                explosionsToRemove.add(explosion);
            }
        }

        explosions.removeAll(explosionsToRemove);
    }

    private void drawBoss(Graphics graphics) {
        if (boss == null || !boss.isVisible()) {
            return;
        }

        graphics.drawImage(
                boss.getImage(),
                boss.getX(),
                boss.getY(),
                this
        );

        int barWidth = 300;
        int barHeight = 14;
        int barX = (BOARD_WIDTH - barWidth) / 2;
        int barY = 20;

        int health = Math.max(0, boss.getHealth());
        int healthWidth = barWidth * health / Boss.MAX_HEALTH;

        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(barX, barY, barWidth, barHeight);

        graphics.setColor(Color.RED);
        graphics.fillRect(barX, barY, healthWidth, barHeight);

        graphics.setColor(Color.WHITE);
        graphics.drawRect(barX, barY, barWidth, barHeight);
        graphics.drawString("FINAL BOSS", barX, barY - 3);
    }

    private void drawBossBullets(Graphics graphics) {
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
    }

    private void drawDashboard(Graphics graphics) {
        graphics.setColor(Color.WHITE);
        graphics.drawString("FRAME: " + frame, 10, 20);
    }

    private void gameOver(Graphics graphics) {
        int boxX = 50;
        int boxY = BOARD_HEIGHT / 2 - 30;
        int boxWidth = BOARD_WIDTH - 100;
        int boxHeight = 50;

        graphics.setColor(new Color(0, 32, 48, 220));
        graphics.fillRect(boxX, boxY, boxWidth, boxHeight);

        graphics.setColor(Color.WHITE);
        graphics.drawRect(boxX, boxY, boxWidth, boxHeight);

        Font font = new Font("Helvetica", Font.BOLD, 14);
        graphics.setFont(font);

        int textX = (
                BOARD_WIDTH
                        - graphics.getFontMetrics(font).stringWidth(message)
        ) / 2;

        graphics.drawString(message, textX, BOARD_HEIGHT / 2);
    }

    private void update() {
        updateBackground();
        spawnScheduledObject();
        spawnBoss();
        updatePlayer();
        updatePowerUps();
        updateEnemies();
        updateBoss();
        updateBossBullets();
        checkPlayerCollisions();

        if (inGame) {
            updateShots();
        }
    }

    private void updateBackground() {
        backgroundX -= BACKGROUND_SCROLL_SPEED;

        if (backgroundX <= -BOARD_WIDTH) {
            backgroundX += BOARD_WIDTH;
        }
    }

    private void spawnScheduledObject() {
        SpawnDetails spawnDetails = spawnMap.get(frame);

        if (spawnDetails == null) {
            return;
        }

        switch (spawnDetails.type) {
            case "Alien1":
                enemies.add(
                        new Alien1(
                                ENEMY_SPAWN_X,
                                randomEnemyY(ALIEN_HEIGHT)
                        )
                );
                break;

            case "MiniBoss":
                enemies.add(
                        new MiniBoss(
                                ENEMY_SPAWN_X,
                                randomEnemyY(ALIEN_HEIGHT * 3)
                        )
                );
                break;

            case "PowerUp-SpeedUp":
                powerups.add(
                        new SpeedUp(
                                ENEMY_SPAWN_X,
                                randomEnemyY(32)
                        )
                );
                break;

            case "Alien2":
                // Add Alien2 here after its class is implemented.
                break;

            default:
                System.err.println(
                        "Unknown spawn type: "
                                + spawnDetails.type
                );
                break;
        }
    }

    private void spawnBoss() {
        if (!bossSpawned && frame >= BOSS_SPAWN_FRAME) {
            boss = new Boss();
            bossSpawned = true;
        }
    }

    private void updatePlayer() {
        if (player != null && player.isVisible()) {
            player.act();
        }
    }

    private void updatePowerUps() {
        List<PowerUp> powerUpsToRemove = new ArrayList<>();

        for (PowerUp powerUp : powerups) {
            if (!powerUp.isVisible()) {
                powerUpsToRemove.add(powerUp);
                continue;
            }

            powerUp.act();

            if (player != null
                    && player.isVisible()
                    && powerUp.collidesWith(player)) {
                powerUp.upgrade(player);
            }

            if (!powerUp.isVisible()) {
                powerUpsToRemove.add(powerUp);
            }
        }

        powerups.removeAll(powerUpsToRemove);
    }

    private void updateEnemies() {
        List<Enemy> enemiesToRemove = new ArrayList<>();

        for (Enemy enemy : enemies) {
            if (!enemy.isVisible()) {
                enemiesToRemove.add(enemy);
                continue;
            }

            enemy.act(direction);

            int enemyWidth = enemy.getImage() == null
                    ? ALIEN_WIDTH
                    : enemy.getImage().getWidth(null);

            if (enemy.getX() + enemyWidth < 0) {
                enemy.die();
                enemiesToRemove.add(enemy);
            }
        }

        enemies.removeAll(enemiesToRemove);
    }

    private void updateBoss() {
        if (boss == null || !boss.isVisible()) {
            return;
        }

        boss.act();

        List<BossBullet> newBullets =
                bossAttack.update(boss, player);

        if (newBullets != null && !newBullets.isEmpty()) {
            bossBullets.addAll(newBullets);
        }
    }

    private void updateBossBullets() {
        List<BossBullet> bulletsToRemove = new ArrayList<>();

        for (BossBullet bullet : bossBullets) {
            if (!bullet.isVisible()) {
                bulletsToRemove.add(bullet);
                continue;
            }

            bullet.act();

            if (player != null
                    && player.isVisible()
                    && bullet.collidesWith(player)) {
                bullet.die();
                bulletsToRemove.add(bullet);
                killPlayer("You were hit by the boss!");
                break;
            }

            if (!bullet.isVisible()) {
                bulletsToRemove.add(bullet);
            }
        }

        bossBullets.removeAll(bulletsToRemove);
    }

    private int randomEnemyY(int spriteHeight) {
        int topMargin = 70;
        int bottomMargin = 30;
        int maximumY = BOARD_HEIGHT - spriteHeight - bottomMargin;

        if (maximumY <= topMargin) {
            return Math.max(0, (BOARD_HEIGHT - spriteHeight) / 2);
        }

        return topMargin + randomizer.nextInt(maximumY - topMargin + 1);
    }

    private void checkPlayerCollisions() {
        if (!inGame || player == null || !player.isVisible()) {
            return;
        }

        for (Enemy enemy : enemies) {
            if (enemy.isVisible() && player.collidesWith(enemy)) {
                String collisionMessage = enemy instanceof MiniBoss
                        ? "You crashed into the mini boss!"
                        : "You crashed into an alien!";

                killPlayer(collisionMessage);
                return;
            }
        }

        if (boss != null
                && boss.isVisible()
                && player.collidesWith(boss)) {
            killPlayer("You crashed into the final boss!");
        }
    }

    private void killPlayer(String gameOverMessage) {
        if (!inGame || player == null) {
            return;
        }

        explosions.add(new Explosion(player.getX(), player.getY()));
        player.setDying(true);
        player.die();
        bossBullets.clear();
        finishGame(gameOverMessage);
    }

    private void updateShots() {
        List<Shot> shotsToRemove = new ArrayList<>();
        List<Enemy> enemiesToAdd = new ArrayList<>();

        for (Shot shot : shots) {
            if (!shot.isVisible()) {
                shotsToRemove.add(shot);
                continue;
            }

            if (handleBossCollision(shot)) {
                shotsToRemove.add(shot);
                continue;
            }

            if (handleEnemyCollision(shot, enemiesToAdd)) {
                shotsToRemove.add(shot);
                continue;
            }

            int nextX = shot.getX() + 20;

            if (nextX > BOARD_WIDTH) {
                shot.die();
                shotsToRemove.add(shot);
            } else {
                shot.setX(nextX);
            }
        }

        shots.removeAll(shotsToRemove);
        enemies.addAll(enemiesToAdd);
    }

    private boolean handleBossCollision(Shot shot) {
        if (boss == null
                || !boss.isVisible()
                || !shot.collidesWith(boss)) {
            return false;
        }

        shot.die();
        boss.takeDamage(1);

        explosions.add(
                new Explosion(
                        shot.getX(),
                        shot.getY()
                )
        );

        if (boss.isDying()) {
            explosions.add(
                    new Explosion(
                            boss.getX() + Boss.WIDTH / 2,
                            boss.getY() + Boss.HEIGHT / 2
                    )
            );

            boss.die();
            bossBullets.clear();
            finishGame("Final boss defeated!");
        }

        return true;
    }

    private boolean handleEnemyCollision(
            Shot shot,
            List<Enemy> enemiesToAdd
    ) {
        for (Enemy enemy : enemies) {
            if (!enemy.isVisible() || !shot.isVisible()) {
                continue;
            }

            int hitWidth = ALIEN_WIDTH;
            int hitHeight = ALIEN_HEIGHT;

            if (enemy instanceof MiniBoss) {
                MiniBoss miniBoss = (MiniBoss) enemy;
                hitWidth = miniBoss.getSpriteWidth();
                hitHeight = miniBoss.getSpriteHeight();
            }

            if (!pointInsideEnemy(
                    shot.getX(),
                    shot.getY(),
                    enemy,
                    hitWidth,
                    hitHeight
            )) {
                continue;
            }

            shot.die();

            explosions.add(
                    new Explosion(
                            enemy.getX(),
                            enemy.getY()
                    )
            );

            if (enemy instanceof MiniBoss) {
                MiniBoss miniBoss = (MiniBoss) enemy;
                boolean destroyed = miniBoss.hit();

                if (destroyed) {
                    destroyEnemy(enemy);
                    enemiesToAdd.addAll(
                            spawnAliensFromMiniBoss(
                                    enemy.getX(),
                                    enemy.getY()
                            )
                    );
                }
            } else {
                destroyEnemy(enemy);
            }

            return true;
        }

        return false;
    }

    private boolean pointInsideEnemy(
            int pointX,
            int pointY,
            Enemy enemy,
            int width,
            int height
    ) {
        return pointX >= enemy.getX()
                && pointX <= enemy.getX() + width
                && pointY >= enemy.getY()
                && pointY <= enemy.getY() + height;
    }

    private void destroyEnemy(Enemy enemy) {
        ImageIcon explosionIcon = new ImageIcon(IMG_EXPLOSION);

        enemy.setImage(explosionIcon.getImage());
        enemy.setDying(true);
        deaths++;
    }

    private List<Enemy> spawnAliensFromMiniBoss(int x, int y) {
        List<Enemy> spawnedEnemies = new ArrayList<>();
        int spacing = ALIEN_WIDTH + 10;

        spawnedEnemies.add(new Alien1(x - spacing, clampSplitY(y - spacing)));
        spawnedEnemies.add(new Alien1(x, clampSplitY(y)));
        spawnedEnemies.add(new Alien1(x + spacing, clampSplitY(y + spacing)));

        return spawnedEnemies;
    }

    private int clampSplitY(int requestedY) {
        int topMargin = 70;
        int bottomMargin = 30;
        int maximumY = BOARD_HEIGHT - ALIEN_HEIGHT - bottomMargin;

        return Math.max(topMargin, Math.min(requestedY, maximumY));
    }

    private void finishGame(String resultMessage) {
        if (!inGame) {
            return;
        }

        inGame = false;
        message = resultMessage;

        if (timer != null) {
            timer.stop();
        }
    }

    private void doGameCycle() {
        if (!inGame) {
            repaint();
            return;
        }

        frame++;
        update();
        repaint();
    }

    private void firePlayerWeapon() {
        if (!inGame || shots.size() >= MAX_PLAYER_SHOTS) {
            return;
        }

        int x = player.getX();
        int y = player.getY();

        switch (player.getWeaponLevel()) {
            case 1:
                shots.add(new Shot(x, y));
                break;

            case 2:
                shots.add(new Shot(x, y, -10));
                shots.add(new Shot(x, y, 10));
                break;

            case 3:
                shots.add(new Shot(x, y, -20));
                shots.add(new Shot(x, y));
                shots.add(new Shot(x, y, 20));
                break;

            case 4:
                shots.add(new Shot(x, y, -30));
                shots.add(new Shot(x, y, -10));
                shots.add(new Shot(x, y, 10));
                shots.add(new Shot(x, y, 30));
                break;

            default:
                shots.add(new Shot(x, y));
                break;
        }
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            doGameCycle();
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent event) {
            if (player != null) {
                player.keyReleased(event);
            }
        }

        @Override
        public void keyPressed(KeyEvent event) {
            if (player == null) {
                return;
            }

            player.keyPressed(event);

            if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                firePlayerWeapon();
            }
        }
    }
}