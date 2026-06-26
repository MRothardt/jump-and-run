// Beschreibt die Lava im Spiel.
// Die Lava bleibt grundsätzlich im unteren Bildschirmbereich.
// Sie steigt dauerhaft langsam nach oben, damit der Spieler in Bewegung bleiben muss.
package game.model;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class Lava {

    private static final BufferedImage lavaOberflaecheBild = ladeBild("lava_surface.png");
    private static final double START_GESCHWINDIGKEIT = 0.20;
    private static final double MAX_GESCHWINDIGKEIT = 0.35;
    private static final double GESCHWINDIGKEIT_PRO_STUFE = 0.03;
    private static final int SCORE_PRO_STUFE = 5000;

    private int x;
    private double y;
    private int breite;
    private int bildschirmHoehe;

    private double lavaGeschwindigkeit;

    public Lava(int bildschirmBreite, int bildschirmHoehe) {
        this.x = 0;
        this.breite = bildschirmBreite;
        this.bildschirmHoehe = bildschirmHoehe;

        this.y = bildschirmHoehe - 40;

        // Moderat schnell, damit die Lava Druck macht, der Spieler aber noch fluechten kann.
        this.lavaGeschwindigkeit = START_GESCHWINDIGKEIT;
    }

    public void aktualisieren(int punktzahl) {
        int stufe = punktzahl / SCORE_PRO_STUFE;
        lavaGeschwindigkeit = Math.min(
                MAX_GESCHWINDIGKEIT,
                START_GESCHWINDIGKEIT + stufe * GESCHWINDIGKEIT_PRO_STUFE
        );

        y -= lavaGeschwindigkeit;
    }

    public void nachUntenDruecken(int distanz) {
        y += distanz;

        int tiefsteErlaubtePosition = bildschirmHoehe - 40;

        if (y > tiefsteErlaubtePosition) {
            y = tiefsteErlaubtePosition;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Nearest-neighbor erhaelt den bewusst pixeligen Look des Lava-PNGs.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int lavaY = (int) y;
        int lavaHoehe = bildschirmHoehe - lavaY;

        if (lavaOberflaecheBild == null) {
            lavaFallbackZeichnen(g2, lavaY, lavaHoehe);
            g2.dispose();
            return;
        }

        // Die Lava ist absichtlich ein statisches grosses PNG.
        // Das Bild enthaelt die Oberflaeche und die komplette Tiefe darunter,
        // damit kein sichtbarer Bruch zwischen Surface und Fill entsteht.
        int lavaBildY = lavaY;
        int lavaBildHoehe = Math.max(lavaOberflaecheBild.getHeight(), bildschirmHoehe - lavaBildY);

        g2.drawImage(
                lavaOberflaecheBild,
                x,
                lavaBildY,
                breite,
                lavaBildHoehe,
                null
        );

        g2.dispose();
    }

    private void lavaFallbackZeichnen(Graphics2D g2, int lavaY, int lavaHoehe) {
        g2.setColor(new java.awt.Color(145, 34, 9));
        g2.fillRect(x, lavaY, breite, lavaHoehe);
        g2.setColor(new java.awt.Color(48, 13, 8));
        g2.fillRect(x, lavaY, breite, 4);
    }

    private static BufferedImage ladeBild(String dateiname) {
        URL bildUrl = Lava.class.getResource("/game/assets/" + dateiname);

        try {
            if (bildUrl != null) {
                return ImageIO.read(bildUrl);
            }

            return ImageIO.read(new File("src/game/assets/" + dateiname));
        } catch (IOException e) {
            return null;
        }
    }

    public int getY() {
        return (int) y;
    }
}
