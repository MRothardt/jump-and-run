// Beschreibt die Lava im Spiel.
// Die Lava ist die Gefahr am unteren Bildschirmrand.
// Wenn der Spieler die Lava berührt oder zu tief fällt, ist das Spiel vorbei.
package game.model;

import java.awt.Color;
import java.awt.Graphics;

public class Lava {

    private int x;
    private int y;
    private int width;
    private int height;

    private int zaehler;
    private int bewegungIntervall;

    public Lava(int bildschirmBreite, int bildschirmHoehe) {
        this.x = 0;
        this.height = 40;
        this.width = bildschirmBreite;
        this.y = bildschirmHoehe - height;

        this.zaehler = 0;
        this.bewegungIntervall = 20; // Je höher die Zahl, desto langsamer steigt die Lava
    }

    public void aktualisieren() {
        zaehler++;

        if (zaehler >= bewegungIntervall) {
            y = y - 1;
            zaehler = 0;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);

        g.setColor(Color.ORANGE);
        g.fillRect(x, y, width, 8);
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }
}