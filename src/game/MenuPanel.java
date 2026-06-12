// Die Klasse gehoert zum Paket game.
package game;

// Importiert die Scoreboard-Klasse fuer gespeicherte Punktestaende.
import game.logic.Scoreboard;
// Importiert den einzelnen Scoreboard-Eintrag fuer die Anzeige.
import game.logic.Scoreboard.ScoreEintrag;

// Importiert ImageIO, damit PNG-Dateien geladen werden koennen.
import javax.imageio.ImageIO;
// Importiert ImageIcon, damit PNG-Dateien auf Buttons angezeigt werden koennen.
import javax.swing.ImageIcon;
// Importiert JButton fuer klickbare Menue-Schilder.
import javax.swing.JButton;
// Importiert JPanel als Basis fuer die Menue-Oberflaeche.
import javax.swing.JPanel;
// Importiert SwingUtilities, damit Firebase-Updates sicher neu zeichnen.
import javax.swing.SwingUtilities;
// Importiert JTextField fuer die Namenseingabe.
import javax.swing.JTextField;
// Importiert DocumentEvent, damit Textaenderungen im Namensfeld erkannt werden.
import javax.swing.event.DocumentEvent;
// Importiert DocumentListener, damit der Spielen-Button auf Texteingaben reagieren kann.
import javax.swing.event.DocumentListener;
// Importiert Color fuer Text- und Fallback-Farben.
import java.awt.Color;
// Importiert Dimension fuer die feste Panel-Groesse.
import java.awt.Dimension;
// Importiert Font fuer die blockige Menue-Schrift.
import java.awt.Font;
// Importiert FontMetrics zum Zentrieren von Text.
import java.awt.FontMetrics;
// Importiert Graphics fuer normale Zeichenoperationen.
import java.awt.Graphics;
// Importiert Graphics2D fuer skalierte Bilder und bessere Zeichenoptionen.
import java.awt.Graphics2D;
// Importiert RenderingHints fuer sauberes Skalieren des Hintergrundbilds.
import java.awt.RenderingHints;
// Importiert MouseWheelEvent, damit das Scoreboard gescrollt werden kann.
import java.awt.event.MouseWheelEvent;
// Importiert MouseWheelListener fuer Mausrad-Bewegungen im Scoreboard.
import java.awt.event.MouseWheelListener;
// Importiert BufferedImage als Datentyp fuer geladene PNG-Dateien.
import java.awt.image.BufferedImage;
// Importiert File fuer den Fallback-Ladepfad aus dem Projektordner.
import java.io.File;
// Importiert IOException fuer Fehler beim Bildladen.
import java.io.IOException;
// Importiert URL fuer das Laden von Ressourcen aus dem Klassenpfad.
import java.net.URL;
// Importiert List fuer die Scoreboard-Eintraege.
import java.util.List;
// Importiert Consumer, damit das Menue dem GameFrame den Spielernamen uebergeben kann.
import java.util.function.Consumer;

// Definiert das Hauptmenue als eigenes JPanel.
public class MenuPanel extends JPanel {

    // Laedt das grosse Hintergrundbild des Hauptmenues.
    private static final BufferedImage menuHintergrund = ladeBild("menu_background.png");
    // Laedt die grosse Tafel hinter Namensfeld und Buttons.
    private static final BufferedImage menuTafelBild = ladeBild("menu_panel.png");
    // Laedt das Schild fuer die Namenseingabe.
    private static final BufferedImage nameSchildBild = ladeBild("menu_sign_name.png");
    // Laedt die Tafel fuer die Scoreboard-Anzeige im Menue.
    private static final BufferedImage scoreboardTafelBild = ladeBild("scoreboard_panel.png");
    // Laedt das normale Spielen-Schild.
    private static final BufferedImage spielenBild = ladeBild("menu_sign_play.png");
    // Laedt das gesperrte Spielen-Schild mit Kreuz.
    private static final BufferedImage spielenGesperrtBild = ladeBild("menu_sign_play_locked.png");

    // Legt die feste Breite des Spielfensters fest.
    private final int bildschirmBreite = 400;
    // Legt die feste Hoehe des Spielfensters fest.
    private final int bildschirmHoehe = 600;

    // Speichert die gemeinsame Scoreboard-Instanz.
    private final Scoreboard scoreboard;
    // Merkt sich den Listener fuer Scoreboard-Aenderungen.
    private final Runnable scoreboardAenderungsListener;
    // Speichert die Funktion, mit der das Spiel aus dem Menue gestartet wird.
    private final Consumer<String> spielStarten;
    // Speichert das Eingabefeld fuer den Spielernamen.
    private final JTextField nameFeld;
    // Speichert den Spielen-Button.
    private final JButton spielenButton;
    // Speichert den Scoreboard-Button.
    private final JButton scoreboardButton;
    // Speichert den ersten sichtbaren Scoreboard-Eintrag fuer Scrollen.
    private int scoreboardScrollIndex;
    // Sammelt feine Trackpad-Scrollbewegungen, bis eine ganze Zeile erreicht ist.
    private double scoreboardScrollRest;

    // Merkt sich, ob das Scoreboard im Menue eingeblendet ist.
    private boolean scoreboardAnzeigen;

    // Erstellt das Menue mit Scoreboard, Start-Funktion und vorausgefuelltem Namen.
    public MenuPanel(Scoreboard scoreboard, Consumer<String> spielStarten, String vorausgefuellterName) {
        // Speichert die uebergebene Scoreboard-Instanz.
        this.scoreboard = scoreboard;
        // Speichert die uebergebene Start-Funktion.
        this.spielStarten = spielStarten;
        // Zeichnet das Menue neu, wenn Scores aus Firebase nachgeladen wurden.
        this.scoreboardAenderungsListener = () -> SwingUtilities.invokeLater(this::repaint);
        this.scoreboard.aenderungsListenerHinzufuegen(scoreboardAenderungsListener);

        // Setzt die feste Groesse des Menues.
        setPreferredSize(new Dimension(bildschirmBreite, bildschirmHoehe));
        // Deaktiviert automatische Layouts, weil die Elemente pixelgenau platziert werden.
        setLayout(null);
        // Macht das Panel fokusfaehig.
        setFocusable(true);

        // Erstellt das Textfeld fuer den Namen.
        nameFeld = new JTextField();
        // Setzt eine blockige Schrift, die besser zum Pixel-/Minenstil passt.
        nameFeld.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        // Setzt die Textfarbe auf helles warmes Beige.
        nameFeld.setForeground(new Color(245, 222, 169));
        // Setzt die Cursorfarbe passend zur Textfarbe.
        nameFeld.setCaretColor(new Color(245, 222, 169));
        // Fuellt den zuletzt genutzten Namen ein, wenn man aus dem Spiel ins Menue zurueckkehrt.
        nameFeld.setText(vorausgefuellterName);
        // Macht den Hintergrund transparent, damit das PNG-Schild sichtbar bleibt.
        nameFeld.setOpaque(false);
        // Verschiebt den eingegebenen Text nach rechts und etwas hoeher im Namensschild.
        nameFeld.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 96, 10, 10));
        // Fuegt das Namensfeld dem Menue hinzu.
        add(nameFeld);

        // Erstellt den Spielen-Button mit PNG-Schild.
        spielenButton = buttonErstellen("menu_sign_play.png");
        // Reagiert auf Klicks auf den Spielen-Button.
        spielenButton.addActionListener(e -> {
            // Verhindert den Start, wenn kein Name eingegeben wurde.
            if (nameFeld.getText().trim().isEmpty()) {
                // Beendet die Aktion ohne Spielstart.
                return;
            }

            // Speichert den bereinigten Spielernamen im Scoreboard.
            String spielerName = scoreboard.spielerNameSetzen(nameFeld.getText());
            // Startet das eigentliche Spiel mit diesem Namen.
            spielStarten.accept(spielerName);
        });
        // Fuegt den Spielen-Button dem Menue hinzu.
        add(spielenButton);

        // Erstellt den Scoreboard-Button mit PNG-Schild.
        scoreboardButton = buttonErstellen("menu_sign_scoreboard.png");
        // Reagiert auf Klicks auf den Scoreboard-Button.
        scoreboardButton.addActionListener(e -> {
            // Schaltet die Scoreboard-Anzeige ein oder aus.
            scoreboardAnzeigen = !scoreboardAnzeigen;
            // Setzt die Scrollposition beim Oeffnen wieder nach oben.
            scoreboardScrollIndex = 0;
            // Verwirft alte Trackpad-Restbewegung beim erneuten Oeffnen.
            scoreboardScrollRest = 0;
            // Zeichnet das Menue neu, damit die Aenderung sichtbar wird.
            repaint();
        });
        // Fuegt den Scoreboard-Button dem Menue hinzu.
        add(scoreboardButton);

        // Registriert das Mausrad fuer das Scrollen im Scoreboard.
        addMouseWheelListener(new MouseWheelListener() {
            // Reagiert auf jede Mausrad-Bewegung.
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Scrollt nur, wenn das Scoreboard sichtbar ist und die Maus ueber der Tafel steht.
                if (scoreboardAnzeigen && istMausImScoreboard(e.getX(), e.getY())) {
                    // Verschiebt die Scoreboard-Liste nach oben oder unten.
                    scoreboardScrollen(e.getPreciseWheelRotation());
                    // Verhindert, dass das Scroll-Event noch anderweitig verarbeitet wird.
                    e.consume();
                }
            }
        });

        // Beobachtet Textaenderungen im Namensfeld.
        nameFeld.getDocument().addDocumentListener(new DocumentListener() {
            // Reagiert, wenn Text eingefuegt wird.
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Aktualisiert das Spielen-Schild nach der Eingabe.
                buttonStatusAktualisieren();
            }

            // Reagiert, wenn Text geloescht wird.
            @Override
            public void removeUpdate(DocumentEvent e) {
                // Aktualisiert das Spielen-Schild nach dem Loeschen.
                buttonStatusAktualisieren();
            }

            // Reagiert auf Format-Aenderungen im Dokument.
            @Override
            public void changedUpdate(DocumentEvent e) {
                // Aktualisiert das Spielen-Schild auch bei Dokument-Aenderungen.
                buttonStatusAktualisieren();
            }
        });

        // Setzt direkt am Anfang das passende Spielen-Schild.
        buttonStatusAktualisieren();
    }

    @Override
    public void removeNotify() {
        scoreboard.aenderungsListenerEntfernen(scoreboardAenderungsListener);
        super.removeNotify();
    }

    // Positioniert alle Swing-Elemente im Menue.
    @Override
    public void doLayout() {
        // Fuehrt zuerst das Standard-Layout-Verhalten aus.
        super.doLayout();

        // Legt die Breite der Menue-Schilder fest.
        int breite = 230;
        // Zentriert die Schilder horizontal im Fenster.
        int x = (bildschirmBreite - breite) / 2;

        // Platziert das Namensfeld exakt ueber dem Name-PNG-Schild.
        nameFeld.setBounds(x, 376, breite, 44);
        // Platziert den Spielen-Button unter dem Namensschild.
        spielenButton.setBounds(x, 438, breite, 42);
        // Platziert den Scoreboard-Button unter dem Spielen-Button.
        scoreboardButton.setBounds(x, 492, breite, 42);
    }

    // Zeichnet den Hintergrund und die PNG-Oberflaechen.
    @Override
    protected void paintComponent(Graphics g) {
        // Loescht zuerst die alte Darstellung des Panels.
        super.paintComponent(g);

        // Erstellt eine Graphics2D-Kopie fuer erweitertes Zeichnen.
        Graphics2D g2 = (Graphics2D) g.create();
        // Aktiviert bilineares Skalieren fuer das grosse Hintergrundbild.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Zeichnet das Menue-Hintergrundbild.
        hintergrundZeichnen(g2);
        // Zeichnet die Menue-Tafel und das Namensschild.
        menuTafelZeichnen(g2);

        // Prueft, ob das Scoreboard angezeigt werden soll.
        if (scoreboardAnzeigen) {
            // Zeichnet die Scoreboard-Tafel und die Eintraege.
            scoreboardZeichnen(g2);
        }

        // Gibt die Graphics2D-Kopie wieder frei.
        g2.dispose();
    }

    // Erstellt einen transparenten Button mit PNG-Icon.
    private JButton buttonErstellen(String bildDatei) {
        // Erstellt einen neuen Button ohne Text.
        JButton button = new JButton();
        // Laedt das passende PNG fuer den Button.
        BufferedImage buttonBild = ladeBild(bildDatei);

        // Prueft, ob das Buttonbild geladen werden konnte.
        if (buttonBild != null) {
            // Setzt das PNG als sichtbares Button-Icon.
            button.setIcon(new ImageIcon(buttonBild));
        }

        // Entfernt die normale Swing-Button-Flaeche.
        button.setContentAreaFilled(false);
        // Entfernt den Fokus-Rahmen.
        button.setFocusPainted(false);
        // Entfernt den Standard-Rahmen.
        button.setBorderPainted(false);
        // Macht den Button selbst transparent.
        button.setOpaque(false);
        // Gibt den fertig konfigurierten Button zurueck.
        return button;
    }

    // Aktualisiert das Spielen-Schild je nach Namenseingabe.
    private void buttonStatusAktualisieren() {
        // Prueft, ob ein nicht-leerer Name eingegeben wurde.
        boolean nameVorhanden = !nameFeld.getText().trim().isEmpty();

        // Zeigt das normale Spielen-Schild, wenn ein Name vorhanden ist.
        if (nameVorhanden && spielenBild != null) {
            // Setzt das normale Spielen-PNG.
            spielenButton.setIcon(new ImageIcon(spielenBild));
        // Zeigt das gesperrte Spielen-Schild, wenn noch kein Name vorhanden ist.
        } else if (!nameVorhanden && spielenGesperrtBild != null) {
            // Setzt das Spielen-PNG mit minenhaftem Kreuz.
            spielenButton.setIcon(new ImageIcon(spielenGesperrtBild));
        }
    }

    // Zeichnet das grosse Menue-Hintergrundbild bildschirmfuellend.
    private void hintergrundZeichnen(Graphics2D g2) {
        // Prueft, ob das Hintergrundbild fehlt.
        if (menuHintergrund == null) {
            // Setzt eine dunkle Fallback-Farbe.
            g2.setColor(new Color(12, 14, 17));
            // Fuellt das komplette Panel mit der Fallback-Farbe.
            g2.fillRect(0, 0, bildschirmBreite, bildschirmHoehe);
            // Beendet die Methode, weil kein Bild gezeichnet werden kann.
            return;
        }

        // Berechnet die Skalierung, damit das Bild den ganzen Bildschirm fuellt.
        double skalierung = Math.max(
                bildschirmBreite / (double) menuHintergrund.getWidth(),
                bildschirmHoehe / (double) menuHintergrund.getHeight()
        );
        // Berechnet die skalierte Bildbreite.
        int breite = (int) Math.round(menuHintergrund.getWidth() * skalierung);
        // Berechnet die skalierte Bildhoehe.
        int hoehe = (int) Math.round(menuHintergrund.getHeight() * skalierung);
        // Berechnet die horizontale Position fuer zentriertes Zeichnen.
        int x = (bildschirmBreite - breite) / 2;
        // Berechnet die vertikale Position fuer zentriertes Zeichnen.
        int y = (bildschirmHoehe - hoehe) / 2;

        // Zeichnet das skalierte Hintergrundbild.
        g2.drawImage(menuHintergrund, x, y, breite, hoehe, null);
    }

    // Zeichnet die Menue-Tafel und das Namensschild aus PNG-Dateien.
    private void menuTafelZeichnen(Graphics2D g2) {
        // Prueft, ob das grosse Menue-Panel geladen wurde.
        if (menuTafelBild != null) {
            // Zeichnet die transparente Minen-Tafel hinter den Bedienelementen.
            g2.drawImage(menuTafelBild, 50, 305, 300, 260, null);
        }

        // Prueft, ob das Namensschild geladen wurde.
        if (nameSchildBild != null) {
            // Zeichnet das Namensschild hinter dem transparenten Textfeld.
            g2.drawImage(nameSchildBild, 85, 376, 230, 44, null);
        }
    }

    // Zeichnet die Scoreboard-Tafel und die besten Scores.
    private void scoreboardZeichnen(Graphics2D g2) {
        // Prueft, ob die Scoreboard-Tafel geladen wurde.
        if (scoreboardTafelBild != null) {
            // Zeichnet die halbtransparente Scoreboard-Tafel.
            g2.drawImage(scoreboardTafelBild, 38, 125, 324, 200, null);
        }

        // Setzt die Ueberschrift-Schrift fuer das Scoreboard.
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        // Setzt die Textfarbe fuer das Scoreboard.
        g2.setColor(new Color(246, 218, 152));
        // Zeichnet die Scoreboard-Ueberschrift zentriert.
        zentriertenTextZeichnen(g2, "Scoreboard", 158);

        // Holt eine Kopie aller gespeicherten Scoreboard-Eintraege.
        List<ScoreEintrag> eintraege = scoreboard.getEintraege();
        // Klemmt die Scrollposition, falls sich die Anzahl der Eintraege geaendert hat.
        scoreboardScrollIndex = scrollIndexBegrenzen(scoreboardScrollIndex, eintraege.size());
        // Setzt die Schrift fuer die einzelnen Scoreboard-Zeilen.
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));

        // Prueft, ob noch keine Scores gespeichert sind.
        if (eintraege.isEmpty()) {
            // Zeichnet einen Hinweis, wenn es noch keine Scores gibt.
            zentriertenTextZeichnen(g2, "Noch keine Scores", 205);
            // Beendet die Methode, weil keine Eintraege gezeichnet werden koennen.
            return;
        }

        // Begrenz die Anzeige auf die sichtbaren fuenf Eintraege.
        int maxAnzahl = Math.max(0, Math.min(5, eintraege.size() - scoreboardScrollIndex));
        // Zeichnet jeden sichtbaren Scoreboard-Eintrag.
        for (int i = 0; i < maxAnzahl; i++) {
            // Berechnet den echten Index in der gesamten Scoreboard-Liste.
            int eintragIndex = scoreboardScrollIndex + i;
            // Holt den aktuellen Eintrag aus der Liste.
            ScoreEintrag eintrag = eintraege.get(eintragIndex);
            // Berechnet die y-Position der aktuellen Zeile.
            int y = 193 + i * 24;

            // Zeichnet die Platznummer.
            g2.drawString((eintragIndex + 1) + ".", 76, y);
            // Zeichnet den Spielernamen.
            g2.drawString(eintrag.getSpielerName(), 112, y);
            // Zeichnet den Score rechtsbuendig.
            rechtsbuendigenTextZeichnen(g2, String.valueOf(eintrag.getScore()), 320, y);
        }
    }


    // Verschiebt die Scoreboard-Anzeige per Mausrad.
    private void scoreboardScrollen(double richtung) {
        // Holt die aktuelle Anzahl gespeicherter Scoreboard-Eintraege.
        int anzahlEintraege = scoreboard.getEintraege().size();
        // Merkt sich die bisherige Scrollposition.
        int alterScrollIndex = scoreboardScrollIndex;

        // Sammelt feine Trackpad-Bewegungen, damit die Zeilen nicht zittern.
        scoreboardScrollRest += richtung;

        // Wandelt die gesammelte Bewegung in ganze Zeilenschritte um.
        int scrollSchritt = (int) scoreboardScrollRest;

        // Wartet, bis mindestens eine ganze Zeile erreicht ist.
        if (scrollSchritt == 0) {
            return;
        }

        // Behaelt nur den noch nicht verbrauchten Scroll-Rest.
        scoreboardScrollRest -= scrollSchritt;
        // Verschiebt die Scrollposition innerhalb der erlaubten Grenzen.
        scoreboardScrollIndex = scrollIndexBegrenzen(scoreboardScrollIndex + scrollSchritt, anzahlEintraege);

        // Zeichnet nur neu, wenn sich die sichtbaren Zeilen wirklich geaendert haben.
        if (scoreboardScrollIndex != alterScrollIndex) {
            repaint();
        }
    }

    // Prueft, ob die Maus ueber der Scoreboard-Tafel steht.
    private boolean istMausImScoreboard(int x, int y) {
        // Gibt nur fuer den sichtbaren Scoreboard-Kasten true zurueck.
        return x >= 38 && x <= 362 && y >= 125 && y <= 330;
    }

    // Klemmt die Scrollposition in den erlaubten Bereich.
    private int scrollIndexBegrenzen(int scrollIndex, int anzahlEintraege) {
        // Berechnet die letzte moegliche Startposition fuer fuenf sichtbare Zeilen.
        int maxScrollIndex = Math.max(0, anzahlEintraege - 5);
        // Gibt die geklemmte Scrollposition zurueck.
        return Math.max(0, Math.min(maxScrollIndex, scrollIndex));
    }

    // Zeichnet einen Text horizontal zentriert.
    private void zentriertenTextZeichnen(Graphics g, String text, int y) {
        // Holt Messdaten der aktuellen Schrift.
        FontMetrics metrics = g.getFontMetrics();
        // Berechnet die x-Position fuer zentrierten Text.
        int x = (bildschirmBreite - metrics.stringWidth(text)) / 2;
        // Zeichnet den Text an der berechneten Position.
        g.drawString(text, x, y);
    }

    // Zeichnet einen Text rechtsbuendig an einem rechten Rand.
    private void rechtsbuendigenTextZeichnen(Graphics g, String text, int rechterRand, int y) {
        // Holt Messdaten der aktuellen Schrift.
        FontMetrics metrics = g.getFontMetrics();
        // Zeichnet den Text so, dass er am rechten Rand endet.
        g.drawString(text, rechterRand - metrics.stringWidth(text), y);
    }

    // Laedt ein Bild zuerst aus den Ressourcen und dann direkt aus dem Projektordner.
    private static BufferedImage ladeBild(String dateiname) {
        // Sucht das Bild im Java-Klassenpfad.
        URL bildUrl = MenuPanel.class.getResource("/game/assets/" + dateiname);

        // Versucht, das Bild zu laden.
        try {
            // Prueft, ob das Bild im Klassenpfad gefunden wurde.
            if (bildUrl != null) {
                // Laedt das Bild aus dem Klassenpfad.
                return ImageIO.read(bildUrl);
            }

            // Laedt das Bild als Fallback direkt aus dem src-Ordner.
            return ImageIO.read(new File("src/game/assets/" + dateiname));
        // Faengt Ladefehler ab.
        } catch (IOException e) {
            // Gibt null zurueck, damit der Aufrufer einen Fallback nutzen kann.
            return null;
        }
    }
}
