package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
        name = "LogoutServlet",
        urlPatterns = {"/LogoutServlet"}
)
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("Goodbye");
        req.getSession().setAttribute("name","");
        req.getSession().setAttribute("RoomID","");
        resp.sendRedirect("logout.jsp");
    }
}
