package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.bean.DropPoint;
import model.service.DropPointService;

import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "ProfiloDropPointServlet", value = "/profilo-drop-point")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class ProfiloDropPointServlet extends HttpServlet {

    private final DropPointService dropPointService = new DropPointService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        DropPoint dp = (session != null) ? (DropPoint) session.getAttribute("dropPoint") : null;

        if (dp == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        DropPoint fresh = dropPointService.trovaPerId(dp.getId());
        if (fresh != null) {
            session.setAttribute("dropPoint", fresh);
            dp = fresh;
        }

        request.setAttribute("dropPoint", dp);
        request.getRequestDispatcher("/WEB-INF/jsp/profilo_drop_point.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        DropPoint dp = (session != null) ? (DropPoint) session.getAttribute("dropPoint") : null;

        if (dp == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        // ==========================
        // DELETE ACCOUNT
        // ==========================
        if ("delete_account".equals(action)) {
            boolean deleted = dropPointService.eliminaDropPoint(dp.getId());
            if (deleted) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login?dpDeleted=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/profilo-drop-point?error=delete_failed");
            }
            return;
        }

        // ==========================
        // UPDATE PROFILE
        // ==========================
        String nomeAttivita  = request.getParameter("nomeAttivita");
        String indirizzo     = request.getParameter("indirizzo");
        String citta         = request.getParameter("citta");
        String provincia     = request.getParameter("provincia");
        String telefono      = request.getParameter("telefono");
        String orariApertura = request.getParameter("orariApertura");

        // evita "null" / azzeramenti
        if (nomeAttivita != null)  dp.setNomeAttivita(nomeAttivita.trim());
        if (indirizzo != null)     dp.setIndirizzo(indirizzo.trim());
        if (citta != null)         dp.setCitta(citta.trim());
        if (provincia != null)     dp.setProvincia(provincia.trim());
        if (telefono != null)      dp.setTelefono(telefono.trim());
        if (orariApertura != null) dp.setOrariApertura(orariApertura.trim());

        boolean removeLogo = "true".equalsIgnoreCase(request.getParameter("removeLogo"));

        Part logoPart = null;
        try {
            logoPart = request.getPart("logo");
        } catch (Exception ignored) {}

        boolean hasNewUpload = (logoPart != null && logoPart.getSize() > 0);

        if (hasNewUpload) {
            try (InputStream is = logoPart.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                dp.setImmagine(bytes);
                dp.setImmagineContentType(logoPart.getContentType());
            }
            removeLogo = false;
        } else if (removeLogo) {
            dp.setImmagine(null);
            dp.setImmagineContentType(null);
        }

        boolean ok = dropPointService.aggiornaProfilo(dp);
        if (!ok) {
            response.sendRedirect(request.getContextPath() + "/profilo-drop-point?error=update_failed");
            return;
        }

        DropPoint updated = dropPointService.trovaPerId(dp.getId());
        session.setAttribute("dropPoint", (updated != null ? updated : dp));

        response.sendRedirect(request.getContextPath() + "/profilo-drop-point?success=1");
    }
}
