// Speichert den aktuellen Zustand des Spiels.
// Dadurch kann unterschieden werden, ob das Spiel läuft oder beendet wurde.
// Wird genutzt, um bei Lava-Berührung Game Over anzuzeigen.
package game;

public enum GameState {
    RUNNING,
    GAME_OVER
}