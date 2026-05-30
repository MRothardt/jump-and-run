// Beschreibt den Spieler.
// Speichert Position, Größe und Geschwindigkeit.
// Enthält Bewegung, Springen, Gravitation und das Zeichnen des Spielers.
package game.model;

import java.awt.Color;
import java.awt.Graphics;

public class Player {

    private int x;
    private int y;
    private int width;
    private int height;

    private int velocityX;
    private int velocityY;

    private boolean onGround;

    private final int speed = 5;
    private final int jumpStrength = -15;
    private final int gravity = 1;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 40;
        this.height = 40;
        this.onGround = false;
    }

    public void update(boolean leftPressed, boolean rightPressed, boolean spacePressed) {
        velocityX = 0;

        if (leftPressed) {
            velocityX = -speed;
        }

        if (rightPressed) {
            velocityX = speed;
        }

        if (spacePressed && onGround) {
            velocityY = jumpStrength;
            onGround = false;
        }

        velocityY += gravity;

        x += velocityX;
        y += velocityY;

        if (x < 0) {
            x = 0;
        }

        if (x > 360) {
            x = 360;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }

    public void landOnPlatform(int platformY) {
        y = platformY - height;
        velocityY = 0;
        onGround = true;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getVelocityY() {
        return velocityY;
    }
}