package model.dao;

import model.ConPool;
import model.bean.DropPoint;
import model.bean.enums.StatoDropPoint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DropPointDAO {

    public boolean doSave(DropPoint dp) {
        String query = "INSERT INTO drop_point (nome_attivita, email, password_hash, indirizzo, " +
                "citta, provincia, telefono, orari_apertura, latitudine, longitudine, " +
                "stato, ritiri_effettuati, immagine) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            ps.setObject(9, dp.getLatitudine());
            ps.setObject(10, dp.getLongitudine());

            ps.setString(11, dp.getStato().toString());
            ps.setInt(12, dp.getRitiriEffettuati());
            ps.setString(13, dp.getImmagine());

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
                    return mapRowToDropPoint(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DropPoint> doRetrieveAllApprovati() {
        List<DropPoint> list = new ArrayList<>();
        String query = "SELECT * FROM drop_point WHERE stato = 'APPROVATO'";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToDropPoint(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** NUOVO: tutti i Drop-Point con un certo stato (per l’admin). */
    public List<DropPoint> doRetrieveByStato(StatoDropPoint stato) {
        List<DropPoint> list = new ArrayList<>();
        String query = "SELECT * FROM drop_point WHERE stato = ? ORDER BY id DESC";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, stato.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDropPoint(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** NUOVO: cambia lo stato di un Drop-Point (IN_ATTESA → APPROVATO/RIFIUTATO). */
    public boolean updateStato(long id, StatoDropPoint nuovoStato) {
        String query = "UPDATE drop_point SET stato = ? WHERE id = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, nuovoStato.toString());
            ps.setLong(2, id);

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Helper per mappare una riga del ResultSet in un DropPoint. */
    private DropPoint mapRowToDropPoint(ResultSet rs) throws SQLException {
        DropPoint dp = new DropPoint();
        dp.setId(rs.getLong("id"));
        dp.setNomeAttivita(rs.getString("nome_attivita"));
        dp.setEmail(rs.getString("email"));
        dp.setPasswordHash(rs.getString("password_hash"));
        dp.setIndirizzo(rs.getString("indirizzo"));
        dp.setCitta(rs.getString("citta"));
        dp.setProvincia(rs.getString("provincia"));
        dp.setTelefono(rs.getString("telefono"));
        dp.setOrariApertura(rs.getString("orari_apertura"));
        dp.setImmagine(rs.getString("immagine"));

        dp.setLatitudine(rs.getObject("latitudine", Double.class));
        dp.setLongitudine(rs.getObject("longitudine", Double.class));
        dp.setRitiriEffettuati(rs.getInt("ritiri_effettuati"));

        try {
            dp.setStato(StatoDropPoint.valueOf(rs.getString("stato")));
        } catch (IllegalArgumentException | NullPointerException e) {
            dp.setStato(StatoDropPoint.IN_ATTESA);
        }

        return dp;
    }
}
