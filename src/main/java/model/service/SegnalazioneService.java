package model.service;

import model.bean.Segnalazione;
import model.bean.enums.StatoSegnalazione;
import model.dao.ReclamoDAO;
import model.dao.SegnalazioneDAO;
import model.utils.GeocodingUtils;

public class SegnalazioneService {

    private final SegnalazioneDAO segnalazioneDAO = new SegnalazioneDAO();
    private final ReclamoDAO reclamoDAO = new ReclamoDAO();

    public boolean creaSegnalazione(Segnalazione segnalazione) {
        if (segnalazione.getLuogoRitrovamento() != null && !segnalazione.getLuogoRitrovamento().isEmpty() &&
                segnalazione.getCitta() != null && !segnalazione.getCitta().isEmpty()) {
            double[] coords = GeocodingUtils.getCoordinates(
                    segnalazione.getLuogoRitrovamento(),
                    segnalazione.getCitta(),
                    segnalazione.getProvincia()
            );
            segnalazione.setLatitudine(coords[0]);
            segnalazione.setLongitudine(coords[1]);
        }
        return segnalazioneDAO.doSave(segnalazione);
    }

    public java.util.List<Segnalazione> getUltimeSegnalazioni() {
        return segnalazioneDAO.doRetrieveLatest(8);
    }

    public Segnalazione trovaPerId(long id) {
        return segnalazioneDAO.doRetrieveById(id);
    }

    public java.util.List<Segnalazione> trovaPerUtente(long idUtente) {
        return segnalazioneDAO.doRetrieveByUtente(idUtente);
    }

    public boolean eliminaSegnalazione(long id) {
        return segnalazioneDAO.doDelete(id);
    }

    /**
     * NUOVO METODO: Accetta reclamo e chiude la segnalazione.
     */
    public boolean accettaReclamoEChiudiSegnalazione(long idReclamo, long idSegnalazione, String codice) {
        boolean successo = reclamoDAO.accettaReclamo(idReclamo, codice);
        if (successo) {
            // Se accetto il reclamo, la segnalazione diventa CHIUSA
            return segnalazioneDAO.updateStato(idSegnalazione, StatoSegnalazione.CHIUSA);
        }
        return false;
    }

    public boolean rifiutaReclamo(long idReclamo) {
        return reclamoDAO.rifiutaReclamo(idReclamo);
    }
}