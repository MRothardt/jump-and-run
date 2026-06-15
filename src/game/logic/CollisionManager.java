// Prüft Kollisionen zwischen Spielobjekten.
// Es wird geprüft, ob der Spieler auf einer Plattform landet.
// Zusätzlich wird geprüft, ob der Spieler die Lava berührt.
package game.logic;

import game.model.Lava;
import game.model.Platform;
import game.model.PlatformType;
import game.model.Player;

public class CollisionManager {

    public boolean checkPlatformCollision(Player spieler, PlatformManager plattformManager) {
        spieler.setOnGround(false);
        boolean trampolinWurdeAusgeloest = false;

        for (Platform plattform : plattformManager.getPlatforms()) {

            boolean spielerFaellt = spieler.getVelocityY() >= 0;

            boolean spielerBeruehrtPlattformVonOben =
                    spieler.getY() + spieler.getHeight() >= plattform.getY()
                            && spieler.getY() + spieler.getHeight() <= plattform.getY() + plattform.getHeight();

            boolean spielerIstHorizontalAufPlattform =
                    spieler.getX() + spieler.getWidth() > plattform.getX()
                            && spieler.getX() < plattform.getX() + plattform.getWidth();

            if (spielerFaellt && spielerBeruehrtPlattformVonOben && spielerIstHorizontalAufPlattform) {
                if (plattform.getPlattformTyp() == PlatformType.TRAMPOLIN) {
                    spieler.springeVonTrampolin(plattform.getY());
                    trampolinWurdeAusgeloest = true;
                } else {
                    spieler.landOnPlatform(plattform.getY());
                }

                plattform.beruehren();
            }
        }

        return trampolinWurdeAusgeloest;
    }

    public boolean checkLavaCollision(Player spieler, Lava lava) {
        return spieler.getY() + spieler.getHeight() >= lava.getY();
    }
}
