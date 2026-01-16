package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.Utente;
import model.service.UtenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ClassificaServletTest {

    private ClassificaServlet servlet;

    @Mock private UtenteService utenteService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new ClassificaServlet();

        // Injection del service mockato tramite Reflection
        injectMock(servlet, "utenteService", utenteService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testDoGet_Successo() throws ServletException, IOException {
        // 1. SETUP: Creiamo una lista finta di utenti (classifica)
        List<Utente> classificaFinta = new ArrayList<>();
        Utente u1 = new Utente(); u1.setUsername("top1"); u1.setPunteggio(100);
        Utente u2 = new Utente(); u2.setUsername("top2"); u2.setPunteggio(80);
        classificaFinta.add(u1);
        classificaFinta.add(u2);

        // Quando il servlet chiede la classifica, restituisci quella finta
        when(utenteService.getClassificaUtenti()).thenReturn(classificaFinta);

        // Mock del dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // 2. EXECUTE
        servlet.doGet(request, response);

        // 3. VERIFY
        // Verifica che la lista sia stata messa nell'attributo corretto della request
        verify(request).setAttribute("classifica", classificaFinta);

        // Verifica che sia stato chiamato il forward verso la JSP della classifica
        verify(request).getRequestDispatcher(eq("/WEB-INF/jsp/classifica.jsp"));
        verify(dispatcher).forward(request, response);
    }
}