package model.service;

import model.bean.Utente;
import model.bean.enums.Ruolo;
import model.dao.UtenteDAO;
import model.utils.PasswordUtils;

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
        nuovoUtente.setImmagineProfilo("img/default-avatar.png");

        boolean salvato = utenteDAO.doSave(nuovoUtente);
        System.out.println("DEBUG: Risultato salvataggio DAO: " + salvato);

        return salvato;
    }

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

        // Qui hai due possibilità:
        // 1) se hai un metodo generico di update (es. doUpdate(utente)):
        //    return utenteDAO.doUpdate(utente);
        //
        // 2) oppure definisci nel DAO un metodo dedicato:
        //    boolean updatePasswordByEmail(String email, String nuovoHash)
        //    e lo usi così:

        return utenteDAO.updatePasswordByEmail(email, nuovoHash);
    }
}
