// Verwaltet alle Plattformen im Spiel.
// Speichert die Plattformen in einer ArrayList.
// Aktualisiert, zeichnet und entfernt Plattformen.
package game.logic;

import game.model.Platform;
import game.model.PlatformType;

import java.awt.Graphics;
import java.util.ArrayList;

public class PlatformManager {

    private ArrayList<Platform> plattformen;

    public PlatformManager() {
        plattformen = new ArrayList<>();

        int[] xPositionen = {
                150, 50, 230, 90, 260,
                30, 210, 120, 270, 60,
                240, 90, 180, 30, 250,
                110, 280, 70, 220, 40
        };

        int startY = 540;
        int abstand = 90;

        for (int i = 0; i < 100; i++) {
            int x = xPositionen[i % xPositionen.length];
            int y = startY - i * abstand;

            PlatformType typ;

            if (i < 6) {
                typ = PlatformType.NORMAL;
            } else if (i % 18 == 0) {
                typ = PlatformType.TRAMPOLIN;
            } else if (i % 4 == 0 || i % 7 == 0) {
                typ = PlatformType.BRUECHIG;
            } else {
                typ = PlatformType.NORMAL;
            }

            plattformen.add(new Platform(x, y, typ));
        }
    }

    public void aktualisieren() {
        for (Platform plattform : plattformen) {
            plattform.aktualisieren();
        }

        plattformen.removeIf(Platform::sollEntferntWerden);
    }

    public void draw(Graphics g) {
        for (Platform plattform : plattformen) {
            plattform.draw(g);
        }
    }

    public void bewegeAllePlattformenNachUnten(int distanz) {
        for (Platform plattform : plattformen) {
            plattform.bewegeNachUnten(distanz);
        }
    }

    public ArrayList<Platform> getPlatforms() {
        return plattformen;
    }
}
