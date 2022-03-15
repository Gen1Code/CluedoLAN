package servlet;

import Database.DBManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(
        name = "WaitingRoomUpdateServlet",
        urlPatterns = {"/WaitingRoomUpdateServlet"}
)
public class WaitingRoomUpdateServlet extends HttpServlet {
    DBManager testObject = new DBManager("Cluedo.db");
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            int GID = (int) req.getSession().getAttribute("GID");

            //Get Data
            ArrayList<ArrayList> data = testObject.getJSONGameInfo(GID);

            boolean isGameFull = testObject.isGameFull(GID);

            if(isGameFull){
                //Send to Waiting Room using flag
                resp.getWriter().write("full");
            }else{
                //Send Data
                resp.getWriter().write(String.valueOf(data));
            }
        } catch (SQLException | NullPointerException ignore) {//If request doesn't have a GID ignore it

        }
    }
}
