package model.service;

import model.bean.DropPoint;
import model.bean.Reclamo;
import model.bean.Segnalazione;
import model.bean.SegnalazioneOggetto;
import model.bean.Utente;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.StatoDropPoint;
import model.bean.enums.StatoSegnalazione;
import model.dao.DropPointDAO;
import model.dao.ReclamoDAO;
import model.dao.SegnalazioneDAO;
import model.utils.PasswordUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DropPointService {

    private final DropPointDAO dropPointDAO = new DropPointDAO();
    private final ReclamoDAO reclamoDAO = new ReclamoDAO();
    private final SegnalazioneDAO segnalazioneDAO = new SegnalazioneDAO();
    private final UtenteService utenteService = new UtenteService();

    // ==========================
    //  REGISTRAZIONE / LOGIN
    // ==========================

    public boolean registraDropPoint(String nomeAttivita, String email, String password,
                                     String indirizzo, String citta, String provincia,
                                     String telefono, String orari,
                                     Double latitudine, Double longitudine) {

        if (dropPointDAO.doRetrieveByEmail(email) != null) {
            return false;
        }

        DropPoint dp = new DropPoint();
        dp.setNomeAttivita(nomeAttivita);
        dp.setEmail(email);
        dp.setPasswordHash(PasswordUtils.hashPassword(password));
        dp.setIndirizzo(indirizzo);
        dp.setCitta(citta);
        dp.setProvincia(provincia);
        dp.setTelefono(telefono);
        dp.setOrariApertura(orari);
        dp.setLatitudine(latitudine);
        dp.setLongitudine(longitudine);

        dp.setStato(StatoDropPoint.IN_ATTESA);
        dp.setRitiriEffettuati(0);
        dp.setImmagine(null);
        dp.setImmagineContentType(null);

        return dropPointDAO.doSave(dp);
    }

    public DropPoint login(String email, String password) {
        DropPoint dp = dropPointDAO.doRetrieveByEmail(email);
        if (dp == null) return null;

        if (!PasswordUtils.checkPassword(password, dp.getPasswordHash())) {
            return null;
        }
        return dropPointDAO.doRetrieveById(dp.getId());
    }

    // ==========================
    //   QUERY DI SUPPORTO
    // ==========================

    public List<DropPoint> findAllApprovati() {
        return dropPointDAO.doRetrieveAllApprovati();
    }

    public List<DropPoint> findAllInAttesa() {
        return dropPointDAO.doRetrieveByStato(StatoDropPoint.IN_ATTESA);
    }

    public boolean approvaDropPoint(long id) {
        return dropPointDAO.updateStato(id, StatoDropPoint.APPROVATO);
    }

    public boolean rifiutaDropPoint(long id) {
        return dropPointDAO.updateStato(id, StatoDropPoint.RIFIUTATO);
    }

    public DropPoint trovaPerId(long id) {
        return dropPointDAO.doRetrieveById(id);
    }

    // =====================================================
    //   LOGICA REALE (DB) - SOSTITUISCE QUELLA IN-MEMORY
    // =====================================================

    /**
     * Registra un RITIRO (Consegna al proprietario).
     * Chiude la segnalazione e assegna i punti.
     */
    public boolean registraRitiro(long idDropPoint, String codiceConsegna) {
        if (codiceConsegna == null || codiceConsegna.isBlank()) {
            return false;
        }

        // 1. Cerca il reclamo associato a questo codice
        Reclamo r = reclamoDAO.doRetrieveByCodice(codiceConsegna);
        if (r == null) {
            return false; // Codice non esistente
        }

        // 2. Recupera la segnalazione
        Segnalazione s = segnalazioneDAO.doRetrieveById(r.getIdSegnalazione());
        if (s == null) return false;

        // 3. Verifica che la segnalazione sia assegnata a QUESTO DropPoint
        if (s instanceof SegnalazioneOggetto) {
            SegnalazioneOggetto so = (SegnalazioneOggetto) s;

            boolean isCorrectDP = (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT
                    && so.getIdDropPoint() != null
                    && so.getIdDropPoint() == idDropPoint);

            if (isCorrectDP) {
                // 4. CHIUDI SEGNALAZIONE
                boolean chiuso = segnalazioneDAO.updateStato(s.getId(), StatoSegnalazione.CHIUSA);

                if (chiuso) {
                    // 5. ASSEGNA PUNTI AL FINDER
                    Utente finder = utenteService.trovaPerId(s.getIdUtente());
                    if (finder != null) {
                        utenteService.aggiornaPunteggioEBadge(finder, 1);
                    }

                    // (Opzionale) Aggiorna contatore ritiri DropPoint nel DB
                    // dropPointDAO.incrementaRitiri(idDropPoint);

                    return true;
                }
            }
        }
        return false;
    }

    // Manteniamo questi metodi per compatibilità, ma ora registraRitiro usa il DB
    // (registraDeposito può rimanere placeholder o essere implementato similarmente)
    public boolean registraDeposito(long idDropPoint, String codiceConsegna) {
        // Implementazione base: verifica solo che il codice esista
        Reclamo r = reclamoDAO.doRetrieveByCodice(codiceConsegna);
        return r != null;
    }

    // Contatori (puoi lasciarli a 0 o implementarli con count DB se vuoi)
    public int countDepositiAttivi(long idDropPoint) { return 0; }
    public int countConsegneCompletate(long idDropPoint) { return 0; }
    public int countTotaleOperazioni(long idDropPoint) { return 0; }

    public boolean aggiornaProfilo(DropPoint dropPoint) {
        return dropPointDAO.updateProfilo(dropPoint);
    }
    public boolean eliminaDropPoint(long id) {
        return dropPointDAO.doDeleteById(id);
    }

}