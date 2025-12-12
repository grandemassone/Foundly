package model.dao;

import model.ConPool;
import model.bean.Reclamo;
import model.bean.enums.StatoReclamo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamoDAO {

    public boolean doSave(Reclamo r) {
        String query = "INSERT INTO reclamo (" +
                "id_segnalazione, id_utente_richiedente, " +
                "risposta_verifica1, risposta_verifica2, " +
                "stato, data_richiesta" +
                ") VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setLong(1, r.getIdSegnalazione());
            ps.setLong(2, r.getIdUtenteRichiedente());
            ps.setString(3, r.getRispostaVerifica1());
            ps.setString(4, r.getRispostaVerifica2());
            ps.setString(5, StatoReclamo.IN_ATTESA.toString());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- NUOVI METODI PER SCAMBIO DIRETTO ---
    public boolean confermaFinder(long idReclamo) {
        String sql = "UPDATE reclamo SET conferma_finder = 1 WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idReclamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean confermaOwner(long idReclamo) {
        String sql = "UPDATE reclamo SET conferma_owner = 1 WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idReclamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // ----------------------------------------

    // --- NUOVO METODO PER DROP-POINT (CERCA PER CODICE) ---
    public Reclamo doRetrieveByCodice(String codice) {
        String query = "SELECT * FROM reclamo WHERE codice_consegna = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, codice);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // -----------------------------------------------------

    public List<Reclamo> doRetrieveBySegnalazione(long idSegnalazione) {
        List<Reclamo> list = new ArrayList<>();
        String query = "SELECT * FROM reclamo WHERE id_segnalazione = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setLong(1, idSegnalazione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Reclamo doRetrieveBySegnalazioneAndUtente(long idSegnalazione, long idUtente) {
        String query = "SELECT * FROM reclamo WHERE id_segnalazione = ? AND id_utente_richiedente = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setLong(1, idSegnalazione);
            ps.setLong(2, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean accettaReclamo(long idReclamo, String codice) {
        String query = "UPDATE reclamo SET stato = 'ACCETTATO', codice_consegna = ? WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, codice);
            ps.setLong(2, idReclamo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Reclamo doRetrieveById(long id) {
        String query = "SELECT * FROM reclamo WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean rifiutaReclamo(long idReclamo) {
        String query = "UPDATE reclamo SET stato = 'RIFIUTATO' WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setLong(1, idReclamo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Reclamo> doRetrieveByRichiedente(long idUtenteRichiedente) {
        List<Reclamo> list = new ArrayList<>();
        String query = "SELECT r.*, s.titolo AS seg_titolo, s.immagine AS seg_immagine " +
                "FROM reclamo r " +
                "JOIN segnalazione s ON r.id_segnalazione = s.id " +
                "WHERE r.id_utente_richiedente = ? " +
                "ORDER BY r.data_richiesta DESC";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setLong(1, idUtenteRichiedente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reclamo r = mapRow(rs);
                    r.setTitoloSegnalazione(rs.getString("seg_titolo"));
                    String imgPath = rs.getString("seg_immagine");
                    if (imgPath != null) {
                        r.setImmagineSegnalazione(imgPath.getBytes());
                    }
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Reclamo mapRow(ResultSet rs) throws SQLException {
        Reclamo r = new Reclamo();
        r.setId(rs.getLong("id"));
        r.setIdSegnalazione(rs.getLong("id_segnalazione"));
        r.setIdUtenteRichiedente(rs.getLong("id_utente_richiedente"));
        r.setRispostaVerifica1(rs.getString("risposta_verifica1"));
        r.setRispostaVerifica2(rs.getString("risposta_verifica2"));
        r.setDataRichiesta(rs.getTimestamp("data_richiesta"));
        r.setDataDeposito(rs.getTimestamp("data_deposito"));
        r.setDataRitiro(rs.getTimestamp("data_ritiro"));
        r.setConfermaFinder(rs.getBoolean("conferma_finder"));
        r.setConfermaOwner(rs.getBoolean("conferma_owner"));
        r.setCodiceConsegna(rs.getString("codice_consegna"));
        try {
            r.setStato(StatoReclamo.valueOf(rs.getString("stato")));
        } catch (Exception e) {
            r.setStato(StatoReclamo.IN_ATTESA);
        }
        return r;
    }
    public Reclamo doRetrieveByCodiceAndDropPoint(String codice, long idDropPoint) {
        String sql = """
        SELECT r.*
        FROM reclamo r
        JOIN segnalazione_oggetto so ON so.id_segnalazione = r.id_segnalazione
        WHERE r.codice_consegna = ?
          AND so.modalita_consegna = 'DROP_POINT'
          AND so.id_drop_point = ?
        """;

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codice);
            ps.setLong(2, idDropPoint);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean marcaDeposito(long idReclamo) {
        String sql = "UPDATE reclamo SET data_deposito = CURRENT_TIMESTAMP WHERE id = ? AND data_deposito IS NULL";
        try (Connection con = ConPool.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idReclamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean marcaRitiro(long idReclamo) {
        String sql = "UPDATE reclamo SET data_ritiro = CURRENT_TIMESTAMP WHERE id = ? AND data_ritiro IS NULL";
        try (Connection con = ConPool.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idReclamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    public boolean setDataDepositoIfNull(long idReclamo) {
        String sql = "UPDATE reclamo SET data_deposito = NOW() WHERE id = ? AND data_deposito IS NULL";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idReclamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setDataRitiroIfNull(long idReclamo) {
        String sql = "UPDATE reclamo SET data_ritiro = NOW() WHERE id = ? AND data_ritiro IS NULL";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idReclamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countDepositiAttiviByDropPoint(long idDropPoint) {
        String sql = """
        SELECT COUNT(*) AS cnt
        FROM reclamo r
        JOIN segnalazione_oggetto so ON so.id_segnalazione = r.id_segnalazione
        WHERE so.modalita_consegna = 'DROP_POINT'
          AND so.id_drop_point = ?
          AND r.data_deposito IS NOT NULL
          AND r.data_ritiro IS NULL
        """;
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idDropPoint);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int countConsegneCompletateByDropPoint(long idDropPoint) {
        String sql = """
        SELECT COUNT(*) AS cnt
        FROM reclamo r
        JOIN segnalazione_oggetto so ON so.id_segnalazione = r.id_segnalazione
        WHERE so.modalita_consegna = 'DROP_POINT'
          AND so.id_drop_point = ?
          AND r.data_ritiro IS NOT NULL
        """;
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idDropPoint);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}