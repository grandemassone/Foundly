package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.DropPointService;
import java.io.IOException;

@WebServlet(name = "RegistrazioneDropPointServlet", value = "/registrazione-droppoint")
public class RegistrazioneDropPointServlet extends HttpServlet {

    private final DropPointService dpService = new DropPointService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Mostra il form per i Drop-Point
        request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nomeAttivita = request.getParameter("nomeAttivita");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String indirizzo = request.getParameter("indirizzo");
        String citta = request.getParameter("citta");
        String provincia = request.getParameter("provincia");
        String telefono = request.getParameter("telefono");
        String orari = request.getParameter("orari");

        if (email == null || password == null || nomeAttivita == null) {
            request.setAttribute("errore", "Campi obbligatori mancanti.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp").forward(request, response);
            return;
        }

        boolean successo = dpService.registraDropPoint(nomeAttivita, email, password, indirizzo, citta, provincia, telefono, orari);

        if (successo) {
            // Manda alla login con un messaggio specifico
            response.sendRedirect("login?registrazione=attesa_approvazione");
        } else {
            request.setAttribute("errore", "Email già registrata per un altro Drop-Point.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp").forward(request, response);
        }
    }
}