package model.dao;

import model.ConPool;
import model.bean.DropPoint;
import java.sql.*;

public class DropPointDAO {

    public boolean doSave(DropPoint dp) {
        String query = "INSERT INTO drop_point (nome_attivita, email, password_hash, indirizzo, citta, provincia, telefono, orari_apertura, descrizione, stato) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, dp.getNomeAttivita());
            ps.setString(2, dp.getEmail());
            ps.setString(3, dp.getPasswordHash());
            ps.setString(4, dp.getIndirizzo());
            ps.setString(5, dp.getCitta());
            ps.setString(6, dp.getProvincia());
            ps.setString(7, dp.getTelefono());
            ps.setString(8, dp.getOrariApertura());
            ps.setString(9, dp.getDescrizione());
            ps.setString(10, dp.getStato()); // Solitamente "IN_ATTESA"

            int result = ps.executeUpdate();
            if (result > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) dp.setId(generatedKeys.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public DropPoint doRetrieveByEmail(String email) {
        String query = "SELECT * FROM drop_point WHERE email = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DropPoint dp = new DropPoint();
                    dp.setId(rs.getLong("id"));
                    dp.setNomeAttivita(rs.getString("nome_attivita"));
                    dp.setEmail(rs.getString("email"));
                    dp.setPasswordHash(rs.getString("password_hash"));
                    dp.setIndirizzo(rs.getString("indirizzo"));
                    dp.setCitta(rs.getString("citta"));
                    dp.setStato(rs.getString("stato"));
                    return dp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}