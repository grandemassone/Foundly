package model.dao;

import model.ConPool;
import model.bean.Utente;
import model.bean.enums.Ruolo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAO {

    public boolean doSave(Utente utente) {
        String query = "INSERT INTO utente (username, email, password_hash, nome, cognome, " +
                "telefono, immagine_profilo, punteggio, ruolo, badge) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, utente.getUsername());
            ps.setString(2, utente.getEmail());
            ps.setString(3, utente.getPasswordHash());
            ps.setString(4, utente.getNome());
            ps.setString(5, utente.getCognome());
            ps.setString(6, utente.getTelefono());
            ps.setString(7, utente.getImmagineProfilo());
            ps.setInt(8, utente.getPunteggio());
            ps.setString(9, utente.getRuolo().toString());
            ps.setString(10, utente.getBadge());

            int result = ps.executeUpdate();

            if (result > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        utente.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Utente doRetrieveByEmail(String email) {
        String query = "SELECT * FROM utente WHERE email = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUtente(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Utente doRetrieveByUsername(String username) {
        String query = "SELECT * FROM utente WHERE username = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUtente(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Utente> doRetrieveAllByPunteggio() {
        List<Utente> utenti = new ArrayList<>();
        String query = "SELECT * FROM utente ORDER BY punteggio DESC LIMIT 50"; // Limitiamo ai primi 50 per performance

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                utenti.add(mapRowToUtente(rs)); // Usa il tuo metodo mapRowToUtente esistente
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utenti;
    }

    public List<Utente> doRetrieveAll() {
        List<Utente> utenti = new ArrayList<>();
        String query = "SELECT * FROM utente";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                utenti.add(mapRowToUtente(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utenti;
    }

    public Utente doRetrieveById(Long id) {
        String query = "SELECT * FROM utente WHERE id = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUtente(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }    //commento fico


    public boolean updateProfilo(Utente utente) {
        String query = "UPDATE utente SET username = ?, nome = ?, cognome = ?, immagine_profilo = ? WHERE id = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, utente.getUsername());
            ps.setString(2, utente.getNome());
            ps.setString(3, utente.getCognome());
            ps.setString(4, utente.getImmagineProfilo());
            ps.setLong(5, utente.getId());

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Utente mapRowToUtente(ResultSet rs) throws SQLException {
        Utente u = new Utente();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setTelefono(rs.getString("telefono"));
        u.setImmagineProfilo(rs.getString("immagine_profilo"));
        u.setPunteggio(rs.getInt("punteggio"));

        String ruoloStr = rs.getString("ruolo");
        if (ruoloStr != null) {
            u.setRuolo(Ruolo.valueOf(ruoloStr));
        }

        u.setBadge(rs.getString("badge"));
        return u;
    }

    public boolean updatePasswordByEmail(String email, String nuovoHash) {
        String query = "UPDATE utente SET password_hash = ? WHERE email = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, nuovoHash);
            ps.setString(2, email);

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updatePunteggioEBadge(Utente utente) {
        String sql = "UPDATE utente SET punteggio = ?, badge = ? WHERE id = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, utente.getPunteggio());
            ps.setString(2, utente.getBadge());
            ps.setLong(3, utente.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** NUOVO: cancella un utente (ban = delete). */
    public boolean deleteById(long id) {
        String sql = "DELETE FROM utente WHERE id = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
