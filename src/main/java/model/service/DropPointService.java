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

public class DropPointService {

    private final DropPointDAO dropPointDAO = new DropPointDAO();
    private final ReclamoDAO reclamoDAO = new ReclamoDAO();
    private final SegnalazioneDAO segnalazioneDAO = new SegnalazioneDAO();
    private final UtenteService utenteService = new UtenteService();
    private final EmailService emailService = new EmailService();

    // ==========================
    //  REGISTRAZIONE / LOGIN
    // ==========================

    public boolean registraDropPoint(String nomeAttivita, String email, String password,
                                     String indirizzo, String citta, String provincia,
                                     String telefono, String orari,
                                     Double latitudine, Double longitudine) {

        if (dropPointDAO.doRetrieveByEmail(email) != null) return false;

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

        boolean salvato = dropPointDAO.doSave(dp);

        // NOTIFICA ADMIN (chiamata diretta, EmailService gestisce il thread)
        if (salvato) {
            List<String> admins = utenteService.getEmailAdmins();
            for (String adminEmail : admins) {
                emailService.inviaNotificaAdminNuovoDropPoint(adminEmail, nomeAttivita, citta);
            }
        }
        return salvato;
    }

    public DropPoint login(String email, String password) {
        DropPoint dp = dropPointDAO.doRetrieveByEmail(email);
        if (dp == null) return null;
        if (!PasswordUtils.checkPassword(password, dp.getPasswordHash())) return null;
        return dropPointDAO.doRetrieveById(dp.getId());
    }

    // ==========================
    //   GESTIONE APPROVAZIONE
    // ==========================

    public boolean approvaDropPoint(long id) {
        boolean aggiornato = dropPointDAO.updateStato(id, StatoDropPoint.APPROVATO);

        // NOTIFICA DROP-POINT (chiamata diretta)
        if (aggiornato) {
            DropPoint dp = dropPointDAO.doRetrieveById(id);
            if (dp != null) {
                emailService.inviaAccettazioneDropPoint(dp.getEmail(), dp.getNomeAttivita());
            }
        }
        return aggiornato;
    }

    public boolean rifiutaDropPoint(long id) {
        return dropPointDAO.updateStato(id, StatoDropPoint.RIFIUTATO);
    }

    public DropPoint trovaPerId(long id) {
        return dropPointDAO.doRetrieveById(id);
    }

    // ==========================
    //   ALTRI METODI INVARIATI
    // ==========================

    public boolean registraRitiro(long idDropPoint, String codiceConsegna) {
        if (codiceConsegna == null || codiceConsegna.isBlank()) return false;
        Reclamo r = reclamoDAO.doRetrieveByCodice(codiceConsegna);
        if (r == null) return false;
        Segnalazione s = segnalazioneDAO.doRetrieveById(r.getIdSegnalazione());
        if (s == null) return false;

        if (s instanceof SegnalazioneOggetto) {
            SegnalazioneOggetto so = (SegnalazioneOggetto) s;
            boolean isCorrectDP = (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT
                    && so.getIdDropPoint() != null
                    && so.getIdDropPoint() == idDropPoint);

            if (isCorrectDP) {
                boolean chiuso = segnalazioneDAO.updateStato(s.getId(), StatoSegnalazione.CHIUSA);
                if (chiuso) {
                    Utente finder = utenteService.trovaPerId(s.getIdUtente());
                    if (finder != null) {
                        utenteService.aggiornaPunteggioEBadge(finder, 1);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean registraDeposito(long idDropPoint, String codiceConsegna) {
        Reclamo r = reclamoDAO.doRetrieveByCodice(codiceConsegna);
        return r != null;
    }

    public int countDepositiAttivi(long id) { return 0; }
    public int countConsegneCompletate(long id) { return 0; }
    public int countTotaleOperazioni(long id) { return 0; }
    public boolean aggiornaProfilo(DropPoint dp) { return dropPointDAO.updateProfilo(dp); }
    public boolean eliminaDropPoint(long id) { return dropPointDAO.doDeleteById(id); }
    public List<DropPoint> findAllApprovati() { return dropPointDAO.doRetrieveAllApprovati(); }
    public List<DropPoint> findAllInAttesa() { return dropPointDAO.doRetrieveByStato(StatoDropPoint.IN_ATTESA); }
}