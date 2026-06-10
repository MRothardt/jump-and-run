package game;
// Startklasse des Spiels.
// Hier beginnt das Programm.
// Die Klasse erstellt das Spielfenster und macht es sichtbar.
public class Main {

    public static void main(String[] args) {
        System.out.println("Spiel startet...");
        GameFrame gameFrame = new GameFrame();
        gameFrame.setVisible(true);
    }
}