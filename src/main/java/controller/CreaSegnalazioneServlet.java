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

        // Carica i drop point per la select
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
            // 1. RECUPERO PARAMETRI (Nomi allineati alla JSP moderna)
            String tipo = request.getParameter("tipo_segnalazione"); // Era "tipo"
            String titolo = request.getParameter("titolo");
            String descrizione = request.getParameter("descrizione");
            String dataStr = request.getParameter("dataRitrovamento");
            String luogo = request.getParameter("luogo_ritrovamento"); // Era "luogo"
            String citta = request.getParameter("citta");
            String provincia = request.getParameter("provincia");
            String domanda1 = request.getParameter("domanda1"); // Nella JSP è name="domanda1"
            String domanda2 = request.getParameter("domanda2"); // Nella JSP è name="domanda2"

            Segnalazione segnalazione;

            // 2. Creazione Oggetto Polimorfico
            if ("OGGETTO".equalsIgnoreCase(tipo)) {
                SegnalazioneOggetto so = new SegnalazioneOggetto();

                String categoriaStr = request.getParameter("categoria");
                // Controllo per evitare errori se la categoria è vuota o non valida
                if (categoriaStr != null && !categoriaStr.isEmpty()) {
                    try {
                        so.setCategoria(CategoriaOggetto.valueOf(categoriaStr));
                    } catch (IllegalArgumentException e) {
                        // Se la categoria non esiste (es. BORSE), fallback su ALTRO
                        so.setCategoria(CategoriaOggetto.ALTRO);
                    }
                } else {
                    so.setCategoria(CategoriaOggetto.ALTRO);
                }

                // Recupero modalità di consegna
                String modalitaStr = request.getParameter("modalita_consegna"); // Era "modalita"

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
                // Caso ANIMALE
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

            // Gestione Data (Supporta sia 'date' che 'datetime-local')
            if (dataStr != null && !dataStr.isEmpty()) {
                if (dataStr.contains("T")) {
                    // Formato datetime-local (yyyy-MM-ddTHH:mm)
                    LocalDateTime dt = LocalDateTime.parse(dataStr);
                    segnalazione.setDataRitrovamento(Timestamp.valueOf(dt));
                } else {
                    // Formato date (yyyy-MM-dd)
                    LocalDate date = LocalDate.parse(dataStr);
                    segnalazione.setDataRitrovamento(Timestamp.valueOf(LocalDateTime.of(date, LocalTime.NOON)));
                }
            }

            // 4. Gestione Immagine
            Part filePart = request.getPart("immagine");
            if (filePart != null && filePart.getSize() > 0) {
                String originalName = filePart.getSubmittedFileName();
                String ext = ".jpg";
                if(originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String fileName = UUID.randomUUID().toString() + ext;

                String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdir();

                filePart.write(uploadPath + File.separator + fileName);
                segnalazione.setImmagine("uploads/" + fileName);
            } else {
                segnalazione.setImmagine("assets/images/default-item.png");
            }

            // 5. Salvataggio
            boolean successo = segnalazioneService.creaSegnalazione(segnalazione);

            if (successo) {
                response.sendRedirect(request.getContextPath() + "/index?msg=success_segnalazione");
            } else {
                request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                request.setAttribute("errore", "Errore nel salvataggio. Controlla i dati inseriti.");
                request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
            request.setAttribute("errore", "Errore tecnico: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp").forward(request, response);
        }
    }
}