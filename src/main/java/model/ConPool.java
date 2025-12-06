package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConPool {
    private static final String URL = "jdbc:mysql://foundly-db-salvolepore7.j.aivencloud.com:21893/defaultdb"
            + "?useSSL=true"
            + "&requireSSL=true"
            + "&verifyServerCertificate=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Europe/Rome"
            + "&enabledTLSProtocols=TLSv1.2,TLSv1.3";

    private static final String USER = "avnadmin";
    private static final String PASSWORD = System.getenv("DB_PASSKEY");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL non trovato!", e);
        }
    }

    /**
     * Restituisce una connessione attiva al database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}