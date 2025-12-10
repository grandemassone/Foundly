package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.Segnalazione;
import model.service.SegnalazioneService;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet(name = "SegnalazioneImgServlet", value = "/segnalazione-img")
public class SegnalazioneImgServlet extends HttpServlet {

    private final SegnalazioneService segnalazioneService = new SegnalazioneService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        Segnalazione s = segnalazioneService.trovaPerId(id);
        if (s == null || s.getImmagine() == null || s.getImmagine().length == 0) {
            // nessuna immagine: puoi mandare 404 o un placeholder
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = s.getImmagineContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "image/jpeg";
        }

        response.setContentType(contentType);
        response.setContentLength(s.getImmagine().length);

        try (OutputStream out = response.getOutputStream()) {
            out.write(s.getImmagine());
        }
    }
}
