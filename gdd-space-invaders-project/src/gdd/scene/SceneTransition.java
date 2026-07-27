package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.IMG_SCENE2_BACKGROUND;
import gdd.sprite.Player;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Short bridge between the side-scrolling stage and the vertical boss stage.
 * Scene 1 only needs to call Game.loadSceneTransition() when 30 minibosses
 * have been defeated.
 */
public class SceneTransition extends JPanel implements ActionListener {

    private static final int DURATION_FRAMES = 180;
    private static final String TRANSITION_AUDIO =
            "gdd-space-invaders-project/src/audio/transition.wav";

    private final Game game;
    private final Player player;
    private final VerticalPlayerAnimation verticalPlane =
            new VerticalPlayerAnimation();
    private final Image background =
            new ImageIcon(IMG_SCENE2_BACKGROUND).getImage();
    private final Timer timer = new Timer(1000 / 60, this);
    private final AudioPlayer audioPlayer;
    private int frame;
    private int backgroundOffset;
    private int startX;
    private int startY;

    public SceneTransition(Game game) {
        this(game, null);
    }

    public SceneTransition(Game game, Player player) {
        this.game = game;
        this.player = player != null ? player : new Player();
        this.audioPlayer = createAudioPlayer();
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    private AudioPlayer createAudioPlayer() {
        try {
            AudioPlayer audio = new AudioPlayer(TRANSITION_AUDIO);
            audio.play();
            return audio;
        } catch (Exception exception) {
            System.err.println(
                    "Could not start transition audio: "
                            + exception.getMessage()
            );
            return null;
        }
    }

    public void start() {
        frame = 0;
        player.setX(135);
        player.setY(540);
        startX = player.getX();
        startY = player.getY();
        backgroundOffset = 0;
        timer.start();
    }

    public void stop() {
        timer.stop();
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
            } catch (Exception exception) {
                System.err.println(
                        "Error stopping transition audio: "
                                + exception.getMessage()
                );
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        frame++;
        backgroundOffset = (backgroundOffset + 2) % BOARD_HEIGHT;
        player.act();
        verticalPlane.update();

        int targetX = (BOARD_WIDTH - verticalPlane.getImage().getWidth(null)) / 2;
        int targetY =
                BOARD_HEIGHT - verticalPlane.getImage().getHeight(null) - 55;
        double progress = Math.min(1.0, frame / (double) DURATION_FRAMES);
        double easedProgress = easeInOut(progress);
        player.setX(interpolate(startX, targetX, easedProgress));
        player.setY(interpolate(startY, targetY, easedProgress));

        if (frame >= DURATION_FRAMES) {
            stop();
            game.loadScene2(backgroundOffset, player);
            return;
        }
        repaint();
    }

    private int interpolate(int start, int end, double progress) {
        return (int) Math.round(start + (end - start) * progress);
    }

    private double easeInOut(double progress) {
        return progress < 0.5
                ? 4 * progress * progress * progress
                : 1 - Math.pow(-2 * progress + 2, 3) / 2;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        drawBackground(g);

        if (frame < 45) {
            float fade = 0.45f * (1f - frame / 45f);
            g.setComposite(AlphaComposite.SrcOver.derive(fade));
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
            g.setComposite(AlphaComposite.SrcOver);
        }
        drawTurningPlayer(g);

        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(24f));
        String message = frame < 90 ? "SECTOR CLEARED" : "FINAL APPROACH";
        int textX = (BOARD_WIDTH - g.getFontMetrics().stringWidth(message)) / 2;
        g.drawString(message, textX, 90);
        g.dispose();
    }

    private void drawBackground(Graphics graphics) {
        graphics.drawImage(
                background,
                0,
                backgroundOffset - BOARD_HEIGHT,
                BOARD_WIDTH,
                BOARD_HEIGHT,
                this
        );
        graphics.drawImage(
                background,
                0,
                backgroundOffset,
                BOARD_WIDTH,
                BOARD_HEIGHT,
                this
        );
    }

    private void drawTurningPlayer(Graphics2D graphics) {
        final int turnStart = 45;
        final int turnEnd = 120;
        Image sideImage = player.getImage();
        Image verticalImage = verticalPlane.getImage();

        if (frame >= turnEnd) {
            graphics.drawImage(
                    verticalImage, player.getX(), player.getY(), this
            );
            return;
        }

        double progress = frame <= turnStart
                ? 0
                : (frame - turnStart) / (double) (turnEnd - turnStart);
        double angle = -Math.PI / 2.0 * easeInOut(progress);
        int centerX = player.getX() + sideImage.getWidth(null) / 2;
        int centerY = player.getY() + sideImage.getHeight(null) / 2;

        Graphics2D turning = (Graphics2D) graphics.create();
        turning.translate(centerX, centerY);
        turning.rotate(angle);
        turning.drawImage(
                sideImage,
                -sideImage.getWidth(null) / 2,
                -sideImage.getHeight(null) / 2,
                this
        );
        turning.dispose();
    }
}
