package game.model;

import java.awt.Color;
import java.awt.Graphics;

public class Player {

    private int x;
    private int y;
    private int width;
    private int height;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 40;
        this.height = 40;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
}