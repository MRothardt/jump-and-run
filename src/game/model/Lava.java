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

    public Lava(int screenWidth, int screenHeight) {
        this.x = 0;
        this.height = 40;
        this.width = screenWidth;
        this.y = screenHeight - height;
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