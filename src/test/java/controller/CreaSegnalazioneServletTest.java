package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.bean.Utente;
import model.service.DropPointService;
import model.service.SegnalazioneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CreaSegnalazioneServletTest {

    private CreaSegnalazioneServlet servlet;

    @Mock private DropPointService dropPointService;
    @Mock private SegnalazioneService segnalazioneService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private Part filePart;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new CreaSegnalazioneServlet();

        injectMock(servlet, "dropPointService", dropPointService);
        injectMock(servlet, "segnalazioneService", segnalazioneService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testDoGet_Success() throws ServletException, IOException {
        setupUserSession();
        when(dropPointService.findAllApprovati()).thenReturn(new ArrayList<>());
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("listaDropPoint"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_DataErrata() throws ServletException, IOException {
        setupUserSession();
        when(request.getParameter("dataRitrovamento")).thenReturn("1990-01-01"); // Anno < 2000
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errore"), contains("2000"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_SuccessoOggettoConDropPoint() throws ServletException, IOException {
        setupUserSession();
        setupCommonParams("OGGETTO");

        when(request.getParameter("categoria")).thenReturn("ELETTRONICA");
        when(request.getParameter("modalita_consegna")).thenReturn("DROP_POINT");
        when(request.getParameter("idDropPoint")).thenReturn("10");

        // Mock immagine
        when(request.getPart("immagine")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(100L);
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));
        when(filePart.getContentType()).thenReturn("image/png");

        when(segnalazioneService.creaSegnalazione(any())).thenReturn(true);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(request, response);

        assertTrue(sw.toString().contains("pubblicata con successo"));
    }

    @Test
    void testDoPost_SuccessoAnimale() throws ServletException, IOException {
        setupUserSession();
        setupCommonParams("ANIMALE");
        when(request.getParameter("specie")).thenReturn("Cane");
        when(request.getParameter("razza")).thenReturn("Labrador");

        when(segnalazioneService.creaSegnalazione(any())).thenReturn(true);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        servlet.doPost(request, response);

        verify(segnalazioneService).creaSegnalazione(argThat(s -> s.getTipoSegnalazione().toString().equals("ANIMALE")));
    }

    @Test
    void testDoPost_ExceptionGenerale() throws ServletException, IOException {
        setupUserSession();
        setupCommonParams("OGGETTO");

        // Invece di far fallire getPart (che viene ignorato dal tuo catch interno),
        // facciamo fallire il service.creaSegnalazione lanciando una RuntimeException.
        // Questo farà saltare il codice direttamente nel CATCH GRANDE della Servlet.

        when(segnalazioneService.creaSegnalazione(any())).thenThrow(new RuntimeException("Simulazione errore generico"));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        // Adesso verifichiamo che entri nel catch finale dove c'è il messaggio sulla grandezza immagine
        verify(request).setAttribute(eq("errore"), contains("troppo grande"));
        verify(dispatcher).forward(request, response);
    }

    // --- Helpers ---
    private void setupUserSession() {
        Utente u = new Utente();
        u.setId(1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("utente")).thenReturn(u);
    }

    private void setupCommonParams(String tipo) {
        when(request.getParameter("tipo_segnalazione")).thenReturn(tipo);
        when(request.getParameter("titolo")).thenReturn("Test Titolo");
        when(request.getParameter("dataRitrovamento")).thenReturn("2024-01-01T10:00");
        when(request.getParameter("citta")).thenReturn("Salerno");
    }
}