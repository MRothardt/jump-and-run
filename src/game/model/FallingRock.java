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

public class FallingRock {

    private static final BufferedImage steinBild = ladeBild("falling_rock.png");
    private static final BufferedImage warnBild = ladeBild("falling_rock_warning.png");

    private int x;
    private int y;
    private int breite;
    private int hoehe;
    private int alter;
    private int warnDauer;
    private final int fallGeschwindigkeit;

    public FallingRock(int x) {
        this.x = x;
        this.y = -48;
        this.breite = 48;
        this.hoehe = 48;
        this.alter = 0;
        // Die Warnung bleibt lang genug sichtbar, damit der Spieler ausweichen kann.
        this.warnDauer = 85;
        // Konstante Fallgeschwindigkeit: Der Stein wird nicht mit der Zeit schneller.
        this.fallGeschwindigkeit = 5;
    }

    public void aktualisieren() {
        alter++;

        if (!istInWarnphase()) {
            // Die Fallgeschwindigkeit ist absichtlich nur dieser feste Wert.
            // Kamera-Scroll oder Spielergeschwindigkeit duerfen den Stein nicht beschleunigen.
            y += fallGeschwindigkeit;
        }
    }

    public void draw(Graphics g) {
        if (istInWarnphase()) {
            warnungZeichnen(g);
            return;
        }

        steinZeichnen(g);
    }

    private void warnungZeichnen(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int warnBreite = 40;
        int warnHoehe = 40;
        int warnX = x + breite / 2 - warnBreite / 2;
        int warnY = 6;

        if (warnBild != null) {
            g2.drawImage(warnBild, warnX, warnY, warnBreite, warnHoehe, null);
        } else {
            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(warnX + 10, warnY + 8, warnBreite - 20, 4);
            g2.setColor(new Color(160, 120, 65));
            g2.fillRect(x + breite / 2 - 4, warnY + 18, 8, 8);
        }

        g2.dispose();
    }

    private void steinZeichnen(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (steinBild != null) {
            g2.drawImage(steinBild, x, y, breite, hoehe, null);
        } else {
            g2.setColor(new Color(80, 84, 82));
            g2.fillRect(x, y, breite, hoehe);
            g2.setColor(new Color(30, 32, 32));
            g2.drawRect(x, y, breite - 1, hoehe - 1);
        }

        g2.dispose();
    }

    public boolean trifft(Player spieler) {
        if (istInWarnphase()) {
            return false;
        }

        boolean horizontal =
                spieler.getX() + spieler.getWidth() > x
                        && spieler.getX() < x + breite;
        boolean vertikal =
                spieler.getY() + spieler.getHeight() > y
                        && spieler.getY() < y + hoehe;

        if (!horizontal || !vertikal) {
            return false;
        }

        if (steinBild == null) {
            return true;
        }

        return trifftSichtbareTextur(spieler);
    }

    private boolean trifftSichtbareTextur(Player spieler) {
        int links = Math.max(spieler.getX(), x);
        int rechts = Math.min(spieler.getX() + spieler.getWidth(), x + breite);
        int oben = Math.max(spieler.getY(), y);
        int unten = Math.min(spieler.getY() + spieler.getHeight(), y + hoehe);

        for (int pruefY = oben; pruefY < unten; pruefY += 3) {
            for (int pruefX = links; pruefX < rechts; pruefX += 3) {
                int bildX = (pruefX - x) * steinBild.getWidth() / breite;
                int bildY = (pruefY - y) * steinBild.getHeight() / hoehe;

                if (bildX >= 0
                        && bildX < steinBild.getWidth()
                        && bildY >= 0
                        && bildY < steinBild.getHeight()
                        && ((steinBild.getRGB(bildX, bildY) >>> 24) > 40)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean istEntfernt() {
        return y > 650;
    }

    private boolean istInWarnphase() {
        return alter < warnDauer;
    }

    private static BufferedImage ladeBild(String dateiname) {
        URL bildUrl = FallingRock.class.getResource("/game/assets/" + dateiname);

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
