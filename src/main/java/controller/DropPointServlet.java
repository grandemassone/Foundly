package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bean.DropPoint;
import model.service.DropPointService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DropPointServlet", value = "/drop-point")
public class DropPointServlet extends HttpServlet {

    private final DropPointService dropPointService = new DropPointService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        // Solo Drop-Point APPROVATI
        List<DropPoint> approvati = dropPointService.findAllApprovati();
        request.setAttribute("dropPoints", approvati);

        request.getRequestDispatcher("/WEB-INF/jsp/drop_point.jsp")
                .forward(request, response);
    }
}
