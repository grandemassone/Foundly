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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 1. Proviamo il login come UTENTE
        Utente utente = utenteService.login(email, password);

        if (utente != null) {
            HttpSession session = request.getSession();
            session.setAttribute("utente", utente); // Chiave "utente"
            session.setAttribute("ruoloLoggato", "CITTADINO");
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }

        // 2. Se fallisce, proviamo come DROP-POINT
        DropPoint dropPoint = dropPointService.login(email, password);

        if (dropPoint != null) {
            HttpSession session = request.getSession();
            session.setAttribute("dropPoint", dropPoint); // Chiave "dropPoint" (diversa da utente!)
            session.setAttribute("ruoloLoggato", "DROPPOINT");
            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect("login.jsp");
            return;
        }

        // 3. Se arriviamo qui, entrambi i login sono falliti
        request.setAttribute("errore", "Email o Password non validi.");
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }
}