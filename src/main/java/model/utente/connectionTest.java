package model.utente;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class connectionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://foundly-db-salvolepore7.j.aivencloud.com:21893/defaultdb"
                + "?useSSL=true"
                + "&requireSSL=true"
                + "&verifyServerCertificate=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=Europe/Rome"
                + "&enabledTLSProtocols=TLSv1.2,TLSv1.3";
        String user = "avnadmin";
        String pass = System.getenv("DB_PASSKEY");;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection c = DriverManager.getConnection(url, user, pass)) {
                System.out.println("✅ Connessione riuscita!");
                System.out.println("📋 Nomi degli utenti registrati:");
                System.out.println("--------------------------------");

                String query = "SELECT nome, cognome FROM user";
                try (PreparedStatement ps = c.prepareStatement(query);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        System.out.println("👤 " + rs.getString("nome") + " " + rs.getString("cognome"));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Errore di connessione o query:");
            e.printStackTrace();
        }
    }
}

