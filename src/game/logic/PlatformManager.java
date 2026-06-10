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
    private int naechsterPlattformIndex;

    private final int[] xPositionen = {
            150, 50, 230, 90, 260,
            30, 210, 120, 270, 60,
            240, 90, 180, 30, 250,
            110, 280, 70, 220, 40
    };
    private final int startY = 540;
    private final int abstand = 90;
    private final int untereEntfernungsGrenze = 700;
    private final int ausweichPlattformHoehenVersatz = 25;

    public PlatformManager() {
        plattformen = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            neuePlattformHinzufuegen(startY - i * abstand);
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

        plattformen.removeIf(plattform -> plattform.getY() > untereEntfernungsGrenze);

        while (oberstePlattformY() > -abstand) {
            neuePlattformHinzufuegen(oberstePlattformY() - abstand);
        }
    }

    private void neuePlattformHinzufuegen(int y) {
        int x = xPositionen[naechsterPlattformIndex % xPositionen.length];
        PlatformType typ;

        if (naechsterPlattformIndex < 6) {
            typ = PlatformType.NORMAL;
        } else if (naechsterPlattformIndex % 18 == 0) {
            typ = PlatformType.TRAMPOLIN;
        } else if (naechsterPlattformIndex % 4 == 0 || naechsterPlattformIndex % 7 == 0) {
            typ = PlatformType.BRUECHIG;
        } else {
            typ = PlatformType.NORMAL;
        }

        plattformen.add(new Platform(x, y, typ));

        if (typ == PlatformType.BRUECHIG) {
            plattformen.add(new Platform(ausweichPlattformX(x), y + ausweichPlattformHoehenVersatz, PlatformType.NORMAL));
        }

        naechsterPlattformIndex++;
    }

    private int ausweichPlattformX(int holzPlattformX) {
        int x;

        if (holzPlattformX < 150) {
            x = holzPlattformX + 170;
        } else {
            x = holzPlattformX - 170;
        }

        if (x < 20) {
            return 20;
        }

        if (x > 280) {
            return 280;
        }

        return x;
    }

    private int oberstePlattformY() {
        int obersteY = Integer.MAX_VALUE;

        for (Platform plattform : plattformen) {
            if (plattform.getY() < obersteY) {
                obersteY = plattform.getY();
            }
        }

        return obersteY;
    }

    public ArrayList<Platform> getPlatforms() {
        return plattformen;
    }
}
