package servlet;

import Database.DBManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(
        name = "LogoutServlet",
        urlPatterns = {"/LogoutServlet"}
)
public class LogoutServlet extends HttpServlet {
    DBManager dbCon = new DBManager("Cluedo.db");
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            //Get rid of Session ID stored in database (clean, can be removed)
            dbCon.setSessionID((int) req.getSession().getAttribute("AID"),"");

            //Invalidate users session and redirect them to login page
            req.getSession().setAttribute("AID",null);
            req.getSession().setAttribute("GID",null);
            req.getSession().invalidate();
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        } //If user isn't logged in then ignore
        resp.sendRedirect("index.jsp");

    }
}
