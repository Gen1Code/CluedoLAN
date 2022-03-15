package servlet;

import Database.DBManager;
import org.json.simple.parser.ParseException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet(
        name = "CreateRoomServlet",
        urlPatterns = {"/CreateRoomServlet"}
)
public class CreateRoomServlet extends HttpServlet {
    //Connection to Database Object
    DBManager dbCon = new DBManager("Cluedo.db");

    //Define regexes
    private final String alphanum2to12 = "^[A-Z0-9a-z ]{2,12}$";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        //Grab Game Settings chosen and the Users AID
        int AID = (int) req.getSession().getAttribute("AID");
        String GameName = req.getParameter("GameName");
        String GameType = req.getParameter("GameType");
        int GameSize = Integer.parseInt(req.getParameter("GameSize"));
        int NoOfBots = Integer.parseInt(req.getParameter("NoOfBots"));

        String dest = "Home.jsp";

        //Check Settings are Correct, if not then don't create Game and redirect to Home Page
        if (Pattern.matches(alphanum2to12, GameName) && GameSize > NoOfBots  //Invalid Name, Size is smaller or equal to Bot(s)
                && (!GameType.equals("Big Game") || (GameSize >= 5 && GameSize <= 7)) //Size is different from should be for game type
                && (!GameType.equals("Normal Game") || (GameSize >= 3 && GameSize <= 5)) //Size is different from should be for game type
                && (GameType.equals("Normal Game") || GameType.equals("Big Game"))) {

            //Set default destination to Home page (if error occurs)
            try {
                //Create a Game according to settings, get GID of game created
                int GID = dbCon.addGame(GameName, GameType, GameSize);

                //Add Player to the Game
                dbCon.addParticipant(AID, GID);

                //Let Player session recognise it's in a Game
                req.getSession().setAttribute("GID", GID);

                //Bot AIDs go from 0 to 5 (total of 6), loop to add all the needed bots
                for (int BotAID = 0; BotAID < NoOfBots; BotAID++) {
                    dbCon.addParticipant(BotAID, GID);
                }

                //If single Player
                if (NoOfBots + 1 == GameSize) {
                    //Redirect to Game
                    dest = "Game.jsp";

                    //Set up Game ready to Play
                    dbCon.setUpGame(GID);
                } else {
                    //Set Game as free to join
                    dbCon.setGameState(GID, "free");

                    //Set destination of Creator in Waiting room
                    dest = "WaitingRoom.jsp";
                }
            } catch (SQLException | ParseException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Redirect Player to correct destination
        resp.sendRedirect(dest);

    }
}
