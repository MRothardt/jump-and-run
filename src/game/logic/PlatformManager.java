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

        plattformen.add(new Platform(150, 550, PlatformType.NORMAL));
        plattformen.add(new Platform(80, 480, PlatformType.NORMAL));
        plattformen.add(new Platform(220, 410, PlatformType.NORMAL));
        plattformen.add(new Platform(120, 340, PlatformType.BRUECHIG));
        plattformen.add(new Platform(250, 270, PlatformType.NORMAL));
        plattformen.add(new Platform(40, 200, PlatformType.KRISTALL));
        plattformen.add(new Platform(200, 130, PlatformType.NORMAL));
        plattformen.add(new Platform(100, 60, PlatformType.BRUECHIG));
        plattformen.add(new Platform(240, -10, PlatformType.NORMAL));
        plattformen.add(new Platform(60, -80, PlatformType.NORMAL));
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