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

    // Recupera tutti i reclami per una data segnalazione (per il proprietario)
    public List<Reclamo> doRetrieveBySegnalazione(long idSegnalazione) {
        List<Reclamo> list = new ArrayList<>();
        String query = "SELECT * FROM reclamo WHERE id_segnalazione = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, idSegnalazione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reclamo r = new Reclamo();
                    r.setId(rs.getLong("id"));
                    r.setIdSegnalazione(rs.getLong("id_segnalazione"));
                    r.setIdUtenteRichiedente(rs.getLong("id_utente_richiedente"));
                    r.setRispostaVerifica1(rs.getString("risposta_verifica1"));
                    r.setRispostaVerifica2(rs.getString("risposta_verifica2"));
                    r.setStato(StatoReclamo.valueOf(rs.getString("stato")));
                    r.setCodiceConsegna(rs.getString("codice_consegna"));
                    list.add(r);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
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
                if(rs.next()){
                    Reclamo r = new Reclamo();
                    r.setId(rs.getLong("id"));
                    r.setIdSegnalazione(rs.getLong("id_segnalazione"));
                    r.setIdUtenteRichiedente(rs.getLong("id_utente_richiedente"));
                    r.setStato(StatoReclamo.valueOf(rs.getString("stato")));
                    return r;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}