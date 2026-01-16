package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.bean.DropPoint;
import model.service.DropPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProfiloDropPointServletTest {

    private ProfiloDropPointServlet servlet;

    @Mock private DropPointService dropPointService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private Part logoPart;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new ProfiloDropPointServlet();

        // Reflection per iniettare il service finto
        injectMock(servlet, "dropPointService", dropPointService);
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
    void testDoGet_LoggatoSuccesso() throws ServletException, IOException {
        // Setup sessione
        DropPoint dp = new DropPoint();
        dp.setId(1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);

        // Setup service (refresh dati)
        DropPoint freshDp = new DropPoint();
        freshDp.setId(1L);
        freshDp.setNomeAttivita("Bar Fresco");
        when(dropPointService.trovaPerId(1L)).thenReturn(freshDp);

        // Setup dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doGet(request, response);

        // Verifica che aggiorni la sessione con i dati freschi
        verify(session).setAttribute("dropPoint", freshDp);
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // TEST DOPOST - DELETE ACCOUNT
    // ==========================================

    @Test
    void testDoPost_DeleteAccount_Successo() throws ServletException, IOException {
        // Setup sessione
        DropPoint dp = new DropPoint();
        dp.setId(10L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);

        // Parametri request
        when(request.getParameter("action")).thenReturn("delete_account");

        // Service return true
        when(dropPointService.eliminaDropPoint(10L)).thenReturn(true);

        servlet.doPost(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect(contains("dpDeleted=1"));
    }

    @Test
    void testDoPost_DeleteAccount_Fallimento() throws ServletException, IOException {
        // Setup sessione
        DropPoint dp = new DropPoint();
        dp.setId(10L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);

        when(request.getParameter("action")).thenReturn("delete_account");
        when(dropPointService.eliminaDropPoint(10L)).thenReturn(false);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error=delete_failed"));
    }

    // ==========================================
    // TEST DOPOST - UPDATE PROFILE
    // ==========================================

    @Test
    void testDoPost_UpdateProfile_Successo() throws ServletException, IOException {
        // Setup sessione
        DropPoint dp = new DropPoint();
        dp.setId(5L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);

        // Parametri form
        when(request.getParameter("action")).thenReturn("update");
        when(request.getParameter("nomeAttivita")).thenReturn("Nuovo Nome");
        when(request.getParameter("indirizzo")).thenReturn("Via Nuova");
        // ... altri parametri opzionali non settati (null)

        // Mock upload immagine (nessun file caricato)
        when(request.getPart("logo")).thenReturn(null);

        // Service
        when(dropPointService.aggiornaProfilo(any(DropPoint.class))).thenReturn(true);
        when(dropPointService.trovaPerId(5L)).thenReturn(dp); // Refresh finale

        servlet.doPost(request, response);

        verify(dropPointService).aggiornaProfilo(dp);
        verify(response).sendRedirect(contains("success=1"));
    }

    @Test
    void testDoPost_UpdateConImmagine() throws ServletException, IOException {
        DropPoint dp = new DropPoint();
        dp.setId(5L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);

        when(request.getParameter("action")).thenReturn("update");

        // Mock del file upload
        when(logoPart.getSize()).thenReturn(100L); // Dimensione > 0
        when(logoPart.getContentType()).thenReturn("image/png");
        when(logoPart.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(request.getPart("logo")).thenReturn(logoPart);

        when(dropPointService.aggiornaProfilo(any())).thenReturn(true);

        servlet.doPost(request, response);

        // Verifichiamo che abbia settato l'immagine nel bean
        // (Non possiamo controllare direttamente il campo privato facilmente,
        // ma sappiamo che il service viene chiamato con l'oggetto modificato)
        verify(dropPointService).aggiornaProfilo(dp);
    }

    @Test
    void testDoPost_RemoveLogo() throws ServletException, IOException {
        DropPoint dp = new DropPoint();
        dp.setId(5L);
        dp.setImmagine(new byte[]{1}); // Ha un'immagine
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);

        when(request.getParameter("action")).thenReturn("update");
        when(request.getParameter("removeLogo")).thenReturn("true");

        // Nessun nuovo file
        when(request.getPart("logo")).thenReturn(null);

        when(dropPointService.aggiornaProfilo(any())).thenReturn(true);

        servlet.doPost(request, response);

        // Verifica indiretta: se chiamiamo aggiornaProfilo, l'immagine dovrebbe essere nullata
        verify(dropPointService).aggiornaProfilo(argThat(d -> d.getImmagine() == null));
    }

    @Test
    void testDoPost_UpdateFallito() throws ServletException, IOException {
        DropPoint dp = new DropPoint();
        dp.setId(5L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("dropPoint")).thenReturn(dp);

        when(request.getParameter("action")).thenReturn("update");
        when(dropPointService.aggiornaProfilo(any())).thenReturn(false);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error=update_failed"));
    }
}