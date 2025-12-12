package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

        request.setAttribute("dropPointsPendenti", dropPointService.findAllInAttesa());
        request.setAttribute("dropPointsApprovati", dropPointService.findAllApprovati());
        request.setAttribute("listaUtenti", utenteService.trovaTutti());

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
        if (action == null || action.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        try {
            switch (action) {
                case "approveDropPoint": {
                    long id = Long.parseLong(request.getParameter("dropPointId"));
                    dropPointService.approvaDropPoint(id);
                    break;
                }
                case "rejectDropPoint": {
                    long id = Long.parseLong(request.getParameter("dropPointId"));
                    dropPointService.rifiutaDropPoint(id);
                    break;
                }
                case "deleteDropPoint": {
                    long id = Long.parseLong(request.getParameter("dropPointId"));
                    dropPointService.eliminaDropPoint(id);
                    break;
                }
                case "deleteUser": {
                    long userId = Long.parseLong(request.getParameter("userId"));
                    // Non permetto all'admin di cancellare se stesso
                    if (userId != admin.getId()) {
                        utenteService.cancellaUtente(userId);
                    }
                    break;
                }
                default:
                    // azione non riconosciuta -> ignora
                    break;
            }
        } catch (NumberFormatException ignore) {
            // input non valido, ignoro
        }

        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
