package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.UtenteService;
import java.io.IOException;
import java.io.PrintWriter; // Import necessario per scrivere l'HTML/JS

@WebServlet(name = "RegistrazioneUtenteServlet", value = "/registrazione-utente")
public class RegistrazioneUtenteServlet extends HttpServlet {

    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String telefono = request.getParameter("telefono");

        // Pattern password: Min 8 char, 1 Maiusc, 1 Minusc, 1 Numero, 1 Speciale (@$!%*?&._#-)
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#-])[A-Za-z\\d@$!%*?&._#-]{8,}$";

        if (!password.matches(passwordPattern)) {
            request.setAttribute("errore", "La password non rispetta i requisiti (usa solo @$!%*?&._#-)");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp").forward(request, response);
            return;
        }

        if (email == null || username == null) {
            request.setAttribute("errore", "Campi obbligatori mancanti.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp").forward(request, response);
            return;
        }

        boolean successo = utenteService.registraUtente(nome, cognome, username, email, password, telefono);

        if (successo) {
            // --- INIZIO LOGICA ALERT SEMPLICE ---
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html><body>");
            out.println("<script type='text/javascript'>");

            // Qui scriviamo l'alert del browser
            out.println("alert('Registrazione effettuata con successo! Ora puoi effettuare il login.');");

            // Qui reindirizziamo l'utente alla pagina di login dopo che clicca OK
            // Nota: Usa request.getContextPath() per essere sicuro del percorso
            out.println("window.location.href = '" + request.getContextPath() + "/login';");

            out.println("</script>");
            out.println("</body></html>");
            // --- FINE LOGICA ALERT SEMPLICE ---

        } else {
            // --- INIZIO LOGICA ALERT SEMPLICE ---
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html><body>");
            out.println("<script type='text/javascript'>");

            // Qui scriviamo l'alert del browser
            out.println("alert('Email o Password già esistenti!');");

            // Qui reindirizziamo l'utente alla pagina di login dopo che clicca OK
            // Nota: Usa request.getContextPath() per essere sicuro del percorso
            out.println("window.location.href = '" + request.getContextPath() + "/RegistrazioneUtente';");

            out.println("</script>");
            out.println("</body></html>");
            // --- FINE LOGICA ALERT SEMPLICE --
            request.setAttribute("errore", "Email o Username già esistenti.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_utente.jsp").forward(request, response);
        }
    }
}