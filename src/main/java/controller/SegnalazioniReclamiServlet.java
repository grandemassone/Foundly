package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.Reclamo;
import model.bean.Segnalazione;
import model.bean.Utente;
import model.service.SegnalazioneService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SegnalazioniReclamiServlet", value = "/le-mie-segnalazioni")
public class SegnalazioniReclamiServlet extends HttpServlet {

    private final SegnalazioneService service = new SegnalazioneService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect("login");
            return;
        }

        // 1. Le Mie Segnalazioni (Oggetti che HO trovato)
        List<Segnalazione> mieSegnalazioni = service.trovaPerUtente(utente.getId());
        request.setAttribute("mieSegnalazioni", mieSegnalazioni);

        // 2. I Miei Reclami (Oggetti che HO perso e sto richiedendo)
        List<Reclamo> mieiReclami = service.trovaReclamiFattiDaUtente(utente.getId());
        request.setAttribute("mieiReclami", mieiReclami);

        request.getRequestDispatcher("/WEB-INF/jsp/segnalazioni_e_reclami.jsp").forward(request, response);
    }
}