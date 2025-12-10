package model.dao;

import model.ConPool;
import model.bean.Reclamo;
import model.bean.enums.StatoReclamo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamoDAO {

    public boolean doSave(Reclamo r) {
        String query = "INSERT INTO reclamo (id_segnalazione, id_utente_richiedente, risposta_verifica1, risposta_verifica2, stato, data_richiesta) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, r.getIdSegnalazione());
            ps.setLong(2, r.getIdUtenteRichiedente());
            ps.setString(3, r.getRispostaVerifica1());
            ps.setString(4, r.getRispostaVerifica2());
            ps.setString(5, StatoReclamo.IN_ATTESA.toString());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

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
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * NUOVO METODO: Controlla se un utente ha già fatto reclamo per una segnalazione
     */
    public Reclamo doRetrieveBySegnalazioneAndUtente(long idSegnalazione, long idUtente) {
        String query = "SELECT * FROM reclamo WHERE id_segnalazione = ? AND id_utente_richiedente = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, idSegnalazione);
            ps.setLong(2, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean accettaReclamo(long idReclamo, String codice) {
        String query = "UPDATE reclamo SET stato = 'ACCETTATO', codice_consegna = ? WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, codice);
            ps.setLong(2, idReclamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Reclamo doRetrieveById(long id) {
        String query = "SELECT * FROM reclamo WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private Reclamo mapRow(ResultSet rs) throws SQLException {
        Reclamo r = new Reclamo();
        r.setId(rs.getLong("id"));
        r.setIdSegnalazione(rs.getLong("id_segnalazione"));
        r.setIdUtenteRichiedente(rs.getLong("id_utente_richiedente"));
        r.setRispostaVerifica1(rs.getString("risposta_verifica1"));
        r.setRispostaVerifica2(rs.getString("risposta_verifica2"));
        try {
            r.setStato(StatoReclamo.valueOf(rs.getString("stato")));
        } catch (Exception e) {}
        r.setCodiceConsegna(rs.getString("codice_consegna"));
        return r;
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
        // Query con JOIN per prendere i dati della segnalazione
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
                    Reclamo r = new Reclamo();
                    r.setId(rs.getLong("id"));
                    r.setIdSegnalazione(rs.getLong("id_segnalazione"));
                    r.setIdUtenteRichiedente(rs.getLong("id_utente_richiedente"));
                    r.setRispostaVerifica1(rs.getString("risposta_verifica1"));
                    r.setRispostaVerifica2(rs.getString("risposta_verifica2"));

                    // --- CORREZIONE QUI ---
                    // Passiamo direttamente il Timestamp al Bean
                    r.setDataRichiesta(rs.getTimestamp("data_richiesta"));

                    try {
                        r.setStato(model.bean.enums.StatoReclamo.valueOf(rs.getString("stato")));
                    } catch (Exception e) {
                        r.setStato(model.bean.enums.StatoReclamo.IN_ATTESA);
                    }

                    r.setCodiceConsegna(rs.getString("codice_consegna"));

                    // --- POPOLIAMO I DATI DELLA SEGNALAZIONE ---
                    // Assicurati di usare gli alias corretti definiti nella query (AS seg_titolo)
                    r.setTitoloSegnalazione(rs.getString("seg_titolo"));
                    r.setImmagineSegnalazione(rs.getString("seg_immagine"));

                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}