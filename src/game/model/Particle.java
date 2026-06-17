package game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Particle {

    private double x;
    private double y;
    private double geschwindigkeitX;
    private double geschwindigkeitY;
    private final double gravitation;
    private final int startGroesse;
    private final int lebensdauer;
    private final Color farbe;
    private int alter;

    public Particle(
            double x,
            double y,
            double geschwindigkeitX,
            double geschwindigkeitY,
            double gravitation,
            int startGroesse,
            int lebensdauer,
            Color farbe
    ) {
        this.x = x;
        this.y = y;
        this.geschwindigkeitX = geschwindigkeitX;
        this.geschwindigkeitY = geschwindigkeitY;
        this.gravitation = gravitation;
        this.startGroesse = startGroesse;
        this.lebensdauer = lebensdauer;
        this.farbe = farbe;
        this.alter = 0;
    }

    public void aktualisieren() {
        alter++;
        x += geschwindigkeitX;
        y += geschwindigkeitY;
        geschwindigkeitY += gravitation;
        geschwindigkeitX *= 0.98;
    }

    public void bewegeNachUnten(int distanz) {
        y += distanz;
    }

    public void draw(Graphics g) {
        if (istAbgelaufen()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        float sichtbarkeit = 1.0f - (float) alter / lebensdauer;
        int alpha = Math.max(0, Math.min(255, Math.round(farbe.getAlpha() * sichtbarkeit)));
        int groesse = Math.max(1, Math.round(startGroesse * (0.45f + sichtbarkeit * 0.55f)));

        g2.setColor(new Color(farbe.getRed(), farbe.getGreen(), farbe.getBlue(), alpha));
        g2.fillRect((int) Math.round(x), (int) Math.round(y), groesse, groesse);
        g2.dispose();
    }

    public boolean istAbgelaufen() {
        return alter >= lebensdauer;
    }
}
