package game.model;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class LavaBurst {

    private static final BufferedImage lavaStossBild = ladeBild("lava_burst.png");

    private int x;
    private int basisY;
    private int breite;
    private int alter;
    private int lebensdauer;
    private int maximaleHoehe;
    private int startUnterDerLava;
    private int warnDauer;
    private int schussDauer;

    public LavaBurst(int x, int basisY) {
        this.x = x;
        this.basisY = basisY;
        this.breite = 48;
        this.alter = 0;
        // Vor dem Schuss sieht der Spieler an der Lava-Oberflaeche,
        // wo der Burst gleich herauskommt.
        this.warnDauer = 105;
        // Der Schuss geht langsamer nach oben, damit man noch reagieren kann.
        this.schussDauer = 34;
        // Der Burst bleibt an seiner Spawn-Stelle und spritzt dafuer hoeher.
        this.lebensdauer = 145 + warnDauer;
        this.maximaleHoehe = 580;
        // Der Burst startet optisch hinter der Lava Surface und schiesst dann heraus.
        this.startUnterDerLava = 42;
    }

    public void aktualisieren() {
        alter++;
    }

    public void bewegeNachUnten(int distanz) {
        if (istInWarnphase()) {
            // Waehrend der Voranimation bleibt die Warnung auf dem Bildschirm beim Spieler.
            // Der eigentliche Weltpunkt wird erst fest, wenn der echte Burst auftaucht.
            return;
        }

        // Sobald der echte Burst auftaucht, bleibt er fest in der Spielwelt.
        // Beim Hochspringen des Spielers wandert er dann so schnell nach unten,
        // wie der Spieler durch den Kamera-Scroll nach oben kommt.
        basisY += distanz;
    }

    public void draw(Graphics g) {
        if (lavaStossBild == null || getHoehe() <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        // Nearest-neighbor passt zur pixeligen Lava-Grafik und vermeidet verwaschene Kanten.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int hoehe = getHoehe();
        int unten = getUnten();
        g2.drawImage(lavaStossBild, x, unten - hoehe, breite, hoehe, null);
        g2.dispose();
    }

    public void warnungZeichnen(Graphics g, int lavaY) {
        if (!istInWarnphase()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int warnFortschritt = alter % 30;
        int warnBreite = 30 + warnFortschritt / 2;
        int vorBurstHoehe = 24 + warnFortschritt;
        int mitteX = x + breite / 2;
        int warnY = lavaY + 3;

        // Das Warnsignal liegt auf der Lava-Oberflaeche und zeigt die Austrittsstelle.
        // Die Warnung folgt der aktuellen Lava-Oberflaeche,
        // waehrend der eigentliche Burst an seiner festen Weltposition bleibt.
        // Es ist bewusst einfach gehalten, damit es lesbar bleibt und nicht wie ein Hindernis wirkt.
        g2.setColor(new Color(255, 190, 54, 160));
        g2.fillRect(mitteX - warnBreite / 2, warnY, warnBreite, 4);
        g2.setColor(new Color(150, 28, 8, 190));
        g2.fillRect(mitteX - warnBreite / 2 - 4, warnY + 5, warnBreite + 8, 4);

        // Kleiner Vor-Burst als deutliche Warnung. Er sieht gefaehrlich aus,
        // ist aber in der Warnphase noch nicht toedlich.
        g2.setColor(new Color(255, 190, 58, 215));
        g2.fillRect(mitteX - 6, warnY - vorBurstHoehe, 12, vorBurstHoehe);
        g2.setColor(new Color(183, 39, 8, 205));
        g2.fillRect(mitteX - 11, warnY - vorBurstHoehe / 2, 5, vorBurstHoehe / 2);
        g2.fillRect(mitteX + 6, warnY - vorBurstHoehe / 2 - 5, 6, vorBurstHoehe / 2 + 5);
        g2.setColor(new Color(255, 225, 92, 190));
        g2.fillRect(mitteX - 3, warnY - vorBurstHoehe - 8, 6, 8);
        g2.setColor(new Color(255, 89, 15, 135));
        g2.fillRect(mitteX - 4, warnY - 5, 8, 4);

        g2.dispose();
    }

    public boolean trifft(Player spieler) {
        if (!istGefaehrlich()) {
            return false;
        }

        int hoehe = getHoehe();
        int unten = getUnten();
        int oben = unten - hoehe;
        boolean horizontal =
                spieler.getX() + spieler.getWidth() > x
                        && spieler.getX() < x + breite;
        boolean vertikal =
                spieler.getY() + spieler.getHeight() > oben
                        && spieler.getY() < unten;

        if (!horizontal || !vertikal) {
            return false;
        }

        // Danach wird nicht nur das Rechteck geprueft, sondern ob der Spieler
        // wirklich sichtbare Pixel der Lava-Textur beruehrt.
        return trifftSichtbareTextur(spieler, oben, hoehe);
    }

    public boolean istAbgelaufen() {
        return alter > lebensdauer;
    }

    private boolean istGefaehrlich() {
        // Ein kleiner Vorlauf und Auslauf verhindert unfaire Treffer beim Ein- und Ausblenden.
        return alter > warnDauer + 2 && alter < lebensdauer - 18;
    }

    private int getHoehe() {
        int schussAlter = alter - warnDauer;

        if (schussAlter <= 0) {
            return 0;
        }

        if (schussAlter < schussDauer) {
            return schussAlter * maximaleHoehe / schussDauer;
        }

        if (alter > lebensdauer - 22) {
            int rest = lebensdauer - alter;
            return Math.max(0, rest * maximaleHoehe / 22);
        }

        return maximaleHoehe;
    }

    private boolean istInWarnphase() {
        return alter < warnDauer;
    }

    private boolean istImAufbau() {
        int schussAlter = alter - warnDauer;
        return schussAlter > 0 && schussAlter < schussDauer;
    }

    private int getUnten() {
        return basisY + startUnterDerLava;
    }

    private boolean trifftSichtbareTextur(Player spieler, int oben, int hoehe) {
        int links = Math.max(spieler.getX(), x);
        int rechts = Math.min(spieler.getX() + spieler.getWidth(), x + breite);
        int obenSchnitt = Math.max(spieler.getY(), oben);
        int untenSchnitt = Math.min(spieler.getY() + spieler.getHeight(), oben + hoehe);

        for (int pruefY = obenSchnitt; pruefY < untenSchnitt; pruefY += 3) {
            for (int pruefX = links; pruefX < rechts; pruefX += 3) {
                int bildX = (pruefX - x) * lavaStossBild.getWidth() / breite;
                int bildY = (pruefY - oben) * lavaStossBild.getHeight() / hoehe;

                if (bildX >= 0
                        && bildX < lavaStossBild.getWidth()
                        && bildY >= 0
                        && bildY < lavaStossBild.getHeight()
                        && ((lavaStossBild.getRGB(bildX, bildY) >>> 24) > 40)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static BufferedImage ladeBild(String dateiname) {
        URL bildUrl = LavaBurst.class.getResource("/game/assets/" + dateiname);

        try {
            if (bildUrl != null) {
                return ImageIO.read(bildUrl);
            }

            return ImageIO.read(new File("src/game/assets/" + dateiname));
        } catch (IOException e) {
            return null;
        }
    }
}
