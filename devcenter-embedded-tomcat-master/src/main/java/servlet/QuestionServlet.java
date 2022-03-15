package servlet;

import Database.DBManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(
        name = "QuestionServlet",
        urlPatterns = {"/question"}
)
public class QuestionServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            //Grab Session Attributes
            int AID = (int) req.getSession().getAttribute("AID");
            int GID = (int) req.getSession().getAttribute("GID");

            int PID = dbConn.getPID(AID,GID);
            int TID =  dbConn.getTurn(PID).get(0);
            String GameState = dbConn.getGameState(GID);

            //Grab Form data
            //Get keyCode and see if user inputted location data
            BufferedReader reader = req.getReader();
            String[] data = reader.readLine().trim().split(",");
            int CID1 = Integer.parseInt(data[0]);
            int CID2 = Integer.parseInt(data[1]);

            //Grab Room Card from database
            int CID3 = Integer.parseInt(dbConn.getLocation(TID).get(0));

            //If TID is correct
            if (TID == dbConn.getCurrentTurnInfoFromGID(GID).get(3) && GameState.equals("playing")){
                //Question, already creates a new turn and generates update Files
                dbConn.Question(AID,GID,CID1,CID2,CID3);
            }
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
}
