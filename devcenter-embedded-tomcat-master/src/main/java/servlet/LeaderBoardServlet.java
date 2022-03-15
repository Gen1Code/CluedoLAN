package servlet;

import Database.DBManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(
        name = "LeaderBoardServlet",
        urlPatterns = {"/LeaderBoardServlet"}
)
public class LeaderBoardServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            //Get Leader Board Data
            ArrayList<ArrayList> data = dbConn.getLeaderBoard(1);

            //Send Data
            resp.getWriter().write(String.valueOf(data));
        } catch (SQLException ignore) {}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            //Grab Post Data
            BufferedReader reader = req.getReader();
            int start = Integer.parseInt(reader.readLine().trim());

            //Get Leader Board Data
            ArrayList<ArrayList> data = dbConn.getLeaderBoard(start);

            //Send Data
            resp.getWriter().write(String.valueOf(data));
        } catch (SQLException | NumberFormatException ignore) {}
    }
}
