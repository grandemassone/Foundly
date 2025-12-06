package model.utente;

import model.bean.Utente;
import model.bean.enums.Ruolo;
import model.dao.UtenteDAO;
import model.utils.PasswordUtils; // Importa la nuova classe utility

public class ConnectionTest {
    public static void main(String[] args) {
        System.out.println("🚀 Inizio Test Sicurezza Password...");

        UtenteDAO utenteDAO = new UtenteDAO();
        long timeSeed = System.currentTimeMillis();

        // 1. Definiamo una password in chiaro
        String passwordInChiaro = "Segreto123!";

        System.out.println("🔑 Password utente: " + passwordInChiaro);

        // 2. Creiamo l'hash
        String passwordHashata = PasswordUtils.hashPassword(passwordInChiaro);
        System.out.println("🔒 Hash generato (da salvare nel DB): " + passwordHashata);

        // 3. Creiamo l'utente con l'hash
        Utente nuovoUtente = new Utente();
        nuovoUtente.setNome("Luigi");
        nuovoUtente.setCognome("Verdi");
        nuovoUtente.setUsername("luigi_" + timeSeed);
        nuovoUtente.setEmail("luigi." + timeSeed + "@secure.com");

        // IMPORTANTE: Settiamo l'hash, NON la password in chiaro!
        nuovoUtente.setPasswordHash(passwordHashata);

        nuovoUtente.setTelefono("3339998888");
        nuovoUtente.setImmagineProfilo("img/avatar.png");
        nuovoUtente.setRuolo(Ruolo.UTENTE_BASE);
        nuovoUtente.setBadge("OCCHIO_DI_FALCO");

        try {
            // 4. Salviamo nel DB
            if (utenteDAO.doSave(nuovoUtente)) {
                System.out.println("✅ Utente salvato con password criptata.");
            }

            // 5. Simuliamo il LOGIN
            System.out.println("\n--- Simulazione Login ---");
            String emailLogin = nuovoUtente.getEmail();
            String passwordInseritaLogin = "Segreto123!"; // Quella giusta
            String passwordErrata = "Sbagliata!";

            // Recuperiamo l'utente dal DB
            Utente utenteDb = utenteDAO.doRetrieveByEmail(emailLogin);

            if (utenteDb != null) {
                // Test Password Corretta
                boolean accessoConsentito = PasswordUtils.checkPassword(passwordInseritaLogin, utenteDb.getPasswordHash());
                System.out.println("Tentativo con password corretta: " + (accessoConsentito ? "✅ ACCESSO RIUSCITO" : "❌ ERRORE"));

                // Test Password Errata
                boolean accessoNegato = PasswordUtils.checkPassword(passwordErrata, utenteDb.getPasswordHash());
                System.out.println("Tentativo con password errata:   " + (!accessoNegato ? "✅ ACCESSO NEGATO (Corretto)" : "❌ ERRORE DI SICUREZZA"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}