package controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

class AvatarServletTest {

    private AvatarServlet servlet;

    @Mock private UtenteService utenteService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private ServletOutputStream outputStream;
    @Mock private ServletContext servletContext;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = spy(new AvatarServlet());

        // Iniezione del service
        injectMock(servlet, "utenteService", utenteService);

        // Mock del ServletContext per l'avatar di default
        doReturn(servletContext).when(servlet).getServletContext();
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testDoGet_Successo() throws IOException {
        long userId = 1L;
        byte[] imgData = new byte[]{1, 2, 3};
        Utente u = new Utente();
        u.setImmagineProfilo(imgData);
        u.setImmagineProfiloContentType("image/png");

        when(request.getParameter("userId")).thenReturn("1");
        when(utenteService.trovaPerId(userId)).thenReturn(u);
        when(response.getOutputStream()).thenReturn(outputStream);

        servlet.doGet(request, response);

        verify(response).setContentType("image/png");
        verify(outputStream).write(imgData);
    }

    @Test
    void testDoGet_IdInvalido_DefaultAvatar() throws IOException {
        // Forza il blocco catch(Exception e)
        when(request.getParameter("userId")).thenReturn("not-a-number");

        // Mock del caricamento risorsa di default
        byte[] defaultImg = new byte[]{9, 9};
        when(servletContext.getResourceAsStream(anyString()))
                .thenReturn(new ByteArrayInputStream(defaultImg));
        when(response.getOutputStream()).thenReturn(outputStream);

        servlet.doGet(request, response);

        verify(response).setContentType("image/png");
        verify(outputStream).write(defaultImg);
    }

    @Test
    void testDoGet_UtenteSenzaFoto_DefaultAvatar() throws IOException {
        long userId = 2L;
        when(request.getParameter("userId")).thenReturn("2");
        when(utenteService.trovaPerId(userId)).thenReturn(null); // Utente non trovato

        when(servletContext.getResourceAsStream(anyString()))
                .thenReturn(new ByteArrayInputStream(new byte[]{0}));
        when(response.getOutputStream()).thenReturn(outputStream);

        servlet.doGet(request, response);

        // Verifica che sia andato nel ramo default
        verify(servletContext).getResourceAsStream(contains("default-avatar.png"));
    }

    @Test
    void testDoGet_DefaultAvatarMancante() throws IOException {
        when(request.getParameter("userId")).thenReturn("invalid");
        // Simula che il file default-avatar.png non esista sul server
        when(servletContext.getResourceAsStream(anyString())).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}