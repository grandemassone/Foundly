package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.bean.Segnalazione;
import model.bean.SegnalazioneAnimale;
import model.bean.SegnalazioneOggetto;
import model.bean.Utente;
import model.bean.enums.CategoriaOggetto;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.StatoSegnalazione;
import model.service.DropPointService;
import model.service.SegnalazioneService;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@WebServlet(name = "CreaSegnalazioneServlet", value = "/crea-segnalazione")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class CreaSegnalazioneServlet extends HttpServlet {

    private final DropPointService dropPointService = new DropPointService();
    private final SegnalazioneService segnalazioneService = new SegnalazioneService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // CARICA LA LISTA DEI DROP-POINT PER LA SELECT
        request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());

        request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect("login");
            return;
        }

        try {
            // 1. Recupero parametri comuni
            String tipo = request.getParameter("tipo"); // OGGETTO o ANIMALE
            String titolo = request.getParameter("titolo");
            String descrizione = request.getParameter("descrizione");
            String dataStr = request.getParameter("dataRitrovamento");
            String luogo = request.getParameter("luogo"); // Via/Piazza
            String citta = request.getParameter("citta");
            String provincia = request.getParameter("provincia");
            String domanda1 = request.getParameter("domanda1");
            String domanda2 = request.getParameter("domanda2");

            // 2. Creazione Oggetto Polimorfico
            Segnalazione segnalazione;

            // ... dentro doPost ...

            if ("OGGETTO".equalsIgnoreCase(tipo)) {
                SegnalazioneOggetto so = new SegnalazioneOggetto();
                String categoriaStr = request.getParameter("categoria");
                so.setCategoria(CategoriaOggetto.valueOf(categoriaStr));

                // NUOVA LOGICA CONSEGNA
                String modalitaStr = request.getParameter("modalita");
                if ("DROP_POINT".equals(modalitaStr)) {
                    so.setModalitaConsegna(ModalitaConsegna.DROP_POINT);
                    String idDpStr = request.getParameter("idDropPoint");
                    if (idDpStr != null && !idDpStr.isEmpty()) {
                        so.setIdDropPoint(Long.parseLong(idDpStr));
                    }
                } else {
                    so.setModalitaConsegna(ModalitaConsegna.DIRETTA);
                    so.setIdDropPoint(null);
                }

                segnalazione = so;
            } else {
                SegnalazioneAnimale sa = new SegnalazioneAnimale();
                sa.setSpecie(request.getParameter("specie"));
                sa.setRazza(request.getParameter("razza"));
                segnalazione = sa;
            }

            // 3. Popolamento Dati Comuni
            segnalazione.setIdUtente(utente.getId());
            segnalazione.setTitolo(titolo);
            segnalazione.setDescrizione(descrizione);
            segnalazione.setLuogoRitrovamento(luogo);
            segnalazione.setCitta(citta);
            segnalazione.setProvincia(provincia);
            segnalazione.setDomandaVerifica1(domanda1);
            segnalazione.setDomandaVerifica2(domanda2);
            segnalazione.setStato(StatoSegnalazione.APERTA);
            segnalazione.setDataPubblicazione(new Timestamp(System.currentTimeMillis()));

            // Conversione Data (da HTML yyyy-mm-dd a Timestamp)
            if (dataStr != null && !dataStr.isEmpty()) {
                LocalDate date = LocalDate.parse(dataStr);
                segnalazione.setDataRitrovamento(Timestamp.valueOf(LocalDateTime.of(date, LocalTime.NOON)));
            }

            // 4. Gestione Immagine (Salvataggio su disco)
            Part filePart = request.getPart("immagine");
            String fileName = "default.png";

            if (filePart != null && filePart.getSize() > 0) {
                // Genera nome unico per evitare sovrascritture
                String originalName = filePart.getSubmittedFileName();
                String ext = originalName.substring(originalName.lastIndexOf("."));
                fileName = UUID.randomUUID().toString() + ext;

                // Percorso di salvataggio (nella cartella upload del server)
                String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdir();

                filePart.write(uploadPath + File.separator + fileName);

                // Salviamo il path relativo nel DB (es. "uploads/foto.jpg")
                segnalazione.setImmagine("uploads/" + fileName);
            } else {
                segnalazione.setImmagine("assets/images/default-item.png");
            }

            // 5. Chiamata al Service (che farà anche il Geocoding)
            boolean successo = segnalazioneService.creaSegnalazione(segnalazione);

            if (successo) {
                response.sendRedirect(request.getContextPath() + "/index?msg=success_segnalazione");
            } else {
                request.setAttribute("errore", "Errore nel salvataggio. Riprova.");
                request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errore", "Errore tecnico: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp").forward(request, response);
        }
    }
}