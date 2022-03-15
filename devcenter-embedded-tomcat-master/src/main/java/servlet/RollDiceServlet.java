package servlet;

import Database.DBManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

@WebServlet(
        name = "RollDiceServlet",
        urlPatterns = {"/roll"}
)
public class RollDiceServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");
    Random rnd = new Random();
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

            //If Turn hasn't rolled their dice yet and Requester is Current Person Playing
            //Only allow if player hasn't moved (Step=0)
            if(Turn.get(0)==AID && Turn.get(1)==0 && Step==0 && GameState.equals("playing")){
                //Generate Rolls
                int Roll1 = rnd.nextInt(6) +1;
                int Roll2 = rnd.nextInt(6) +1;

                //Set Rolls
                dbConn.setRolls(TID,Roll1,Roll2);

                //Generate Update Files
                dbConn.createUpdateFiles(GID);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
