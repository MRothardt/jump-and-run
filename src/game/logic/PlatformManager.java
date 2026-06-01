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
                150, 220, 140, 60, 20,
                90, 210, 270, 180, 80,
                10, 120, 240, 170, 40,
                230, 130, 280, 190, 70
        };

        int startY = 540;
        int abstand = 70;

        for (int i = 0; i < 100; i++) {
            int x = xPositionen[i % xPositionen.length];
            int y = startY - i * abstand;

            PlatformType typ;

            if (i < 6) {
                typ = PlatformType.NORMAL;
            } else if (i % 10 == 0) {
                typ = PlatformType.KRISTALL;
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
