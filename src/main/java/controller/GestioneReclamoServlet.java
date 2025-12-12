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
import model.bean.enums.StatoSegnalazione;
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
        DropPoint dropPoint = (session != null) ? (DropPoint) session.getAttribute("dropPoint") : null;

        if (utente == null && dropPoint == null) {
            response.sendRedirect("login");
            return;
        }

        // --- 1. DROP-POINT: Conferma Ritiro con Codice ---
        if ("conferma_ritiro".equals(action)) {
            if (dropPoint == null) { response.sendRedirect("login"); return; }
            try {
                long idReclamo = Long.parseLong(request.getParameter("idReclamo"));
                long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));
                String codice = request.getParameter("codiceConsegna");

                boolean successo = segnalazioneService.accettaReclamoEChiudiSegnalazione(idReclamo, idSegnalazione, codice);
                response.sendRedirect("area-drop-point?msg=" + (successo ? "successo_consegna" : "codice_errato"));
            } catch (Exception e) {
                response.sendRedirect("area-drop-point?err=errore");
            }
            return;
        }

        // --- 2. AZIONI UTENTE (Finder / Owner) ---
        if (utente == null) { response.sendRedirect("login"); return; }

        if ("invia".equals(action)) {
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));
            Segnalazione s = segnalazioneService.trovaPerId(idSegnalazione);

            if (s == null || s.getStato() != StatoSegnalazione.APERTA) {
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=segnalazione_chiusa");
                return;
            }
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
            boolean isDropPoint = false;

            // VERIFICA SE È DROP-POINT PER GENERARE IL CODICE
            if (s instanceof SegnalazioneOggetto) {
                SegnalazioneOggetto so = (SegnalazioneOggetto) s;
                if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
                    isDropPoint = true;
                    // Genera codice univoco (6 caratteri)
                    codice = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                }
            }

            // Salva nel DB (se DropPoint salva codice, se Diretta salva null)
            reclamoDAO.accettaReclamo(idReclamo, codice);

            if (isDropPoint) {
                // Reindirizza con un messaggio che la JSP userà per mostrare il codice
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=attesa_ritiro");
            } else {
                // Scambio diretto: avvia il flusso dei pulsanti
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=scambio_avviato");
            }

        } else if ("rifiuta".equals(action)) {
            long idReclamo = Long.parseLong(request.getParameter("idReclamo"));
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));
            segnalazioneService.rifiutaReclamo(idReclamo);
            response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_rifiutato");

        } else if ("conferma_scambio".equals(action)) {
            // SCAMBIO DIRETTO
            long idReclamo = Long.parseLong(request.getParameter("idReclamo"));
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));

            Segnalazione s = segnalazioneService.trovaPerId(idSegnalazione);
            boolean isFinder = (s.getIdUtente() == utente.getId());

            boolean finito = segnalazioneService.gestisciConfermaScambio(idReclamo, isFinder);

            if (finito) {
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=scambio_completato");
            } else {
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=conferma_registrata");
            }
        }
    }
}