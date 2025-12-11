package model.service;

import model.bean.Reclamo;
import model.bean.Segnalazione;
import model.bean.SegnalazioneOggetto;
import model.bean.Utente;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.StatoSegnalazione;
import model.dao.ReclamoDAO;
import model.dao.SegnalazioneDAO;
import model.utils.GeocodingUtils;

import java.util.List;

public class SegnalazioneService {

    private final SegnalazioneDAO segnalazioneDAO = new SegnalazioneDAO();
    private final ReclamoDAO reclamoDAO = new ReclamoDAO();
    private final UtenteService utenteService = new UtenteService();

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

    public List<Segnalazione> getUltimeSegnalazioni() {
        return segnalazioneDAO.doRetrieveLatest(8);
    }

    public Segnalazione trovaPerId(long id) {
        return segnalazioneDAO.doRetrieveById(id);
    }

    public List<Segnalazione> trovaPerUtente(long idUtente) {
        return segnalazioneDAO.doRetrieveByUtente(idUtente);
    }

    public boolean eliminaSegnalazione(long id) {
        return segnalazioneDAO.doDelete(id);
    }

    /**
     * LOGICA DROP-POINT: Chiude e da punti quando il DP inserisce il codice.
     */
    public boolean accettaReclamoEChiudiSegnalazione(long idReclamo, long idSegnalazione, String codice) {
        boolean successoReclamo = reclamoDAO.accettaReclamo(idReclamo, codice);

        if (successoReclamo) {
            boolean successoSegnalazione = segnalazioneDAO.updateStato(idSegnalazione, StatoSegnalazione.CHIUSA);

            if (successoSegnalazione) {
                // Controllo se è un DropPoint prima di dare i punti
                Segnalazione s = segnalazioneDAO.doRetrieveById(idSegnalazione);
                if (s != null) {
                    boolean isDropPoint = false;
                    if (s instanceof SegnalazioneOggetto) {
                        SegnalazioneOggetto so = (SegnalazioneOggetto) s;
                        if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
                            isDropPoint = true;
                        }
                    }

                    // Se DropPoint, assegna subito il punto
                    if (isDropPoint) {
                        Utente finder = utenteService.trovaPerId(s.getIdUtente());
                        if (finder != null) {
                            utenteService.aggiornaPunteggioEBadge(finder, 1);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * LOGICA SCAMBIO DIRETTO: Chiude e da punti SOLO SE entrambi confermano.
     */
    public boolean gestisciConfermaScambio(long idReclamo, boolean isFinder) {
        // 1. Metti la spunta nel DB
        if (isFinder) {
            reclamoDAO.confermaFinder(idReclamo);
        } else {
            reclamoDAO.confermaOwner(idReclamo);
        }

        // 2. Ricarica il reclamo per vedere se ORA sono entrambi a true
        Reclamo r = reclamoDAO.doRetrieveById(idReclamo);

        if (r != null && r.isConfermaFinder() && r.isConfermaOwner()) {
            // ENTRAMBI HANNO CONFERMATO -> CHIUDI TUTTO E DAI PUNTI

            segnalazioneDAO.updateStato(r.getIdSegnalazione(), StatoSegnalazione.CHIUSA);

            Segnalazione s = segnalazioneDAO.doRetrieveById(r.getIdSegnalazione());
            if (s != null) {
                Utente finder = utenteService.trovaPerId(s.getIdUtente());
                if (finder != null) {
                    utenteService.aggiornaPunteggioEBadge(finder, 1); // +1 Punto al Finder
                }
            }
            return true; // Scambio completato
        }

        return false; // Manca ancora una conferma
    }

    public boolean rifiutaReclamo(long idReclamo) {
        return reclamoDAO.rifiutaReclamo(idReclamo);
    }

    public List<Segnalazione> cercaSegnalazioni(String q, String tipo, String categoria) {
        return segnalazioneDAO.doRetrieveByFiltri(q, tipo, categoria);
    }

    public List<Reclamo> trovaReclamiFattiDaUtente(long idUtente) {
        return reclamoDAO.doRetrieveByRichiedente(idUtente);
    }
}