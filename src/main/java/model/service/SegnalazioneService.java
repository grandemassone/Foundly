package model.service;

import model.bean.Segnalazione;
import model.dao.SegnalazioneDAO;
import model.utils.GeocodingUtils; // Importa la utility

public class SegnalazioneService {

    private final SegnalazioneDAO segnalazioneDAO = new SegnalazioneDAO();

    public boolean creaSegnalazione(Segnalazione segnalazione) {

        // 1. Calcolo automatico Latitudine e Longitudine
        // CORREZIONE: getIndirizzo() -> getLuogoRitrovamento()
        if (segnalazione.getLuogoRitrovamento() != null && !segnalazione.getLuogoRitrovamento().isEmpty() &&
                segnalazione.getCitta() != null && !segnalazione.getCitta().isEmpty()) {

            double[] coords = GeocodingUtils.getCoordinates(
                    segnalazione.getLuogoRitrovamento(), // QUI ERA L'ERRORE
                    segnalazione.getCitta(),
                    segnalazione.getProvincia()
            );

            segnalazione.setLatitudine(coords[0]);
            segnalazione.setLongitudine(coords[1]);
        }

        // 2. Salvataggio nel DB
        return segnalazioneDAO.doSave(segnalazione);
    }

    public java.util.List<Segnalazione> getUltimeSegnalazioni() {
        // Recuperiamo ad esempio le ultime 8 per la Home Page
        return segnalazioneDAO.doRetrieveLatest(8);
    }

    public Segnalazione trovaPerId(long id) {
        return segnalazioneDAO.doRetrieveById(id);
    }

    public java.util.List<Segnalazione> trovaPerUtente(long idUtente) {
        return segnalazioneDAO.doRetrieveByUtente(idUtente);
    }

    public boolean eliminaSegnalazione(long id) {
        // Qui si potrebbe aggiungere controllo se l'utente è il proprietario
        return segnalazioneDAO.doDelete(id);
    }
}