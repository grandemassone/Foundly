package model.service;

import model.bean.DropPoint;
import model.bean.Reclamo;
import model.bean.Segnalazione;
import model.bean.SegnalazioneOggetto;
import model.bean.Utente;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.StatoDropPoint;
import model.bean.enums.StatoReclamo;
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

    public boolean registraRitiro(long idDropPoint, String codiceRitiro) {
        Reclamo r = reclamoDAO.doRetrieveByCodiceAndDropPoint(codiceRitiro, idDropPoint);
        if (r == null) return false;

        // deve essere stato depositato prima
        if (r.getDataDeposito() == null) return false;

        // evita doppie registrazioni
        if (r.getDataRitiro() != null) return false;

        // 1) marca ritiro
        boolean okRitiro = reclamoDAO.marcaRitiro(r.getId());
        if (!okRitiro) return false;

        // 2) chiudi segnalazione
        boolean okChiusa = segnalazioneDAO.updateStato(r.getIdSegnalazione(), StatoSegnalazione.CHIUSA);
        if (!okChiusa) return false;

        // 3) assegna punto al finder
        Segnalazione s = segnalazioneDAO.doRetrieveById(r.getIdSegnalazione());
        if (s != null) {
            Utente finder = utenteService.trovaPerId(s.getIdUtente());
            if (finder != null) utenteService.aggiornaPunteggioEBadge(finder, 1);
        }

        return true;
    }

    public boolean registraDeposito(long idDropPoint, String codiceConsegna) {
        Reclamo r = reclamoDAO.doRetrieveByCodiceAndDropPoint(codiceConsegna, idDropPoint);
        if (r == null) return false;

        // evita doppie registrazioni
        if (r.getDataDeposito() != null) return false;

        return reclamoDAO.marcaDeposito(r.getId());
    }


    public int countDepositiAttivi(long idDropPoint) {
        return reclamoDAO.countDepositiAttiviByDropPoint(idDropPoint);
    }
    public int countConsegneCompletate(long idDropPoint) {
        return reclamoDAO.countConsegneCompletateByDropPoint(idDropPoint);
    }
    public boolean aggiornaProfilo(DropPoint dp) { return dropPointDAO.updateProfilo(dp); }
    public boolean eliminaDropPoint(long id) { return dropPointDAO.doDeleteById(id); }
    public List<DropPoint> findAllApprovati() { return dropPointDAO.doRetrieveAllApprovati(); }
    public List<DropPoint> findAllInAttesa() { return dropPointDAO.doRetrieveByStato(StatoDropPoint.IN_ATTESA); }
}