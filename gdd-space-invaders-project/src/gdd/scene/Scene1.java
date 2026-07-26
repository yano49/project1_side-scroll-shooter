package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.MiniBoss;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.Color;
import java.awt.Dimension;
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

    private int frame = 0;

    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;

    private Player player;

    /*
     * Background image variables
     */
    private Image backgroundImage;

    /*
     * Horizontal position of the first background image.
     */
    private int backgroundX = 0;

    /*
     * Background movement speed.
     *
     * 1 = slow
     * 2 = medium
     * 3 = fast
     */
    private static final int BACKGROUND_SCROLL_SPEED = 1;

    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;

    final int BLOCKS_TO_DRAW = BOARD_HEIGHT / BLOCKHEIGHT;

    private int direction = -1;
    private int deaths = 0;

    private boolean inGame = true;
    private String message = "Game Over";

    private final Dimension d =
            new Dimension(BOARD_WIDTH, BOARD_HEIGHT);

    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;

    private int currentRow = -1;
    private int mapOffset = 0;

    /*
     * The old MAP can remain in the class.
     *
     * It is no longer drawn because drawMap(g)
     * is not called inside doDrawing().
     */
    private final int[][] MAP = {
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},

        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}
    };

    private HashMap<Integer, SpawnDetails> spawnMap =
            new HashMap<>();

    private AudioPlayer audioPlayer;

    private int lastRowToShow;
    private int firstRowToShow;

    public Scene1(Game game) {

        this.game = game;

        loadSpawnDetails();
    }

    /*
     * Load the background image.
     */
    private void loadBackgroundImage() {

        ImageIcon backgroundIcon =
                new ImageIcon(IMG_BACKGROUND);

        backgroundImage = backgroundIcon.getImage();

        if (backgroundIcon.getIconWidth() <= 0
                || backgroundIcon.getIconHeight() <= 0) {

            System.err.println(
                    "Could not load background image: "
                    + IMG_BACKGROUND
            );
        }
    }

    private void initAudio() {

        try {

            String filePath =
                    "gdd-space-invaders-project/src/audio/scene1.wav";

            audioPlayer = new AudioPlayer(filePath);
            audioPlayer.play();

        } catch (Exception e) {

            System.err.println(
                    "Error initializing audio player: "
                    + e.getMessage()
            );
        }
    }

    private void loadSpawnDetails() {

        spawnMap.put(
                50,
                new SpawnDetails(
                        "PowerUp-SpeedUp",
                        100,
                        0
                )
        );

        spawnMap.put(
                200,
                new SpawnDetails(
                        "Alien1",
                        200,
                        0
                )
        );

        spawnMap.put(
                300,
                new SpawnDetails(
                        "Alien1",
                        300,
                        0
                )
        );

        spawnMap.put(
                400,
                new SpawnDetails(
                        "Alien1",
                        400,
                        0
                )
        );

        spawnMap.put(
                401,
                new SpawnDetails(
                        "Alien1",
                        450,
                        0
                )
        );

        spawnMap.put(
                402,
                new SpawnDetails(
                        "Alien1",
                        500,
                        0
                )
        );

        spawnMap.put(
                403,
                new SpawnDetails(
                        "Alien1",
                        550,
                        0
                )
        );

        spawnMap.put(
                500,
                new SpawnDetails(
                        "Alien1",
                        100,
                        0
                )
        );

        spawnMap.put(
                501,
                new SpawnDetails(
                        "Alien1",
                        150,
                        0
                )
        );

        spawnMap.put(
                502,
                new SpawnDetails(
                        "Alien1",
                        200,
                        0
                )
        );

        spawnMap.put(
                503,
                new SpawnDetails(
                        "Alien1",
                        350,
                        0
                )
        );

        spawnMap.put(
                600,
                new SpawnDetails(
                        "MiniBoss",
                        300,
                        0
                )
        );
    }

    private void initBoard() {

    }

    public void start() {

        addKeyListener(new TAdapter());

        setFocusable(true);
        requestFocusInWindow();

        setBackground(Color.black);

        /*
         * Load the city background before the game starts.
         */
        loadBackgroundImage();

        /*
         * Reset background position each time Scene1 starts.
         */
        backgroundX = 0;

        timer = new Timer(
                1000 / 60,
                new GameCycle()
        );

        timer.start();

        gameInit();
        initAudio();
    }

    public void stop() {

        if (timer != null) {
            timer.stop();
        }

        try {

            if (audioPlayer != null) {
                audioPlayer.stop();
            }

        } catch (Exception e) {

            System.err.println(
                    "Error closing audio player."
            );
        }
    }

    private void gameInit() {

        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();

        player = new Player();
    }

    /*
     * Draw two copies of the background.
     *
     * The first image moves to the left.
     * The second image follows immediately behind it.
     *
     * This creates a continuous scrolling effect.
     */
    private void drawScrollingBackground(Graphics g) {

        if (backgroundImage == null) {

            g.setColor(Color.BLACK);

            g.fillRect(
                    0,
                    0,
                    BOARD_WIDTH,
                    BOARD_HEIGHT
            );

            return;
        }

        /*
         * First background copy.
         */
        g.drawImage(
                backgroundImage,
                backgroundX,
                0,
                BOARD_WIDTH,
                BOARD_HEIGHT,
                this
        );

        /*
         * Second background copy.
         *
         * It is placed immediately after the first image.
         */
        g.drawImage(
                backgroundImage,
                backgroundX + BOARD_WIDTH,
                0,
                BOARD_WIDTH,
                BOARD_HEIGHT,
                this
        );
    }

    /*
     * Old star-map drawing method.
     *
     * This method is kept so the existing project structure
     * is not heavily changed, but it is no longer called.
     */
    private void drawMap(Graphics g) {

        int scrollOffset = frame % BLOCKHEIGHT;

        int baseRow = frame / BLOCKHEIGHT;

        int rowsNeeded =
                (BOARD_HEIGHT / BLOCKHEIGHT) + 2;

        for (
                int screenRow = 0;
                screenRow < rowsNeeded;
                screenRow++
        ) {

            int mapRow =
                    (baseRow + screenRow) % MAP.length;

            int y =
                    BOARD_HEIGHT
                    - (
                        (screenRow * BLOCKHEIGHT)
                        - scrollOffset
                    );

            if (
                    y > BOARD_HEIGHT
                    || y < -BLOCKHEIGHT
            ) {
                continue;
            }

            for (
                    int col = 0;
                    col < MAP[mapRow].length;
                    col++
            ) {

                if (MAP[mapRow][col] == 1) {

                    int x = col * BLOCKWIDTH;

                    drawStarCluster(
                            g,
                            x,
                            y,
                            BLOCKWIDTH,
                            BLOCKHEIGHT
                    );
                }
            }
        }
    }

    private void drawStarCluster(
            Graphics g,
            int x,
            int y,
            int width,
            int height
    ) {

        g.setColor(Color.WHITE);

        int centerX =
                x + width / 2;

        int centerY =
                y + height / 2;

        g.fillOval(
                centerX - 2,
                centerY - 2,
                4,
                4
        );

        g.fillOval(
                centerX - 15,
                centerY - 10,
                2,
                2
        );

        g.fillOval(
                centerX + 12,
                centerY - 8,
                2,
                2
        );

        g.fillOval(
                centerX - 8,
                centerY + 12,
                2,
                2
        );

        g.fillOval(
                centerX + 10,
                centerY + 15,
                2,
                2
        );

        g.fillOval(
                centerX - 20,
                centerY + 5,
                1,
                1
        );

        g.fillOval(
                centerX + 18,
                centerY - 15,
                1,
                1
        );

        g.fillOval(
                centerX - 5,
                centerY - 18,
                1,
                1
        );

        g.fillOval(
                centerX + 8,
                centerY + 20,
                1,
                1
        );
    }

    private void drawAliens(Graphics g) {

        for (Enemy enemy : enemies) {

            if (enemy.isVisible()) {

                g.drawImage(
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

    private void drawPowreUps(Graphics g) {

        for (PowerUp powerUp : powerups) {

            if (powerUp.isVisible()) {

                g.drawImage(
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

    private void drawPlayer(Graphics g) {

        if (player.isVisible()) {

            g.drawImage(
                    player.getImage(),
                    player.getX(),
                    player.getY(),
                    this
            );
        }

        if (player.isDying()) {

            player.die();
            inGame = false;
        }
    }

    private void drawShot(Graphics g) {

        for (Shot shot : shots) {

            if (shot.isVisible()) {

                g.drawImage(
                        shot.getImage(),
                        shot.getX(),
                        shot.getY(),
                        this
                );
            }
        }
    }

    private void drawBombing(Graphics g) {

        /*
         * Enemy bombing is currently disabled
         * in the original project.
         */
    }

    private void drawExplosions(Graphics g) {

        List<Explosion> toRemove =
                new ArrayList<>();

        for (Explosion explosion : explosions) {

            if (explosion.isVisible()) {

                g.drawImage(
                        explosion.getImage(),
                        explosion.getX(),
                        explosion.getY(),
                        this
                );

                explosion.visibleCountDown();

                if (!explosion.isVisible()) {
                    toRemove.add(explosion);
                }
            }
        }

        explosions.removeAll(toRemove);
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        /*
         * Draw the background first.
         *
         * The player, enemies, shots and power-ups are
         * drawn afterward so they appear above it.
         */
        drawScrollingBackground(g);

        if (inGame) {

            drawExplosions(g);
            drawPowreUps(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);

            /*
             * Draw frame information last so it remains visible.
             */
            g.setColor(Color.WHITE);

            g.drawString(
                    "FRAME: " + frame,
                    10,
                    20
            );

        } else {

            if (
                    timer != null
                    && timer.isRunning()
            ) {
                timer.stop();
            }

            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void gameOver(Graphics g) {

        /*
         * Keep the scrolling city visible behind
         * the game-over message.
         */
        drawScrollingBackground(g);

        int boxX = 50;
        int boxY = BOARD_HEIGHT / 2 - 30;
        int boxWidth = BOARD_WIDTH - 100;
        int boxHeight = 50;

        g.setColor(
                new Color(
                        0,
                        32,
                        48,
                        220
                )
        );

        g.fillRect(
                boxX,
                boxY,
                boxWidth,
                boxHeight
        );

        g.setColor(Color.WHITE);

        g.drawRect(
                boxX,
                boxY,
                boxWidth,
                boxHeight
        );

        Font small =
                new Font(
                        "Helvetica",
                        Font.BOLD,
                        14
                );

        var fontMetrics =
                this.getFontMetrics(small);

        g.setFont(small);

        g.drawString(
                message,
                (
                    BOARD_WIDTH
                    - fontMetrics.stringWidth(message)
                ) / 2,
                BOARD_HEIGHT / 2
        );
    }

    private void update() {

        /*
         * Move the background from right to left.
         */
        backgroundX -= BACKGROUND_SCROLL_SPEED;

        /*
         * Once the first image completely leaves the screen,
         * reset it to its original position.
         */
        if (backgroundX <= -BOARD_WIDTH) {
            backgroundX = 0;
        }

        /*
         * Check whether something must spawn
         * during the current frame.
         */
        SpawnDetails spawnDetails =
                spawnMap.get(frame);

        if (spawnDetails != null) {

            switch (spawnDetails.type) {

                case "Alien1":

                    Enemy enemy =
                            new Alien1(
                                    spawnDetails.x,
                                    spawnDetails.y
                            );

                    enemies.add(enemy);

                    break;

                case "Alien2":

                    /*
                     * Alien2 can be added here later.
                     */
                    break;

                case "MiniBoss":

                    System.out.println(
                            "Spawning MiniBoss at frame "
                            + frame
                            + " (x="
                            + spawnDetails.x
                            + ", y="
                            + spawnDetails.y
                            + ")"
                    );

                    Enemy miniBoss =
                            new MiniBoss(
                                    spawnDetails.x,
                                    spawnDetails.y
                            );

                    enemies.add(miniBoss);

                    break;

                case "PowerUp-SpeedUp":

                    PowerUp speedUp =
                            new SpeedUp(
                                    spawnDetails.x,
                                    spawnDetails.y
                            );

                    powerups.add(speedUp);

                    break;

                default:

                    System.out.println(
                            "Unknown enemy type: "
                            + spawnDetails.type
                    );

                    break;
            }
        }

        if (
                deaths
                == NUMBER_OF_ALIENS_TO_DESTROY
        ) {

            inGame = false;

            timer.stop();

            message = "Game won!";
        }

        /*
         * Update player movement.
         */
        player.act();

        /*
         * Update power-ups.
         */
        for (PowerUp powerUp : powerups) {

            if (powerUp.isVisible()) {

                powerUp.act();

                if (
                        powerUp.collidesWith(player)
                ) {
                    powerUp.upgrade(player);
                }
            }
        }

        /*
         * Update enemies.
         */
        for (Enemy enemy : enemies) {

            if (enemy.isVisible()) {
                enemy.act(direction);
            }
        }

        /*
         * Shot update and collision checking.
         */
        List<Shot> shotsToRemove =
                new ArrayList<>();

        List<Enemy> enemiesToAdd =
                new ArrayList<>();

        for (Shot shot : shots) {

            if (shot.isVisible()) {

                int shotX = shot.getX();
                int shotY = shot.getY();

                for (Enemy enemy : enemies) {

                    int enemyX = enemy.getX();
                    int enemyY = enemy.getY();

                    int hitWidth = ALIEN_WIDTH;
                    int hitHeight = ALIEN_HEIGHT;

                    if (enemy instanceof MiniBoss) {

                        MiniBoss miniBoss =
                                (MiniBoss) enemy;

                        hitWidth =
                                miniBoss.getSpriteWidth();

                        hitHeight =
                                miniBoss.getSpriteHeight();
                    }

                    boolean shotHitEnemy =
                            enemy.isVisible()
                            && shot.isVisible()
                            && shotX >= enemyX
                            && shotX <= enemyX + hitWidth
                            && shotY >= enemyY
                            && shotY <= enemyY + hitHeight;

                    if (shotHitEnemy) {

                        shot.die();
                        shotsToRemove.add(shot);

                        if (
                                enemy
                                instanceof MiniBoss
                        ) {

                            MiniBoss miniBoss =
                                    (MiniBoss) enemy;

                            boolean destroyed =
                                    miniBoss.hit();

                            explosions.add(
                                    new Explosion(
                                            enemyX,
                                            enemyY
                                    )
                            );

                            if (destroyed) {

                                ImageIcon explosionIcon =
                                        new ImageIcon(
                                                IMG_EXPLOSION
                                        );

                                enemy.setImage(
                                        explosionIcon.getImage()
                                );

                                enemy.setDying(true);

                                deaths++;

                                enemiesToAdd.addAll(
                                        spawnAliensFromMiniBoss(
                                                enemyX,
                                                enemyY
                                        )
                                );
                            }

                        } else {

                            ImageIcon explosionIcon =
                                    new ImageIcon(
                                            IMG_EXPLOSION
                                    );

                            enemy.setImage(
                                    explosionIcon.getImage()
                            );

                            enemy.setDying(true);

                            explosions.add(
                                    new Explosion(
                                            enemyX,
                                            enemyY
                                    )
                            );

                            deaths++;
                        }
                    }
                }

                int x = shot.getX();

                x += 20;

                if (x > BOARD_WIDTH) {

                    shot.die();

                    shotsToRemove.add(shot);

                } else {

                    shot.setX(x);
                }
            }
        }

        shots.removeAll(shotsToRemove);
        enemies.addAll(enemiesToAdd);
    }

    /*
     * Called when a MiniBoss is destroyed.
     *
     * It splits into three regular aliens.
     */
    private List<Enemy> spawnAliensFromMiniBoss(
            int x,
            int y
    ) {

        List<Enemy> spawned =
                new ArrayList<>();

        int spacing =
                ALIEN_WIDTH + 10;

        spawned.add(
                new Alien1(
                        x - spacing,
                        y
                )
        );

        spawned.add(
                new Alien1(
                        x,
                        y
                )
        );

        spawned.add(
                new Alien1(
                        x + spacing,
                        y
                )
        );

        return spawned;
    }

    private void doGameCycle() {

        frame++;

        update();

        repaint();
    }

    private class GameCycle
            implements ActionListener {

        @Override
        public void actionPerformed(
                ActionEvent event
        ) {

            doGameCycle();
        }
    }

    private class TAdapter
            extends KeyAdapter {

        @Override
        public void keyReleased(
                KeyEvent event
        ) {

            player.keyReleased(event);
        }

        @Override
        public void keyPressed(
                KeyEvent event
        ) {

            System.out.println(
                    "Scene1.keyPressed: "
                    + event.getKeyCode()
            );

            player.keyPressed(event);

            int x = player.getX();
            int y = player.getY();

            int key =
                    event.getKeyCode();

            if (
                    key == KeyEvent.VK_SPACE
                    && inGame
            ) {

                if (shots.size() < 8) {

                    switch (
                            player.getWeaponLevel()
                    ) {

                        case 1:

                            shots.add(
                                    new Shot(
                                            x,
                                            y
                                    )
                            );

                            break;

                        case 2:

                            shots.add(
                                    new Shot(
                                            x,
                                            y,
                                            -10
                                    )
                            );

                            shots.add(
                                    new Shot(
                                            x,
                                            y,
                                            10
                                    )
                            );

                            break;

                        case 3:

                            shots.add(
                                    new Shot(
                                            x,
                                            y,
                                            -20
                                    )
                            );

                            shots.add(
                                    new Shot(
                                            x,
                                            y
                                    )
                            );

                            shots.add(
                                    new Shot(
                                            x,
                                            y,
                                            20
                                    )
                            );

                            break;

                        case 4:

                            shots.add(
                                    new Shot(
                                            x,
                                            y,
                                            -30
                                    )
                            );

                            shots.add(
                                    new Shot(
                                            x,
                                            y,
                                            -10
                                    )
                            );

                            shots.add(
                                    new Shot(
                                            x,
                                            y,
                                            10
                                    )
                            );

                            shots.add(
                                    new Shot(
                                            x,
                                            y,
                                            30
                                    )
                            );

                            break;

                        default:

                            shots.add(
                                    new Shot(
                                            x,
                                            y
                                    )
                            );

                            break;
                    }
                }
            }
        }
    }
}