package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.Utente;
import model.service.UtenteService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@WebServlet(name = "AvatarServlet", value = "/avatar")
public class AvatarServlet extends HttpServlet {

    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        String idParam = request.getParameter("userId");
        long userId;

        try {
            userId = Long.parseLong(idParam);
        } catch (Exception e) {
            sendDefaultAvatar(response);
            return;
        }

        Utente u = utenteService.trovaPerId(userId);
        if (u == null || u.getImmagineProfilo() == null || u.getImmagineProfilo().length == 0) {
            sendDefaultAvatar(response);
            return;
        }

        String contentType = u.getImmagineProfiloContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "image/jpeg";
        }

        byte[] data = u.getImmagineProfilo();

        // (opzionale) disabilita la cache se vuoi sempre l'ultima versione
        // response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");

        response.setContentType(contentType);
        response.setContentLength(data.length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(data);
        }
    }

    private void sendDefaultAvatar(HttpServletResponse response) throws IOException {
        // avatar di default: metti un file in src/main/webapp/assets/images/default-avatar.png
        try (InputStream is = getServletContext()
                .getResourceAsStream("/assets/images/default-avatar.png")) {

            if (is == null) {
                // nessun default: non mando contenuto
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            byte[] buffer = is.readAllBytes();

            response.setContentType("image/png");
            response.setContentLength(buffer.length);

            try (OutputStream os = response.getOutputStream()) {
                os.write(buffer);
            }
        }
    }
}
