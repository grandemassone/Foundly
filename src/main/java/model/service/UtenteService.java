package model.service;

import model.bean.Utente;
import model.bean.enums.Ruolo;
import model.dao.UtenteDAO;
import model.utils.PasswordUtils;

import java.util.List;

public class UtenteService {

    private final UtenteDAO utenteDAO = new UtenteDAO();

    /**
     * Registrazione di un nuovo utente "cittadino".
     */
    public boolean registraUtente(String nome,
                                  String cognome,
                                  String username,
                                  String email,
                                  String password,
                                  String telefono) {

        System.out.println("DEBUG: Inizio registrazione per " + email);

        // 1. Controllo Email
        if (utenteDAO.doRetrieveByEmail(email) != null) {
            System.out.println("DEBUG: Email già presente.");
            return false;
        }

        // 2. Controllo Username
        if (utenteDAO.doRetrieveByUsername(username) != null) {
            System.out.println("DEBUG: Username già presente.");
            return false;
        }

        System.out.println("DEBUG: Email e Username liberi. Procedo...");

        Utente nuovoUtente = new Utente();
        nuovoUtente.setNome(nome);
        nuovoUtente.setCognome(cognome);
        nuovoUtente.setUsername(username);
        nuovoUtente.setEmail(email);
        nuovoUtente.setTelefono(telefono);

        // Hash della password
        String passwordHash = PasswordUtils.hashPassword(password);
        nuovoUtente.setPasswordHash(passwordHash);

        // Impostazioni di default
        nuovoUtente.setRuolo(Ruolo.UTENTE_BASE);
        nuovoUtente.setPunteggio(0);
        nuovoUtente.setBadge("OCCHIO_DI_FALCO");
        nuovoUtente.setImmagineProfilo(null);

        boolean salvato = utenteDAO.doSave(nuovoUtente);
        System.out.println("DEBUG: Risultato salvataggio DAO: " + salvato);

        return salvato;
    }
    public List<Utente> getClassificaUtenti() {
        return utenteDAO.doRetrieveAllByPunteggio();
    }
    //commento fico

    /**
     * Login utente "cittadino" con email + password.
     */
    public Utente login(String email, String password) {
        // 1. Recupera l'utente dal DB
        Utente utente = utenteDAO.doRetrieveByEmail(email);
        if (utente == null) {
            return null; // Utente non trovato
        }

        // 2. Verifica la password (hash vs password in chiaro)
        if (PasswordUtils.checkPassword(password, utente.getPasswordHash())) {
            return utente; // Login successo
        }

        return null; // Password errata
    }

    /**
     * Comodo per il recupero password:
     * restituisce l'utente associato a una certa email,
     * oppure null se non esiste.
     */
    public Utente trovaPerEmail(String email) {
        return utenteDAO.doRetrieveByEmail(email);
    }

    /**
     * Usato nel flusso di recupero password:
     * - trova l'utente tramite email
     * - calcola il nuovo hash
     * - aggiorna la password nel DB
     *
     * @return true se l'aggiornamento va a buon fine, false altrimenti
     */
    public boolean resetPasswordByEmail(String email, String nuovaPassword) {
        Utente utente = utenteDAO.doRetrieveByEmail(email);
        if (utente == null) {
            return false; // nessun utente con questa email
        }

        // Applica gli stessi criteri di sicurezza che usi in registrazione (già validati a monte)
        String nuovoHash = PasswordUtils.hashPassword(nuovaPassword);
        utente.setPasswordHash(nuovoHash);

        return utenteDAO.updatePasswordByEmail(email, nuovoHash);
    }

    /* =========================================================
       METODI PER GESTIONE PROFILO
       ========================================================= */

    /**
     * Aggiorna i dati base del profilo (username, nome, cognome).
     * Si aspetta un Utente già caricato (es. da sessione) con id valorizzato.
     *
     * @param utente utente da aggiornare
     * @return true se l'update su DB va a buon fine, false altrimenti
     */
    public boolean aggiornaProfilo(Utente utente) {
        // id è un long primitivo: uso un controllo > 0 invece di null
        if (utente == null || utente.getId() <= 0) {
            return false;
        }

        // Controllo eventuale duplicato di username
        Utente esistente = utenteDAO.doRetrieveByUsername(utente.getUsername());
        if (esistente != null && esistente.getId() != utente.getId()) {
            // c'è già un altro utente con questo username
            return false;
        }

        return utenteDAO.updateProfilo(utente);
    }


    /**
     * (Opzionale) Recupera utente per id, utile se in futuro
     * ti serve ricaricare i dati dal DB.
     */
    public Utente trovaPerId(long id) {
        return utenteDAO.doRetrieveById(id);
    }
    /* ==========================
   LOGICA BADGE/PUNTEGGIO
   ========================== */

    /**
     * Ritorna il nome del badge (stringa ENUM) in base al punteggio.
     * 0-2 punti  -> OCCHIO_DI_FALCO
     * 3-4 punti  -> DETECTIVE
     * >=5 punti  -> SHERLOCK_HOLMES
     */
    // in UtenteService

    /**
     * 0-2 punti  -> OCCHIO_DI_FALCO
     * 3-4 punti  -> DETECTIVE
     * >=5 punti  -> SHERLOCK_HOLMES
     */
    private String calcolaBadgePerPunteggio(int punti) {
        if (punti >= 150) {
            return "SHERLOCK_HOLMES";
        } else if (punti >= 100) {
            return "DETECTIVE";
        } else {
            return "OCCHIO_DI_FALCO";
        }
    }

    public boolean aggiornaPunteggioEBadge(Utente utente, int deltaPunti) {
        if (utente == null || utente.getId() <= 0) {
            return false;
        }

        int nuovoPunteggio = utente.getPunteggio() + deltaPunti;
        if (nuovoPunteggio < 0) {
            nuovoPunteggio = 0;
        }

        String nuovoBadge = calcolaBadgePerPunteggio(nuovoPunteggio);

        utente.setPunteggio(nuovoPunteggio);
        utente.setBadge(nuovoBadge);

        return utenteDAO.updatePunteggioEBadge(utente);
    }
    // =========================================================
    // METODI AREA ADMIN
    // =========================================================

    /** Tutti gli utenti, usato in Area Admin. */
    public java.util.List<Utente> trovaTutti() {
        return utenteDAO.doRetrieveAll();
    }

    /** Ban = cancellazione account. */
    public boolean cancellaUtente(long id) {
        return utenteDAO.deleteById(id);
    }

    public List<String> getEmailAdmins() {
        return utenteDAO.doRetrieveAll().stream()
                .filter(u -> u.getRuolo() == model.bean.enums.Ruolo.ADMIN)
                .map(Utente::getEmail)
                .toList();
    }
}
