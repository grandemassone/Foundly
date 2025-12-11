package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.DropPoint;
import model.bean.enums.StatoDropPoint;
import model.service.DropPointService;

import java.io.IOException;

@WebServlet(name = "AreaDropPointServlet", value = "/area-drop-point")
public class AreaDropPointServlet extends HttpServlet {

    private final DropPointService dropPointService = new DropPointService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        DropPoint dp = (session != null) ? (DropPoint) session.getAttribute("dropPoint") : null;

        if (dp != null) {
            // ricarico dal DB per avere lo stato aggiornato
            DropPoint fromDb = dropPointService.trovaPerId(dp.getId());
            if (fromDb != null) {
                dp = fromDb;
                session.setAttribute("dropPoint", dp);
            }
        }

        request.setAttribute("dropPoint", dp);

        if (dp != null && dp.getStato() == StatoDropPoint.APPROVATO) {
            long id = dp.getId();
            request.setAttribute("depositiAttivi",      dropPointService.countDepositiAttivi(id));
            request.setAttribute("consegneCompletate", dropPointService.countConsegneCompletate(id));
            request.setAttribute("totaleOperazioni",   dropPointService.countTotaleOperazioni(id));
        } else {
            request.setAttribute("depositiAttivi", 0);
            request.setAttribute("consegneCompletate", 0);
            request.setAttribute("totaleOperazioni", 0);
        }

        request.getRequestDispatcher("/WEB-INF/jsp/area_drop_point.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        DropPoint dp = (session != null) ? (DropPoint) session.getAttribute("dropPoint") : null;

        if (dp == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // ricarico stato
        dp = dropPointService.trovaPerId(dp.getId());
        session.setAttribute("dropPoint", dp);
        request.setAttribute("dropPoint", dp);

        String action = request.getParameter("action");
        String codice = request.getParameter("codice");

        if (dp.getStato() != StatoDropPoint.APPROVATO) {
            request.setAttribute("erroreGenerale", "Devi avere un Drop-Point approvato per effettuare operazioni.");
        } else if (action != null && codice != null && !codice.isBlank()) {

            if ("deposito".equals(action)) {
                boolean ok = dropPointService.registraDeposito(dp.getId(), codice.trim());
                if (ok) {
                    request.setAttribute("msgDeposito", "Deposito verificato.");
                } else {
                    request.setAttribute("msgDeposito", "Codice non valido o non associato a questo punto.");
                }
            } else if ("ritiro".equals(action)) {
                // QUI AVVIENE LA MAGIA: Chiama il service aggiornato che chiude la segnalazione nel DB
                boolean ok = dropPointService.registraRitiro(dp.getId(), codice.trim());
                if (ok) {
                    request.setAttribute("msgRitiro", "Ritiro confermato! Segnalazione chiusa e punti assegnati.");
                } else {
                    request.setAttribute("msgRitiro", "Errore: Codice non valido o segnalazione già chiusa.");
                }
            }
        }

        long id = dp.getId();
        request.setAttribute("depositiAttivi",      dropPointService.countDepositiAttivi(id));
        request.setAttribute("consegneCompletate", dropPointService.countConsegneCompletate(id));
        request.setAttribute("totaleOperazioni",   dropPointService.countTotaleOperazioni(id));

        request.getRequestDispatcher("/WEB-INF/jsp/area_drop_point.jsp")
                .forward(request, response);
    }
}