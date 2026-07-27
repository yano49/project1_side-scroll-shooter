package gdd.scene;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import gdd.Game;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Short bridge between the side-scrolling stage and the vertical boss stage.
 * Scene 1 only needs to call Game.loadSceneTransition() when 30 minibosses
 * have been defeated.
 */
public class SceneTransition extends JPanel implements ActionListener {

    private static final int DURATION_FRAMES = 180;
    private static final int TARGET_Y = BOARD_HEIGHT - 130;

    private final Game game;
    private final Player player = new Player();
    private final VerticalPlayerAnimation verticalPlane =
            new VerticalPlayerAnimation();
    private final Timer timer = new Timer(1000 / 60, this);
    private int frame;

    public SceneTransition(Game game) {
        this.game = game;
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    public void start() {
        frame = 0;
        player.setX(135);
        player.setY(540);
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        frame++;
        player.act();
        verticalPlane.update();

        int targetX =
                (BOARD_WIDTH - verticalPlane.getImage().getWidth(null)) / 2;
        player.setX(moveToward(player.getX(), targetX, 3));
        player.setY(moveToward(player.getY(), TARGET_Y, 2));

        if (frame >= DURATION_FRAMES) {
            stop();
            game.loadScene2();
            return;
        }
        repaint();
    }

    private int moveToward(int value, int target, int amount) {
        if (value < target) {
            return Math.min(value + amount, target);
        }
        return Math.max(value - amount, target);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int scroll = (frame * 6) % BOARD_HEIGHT;
        g.setColor(Color.WHITE);
        for (int i = 0; i < 45; i++) {
            int x = (i * 83 + 31) % BOARD_WIDTH;
            int y = (i * 127 + scroll) % BOARD_HEIGHT;
            g.fillOval(x, y, 2, 8);
        }

        float progress = Math.min(1f, frame / (float) DURATION_FRAMES);
        g.setColor(new Color(0f, 0.5f, 1f, 1f - progress * 0.45f));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        drawTurningPlayer(g);

        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(24f));
        String message = frame < 90 ? "SECTOR CLEARED" : "FINAL APPROACH";
        int textX = (BOARD_WIDTH - g.getFontMetrics().stringWidth(message)) / 2;
        g.drawString(message, textX, 90);
        g.dispose();
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
        double angle = -Math.PI / 2.0 * progress;
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
