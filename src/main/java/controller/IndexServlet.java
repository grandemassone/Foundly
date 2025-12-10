package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.SegnalazioneService;

import java.io.IOException;

@WebServlet(name = "IndexServlet", value = "/index")
public class IndexServlet extends HttpServlet {

    private final SegnalazioneService service = new SegnalazioneService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // CONTROLLO FONDAMENTALE:
        // Se l'attributo "segnalazioni" è GIA' presente, significa che arrivo dalla SearchServlet.
        // In quel caso NON devo sovrascriverlo con le "ultime segnalazioni".
        if (request.getAttribute("segnalazioni") == null) {
            // Sono in un caricamento normale della Home (nessuna ricerca attiva)
            request.setAttribute("segnalazioni", service.getUltimeSegnalazioni());
        }

        // Infine, delego la visualizzazione alla JSP
        request.getRequestDispatcher("WEB-INF/jsp/index.jsp").forward(request, response);
    }
}