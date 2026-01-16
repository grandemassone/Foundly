package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.bean.DropPoint;
import model.bean.enums.StatoDropPoint;
import model.service.DropPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AreaDropPointServletTest {

    private AreaDropPointServlet servlet;

    @Mock private DropPointService dropPointService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new AreaDropPointServlet();

        // Injection del service mockato tramite Reflection
        injectMock(servlet, "dropPointService", dropPointService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    // --- TEST DOGET ---

    @Test
    void testDoGet_Approvato() throws ServletException, IOException {
        DropPoint dp = new DropPoint();
        dp.setId(1L);
        dp.setStato(StatoDropPoint.APPROVATO);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);
        when(dropPointService.trovaPerId(1L)).thenReturn(dp);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dropPointService).countDepositiAttivi(1L);
        verify(request).setAttribute(eq("depositiAttivi"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_InAttesa() throws ServletException, IOException {
        DropPoint dp = new DropPoint();
        dp.setId(1L);
        dp.setStato(StatoDropPoint.IN_ATTESA);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);
        when(dropPointService.trovaPerId(1L)).thenReturn(dp);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(request, response);

        // Se in attesa, le statistiche devono essere forzate a 0
        verify(request).setAttribute("depositiAttivi", 0);
        verify(dispatcher).forward(request, response);
    }

    // --- TEST DOPOST ---

    @Test
    void testDoPost_NonLoggato() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/Foundly");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/Foundly/login");
    }

    @Test
    void testDoPost_DepositoSuccesso() throws ServletException, IOException {
        setupMockDropPoint(StatoDropPoint.APPROVATO);
        when(request.getParameter("action")).thenReturn("deposito");
        when(request.getParameter("codice")).thenReturn("DEP123");
        when(dropPointService.registraDeposito(1L, "DEP123")).thenReturn(true);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("msgDeposito", "Deposito verificato.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_RitiroSuccesso() throws ServletException, IOException {
        setupMockDropPoint(StatoDropPoint.APPROVATO);
        when(request.getParameter("action")).thenReturn("ritiro");
        when(request.getParameter("codice")).thenReturn("RIT456");
        when(dropPointService.registraRitiro(1L, "RIT456")).thenReturn(true);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("msgRitiro"), contains("confermato"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_ErroreStatoNonApprovato() throws ServletException, IOException {
        setupMockDropPoint(StatoDropPoint.IN_ATTESA);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("erroreGenerale"), anyString());
        verify(dropPointService, never()).registraDeposito(anyLong(), anyString());
    }

    // --- HELPER ---
    private void setupMockDropPoint(StatoDropPoint stato) {
        DropPoint dp = new DropPoint();
        dp.setId(1L);
        dp.setStato(stato);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);
        when(dropPointService.trovaPerId(1L)).thenReturn(dp);
    }
}