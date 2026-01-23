package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.UtenteService;

import java.io.IOException;

@WebServlet(name = "RegistrazioneUtenteServlet", value = "/registrazione-utente")
public class RegistrazioneUtenteServlet extends HttpServlet {

    private final UtenteService utenteService = new UtenteService();

    // Pattern password: Min 8 char, 1 Maiusc, 1 Minusc, 1 Numero, 1 Speciale (@$!%*?&._#-)
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#-])[A-Za-z\\d@$!%*?&._#-]{8,}$";

    // Telefono: esattamente 10 cifre
    private static final String TELEFONO_PATTERN = "^\\d{10}$";

    // Email base (non perfetta ma meglio di niente)
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // trim per evitare spazi “furbi”
        String nome = trimOrNull(request.getParameter("nome"));
        String cognome = trimOrNull(request.getParameter("cognome"));
        String username = trimOrNull(request.getParameter("username"));
        String email = trimOrNull(request.getParameter("email"));
        String password = request.getParameter("password"); // niente trim sulla password
        String telefono = trimOrNull(request.getParameter("telefono"));

        // Campi obbligatori
        if (isBlank(nome) || isBlank(cognome) ||
                isBlank(username) || isBlank(email) || isBlank(password) || isBlank(telefono)) {

            request.setAttribute("errore", "Tutti i campi sono obbligatori.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                    .forward(request, response);
            return;
        }

        // Lunghezze massime (scegli numeri coerenti con il DB)
        if (nome.length() > 50 || cognome.length() > 50 || username.length() > 30 || email.length() > 100) {
            request.setAttribute("errore", "Uno o più campi superano la lunghezza massima consentita.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                    .forward(request, response);
            return;
        }

        // Password
        if (!password.matches(PASSWORD_PATTERN)) {
            request.setAttribute("errore",
                    "La password non rispetta i requisiti (usa solo @$!%*?&._#-).");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                    .forward(request, response);
            return;
        }

        // Email
        if (!email.matches(EMAIL_PATTERN)) {
            request.setAttribute("errore", "Formato email non valido.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                    .forward(request, response);
            return;
        }

        // Telefono
        if (!telefono.matches(TELEFONO_PATTERN)) {
            request.setAttribute("errore", "Il numero di telefono deve contenere esattamente 10 cifre.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                    .forward(request, response);
            return;
        }

        boolean successo = utenteService.registraUtente(
                nome,
                cognome,
                username,
                email,
                password,
                telefono
        );

        if (successo) {
            response.sendRedirect(request.getContextPath() + "/login?regOk=1");
        } else {
            // Attenzione: questo messaggio è già abbastanza “informativo”.
            // Per maggiore sicurezza potresti usare qualcosa di più generico (tipo “Registrazione non riuscita.”)
            request.setAttribute("errore", "Email o Username già esistenti.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                    .forward(request, response);
        }
    }

    private static String trimOrNull(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
