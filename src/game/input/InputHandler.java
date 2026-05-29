package game.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {

    private boolean leftPressed;
    private boolean rightPressed;
    private boolean spacePressed;

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A) {
            leftPressed = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_D) {
            rightPressed = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            spacePressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A) {
            leftPressed = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_D) {
            rightPressed = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            spacePressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Wird hier nicht benötigt
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isSpacePressed() {
        return spacePressed;
    }
}