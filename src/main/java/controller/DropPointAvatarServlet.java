package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.DropPoint;
import model.service.DropPointService;

import java.io.IOException;

@WebServlet(name = "DropPointAvatarServlet", value = "/drop-point-avatar")
public class DropPointAvatarServlet extends HttpServlet {

    private final DropPointService dropPointService = new DropPointService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        String idParam = request.getParameter("dpId");
        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        DropPoint dp = dropPointService.trovaPerId(id);
        if (dp == null || dp.getImmagine() == null || dp.getImmagine().length == 0) {
            // nessun logo salvato -> 404 (oppure qui puoi servire un png di default)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = dp.getImmagineContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "image/png";
        }

        byte[] img = dp.getImmagine();
        response.setContentType(contentType);
        response.setContentLength(img.length);
        response.getOutputStream().write(img);
    }
}
