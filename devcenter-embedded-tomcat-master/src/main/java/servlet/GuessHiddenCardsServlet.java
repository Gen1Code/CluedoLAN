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
        name = "GuessHiddenCardsServlet",
        urlPatterns = {"/guess"}
)
public class GuessHiddenCardsServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            //Grab Session Attributes
            int AID = (int) req.getSession().getAttribute("AID");
            int GID = (int) req.getSession().getAttribute("GID");

            //Grab Post Data
            BufferedReader reader = req.getReader();
            String[] data = reader.readLine().split(",");
            int SuspectCID = Integer.parseInt(data[0].trim());
            int WeaponCID = Integer.parseInt(data[1].trim());
            int RoomCID = Integer.parseInt(data[2].trim());

            //[AID,Roll1,Roll2,TID]
            ArrayList<Integer> Turn = dbConn.getCurrentTurnInfoFromGID(GID);
            int TID = Turn.get(3);

            if(Turn.get(0) == AID){
                dbConn.GuessHiddenCards(GID,TID,SuspectCID,WeaponCID,RoomCID);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
