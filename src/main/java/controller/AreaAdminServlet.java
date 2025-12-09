package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.DropPoint;
import model.bean.Utente;
import model.bean.enums.Ruolo;
import model.service.DropPointService;
import model.service.UtenteService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AreaAdminServlet", value = "/admin")
public class AreaAdminServlet extends HttpServlet {

    private final DropPointService dropPointService = new DropPointService();
    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente admin = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (admin == null || admin.getRuolo() != Ruolo.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<DropPoint> pendenti = dropPointService.findAllInAttesa();
        List<Utente> utenti = utenteService.trovaTutti();

        request.setAttribute("dropPointsPendenti", pendenti);
        request.setAttribute("listaUtenti", utenti);

        request.getRequestDispatcher("/WEB-INF/jsp/area_admin.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente admin = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (admin == null || admin.getRuolo() != Ruolo.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("approveDropPoint".equals(action)) {
                long id = Long.parseLong(request.getParameter("dropPointId"));
                dropPointService.approvaDropPoint(id);

            } else if ("rejectDropPoint".equals(action)) {
                long id = Long.parseLong(request.getParameter("dropPointId"));
                dropPointService.rifiutaDropPoint(id);

            } else if ("deleteUser".equals(action)) {
                long userId = Long.parseLong(request.getParameter("userId"));
                // Non permetto all'admin di cancellare se stesso
                if (userId != admin.getId()) {
                    utenteService.cancellaUtente(userId);
                }
            }
        } catch (NumberFormatException ignore) {
            // input non valido, ignoro
        }

        response.sendRedirect(request.getContextPath() + "/admin");
    }
}