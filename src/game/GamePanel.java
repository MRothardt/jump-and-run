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
import javax.swing.SwingUtilities;
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
    private static final int LAVA_TOD_ANIMATION_DAUER = 26;
    private static final int STEIN_TOD_ANIMATION_DAUER = 18;
    private static final double HINTERGRUND_SCROLL_GLAETTUNG = 0.22;

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
    private Runnable scoreboardAenderungsListener;
    private String spielerName;
    private Runnable menuAnzeigen;
    private JButton neuVersuchenButton;
    private JButton menuButton;
    private TodesUrsache todesUrsache;
    private FallingRock todesStein;
    private int sterbeAnimationZaehler;

    private GameState spielZustand;

    private Timer timer;

    private int punktzahl;
    private double hintergrundScroll;
    private int lavaStossCooldown;
    private int steinCooldown;
    private Random zufall;

    private enum TodesUrsache {
        LAVA,
        FALLENDER_STEIN
    }

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
        scoreboardAenderungsListener = () -> SwingUtilities.invokeLater(this::repaint);
        this.scoreboard.aenderungsListenerHinzufuegen(scoreboardAenderungsListener);

        gameOverButtonsErstellen();
        hotkeysRegistrieren();

        spielNeustarten();

        timer = new Timer(16, e -> {
            spielAktualisieren();
            repaint();
        });

        timer.start();
    }

    @Override
    public void removeNotify() {
        scoreboard.aenderungsListenerEntfernen(scoreboardAenderungsListener);
        super.removeNotify();
    }

    private void spielAktualisieren() {
        if (spielZustand == GameState.DYING) {
            sterbeAnimationAktualisieren();
            return;
        }

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
        hintergrundScrollAktualisieren();

        if (kollisionsManager.checkLavaCollision(spieler, lava) || lavaStossTrifftSpieler()) {
            sterbeAnimationStarten(TodesUrsache.LAVA, null);
            return;
        }

        FallingRock treffenderStein = treffendenFallendenSteinFinden();

        if (treffenderStein != null) {
            sterbeAnimationStarten(TodesUrsache.FALLENDER_STEIN, treffenderStein);
        }
    }

    private void spielBeenden() {
        if (spielZustand == GameState.GAME_OVER) {
            return;
        }

        // Setzt den Spielzustand auf Game Over, damit die Game-Over-Oberflaeche gezeichnet wird.
        spielZustand = GameState.GAME_OVER;
        // Traegt den aktuellen Score fuer den im Menue gewaehlten Spielernamen ein.
        scoreboard.scoreEintragen(spielerName, punktzahl);
        // Blendet die Game-Over-Buttons ein, weil sie nur nach dem Tod anklickbar sein sollen.
        gameOverButtonsSichtbarSetzen(true);
    }

    private void sterbeAnimationStarten(TodesUrsache ursache, FallingRock stein) {
        if (spielZustand != GameState.RUNNING) {
            return;
        }

        spielZustand = GameState.DYING;
        todesUrsache = ursache;
        todesStein = stein;
        sterbeAnimationZaehler = 0;
        eingabeHandler.zuruecksetzen();
    }

    private void sterbeAnimationAktualisieren() {
        sterbeAnimationZaehler++;

        if (todesUrsache == TodesUrsache.LAVA) {
            spieler.bewegeNachUnten(2);
        }

        if (sterbeAnimationZaehler >= sterbeAnimationDauer()) {
            spielBeenden();
        }
    }

    private int sterbeAnimationDauer() {
        if (todesUrsache == TodesUrsache.FALLENDER_STEIN) {
            return STEIN_TOD_ANIMATION_DAUER;
        }

        return LAVA_TOD_ANIMATION_DAUER;
    }

    private int kameraAktualisieren() {
        if (spieler.getY() < kameraGrenze) {
            int verschiebung = kameraGrenze - spieler.getY();

            spieler.bewegeNachUnten(verschiebung);
            plattformManager.bewegeAllePlattformenNachUnten(verschiebung);
            lavaStoesseNachUntenBewegen(verschiebung);
            fallendeSteineNachUntenBewegen(verschiebung);

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

    private void fallendeSteineNachUntenBewegen(int distanz) {
        for (FallingRock stein : fallendeSteine) {
            stein.bewegeNachUnten(distanz);
        }
    }

    private void hintergrundScrollAktualisieren() {
        double differenz = punktzahl - hintergrundScroll;

        if (Math.abs(differenz) < 0.05) {
            hintergrundScroll = punktzahl;
            return;
        }

        hintergrundScroll += differenz * HINTERGRUND_SCROLL_GLAETTUNG;
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

    private FallingRock treffendenFallendenSteinFinden() {
        for (FallingRock stein : fallendeSteine) {
            if (stein.trifft(spieler)) {
                return stein;
            }
        }

        return null;
    }

    private void spielNeustarten() {
        spieler = new Player(180, 500);
        plattformManager = new PlatformManager();
        lava = new Lava(bildschirmBreite, bildschirmHoehe);
        lavaStoesse = new ArrayList<>();
        fallendeSteine = new ArrayList<>();
        spielZustand = GameState.RUNNING;
        todesUrsache = null;
        todesStein = null;
        sterbeAnimationZaehler = 0;
        punktzahl = 0;
        hintergrundScroll = 0.0;
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
        if (istLavaTodAnimation()) {
            spieler.draw(g);
        }
        lava.draw(g);
        lavaStossWarnungenZeichnen(g);
        if (!istLavaTodAnimation()) {
            spielerZeichnen(g);
        }

        if (spielZustand == GameState.DYING) {
            sterbeAnimationZeichnen(g);
        }

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

    private void spielerZeichnen(Graphics g) {
        if (spielZustand == GameState.DYING && todesUrsache == TodesUrsache.FALLENDER_STEIN) {
            spieler.drawCrushed(g, sterbeFortschritt());
            return;
        }

        spieler.draw(g);
    }

    private boolean istLavaTodAnimation() {
        return spielZustand == GameState.DYING && todesUrsache == TodesUrsache.LAVA;
    }

    private void sterbeAnimationZeichnen(Graphics g) {
        if (todesUrsache == TodesUrsache.FALLENDER_STEIN) {
            steinTodAnimationZeichnen(g);
            return;
        }

        lavaTodAnimationZeichnen(g);
    }

    private double sterbeFortschritt() {
        double linearerFortschritt = Math.min(1.0, sterbeAnimationZaehler / (double) sterbeAnimationDauer());
        return 1.0 - Math.pow(1.0 - linearerFortschritt, 2.0);
    }

    private void lavaTodAnimationZeichnen(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        double fortschritt = sterbeFortschritt();
        int lavaY = lava.getY();
        int mitteX = spieler.getX() + spieler.getWidth() / 2;
        int spritzerHoehe = 12 + (int) (22 * (1.0 - fortschritt));

        for (int i = 0; i < 7; i++) {
            int x = mitteX - 30 + i * 10;
            int y = lavaY - spritzerHoehe + (i % 2) * 7 + (int) (fortschritt * 16);
            int groesse = 4 + (i % 3);

            g2.setColor(new Color(255, 198, 57, 190));
            g2.fillRect(x, y, groesse, groesse);
            g2.setColor(new Color(189, 45, 10, 185));
            g2.fillRect(x - 2, y + groesse, groesse + 4, 3);
        }

        int overlayY = Math.max(0, lavaY - 18);
        int overlayAlpha = Math.min(120, 25 + (int) (fortschritt * 95));
        g2.setColor(new Color(255, 82, 14, overlayAlpha));
        g2.fillRect(0, overlayY, bildschirmBreite, bildschirmHoehe - overlayY);
        g2.dispose();
    }

    private void steinTodAnimationZeichnen(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        double fortschritt = sterbeFortschritt();
        int mitteX = spieler.getX() + spieler.getWidth() / 2;
        int bodenY = spieler.getY() + spieler.getHeight();
        int staubAlpha = Math.max(0, 180 - (int) (fortschritt * 180));

        if (todesStein != null) {
            todesStein.draw(g2);
        }

        g2.setColor(new Color(136, 112, 79, staubAlpha));

        for (int i = 0; i < 9; i++) {
            int richtung = i - 4;
            int x = mitteX + richtung * (6 + (int) (fortschritt * 14));
            int y = bodenY - 6 - Math.abs(richtung) * 2 - (int) (fortschritt * 10);
            g2.fillRect(x, y, 9, 4);
        }

        if (fortschritt < 0.42) {
            int alpha = Math.max(0, 210 - (int) (fortschritt * 500));
            g2.setColor(new Color(255, 223, 125, alpha));
            g2.drawLine(mitteX - 32, bodenY - 38, mitteX - 12, bodenY - 18);
            g2.drawLine(mitteX + 32, bodenY - 38, mitteX + 12, bodenY - 18);
            g2.drawLine(mitteX, bodenY - 52, mitteX, bodenY - 24);
        }

        g2.dispose();
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
        int startBildY = (int) Math.round(bildschirmHoehe - skalierteStartBildHoehe + hintergrundScroll);

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
