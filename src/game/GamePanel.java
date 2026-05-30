// Spielfläche des Spiels.
// Hier werden Spieler, Plattformen und Lava gezeichnet.
// Außerdem läuft hier der GameLoop über einen Timer.
package game;

import game.input.InputHandler;
import game.logic.CollisionManager;
import game.logic.PlatformManager;
import game.model.Lava;
import game.model.Player;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

public class GamePanel extends JPanel {

    private final int bildschirmBreite = 400;
    private final int bildschirmHoehe = 600;

    private Player spieler;
    private PlatformManager plattformManager;
    private CollisionManager kollisionsManager;
    private InputHandler eingabeHandler;
    private Lava lava;

    private GameState spielZustand;

    private Timer timer;

    public GamePanel() {
        setPreferredSize(new Dimension(bildschirmBreite, bildschirmHoehe));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);

        eingabeHandler = new InputHandler();
        addKeyListener(eingabeHandler);

        kollisionsManager = new CollisionManager();

        spielNeustarten();

        timer = new Timer(16, e -> {
            spielAktualisieren();
            repaint();
        });

        timer.start();
    }

    private void spielAktualisieren() {
        if (spielZustand == GameState.GAME_OVER) {
            if (eingabeHandler.isNeustartGedrueckt()) {
                spielNeustarten();
            }

            return;
        }

        spieler.update(
                eingabeHandler.isLinksGedrueckt(),
                eingabeHandler.isRechtsGedrueckt(),
                eingabeHandler.isSpringenGedrueckt()
        );

        kollisionsManager.checkPlatformCollision(spieler, plattformManager);

        if (kollisionsManager.checkLavaCollision(spieler, lava)) {
            spielZustand = GameState.GAME_OVER;
        }
    }

    private void spielNeustarten() {
        spieler = new Player(180, 500);
        plattformManager = new PlatformManager();
        lava = new Lava(bildschirmBreite, bildschirmHoehe);
        spielZustand = GameState.RUNNING;

        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        plattformManager.draw(g);
        lava.draw(g);
        spieler.draw(g);

        if (spielZustand == GameState.GAME_OVER) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("GAME OVER", 85, 260);

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Du bist in die Lava gefallen", 80, 300);
            g.drawString("Drücke R zum Neustarten", 85, 330);
        }
    }
}