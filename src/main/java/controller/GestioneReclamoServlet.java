package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.Reclamo;
import model.bean.Segnalazione;
import model.bean.SegnalazioneOggetto;
import model.bean.Utente;
import model.bean.enums.ModalitaConsegna;
import model.dao.ReclamoDAO;
import model.service.SegnalazioneService;

import java.io.IOException;
import java.util.UUID;

@WebServlet(name = "GestioneReclamoServlet", value = "/gestione-reclamo")
public class GestioneReclamoServlet extends HttpServlet {

    private final ReclamoDAO reclamoDAO = new ReclamoDAO();
    private final SegnalazioneService segnalazioneService = new SegnalazioneService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect("login");
            return;
        }

        if ("invia".equals(action)) {
            // L'utente risponde alle domande
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));
            String r1 = request.getParameter("risposta1");
            String r2 = request.getParameter("risposta2");

            Reclamo r = new Reclamo();
            r.setIdSegnalazione(idSegnalazione);
            r.setIdUtenteRichiedente(utente.getId());
            r.setRispostaVerifica1(r1);
            r.setRispostaVerifica2(r2);

            reclamoDAO.doSave(r);
            response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_inviato");

        } else if ("accetta".equals(action)) {
            // Il proprietario accetta un reclamo
            long idReclamo = Long.parseLong(request.getParameter("idReclamo"));
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));

            // Verifica sicurezza (opzionale ma consigliata): controllare se utente è proprietario segnalazione

            Segnalazione s = segnalazioneService.trovaPerId(idSegnalazione);
            String codice = null;

            // Se è un oggetto con Drop-Point, genera codice
            if (s instanceof SegnalazioneOggetto) {
                SegnalazioneOggetto so = (SegnalazioneOggetto) s;
                if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
                    codice = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); // Codice 6 cifre
                }
            }

            reclamoDAO.accettaReclamo(idReclamo, codice);
            response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_accettato");
        }
    }
}