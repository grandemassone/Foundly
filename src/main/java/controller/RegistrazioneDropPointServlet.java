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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nomeAttivita = request.getParameter("nomeAttivita");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String indirizzo = request.getParameter("indirizzo");
        String citta = request.getParameter("citta");
        String provincia = request.getParameter("provincia");
        String telefono = request.getParameter("telefono");
        String orari = request.getParameter("orari");

        String latStr = request.getParameter("latitudine");
        String lonStr = request.getParameter("longitudine");

        Double latitudine = null;
        Double longitudine = null;

        try {
            if (latStr != null && !latStr.isEmpty()) {
                latitudine = Double.parseDouble(latStr);
            }
            if (lonStr != null && !lonStr.isEmpty()) {
                longitudine = Double.parseDouble(lonStr);
            }
        } catch (NumberFormatException e) {
            // Ignora formato non valido, gestito sotto come errore generale
        }

        // Pattern password come per registrazione utente
        String passwordPattern =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#-])[A-Za-z\\d@$!%*?&._#-]{8,}$";

        if (password == null || !password.matches(passwordPattern)) {
            request.setAttribute("errore",
                    "La password non rispetta i requisiti (usa solo @$!%*?&._#-).");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        // Controllo campi obbligatori base + posizione mappa
        if (email == null || email.isBlank() ||
                nomeAttivita == null || nomeAttivita.isBlank() ||
                latitudine == null || longitudine == null) {

            request.setAttribute("errore",
                    "Campi obbligatori mancanti o posizione mappa non selezionata.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
            return;
        }

        boolean successo = dpService.registraDropPoint(
                nomeAttivita,
                email,
                password,
                indirizzo,
                citta,
                provincia,
                telefono,
                orari,
                latitudine,
                longitudine
        );

        if (successo) {
            // Registrazione Drop-Point OK → vai al login con flag dpOk=1
            response.sendRedirect(request.getContextPath() + "/login?dpOk=1");
        } else {
            // Email già usata per altro Drop-Point
            request.setAttribute("errore", "Email già registrata per un altro Drop-Point.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp")
                    .forward(request, response);
        }
    }
}
