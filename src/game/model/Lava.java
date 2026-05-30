// Beschreibt die Lava im Spiel.
// Die Lava bleibt grundsätzlich im unteren Bildschirmbereich.
// Wenn der Spieler zu lange stehen bleibt, steigt sie langsam nach oben.
package game.model;

import java.awt.Color;
import java.awt.Graphics;

public class Lava {

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

        // Sehr langsam, damit die Lava nur Druck macht, wenn man zu lange stehen bleibt.
        this.lavaGeschwindigkeit = 0.08;
    }

    public void aktualisieren() {
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
        int lavaY = (int) y;
        int lavaHoehe = bildschirmHoehe - lavaY;

        g.setColor(Color.RED);
        g.fillRect(x, lavaY, breite, lavaHoehe);

        g.setColor(Color.ORANGE);
        g.fillRect(x, lavaY, breite, 8);
    }

    public int getY() {
        return (int) y;
    }

    public int getHeight() {
        return bildschirmHoehe - (int) y;
    }
}