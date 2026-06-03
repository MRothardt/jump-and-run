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
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Polygon;

public class GamePanel extends JPanel {

    private final int bildschirmBreite = 400;
    private final int bildschirmHoehe = 600;
    private final int kameraGrenze = 250;

    private Player spieler;
    private PlatformManager plattformManager;
    private CollisionManager kollisionsManager;
    private InputHandler eingabeHandler;
    private Lava lava;

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
            spielZustand = GameState.GAME_OVER;
        }
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

        GradientPaint felsVerlauf = new GradientPaint(
                0, 0, new Color(19, 20, 24),
                0, bildschirmHoehe, new Color(48, 37, 31)
        );
        g2.setPaint(felsVerlauf);
        g2.fillRect(0, 0, bildschirmBreite, bildschirmHoehe);

        int langsamerVersatz = (punktzahl / 5) % 160;
        int schnellerVersatz = (punktzahl / 3) % 220;

        entfernteSchaechteZeichnen(g2, langsamerVersatz);
        felsTexturZeichnen(g2, langsamerVersatz);
        naheSchaechteZeichnen(g2, schnellerVersatz);
        stollenMitteZeichnen(g2);

        g2.dispose();
    }

    private void entfernteSchaechteZeichnen(Graphics2D g2, int versatz) {
        g2.setColor(new Color(9, 10, 12));
        for (int y = -160 + versatz; y < bildschirmHoehe; y += 160) {
            Polygon links = new Polygon();
            links.addPoint(0, y + 28);
            links.addPoint(92, y + 46);
            links.addPoint(92, y + 88);
            links.addPoint(0, y + 104);
            g2.fillPolygon(links);

            Polygon rechts = new Polygon();
            rechts.addPoint(bildschirmBreite, y + 98);
            rechts.addPoint(bildschirmBreite - 108, y + 116);
            rechts.addPoint(bildschirmBreite - 108, y + 154);
            rechts.addPoint(bildschirmBreite, y + 174);
            g2.fillPolygon(rechts);
        }

        g2.setColor(new Color(84, 58, 39));
        for (int y = -160 + versatz; y < bildschirmHoehe; y += 160) {
            g2.fillRect(84, y + 44, 5, 48);
            g2.fillRect(58, y + 50, 5, 38);
            g2.fillRect(bildschirmBreite - 112, y + 114, 5, 44);
            g2.fillRect(bildschirmBreite - 68, y + 122, 5, 36);
        }
    }

    private void felsTexturZeichnen(Graphics2D g2, int versatz) {
        Color dunklerFels = new Color(31, 29, 30);
        Color hellerFels = new Color(73, 57, 45);

        for (int y = -80 + versatz; y < bildschirmHoehe; y += 80) {
            for (int x = 0; x < bildschirmBreite; x += 56) {
                int muster = (x * 7 + y * 3) % 19;
                g2.setColor(muster % 2 == 0 ? dunklerFels : hellerFels);
                g2.drawLine(x + 6, y + 18 + muster, x + 34, y + 11 + muster / 2);
                g2.drawLine(x + 18, y + 48 - muster / 2, x + 50, y + 54 - muster);
            }
        }

        g2.setColor(new Color(92, 70, 48));
        for (int y = -120 + versatz; y < bildschirmHoehe; y += 120) {
            g2.fillRect(22, y + 24, 5, 72);
            g2.fillRect(bildschirmBreite - 28, y + 64, 5, 82);
        }
    }

    private void naheSchaechteZeichnen(Graphics2D g2, int versatz) {
        for (int y = -220 + versatz; y < bildschirmHoehe; y += 220) {
            g2.setColor(new Color(13, 12, 12));
            g2.fillRect(0, y + 38, 78, 62);
            g2.fillRect(bildschirmBreite - 84, y + 132, 84, 58);

            g2.setColor(new Color(103, 69, 42));
            g2.fillRect(69, y + 34, 6, 70);
            g2.fillRect(10, y + 42, 6, 54);
            g2.fillRect(16, y + 42, 55, 5);
            g2.fillRect(16, y + 91, 55, 5);

            g2.fillRect(bildschirmBreite - 88, y + 128, 6, 66);
            g2.fillRect(bildschirmBreite - 18, y + 137, 6, 48);
            g2.fillRect(bildschirmBreite - 82, y + 137, 64, 5);
            g2.fillRect(bildschirmBreite - 82, y + 181, 64, 5);

            g2.setColor(new Color(166, 105, 45));
            g2.fillRect(20, y + 50, 32, 3);
            g2.fillRect(bildschirmBreite - 62, y + 145, 34, 3);
        }
    }

    private void stollenMitteZeichnen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(72, 65, 256, 520);

        g2.setColor(new Color(118, 80, 42, 105));
        for (int y = 45; y < bildschirmHoehe; y += 95) {
            g2.fillRect(82, y, 236, 6);
            g2.fillRect(96, y - 4, 8, 42);
            g2.fillRect(296, y - 4, 8, 42);
        }

        g2.setColor(new Color(255, 196, 72, 24));
        g2.fillOval(125, 10, 150, 190);
    }

    private void scoreZeichnen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + punktzahl, 15, 25);
    }

    private void gameOverZeichnen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("GAME OVER", 85, 250);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Du bist in die Lava gefallen", 80, 290);
        g.drawString("Score: " + punktzahl, 150, 320);
        g.drawString("Drücke R zum Neustarten", 85, 350);
    }
}
