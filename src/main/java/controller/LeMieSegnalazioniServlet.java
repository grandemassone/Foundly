package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.Segnalazione;
import model.bean.Utente;
import model.service.SegnalazioneService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "LeMieSegnalazioniServlet", value = "/le-mie-segnalazioni")
public class LeMieSegnalazioniServlet extends HttpServlet {

    private final SegnalazioneService segnalazioneService = new SegnalazioneService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {

            response.sendRedirect("login");
            return;
        }

        List<Segnalazione> mieSegnalazioni = segnalazioneService.trovaPerUtente(utente.getId());
        request.setAttribute("mieSegnalazioni", mieSegnalazioni);

        request.getRequestDispatcher("/WEB-INF/jsp/le_mie_segnalazioni.jsp").forward(request, response);
    }
}