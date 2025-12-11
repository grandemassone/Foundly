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

        // ricarico dal DB per avere i dati aggiornati
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

        HttpSession session = request.getSession(false);
        DropPoint dp = (session != null) ? (DropPoint) session.getAttribute("dropPoint") : null;

        if (dp == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Parametri testo (NON tocco l'email qui, altrimenti la azzeri)
        String nomeAttivita   = request.getParameter("nomeAttivita");
        String indirizzo      = request.getParameter("indirizzo");
        String citta          = request.getParameter("citta");
        String provincia      = request.getParameter("provincia");
        String telefono       = request.getParameter("telefono");
        String orariApertura  = request.getParameter("orariApertura");
        String removeLogo     = request.getParameter("removeLogo");

        dp.setNomeAttivita(nomeAttivita);
        dp.setIndirizzo(indirizzo);
        dp.setCitta(citta);
        dp.setProvincia(provincia);
        dp.setTelefono(telefono);
        dp.setOrariApertura(orariApertura);

        // Gestione logo (BLOB)
        Part logoPart = null;
        try {
            logoPart = request.getPart("logo");
        } catch (Exception ignored) { }

        if ("true".equalsIgnoreCase(removeLogo)) {
            dp.setImmagine(null);
            dp.setImmagineContentType(null);
        } else if (logoPart != null && logoPart.getSize() > 0) {
            try (InputStream is = logoPart.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                dp.setImmagine(bytes);
                dp.setImmagineContentType(logoPart.getContentType());
            }
        }
        // Se non carico nulla e non chiedo rimozione, l'immagine rimane quella attuale.

        boolean ok = dropPointService.aggiornaProfilo(dp);

        if (ok) {
            // ricarico dal DB e aggiorno sessione
            DropPoint updated = dropPointService.trovaPerId(dp.getId());
            if (updated != null) {
                session.setAttribute("dropPoint", updated);
            } else {
                session.setAttribute("dropPoint", dp);
            }
            response.sendRedirect(request.getContextPath() + "/profilo-drop-point?success=1");
        } else {
            request.setAttribute("dropPoint", dp);
            request.setAttribute("errore", "Errore nel salvataggio del profilo Drop-Point.");
            request.getRequestDispatcher("/WEB-INF/jsp/profilo_drop_point.jsp")
                    .forward(request, response);
        }
    }
}
