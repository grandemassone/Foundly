package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.*;
import model.bean.enums.ModalitaConsegna;
import model.dao.ReclamoDAO;
import model.dao.UtenteDAO;
import model.service.DropPointService;
import model.service.SegnalazioneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DettaglioSegnalazioneServletTest {

    private DettaglioSegnalazioneServlet servlet;

    @Mock private SegnalazioneService segnalazioneService;
    @Mock private ReclamoDAO reclamoDAO;
    @Mock private UtenteDAO utenteDAO;
    @Mock private DropPointService dropPointService;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new DettaglioSegnalazioneServlet();

        // Iniezione di tutte le dipendenze
        injectMock(servlet, "segnalazioneService", segnalazioneService);
        injectMock(servlet, "reclamoDAO", reclamoDAO);
        injectMock(servlet, "utenteDAO", utenteDAO);
        injectMock(servlet, "dropPointService", dropPointService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testDoGet_IdMancante() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("index");
    }

    @Test
    void testDoGet_SegnalazioneInesistente() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("10");
        when(segnalazioneService.trovaPerId(10L)).thenReturn(null);

        servlet.doGet(request, response);
        verify(response).sendRedirect("index");
    }

    @Test
    void testDoGet_VistaFinder_ConReclami() throws ServletException, IOException {
        long idSeg = 1L;
        long idFinder = 100L;
        long idRichiedente = 200L;

        // Setup Segnalazione (Oggetto con DropPoint)
        SegnalazioneOggetto so = new SegnalazioneOggetto();
        so.setId(idSeg);
        so.setIdUtente(idFinder);
        so.setModalitaConsegna(ModalitaConsegna.DROP_POINT);
        so.setIdDropPoint(5L);

        // Setup Utente loggato (Finder)
        Utente finder = new Utente();
        finder.setId(idFinder);

        when(request.getParameter("id")).thenReturn(String.valueOf(idSeg));
        when(segnalazioneService.trovaPerId(idSeg)).thenReturn(so);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(finder);

        // Mock DropPoint
        DropPoint dp = new DropPoint();
        when(dropPointService.trovaPerId(5L)).thenReturn(dp);

        // Mock Reclami ricevuti
        List<Reclamo> reclami = new ArrayList<>();
        Reclamo r = new Reclamo();
        r.setIdUtenteRichiedente(idRichiedente);
        reclami.add(r);
        when(reclamoDAO.doRetrieveBySegnalazione(idSeg)).thenReturn(reclami);

        // Mock caricamento dati richiedente per la mappa
        Utente richiedente = new Utente();
        richiedente.setId(idRichiedente);
        when(utenteDAO.doRetrieveById(idRichiedente)).thenReturn(richiedente);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(request, response);

        // Verifiche attributi
        verify(request).setAttribute(eq("dropPointRitiro"), eq(dp));
        verify(request).setAttribute(eq("reclamiRicevuti"), eq(reclami));
        verify(request).setAttribute(argThat(s -> s.equals("mappaRichiedenti")), any(Map.class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_VistaOwner_ConMioReclamo() throws ServletException, IOException {
        long idSeg = 1L;
        long idFinder = 100L;
        long idOwner = 300L;

        SegnalazioneOggetto s = new SegnalazioneOggetto();
        s.setId(idSeg);
        s.setIdUtente(idFinder);

        Utente owner = new Utente();
        owner.setId(idOwner);

        when(request.getParameter("id")).thenReturn(String.valueOf(idSeg));
        when(segnalazioneService.trovaPerId(idSeg)).thenReturn(s);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(owner);

        // Mock mio reclamo esistente
        Reclamo mioR = new Reclamo();
        when(reclamoDAO.doRetrieveBySegnalazioneAndUtente(idSeg, idOwner)).thenReturn(mioR);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("mioReclamo"), eq(mioR));
        verify(request).setAttribute(eq("segnalazione"), eq(s));
    }

    @Test
    void testDoPost_DeleteSuccess() throws ServletException, IOException {
        long idSeg = 1L;
        long idUser = 10L;

        Utente u = new Utente();
        u.setId(idUser);

        Segnalazione s = new SegnalazioneOggetto();
        s.setIdUtente(idUser);

        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("idSegnalazione")).thenReturn(String.valueOf(idSeg));
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);
        when(segnalazioneService.trovaPerId(idSeg)).thenReturn(s);

        servlet.doPost(request, response);

        verify(segnalazioneService).eliminaSegnalazione(idSeg);
        verify(response).sendRedirect("le-mie-segnalazioni");
    }
}