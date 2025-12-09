package model.dao;

import model.ConPool;
import model.bean.Segnalazione;
import model.bean.SegnalazioneAnimale;
import model.bean.SegnalazioneOggetto;
import model.bean.enums.TipoSegnalazione;

import java.sql.*;

public class SegnalazioneDAO {

    /**
     * Salva una segnalazione (Oggetto o Animale) gestendo la strategia Joined.
     */
    public boolean doSave(Segnalazione s) {
        Connection con = null;
        PreparedStatement psPadre = null;
        PreparedStatement psFiglio = null;

        // Query per la tabella padre
        String sqlPadre = "INSERT INTO segnalazione (id_utente, titolo, descrizione, data_ritrovamento, " +
                "luogo_ritrovamento, citta, provincia, latitudine, longitudine, immagine, " +
                "domanda_verifica1, domanda_verifica2, stato, tipo_segnalazione) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            con = ConPool.getConnection();
            con.setAutoCommit(false); // Iniziamo una transazione manuale

            // 1. Inserimento tabella PADRE
            psPadre = con.prepareStatement(sqlPadre, Statement.RETURN_GENERATED_KEYS);
            psPadre.setLong(1, s.getIdUtente());
            psPadre.setString(2, s.getTitolo());
            psPadre.setString(3, s.getDescrizione());
            psPadre.setTimestamp(4, s.getDataRitrovamento());
            psPadre.setString(5, s.getLuogoRitrovamento());
            psPadre.setString(6, s.getCitta());
            psPadre.setString(7, s.getProvincia());
            psPadre.setDouble(8, s.getLatitudine());
            psPadre.setDouble(9, s.getLongitudine());
            psPadre.setString(10, s.getImmagine());
            psPadre.setString(11, s.getDomandaVerifica1());
            psPadre.setString(12, s.getDomandaVerifica2());
            psPadre.setString(13, s.getStato().toString());
            psPadre.setString(14, s.getTipoSegnalazione().toString());

            int resultPadre = psPadre.executeUpdate();

            if (resultPadre > 0) {
                // Recuperiamo ID generato
                ResultSet generatedKeys = psPadre.getGeneratedKeys();
                if (generatedKeys.next()) {
                    long idSegnalazione = generatedKeys.getLong(1);
                    s.setId(idSegnalazione);

                    // 2. Inserimento tabella FIGLIA in base al tipo
                    if (s instanceof SegnalazioneOggetto) {
                        SegnalazioneOggetto so = (SegnalazioneOggetto) s;
                        String sqlOggetto = "INSERT INTO segnalazione_oggetto (id_segnalazione, categoria, modalita_consegna, id_drop_point) VALUES (?, ?, ?, ?)";
                        psFiglio = con.prepareStatement(sqlOggetto);
                        psFiglio.setLong(1, idSegnalazione);
                        psFiglio.setString(2, so.getCategoria().toString());
                        psFiglio.setString(3, so.getModalitaConsegna().toString());

                        if (so.getIdDropPoint() != null && so.getIdDropPoint() > 0) {
                            psFiglio.setLong(4, so.getIdDropPoint());
                        } else {
                            psFiglio.setNull(4, Types.BIGINT);
                        }

                    } else if (s instanceof SegnalazioneAnimale) {
                        SegnalazioneAnimale sa = (SegnalazioneAnimale) s;
                        String sqlAnimale = "INSERT INTO segnalazione_animale (id_segnalazione, specie, razza) VALUES (?, ?, ?)";
                        psFiglio = con.prepareStatement(sqlAnimale);
                        psFiglio.setLong(1, idSegnalazione);
                        psFiglio.setString(2, sa.getSpecie());
                        psFiglio.setString(3, sa.getRazza());
                    }

                    // Eseguiamo insert figlio
                    if (psFiglio != null) {
                        psFiglio.executeUpdate();
                    }

                    con.commit(); // Conferma tutto
                    return true;
                }
            }
            con.rollback(); // Se qualcosa va storto, annulla l'inserimento padre
            return false;

        } catch (SQLException e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            // Chiudiamo tutto manualmente perché non usiamo try-with-resources per gestire il rollback
            try {
                if (psPadre != null) psPadre.close();
                if (psFiglio != null) psFiglio.close();
                if (con != null) {
                    con.setAutoCommit(true); // Ripristina default
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Recupera le ultime N segnalazioni ordinate per data decrescente.
     */
    public java.util.List<Segnalazione> doRetrieveLatest(int limit) {
        java.util.List<Segnalazione> list = new java.util.ArrayList<>();

        // Query che unisce Padre + Figlio Oggetto + Figlio Animale
        String query = "SELECT s.*, " +
                "so.categoria, so.modalita_consegna, so.id_drop_point, " +
                "sa.specie, sa.razza " +
                "FROM segnalazione s " +
                "LEFT JOIN segnalazione_oggetto so ON s.id = so.id_segnalazione " +
                "LEFT JOIN segnalazione_animale sa ON s.id = sa.id_segnalazione " +
                "WHERE s.stato = 'APERTA' " + // Mostriamo solo quelle aperte
                "ORDER BY s.data_pubblicazione DESC " +
                "LIMIT ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Segnalazione s = null;
                    String tipoStr = rs.getString("tipo_segnalazione");

                    // Istanziamo il bean corretto in base al tipo
                    if ("OGGETTO".equals(tipoStr)) {
                        SegnalazioneOggetto so = new SegnalazioneOggetto();
                        try {
                            so.setCategoria(model.bean.enums.CategoriaOggetto.valueOf(rs.getString("categoria")));
                            so.setModalitaConsegna(model.bean.enums.ModalitaConsegna.valueOf(rs.getString("modalita_consegna")));
                        } catch (Exception e) { /* Enum error handling */ }
                        so.setIdDropPoint(rs.getLong("id_drop_point"));
                        if (rs.wasNull()) so.setIdDropPoint(null);
                        s = so;
                    } else {
                        SegnalazioneAnimale sa = new SegnalazioneAnimale();
                        sa.setSpecie(rs.getString("specie"));
                        sa.setRazza(rs.getString("razza"));
                        s = sa;
                    }

                    // Popoliamo i dati comuni (Tabella Padre)
                    s.setId(rs.getLong("id"));
                    s.setIdUtente(rs.getLong("id_utente"));
                    s.setTitolo(rs.getString("titolo"));
                    s.setDescrizione(rs.getString("descrizione"));
                    s.setDataRitrovamento(rs.getTimestamp("data_ritrovamento"));
                    s.setLuogoRitrovamento(rs.getString("luogo_ritrovamento"));
                    s.setCitta(rs.getString("citta"));
                    s.setProvincia(rs.getString("provincia"));
                    s.setLatitudine(rs.getDouble("latitudine"));
                    s.setLongitudine(rs.getDouble("longitudine"));
                    s.setImmagine(rs.getString("immagine"));
                    s.setDataPubblicazione(rs.getTimestamp("data_pubblicazione"));

                    try {
                        s.setStato(model.bean.enums.StatoSegnalazione.valueOf(rs.getString("stato")));
                        s.setTipoSegnalazione(model.bean.enums.TipoSegnalazione.valueOf(tipoStr));
                    } catch (Exception e) { /* Enum handling */ }

                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}