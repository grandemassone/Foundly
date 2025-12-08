package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.Utente;
import model.service.EmailService;
import model.service.UtenteService;

import java.io.IOException;
import java.security.SecureRandom;

@WebServlet(name = "RecuperoPasswordServlet", value = "/recupero-password")
public class RecuperoPasswordServlet extends HttpServlet {

    private final UtenteService utenteService = new UtenteService();
    private final EmailService emailService = new EmailService();

    // durata codice in millisecondi (es. 15 minuti)
    private static final long DURATA_CODICE_MS = 15 * 60 * 1000L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        // Step iniziale: pagina per inserire email
        request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_email.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        String step = request.getParameter("step");
        if (step == null || step.isEmpty()) {
            step = "email";
        }

        if ("email".equals(step)) {
            gestisciStepEmail(request, response);
        } else if ("codice".equals(step)) {
            gestisciStepCodice(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Step non valido");
        }
    }

    private void gestisciStepEmail(HttpServletRequest request,
                                   HttpServletResponse response) throws ServletException, IOException {

        String email = request.getParameter("email");
        HttpSession session = request.getSession();

        // Non riveliamo se l'email esiste o meno:
        // ma se esiste, generiamo codice e inviamo mail.

        Utente utente = utenteService.trovaPerEmail(email); // implementa questo metodo nel service
        if (utente != null) {
            // genera codice 6 cifre
            String codice = generaCodiceSeiCifre();

            long scadenza = System.currentTimeMillis() + DURATA_CODICE_MS;

            session.setAttribute("recuperoEmail", email);
            session.setAttribute("recuperoCodice", codice);
            session.setAttribute("recuperoScadenza", scadenza);

            try {
                emailService.inviaCodiceRecuperoPassword(email, codice);
            } catch (Exception e) {
                e.printStackTrace();
                // NON diciamo all'utente che è fallito, per sicurezza
            }
        }

        // Messaggio generico, anche se utente == null
        request.setAttribute("messaggio",
                "Se l'email esiste nel sistema, riceverai un codice di verifica a breve.");
        // Mostra pagina per inserire codice + nuova password
        request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_codice.jsp")
                .forward(request, response);
    }

    private void gestisciStepCodice(HttpServletRequest request,
                                    HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            request.setAttribute("errore",
                    "Sessione di recupero scaduta. Ripeti la procedura.");
            request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_email.jsp")
                    .forward(request, response);
            return;
        }

        String emailSession = (String) session.getAttribute("recuperoEmail");
        String codiceSession = (String) session.getAttribute("recuperoCodice");
        Long scadenza = (Long) session.getAttribute("recuperoScadenza");

        if (emailSession == null || codiceSession == null || scadenza == null) {
            request.setAttribute("errore",
                    "Sessione di recupero non valida. Ripeti la procedura.");
            request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_email.jsp")
                    .forward(request, response);
            return;
        }

        if (System.currentTimeMillis() > scadenza) {
            // codice scaduto
            session.removeAttribute("recuperoEmail");
            session.removeAttribute("recuperoCodice");
            session.removeAttribute("recuperoScadenza");

            request.setAttribute("errore",
                    "Il codice è scaduto. Richiedi un nuovo codice.");
            request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_email.jsp")
                    .forward(request, response);
            return;
        }

        String codiceInserito = request.getParameter("codice");
        String nuovaPassword = request.getParameter("nuovaPassword");
        String confermaPassword = request.getParameter("confermaPassword");

        // verifica codice
        if (codiceInserito == null || !codiceInserito.equals(codiceSession)) {
            request.setAttribute("errore", "Codice non valido.");
            request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_codice.jsp")
                    .forward(request, response);
            return;
        }

        // verifica password + conferma
        if (nuovaPassword == null || confermaPassword == null ||
                !nuovaPassword.equals(confermaPassword)) {

            request.setAttribute("errore", "Le password non coincidono.");
            request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_codice.jsp")
                    .forward(request, response);
            return;
        }

        // opzionale: applica gli stessi controlli di sicurezza delle altre password
        String patternPassword =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$";

        if (!nuovaPassword.matches(patternPassword)) {
            request.setAttribute("errore",
                    "La password non rispetta i requisiti di sicurezza.");
            request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_codice.jsp")
                    .forward(request, response);
            return;
        }

        // aggiorna password nel DB
        boolean aggiornato = utenteService.resetPasswordByEmail(emailSession, nuovaPassword);
        // implementa resetPasswordByEmail(email, nuovaPassword) nel tuo service

        // pulizia attributi di sessione
        session.removeAttribute("recuperoEmail");
        session.removeAttribute("recuperoCodice");
        session.removeAttribute("recuperoScadenza");

        if (!aggiornato) {
            request.setAttribute("errore",
                    "Si è verificato un errore durante l'aggiornamento della password.");
            request.getRequestDispatcher("/WEB-INF/jsp/recupero_password_codice.jsp")
                    .forward(request, response);
            return;
        }

        // scenario: mostra messaggio e reindirizza al login
        request.setAttribute("messaggioSuccesso", "Password aggiornata con successo. Ora puoi accedere.");
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp")
                .forward(request, response);
    }

    private String generaCodiceSeiCifre() {
        SecureRandom random = new SecureRandom();
        int codice = random.nextInt(1_000_000); // 0..999999
        return String.format("%06d", codice);
    }
}
