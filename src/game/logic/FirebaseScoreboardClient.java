// Kapselt den Zugriff auf die Firebase Realtime Database fuer das Scoreboard.
package game.logic;

import game.logic.Scoreboard.ScoreEintrag;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

class FirebaseScoreboardClient {

    private static final String FIREBASE_SCORES_URL = "https://lava-miner-default-rtdb.firebaseio.com/scoreboard/scores";

    List<ScoreEintrag> eintraegeLaden() throws IOException {
        String json = httpAnfrage("GET", FIREBASE_SCORES_URL + ".json", null);
        return scoreEintraegeAusJson(json);
    }

    ScoreEintrag eintragLaden(String spielerName) throws IOException {
        String json = httpAnfrage("GET", FIREBASE_SCORES_URL + "/" + firebaseKey(spielerName) + ".json", null);
        return scoreEintragAusJson(json);
    }

    void eintragSpeichern(String spielerName, int score) throws IOException {
        String json = "{\"spielerName\":\"" + jsonEscapen(spielerName) + "\",\"score\":" + Math.max(score, 0) + "}";
        httpAnfrage("PUT", FIREBASE_SCORES_URL + "/" + firebaseKey(spielerName) + ".json", json);
    }

    private String httpAnfrage(String methode, String url, String body) throws IOException {
        URL verbindungsUrl = URI.create(url).toURL();
        HttpURLConnection verbindung = (HttpURLConnection) verbindungsUrl.openConnection();
        verbindung.setRequestMethod(methode);
        verbindung.setConnectTimeout(5000);
        verbindung.setReadTimeout(5000);
        verbindung.setRequestProperty("Accept", "application/json");

        if (body != null) {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            verbindung.setDoOutput(true);
            verbindung.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            verbindung.setRequestProperty("Content-Length", String.valueOf(bytes.length));

            try (OutputStream outputStream = verbindung.getOutputStream()) {
                outputStream.write(bytes);
            }
        }

        int status = verbindung.getResponseCode();

        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status);
        }

        try (java.io.InputStream inputStream = verbindung.getInputStream();
             java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream()) {
            byte[] bytes = new byte[1024];
            int gelesen;

            while ((gelesen = inputStream.read(bytes)) != -1) {
                buffer.write(bytes, 0, gelesen);
            }

            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            verbindung.disconnect();
        }
    }

    private String firebaseKey(String spielerName) {
        String normalisierterName = spielerName.toLowerCase(Locale.ROOT);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(normalisierterName.getBytes(StandardCharsets.UTF_8));
    }

    private List<ScoreEintrag> scoreEintraegeAusJson(String json) {
        ArrayList<ScoreEintrag> geladeneEintraege = new ArrayList<>();

        if (json == null || json.equals("null")) {
            return geladeneEintraege;
        }

        int index = 0;

        while (index < json.length()) {
            int feldIndex = json.indexOf("\"spielerName\"", index);

            if (feldIndex < 0) {
                break;
            }

            int objektStart = json.lastIndexOf('{', feldIndex);
            int objektEnde = passendesObjektEndeFinden(json, objektStart);

            if (objektEnde < 0) {
                break;
            }

            ScoreEintrag eintrag = scoreEintragAusJson(json.substring(objektStart, objektEnde + 1));

            if (eintrag != null) {
                geladeneEintraege.add(eintrag);
            }

            index = objektEnde + 1;
        }

        return geladeneEintraege;
    }

    private ScoreEintrag scoreEintragAusJson(String json) {
        if (json == null || json.equals("null")) {
            return null;
        }

        String spielerName = jsonStringFeldLesen(json, "spielerName");
        Integer score = jsonIntFeldLesen(json, "score");

        if (spielerName == null || score == null) {
            return null;
        }

        return new ScoreEintrag(spielerName, Math.max(score, 0));
    }

    private String jsonStringFeldLesen(String json, String feldName) {
        String feld = "\"" + feldName + "\":\"";
        int start = json.indexOf(feld);

        if (start < 0) {
            return null;
        }

        start += feld.length();
        StringBuilder wert = new StringBuilder();
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char zeichen = json.charAt(i);

            if (escaped) {
                if (zeichen == 'n') {
                    wert.append('\n');
                } else if (zeichen == 'r') {
                    wert.append('\r');
                } else if (zeichen == 't') {
                    wert.append('\t');
                } else {
                    wert.append(zeichen);
                }

                escaped = false;
            } else if (zeichen == '\\') {
                escaped = true;
            } else if (zeichen == '"') {
                return wert.toString();
            } else {
                wert.append(zeichen);
            }
        }

        return null;
    }

    private Integer jsonIntFeldLesen(String json, String feldName) {
        String feld = "\"" + feldName + "\":";
        int start = json.indexOf(feld);

        if (start < 0) {
            return null;
        }

        start += feld.length();
        int ende = start;

        while (ende < json.length() && (Character.isDigit(json.charAt(ende)) || json.charAt(ende) == '-')) {
            ende++;
        }

        try {
            return Integer.parseInt(json.substring(start, ende));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int passendesObjektEndeFinden(String json, int objektStart) {
        int tiefe = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = objektStart; i < json.length(); i++) {
            char zeichen = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (zeichen == '\\') {
                escaped = true;
                continue;
            }

            if (zeichen == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            if (zeichen == '{') {
                tiefe++;
            } else if (zeichen == '}') {
                tiefe--;

                if (tiefe == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private String jsonEscapen(String text) {
        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char zeichen = text.charAt(i);

            if (zeichen == '"' || zeichen == '\\') {
                escaped.append('\\').append(zeichen);
            } else if (zeichen == '\n') {
                escaped.append("\\n");
            } else if (zeichen == '\r') {
                escaped.append("\\r");
            } else if (zeichen == '\t') {
                escaped.append("\\t");
            } else {
                escaped.append(zeichen);
            }
        }

        return escaped.toString();
    }
}
