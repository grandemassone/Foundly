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

@WebServlet(name = "IndexServlet", urlPatterns = {"/index"})
public class IndexServlet extends HttpServlet {

    private final SegnalazioneService segnalazioneService = new SegnalazioneService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Recupera la lista dal DB
        List<Segnalazione> lista = segnalazioneService.getUltimeSegnalazioni();

        // 2. Passa la lista alla JSP
        request.setAttribute("segnalazioni", lista);

        request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);
    }
}