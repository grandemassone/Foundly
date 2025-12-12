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
import model.service.EmailService;
import model.service.SegnalazioneService;
import model.service.UtenteService;

import java.io.IOException;
import java.util.UUID;

@WebServlet(name = "GestioneReclamoServlet", value = "/gestione-reclamo")
public class GestioneReclamoServlet extends HttpServlet {

    private final ReclamoDAO reclamoDAO = new ReclamoDAO();
    private final SegnalazioneService segnalazioneService = new SegnalazioneService();
    private final EmailService emailService = new EmailService();
    private final UtenteService utenteService = new UtenteService();

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

        // --- DROP-POINT: Conferma Ritiro ---
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

        // --- UTENTI (Finder/Owner) ---
        if (utente == null) { response.sendRedirect("login"); return; }

        if ("invia".equals(action)) {
            // ... invia reclamo ...
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
            boolean salvato = reclamoDAO.doSave(r);

            // MAIL AL FINDER (Chiamata Diretta)
            if (salvato) {
                Utente finder = utenteService.trovaPerId(s.getIdUtente());
                if (finder != null) {
                    emailService.inviaNotificaNuovoReclamo(finder.getEmail(), s.getTitolo(), utente.getUsername());
                }
            }
            response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_inviato");

        } else if ("accetta".equals(action)) {
            // ... accetta reclamo ...
            long idReclamo = Long.parseLong(request.getParameter("idReclamo"));
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));
            Segnalazione s = segnalazioneService.trovaPerId(idSegnalazione);

            String codice = null;
            boolean isDropPoint = false;

            if (s instanceof SegnalazioneOggetto) {
                SegnalazioneOggetto so = (SegnalazioneOggetto) s;
                if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
                    isDropPoint = true;
                    codice = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                }
            }

            boolean ok = reclamoDAO.accettaReclamo(idReclamo, codice);

            if (ok) {
                Reclamo r = reclamoDAO.doRetrieveById(idReclamo);
                Utente richiedente = utenteService.trovaPerId(r.getIdUtenteRichiedente());
                Utente finder = utente;

                // MAIL 1: FINDER (Chiamata Diretta)
                emailService.inviaConfermaAccettazioneFinder(finder.getEmail(), s.getTitolo(), richiedente.getNome() + " " + richiedente.getCognome());

                // MAIL 2: OWNER (Chiamata Diretta)
                emailService.inviaReclamoAccettatoOwner(richiedente.getEmail(), s.getTitolo(), finder.getNome() + " " + finder.getCognome(), codice);
            }

            if (isDropPoint) {
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=attesa_ritiro");
            } else {
                response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=scambio_avviato");
            }

        } else if ("rifiuta".equals(action)) {
            long idReclamo = Long.parseLong(request.getParameter("idReclamo"));
            long idSegnalazione = Long.parseLong(request.getParameter("idSegnalazione"));
            segnalazioneService.rifiutaReclamo(idReclamo);
            response.sendRedirect("dettaglio-segnalazione?id=" + idSegnalazione + "&msg=reclamo_rifiutato");

        } else if ("conferma_scambio".equals(action)) {
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