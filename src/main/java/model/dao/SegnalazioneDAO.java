package model.dao;

import model.ConPool;
import model.bean.Segnalazione;
import model.bean.SegnalazioneAnimale;
import model.bean.SegnalazioneOggetto;
import model.bean.enums.CategoriaOggetto;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.StatoSegnalazione;
import model.bean.enums.TipoSegnalazione;

import java.sql.*;
import java.util.ArrayList;
import java.util.List; // FONDAMENTALE: java.util, non java.awt

public class SegnalazioneDAO {

    public boolean updateStato(long id, StatoSegnalazione nuovoStato) {
        String query = "UPDATE segnalazione SET stato = ? WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, nuovoStato.toString());
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean doSave(Segnalazione s) {
        String sqlPadre = "INSERT INTO segnalazione (id_utente, titolo, descrizione, data_ritrovamento, " +
                "luogo_ritrovamento, citta, provincia, latitudine, longitudine, immagine, " +
                "domanda_verifica1, domanda_verifica2, stato, tipo_segnalazione) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConPool.getConnection();
             PreparedStatement psPadre = con.prepareStatement(sqlPadre, Statement.RETURN_GENERATED_KEYS)) {

            con.setAutoCommit(false); // Transazione
            psPadre.setLong(1, s.getIdUtente());
            psPadre.setString(2, s.getTitolo());
            psPadre.setString(3, s.getDescrizione());
            psPadre.setTimestamp(4, s.getDataRitrovamento());
            psPadre.setString(5, s.getLuogoRitrovamento());
            psPadre.setString(6, s.getCitta());
            psPadre.setString(7, s.getProvincia());
            psPadre.setObject(8, s.getLatitudine());
            psPadre.setObject(9, s.getLongitudine());
            psPadre.setString(10, s.getImmagine());
            psPadre.setString(11, s.getDomandaVerifica1());
            psPadre.setString(12, s.getDomandaVerifica2());
            psPadre.setString(13, s.getStato().toString());
            psPadre.setString(14, s.getTipoSegnalazione().toString());

            int result = psPadre.executeUpdate();

            if (result > 0) {
                try (ResultSet generatedKeys = psPadre.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        s.setId(id);
                        if (s instanceof SegnalazioneOggetto) saveOggetto(con, (SegnalazioneOggetto) s);
                        else if (s instanceof SegnalazioneAnimale) saveAnimale(con, (SegnalazioneAnimale) s);
                        con.commit();
                        return true;
                    }
                }
            }
            con.rollback();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private void saveOggetto(Connection con, SegnalazioneOggetto so) throws SQLException {
        String sql = "INSERT INTO segnalazione_oggetto (id_segnalazione, categoria, modalita_consegna, id_drop_point) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, so.getId());
            ps.setString(2, so.getCategoria().toString());
            ps.setString(3, so.getModalitaConsegna().toString());
            ps.setObject(4, so.getIdDropPoint());
            ps.executeUpdate();
        }
    }

    private void saveAnimale(Connection con, SegnalazioneAnimale sa) throws SQLException {
        String sql = "INSERT INTO segnalazione_animale (id_segnalazione, specie, razza) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, sa.getId());
            ps.setString(2, sa.getSpecie());
            ps.setString(3, sa.getRazza());
            ps.executeUpdate();
        }
    }

    public List<Segnalazione> doRetrieveLatest(int limit) {
        List<Segnalazione> list = new ArrayList<>();
        String query = "SELECT s.*, so.categoria, so.modalita_consegna, so.id_drop_point, sa.specie, sa.razza " +
                "FROM segnalazione s " +
                "LEFT JOIN segnalazione_oggetto so ON s.id = so.id_segnalazione " +
                "LEFT JOIN segnalazione_animale sa ON s.id = sa.id_segnalazione " +
                "WHERE s.stato IN ('APERTA', 'CHIUSA') " +
                "ORDER BY s.data_pubblicazione DESC LIMIT ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Segnalazione doRetrieveById(long id) {
        String query = "SELECT s.*, so.categoria, so.modalita_consegna, so.id_drop_point, sa.specie, sa.razza " +
                "FROM segnalazione s " +
                "LEFT JOIN segnalazione_oggetto so ON s.id = so.id_segnalazione " +
                "LEFT JOIN segnalazione_animale sa ON s.id = sa.id_segnalazione " +
                "WHERE s.id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Segnalazione> doRetrieveByUtente(long idUtente) {
        List<Segnalazione> list = new ArrayList<>();
        String query = "SELECT s.*, so.categoria, so.modalita_consegna, so.id_drop_point, sa.specie, sa.razza " +
                "FROM segnalazione s " +
                "LEFT JOIN segnalazione_oggetto so ON s.id = so.id_segnalazione " +
                "LEFT JOIN segnalazione_animale sa ON s.id = sa.id_segnalazione " +
                "WHERE s.id_utente = ? ORDER BY s.data_pubblicazione DESC";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean doDelete(long id) {
        String query = "DELETE FROM segnalazione WHERE id = ?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Segnalazione> doRetrieveByFiltri(String queryTesto, String tipo, String categoria) {
        StringBuilder sql = new StringBuilder("SELECT s.*, so.categoria, so.modalita_consegna, so.id_drop_point, sa.specie, sa.razza " +
                "FROM segnalazione s " +
                "LEFT JOIN segnalazione_oggetto so ON s.id = so.id_segnalazione " +
                "LEFT JOIN segnalazione_animale sa ON s.id = sa.id_segnalazione " +
                "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        // Filtro Testo
        if (queryTesto != null && !queryTesto.trim().isEmpty()) {
            sql.append(" AND (s.titolo LIKE ? OR s.descrizione LIKE ? OR s.luogo_ritrovamento LIKE ?)");
            String likeQuery = "%" + queryTesto + "%";
            params.add(likeQuery);
            params.add(likeQuery);
            params.add(likeQuery);
        }

        // Filtro Tipo (Gestisce null e stringhe vuote)
        if (tipo != null && !tipo.trim().isEmpty()) {
            sql.append(" AND s.tipo_segnalazione = ?");
            params.add(tipo.toUpperCase());
        }

        // Filtro Categoria (Gestisce null e stringhe vuote)
        if (categoria != null && !categoria.trim().isEmpty()) {
            sql.append(" AND so.categoria = ?");
            params.add(categoria.toUpperCase());
        }

        sql.append(" ORDER BY s.data_pubblicazione DESC");

        List<Segnalazione> list = new ArrayList<>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs)); // Assicurati di avere il metodo mapRow definito nel DAO
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- MAPPER UNICO PER EVITARE DUPLICAZIONI ---
    private Segnalazione mapRow(ResultSet rs) throws SQLException {
        Segnalazione s;
        String tipo = rs.getString("tipo_segnalazione");

        if ("OGGETTO".equals(tipo)) {
            SegnalazioneOggetto so = new SegnalazioneOggetto();
            try {
                String cat = rs.getString("categoria");
                if (cat != null) so.setCategoria(CategoriaOggetto.valueOf(cat));

                String mod = rs.getString("modalita_consegna");
                if (mod != null) so.setModalitaConsegna(ModalitaConsegna.valueOf(mod));
            } catch (Exception e) {}
            so.setIdDropPoint(rs.getObject("id_drop_point", Long.class));
            s = so;
        } else {
            SegnalazioneAnimale sa = new SegnalazioneAnimale();
            sa.setSpecie(rs.getString("specie"));
            sa.setRazza(rs.getString("razza"));
            s = sa;
        }

        s.setId(rs.getLong("id"));
        s.setIdUtente(rs.getLong("id_utente"));
        s.setTitolo(rs.getString("titolo"));
        s.setDescrizione(rs.getString("descrizione"));
        s.setDataRitrovamento(rs.getTimestamp("data_ritrovamento"));
        s.setLuogoRitrovamento(rs.getString("luogo_ritrovamento"));
        s.setCitta(rs.getString("citta"));
        s.setProvincia(rs.getString("provincia"));
        s.setLatitudine(rs.getObject("latitudine", Double.class));
        s.setLongitudine(rs.getObject("longitudine", Double.class));
        s.setImmagine(rs.getString("immagine"));
        s.setDomandaVerifica1(rs.getString("domanda_verifica1"));
        s.setDomandaVerifica2(rs.getString("domanda_verifica2"));
        s.setDataPubblicazione(rs.getTimestamp("data_pubblicazione"));

        try {
            s.setStato(StatoSegnalazione.valueOf(rs.getString("stato")));
            s.setTipoSegnalazione(TipoSegnalazione.valueOf(tipo));
        } catch (Exception e) {}

        return s;
    }
}