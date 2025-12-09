package model.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeocodingUtils {

    // Coordinate di default (es. Roma) se l'indirizzo non viene trovato
    private static final double DEFAULT_LAT = 41.9028;
    private static final double DEFAULT_LON = 12.4964;

    /**
     * Restituisce un array di double {latitudine, longitudine} dato un indirizzo.
     */
    public static double[] getCoordinates(String indirizzo, String citta, String provincia) {
        try {
            // 1. Costruiamo la query per OpenStreetMap
            String fullAddress = indirizzo + ", " + citta + ", " + provincia;
            String encodedAddress = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8);

            // URL API Nominatim (OpenStreetMap)
            String urlString = "https://nominatim.openstreetmap.org/search?q=" + encodedAddress + "&format=json&limit=1";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // IMPORTANTE: Nominatim richiede uno User-Agent identificativo
            conn.setRequestProperty("User-Agent", "FoundlyApp/1.0 (student.project@unisa.it)");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                // 2. Parsiamo il JSON ricevuto
                JSONArray jsonArray = new JSONArray(content.toString());

                if (jsonArray.length() > 0) {
                    JSONObject location = jsonArray.getJSONObject(0);
                    double lat = location.getDouble("lat");
                    double lon = location.getDouble("lon");

                    System.out.println("Geocoding Successo: " + fullAddress + " -> " + lat + ", " + lon);
                    return new double[]{lat, lon};
                }
            } else {
                System.err.println("Errore API Geocoding: HTTP " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Eccezione durante il geocoding per: " + indirizzo);
        }

        System.out.println("Indirizzo non trovato, uso coordinate default.");
        return new double[]{DEFAULT_LAT, DEFAULT_LON};
    }
}