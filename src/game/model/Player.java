// Beschreibt den Spieler.
// Speichert Position, Größe und Geschwindigkeit.
// Enthält Bewegung, Springen, Gravitation und das Zeichnen des Spielers.
package game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Player {

    private int x;
    private int y;
    private int breite;
    private int hoehe;

    private int geschwindigkeitX;
    private int geschwindigkeitY;

    private boolean stehtAufBoden;
    private boolean schautNachRechts;

    private final int bildschirmBreite = 400;
    private final int laufGeschwindigkeit = 5;
    private final int sprungKraft = -15;
    private final int trampolinSprungKraft = sprungKraft * 3;
    private final int gravitation = 1;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.breite = 40;
        this.hoehe = 40;
        this.stehtAufBoden = false;
        this.schautNachRechts = true;
    }

    public void update(boolean linksGedrueckt, boolean rechtsGedrueckt, boolean springenGedrueckt) {
        geschwindigkeitX = 0;

        if (linksGedrueckt) {
            geschwindigkeitX = -laufGeschwindigkeit;
            schautNachRechts = true;
        }

        if (rechtsGedrueckt) {
            geschwindigkeitX = laufGeschwindigkeit;
            schautNachRechts = false;
        }

        if (springenGedrueckt && stehtAufBoden) {
            geschwindigkeitY = sprungKraft;
            stehtAufBoden = false;
        }

        geschwindigkeitY += gravitation;

        x += geschwindigkeitX;
        y += geschwindigkeitY;

        if (x + breite < 0) {
            x = bildschirmBreite;
        }

        if (x > bildschirmBreite) {
            x = -breite;
        }
    }

    public void draw(Graphics g) {
        zeichneMinenarbeiterMitRichtung(g, x, y);

        if (x < 0) {
            zeichneMinenarbeiterMitRichtung(g, x + bildschirmBreite, y);
        }

        if (x + breite > bildschirmBreite) {
            zeichneMinenarbeiterMitRichtung(g, x - bildschirmBreite, y);
        }
    }

    private void zeichneMinenarbeiterMitRichtung(Graphics g, int zeichneX, int zeichneY) {
        if (schautNachRechts) {
            zeichneMinenarbeiter(g, zeichneX, zeichneY);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(zeichneX + breite, zeichneY);
        g2.scale(-1, 1);
        zeichneMinenarbeiter(g2, 0, 0);
        g2.dispose();
    }

    private void zeichneMinenarbeiter(Graphics g, int zeichneX, int zeichneY) {
        Color hautFarbe = new Color(232, 164, 104);
        Color helmFarbe = new Color(238, 192, 42);
        Color helmSchatten = new Color(170, 120, 25);
        Color hemdFarbe = new Color(42, 105, 172);
        Color hoseFarbe = new Color(62, 48, 42);
        Color schuhFarbe = new Color(30, 24, 20);
        Color metallFarbe = new Color(180, 190, 190);
        Color holzFarbe = new Color(116, 74, 36);

        g.setColor(holzFarbe);
        g.fillRect(zeichneX + 28, zeichneY + 9, 4, 24);

        g.setColor(metallFarbe);
        g.fillRect(zeichneX + 22, zeichneY + 6, 16, 4);
        g.fillRect(zeichneX + 19, zeichneY + 8, 5, 4);
        g.fillRect(zeichneX + 36, zeichneY + 8, 4, 5);

        g.setColor(hautFarbe);
        g.fillRect(zeichneX + 12, zeichneY + 11, 16, 13);
        g.fillRect(zeichneX + 9, zeichneY + 24, 6, 7);
        g.fillRect(zeichneX + 25, zeichneY + 24, 6, 7);

        g.setColor(helmFarbe);
        g.fillRect(zeichneX + 10, zeichneY + 5, 20, 8);
        g.fillRect(zeichneX + 8, zeichneY + 10, 24, 4);

        g.setColor(helmSchatten);
        g.fillRect(zeichneX + 18, zeichneY + 5, 4, 9);
        g.fillRect(zeichneX + 8, zeichneY + 13, 24, 2);

        g.setColor(Color.WHITE);
        g.fillRect(zeichneX + 17, zeichneY + 7, 6, 4);

        g.setColor(Color.BLACK);
        g.fillRect(zeichneX + 14, zeichneY + 16, 3, 3);
        g.fillRect(zeichneX + 23, zeichneY + 16, 3, 3);
        g.fillRect(zeichneX + 18, zeichneY + 21, 5, 2);

        g.setColor(hemdFarbe);
        g.fillRect(zeichneX + 12, zeichneY + 24, 16, 10);
        g.fillRect(zeichneX + 8, zeichneY + 25, 5, 6);
        g.fillRect(zeichneX + 27, zeichneY + 25, 5, 6);

        g.setColor(hoseFarbe);
        g.fillRect(zeichneX + 12, zeichneY + 34, 7, 5);
        g.fillRect(zeichneX + 21, zeichneY + 34, 7, 5);

        g.setColor(schuhFarbe);
        g.fillRect(zeichneX + 10, zeichneY + 38, 9, 2);
        g.fillRect(zeichneX + 21, zeichneY + 38, 9, 2);
    }

    public void landOnPlatform(int plattformY) {
        y = plattformY - hoehe;
        geschwindigkeitY = 0;
        stehtAufBoden = true;
    }

    public void springeVonTrampolin(int plattformY) {
        y = plattformY - hoehe;
        geschwindigkeitY = trampolinSprungKraft;
        stehtAufBoden = false;
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
