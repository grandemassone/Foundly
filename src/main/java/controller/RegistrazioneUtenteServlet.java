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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String telefono = request.getParameter("telefono");

        // Pattern password: Min 8 char, 1 Maiusc, 1 Minusc, 1 Numero, 1 Speciale (@$!%*?&._#-)
        String passwordPattern =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#-])[A-Za-z\\d@$!%*?&._#-]{8,}$";

        // Validazione password
        if (password == null || !password.matches(passwordPattern)) {
            request.setAttribute("errore",
                    "La password non rispetta i requisiti (usa solo @$!%*?&._#-).");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                    .forward(request, response);
            return;
        }

        // Controllo campi obbligatori base
        if (email == null || email.isBlank() || username == null || username.isBlank()) {
            request.setAttribute("errore", "Campi obbligatori mancanti.");
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
            // Registrazione OK → redirect alla pagina di login con flag regOk=1
            response.sendRedirect(request.getContextPath() + "/login?regOk=1");
        } else {
            // Utente già esistente
            request.setAttribute("errore", "Email o Username già esistenti.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp")
                    .forward(request, response);
        }
    }
}
