package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.Utente;
import model.service.EmailService;
import model.service.UtenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecuperoPasswordServletTest {

    private RecuperoPasswordServlet servlet;

    @Mock private UtenteService utenteService;
    @Mock private EmailService emailService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new RecuperoPasswordServlet();

        // Injection dei service mockati
        injectMock(servlet, "utenteService", utenteService);
        injectMock(servlet, "emailService", emailService);
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
    void testDoGet() throws ServletException, IOException {
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // TEST DOPOST - STEP EMAIL
    // ==========================================

    @Test
    void testStepEmail_UtenteEsistente() throws ServletException, IOException {
        // Setup
        when(request.getParameter("step")).thenReturn("email");
        when(request.getParameter("email")).thenReturn("mario@test.it");
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(contains("codice.jsp"))).thenReturn(dispatcher);

        // Utente trovato
        when(utenteService.trovaPerEmail("mario@test.it")).thenReturn(new Utente());

        // Execute
        servlet.doPost(request, response);

        // Verify
        // Deve aver settato il codice in sessione
        verify(session).setAttribute(eq("recuperoCodice"), anyString());
        // Deve aver inviato l'email
        verify(emailService).inviaCodiceRecuperoPassword(eq("mario@test.it"), anyString());
        // Forward alla pagina del codice
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testStepEmail_UtenteNonTrovato() throws ServletException, IOException {
        // Setup
        when(request.getParameter("step")).thenReturn("email");
        when(request.getParameter("email")).thenReturn("ignoto@test.it");
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(contains("codice.jsp"))).thenReturn(dispatcher);

        // Utente NON trovato
        when(utenteService.trovaPerEmail(anyString())).thenReturn(null);

        // Execute
        servlet.doPost(request, response);

        // Verify
        // NON deve inviare email
        verify(emailService, never()).inviaCodiceRecuperoPassword(anyString(), anyString());
        // Ma deve comunque andare alla pagina del codice (per sicurezza/privacy)
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // TEST DOPOST - STEP CODICE
    // ==========================================

    @Test
    void testStepCodice_SessioneScadutaONulla() throws ServletException, IOException {
        when(request.getParameter("step")).thenReturn("codice");
        when(request.getSession(false)).thenReturn(null); // Sessione persa
        when(request.getRequestDispatcher(contains("email.jsp"))).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errore"), contains("scaduta"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testStepCodice_DatiMancantiInSessione() throws ServletException, IOException {
        when(request.getParameter("step")).thenReturn("codice");
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(contains("email.jsp"))).thenReturn(dispatcher);

        // Sessione c'è, ma mancano gli attributi (es. ha saltato il primo step)
        when(session.getAttribute("recuperoEmail")).thenReturn(null);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errore"), contains("non valida"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testStepCodice_CodiceScaduto() throws ServletException, IOException {
        when(request.getParameter("step")).thenReturn("codice");
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(contains("email.jsp"))).thenReturn(dispatcher);

        // Simuliamo dati validi ma scaduti nel passato
        when(session.getAttribute("recuperoEmail")).thenReturn("test@test.it");
        when(session.getAttribute("recuperoCodice")).thenReturn("123456");
        when(session.getAttribute("recuperoScadenza")).thenReturn(System.currentTimeMillis() - 10000); // Scaduto da 10s

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errore"), contains("scaduto"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testStepCodice_CodiceErrato() throws ServletException, IOException {
        // Setup dati sessione validi
        setupSessioneValida("123456");
        when(request.getRequestDispatcher(contains("codice.jsp"))).thenReturn(dispatcher);

        // Input utente errato
        when(request.getParameter("codice")).thenReturn("000000");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errore"), contains("Codice non valido"));
    }

    @Test
    void testStepCodice_PasswordNonCoincidenti() throws ServletException, IOException {
        setupSessioneValida("123456");
        when(request.getRequestDispatcher(contains("codice.jsp"))).thenReturn(dispatcher);

        when(request.getParameter("codice")).thenReturn("123456");
        when(request.getParameter("nuovaPassword")).thenReturn("Pass1");
        when(request.getParameter("confermaPassword")).thenReturn("Pass2");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errore"), contains("non coincidono"));
    }

    @Test
    void testStepCodice_PasswordDebole() throws ServletException, IOException {
        setupSessioneValida("123456");
        when(request.getRequestDispatcher(contains("codice.jsp"))).thenReturn(dispatcher);

        when(request.getParameter("codice")).thenReturn("123456");
        // Password troppo semplice (manca maiuscola o carattere speciale)
        when(request.getParameter("nuovaPassword")).thenReturn("debole");
        when(request.getParameter("confermaPassword")).thenReturn("debole");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errore"), contains("requisiti"));
    }

    @Test
    void testStepCodice_Successo() throws ServletException, IOException {
        setupSessioneValida("123456");
        when(request.getRequestDispatcher(contains("login.jsp"))).thenReturn(dispatcher);

        when(request.getParameter("codice")).thenReturn("123456");
        // Password forte valida per la regex: 1 Maiusc, 1 minusc, 1 numero, 1 speciale, min 8 char
        String validPass = "PasswordForte1!";
        when(request.getParameter("nuovaPassword")).thenReturn(validPass);
        when(request.getParameter("confermaPassword")).thenReturn(validPass);

        // Simuliamo che il service aggiorni correttamente
        when(utenteService.resetPasswordByEmail(eq("test@test.it"), eq(validPass))).thenReturn(true);

        servlet.doPost(request, response);

        // Verifica pulizia sessione
        verify(session).removeAttribute("recuperoCodice");
        // Verifica redirect al login
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testStepInvalido() throws ServletException, IOException {
        when(request.getParameter("step")).thenReturn("step_inesistente");
        servlet.doPost(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // --- Helper per setup sessione valida ---
    private void setupSessioneValida(String codice) {
        when(request.getParameter("step")).thenReturn("codice");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("recuperoEmail")).thenReturn("test@test.it");
        when(session.getAttribute("recuperoCodice")).thenReturn(codice);
        // Scadenza nel futuro
        when(session.getAttribute("recuperoScadenza")).thenReturn(System.currentTimeMillis() + 60000);
    }
}