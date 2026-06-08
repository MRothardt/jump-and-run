// Verarbeitet die Tastatureingaben.
// Speichert, ob A, D, Leertaste oder R gedrückt werden.
// Diese Werte werden vom GamePanel abgefragt, um den Spieler zu bewegen oder das Spiel neu zu starten.
package game.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {

    private boolean linksGedrueckt;
    private boolean rechtsGedrueckt;
    private boolean springenGedrueckt;
    private boolean neustartGedrueckt;

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A) {
            linksGedrueckt = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_D) {
            rechtsGedrueckt = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            springenGedrueckt = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_R) {
            neustartGedrueckt = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A) {
            linksGedrueckt = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_D) {
            rechtsGedrueckt = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            springenGedrueckt = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_R) {
            neustartGedrueckt = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Wird hier nicht benötigt.
    }

    public void zuruecksetzen() {
        linksGedrueckt = false;
        rechtsGedrueckt = false;
        springenGedrueckt = false;
        neustartGedrueckt = false;
    }

    public boolean isLinksGedrueckt() {
        return linksGedrueckt;
    }

    public boolean isRechtsGedrueckt() {
        return rechtsGedrueckt;
    }

    public boolean isSpringenGedrueckt() {
        return springenGedrueckt;
    }

    public boolean isNeustartGedrueckt() {
        return neustartGedrueckt;
    }
}