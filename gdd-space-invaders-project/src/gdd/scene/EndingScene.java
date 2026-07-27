package gdd.scene;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import gdd.Game;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class EndingScene extends JPanel implements ActionListener {

    private final Game game;
    private final Timer timer = new Timer(1000 / 60, this);
    private int frame;

    public EndingScene(Game game) {
        this.game = game;
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    stop();
                    game.loadScene1();
                }
            }
        });
    }

    public void start() {
        frame = 0;
        timer.start();
        requestFocusInWindow();
    }

    public void stop() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        frame++;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < 55; i++) {
            int x = (i * 97 + 23) % BOARD_WIDTH;
            int y = (i * 61 + frame) % BOARD_HEIGHT;
            graphics.fillOval(x, y, 2, 2);
        }

        int alpha = Math.min(255, frame * 3);
        graphics.setColor(new Color(255, 220, 70, alpha));
        graphics.setFont(new Font("Helvetica", Font.BOLD, 42));
        String title = "MISSION COMPLETE";
        int x = (BOARD_WIDTH - graphics.getFontMetrics().stringWidth(title)) / 2;
        graphics.drawString(title, x, BOARD_HEIGHT / 2 - 35);

        graphics.setColor(new Color(255, 255, 255, alpha));
        graphics.setFont(graphics.getFont().deriveFont(21f));
        String message = "The final boss has been defeated.";
        x = (BOARD_WIDTH - graphics.getFontMetrics().stringWidth(message)) / 2;
        graphics.drawString(message, x, BOARD_HEIGHT / 2 + 15);

        if (frame > 120 && frame % 60 < 42) {
            graphics.setFont(graphics.getFont().deriveFont(17f));
            String restart = "Press ENTER to play again";
            x = (BOARD_WIDTH - graphics.getFontMetrics().stringWidth(restart)) / 2;
            graphics.drawString(restart, x, BOARD_HEIGHT / 2 + 80);
        }
    }
}
