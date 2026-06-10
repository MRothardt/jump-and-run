// Verwaltet die besten Scores mit Spielernamen über mehrere Programmstarts hinweg.
package game.logic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Scoreboard {

    private static final int MAX_EINTRAEGE = 10;
    private static final String STANDARD_NAME = "Spieler";

    private final Path scoreboardDatei;
    private final ArrayList<ScoreEintrag> eintraege;
    private String letzterSpielerName;

    public Scoreboard() {
        this(Path.of(System.getProperty("user.home"), ".jump-and-run", "scoreboard.txt"));
    }

    Scoreboard(Path scoreboardDatei) {
        this.scoreboardDatei = scoreboardDatei;
        this.eintraege = new ArrayList<>();
        this.letzterSpielerName = STANDARD_NAME;
        scoreboardLaden();
    }

    public void scoreEintragen(String spielerName, int score) {
        String bereinigterName = namenBereinigen(spielerName);
        int bereinigterScore = Math.max(score, 0);

        scoreAktualisieren(bereinigterName, bereinigterScore);
        letzterSpielerName = bereinigterName;
        eintraegeSortierenUndBegrenzen();
        scoreboardSpeichern();
    }

    public String spielerNameSetzen(String spielerName) {
        letzterSpielerName = namenBereinigen(spielerName);
        scoreboardSpeichern();
        return letzterSpielerName;
    }

    public int getBesterScore() {
        if (eintraege.isEmpty()) {
            return 0;
        }

        return eintraege.get(0).getScore();
    }

    public String getLetzterSpielerName() {
        return letzterSpielerName;
    }

    public List<ScoreEintrag> getEintraege() {
        return new ArrayList<>(eintraege);
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

        private ScoreEintrag(String spielerName, int score) {
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
