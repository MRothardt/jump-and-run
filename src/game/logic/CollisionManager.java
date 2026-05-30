// Prüft Kollisionen zwischen Spielobjekten.
// Aktuell wird geprüft, ob der Spieler auf einer Plattform landet.
// Zusätzlich wird geprüft, ob der Spieler die Lava berührt.
package game.logic;

import game.model.Lava;
import game.model.Platform;
import game.model.Player;

public class CollisionManager {

    public void checkPlatformCollision(Player player, PlatformManager platformManager) {
        player.setOnGround(false);

        for (Platform platform : platformManager.getPlatforms()) {

            boolean playerFalls = player.getVelocityY() >= 0;

            boolean playerBottomTouchesPlatform =
                    player.getY() + player.getHeight() >= platform.getY()
                            && player.getY() + player.getHeight() <= platform.getY() + platform.getHeight();

            boolean playerIsHorizontallyOnPlatform =
                    player.getX() + player.getWidth() > platform.getX()
                            && player.getX() < platform.getX() + platform.getWidth();

            if (playerFalls && playerBottomTouchesPlatform && playerIsHorizontallyOnPlatform) {
                player.landOnPlatform(platform.getY());
            }
        }
    }

    public boolean checkLavaCollision(Player player, Lava lava) {
        return player.getY() + player.getHeight() >= lava.getY();
    }
}