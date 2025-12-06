package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Questa annotazione collega l'URL "/recupero-password" a questa classe Java
@WebServlet(name = "RecuperoPasswordServlet", value = "/recupero-password")
public class RecuperoPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Quando clicchi il link, il server esegue questo metodo
        // e ti "gira" (forward) alla pagina JSP che è nascosta in WEB-INF
        request.getRequestDispatcher("/WEB-INF/jsp/recupero_password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Questo metodo servirà quando l'utente compilerà il form e cliccherà "Invia"
        String email = request.getParameter("email");

        // TODO: Qui in futuro chiamerai il Service per inviare l'email vera

        // Per ora simuliamo il successo visivo
        request.setAttribute("messaggio", "Se l'email esiste nel sistema, riceverai un link di recupero a breve.");
        request.getRequestDispatcher("/WEB-INF/jsp/recupero_password.jsp").forward(request, response);
    }
}