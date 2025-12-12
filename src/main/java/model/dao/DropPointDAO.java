package model.dao;

import model.ConPool;
import model.bean.DropPoint;
import model.bean.enums.StatoDropPoint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DropPointDAO {

    public boolean doSave(DropPoint dp) {
        String query = "INSERT INTO drop_point (" +
                "nome_attivita, email, password_hash, indirizzo, " +
                "citta, provincia, telefono, orari_apertura, descrizione, " +
                "immagine, immagine_content_type, " +
                "latitudine, longitudine, ritiri_effettuati, stato" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            // immagine BLOB + content type
            if (dp.getImmagine() != null && dp.getImmagine().length > 0) {
                ps.setBytes(10, dp.getImmagine());
                ps.setString(11, dp.getImmagineContentType());
            } else {
                ps.setNull(10, Types.BLOB);
                ps.setNull(11, Types.VARCHAR);
            }

            ps.setObject(12, dp.getLatitudine());
            ps.setObject(13, dp.getLongitudine());
            ps.setInt(14, dp.getRitiriEffettuati());
            ps.setString(15, dp.getStato().toString());

            int result = ps.executeUpdate();
            if (result > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        dp.setId(generatedKeys.getLong(1));
                    }
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

    /** Tutti i Drop-Point con un certo stato (per l’admin). */
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

    /** Cambia lo stato di un Drop-Point (IN_ATTESA → APPROVATO/RIFIUTATO). */
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
        dp.setDescrizione(rs.getString("descrizione"));

        // immagine BLOB + content-type
        dp.setImmagine(rs.getBytes("immagine"));
        dp.setImmagineContentType(rs.getString("immagine_content_type"));

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

    public DropPoint doRetrieveById(long id) {
        String query = "SELECT * FROM drop_point WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, id);
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
    public boolean updateProfilo(DropPoint dp) {
        String sql = "UPDATE drop_point SET " +
                "nome_attivita = ?, " +
                "email = ?, " +
                "indirizzo = ?, " +
                "citta = ?, " +
                "provincia = ?, " +
                "telefono = ?, " +
                "orari_apertura = ?, " +
                "immagine = ?, " +
                "immagine_content_type = ? " +
                "WHERE id = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dp.getNomeAttivita());
            ps.setString(2, dp.getEmail());
            ps.setString(3, dp.getIndirizzo());
            ps.setString(4, dp.getCitta());
            ps.setString(5, dp.getProvincia());
            ps.setString(6, dp.getTelefono());
            ps.setString(7, dp.getOrariApertura());

            if (dp.getImmagine() != null && dp.getImmagine().length > 0) {
                ps.setBytes(8, dp.getImmagine());
                ps.setString(9, dp.getImmagineContentType());
            } else {
                ps.setNull(8, Types.BLOB);
                ps.setNull(9, Types.VARCHAR);
            }

            ps.setLong(10, dp.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean doDeleteById(long id) {
        String sql = "DELETE FROM drop_point WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean doUpdateProfilo(DropPoint dp) {
        String sql = "UPDATE drop_point " +
                "SET nome_attivita = ?, " +
                "    indirizzo = ?, " +
                "    citta = ?, " +
                "    provincia = ?, " +
                "    telefono = ?, " +
                "    orari_apertura = ?, " +
                "    immagine = ?, " +
                "    immagine_content_type = ? " +
                "WHERE id = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dp.getNomeAttivita());
            ps.setString(2, dp.getIndirizzo());
            ps.setString(3, dp.getCitta());
            ps.setString(4, dp.getProvincia());
            ps.setString(5, dp.getTelefono());
            ps.setString(6, dp.getOrariApertura());

            if (dp.getImmagine() != null && dp.getImmagine().length > 0) {
                ps.setBytes(7, dp.getImmagine());
                ps.setString(8, dp.getImmagineContentType());
            } else {
                ps.setNull(7, java.sql.Types.BLOB);
                ps.setNull(8, java.sql.Types.VARCHAR);
            }

            ps.setLong(9, dp.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
