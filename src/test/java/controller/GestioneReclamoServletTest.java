package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.*;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.StatoSegnalazione;
import model.dao.ReclamoDAO;
import model.service.DropPointService;
import model.service.EmailService;
import model.service.SegnalazioneService;
import model.service.UtenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GestioneReclamoServletTest {

    private GestioneReclamoServlet servlet;

    @Mock private ReclamoDAO reclamoDAO;
    @Mock private SegnalazioneService segnalazioneService;
    @Mock private EmailService emailService;
    @Mock private UtenteService utenteService;
    @Mock private DropPointService dropPointService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new GestioneReclamoServlet();

        injectMock(servlet, "reclamoDAO", reclamoDAO);
        injectMock(servlet, "segnalazioneService", segnalazioneService);
        injectMock(servlet, "emailService", emailService);
        injectMock(servlet, "utenteService", utenteService);
        injectMock(servlet, "dropPointService", dropPointService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    // --- RAMO: SICUREZZA E SESSIONE ---

    @Test
    void testDoPost_NoSession_RedirectLogin() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login");
    }

    // --- RAMO: DROP-POINT (Conferma Ritiro) ---

    @Test
    void testDoPost_ConfermaRitiro_DropPointMancante() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(null);
        when(session.getAttribute("dropPoint")).thenReturn(null); // Forza errore autorizzazione
        when(request.getParameter("action")).thenReturn("conferma_ritiro");

        servlet.doPost(request, response);
        verify(response).sendRedirect("login");
    }

    @Test
    void testDoPost_ConfermaRitiro_Exception_Catch() throws ServletException, IOException {
        setupDropPointSession();
        when(request.getParameter("action")).thenReturn("conferma_ritiro");
        when(request.getParameter("idReclamo")).thenReturn("invalid_id"); // Forza NumberFormatException

        servlet.doPost(request, response);
        verify(response).sendRedirect(contains("err=errore"));
    }

    // --- RAMO: AZIONE "INVIA" (Reclamo) ---

    @Test
    void testDoPost_Invia_ReclamoEsistente() throws ServletException, IOException {
        setupUtenteSession(100L);
        when(request.getParameter("action")).thenReturn("invia");
        when(request.getParameter("idSegnalazione")).thenReturn("1");

        Segnalazione s = new SegnalazioneOggetto();
        s.setStato(StatoSegnalazione.APERTA);
        when(segnalazioneService.trovaPerId(1L)).thenReturn(s);

        // Simula reclamo già esistente per questo utente
        when(reclamoDAO.doRetrieveBySegnalazioneAndUtente(1L, 100L)).thenReturn(new Reclamo());

        servlet.doPost(request, response);
        verify(response).sendRedirect(contains("msg=reclamo_esistente"));
    }

    @Test
    void testDoPost_Invia_SalvataggioFallito() throws ServletException, IOException {
        setupUtenteSession(100L);
        when(request.getParameter("action")).thenReturn("invia");
        when(request.getParameter("idSegnalazione")).thenReturn("1");

        Segnalazione s = new SegnalazioneOggetto();
        s.setStato(StatoSegnalazione.APERTA);
        when(segnalazioneService.trovaPerId(1L)).thenReturn(s);

        when(reclamoDAO.doRetrieveBySegnalazioneAndUtente(1L, 100L)).thenReturn(null);
        when(reclamoDAO.doSave(any())).thenReturn(false); // Salvataggio fallito

        servlet.doPost(request, response);
        // Verifica che NON invii la mail se non salvato
        verify(emailService, never()).inviaNotificaNuovoReclamo(anyString(), anyString(), anyString());
    }

    // --- RAMO: AZIONE "ACCETTA" (Completamento) ---

    @Test
    void testDoPost_Accetta_ConsegnaDiretta() throws ServletException, IOException {
        Utente finder = setupUtenteSession(1L);
        finder.setNome("Mario"); finder.setCognome("Rossi"); finder.setEmail("mario@test.it");

        when(request.getParameter("action")).thenReturn("accetta");
        when(request.getParameter("idReclamo")).thenReturn("10");
        when(request.getParameter("idSegnalazione")).thenReturn("20");

        // Forza Segnalazione Animale (che ha sempre consegna DIRETTA)
        SegnalazioneAnimale sa = new SegnalazioneAnimale();
        sa.setTitolo("Gatto");
        sa.setIdUtente(1L);
        when(segnalazioneService.trovaPerId(20L)).thenReturn(sa);

        when(reclamoDAO.accettaReclamo(10L, null)).thenReturn(true);

        Reclamo r = new Reclamo(); r.setIdUtenteRichiedente(2L);
        when(reclamoDAO.doRetrieveById(10L)).thenReturn(r);

        Utente owner = new Utente(); owner.setNome("Luigi"); owner.setCognome("Verdi"); owner.setEmail("luigi@test.it");
        when(utenteService.trovaPerId(2L)).thenReturn(owner);

        servlet.doPost(request, response);

        verify(emailService).inviaConfermaAccettazioneFinder(eq("mario@test.it"), anyString(), eq("Luigi Verdi"), isNull(), isNull());
        verify(response).sendRedirect(contains("msg=scambio_avviato"));
    }

    // --- RAMO: AZIONE "RIFIUTA" E "CONFERMA SCAMBIO" ---

    @Test
    void testDoPost_Rifiuta() throws ServletException, IOException {
        setupUtenteSession(1L);
        when(request.getParameter("action")).thenReturn("rifiuta");
        when(request.getParameter("idReclamo")).thenReturn("5");
        when(request.getParameter("idSegnalazione")).thenReturn("10");

        servlet.doPost(request, response);

        verify(segnalazioneService).rifiutaReclamo(5L);
        verify(response).sendRedirect(contains("msg=reclamo_rifiutato"));
    }

    @Test
    void testDoPost_ConfermaScambio_NonAncoraFinito() throws ServletException, IOException {
        setupUtenteSession(1L);
        when(request.getParameter("action")).thenReturn("conferma_scambio");
        when(request.getParameter("idReclamo")).thenReturn("1");
        when(request.getParameter("idSegnalazione")).thenReturn("2");

        Segnalazione s = new SegnalazioneOggetto();
        s.setIdUtente(99L); // L'utente loggato (1L) NON è il finder
        when(segnalazioneService.trovaPerId(2L)).thenReturn(s);

        // gestisciConfermaScambio ritorna false (manca ancora una conferma)
        when(segnalazioneService.gestisciConfermaScambio(1L, false)).thenReturn(false);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("msg=conferma_registrata"));
    }

    // --- HELPERS PER MASSIMIZZARE COPERTURA ---

    private Utente setupUtenteSession(long id) {
        Utente u = new Utente();
        u.setId(id);
        u.setUsername("testuser");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);
        return u;
    }

    private void setupDropPointSession() {
        DropPoint dp = new DropPoint();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);
    }
}