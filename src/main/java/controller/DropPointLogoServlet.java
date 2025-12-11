package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.DropPoint;
import model.service.DropPointService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@WebServlet(name = "DropPointLogoServlet", value = "/drop-point-logo")
public class DropPointLogoServlet extends HttpServlet {

    private final DropPointService dropPointService = new DropPointService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        DropPoint dp = dropPointService.trovaPerId(id);
        if (dp == null) {
            sendPlaceholder(response, request);
            return;
        }

        byte[] logo = dp.getImmagine();
        String contentType = dp.getImmagineContentType();

        if (logo == null || logo.length == 0) {
            sendPlaceholder(response, request);
            return;
        }

        if (contentType == null || contentType.isBlank()) {
            contentType = "image/jpeg";
        }

        response.setContentType(contentType);
        response.setContentLength(logo.length);

        try (OutputStream out = response.getOutputStream()) {
            out.write(logo);
        }
    }

    private void sendPlaceholder(HttpServletResponse response,
                                 HttpServletRequest request) throws IOException {
        // Cambia il path se hai un altro placeholder
        String path = "/assets/images/drop_point_placeholder.png";

        try (InputStream is = request.getServletContext().getResourceAsStream(path)) {
            if (is == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("image/png");
            byte[] buffer = is.readAllBytes();
            response.setContentLength(buffer.length);
            try (OutputStream out = response.getOutputStream()) {
                out.write(buffer);
            }
        }
    }
}
