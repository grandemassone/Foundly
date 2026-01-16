package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class LogoutServletTest {

    private LogoutServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new LogoutServlet();
    }

    @Test
    void testDoGet_ConSessioneAttiva() throws ServletException, IOException {
        // 1. SETUP: Simuliamo che la sessione esista
        when(request.getSession(false)).thenReturn(session);

        // 2. EXECUTE
        servlet.doGet(request, response);

        // 3. VERIFY
        // Verifica che la sessione sia stata invalidata (distrutta)
        verify(session).invalidate();
        // Verifica il redirect alla home
        verify(response).sendRedirect(contains("/index"));
    }

    @Test
    void testDoGet_SenzaSessione() throws ServletException, IOException {
        // 1. SETUP: La sessione è null (già scaduta o mai loggato)
        when(request.getSession(false)).thenReturn(null);

        // 2. EXECUTE
        servlet.doGet(request, response);

        // 3. VERIFY
        // IMPORTANTE: Verifica che NON abbia provato a invalidare null (altrimenti NullPointer)
        verify(session, never()).invalidate();
        // Il redirect deve avvenire comunque
        verify(response).sendRedirect(contains("/index"));
    }

    @Test
    void testDoPost() throws ServletException, IOException {
        // Il doPost chiama doGet, quindi testiamo che faccia la stessa cosa
        when(request.getSession(false)).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect(contains("/index"));
    }
}