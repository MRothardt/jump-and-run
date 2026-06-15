// Speichert den aktuellen Zustand des Spiels.
// Dadurch kann unterschieden werden, ob das Spiel läuft, eine Sterbeanimation zeigt oder beendet wurde.
// Wird genutzt, um vor Game Over noch eine kurze Animation abzuspielen.
package game;

public enum GameState {
    RUNNING,
    DYING,
    GAME_OVER
}
