package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.SuperPowerUp;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {

    private static final int BACKGROUND_SCROLL_SPEED = 1;
    // Final boss is temporarily disabled in Scene 1.
    private static final boolean FINAL_BOSS_ENABLED = false;
    private static final int BOSS_SPAWN_FRAME = Integer.MAX_VALUE;

    // Spawn after 5 seconds for easy testing.
    private static final long SUPER_POWER_FIRST_SPAWN_SECONDS = 5L;
    private static final long SUPER_POWER_SPAWN_INTERVAL_MS = 30_000L;
    private static final int MAX_PLAYER_SHOTS = 24;

    private static final int NORMAL_ALIEN_SCORE = 2;
    private static final int MINI_BOSS_SCORE = 5;

    private static final long LASER_ACTIVE_DURATION_MS = 5_000L;
    private static final long LASER_COOLDOWN_MS = 10_000L;
    private static final long LASER_FIRE_INTERVAL_MS = 140L;

    private static final int[] MAGAZINE_SIZE_BY_LEVEL = {
        0, 4, 5, 6, 10
    };

    private static final long[] RELOAD_DURATION_MS_BY_LEVEL = {
        0L, 2_000L, 2_000L, 2_000L, 4_000L
    };

    private static final Path BEST_SCORE_FILE = Paths.get(
            System.getProperty("user.home"),
            ".gdd-space-invaders-best-score.txt"
    );
    private static final int ENEMY_SPAWN_X = BOARD_WIDTH + 20;

    // Continue spawning after the original scripted opening wave.
    private static final long CONTINUOUS_ALIEN_START_SECONDS = 10L;
    private static final long BASE_ALIEN_SPAWN_INTERVAL_MS = 2_000L;
    private static final long MIN_ALIEN_SPAWN_INTERVAL_MS = 700L;
    private static final int MAX_ALIENS_PER_WAVE = 5;
    private static final long FIRST_MINIBOSS_SECONDS = 45L;
    private static final long REPEATING_MINIBOSS_START_SECONDS = 90L;
    private static final long MINIBOSS_INTERVAL_MS = 10_000L;
    private static final int MAX_MINIBOSSES_TO_SPAWN = 1;

    private final Game game;
    private final HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private final Random randomizer = new Random();

    private int frame;
    private int direction;
    private int deaths;
    private int backgroundX;
    private int score;
    private int bestScore;

    private boolean laserActive;
    private long laserActiveUntil;
    private long laserCooldownUntil;
    private long lastLaserFireTime;

    private int ammoRemaining;
    private int ammoWeaponLevel;
    private boolean reloading;
    private long reloadCompleteTime;
    private boolean powerUpAmmoModeWasActive;

    private boolean inGame;
    private boolean bossSpawned;
    private boolean firstSuperPowerUpSpawned;
    private long lastSuperPowerUpSpawnTime;
    private long sceneStartTime;
    private long lastContinuousAlienSpawnTime;
    private long lastMiniBossSpawnTime;
    private int miniBossSpawnCount;
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
        firstSuperPowerUpSpawned = false;

        sceneStartTime = System.currentTimeMillis();
        lastSuperPowerUpSpawnTime = sceneStartTime;
        lastContinuousAlienSpawnTime = sceneStartTime;
        lastMiniBossSpawnTime = sceneStartTime;
        miniBossSpawnCount = 0;

        score = 0;
        bestScore = loadBestScore();
        laserActive = false;
        laserActiveUntil = 0L;
        laserCooldownUntil = 0L;
        lastLaserFireTime = 0L;

        ammoWeaponLevel = 1;
        ammoRemaining = getMagazineSizeForLevel(ammoWeaponLevel);
        reloading = false;
        reloadCompleteTime = 0L;
        powerUpAmmoModeWasActive = false;

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
            if (FINAL_BOSS_ENABLED) {
                drawBoss(graphics);
                drawBossBullets(graphics);
            }
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
        graphics.setFont(new Font("Helvetica", Font.BOLD, 14));

        graphics.setColor(Color.WHITE);
        graphics.drawString("SCORE: " + score, 10, 20);
        graphics.drawString("BEST: " + bestScore, 10, 40);

        if (player != null) {
            graphics.drawString(
                    "WEAPON LVL: " + player.getWeaponLevel(),
                    10,
                    60
            );

            graphics.drawString(
                    "SPEED: " + player.getSpeed(),
                    10,
                    80
            );
        }

        int nextLineY = 100;

        if (player != null && player.isSuperPowered()) {
            long remainingSeconds =
                    (player.getSuperPowerRemainingMillis() + 999L) / 1000L;

            graphics.setColor(Color.ORANGE);
            graphics.drawString(
                    "SUPER POWER: " + remainingSeconds + "s",
                    10,
                    nextLineY
            );
            nextLineY += 20;
        }

        if (player != null && player.getWeaponLevel() >= 4) {
            long now = System.currentTimeMillis();

            if (laserActive) {
                long remaining =
                        Math.max(0L, laserActiveUntil - now);
                graphics.setColor(Color.CYAN);
                graphics.drawString(
                        "LASER ACTIVE: "
                                + ((remaining + 999L) / 1000L)
                                + "s",
                        10,
                        nextLineY
                );
            } else if (now < laserCooldownUntil) {
                long remaining =
                        Math.max(0L, laserCooldownUntil - now);
                graphics.setColor(Color.LIGHT_GRAY);
                graphics.drawString(
                        "F LASER COOLDOWN: "
                                + ((remaining + 999L) / 1000L)
                                + "s",
                        10,
                        nextLineY
                );
            } else {
                graphics.setColor(Color.CYAN);
                graphics.drawString(
                        "F LASER: READY",
                        10,
                        nextLineY
                );
            }
        }

        drawAmmoStatus(graphics);
    }

    private void drawAmmoStatus(Graphics graphics) {
        if (player == null) {
            return;
        }

        int rightMargin = 14;
        int y = 20;

        graphics.setFont(new Font("Helvetica", Font.BOLD, 14));

        String levelText =
                "AMMO LVL " + player.getWeaponLevel();

        int levelX =
                BOARD_WIDTH
                        - graphics.getFontMetrics().stringWidth(levelText)
                        - rightMargin;

        graphics.setColor(Color.WHITE);
        graphics.drawString(levelText, levelX, y);

        if (player.isSuperPowered()) {
            String unlimitedText = "AMMO: UNLIMITED";

            int unlimitedX =
                    BOARD_WIDTH
                            - graphics.getFontMetrics().stringWidth(
                                    unlimitedText
                            )
                            - rightMargin;

            graphics.setColor(Color.ORANGE);
            graphics.drawString(
                    unlimitedText,
                    unlimitedX,
                    y + 20
            );
            return;
        }

        if (reloading) {
            long remaining =
                    Math.max(
                            0L,
                            reloadCompleteTime
                                    - System.currentTimeMillis()
                    );

            String reloadText =
                    "RELOADING: "
                            + ((remaining + 999L) / 1000L)
                            + "s";

            int reloadX =
                    BOARD_WIDTH
                            - graphics.getFontMetrics().stringWidth(
                                    reloadText
                            )
                            - rightMargin;

            graphics.setColor(Color.ORANGE);
            graphics.drawString(reloadText, reloadX, y + 20);
        } else {
            int magazineSize =
                    getMagazineSizeForLevel(
                            player.getWeaponLevel()
                    );

            String ammoText =
                    "AMMO: "
                            + ammoRemaining
                            + "/"
                            + magazineSize;

            int ammoX =
                    BOARD_WIDTH
                            - graphics.getFontMetrics().stringWidth(
                                    ammoText
                            )
                            - rightMargin;

            graphics.setColor(Color.WHITE);
            graphics.drawString(ammoText, ammoX, y + 20);
        }
    }

    private void updateAmmoSystem() {
        if (player == null) {
            return;
        }

        /*
         * While the orange power-up is active:
         * - normal shooting is unlimited,
         * - reloading is cancelled temporarily,
         * - the ammo display shows UNLIMITED.
         */
        if (player.isSuperPowered()) {
            powerUpAmmoModeWasActive = true;
            reloading = false;
            reloadCompleteTime = 0L;
            return;
        }

        int currentWeaponLevel =
                Math.max(
                        1,
                        Math.min(4, player.getWeaponLevel())
                );

        /*
         * When power-up finishes, restore a full magazine using the
         * player's permanent weapon level.
         */
        if (powerUpAmmoModeWasActive) {
            powerUpAmmoModeWasActive = false;
            ammoWeaponLevel = currentWeaponLevel;
            ammoRemaining =
                    getMagazineSizeForLevel(ammoWeaponLevel);
            reloading = false;
            reloadCompleteTime = 0L;
            return;
        }

        /*
         * Refill when the player permanently reaches a new level.
         */
        if (currentWeaponLevel != ammoWeaponLevel) {
            ammoWeaponLevel = currentWeaponLevel;
            ammoRemaining =
                    getMagazineSizeForLevel(ammoWeaponLevel);
            reloading = false;
            reloadCompleteTime = 0L;
        }

        if (reloading
                && System.currentTimeMillis()
                >= reloadCompleteTime) {

            reloading = false;
            ammoWeaponLevel = currentWeaponLevel;
            ammoRemaining =
                    getMagazineSizeForLevel(ammoWeaponLevel);
            reloadCompleteTime = 0L;
        }
    }

    private int getMagazineSizeForLevel(int level) {
        int safeLevel = Math.max(1, Math.min(4, level));
        return MAGAZINE_SIZE_BY_LEVEL[safeLevel];
    }

    private long getReloadDurationForLevel(int level) {
        int safeLevel = Math.max(1, Math.min(4, level));
        return RELOAD_DURATION_MS_BY_LEVEL[safeLevel];
    }

    private void beginReload() {
        if (player == null || reloading) {
            return;
        }

        int level =
                Math.max(
                        1,
                        Math.min(4, player.getWeaponLevel())
                );

        reloading = true;
        ammoRemaining = 0;
        reloadCompleteTime =
                System.currentTimeMillis()
                        + getReloadDurationForLevel(level);
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
        updateContinuousSpawning();
        trySpawnSuperPowerUp();
        if (FINAL_BOSS_ENABLED) {
            spawnBoss();
        }
        updatePlayer();
        updateAmmoSystem();
        updateLaserAbility();
        updatePowerUps();
        updateEnemies();
        if (FINAL_BOSS_ENABLED) {
            updateBoss();
            updateBossBullets();
        }
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

    private void updateContinuousSpawning() {
        long now = System.currentTimeMillis();
        long elapsedSeconds = getElapsedSeconds();

        /*
         * Alien difficulty increases every 30 seconds:
         * faster waves, more aliens, and more zigzag movement.
         */
        int alienDifficultyTier = (int) (elapsedSeconds / 30L);
        long alienSpawnInterval =
                getAlienSpawnInterval(alienDifficultyTier);

        if (elapsedSeconds >= CONTINUOUS_ALIEN_START_SECONDS
                && now - lastContinuousAlienSpawnTime
                >= alienSpawnInterval) {

            spawnAdaptiveAlienWave(alienDifficultyTier);
            lastContinuousAlienSpawnTime = now;
        }

        /*
         * First miniboss at 45 seconds.
         */
        if (miniBossSpawnCount == 0
                && elapsedSeconds >= FIRST_MINIBOSS_SECONDS) {
            spawnTimedMiniBoss(now);
        }

        /*
         * From 90 seconds onward, spawn one every 10 seconds.
         */
        if (elapsedSeconds >= REPEATING_MINIBOSS_START_SECONDS
                && miniBossSpawnCount > 0
                && miniBossSpawnCount < MAX_MINIBOSSES_TO_SPAWN
                && now - lastMiniBossSpawnTime >= MINIBOSS_INTERVAL_MS) {
            spawnTimedMiniBoss(now);
        }
    }

    private long getAlienSpawnInterval(int difficultyTier) {
        long reduction = difficultyTier * 220L;

        return Math.max(
                MIN_ALIEN_SPAWN_INTERVAL_MS,
                BASE_ALIEN_SPAWN_INTERVAL_MS - reduction
        );
    }

    private void spawnAdaptiveAlienWave(int difficultyTier) {
        int waveSize = Math.min(
                MAX_ALIENS_PER_WAVE,
                1 + difficultyTier
        );

        int straightSpeed = Math.min(
                5,
                2 + difficultyTier / 2
        );

        for (int index = 0; index < waveSize; index++) {
            int spawnX =
                    ENEMY_SPAWN_X + index * (ALIEN_WIDTH + 18);
            int spawnY = randomEnemyY(ALIEN_HEIGHT);

            Alien1.MovementType movementType =
                    selectAlienMovementType(difficultyTier);

            int selectedSpeed =
                    movementType == Alien1.MovementType.SLOW_TRACKING
                            ? 1
                            : straightSpeed;

            enemies.add(
                    new Alien1(
                            spawnX,
                            spawnY,
                            movementType,
                            selectedSpeed
                    )
            );
        }
    }

    private Alien1.MovementType selectAlienMovementType(
            int difficultyTier
    ) {
        int randomValue = randomizer.nextInt(100);

        /*
         * At 100 score, some aliens slowly follow the player's height.
         */
        if (score >= 100 && randomValue < 25) {
            return Alien1.MovementType.SLOW_TRACKING;
        }

        /*
         * Zigzag aliens begin after 30 seconds and become more common
         * every 30 seconds.
         */
        int zigzagChance = Math.min(
                70,
                difficultyTier * 15
        );

        if (difficultyTier >= 1
                && randomValue < 25 + zigzagChance) {
            return Alien1.MovementType.ZIGZAG;
        }

        return Alien1.MovementType.STRAIGHT;
    }

    private void spawnTimedMiniBoss(long spawnTime) {
        enemies.add(
                new MiniBoss(
                        ENEMY_SPAWN_X,
                        randomEnemyY(ALIEN_HEIGHT * 3)
                )
        );

        miniBossSpawnCount++;
        lastMiniBossSpawnTime = spawnTime;

        System.out.println(
                "MiniBoss spawned: "
                        + miniBossSpawnCount
                        + "/"
                        + MAX_MINIBOSSES_TO_SPAWN
        );
    }

    private void trySpawnSuperPowerUp() {
        long now = System.currentTimeMillis();
        long elapsedSeconds = getElapsedSeconds();

        /*
         * First power-up appears after 5 seconds.
         */
        if (!firstSuperPowerUpSpawned
                && elapsedSeconds >= SUPER_POWER_FIRST_SPAWN_SECONDS) {

            spawnSuperPowerUp(now);
            firstSuperPowerUpSpawned = true;
            return;
        }

        /*
         * After the first one, spawn another every 30 seconds.
         */
        if (firstSuperPowerUpSpawned
                && now - lastSuperPowerUpSpawnTime
                >= SUPER_POWER_SPAWN_INTERVAL_MS) {

            spawnSuperPowerUp(now);
        }
    }

    private void spawnSuperPowerUp(long spawnTime) {
        int spawnX = ENEMY_SPAWN_X;
        int spawnY = powerUpYNearPlayer();

        powerups.add(
                new SuperPowerUp(
                        spawnX,
                        spawnY
                )
        );

        lastSuperPowerUpSpawnTime = spawnTime;

        System.out.println(
                "Super power-up spawned at x="
                        + spawnX
                        + ", y="
                        + spawnY
        );
    }

    private int powerUpYNearPlayer() {
        int topMargin = 70;
        int bottomMargin = 30;
        int maximumY =
                BOARD_HEIGHT - SuperPowerUp.DRAW_HEIGHT - bottomMargin;

        if (player == null) {
            return randomEnemyY(SuperPowerUp.DRAW_HEIGHT);
        }

        int variation = randomizer.nextInt(121) - 60;
        int requestedY = player.getY() + variation;

        return Math.max(
                topMargin,
                Math.min(requestedY, maximumY)
        );
    }

    private long getElapsedSeconds() {
        return Math.max(
                0L,
                (System.currentTimeMillis() - sceneStartTime) / 1000L
        );
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
        boolean fastMiniBossMode = getElapsedSeconds() >= 150;

        for (Enemy enemy : enemies) {
            if (!enemy.isVisible()) {
                enemiesToRemove.add(enemy);
                continue;
            }

            if (enemy instanceof MiniBoss) {
                MiniBoss miniBoss = (MiniBoss) enemy;
                miniBoss.act(player, fastMiniBossMode);
            } else if (enemy instanceof Alien1) {
                Alien1 alien = (Alien1) enemy;
                alien.act(player);
            } else {
                enemy.act(direction);

                int enemyWidth = enemy.getImage() == null
                        ? ALIEN_WIDTH
                        : enemy.getImage().getWidth(null);

                if (enemy.getX() + enemyWidth < 0) {
                    enemy.die();
                    enemiesToRemove.add(enemy);
                }
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

                if (!player.isInvincible()) {
                    killPlayer("You were hit by the boss!");
                    break;
                }
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
            if (!enemy.isVisible() || !player.collidesWith(enemy)) {
                continue;
            }

            if (player.canCrashEnemies() && !(enemy instanceof MiniBoss)) {
                explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                destroyEnemy(enemy);
                addScore(NORMAL_ALIEN_SCORE);
                continue;
            }

            if (player.isInvincible()) {
                continue;
            }

            String collisionMessage = enemy instanceof MiniBoss
                    ? "You crashed into the mini boss!"
                    : "You crashed into an alien!";

            killPlayer(collisionMessage);
            return;
        }

        if (FINAL_BOSS_ENABLED
                && boss != null
                && boss.isVisible()
                && player.collidesWith(boss)
                && !player.isInvincible()) {
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

            if (FINAL_BOSS_ENABLED && handleBossCollision(shot)) {
                shotsToRemove.add(shot);
                continue;
            }

            if (handleEnemyCollision(shot, enemiesToAdd)) {
                shotsToRemove.add(shot);
                continue;
            }

            shot.act();

            if (!shot.isVisible()) {
                shotsToRemove.add(shot);
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

            if (!shotOverlapsEnemy(
                    shot,
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
                    addScore(MINI_BOSS_SCORE);

                    if (miniBossSpawnCount >= MAX_MINIBOSSES_TO_SPAWN) {
                        game.loadSceneTransition();
                    } else {
                        enemiesToAdd.addAll(
                                spawnAliensFromMiniBoss(
                                        enemy.getX(),
                                        enemy.getY()
                                )
                        );
                    }
                }
            } else {
                destroyEnemy(enemy);
                addScore(NORMAL_ALIEN_SCORE);
            }

            return true;
        }

        return false;
    }

    private boolean shotOverlapsEnemy(
            Shot shot,
            Enemy enemy,
            int enemyWidth,
            int enemyHeight
    ) {
        int shotWidth = Math.max(1, shot.getHitWidth());
        int shotHeight = Math.max(1, shot.getHitHeight());

        return shot.getX() < enemy.getX() + enemyWidth
                && shot.getX() + shotWidth > enemy.getX()
                && shot.getY() < enemy.getY() + enemyHeight
                && shot.getY() + shotHeight > enemy.getY();
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
        if (!inGame
                || player == null
                || shots.size() >= MAX_PLAYER_SHOTS) {
            return;
        }

        boolean unlimitedAmmo = player.isSuperPowered();

        if (!unlimitedAmmo && reloading) {
            return;
        }

        if (!unlimitedAmmo && ammoRemaining <= 0) {
            beginReload();
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
            case 4:
                shots.add(new Shot(x, y, -12, 20, -6));
                shots.add(new Shot(x, y, 0, 20, 0));
                shots.add(new Shot(x, y, 12, 20, 6));
                break;

            default:
                shots.add(new Shot(x, y));
                break;
        }

        /*
         * One press consumes one ammo normally. During orange power-up,
         * shooting is unlimited and does not start a reload.
         */
        if (!unlimitedAmmo) {
            ammoRemaining--;

            if (ammoRemaining <= 0) {
                beginReload();
            }
        }
    }

    private void activateLaserAbility() {
        if (!inGame
                || player == null
                || player.getWeaponLevel() < 4) {
            return;
        }

        long now = System.currentTimeMillis();

        if (laserActive || now < laserCooldownUntil) {
            return;
        }

        laserActive = true;
        laserActiveUntil = now + LASER_ACTIVE_DURATION_MS;
        laserCooldownUntil = now + LASER_COOLDOWN_MS;
        lastLaserFireTime = 0L;
    }

    private void updateLaserAbility() {
        if (!laserActive || player == null || !player.isVisible()) {
            return;
        }

        long now = System.currentTimeMillis();

        if (now >= laserActiveUntil) {
            laserActive = false;
            return;
        }

        if (now - lastLaserFireTime < LASER_FIRE_INTERVAL_MS) {
            return;
        }

        if (shots.size() + 3 > MAX_PLAYER_SHOTS) {
            return;
        }

        int x = player.getX();
        int y = player.getY();

        shots.add(Shot.createLaser(x, y, -12, -4));
        shots.add(Shot.createLaser(x, y, 0, 0));
        shots.add(Shot.createLaser(x, y, 12, 4));

        lastLaserFireTime = now;
    }

    private void addScore(int points) {
        if (points <= 0) {
            return;
        }

        score += points;

        if (player != null) {
            player.applyScoreProgression(score);
        }

        if (score > bestScore) {
            bestScore = score;
            saveBestScore();
        }
    }

    private int loadBestScore() {
        try {
            if (!Files.exists(BEST_SCORE_FILE)) {
                return 0;
            }

            String value = new String(
                    Files.readAllBytes(BEST_SCORE_FILE),
                    StandardCharsets.UTF_8
            ).trim();

            return Math.max(0, Integer.parseInt(value));

        } catch (Exception exception) {
            System.err.println(
                    "Could not load best score: "
                            + exception.getMessage()
            );
            return 0;
        }
    }

    private void saveBestScore() {
        try {
            Files.write(
                    BEST_SCORE_FILE,
                    String.valueOf(bestScore).getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        } catch (Exception exception) {
            System.err.println(
                    "Could not save best score: "
                            + exception.getMessage()
            );
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

            if (event.getKeyCode() == KeyEvent.VK_F) {
                activateLaserAbility();
            }
        }
    }
}