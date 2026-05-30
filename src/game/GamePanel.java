package game;
// Spielfläche des Spiels.
// Hier werden Spieler und Plattformen gezeichnet.
// Außerdem läuft hier der GameLoop über einen Timer.
import game.input.InputHandler;
import game.logic.CollisionManager;
import game.logic.PlatformManager;
import game.model.Player;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class GamePanel extends JPanel {

    private final int screenWidth = 400;
    private final int screenHeight = 600;

    private Player player;
    private PlatformManager platformManager;
    private CollisionManager collisionManager;
    private InputHandler inputHandler;

    private Timer timer;

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.CYAN);
        setFocusable(true);

        player = new Player(180, 500);
        platformManager = new PlatformManager();
        collisionManager = new CollisionManager();

        inputHandler = new InputHandler();
        addKeyListener(inputHandler);

        timer = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        timer.start();
    }

    private void updateGame() {
        player.update(
                inputHandler.isLeftPressed(),
                inputHandler.isRightPressed(),
                inputHandler.isSpacePressed()
        );

        collisionManager.checkPlatformCollision(player, platformManager);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        platformManager.draw(g);
        player.draw(g);
    }
}