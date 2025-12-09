package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.Utente;
import model.service.UtenteService;

import java.io.IOException;

@WebServlet(name = "ProfiloServlet", value = "/profilo")
public class ProfiloServlet extends HttpServlet {

    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        Utente utente = null;
        if (session != null) {
            utente = (Utente) session.getAttribute("utente");
        }

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // RICALCOLA SEMPRE IL BADGE IN BASE AI PUNTI ATTUALI (senza cambiare i punti)
        utenteService.aggiornaPunteggioEBadge(utente, 0);

        // aggiorna la sessione con eventuali modifiche
        session.setAttribute("utente", utente);

        request.getRequestDispatcher("/WEB-INF/jsp/profilo.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // leggi valori modificati
        String username = request.getParameter("username");
        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");

        // (qui potresti fare validazioni base, es. non vuoti)

        utente.setUsername(username);
        utente.setNome(nome);
        utente.setCognome(cognome);

        // persisti nel DB
        utenteService.aggiornaProfilo(utente);   // implementa questo metodo nel service/DAO

        // aggiorna anche la sessione
        session.setAttribute("utente", utente);

        // torna alla pagina profilo (magari con un flag success)
        response.sendRedirect(request.getContextPath() + "/profilo?success=1");
    }
}
