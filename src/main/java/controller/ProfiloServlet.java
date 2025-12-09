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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

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

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // campi testo
        String username = request.getParameter("username");
        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");

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
            // se qualcosa va storto sull'upload, ignoro e tengo immagine precedente
        }

        boolean hasNewUpload = (avatarPart != null && avatarPart.getSize() > 0);

        if (hasNewUpload) {
            // elimina il vecchio file se esiste
            String oldPath = utente.getImmagineProfilo();
            if (oldPath != null && !oldPath.trim().isEmpty()) {
                String oldRealPath = getServletContext().getRealPath("/" + oldPath);
                File oldFile = new File(oldRealPath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            // nome file originale
            String submittedFileName = Paths.get(avatarPart.getSubmittedFileName())
                    .getFileName().toString();

            // estensione (es. .jpg, .png)
            String extension = "";
            int dotIndex = submittedFileName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = submittedFileName.substring(dotIndex);
            }

            // nuovo nome file unico
            String newFileName = "avatar_" + utente.getId() + "_" + System.currentTimeMillis() + extension;

            // cartella di upload (es. .../webapp/uploads/avatars)
            String uploadPath = getServletContext().getRealPath("/uploads/avatars");
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File targetFile = new File(uploadDir, newFileName);
            avatarPart.write(targetFile.getAbsolutePath());

            // path relativo da salvare nel DB
            String relativePath = "uploads/avatars/" + newFileName;
            utente.setImmagineProfilo(relativePath);

        } else if (removeAvatar) {
            // rimozione esplicita senza nuovo upload
            String oldPath = utente.getImmagineProfilo();
            if (oldPath != null && !oldPath.trim().isEmpty()) {
                String oldRealPath = getServletContext().getRealPath("/" + oldPath);
                File oldFile = new File(oldRealPath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            utente.setImmagineProfilo(null);
        }

        // aggiorna profilo (username, nome, cognome, immagine_profilo)
        utenteService.aggiornaProfilo(utente);

        // aggiorna sessione
        session.setAttribute("utente", utente);

        // redirect al profilo (per evitare il resubmit del POST)
        response.sendRedirect(request.getContextPath() + "/profilo?success=1");
    }
}