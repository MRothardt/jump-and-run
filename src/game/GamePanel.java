// Spielfläche des Spiels.
// Hier werden Spieler, Plattformen und Lava gezeichnet.
// Außerdem läuft hier der GameLoop über einen Timer.
package game;

import game.input.InputHandler;
import game.logic.CollisionManager;
import game.logic.PlatformManager;
import game.logic.Scoreboard;
import game.logic.Scoreboard.ScoreEintrag;
import game.model.Lava;
import game.model.Player;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {

    private static final BufferedImage startHoehlenBild = ladeBild("cave_background.png");
    private static final BufferedImage hoehlenKachelBild = ladeBild("cave_tile.png");

    private final int bildschirmBreite = 400;
    private final int bildschirmHoehe = 600;
    private final int kameraGrenze = 250;

    private Player spieler;
    private PlatformManager plattformManager;
    private CollisionManager kollisionsManager;
    private InputHandler eingabeHandler;
    private Lava lava;
    private Scoreboard scoreboard;

    private GameState spielZustand;

    private Timer timer;

    private int punktzahl;

    public GamePanel() {
        setPreferredSize(new Dimension(bildschirmBreite, bildschirmHoehe));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);

        eingabeHandler = new InputHandler();
        addKeyListener(eingabeHandler);

        kollisionsManager = new CollisionManager();
        scoreboard = new Scoreboard();

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

        plattformManager.aktualisieren();

        kameraAktualisieren();

        // Die Lava steigt langsam. Wenn der Spieler nicht weiterkommt,
        // holt die Lava ihn irgendwann ein.
        lava.aktualisieren();

        if (kollisionsManager.checkLavaCollision(spieler, lava)) {
            spielBeenden();
        }
    }

    private void spielBeenden() {
        spielZustand = GameState.GAME_OVER;

        String spielerName = JOptionPane.showInputDialog(
                this,
                "Name für das Scoreboard:",
                scoreboard.getLetzterSpielerName()
        );
        scoreboard.scoreEintragen(spielerName, punktzahl);
    }

    private void kameraAktualisieren() {
        if (spieler.getY() < kameraGrenze) {
            int verschiebung = kameraGrenze - spieler.getY();

            spieler.bewegeNachUnten(verschiebung);
            plattformManager.bewegeAllePlattformenNachUnten(verschiebung);

            // Wenn der Spieler wirklich Fortschritt macht,
            // wird die Lava wieder nach unten gedrückt.
            // Dadurch bleibt sie meistens im unteren Bereich,
            // steigt aber trotzdem, wenn man zu lange stehen bleibt.
            lava.nachUntenDruecken(verschiebung);

            punktzahl += verschiebung;
        }
    }

    private void spielNeustarten() {
        spieler = new Player(180, 500);
        plattformManager = new PlatformManager();
        lava = new Lava(bildschirmBreite, bildschirmHoehe);
        spielZustand = GameState.RUNNING;
        punktzahl = 0;

        requestFocusInWindow();
        eingabeHandler.zuruecksetzen();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        hintergrundZeichnen(g);
        plattformManager.draw(g);
        lava.draw(g);
        spieler.draw(g);

        scoreZeichnen(g);

        if (spielZustand == GameState.GAME_OVER) {
            gameOverZeichnen(g);
        }
    }

    private void hintergrundZeichnen(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (startHoehlenBild == null || hoehlenKachelBild == null) {
            g2.setColor(new Color(19, 20, 24));
            g2.fillRect(0, 0, bildschirmBreite, bildschirmHoehe);
            g2.dispose();
            return;
        }

        g2.setColor(new Color(12, 14, 17));
        g2.fillRect(0, 0, bildschirmBreite, bildschirmHoehe);

        int skalierteStartBildHoehe = startHoehlenBild.getHeight() * bildschirmBreite / startHoehlenBild.getWidth();
        int skalierteKachelHoehe = hoehlenKachelBild.getHeight() * bildschirmBreite / hoehlenKachelBild.getWidth();
        int startBildY = bildschirmHoehe - skalierteStartBildHoehe + punktzahl;

        int kachelY = startBildY - skalierteKachelHoehe;
        while (kachelY > -skalierteKachelHoehe) {
            kachelY -= skalierteKachelHoehe;
        }

        for (int y = kachelY; y < bildschirmHoehe; y += skalierteKachelHoehe) {
            g2.drawImage(hoehlenKachelBild, 0, y, bildschirmBreite, skalierteKachelHoehe, null);
        }

        if (startBildY < bildschirmHoehe) {
            g2.drawImage(startHoehlenBild, 0, startBildY, bildschirmBreite, skalierteStartBildHoehe, null);
        }

        g2.dispose();
    }

    private static BufferedImage ladeBild(String dateiname) {
        URL bildUrl = GamePanel.class.getResource("/game/assets/" + dateiname);

        try {
            if (bildUrl != null) {
                return ImageIO.read(bildUrl);
            }

            return ImageIO.read(new File("src/game/assets/" + dateiname));
        } catch (IOException e) {
            return null;
        }
    }

    private void scoreZeichnen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + punktzahl, 15, 25);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Best: " + scoreboard.getBesterScore(), 15, 45);
    }

    private void gameOverZeichnen(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 175));
        g2.fillRoundRect(35, 145, 330, 380, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(35, 145, 330, 380, 12, 12);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        zentriertenTextZeichnen(g, "GAME OVER", 205);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        zentriertenTextZeichnen(g, "Du bist in die Lava gefallen", 240);
        zentriertenTextZeichnen(g, "Score: " + punktzahl, 270);

        scoreboardZeichnen(g, 320);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        zentriertenTextZeichnen(g, "Drücke R zum Neustarten", 495);
        g2.dispose();
    }

    private void scoreboardZeichnen(Graphics g, int startY) {
        List<ScoreEintrag> eintraege = scoreboard.getEintraege();

        g.setFont(new Font("Arial", Font.BOLD, 20));
        zentriertenTextZeichnen(g, "Scoreboard", startY);

        g.setFont(new Font("Arial", Font.PLAIN, 16));

        if (eintraege.isEmpty()) {
            zentriertenTextZeichnen(g, "Noch keine Scores", startY + 35);
            return;
        }

        int maxAnzahl = Math.min(5, eintraege.size());

        for (int i = 0; i < maxAnzahl; i++) {
            ScoreEintrag eintrag = eintraege.get(i);
            int y = startY + 35 + i * 25;

            g.drawString((i + 1) + ".", 80, y);
            g.drawString(eintrag.getSpielerName(), 115, y);
            rechtsbuendigenTextZeichnen(g, String.valueOf(eintrag.getScore()), 320, y);
        }
    }

    private void zentriertenTextZeichnen(Graphics g, String text, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int x = (bildschirmBreite - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void rechtsbuendigenTextZeichnen(Graphics g, String text, int rechterRand, int y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, rechterRand - metrics.stringWidth(text), y);
    }
}
