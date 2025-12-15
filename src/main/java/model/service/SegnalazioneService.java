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
    private final UtenteService utenteService = new UtenteService(); // Per gestire i punti

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

    // --- LOGICA DROP-POINT (Con Codice) ---
    public boolean accettaReclamoEChiudiSegnalazione(long idReclamo, long idSegnalazione, String codice) {
        boolean okReclamo = reclamoDAO.accettaReclamo(idReclamo, codice);
        if (!okReclamo) return false;

        // NON chiudere qui: verrà chiusa dal DropPoint quando registra il ritiro
        return segnalazioneDAO.updateStato(idSegnalazione, StatoSegnalazione.IN_CONSEGNA);
    }

    // --- LOGICA SCAMBIO DIRETTO (Doppia Conferma) ---
    public boolean gestisciConfermaScambio(long idReclamo, boolean isFinder) {
        // 1. Registra la conferma singola
        if (isFinder) {
            reclamoDAO.confermaFinder(idReclamo);
        } else {
            reclamoDAO.confermaOwner(idReclamo);
        }

        // 2. Controlla se ORA sono entrambi confermati
        Reclamo r = reclamoDAO.doRetrieveById(idReclamo);
        if (r != null && r.isConfermaFinder() && r.isConfermaOwner()) {

            // Entrambi hanno confermato: Chiudi e dai punti
            segnalazioneDAO.updateStato(r.getIdSegnalazione(), StatoSegnalazione.CHIUSA);

            Segnalazione s = segnalazioneDAO.doRetrieveById(r.getIdSegnalazione());
            if (s != null) {
                Utente finder = utenteService.trovaPerId(s.getIdUtente());
                if (finder != null) {
                    utenteService.aggiornaPunteggioEBadge(finder, 1);
                }
            }
            return true; // Scambio completato
        }
        return false; // Manca l'altro utente
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

    public List<Segnalazione> trovaTutte() {
        return segnalazioneDAO.doRetrieveAll(); // lo aggiungi nel DAO
    }

}