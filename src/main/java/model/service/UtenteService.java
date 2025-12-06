package model.service;

import model.bean.Utente;
import model.bean.enums.Ruolo;
import model.dao.UtenteDAO;
import model.utils.PasswordUtils;

import java.sql.Timestamp;
import java.time.Instant;

public class UtenteService {

    private final UtenteDAO utenteDAO = new UtenteDAO();
    public boolean registraUtente(String nome, String cognome, String username, String email, String password, String telefono) {
        System.out.println("🔍 DEBUG: Inizio registrazione per " + email);

        // 1. Controllo Email
        if (utenteDAO.doRetrieveByEmail(email) != null) {
            System.out.println("❌ DEBUG: Email già presente.");
            return false;
        }

        // 2. Controllo Username (NUOVO!)
        if (utenteDAO.doRetrieveByUsername(username) != null) {
            System.out.println("❌ DEBUG: Username già presente.");
            return false;
        }

        System.out.println("✅ DEBUG: Email e Username liberi. Procedo...");

        Utente nuovoUtente = new Utente();
        nuovoUtente.setNome(nome);
        nuovoUtente.setCognome(cognome);
        nuovoUtente.setUsername(username);
        nuovoUtente.setEmail(email);
        nuovoUtente.setTelefono(telefono);

        String passwordHash = PasswordUtils.hashPassword(password);
        nuovoUtente.setPasswordHash(passwordHash);

        nuovoUtente.setRuolo(Ruolo.UTENTE_BASE);
        nuovoUtente.setPunteggio(0);
        nuovoUtente.setBadge("OCCHIO_DI_FALCO");
        nuovoUtente.setImmagineProfilo("img/default-avatar.png");

        boolean salvato = utenteDAO.doSave(nuovoUtente);
        System.out.println("💾 DEBUG: Risultato salvataggio DAO: " + salvato);

        return salvato;
    }

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
}