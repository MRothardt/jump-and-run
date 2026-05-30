// Beschreibt den Spieler.
// Speichert Position, Größe und Geschwindigkeit.
// Enthält Bewegung, Springen, Gravitation und das Zeichnen des Spielers.
package game.model;

import java.awt.Color;
import java.awt.Graphics;

public class Player {

    private int x;
    private int y;
    private int breite;
    private int hoehe;

    private int geschwindigkeitX;
    private int geschwindigkeitY;

    private boolean stehtAufBoden;

    private final int laufGeschwindigkeit = 5;
    private final int sprungKraft = -15;
    private final int gravitation = 1;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.breite = 40;
        this.hoehe = 40;
        this.stehtAufBoden = false;
    }

    public void update(boolean linksGedrueckt, boolean rechtsGedrueckt, boolean springenGedrueckt) {
        geschwindigkeitX = 0;

        if (linksGedrueckt) {
            geschwindigkeitX = -laufGeschwindigkeit;
        }

        if (rechtsGedrueckt) {
            geschwindigkeitX = laufGeschwindigkeit;
        }

        if (springenGedrueckt && stehtAufBoden) {
            geschwindigkeitY = sprungKraft;
            stehtAufBoden = false;
        }

        geschwindigkeitY += gravitation;

        x += geschwindigkeitX;
        y += geschwindigkeitY;

        if (x < 0) {
            x = 0;
        }

        if (x > 360) {
            x = 360;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, breite, hoehe);
    }

    public void landOnPlatform(int plattformY) {
        y = plattformY - hoehe;
        geschwindigkeitY = 0;
        stehtAufBoden = true;
    }

    public void setOnGround(boolean stehtAufBoden) {
        this.stehtAufBoden = stehtAufBoden;
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

    public int getVelocityY() {
        return geschwindigkeitY;
    }
}