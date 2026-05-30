package game;
// Hauptfenster des Spiels.
// Diese Klasse erbt von JFrame.
// Sie enthält das GamePanel und legt die Fenstereinstellungen fest.
import javax.swing.JFrame;

public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("Jump and Run");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
    }
}