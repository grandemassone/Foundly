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
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));

            // 1. Recupero la segnalazione per verificare lo stato
            Segnalazione s = segnalazioneService.trovaPerId(idSegnalazione);

            // CONTROLLO STATO: Se non è APERTA, blocco tutto.
            if (s == null || s.getStato() != model.bean.enums.StatoSegnalazione.APERTA) {
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=segnalazione_chiusa");
                return;
            }

            // 2. CONTROLLO DUPLICATI: Se esiste già un reclamo mio, stop.
            if (reclamoDAO.doRetrieveBySegnalazioneAndUtente(idSegnalazione, utente.getId()) != null) {
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_esistente");
                return;
            }

            Reclamo r = new Reclamo();
            r.setIdSegnalazione(idSegnalazione);
            r.setIdUtenteRichiedente(utente.getId());
            r.setRispostaVerifica1(request.getParameter("risposta1"));
            r.setRispostaVerifica2(request.getParameter("risposta2"));

            reclamoDAO.doSave(r);
            response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_inviato");

        } else if ("accetta".equals(action)) {
            long idReclamo = Long.parseLong(request.getParameter("idReclamo"));
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));

            Segnalazione s = segnalazioneService.trovaPerId(idSegnalazione);
            String codice = null;
            if (s instanceof SegnalazioneOggetto) {
                SegnalazioneOggetto so = (SegnalazioneOggetto) s;
                if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
                    codice = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                }
            }

            // Chiama il service che chiude la segnalazione e accetta il reclamo
            segnalazioneService.accettaReclamoEChiudiSegnalazione(idReclamo, idSegnalazione, codice);

            response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_accettato");

        } else if ("rifiuta".equals(action)) {
            // --- NUOVA LOGICA DI RIFIUTO ---
            long idReclamo = Long.parseLong(request.getParameter("idReclamo"));
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));

            // Chiama il service per aggiornare lo stato del reclamo a RIFIUTATO
            segnalazioneService.rifiutaReclamo(idReclamo);

            response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_rifiutato");
        }
    }
}