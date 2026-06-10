// Beschreibt eine einzelne Plattform.
// Speichert Position, Größe und Plattformtyp.
// Brüchige Plattformen können nach dem Betreten verschwinden.
package game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class Platform {

    private static final BufferedImage holzBild = ladeBild("platform_wood.png");
    private static final BufferedImage steinBild = ladeBild("platform_stone.png");
    private static final BufferedImage trampolinBild = ladeBild("platform_trampoline.png");

    private int x;
    private int y;
    private int breite;
    private int hoehe;

    private PlatformType plattformTyp;

    private boolean wurdeBeruehrt;
    private int zerbrechenZaehler;

    public Platform(int x, int y, PlatformType plattformTyp) {
        this.x = x;
        this.y = y;
        this.breite = 100;
        this.hoehe = 15;
        this.plattformTyp = plattformTyp;

        this.wurdeBeruehrt = false;
        this.zerbrechenZaehler = 0;
    }

    public void aktualisieren() {
        if (plattformTyp == PlatformType.BRUECHIG && wurdeBeruehrt) {
            zerbrechenZaehler++;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        if (plattformTyp == PlatformType.NORMAL) {
            plattformBildZeichnen(g2, steinBild, new Color(70, 72, 72));
        } else if (plattformTyp == PlatformType.BRUECHIG) {
            plattformBildZeichnen(g2, holzBild, new Color(122, 76, 34));
            if (wurdeBeruehrt) {
                holzRisseZeichnen(g2);
            }
        } else if (plattformTyp == PlatformType.TRAMPOLIN) {
            plattformBildZeichnen(g2, trampolinBild, new Color(130, 60, 30));
        }

        g2.dispose();
    }

    private void plattformBildZeichnen(Graphics2D g2, BufferedImage bild, Color fallbackFarbe) {
        if (bild == null) {
            g2.setColor(fallbackFarbe);
            g2.fillRect(x, y, breite, hoehe);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, breite, hoehe);
            return;
        }

        g2.drawImage(bild, x - 2, y - 1, breite + 4, hoehe + 6, null);
    }

    private void holzRisseZeichnen(Graphics2D g2) {
        g2.setColor(new Color(9, 6, 4));
        g2.drawLine(x + 28, y + 1, x + 38, y + hoehe + 2);
        g2.drawLine(x + 38, y + 8, x + 45, y + 3);
        g2.drawLine(x + 68, y + 1, x + 80, y + hoehe + 2);
        g2.drawLine(x + 80, y + 10, x + 88, y + 6);
    }

    private static BufferedImage ladeBild(String dateiname) {
        URL bildUrl = Platform.class.getResource("/game/assets/" + dateiname);

        try {
            if (bildUrl != null) {
                return ImageIO.read(bildUrl);
            }

            return ImageIO.read(new File("src/game/assets/" + dateiname));
        } catch (IOException e) {
            return null;
        }
    }

    public void beruehren() {
        if (plattformTyp == PlatformType.BRUECHIG) {
            wurdeBeruehrt = true;
        }
    }

    public boolean sollEntferntWerden() {
        return plattformTyp == PlatformType.BRUECHIG && zerbrechenZaehler > 100;
    }

    public void bewegeNachUnten(int distanz) {
        y += distanz;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return breite;
    }

    public int getHeight() {
        return hoehe;
    }

    public PlatformType getPlattformTyp() {
        return plattformTyp;
    }
}
