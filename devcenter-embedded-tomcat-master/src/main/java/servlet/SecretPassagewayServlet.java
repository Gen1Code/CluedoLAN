package servlet;

import Database.DBManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(
        name = "SecretPassagewayServlet",
        urlPatterns = {"/passage"}
)
public class SecretPassagewayServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            //Grab Session Attributes
            int AID = (int) req.getSession().getAttribute("AID");
            int GID = (int) req.getSession().getAttribute("GID");

            //[AID,Roll1,Roll2,TID]
            ArrayList<Integer> Turn = dbConn.getCurrentTurnInfoFromGID(GID);

            String GameState = dbConn.getGameState(GID);
            int TID = Turn.get(3);
            int Step = Integer.parseInt(dbConn.getLocation(TID).get(2));

            //If AID is Playing and Only allow if player hasn't moved (Step=0)
            if (Turn.get(0) == AID && Step == 0 && GameState.equals("playing")){
                //If valid Room then is moved
                dbConn.PassageWay(TID,GID);
            }
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
}
