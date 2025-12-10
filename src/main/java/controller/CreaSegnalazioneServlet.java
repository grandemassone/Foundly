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
import model.bean.enums.TipoSegnalazione;
import model.service.DropPointService;
import model.service.SegnalazioneService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;

        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
        request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
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

        try {
            // 1) parametri base
            String tipo = request.getParameter("tipo_segnalazione");
            String titolo = request.getParameter("titolo");
            String descrizione = request.getParameter("descrizione");
            String dataStr = request.getParameter("dataRitrovamento");
            String luogo = request.getParameter("luogo_ritrovamento");
            String citta = request.getParameter("citta");
            String provincia = request.getParameter("provincia");
            String domanda1 = request.getParameter("domanda1");
            String domanda2 = request.getParameter("domanda2");

            Segnalazione segnalazione;

            // 2) oggetto polimorfico
            if ("OGGETTO".equalsIgnoreCase(tipo)) {
                SegnalazioneOggetto so = new SegnalazioneOggetto();

                String categoriaStr = request.getParameter("categoria");
                if (categoriaStr != null && !categoriaStr.isEmpty()) {
                    try {
                        so.setCategoria(CategoriaOggetto.valueOf(categoriaStr));
                    } catch (IllegalArgumentException e) {
                        so.setCategoria(CategoriaOggetto.ALTRO);
                    }
                } else {
                    so.setCategoria(CategoriaOggetto.ALTRO);
                }

                String modalitaStr = request.getParameter("modalita_consegna");
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

                so.setTipoSegnalazione(TipoSegnalazione.OGGETTO);
                segnalazione = so;

            } else {
                SegnalazioneAnimale sa = new SegnalazioneAnimale();
                sa.setSpecie(request.getParameter("specie"));
                sa.setRazza(request.getParameter("razza"));
                sa.setTipoSegnalazione(TipoSegnalazione.ANIMALE);
                segnalazione = sa;
            }

            // 3) dati comuni
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

            if (dataStr != null && !dataStr.isEmpty()) {
                if (dataStr.contains("T")) {
                    LocalDateTime dt = LocalDateTime.parse(dataStr);
                    segnalazione.setDataRitrovamento(Timestamp.valueOf(dt));
                } else {
                    LocalDate date = LocalDate.parse(dataStr);
                    segnalazione.setDataRitrovamento(
                            Timestamp.valueOf(LocalDateTime.of(date, LocalTime.NOON)));
                }
            }

            // 4) immagine BLOB
            Part filePart = null;
            try {
                filePart = request.getPart("immagine");
            } catch (IllegalStateException | ServletException e) {
                // niente upload → lascio immagine null
            }

            if (filePart != null && filePart.getSize() > 0) {
                try (InputStream is = filePart.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    segnalazione.setImmagine(bytes);
                    segnalazione.setImmagineContentType(filePart.getContentType());
                }
            } else {
                segnalazione.setImmagine(null);
                segnalazione.setImmagineContentType(null);
            }

            // 5) salvataggio
            boolean successo = segnalazioneService.creaSegnalazione(segnalazione);

            if (successo) {
                response.sendRedirect(request.getContextPath() + "/index?msg=success_segnalazione");
            } else {
                request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                request.setAttribute("errore", "Errore nel salvataggio. Controlla i dati inseriti.");
                request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                        .forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
            request.setAttribute("errore", "Errore tecnico: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                    .forward(request, response);
        }
    }
}
