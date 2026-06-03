// Beschreibt die Lava im Spiel.
// Die Lava bleibt grundsätzlich im unteren Bildschirmbereich.
// Wenn der Spieler zu lange stehen bleibt, steigt sie langsam nach oben.
package game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;

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
        Graphics2D g2 = (Graphics2D) g.create();
        int lavaY = (int) y;
        int lavaHoehe = bildschirmHoehe - lavaY;

        GradientPaint lavaVerlauf = new GradientPaint(
                x, lavaY, new Color(255, 210, 54),
                x, bildschirmHoehe, new Color(128, 18, 0)
        );
        g2.setPaint(lavaVerlauf);
        g2.fillRect(x, lavaY, breite, lavaHoehe);

        g2.setColor(new Color(255, 238, 98));
        for (int i = -20; i < breite + 30; i += 34) {
            int wellenHoehe = (i / 34) % 2 == 0 ? 4 : 9;
            g2.fillRect(x + i, lavaY + wellenHoehe, 24, 4);
        }

        g2.setColor(new Color(255, 118, 12));
        for (int i = 0; i < breite; i += 46) {
            g2.fillRect(x + i + 8, lavaY + 18, 28, 5);
            g2.fillRect(x + i + 18, lavaY + 31, 16, 4);
        }

        g2.setColor(new Color(92, 12, 0));
        for (int i = 0; i < breite; i += 58) {
            g2.fillRect(x + i + 4, lavaY + 43, 34, 6);
            g2.fillRect(x + i + 27, lavaY + 55, 18, 5);
        }

        g2.setColor(new Color(255, 245, 137));
        g2.fillRect(x, lavaY, breite, 3);
        g2.setColor(new Color(180, 32, 0));
        g2.fillRect(x, lavaY + 7, breite, 3);

        g2.dispose();
    }

    public int getY() {
        return (int) y;
    }

    public int getHeight() {
        return bildschirmHoehe - (int) y;
    }
}
