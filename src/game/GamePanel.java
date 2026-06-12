// Spielfläche des Spiels.
// Hier werden Spieler, Plattformen und Lava gezeichnet.
// Außerdem läuft hier der GameLoop über einen Timer.
package game;

import game.input.InputHandler;
import game.logic.CollisionManager;
import game.logic.PlatformManager;
import game.logic.Scoreboard;
import game.logic.Scoreboard.ScoreEintrag;
import game.model.FallingRock;
import game.model.Lava;
import game.model.LavaBurst;
import game.model.Player;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {

    private static final BufferedImage startHoehlenBild = ladeBild("cave_background.png");
    private static final BufferedImage hoehlenHauptBild = ladeBild("cave_main.png");
    private static final BufferedImage gameOverTafelBild = ladeBild("game_over_panel.png");

    private final int bildschirmBreite = 400;
    private final int bildschirmHoehe = 600;
    private final int kameraGrenze = 250;

    private Player spieler;
    private PlatformManager plattformManager;
    private CollisionManager kollisionsManager;
    private InputHandler eingabeHandler;
    private Lava lava;
    private ArrayList<LavaBurst> lavaStoesse;
    private ArrayList<FallingRock> fallendeSteine;
    private Scoreboard scoreboard;
    private String spielerName;
    private Runnable menuAnzeigen;
    private JButton neuVersuchenButton;
    private JButton menuButton;

    private GameState spielZustand;

    private Timer timer;

    private int punktzahl;
    private int lavaStossCooldown;
    private int steinCooldown;
    private Random zufall;

    public GamePanel(Scoreboard scoreboard, String spielerName, Runnable menuAnzeigen) {
        setPreferredSize(new Dimension(bildschirmBreite, bildschirmHoehe));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        setLayout(null);

        eingabeHandler = new InputHandler();
        addKeyListener(eingabeHandler);

        kollisionsManager = new CollisionManager();
        zufall = new Random();
        this.scoreboard = scoreboard;
        this.spielerName = spielerName;
        this.menuAnzeigen = menuAnzeigen;

        gameOverButtonsErstellen();
        hotkeysRegistrieren();

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

            if (eingabeHandler.isMenuGedrueckt()) {
                zumMenuWechseln();
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

        int hoehenFortschritt = kameraAktualisieren();

        lavaAktualisieren(hoehenFortschritt);
        lavaStoesseAktualisieren();
        fallendeSteineAktualisieren();

        if (kollisionsManager.checkLavaCollision(spieler, lava)
                || lavaStossTrifftSpieler()
                || fallenderSteinTrifftSpieler()) {
            spielBeenden();
        }
    }

    private void spielBeenden() {
        // Setzt den Spielzustand auf Game Over, damit die Game-Over-Oberflaeche gezeichnet wird.
        spielZustand = GameState.GAME_OVER;
        // Traegt den aktuellen Score fuer den im Menue gewaehlten Spielernamen ein.
        scoreboard.scoreEintragen(spielerName, punktzahl);
        // Blendet die Game-Over-Buttons ein, weil sie nur nach dem Tod anklickbar sein sollen.
        gameOverButtonsSichtbarSetzen(true);
    }

    private int kameraAktualisieren() {
        if (spieler.getY() < kameraGrenze) {
            int verschiebung = kameraGrenze - spieler.getY();

            spieler.bewegeNachUnten(verschiebung);
            plattformManager.bewegeAllePlattformenNachUnten(verschiebung);
            lavaStoesseNachUntenBewegen(verschiebung);
            // Fallende Steine werden hier bewusst NICHT bewegt.
            // Sie fallen mit fester Bildschirmgeschwindigkeit und sollen nicht
            // schneller/langsamer werden, nur weil der Spieler schnell hochspringt.

            punktzahl += verschiebung;
            return verschiebung;
        }

        return 0;
    }

    private void lavaAktualisieren(int hoehenFortschritt) {
        if (hoehenFortschritt > 0) {
            // Bei echtem Fortschritt kann der Spieler vor der Lava fliehen.
            // Die Lava wird nach unten gedrueckt, steigt aber trotzdem pro Tick ein kleines Stueck.
            lava.nachUntenDruecken(hoehenFortschritt);
        }

        // Die Lava steigt dauerhaft leicht nach oben und wird mit steigendem Score schneller.
        lava.aktualisieren(punktzahl);
    }

    private void lavaStoesseAktualisieren() {
        for (LavaBurst lavaStoss : lavaStoesse) {
            lavaStoss.aktualisieren();
        }

        lavaStoesse.removeIf(LavaBurst::istAbgelaufen);

        if (punktzahl < 20000) {
            return;
        }

        lavaStossCooldown--;

        if (lavaStossCooldown <= 0) {
            int x = 35 + zufall.nextInt(bildschirmBreite - 80);
            lavaStoesse.add(new LavaBurst(x, lava.getY()));
            // Nach einem Lava-Stoss kommt bewusst eine laengere Pause.
            // Dadurch bleibt der Effekt gefaehrlich, wirkt aber nicht wie Dauerbeschuss.
            lavaStossCooldown = 430 + zufall.nextInt(310);
        }
    }

    private void lavaStoesseNachUntenBewegen(int distanz) {
        for (LavaBurst lavaStoss : lavaStoesse) {
            lavaStoss.bewegeNachUnten(distanz);
        }
    }

    private void fallendeSteineAktualisieren() {
        for (FallingRock stein : fallendeSteine) {
            stein.aktualisieren();
        }

        fallendeSteine.removeIf(FallingRock::istEntfernt);

        steinCooldown--;

        if (steinCooldown <= 0) {
            int x = 25 + zufall.nextInt(bildschirmBreite - 73);
            fallendeSteine.add(new FallingRock(x));
            // Neue Steine fallen nicht dauernd, sondern mit Abstand,
            // damit die Warnung sichtbar bleibt und Ausweichen moeglich ist.
            steinCooldown = 240 + zufall.nextInt(190);
        }
    }

    private boolean lavaStossTrifftSpieler() {
        for (LavaBurst lavaStoss : lavaStoesse) {
            if (lavaStoss.trifft(spieler)) {
                return true;
            }
        }

        return false;
    }

    private boolean fallenderSteinTrifftSpieler() {
        for (FallingRock stein : fallendeSteine) {
            if (stein.trifft(spieler)) {
                return true;
            }
        }

        return false;
    }

    private void spielNeustarten() {
        spieler = new Player(180, 500);
        plattformManager = new PlatformManager();
        lava = new Lava(bildschirmBreite, bildschirmHoehe);
        lavaStoesse = new ArrayList<>();
        fallendeSteine = new ArrayList<>();
        spielZustand = GameState.RUNNING;
        punktzahl = 0;
        // Erster Lava-Stoss kommt erst mit Abstand nach dem Erreichen der Score-Grenze.
        lavaStossCooldown = 520;
        // Der erste Deckenstein kommt erst nach kurzer Spielzeit.
        steinCooldown = 240;

        gameOverButtonsSichtbarSetzen(false);
        requestFocusInWindow();
        eingabeHandler.zuruecksetzen();
    }

    private void gameOverButtonsErstellen() {
        // Erstellt den Button zum direkten Neustarten mit dem gleichen Namen.
        neuVersuchenButton = buttonErstellen("menu_sign_retry.png");
        // Startet beim Klick eine neue Runde ohne Rueckkehr ins Hauptmenue.
        neuVersuchenButton.addActionListener(e -> {
            // Setzt Spieler, Plattformen, Lava und Score zurueck.
            spielNeustarten();
            // Gibt den Tastaturfokus zurueck an das Spielpanel.
            requestFocusInWindow();
        });
        // Fuegt den Neustart-Button dem Spielpanel hinzu.
        add(neuVersuchenButton);

        // Erstellt den Button fuer die Rueckkehr ins Hauptmenue.
        menuButton = buttonErstellen("menu_sign_menu.png");
        // Wechselt beim Klick zurueck ins Menue.
        menuButton.addActionListener(e -> zumMenuWechseln());
        // Fuegt den Menue-Button dem Spielpanel hinzu.
        add(menuButton);

        // Versteckt beide Buttons, solange das Spiel noch laeuft.
        gameOverButtonsSichtbarSetzen(false);
    }

    private void hotkeysRegistrieren() {
        // Registriert R als globalen Hotkey fuer den Game-Over-Neustart.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("R"), "neuVersuchen");
        // Verknuepft den Hotkey R mit einer Neustart-Aktion.
        getActionMap().put("neuVersuchen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Neustart ist nur im Game-Over-Zustand erlaubt.
                if (spielZustand == GameState.GAME_OVER) {
                    // Startet die Runde neu.
                    spielNeustarten();
                }
            }
        });

        // Registriert M als globalen Hotkey fuer die Rueckkehr ins Menue.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("M"), "menu");
        // Verknuepft den Hotkey M mit dem Menuewechsel.
        getActionMap().put("menu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Menuewechsel ist nur im Game-Over-Zustand erlaubt.
                if (spielZustand == GameState.GAME_OVER) {
                    // Stoppt das Spiel und zeigt das Hauptmenue.
                    zumMenuWechseln();
                }
            }
        });
    }

    @Override
    public void doLayout() {
        super.doLayout();

        if (neuVersuchenButton != null && menuButton != null) {
            // Platziert den Neustart-Button unten links auf der Game-Over-Tafel.
            neuVersuchenButton.setBounds(45, 482, 150, 38);
            // Platziert den Menue-Button unten rechts auf der Game-Over-Tafel.
            menuButton.setBounds(205, 482, 150, 38);
        }
    }

    private JButton buttonErstellen(String bildDatei) {
        JButton button = new JButton();
        BufferedImage bild = ladeBild(bildDatei);

        if (bild != null) {
            button.setIcon(new ImageIcon(bild));
        }

        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        return button;
    }

    private void gameOverButtonsSichtbarSetzen(boolean sichtbar) {
        if (neuVersuchenButton != null) {
            neuVersuchenButton.setVisible(sichtbar);
        }

        if (menuButton != null) {
            menuButton.setVisible(sichtbar);
        }
    }

    private void zumMenuWechseln() {
        // Stoppt den Timer, damit im Hintergrund kein alter GameLoop weiterlaeuft.
        timer.stop();
        // Ruft die vom GameFrame uebergebene Funktion auf, die das Menue wieder einsetzt.
        menuAnzeigen.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        hintergrundZeichnen(g);
        plattformManager.draw(g);
        // Lava-Stoesse werden vor der Lava gezeichnet, damit ihr Ursprung
        // optisch hinter der Lava-Oberflaeche liegt.
        lavaStoesseZeichnen(g);
        // Fallende Steine werden ebenfalls vor der Lava gezeichnet.
        // Dadurch verschwinden sie optisch hinter der Lava-Ebene, wenn sie unten ankommen.
        fallendeSteineZeichnen(g);
        lava.draw(g);
        lavaStossWarnungenZeichnen(g);
        spieler.draw(g);

        scoreZeichnen(g);

        if (spielZustand == GameState.GAME_OVER) {
            gameOverZeichnen(g);
        }
    }

    private void lavaStoesseZeichnen(Graphics g) {
        for (LavaBurst lavaStoss : lavaStoesse) {
            lavaStoss.draw(g);
        }
    }

    private void lavaStossWarnungenZeichnen(Graphics g) {
        for (LavaBurst lavaStoss : lavaStoesse) {
            lavaStoss.warnungZeichnen(g, lava.getY());
        }
    }

    private void fallendeSteineZeichnen(Graphics g) {
        for (FallingRock stein : fallendeSteine) {
            stein.draw(g);
        }
    }

    private void hintergrundZeichnen(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (startHoehlenBild == null || hoehlenHauptBild == null) {
            g2.setColor(new Color(19, 20, 24));
            g2.fillRect(0, 0, bildschirmBreite, bildschirmHoehe);
            g2.dispose();
            return;
        }

        g2.setColor(new Color(12, 14, 17));
        g2.fillRect(0, 0, bildschirmBreite, bildschirmHoehe);

        int skalierteStartBildHoehe = startHoehlenBild.getHeight() * bildschirmBreite / startHoehlenBild.getWidth();
        int skalierteHauptBildHoehe = hoehlenHauptBild.getHeight() * bildschirmBreite / hoehlenHauptBild.getWidth();
        int startBildY = bildschirmHoehe - skalierteStartBildHoehe + punktzahl;

        int hauptBildY = startBildY - skalierteHauptBildHoehe;
        while (hauptBildY > -skalierteHauptBildHoehe) {
            hauptBildY -= skalierteHauptBildHoehe;
        }

        for (int y = hauptBildY; y < bildschirmHoehe; y += skalierteHauptBildHoehe) {
            g2.drawImage(hoehlenHauptBild, 0, y, bildschirmBreite, skalierteHauptBildHoehe, null);
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
        if (gameOverTafelBild != null) {
            g2.drawImage(gameOverTafelBild, 35, 145, 330, 380, null);
        } else {
            g2.setColor(new Color(0, 0, 0, 175));
            g2.fillRoundRect(35, 145, 330, 380, 12, 12);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(35, 145, 330, 380, 12, 12);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        zentriertenTextZeichnen(g, "GAME OVER", 205);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        zentriertenTextZeichnen(g, "Du bist in die Lava gefallen", 240);
        zentriertenTextZeichnen(g, "Score: " + punktzahl, 270);
        // Zeichnet, welchen Platz der Spieler mit seinem gespeicherten Score aktuell hat.
        rangZeichnen(g, 292);

        // Zeichnet eine kurze Scoreboard-Liste auf der Game-Over-Tafel.
        scoreboardZeichnen(g, 325);
        // Zeichnet eine kleine Legende fuer die zwei wichtigsten Hotkeys.
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.dispose();
    }

    private void rangZeichnen(Graphics g, int y) {
        // Fragt den aktuellen Rang des Spielers aus dem Scoreboard ab.
        int rang = scoreboard.getRang(spielerName);

        // Zeichnet den Rang nur, wenn der Spieler im Scoreboard gefunden wurde.
        if (rang > 0) {
            // Zeigt den Rang zentriert unter dem aktuellen Score.
            zentriertenTextZeichnen(g, "Dein Rang: " + rang + ".", y);
        }
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

        int maxAnzahl = Math.min(3, eintraege.size());

        for (int i = 0; i < maxAnzahl; i++) {
            ScoreEintrag eintrag = eintraege.get(i);
            int y = startY + 32 + i * 23;

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
