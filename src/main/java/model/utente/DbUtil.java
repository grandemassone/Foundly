package model.utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbUtil {
    private static final String URL =
            "jdbc:mysql://foundly-db-salvolepore7.j.aivencloud.com:21893/defaultdb"
                    + "?useSSL=true"
                    + "&requireSSL=true"
                    + "&verifyServerCertificate=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=Europe/Rome"
                    + "&enabledTLSProtocols=TLSv1.2,TLSv1.3";

    private static final String USER = "avnadmin";
    // Metti la password in variabile d'ambiente "DB_PASSKEY" o (solo per test) in chiaro
    private static final String PASS = System.getenv("DB_PASSKEY"); // per test: "LA_TUA_PASSWORD"

    // Se vuoi anche esporre la Connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Metodo pronto che ritorna i nomi completi
    public static List<String> getNomiCompleti() throws SQLException {
        List<String> out = new ArrayList<>();
        String sql = "SELECT nome, cognome FROM user";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(rs.getString("nome") + " " + rs.getString("cognome"));
            }
        }
        return out;
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // carica il driver una volta
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver non trovato", e);
        }
    }
}
