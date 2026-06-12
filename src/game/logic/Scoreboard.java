// Verwaltet die besten Scores mit Spielernamen über mehrere Programmstarts hinweg.
package game.logic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scoreboard {

    private static final int MAX_EINTRAEGE = 100;
    private static final String STANDARD_NAME = "Spieler";

    private final Path scoreboardDatei;
    private final ArrayList<ScoreEintrag> eintraege;
    private final ArrayList<Runnable> aenderungsListener;
    private final ExecutorService firebaseExecutor;
    private final FirebaseScoreboardClient firebaseClient;
    private String letzterSpielerName;

    public Scoreboard() {
        this(Path.of(System.getProperty("user.home"), ".jump-and-run", "scoreboard.txt"));
    }

    Scoreboard(Path scoreboardDatei) {
        this.scoreboardDatei = scoreboardDatei;
        this.eintraege = new ArrayList<>();
        this.aenderungsListener = new ArrayList<>();
        this.firebaseClient = new FirebaseScoreboardClient();
        this.firebaseExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "scoreboard-firebase");
            thread.setDaemon(true);
            return thread;
        });
        this.letzterSpielerName = STANDARD_NAME;
        scoreboardLaden();
        firebaseLadenAsync();
    }

    public synchronized void scoreEintragen(String spielerName, int score) {
        String bereinigterName = namenBereinigen(spielerName);
        int bereinigterScore = Math.max(score, 0);

        scoreAktualisieren(bereinigterName, bereinigterScore);
        letzterSpielerName = bereinigterName;
        eintraegeSortierenUndBegrenzen();
        scoreboardSpeichern();
        firebaseScoreSpeichernAsync(bereinigterName, bereinigterScore);
        aenderungMelden();
    }

    public synchronized String spielerNameSetzen(String spielerName) {
        letzterSpielerName = namenBereinigen(spielerName);
        scoreboardSpeichern();
        aenderungMelden();
        return letzterSpielerName;
    }

    public synchronized int getBesterScore() {
        if (eintraege.isEmpty()) {
            return 0;
        }

        return eintraege.get(0).getScore();
    }

    public synchronized String getLetzterSpielerName() {
        return letzterSpielerName;
    }

    public synchronized List<ScoreEintrag> getEintraege() {
        return new ArrayList<>(eintraege);
    }

    public synchronized int getRang(String spielerName) {
        String bereinigterName = namenBereinigen(spielerName);

        for (int i = 0; i < eintraege.size(); i++) {
            if (eintraege.get(i).getSpielerName().equalsIgnoreCase(bereinigterName)) {
                return i + 1;
            }
        }

        return -1;
    }

    public synchronized void aenderungsListenerHinzufuegen(Runnable listener) {
        if (listener != null) {
            aenderungsListener.add(listener);
        }
    }

    public synchronized void aenderungsListenerEntfernen(Runnable listener) {
        aenderungsListener.remove(listener);
    }

    private void firebaseLadenAsync() {
        firebaseExecutor.submit(() -> {
            try {
                List<ScoreEintrag> firebaseEintraege = firebaseClient.eintraegeLaden();

                if (!firebaseEintraege.isEmpty()) {
                    synchronized (this) {
                        for (ScoreEintrag eintrag : firebaseEintraege) {
                            scoreAktualisieren(namenBereinigen(eintrag.getSpielerName()), eintrag.getScore());
                        }

                        eintraegeSortierenUndBegrenzen();
                        scoreboardSpeichern();
                    }

                    aenderungMelden();
                }
            } catch (IOException e) {
                System.err.println("Firebase-Scoreboard konnte nicht geladen werden: " + e.getMessage());
            }
        });
    }

    private void firebaseScoreSpeichernAsync(String spielerName, int score) {
        firebaseExecutor.submit(() -> {
            try {
                ScoreEintrag firebaseEintrag = firebaseClient.eintragLaden(spielerName);
                int besterScore = score;

                if (firebaseEintrag != null && firebaseEintrag.getScore() > besterScore) {
                    besterScore = firebaseEintrag.getScore();
                }

                firebaseClient.eintragSpeichern(spielerName, besterScore);

                if (besterScore > score) {
                    synchronized (this) {
                        scoreAktualisieren(spielerName, besterScore);
                        eintraegeSortierenUndBegrenzen();
                        scoreboardSpeichern();
                    }

                    aenderungMelden();
                }
            } catch (IOException e) {
                System.err.println("Firebase-Score konnte nicht gespeichert werden: " + e.getMessage());
            }
        });
    }

    private void scoreboardLaden() {
        if (!Files.exists(scoreboardDatei)) {
            return;
        }

        try {
            for (String zeile : Files.readAllLines(scoreboardDatei, StandardCharsets.UTF_8)) {
                String[] teile = zeile.split("\t", 3);

                if (teile.length == 2 && teile[0].equals("last")) {
                    letzterSpielerName = namenBereinigen(teile[1]);
                } else if (teile.length == 3 && teile[0].equals("score")) {
                    scoreAktualisieren(namenBereinigen(teile[2]), Integer.parseInt(teile[1]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            eintraege.clear();
            letzterSpielerName = STANDARD_NAME;
        }

        eintraegeSortierenUndBegrenzen();
    }

    private synchronized void aenderungMelden() {
        ArrayList<Runnable> listenerKopie = new ArrayList<>(aenderungsListener);

        for (Runnable listener : listenerKopie) {
            listener.run();
        }
    }

    private void scoreAktualisieren(String spielerName, int score) {
        ScoreEintrag vorhandenerEintrag = eintragFinden(spielerName);

        if (vorhandenerEintrag == null) {
            eintraege.add(new ScoreEintrag(spielerName, Math.max(score, 0)));
        } else if (score > vorhandenerEintrag.getScore()) {
            vorhandenerEintrag.setScore(Math.max(score, 0));
            vorhandenerEintrag.setSpielerName(spielerName);
        }
    }

    private void scoreboardSpeichern() {
        ArrayList<String> zeilen = new ArrayList<>();
        zeilen.add("last\t" + letzterSpielerName);

        for (ScoreEintrag eintrag : eintraege) {
            zeilen.add("score\t" + eintrag.getScore() + "\t" + eintrag.getSpielerName());
        }

        try {
            Path parent = scoreboardDatei.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.write(scoreboardDatei, zeilen, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Scoreboard konnte nicht gespeichert werden: " + e.getMessage());
        }
    }

    private ScoreEintrag eintragFinden(String spielerName) {
        for (ScoreEintrag eintrag : eintraege) {
            if (eintrag.getSpielerName().equalsIgnoreCase(spielerName)) {
                return eintrag;
            }
        }

        return null;
    }

    private void eintraegeSortierenUndBegrenzen() {
        eintraege.removeIf(eintrag -> eintrag.getScore() < 0);
        eintraege.sort(
                Comparator.comparingInt(ScoreEintrag::getScore)
                        .reversed()
                        .thenComparing(ScoreEintrag::getSpielerName)
        );

        while (eintraege.size() > MAX_EINTRAEGE) {
            eintraege.remove(eintraege.size() - 1);
        }
    }

    private String namenBereinigen(String spielerName) {
        if (spielerName == null) {
            return STANDARD_NAME;
        }

        String bereinigterName = spielerName
                .replace('\t', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();

        if (bereinigterName.isEmpty()) {
            return STANDARD_NAME;
        }

        if (bereinigterName.length() > 18) {
            return bereinigterName.substring(0, 18);
        }

        return bereinigterName;
    }

    public static class ScoreEintrag {

        private String spielerName;
        private int score;

        ScoreEintrag(String spielerName, int score) {
            this.spielerName = spielerName;
            this.score = score;
        }

        public String getSpielerName() {
            return spielerName;
        }

        public int getScore() {
            return score;
        }

        private void setSpielerName(String spielerName) {
            this.spielerName = spielerName;
        }

        private void setScore(int score) {
            this.score = score;
        }
    }
}
