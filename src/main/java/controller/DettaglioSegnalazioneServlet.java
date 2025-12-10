package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.DropPoint;
import model.bean.Reclamo;
import model.bean.Segnalazione;
import model.bean.SegnalazioneOggetto;
import model.bean.Utente;
import model.bean.enums.ModalitaConsegna;
import model.dao.ReclamoDAO;
import model.dao.UtenteDAO;
import model.service.DropPointService;
import model.service.SegnalazioneService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DettaglioSegnalazioneServlet", value = "/dettaglio-segnalazione")
public class DettaglioSegnalazioneServlet extends HttpServlet {

    private final SegnalazioneService segnalazioneService = new SegnalazioneService();
    private final ReclamoDAO reclamoDAO = new ReclamoDAO();
    private final UtenteDAO utenteDAO = new UtenteDAO();
    private final DropPointService dropPointService = new DropPointService();

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

        // --- Recupero Drop-Point per la consegna (se applicabile) ---
        if (s instanceof SegnalazioneOggetto) {
            SegnalazioneOggetto so = (SegnalazioneOggetto) s;
            if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT && so.getIdDropPoint() != null) {
                DropPoint dp = dropPointService.trovaPerId(so.getIdDropPoint());
                request.setAttribute("dropPointRitiro", dp);
            }
        }

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente != null) {
            if (utente.getId() == s.getIdUtente()) {
                // CASO PROPRIETARIO (Finder)
                List<Reclamo> reclami = reclamoDAO.doRetrieveBySegnalazione(s.getId());
                request.setAttribute("reclamiRicevuti", reclami);

                // --- NUOVO: Mappa per associare ID Utente -> Oggetto Utente (Richiedente) ---
                // Serve per mostrare i dati di chi ha fatto reclamo al Finder
                Map<Long, Utente> mappaRichiedenti = new HashMap<>();
                for (Reclamo r : reclami) {
                    if (!mappaRichiedenti.containsKey(r.getIdUtenteRichiedente())) {
                        Utente richiedente = utenteDAO.doRetrieveById(r.getIdUtenteRichiedente());
                        mappaRichiedenti.put(r.getIdUtenteRichiedente(), richiedente);
                    }
                }
                request.setAttribute("mappaRichiedenti", mappaRichiedenti);
                // ----------------------------------------------------------------------------

            } else {
                // CASO UTENTE CHE CERCA (Owner)
                Reclamo mioReclamo = reclamoDAO.doRetrieveBySegnalazioneAndUtente(s.getId(), utente.getId());
                request.setAttribute("mioReclamo", mioReclamo);
            }
        }

        // Recupero info del proprietario (Finder) per mostrarle all'Owner in caso di vittoria
        Utente proprietario = utenteDAO.doRetrieveById(s.getIdUtente());
        request.setAttribute("proprietarioSegnalazione", proprietario);

        request.setAttribute("segnalazione", s);
        request.getRequestDispatcher("/WEB-INF/jsp/dettaglio_segnalazione.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String idStr = request.getParameter("idSegnalazione");

        if ("delete".equals(action) && idStr != null) {
            long id = Long.parseLong(idStr);
            HttpSession session = request.getSession(false);
            Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;
            Segnalazione s = segnalazioneService.trovaPerId(id);

            if (utente != null && s != null && s.getIdUtente() == utente.getId()) {
                segnalazioneService.eliminaSegnalazione(id);
            }
            response.sendRedirect("le-mie-segnalazioni");
        }
    }
}