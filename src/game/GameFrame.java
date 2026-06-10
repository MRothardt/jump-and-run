package game;
// Hauptfenster des Spiels.
// Diese Klasse erbt von JFrame.
// Sie enthält das GamePanel und legt die Fenstereinstellungen fest.
import game.logic.Scoreboard;

import javax.swing.JFrame;

public class GameFrame extends JFrame {

    private final Scoreboard scoreboard;
    private String aktuellerSpielerName;

    public GameFrame() {
        setTitle("Jump and Run");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        scoreboard = new Scoreboard();
        aktuellerSpielerName = "";
        menuAnzeigen();

        pack();
        setLocationRelativeTo(null);
    }

    private void menuAnzeigen() {
        MenuPanel menuPanel = new MenuPanel(scoreboard, this::spielStarten, aktuellerSpielerName);
        setContentPane(menuPanel);
        pack();
        setLocationRelativeTo(null);
        menuPanel.requestFocusInWindow();
    }

    private void spielStarten(String spielerName) {
        aktuellerSpielerName = spielerName;
        GamePanel gamePanel = new GamePanel(scoreboard, spielerName, this::menuAnzeigen);
        setContentPane(gamePanel);
        pack();
        setLocationRelativeTo(null);
        gamePanel.requestFocusInWindow();
    }
}
