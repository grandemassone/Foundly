package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.bean.Utente;
import model.service.UtenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProfiloServletTest {

    private ProfiloServlet servlet;

    @Mock private UtenteService utenteService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private Part part;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new ProfiloServlet();

        // Reflection per iniettare il service finto
        injectMock(servlet, "utenteService", utenteService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    // ==========================================
    // TEST DOGET
    // ==========================================

    @Test
    void testDoGet_NonLoggato() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect(contains("/login"));
    }

    @Test
    void testDoGet_Loggato() throws ServletException, IOException {
        Utente u = new Utente();
        u.setId(1L);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(request, response);

        // Verifica che aggiorni il badge/punteggio prima di mostrare la pagina
        verify(utenteService).aggiornaPunteggioEBadge(u, 0);
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // TEST DOPOST - DELETE ACCOUNT
    // ==========================================

    @Test
    void testDoPost_DeleteAccount_Successo() throws ServletException, IOException {
        Utente u = new Utente();
        u.setId(99L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);

        when(request.getParameter("action")).thenReturn("delete_account");
        when(utenteService.cancellaUtente(99L)).thenReturn(true);

        servlet.doPost(request, response);

        verify(session).invalidate(); // Deve distruggere la sessione
        verify(response).sendRedirect(contains("deleted=1"));
    }

    @Test
    void testDoPost_DeleteAccount_Fallimento() throws ServletException, IOException {
        Utente u = new Utente();
        u.setId(99L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);

        when(request.getParameter("action")).thenReturn("delete_account");
        when(utenteService.cancellaUtente(99L)).thenReturn(false);

        servlet.doPost(request, response);

        verify(session, never()).invalidate(); // NON deve distruggere la sessione
        verify(response).sendRedirect(contains("error=delete_failed"));
    }

    // ==========================================
    // TEST DOPOST - UPDATE PROFILE
    // ==========================================

    @Test
    void testDoPost_UpdateBase_Successo() throws ServletException, IOException {
        Utente u = new Utente();
        u.setId(1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);

        // Parametri form
        when(request.getParameter("username")).thenReturn("mario99");
        when(request.getParameter("nome")).thenReturn("Mario");
        when(request.getParameter("cognome")).thenReturn("Rossi");

        // Mock upload nullo
        when(request.getPart("avatar")).thenReturn(null);

        when(utenteService.aggiornaProfilo(u)).thenReturn(true);

        servlet.doPost(request, response);

        verify(utenteService).aggiornaProfilo(u);
        verify(response).sendRedirect(contains("success=1"));
    }

    @Test
    void testDoPost_UpdateConImmagine() throws ServletException, IOException {
        Utente u = new Utente();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);

        // Mock del Part (file upload)
        when(part.getSize()).thenReturn(100L);
        when(part.getContentType()).thenReturn("image/jpeg");
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(request.getPart("avatar")).thenReturn(part);

        when(utenteService.aggiornaProfilo(u)).thenReturn(true);

        servlet.doPost(request, response);

        // Verifica che l'immagine sia stata settata (indirettamente tramite service call)
        // Poiché l'oggetto 'u' è modificato by reference, potremmo anche fare:
        // assertNotNull(u.getImmagineProfilo());
        verify(utenteService).aggiornaProfilo(argThat(user -> user.getImmagineProfilo() != null));
    }

    @Test
    void testDoPost_RimuoviAvatar() throws ServletException, IOException {
        Utente u = new Utente();
        u.setImmagineProfilo(new byte[]{1}); // Ha un'immagine
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);

        when(request.getParameter("removeAvatar")).thenReturn("true");
        when(request.getPart("avatar")).thenReturn(null); // Nessun nuovo file

        when(utenteService.aggiornaProfilo(u)).thenReturn(true);

        servlet.doPost(request, response);

        // Verifica che l'immagine sia stata rimossa
        verify(utenteService).aggiornaProfilo(argThat(user -> user.getImmagineProfilo() == null));
    }

    @Test
    void testDoPost_UpdateFallito() throws ServletException, IOException {
        Utente u = new Utente();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);

        when(utenteService.aggiornaProfilo(u)).thenReturn(false);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error=update_failed"));
    }

    @Test
    void testDoPost_NonLoggato() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect(contains("/login"));
    }
}