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
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();

        // 1. Login come UTENTE (cittadino)
        Utente utente = utenteService.login(email, password);
        if (utente != null) {
            session.setAttribute("utente", utente);         // usato in index.jsp
            session.setAttribute("ruoloLoggato", "CITTADINO");
            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }

        // 2. Login come DROP-POINT
        DropPoint dropPoint = dropPointService.login(email, password);
        if (dropPoint != null) {
            // opzionale: tieni un attributo specifico
            session.setAttribute("dropPoint", dropPoint);

            // IMPORTANTE: metto anche "utente" così index.jsp lo vede come loggato
            session.setAttribute("utente", dropPoint);

            session.setAttribute("ruoloLoggato", "DROPPOINT");
            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }

        // 3. Nessun login valido
        request.setAttribute("errore", "Email o Password non validi.");
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }
}
