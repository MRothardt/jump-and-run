// Beschreibt den Spieler.
// Speichert Position, Größe und Geschwindigkeit.
// Enthält Bewegung, Springen, Gravitation und das Zeichnen des Spielers.
package game.model;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class Player {

    private static final BufferedImage minerBild = ladeMinerBild();
    private static final int minerQuelleX = 245;
    private static final int minerQuelleY = 175;
    private static final int minerQuelleBreite = 480;
    private static final int minerQuelleHoehe = 660;
    private static final int minerSpriteBreite = 46;
    private static final int minerSpriteHoehe = 58;

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
            schautNachRechts = false;
        }

        if (rechtsGedrueckt) {
            geschwindigkeitX = laufGeschwindigkeit;
            schautNachRechts = true;
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

        if (x + breite > bildschirmBreite) {
            x = bildschirmBreite - breite;
        }
    }

    public void draw(Graphics g) {
        zeichneMinenarbeiterMitRichtung(g, x, y);
    }

    private void zeichneMinenarbeiterMitRichtung(Graphics g, int zeichneX, int zeichneY) {
        if (schautNachRechts) {
            zeichneMinenarbeiterBild(g, zeichneX, zeichneY);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(zeichneX + breite, zeichneY);
        g2.scale(-1, 1);
        zeichneMinenarbeiterBild(g2, 0, 0);
        g2.dispose();
    }

    private void zeichneMinenarbeiterBild(Graphics g, int zeichneX, int zeichneY) {
        if (minerBild == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int zielX = zeichneX - 3;
        int zielY = zeichneY + hoehe - minerSpriteHoehe;

        g2.drawImage(
                minerBild,
                zielX,
                zielY,
                zielX + minerSpriteBreite,
                zielY + minerSpriteHoehe,
                minerQuelleX,
                minerQuelleY,
                minerQuelleX + minerQuelleBreite,
                minerQuelleY + minerQuelleHoehe,
                null
        );

        g2.dispose();
    }

    private static BufferedImage ladeMinerBild() {
        URL bildUrl = Player.class.getResource("/game/assets/miner.png");

        try {
            if (bildUrl != null) {
                return ImageIO.read(bildUrl);
            }

            return ImageIO.read(new File("src/game/assets/miner.png"));
        } catch (IOException e) {
            return null;
        }
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
