package servlet;

import Database.DBManager;
import org.json.simple.parser.ParseException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(
        name = "JoinRoomServlet",
        urlPatterns = {"/JoinRoomServlet"}
)
public class JoinRoomServlet extends HttpServlet {
    DBManager dbCon = new DBManager("Cluedo.db");
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            //Get Data
            ArrayList<ArrayList> data = dbCon.getAllFreeGameInfo();

            //Send data
            resp.getWriter().write(String.valueOf(data));

        } catch (SQLException | NullPointerException ignored) {
            System.out.println("Error Join game");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //Get AID of player requesting to join
        int AID = (int) req.getSession().getAttribute("AID");

        //Get Game GID player wants to join
        int GID = Integer.parseInt(req.getParameter("GID"));

        //Set default destination
        String dest = "JoinRoom.jsp";

        try {
            //Check user isn't already in a room or playing a Game
            boolean occupied = dbCon.isOccupied(AID);
            if(!occupied){

                //check that Game isn't full
                boolean full = dbCon.isGameFull(GID);
                if(!full){
                    //Add participant
                    dbCon.addParticipant(AID,GID);
                    //set GID attribute
                    req.getSession().setAttribute("GID",GID);

                    //Since Participant is added now, if Game is now Full then this player is the last person needed to 'fill' the room
                    boolean last = dbCon.isGameFull(GID);

                    //Setup Game if joining last
                    if (last) {
                        //Setup Game
                        dbCon.setUpGame(GID);

                        //set destination to Game Page
                        dest = "Game.jsp";
                    }else{
                        //Set destination to Waiting Room
                        dest = "WaitingRoom.jsp";
                    }
                }
            }
            //Messages maybe TDL?
        }catch(SQLException | ParseException | InterruptedException e){
            e.printStackTrace();
        }

        resp.sendRedirect(dest);
    }
}
