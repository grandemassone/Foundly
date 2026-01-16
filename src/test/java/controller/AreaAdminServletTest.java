package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.Utente;
import model.bean.enums.Ruolo;
import model.service.DropPointService;
import model.service.SegnalazioneService;
import model.service.UtenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AreaAdminServletTest {

    private AreaAdminServlet servlet;

    @Mock private DropPointService dropPointService;
    @Mock private UtenteService utenteService;
    @Mock private SegnalazioneService segnalazioneService;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new AreaAdminServlet();

        // Injection dei 3 service mockati tramite Reflection
        injectMock(servlet, "dropPointService", dropPointService);
        injectMock(servlet, "utenteService", utenteService);
        injectMock(servlet, "segnalazioneService", segnalazioneService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    // --- TEST SICUREZZA ---

    @Test
    void testDoGet_AccessoNegato_NonAdmin() throws ServletException, IOException {
        Utente u = new Utente();
        u.setRuolo(Ruolo.UTENTE_BASE); // NON è admin

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    // --- TEST DOGET ---

    @Test
    void testDoGet_SuccessoAdmin() throws ServletException, IOException {
        Utente admin = new Utente();
        admin.setRuolo(Ruolo.ADMIN);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(admin);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Simuliamo liste caricate dai service
        when(dropPointService.findAllInAttesa()).thenReturn(new ArrayList<>());
        when(dropPointService.findAllApprovati()).thenReturn(new ArrayList<>());
        when(utenteService.trovaTutti()).thenReturn(new ArrayList<>());
        when(segnalazioneService.trovaTutte()).thenReturn(new ArrayList<>());

        servlet.doGet(request, response);

        // Verifichiamo che tutti gli attributi per la dashboard siano stati settati
        verify(request).setAttribute(eq("dropPointsPendenti"), any());
        verify(request).setAttribute(eq("dropPointsApprovati"), any());
        verify(request).setAttribute(eq("listaUtenti"), any());
        verify(request).setAttribute(eq("listaSegnalazioni"), any());
        verify(dispatcher).forward(request, response);
    }

    // --- TEST DOPOST AZIONI ---

    @Test
    void testDoPost_ApproveDropPoint() throws ServletException, IOException {
        setupAdminSession();
        when(request.getParameter("action")).thenReturn("approveDropPoint");
        when(request.getParameter("dropPointId")).thenReturn("10");

        servlet.doPost(request, response);

        verify(dropPointService).approvaDropPoint(10L);
        verify(response).sendRedirect(anyString());
    }

    @Test
    void testDoPost_RejectDropPoint() throws ServletException, IOException {
        setupAdminSession();
        when(request.getParameter("action")).thenReturn("rejectDropPoint");
        when(request.getParameter("dropPointId")).thenReturn("20");

        servlet.doPost(request, response);

        verify(dropPointService).rifiutaDropPoint(20L);
    }

    @Test
    void testDoPost_DeleteDropPoint() throws ServletException, IOException {
        setupAdminSession();
        when(request.getParameter("action")).thenReturn("deleteDropPoint");
        when(request.getParameter("dropPointId")).thenReturn("30");

        servlet.doPost(request, response);

        verify(dropPointService).eliminaDropPoint(30L);
    }

    @Test
    void testDoPost_DeleteUser() throws ServletException, IOException {
        Utente admin = new Utente();
        admin.setId(1L);
        admin.setRuolo(Ruolo.ADMIN);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(admin);

        when(request.getParameter("action")).thenReturn("deleteUser");
        when(request.getParameter("userId")).thenReturn("99"); // Utente diverso dall'admin

        servlet.doPost(request, response);

        verify(utenteService).cancellaUtente(99L);
    }

    @Test
    void testDoPost_DeleteSegnalazione() throws ServletException, IOException {
        setupAdminSession();
        when(request.getParameter("action")).thenReturn("deleteSegnalazione");
        when(request.getParameter("segnalazioneId")).thenReturn("500");

        servlet.doPost(request, response);

        verify(segnalazioneService).eliminaSegnalazione(500L);
    }

    @Test
    void testDoPost_ActionNull() throws ServletException, IOException {
        setupAdminSession();
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect(anyString());
        verifyNoInteractions(utenteService, dropPointService, segnalazioneService);
    }

    // --- HELPER ---
    private void setupAdminSession() {
        Utente admin = new Utente();
        admin.setRuolo(Ruolo.ADMIN);
        admin.setId(1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(admin);
    }
}