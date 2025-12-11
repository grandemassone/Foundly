package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.DropPoint;
import model.bean.Utente;
import model.service.DropPointService;
import model.service.UtenteService;

import java.io.IOException;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    private final UtenteService utenteService = new UtenteService();
    private final DropPointService dropPointService = new DropPointService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        HttpSession session = request.getSession(true);

        // 1) Provo login come UTENTE "cittadino"
        Utente utente = utenteService.login(email, password);
        if (utente != null) {
            // se prima era loggato un Drop-Point lo sgancio
            session.removeAttribute("dropPoint");

            session.setAttribute("utente", utente);
            session.setAttribute("ruoloLoggato", "CITTADINO");
            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }

        // 2) Provo login come DROP-POINT
        DropPoint dropPoint = dropPointService.login(email, password);
        if (dropPoint != null) {
            // se prima era loggato un utente "cittadino" lo sgancio
            session.removeAttribute("utente");

            session.setAttribute("dropPoint", dropPoint);
            session.setAttribute("ruoloLoggato", "DROPPOINT");
            session.setMaxInactiveInterval(30 * 60);

            // QUI è la differenza: il Drop-Point va SOLO nella sua area dedicata
            response.sendRedirect(request.getContextPath() + "/area-drop-point");
            return;
        }

        // 3) Credenziali non valide né per utente né per Drop-Point
        request.setAttribute("errore", "Email o Password non validi.");
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp")
                .forward(request, response);
    }
}
