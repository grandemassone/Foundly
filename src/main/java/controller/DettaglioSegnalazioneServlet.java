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
import model.dao.ReclamoDAO;
import model.dao.UtenteDAO;
import model.service.SegnalazioneService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DettaglioSegnalazioneServlet", value = "/dettaglio-segnalazione")
public class DettaglioSegnalazioneServlet extends HttpServlet {

    private final SegnalazioneService segnalazioneService = new SegnalazioneService();
    private final ReclamoDAO reclamoDAO = new ReclamoDAO();
    private final UtenteDAO utenteDAO = new UtenteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect("index");
            return;
        }

        long id = Long.parseLong(idStr);
        Segnalazione s = segnalazioneService.trovaPerId(id);

        if (s == null) {
            response.sendRedirect("index");
            return;
        }

        // Se sono il proprietario, carico anche i reclami ricevuti
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente != null && utente.getId() == s.getIdUtente()) {
            List<Reclamo> reclami = reclamoDAO.doRetrieveBySegnalazione(s.getId());
            // Carico i dati degli utenti che hanno fatto reclamo (nome, cognome, email)
            // (In un progetto reale si farebbe un DTO, qui usiamo una lista parallela o logica in JSP)
            // Per semplicità, passiamo la lista reclami. Nella JSP itereremo.
            request.setAttribute("reclamiRicevuti", reclami);
        }

        // Recupero info del proprietario della segnalazione (per mostrarle se reclamo accettato)
        Utente proprietario = utenteDAO.doRetrieveById(s.getIdUtente());
        request.setAttribute("proprietarioSegnalazione", proprietario);

        request.setAttribute("segnalazione", s);
        request.getRequestDispatcher("/WEB-INF/jsp/dettaglio_segnalazione.jsp").forward(request, response);
    }

    // Gestione eliminazione
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String idStr = request.getParameter("idSegnalazione");

        if ("delete".equals(action) && idStr != null) {
            long id = Long.parseLong(idStr);
            HttpSession session = request.getSession(false);
            Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

            Segnalazione s = segnalazioneService.trovaPerId(id);

            // Controllo sicurezza: solo il proprietario elimina
            if (utente != null && s != null && s.getIdUtente() == utente.getId()) {
                segnalazioneService.eliminaSegnalazione(id);
            }
            response.sendRedirect("le-mie-segnalazioni");
        }
    }
}