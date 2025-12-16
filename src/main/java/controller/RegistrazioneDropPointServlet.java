package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.DropPointService;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "RegistrazioneDropPointServlet", value = "/registrazione-droppoint")
public class RegistrazioneDropPointServlet extends HttpServlet {

    private final DropPointService dpService = new DropPointService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

        String latStr = request.getParameter("latitudine");
        String lonStr = request.getParameter("longitudine");

        Double latitudine = null;
        Double longitudine = null;

        try {
            if (latStr != null && !latStr.isEmpty()) latitudine = Double.parseDouble(latStr);
            if (lonStr != null && !lonStr.isEmpty()) longitudine = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            // Ignora
        }

        if (email == null || password == null || nomeAttivita == null || latitudine == null || longitudine == null) {
            request.setAttribute("errore", "Campi obbligatori mancanti o posizione mappa non selezionata.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp").forward(request, response);
            return;
        }

        // CORRETTO: Ordine parametri allineato col Service aggiornato (Lat, Lon)
        boolean successo = dpService.registraDropPoint(nomeAttivita, email, password, indirizzo, citta, provincia, telefono, orari, latitudine, longitudine);

        if (successo) {
            // --- INIZIO LOGICA ALERT SEMPLICE ---
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html><body>");
            out.println("<script type='text/javascript'>");

            // Qui scriviamo l'alert del browser
            out.println("alert('Registrazione effettuata con successo! Ora puoi effettuare il login.');");

            // Qui reindirizziamo l'utente alla pagina di login dopo che clicca OK
            // Nota: Usa request.getContextPath() per essere sicuro del percorso

            out.println("</script>");
            out.println("</body></html>");
            // --- FINE LOGICA ALERT SEMPLICE --
            response.sendRedirect("login?registrazione=attesa_approvazione");
        } else {
            // --- INIZIO LOGICA ALERT SEMPLICE ---
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html><body>");
            out.println("<script type='text/javascript'>");

            // Qui scriviamo l'alert del browser
            out.println("alert('Email già registrata per un altro Drop-Point.');");

            // Qui reindirizziamo l'utente alla pagina di login dopo che clicca OK
            // Nota: Usa request.getContextPath() per essere sicuro del percorso
            out.println("window.location.href = '" + request.getContextPath() + "/login';");

            out.println("</script>");
            out.println("</body></html>");
            // --- FINE LOGICA ALERT SEMPLICE --
            request.setAttribute("errore", "Email già registrata per un altro Drop-Point.");
            request.getRequestDispatcher("/WEB-INF/jsp/registrazione_droppoint.jsp").forward(request, response);
        }
    }
}