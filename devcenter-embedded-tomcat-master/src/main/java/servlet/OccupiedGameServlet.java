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
        name = "OccupiedGameServlet",
        urlPatterns = {"/OccupiedGameServlet"}
)
public class OccupiedGameServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            //Get AID
            int AID = (int) req.getSession().getAttribute("AID");

            //Check if user is occupied returns -1 if NOT occupied
            int GID = dbConn.getGIDOccupiedFromAID(AID);

            //Send Game Data if occupied
            if(GID != -1){
                ArrayList<ArrayList> data = dbConn.getJSONGameInfo(GID);
                resp.getWriter().write(String.valueOf(data));
            }else{
                resp.getWriter().write("[]");
            }

        } catch (SQLException | NullPointerException ignore) {} //If user isn't logged in then ignore
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String ParamName = req.getParameterNames().nextElement();
            int AID = (int) req.getSession().getAttribute("AID");
            //If Rejoin button was clicked
            if (ParamName.equals("SetGID")) {
                int GID = Integer.parseInt(req.getParameter("SetGID"));
                req.getSession().setAttribute("GID", GID);
                String GameState = dbConn.getGameState(GID);
                String dest;
                if(GameState.equals("free")){
                    dest = "WaitingRoom.jsp";
                }else{//(else if) 'playing' TDL If bot hasn't taken players place due to timeout!!!
                    dest ="Game.jsp";
                }//else{} you timed out...
                resp.sendRedirect(dest);

            } else if (ParamName.equals("QuitGID")) {//If Quit button was clicked, source:Home.jsp or Game.jsp
                int GID = Integer.parseInt(req.getParameter("QuitGID"));
                String GameState = dbConn.getGameState(GID);

                //If Game was never started just remove participant
                if(GameState.equals("free")){
                    dbConn.removeParticipant(AID,GID);
                }else if(GameState.equals("finished")) {//Dont allow changes when Game is Finished

                }else{//'playing' TDL If bot hasn't taken players place due to timeout!!!

                    //Add loss to Account
                    dbConn.increaseAccountGameEndings(AID,"Losses");

                    //Change Points according to a Wrong Guess (quitting is bad :) )
                    int ChangePoints = 10 * dbConn.getGameSize(GID);
                    dbConn.setPoints(AID,dbConn.getPoints(AID)-ChangePoints);

                    //remove participant
                    dbConn.removeParticipant(AID,GID);

                }//else{} you timed out...
                resp.sendRedirect("Home.jsp");
            }
        }catch(SQLException | IOException | NullPointerException | ParseException | InterruptedException throwable){
            resp.sendRedirect("index.jsp");
        }
    }
}
