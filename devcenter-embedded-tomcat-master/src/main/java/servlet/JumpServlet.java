package servlet;

import Database.DBManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(
        name = "JumpServlet",
        urlPatterns = {"/jump"}
)
public class JumpServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            //Grab Session Attributes
            int AID = (int) req.getSession().getAttribute("AID");
            int GID = (int) req.getSession().getAttribute("GID");

            //Get CID posted
            BufferedReader reader = req.getReader();
            int RoomCID = Integer.parseInt(reader.readLine().trim());

            String GameType = dbConn.getGameType(GID);

            //Check CID is a valid room for the Game Type
            boolean validCID = (GameType.equals("Normal Game") && RoomCID>20 && RoomCID < 30) ||
                    (GameType.equals("Big Game") &&RoomCID>20 && RoomCID <34);

            //[AID,Roll1,Roll2,TID]
            ArrayList<Integer> Turn = dbConn.getCurrentTurnInfoFromGID(GID);

            int TID = Turn.get(3);
            int totalRoll = Turn.get(1)+Turn.get(2);
            int Step = Integer.parseInt(dbConn.getLocation(TID).get(2));
            String GameState = dbConn.getGameState(GID);
            //If Requester is Current Person Playing and has rolled 2 or 12
            //Only allow if player hasn't moved (Step=0)
            if(Turn.get(0)==AID && (totalRoll==2 || totalRoll == 12) && Step==0 && validCID && GameState.equals("playing")){

                dbConn.Jump(TID,RoomCID,totalRoll);

                //Generate Update Files
                dbConn.createUpdateFiles(GID);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
