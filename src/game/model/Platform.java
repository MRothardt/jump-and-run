// Beschreibt eine einzelne Plattform.
// Speichert Position, Größe und Plattformtyp.
// Brüchige Plattformen können nach dem Betreten verschwinden.
package game.model;

import java.awt.Color;
import java.awt.Graphics;

public class Platform {

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
        if (plattformTyp == PlatformType.NORMAL) {
            g.setColor(new Color(100, 100, 100));
        } else if (plattformTyp == PlatformType.BRUECHIG) {
            g.setColor(new Color(120, 70, 40));
        } else if (plattformTyp == PlatformType.KRISTALL) {
            g.setColor(new Color(0, 180, 220));
        }

        g.fillRect(x, y, breite, hoehe);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, breite, hoehe);

        if (plattformTyp == PlatformType.BRUECHIG && wurdeBeruehrt) {
            g.setColor(Color.BLACK);
            g.drawLine(x + 20, y, x + 35, y + hoehe);
            g.drawLine(x + 55, y, x + 70, y + hoehe);
        }
    }

    public void beruehren() {
        if (plattformTyp == PlatformType.BRUECHIG) {
            wurdeBeruehrt = true;
        }
    }

    public boolean sollEntferntWerden() {
        return plattformTyp == PlatformType.BRUECHIG && zerbrechenZaehler > 25;
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