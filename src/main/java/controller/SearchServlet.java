package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.Segnalazione;
import model.service.SegnalazioneService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SearchServlet", value = "/search")
public class SearchServlet extends HttpServlet {
    private final SegnalazioneService service = new SegnalazioneService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String q = request.getParameter("q");
        String tipo = request.getParameter("tipo");
        String categoria = request.getParameter("categoria");

        List<Segnalazione> risultati = service.cercaSegnalazioni(q, tipo, categoria);

        // Salvo i risultati nella request
        request.setAttribute("segnalazioni", risultati);

        // Manteniamo i filtri selezionati (per UX)
        request.setAttribute("paramQ", q);
        request.setAttribute("paramTipo", tipo);
        request.setAttribute("paramCat", categoria);

        // MODIFICA QUI: Inoltro alla Servlet "Index" invece che alla JSP diretta
        request.getRequestDispatcher("/index").forward(request, response);
    }
}