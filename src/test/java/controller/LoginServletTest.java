package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.DropPoint;
import model.bean.Utente;
import model.service.DropPointService;
import model.service.UtenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoginServletTest {

    private LoginServlet servlet;

    // I Mocks (oggetti finti)
    @Mock private UtenteService utenteService;
    @Mock private DropPointService dropPointService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Inizializza i mock
        MockitoAnnotations.openMocks(this);

        // 2. Crea la servlet reale (qui dentro si creano i service "veri")
        servlet = new LoginServlet();

        // 3. TRUCCO (Reflection): Sostituiamo i service veri con quelli finti
        // Sostituisco 'utenteService'
        injectMock(servlet, "utenteService", utenteService);
        // Sostituisco 'dropPointService'
        injectMock(servlet, "dropPointService", dropPointService);

        // 4. Configurazione base della request
        when(request.getSession(true)).thenReturn(session);
    }

    /**
     * Metodo helper per iniettare i mock nei campi privati/final usando Reflection
     */
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        // Trova il campo privato nella classe
        Field field = target.getClass().getDeclaredField(fieldName);
        // Rendilo accessibile (togli il "private")
        field.setAccessible(true);
        // Sovrascrivi il valore con il nostro mock
        field.set(target, mock);
    }

    @Test
    void testLoginUtenteSuccess() throws ServletException, IOException {
        // SETUP
        when(request.getParameter("email")).thenReturn("mario@test.it");
        when(request.getParameter("password")).thenReturn("password123");

        Utente utenteFinto = new Utente();
        utenteFinto.setEmail("mario@test.it");

        // Istruiamo il mock del service
        when(utenteService.login("mario@test.it", "password123")).thenReturn(utenteFinto);

        // EXECUTE
        servlet.doPost(request, response);

        // VERIFY
        verify(session).setAttribute("utente", utenteFinto);
        verify(session).setAttribute("ruoloLoggato", "CITTADINO");
        verify(response).sendRedirect(contains("/index"));
    }

    @Test
    void testLoginDropPointSuccess() throws ServletException, IOException {
        // SETUP
        when(request.getParameter("email")).thenReturn("dp@foundly.it");
        when(request.getParameter("password")).thenReturn("dpPass");

        // Utente non trovato
        when(utenteService.login(anyString(), anyString())).thenReturn(null);

        // DropPoint trovato
        DropPoint dpFinto = new DropPoint();
        when(dropPointService.login("dp@foundly.it", "dpPass")).thenReturn(dpFinto);

        // EXECUTE
        servlet.doPost(request, response);

        // VERIFY
        verify(session).setAttribute("dropPoint", dpFinto);
        verify(session).setAttribute("ruoloLoggato", "DROPPOINT");
        verify(response).sendRedirect(contains("/area-drop-point"));
    }

    @Test
    void testLoginFailure() throws ServletException, IOException {
        // SETUP
        when(request.getParameter("email")).thenReturn("errato@test.it");
        when(request.getParameter("password")).thenReturn("errato");

        // Nessuno trovato
        when(utenteService.login(anyString(), anyString())).thenReturn(null);
        when(dropPointService.login(anyString(), anyString())).thenReturn(null);

        // Simuliamo il dispatcher
        when(request.getRequestDispatcher("/WEB-INF/jsp/login.jsp")).thenReturn(dispatcher);

        // EXECUTE
        servlet.doPost(request, response);

        // VERIFY
        verify(request).setAttribute(eq("errore"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet() throws ServletException, IOException {
        // Testiamo anche il doGet per avere copertura completa
        when(request.getRequestDispatcher("/WEB-INF/jsp/login.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }
}