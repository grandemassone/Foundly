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
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        // Se non arrivo da una SearchServlet che ha già settato "segnalazioni"
        if (request.getAttribute("segnalazioni") == null) {
            request.setAttribute("segnalazioni", service.getUltimeSegnalazioni());
        }

        // NOTA: ora punta a home.jsp
        request.getRequestDispatcher("/WEB-INF/jsp/home.jsp")
                .forward(request, response);
    }
}
