package servlet;

import Database.DBManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@WebServlet(
        name = "SkipServlet",
        urlPatterns = {"/skip"}
)
public class SkipServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            //Grab Session Attributes
            int AID = (int) req.getSession().getAttribute("AID");
            int GID = (int) req.getSession().getAttribute("GID");

            String GameState = dbConn.getGameState(GID);
            //[AID,...]
            ArrayList<Integer> Turn = dbConn.getCurrentTurnInfoFromGID(GID);
            if(Turn.get(0) == AID && GameState.equals("playing")){
                //Skip Turn
                dbConn.createNewTurn(AID,GID);
            }
        }catch (Exception ignore){}
    }
}
