package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.DropPoint;
import model.service.DropPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DropPointAvatarServletTest {

    private DropPointAvatarServlet servlet;

    @Mock private DropPointService dropPointService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private ServletOutputStream outputStream;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new DropPointAvatarServlet();

        // Iniezione del service tramite Reflection
        injectMock(servlet, "dropPointService", dropPointService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testDoGet_IdMancante() throws ServletException, IOException {
        when(request.getParameter("dpId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testDoGet_IdNonNumerico() throws ServletException, IOException {
        when(request.getParameter("dpId")).thenReturn("abc");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testDoGet_DropPointOImmagineAssente() throws ServletException, IOException {
        when(request.getParameter("dpId")).thenReturn("1");

        // Caso 1: DropPoint non esiste
        when(dropPointService.trovaPerId(1L)).thenReturn(null);
        servlet.doGet(request, response);

        // Caso 2: Esiste ma non ha byte immagine
        DropPoint dp = new DropPoint();
        dp.setImmagine(null);
        when(dropPointService.trovaPerId(1L)).thenReturn(dp);
        servlet.doGet(request, response);

        verify(response, times(2)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testDoGet_Successo() throws ServletException, IOException {
        byte[] mockImg = new byte[]{10, 20, 30};
        DropPoint dp = new DropPoint();
        dp.setImmagine(mockImg);
        dp.setImmagineContentType("image/jpeg");

        when(request.getParameter("dpId")).thenReturn("5");
        when(dropPointService.trovaPerId(5L)).thenReturn(dp);
        when(response.getOutputStream()).thenReturn(outputStream);

        servlet.doGet(request, response);

        verify(response).setContentType("image/jpeg");
        verify(response).setContentLength(mockImg.length);
        verify(outputStream).write(mockImg);
    }

    @Test
    void testDoGet_ContentTypeDefault() throws ServletException, IOException {
        byte[] mockImg = new byte[]{1, 2};
        DropPoint dp = new DropPoint();
        dp.setImmagine(mockImg);
        dp.setImmagineContentType(null); // Forza il ramo default "image/png"

        when(request.getParameter("dpId")).thenReturn("10");
        when(dropPointService.trovaPerId(10L)).thenReturn(dp);
        when(response.getOutputStream()).thenReturn(outputStream);

        servlet.doGet(request, response);

        verify(response).setContentType("image/png");
    }
}