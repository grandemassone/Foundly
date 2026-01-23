package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.DropPointService;

import java.io.IOException;

@WebServlet(name = "RegistrazioneDropPointServlet", value = "/registrazione-droppoint")
public class RegistrazioneDropPointServlet extends HttpServlet {

    private final DropPointService dpService = new DropPointService();

    // Pattern password: Min 8 char, 1 Maiusc, 1 Minusc, 1 Numero, 1 Speciale (@$!%*?&._#-)
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#-])[A-Za-z\\d@$!%*?&._#-]{8,}$";

    // Telefono: esattamente 10 cifre
    private static final String TELEFONO_PATTERN = "^\\d{10}$";

    // Email base
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    // Provincia: due lettere
    private static final String PROVINCIA_PATTERN = "^[A-Za-z]{2}$";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // trim su tutti i campi stringa (tranne password)
        String nomeAttivita = trimOrNull(request.getParameter("nomeAttivita"));
        String email = trimOrNull(request.getParameter("email"));
        String password = request.getParameter("password"); // no trim
        String indirizzo = trimOrNull(request.getParameter("indirizzo"));
        String citta = trimOrNull(request.getParameter("citta"));
        String provincia = trimOrNull(request.getParameter("provincia"));
        String telefono = trimOrNull(request.getParameter("telefono"));
        String orari = trimOrNull(request.getParameter("orari"));

        String latStr = trimOrNull(request.getParameter("latitudine"));
        String lonStr = trimOrNull(request.getParameter("longitudine"));

        Double latitudine = null;
        Double longitudine = null;

        try {
            if (!isBlank(latStr)) {
                latitudine = Double.parseDouble(latStr);
            }
            if (!isBlank(lonStr)) {
                longitudine = Double.parseDouble(lonStr);
            }
        } catch (NumberFormatException e) {
            // schema coordinata non numerico
            latitudine = null;
            longitudine = null;
        }

        // Campi obbligatori
        if (isBlank(nomeAttivita) || isBlank(email) || isBlank(password) ||
                isBlank(indirizzo) || isBlank(citta) || isBlank(provincia) ||
                isBlank(telefono) || isBlank(orari) ||
                latitudine == null || longitudine == null) {

            request.setAttribute("errore",
                    "Tutti i campi sono obbligatori e la posizione sulla mappa deve essere selezionata.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        // Lunghezze massime (adattale alle colonne del DB se servono)
        if (nomeAttivita.length() > 100 ||
                email.length() > 100 ||
                indirizzo.length() > 100 ||
                citta.length() > 50 ||
                provincia.length() > 2 ||
                orari.length() > 100) {

            request.setAttribute("errore",
                    "Uno o più campi superano la lunghezza massima consentita.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        // Password
        if (!password.matches(PASSWORD_PATTERN)) {
            request.setAttribute("errore",
                    "La password non rispetta i requisiti (usa solo @$!%*?&._#-).");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        // Email
        if (!email.matches(EMAIL_PATTERN)) {
            request.setAttribute("errore", "Formato email non valido.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        // Telefono
        if (!telefono.matches(TELEFONO_PATTERN)) {
            request.setAttribute("errore",
                    "Il numero di telefono deve contenere esattamente 10 cifre.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        // Provincia
        if (!provincia.matches(PROVINCIA_PATTERN)) {
            request.setAttribute("errore",
                    "La provincia deve essere indicata con due lettere (es. MI).");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        // Coordinate in range plausibile
        if (latitudine < -90 || latitudine > 90 || longitudine < -180 || longitudine > 180) {
            request.setAttribute("errore",
                    "Coordinate geografiche non valide.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        boolean successo = dpService.registraDropPoint(
                nomeAttivita,
                email,
                password,
                indirizzo,
                citta,
                provincia.toUpperCase(),
                telefono,
                orari,
                latitudine,
                longitudine
        );

        if (successo) {
            response.sendRedirect(request.getContextPath() + "/login?dpOk=1");
        } else {
            request.setAttribute("errore", "Email già registrata per un altro Drop-Point.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
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
