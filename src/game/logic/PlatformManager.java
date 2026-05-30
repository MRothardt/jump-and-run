// Verwaltet alle Plattformen im Spiel.
// Speichert die Plattformen in einer ArrayList.
// Zeichnet alle Plattformen und stellt sie für die Kollisionsprüfung bereit.
package game.logic;

import game.model.Platform;

import java.awt.Graphics;
import java.util.ArrayList;

public class PlatformManager {

    private ArrayList<Platform> platforms;

    public PlatformManager() {
        platforms = new ArrayList<>();

        // Erste feste Plattformen zum Testen
        platforms.add(new Platform(150, 550));
        platforms.add(new Platform(80, 450));
        platforms.add(new Platform(220, 350));
        platforms.add(new Platform(120, 250));
        platforms.add(new Platform(250, 150));
    }

    public void draw(Graphics g) {
        for (Platform platform : platforms) {
            platform.draw(g);
        }
    }

    public ArrayList<Platform> getPlatforms() {
        return platforms;
    }
}