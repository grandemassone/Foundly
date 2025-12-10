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

        // campi testo
        String username = request.getParameter("username");
        String nome     = request.getParameter("nome");
        String cognome  = request.getParameter("cognome");

        utente.setUsername(username);
        utente.setNome(nome);
        utente.setCognome(cognome);

        // flag rimozione immagine
        String removeAvatarParam = request.getParameter("removeAvatar");
        boolean removeAvatar = "true".equalsIgnoreCase(removeAvatarParam);

        // upload immagine profilo (campo "avatar")
        Part avatarPart = null;
        try {
            avatarPart = request.getPart("avatar");
        } catch (IllegalStateException | ServletException e) {
            avatarPart = null; // tieni l'immagine precedente
        }

        boolean hasNewUpload = (avatarPart != null && avatarPart.getSize() > 0);

        if (hasNewUpload) {
            // nuova immagine: carico i byte nel BLOB e salvo il content-type
            try (InputStream is = avatarPart.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                utente.setImmagineProfilo(bytes);
                utente.setImmagineProfiloContentType(avatarPart.getContentType());
            }
            // se carico una nuova immagine, ignoro il flag di rimozione
            removeAvatar = false;

        } else if (removeAvatar) {
            // rimozione esplicita senza nuovo upload
            utente.setImmagineProfilo(null);
            utente.setImmagineProfiloContentType(null);
        }

        // aggiorna profilo (username, nome, cognome, immagine_profilo + content_type)
        utenteService.aggiornaProfilo(utente);

        // aggiorna sessione
        session.setAttribute("utente", utente);

        // redirect al profilo (per evitare il resubmit del POST)
        response.sendRedirect(request.getContextPath() + "/profilo?success=1");
    }
}
