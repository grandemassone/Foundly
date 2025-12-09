package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.Utente;
import model.service.UtenteService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ClassificaServlet", value = "/classifica")
public class ClassificaServlet extends HttpServlet {

    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. Recupera la lista completa degli utenti ordinata per punteggio decrescente
        // Assicurati che nel tuo Service/DAO esista questo metodo
        List<Utente> classificaCompleta = utenteService.getClassificaUtenti();

        // 2. Opzionale: Se volessi passare solo i top 3 separatamente (ma la JSP attuale usa la lista completa)
        // List<Utente> top3 = classificaCompleta.stream().limit(3).toList();
        // request.setAttribute("top3", top3);

        // 3. Passa la lista completa alla JSP
        request.setAttribute("classifica", classificaCompleta);

        // 4. Mostra la pagina
        request.getRequestDispatcher("/WEB-INF/jsp/classifica.jsp").forward(request, response);
    }
}