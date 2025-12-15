package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.bean.Utente;
import model.service.UtenteService;

import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "ProfiloServlet", value = "/profilo")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,        // 5 MB
        maxRequestSize = 10 * 1024 * 1024     // 10 MB
)
public class ProfiloServlet extends HttpServlet {

    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // mantiene il badge coerente con il punteggio
        utenteService.aggiornaPunteggioEBadge(utente, 0);
        session.setAttribute("utente", utente);

        request.getRequestDispatcher("/WEB-INF/jsp/profilo.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        // ==========================
        // DELETE ACCOUNT
        // ==========================
        if ("delete_account".equals(action)) {
            boolean deleted = utenteService.cancellaUtente(utente.getId());
            if (deleted) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login?deleted=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/profilo?error=delete_failed");
            }
            return;
        }

        // ==========================
        // UPDATE PROFILE
        // ==========================
        // (consigliato: controlla action update_profile, ma puoi anche farne a meno)
        // if (!"update_profile".equals(action)) { response.sendRedirect(...); return; }

        // campi testo (non sovrascrivere con null)
        String username = request.getParameter("username");
        String nome     = request.getParameter("nome");
        String cognome  = request.getParameter("cognome");

        if (username != null) utente.setUsername(username.trim());
        if (nome != null)     utente.setNome(nome.trim());
        if (cognome != null)  utente.setCognome(cognome.trim());

        // flag rimozione immagine
        boolean removeAvatar = "true".equalsIgnoreCase(request.getParameter("removeAvatar"));

        // upload immagine profilo (campo "avatar")
        Part avatarPart = null;
        try {
            avatarPart = request.getPart("avatar");
        } catch (IllegalStateException | ServletException e) {
            avatarPart = null;
        }

        boolean hasNewUpload = (avatarPart != null && avatarPart.getSize() > 0);

        if (hasNewUpload) {
            try (InputStream is = avatarPart.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                utente.setImmagineProfilo(bytes);
                utente.setImmagineProfiloContentType(avatarPart.getContentType());
            }
            removeAvatar = false;
        } else if (removeAvatar) {
            utente.setImmagineProfilo(null);
            utente.setImmagineProfiloContentType(null);
        }

        boolean ok = utenteService.aggiornaProfilo(utente);
        if (!ok) {
            response.sendRedirect(request.getContextPath() + "/profilo?error=update_failed");
            return;
        }

        session.setAttribute("utente", utente);
        response.sendRedirect(request.getContextPath() + "/profilo?success=1");
    }
}
