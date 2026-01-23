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
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class CreaSegnalazioneServlet extends HttpServlet {

    private final DropPointService dropPointService = new DropPointService();
    private final SegnalazioneService segnalazioneService = new SegnalazioneService();

    private static final LocalDate MIN_DATE = LocalDate.of(2000, 1, 1);
    private static final String PROVINCIA_PATTERN = "^[A-Za-z]{2}$";

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
            String tipo = trimOrNull(request.getParameter("tipo_segnalazione"));
            String titolo = trimOrNull(request.getParameter("titolo"));
            String descrizione = trimOrNull(request.getParameter("descrizione"));
            String dataStr = trimOrNull(request.getParameter("dataRitrovamento"));
            String luogo = trimOrNull(request.getParameter("luogo_ritrovamento"));
            String citta = trimOrNull(request.getParameter("citta"));
            String provincia = trimOrNull(request.getParameter("provincia"));
            String domanda1 = trimOrNull(request.getParameter("domanda1"));
            String domanda2 = trimOrNull(request.getParameter("domanda2"));

            // Controllo campi obbligatori base
            if (isBlank(tipo) || isBlank(titolo) || isBlank(descrizione) ||
                    isBlank(luogo) || isBlank(citta) || isBlank(provincia) ||
                    isBlank(domanda1) || isBlank(domanda2) || isBlank(dataStr)) {

                request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                request.setAttribute("errore", "Tutti i campi obbligatori devono essere compilati.");
                request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                        .forward(request, response);
                return;
            }

            // Lunghezze massime indicative (allineale al DB se necessario)
            if (titolo.length() > 100 || luogo.length() > 100 ||
                    citta.length() > 50 || domanda1.length() > 200 ||
                    domanda2.length() > 200 || descrizione.length() > 2000) {

                request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                request.setAttribute("errore", "Uno o più campi superano la lunghezza massima consentita.");
                request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                        .forward(request, response);
                return;
            }

            // Provincia: due lettere
            if (!provincia.matches(PROVINCIA_PATTERN)) {
                request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                request.setAttribute("errore", "La provincia deve essere indicata con due lettere (es. MI).");
                request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                        .forward(request, response);
                return;
            }

            // Validazione data: non nel futuro, non prima del 01/01/2000
            LocalDate checkDate;
            try {
                if (dataStr.contains("T")) {
                    checkDate = LocalDateTime.parse(dataStr).toLocalDate();
                } else {
                    checkDate = LocalDate.parse(dataStr);
                }
            } catch (Exception ex) {
                request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                request.setAttribute("errore", "Formato data non valido.");
                request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                        .forward(request, response);
                return;
            }

            LocalDate today = LocalDate.now();
            if (checkDate.isBefore(MIN_DATE) || checkDate.isAfter(today)) {
                request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                request.setAttribute("errore",
                        "La data deve essere compresa tra il 01/01/2000 e la data odierna.");
                request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                        .forward(request, response);
                return;
            }

            Segnalazione segnalazione;

            if ("OGGETTO".equalsIgnoreCase(tipo)) {
                SegnalazioneOggetto so = new SegnalazioneOggetto();

                String categoriaStr = trimOrNull(request.getParameter("categoria"));
                if (categoriaStr != null && !categoriaStr.isEmpty()) {
                    try {
                        so.setCategoria(CategoriaOggetto.valueOf(categoriaStr));
                    } catch (IllegalArgumentException e) {
                        so.setCategoria(CategoriaOggetto.ALTRO);
                    }
                } else {
                    so.setCategoria(CategoriaOggetto.ALTRO);
                }

                String modalitaStr = trimOrNull(request.getParameter("modalita_consegna"));
                if ("DROP_POINT".equals(modalitaStr)) {
                    so.setModalitaConsegna(ModalitaConsegna.DROP_POINT);
                    String idDpStr = trimOrNull(request.getParameter("idDropPoint"));

                    if (isBlank(idDpStr)) {
                        request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                        request.setAttribute("errore",
                                "Se scegli la modalità Drop-Point devi selezionare un negozio.");
                        request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                                .forward(request, response);
                        return;
                    }

                    try {
                        so.setIdDropPoint(Long.parseLong(idDpStr));
                    } catch (NumberFormatException ex) {
                        request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                        request.setAttribute("errore", "Drop-Point selezionato non valido.");
                        request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                                .forward(request, response);
                        return;
                    }

                } else {
                    so.setModalitaConsegna(ModalitaConsegna.DIRETTA);
                    so.setIdDropPoint(null);
                }

                so.setTipoSegnalazione(TipoSegnalazione.OGGETTO);
                segnalazione = so;

            } else {
                // Tipo ANIMALE
                SegnalazioneAnimale sa = new SegnalazioneAnimale();
                String specie = trimOrNull(request.getParameter("specie"));
                String razza = trimOrNull(request.getParameter("razza"));

                if (isBlank(specie)) {
                    request.setAttribute("listaDropPoint", dropPointService.findAllApprovati());
                    request.setAttribute("errore", "Per una segnalazione di animale la specie è obbligatoria.");
                    request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                            .forward(request, response);
                    return;
                }

                sa.setSpecie(specie);
                sa.setRazza(razza);
                sa.setTipoSegnalazione(TipoSegnalazione.ANIMALE);
                segnalazione = sa;
            }

            segnalazione.setIdUtente(utente.getId());
            segnalazione.setTitolo(titolo);
            segnalazione.setDescrizione(descrizione);
            segnalazione.setLuogoRitrovamento(luogo);
            segnalazione.setCitta(citta);
            segnalazione.setProvincia(provincia.toUpperCase());
            segnalazione.setDomandaVerifica1(domanda1);
            segnalazione.setDomandaVerifica2(domanda2);
            segnalazione.setStato(StatoSegnalazione.APERTA);
            segnalazione.setDataPubblicazione(new Timestamp(System.currentTimeMillis()));

            // Imposta data ritrovamento (mezzogiorno per evitare problemi di timezone)
            segnalazione.setDataRitrovamento(
                    Timestamp.valueOf(LocalDateTime.of(checkDate, LocalTime.NOON)));

            // Gestione immagine (opzionale)
            Part filePart = null;
            try {
                filePart = request.getPart("immagine");
            } catch (IllegalStateException | ServletException e) {
                // ignoriamo, verrà gestito sotto se necessario
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

            boolean successo = segnalazioneService.creaSegnalazione(segnalazione);

            if (successo) {
                // redirect semplice, gestisci il messaggio lato JSP
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
            request.setAttribute("errore", "L'immagine inserita è troppo grande (max 10Mb) o i dati non sono validi.");
            request.getRequestDispatcher("/WEB-INF/jsp/crea_segnalazione.jsp")
                    .forward(request, response);
        }
    }

    private static String trimOrNull(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
