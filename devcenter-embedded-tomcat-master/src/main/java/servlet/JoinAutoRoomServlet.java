package servlet;

import Database.DBManager;
import org.json.simple.parser.ParseException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;


@WebServlet(
        name = "JoinAutoRoomServlet",
        urlPatterns = {"/JoinAutoRoomServlet"}
)
public class JoinAutoRoomServlet extends HttpServlet {
    DBManager dbCon = new DBManager("Cluedo.db");
    Random rnd = new Random();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try{
            //Get AID of player requesting to join
            int AID = (int) req.getSession().getAttribute("AID");

            //GameSize - 3 to 7
            //GameType - Any/Normal/Big
            //Bots - 0 to 6
            //NBAny - true/false
            //GSAny - true/false

            int GameSize = Integer.parseInt(req.getParameter("GameSize"));
            int Bots = Integer.parseInt(req.getParameter("Bots"));
            String GameType = req.getParameter("GameType").trim();

            boolean GSAny = false;
            boolean NBAny = false;
            try{
                GSAny = req.getParameter("GSAny").trim().equals("on");
            }catch (NullPointerException ignore){}
            try{
                NBAny = req.getParameter("NBAny").trim().equals("on");
            }catch (NullPointerException ignore){}

            int MINGS = 3;
            int MAXGS = 7;

            if(GameType.equals("Normal Game")){
                MAXGS = 5;
            }else if(GameType.equals("Big Game")){
                MINGS = 5;
            }

            //Set default destination
            String dest = "Home.jsp";

            //Check for valid entries for Games
            if((GameSize > Bots || NBAny || GSAny) //Invalid Size is smaller or equal to Bot(s)
                    && (
                    //Correct Sizes for each Game Type
                    (GameType.equals("Big Game") && ((GameSize >= 5 && GameSize <= 7) || GSAny)) ||
                    (GameType.equals("Normal Game") && ((GameSize >= 3 && GameSize <= 5) || GSAny)) ||
                    (GameType.equals("Any") && ((GameSize >= 3 && GameSize <= 7) || GSAny))
                )
            ){
                try {
                    int GID = dbCon.getGameWithGenericSettings(GSAny,NBAny,GameSize,Bots,GameType);

                    if(GID!= -1){
                        //Check user isn't already in a room or playing a Game
                        boolean occupied = dbCon.isOccupied(AID);
                        if(!occupied){
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
                    else{
                        //Generate Game Size if needed
                        if(GSAny){
                            GameSize = MINGS + rnd.nextInt(MAXGS - MINGS + 1);
                        }

                        //Generate Number of Bots if needed
                        if(NBAny){
                            Bots = rnd.nextInt(GameSize-1);
                        }

                        //Correct Game Size if GameSize<=Bots, this only occurs when: GSAny && !NBAny
                        while(GameSize<=Bots){
                            GameSize++;
                        }

                        //Change to the Correct Game Type according to Game Size
                        if(GameType.equals("Any")){
                            if(GameSize<=5){
                                GameType = "Normal Game";
                            }else {
                                GameType = "Big Game";
                            }
                        }

                        //Create a Game according to settings, get GID of game created
                        GID = dbCon.addGame("", GameType, GameSize);

                        //Add Player to the Game
                        dbCon.addParticipant(AID, GID);

                        //Let Player session recognise it's in a Game
                        req.getSession().setAttribute("GID", GID);

                        //Bot AIDs go from 0 to 5 (total of 6), loop to add all the needed bots
                        for (int BotAID = 0; BotAID < Bots; BotAID++) {
                            dbCon.addParticipant(BotAID, GID);
                        }

                        //If single Player
                        if (Bots + 1 == GameSize) {
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
                    }
                }catch(SQLException | ParseException e){
                    e.printStackTrace();
                }
            }
            resp.sendRedirect(dest);
        }catch (Exception ignore){
            resp.sendRedirect("Home.jsp");
        }
    }
}
