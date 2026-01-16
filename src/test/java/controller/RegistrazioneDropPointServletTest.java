package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.DropPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrazioneDropPointServletTest {

    private RegistrazioneDropPointServlet servlet;

    @Mock private DropPointService dpService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new RegistrazioneDropPointServlet();

        // Iniezione del service tramite Reflection
        injectMock(servlet, "dpService", dpService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testDoGet() throws ServletException, IOException {
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher(eq("/WEB-INF/jsp/registrazione_droppoint.jsp"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_Successo() throws ServletException, IOException {
        // 1. SETUP: Parametri validi
        setupRequestParams("Bar Centrale", "bar@test.it", "40.5", "14.5");

        // Usiamo any() per OGNI singolo parametro.
        // Questo ignora il tipo (String, Double, null) e forza il ritorno a true.
        when(dpService.registraDropPoint(
                any(), any(), any(), any(),
                any(), any(), any(), any(),
                any(), any()))
                .thenReturn(true);

        // Mockiamo le infrastrutture della Servlet
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/Foundly");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        // 2. EXECUTE
        servlet.doPost(request, response);
        pw.flush();

        // 3. VERIFY
        String output = sw.toString();

        // Adesso, poiché il mock DEVE aver restituito true, verifichiamo l'alert di successo
        assertTrue(output.contains("effettuata con successo"),
                "La Servlet è entrata nel ramo di errore invece di quello di successo. Output: " + output);

        verify(response).sendRedirect(contains("attesa_approvazione"));
    }

    @Test
    void testDoPost_CampiMancanti() throws ServletException, IOException {
        // Manca la latitudine/longitudine (parametri null)
        when(request.getParameter("email")).thenReturn("test@test.it");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errore"), contains("obbligatori mancanti"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_EmailDuplicata() throws ServletException, IOException {
        setupRequestParams("Bar Duplicato", "esiste@test.it", "40.0", "14.0");

        // Il service restituisce false (email già presente)
        when(dpService.registraDropPoint(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(false);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        // Verifiche
        assertTrue(sw.toString().contains("Email già registrata"));
        verify(request).setAttribute(eq("errore"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_NumberFormatException() throws ServletException, IOException {
        // Testiamo il blocco catch della Servlet inviando stringhe non numeriche
        setupRequestParams("Bar Errore", "err@test.it", "not_a_number", "invalid");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        // Deve scattare il controllo "latitudine == null" e mostrare l'errore campi mancanti
        verify(request).setAttribute(eq("errore"), anyString());
    }

    // Helper per settare i parametri comuni
    private void setupRequestParams(String nome, String email, String lat, String lon) {
        when(request.getParameter("nomeAttivita")).thenReturn(nome);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("latitudine")).thenReturn(lat);
        when(request.getParameter("longitudine")).thenReturn(lon);
        // Altri parametri opzionali
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        when(request.getParameter("citta")).thenReturn("Salerno");
    }
}