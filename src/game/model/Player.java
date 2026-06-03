// Beschreibt den Spieler.
// Speichert Position, Größe und Geschwindigkeit.
// Enthält Bewegung, Springen, Gravitation und das Zeichnen des Spielers.
package game.model;

import java.awt.Color;
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
            zeichneMinenarbeiterBildOderFallback(g, zeichneX, zeichneY);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(zeichneX + breite, zeichneY);
        g2.scale(-1, 1);
        zeichneMinenarbeiterBildOderFallback(g2, 0, 0);
        g2.dispose();
    }

    private void zeichneMinenarbeiterBildOderFallback(Graphics g, int zeichneX, int zeichneY) {
        if (minerBild == null) {
            zeichneMinenarbeiter(g, zeichneX, zeichneY);
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

    private void zeichneMinenarbeiter(Graphics g, int zeichneX, int zeichneY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        Color kontur = new Color(22, 18, 16);
        Color hautFarbe = new Color(224, 151, 92);
        Color hautSchatten = new Color(154, 82, 48);
        Color helmFarbe = new Color(246, 196, 42);
        Color helmLicht = new Color(255, 226, 92);
        Color helmSchatten = new Color(157, 105, 26);
        Color lampe = new Color(255, 244, 178);
        Color hemdFarbe = new Color(32, 92, 146);
        Color hemdSchatten = new Color(20, 56, 95);
        Color traegerFarbe = new Color(83, 57, 42);
        Color hoseFarbe = new Color(55, 45, 42);
        Color schuhFarbe = new Color(26, 22, 19);
        Color metallFarbe = new Color(178, 190, 184);
        Color metallSchatten = new Color(88, 96, 94);
        Color holzFarbe = new Color(122, 76, 34);
        Color holzSchatten = new Color(72, 42, 22);

        // Spitzhacke hinter dem Koerper
        g2.setColor(kontur);
        g2.fillRect(zeichneX + 29, zeichneY + 7, 5, 28);
        g2.fillRect(zeichneX + 20, zeichneY + 4, 19, 7);
        g2.setColor(holzFarbe);
        g2.fillRect(zeichneX + 30, zeichneY + 8, 3, 26);
        g2.setColor(holzSchatten);
        g2.fillRect(zeichneX + 32, zeichneY + 9, 1, 24);
        g2.setColor(metallFarbe);
        g2.fillRect(zeichneX + 22, zeichneY + 5, 15, 4);
        g2.fillRect(zeichneX + 18, zeichneY + 8, 6, 3);
        g2.setColor(metallSchatten);
        g2.fillRect(zeichneX + 34, zeichneY + 8, 5, 3);

        // Beine und Stiefel
        g2.setColor(kontur);
        g2.fillRect(zeichneX + 10, zeichneY + 31, 20, 9);
        g2.setColor(hoseFarbe);
        g2.fillRect(zeichneX + 12, zeichneY + 31, 7, 7);
        g2.fillRect(zeichneX + 21, zeichneY + 31, 7, 7);
        g2.setColor(schuhFarbe);
        g2.fillRect(zeichneX + 9, zeichneY + 37, 11, 3);
        g2.fillRect(zeichneX + 21, zeichneY + 37, 10, 3);

        // Koerper mit Latzhose und Guertel
        g2.setColor(kontur);
        g2.fillRect(zeichneX + 9, zeichneY + 22, 22, 12);
        g2.fillRect(zeichneX + 6, zeichneY + 24, 7, 8);
        g2.fillRect(zeichneX + 27, zeichneY + 24, 7, 8);
        g2.setColor(hemdFarbe);
        g2.fillRect(zeichneX + 11, zeichneY + 23, 18, 10);
        g2.fillRect(zeichneX + 7, zeichneY + 25, 6, 6);
        g2.fillRect(zeichneX + 27, zeichneY + 25, 6, 6);
        g2.setColor(hemdSchatten);
        g2.fillRect(zeichneX + 11, zeichneY + 30, 18, 3);
        g2.setColor(traegerFarbe);
        g2.fillRect(zeichneX + 14, zeichneY + 23, 3, 9);
        g2.fillRect(zeichneX + 23, zeichneY + 23, 3, 9);
        g2.setColor(new Color(210, 150, 58));
        g2.fillRect(zeichneX + 18, zeichneY + 29, 4, 3);

        // Kopf, Bart und Helm
        g2.setColor(kontur);
        g2.fillRect(zeichneX + 10, zeichneY + 10, 20, 14);
        g2.setColor(hautFarbe);
        g2.fillRect(zeichneX + 12, zeichneY + 11, 16, 12);
        g2.setColor(hautSchatten);
        g2.fillRect(zeichneX + 12, zeichneY + 19, 16, 4);
        g2.setColor(new Color(83, 48, 28));
        g2.fillRect(zeichneX + 15, zeichneY + 19, 10, 5);
        g2.fillRect(zeichneX + 17, zeichneY + 23, 6, 2);
        g2.setColor(kontur);
        g2.fillRect(zeichneX + 14, zeichneY + 15, 3, 3);
        g2.fillRect(zeichneX + 23, zeichneY + 15, 3, 3);
        g2.setColor(new Color(248, 190, 126));
        g2.fillRect(zeichneX + 19, zeichneY + 16, 3, 3);

        g2.setColor(kontur);
        g2.fillRect(zeichneX + 9, zeichneY + 4, 22, 10);
        g2.fillRect(zeichneX + 7, zeichneY + 11, 26, 4);
        g2.setColor(helmFarbe);
        g2.fillRect(zeichneX + 10, zeichneY + 5, 20, 8);
        g2.fillRect(zeichneX + 8, zeichneY + 11, 24, 3);
        g2.setColor(helmLicht);
        g2.fillRect(zeichneX + 12, zeichneY + 5, 9, 2);
        g2.setColor(helmSchatten);
        g2.fillRect(zeichneX + 18, zeichneY + 5, 4, 9);
        g2.fillRect(zeichneX + 8, zeichneY + 13, 24, 1);
        g2.setColor(kontur);
        g2.fillRect(zeichneX + 16, zeichneY + 6, 8, 6);
        g2.setColor(lampe);
        g2.fillRect(zeichneX + 17, zeichneY + 7, 6, 4);
        g2.setColor(new Color(255, 255, 225));
        g2.fillRect(zeichneX + 19, zeichneY + 8, 2, 2);

        g2.dispose();
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
