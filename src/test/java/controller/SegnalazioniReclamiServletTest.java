package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.Reclamo;
import model.bean.Segnalazione;
import model.bean.Utente;
import model.service.SegnalazioneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SegnalazioniReclamiServletTest {

    private SegnalazioniReclamiServlet servlet;

    @Mock private SegnalazioneService service;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new SegnalazioniReclamiServlet();

        // Injection del service mockato tramite Reflection
        injectMock(servlet, "service", service);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testDoGet_UtenteNonLoggato() throws ServletException, IOException {
        // Setup: sessione nulla o utente mancante
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        // Verifica: redirect alla login
        verify(response).sendRedirect("login");
        verify(service, never()).trovaPerUtente(anyLong());
    }

    @Test
    void testDoGet_UtenteLoggato_Successo() throws ServletException, IOException {
        // 1. SETUP: Utente in sessione
        Utente u = new Utente();
        u.setId(123L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);

        // 2. SETUP: Dati finti dal service
        List<Segnalazione> mieSeg = new ArrayList<>();
        mieSeg.add(new Segnalazione() {}); // Classe anonima per Segnalazione abstract

        List<Reclamo> mieiRec = new ArrayList<>();
        mieiRec.add(new Reclamo());

        when(service.trovaPerUtente(123L)).thenReturn(mieSeg);
        when(service.trovaReclamiFattiDaUtente(123L)).thenReturn(mieiRec);

        // Dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // EXECUTE
        servlet.doGet(request, response);

        // VERIFY
        // Verifica che le liste siano state messe negli attributi della request
        verify(request).setAttribute("mieSegnalazioni", mieSeg);
        verify(request).setAttribute("mieiReclami", mieiRec);

        // Verifica forward alla JSP corretta
        verify(request).getRequestDispatcher(eq("/WEB-INF/jsp/segnalazioni_e_reclami.jsp"));
        verify(dispatcher).forward(request, response);
    }
}