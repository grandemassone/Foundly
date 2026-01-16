package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.Segnalazione;
import model.bean.SegnalazioneOggetto;
import model.service.SegnalazioneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SegnalazioneImgServletTest {

    private SegnalazioneImgServlet servlet;

    @Mock private SegnalazioneService segnalazioneService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private ServletOutputStream outputStream;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new SegnalazioneImgServlet();

        // Iniettiamo il service tramite Reflection
        injectMock(servlet, "segnalazioneService", segnalazioneService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testDoGet_MissingId() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    void testDoGet_InvalidId() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("abc");

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    void testDoGet_ImageNotFound() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("1");
        Segnalazione s = new SegnalazioneOggetto();
        s.setImmagine(null); // Segnalazione esiste ma non ha immagine

        when(segnalazioneService.trovaPerId(1L)).thenReturn(s);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND));
    }

    @Test
    void testDoGet_Success() throws ServletException, IOException {
        // Setup dati binari finti
        byte[] imageData = new byte[]{0x12, 0x34, 0x56};
        Segnalazione s = new SegnalazioneOggetto();
        s.setImmagine(imageData);
        s.setImmagineContentType("image/png");

        when(request.getParameter("id")).thenReturn("1");
        when(segnalazioneService.trovaPerId(1L)).thenReturn(s);
        when(response.getOutputStream()).thenReturn(outputStream);

        servlet.doGet(request, response);

        // Verifichiamo gli header della risposta
        verify(response).setContentType("image/png");
        verify(response).setContentLength(imageData.length);

        // Verifichiamo che i byte siano stati scritti nell'output stream
        verify(outputStream).write(imageData);
    }

    @Test
    void testDoGet_DefaultContentType() throws ServletException, IOException {
        byte[] imageData = new byte[]{0x12};
        Segnalazione s = new SegnalazioneOggetto();
        s.setImmagine(imageData);
        s.setImmagineContentType(null); // Content Type mancante

        when(request.getParameter("id")).thenReturn("1");
        when(segnalazioneService.trovaPerId(1L)).thenReturn(s);
        when(response.getOutputStream()).thenReturn(outputStream);

        servlet.doGet(request, response);

        // Deve usare il default image/jpeg
        verify(response).setContentType("image/jpeg");
    }
}