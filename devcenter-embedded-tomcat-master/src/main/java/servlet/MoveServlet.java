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
        name = "MoveServlet",
        urlPatterns = {"/move"}
)
public class MoveServlet extends HttpServlet {
    DBManager dbConn = new DBManager("Cluedo.db");
    Board board = new Board();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            //Grab Session Attributes
            int AID = (int) req.getSession().getAttribute("AID");
            int GID = (int) req.getSession().getAttribute("GID");

            //Set Database Locations in variables
            //[AID,Roll1,Roll2,TID]
            ArrayList<Integer> Turn = dbConn.getCurrentTurnInfoFromGID(GID);
            int TID = Turn.get(3);
            int maxStep = Turn.get(1) + Turn.get(2);

            //[Room,Location,Step]
            ArrayList<String> TurnLocation = dbConn.getLocation(TID);

            int Room = Integer.parseInt(TurnLocation.get(0));
            String[] Location = TurnLocation.get(1).split(",");
            int Step = Integer.parseInt(TurnLocation.get(2));
            String GameState = dbConn.getGameState(GID);

            int currentRow = Integer.parseInt(Location[0].trim());
            int currentCol = Integer.parseInt(Location[1].trim());
            int currentFloor = Integer.parseInt(Location[2].trim());

            //Get keyCode and see if user inputted location data
            BufferedReader reader = req.getReader();
            String[] data = reader.readLine().trim().split(",");
            boolean didInputLocation = (data.length>1);
            String keyCode = data[0];

            String GameType = dbConn.getGameType(GID);


            //If correct player is making a request
            //Player has steps left
            if(Turn.get(0)==AID && maxStep>Step && GameState.equals("playing")){
                //If Player did input Location data then the Player wants to leave a room
                //Since Location data is only sent if user is exiting a room
                if(didInputLocation){
                    String[] InputLocation = data[1].split(" ");
                    int InputRow = Integer.parseInt(InputLocation[0]);
                    int InputCol = Integer.parseInt(InputLocation[1]);
                    int InputFloor = Integer.parseInt(InputLocation[2]);

                    if(board.validEntrance(InputRow,InputCol,InputFloor,Room)){
                        if(board.correctDirection(keyCode,InputRow,InputCol,InputFloor)){
                            int[] NewLocation = board.ChangeLocation(keyCode,InputRow,InputCol,InputFloor);
                            //Exit Room onto a Location
                            dbConn.movePlayer(TID,Step+1,NewLocation,0);

                            //Generate Update Files
                            dbConn.createUpdateFiles(GID);

                        }
                    }
                }else{
                    //[2] == -1 if user goes into a Room and -2 if move is invalid
                    int[] NewLocation = board.ChangeLocation(keyCode,currentRow,currentCol,currentFloor);

                    //If Player is doing a valid move into Free Space, reject floor 1 moves if user is in a Normal Game
                    if((NewLocation[2] == 1 && GameType.equals("Big Game")) || NewLocation[2] == 0){
                        dbConn.movePlayer(TID,Step+1,NewLocation,0);
                        if(Step+1 == maxStep){
                            dbConn.createNewTurn(AID,GID);
                        }
                        //Generate Update Files
                        dbConn.createUpdateFiles(GID);

                    }else if(NewLocation[2] == -1) {//If user goes into a room
                        //Make a null Location
                        int[] nullLocation = {-1,-1,-1};

                        //Set Room (i=1)
                        //Fill rest of Steps of Turn, since once a player enters a room they aren't allowed to leave in that turn
                        for (int i = 1; Step + i <= maxStep; i++) {
                            dbConn.movePlayer(TID, Step + i, nullLocation, NewLocation[0]);
                        }

                        //Generate Update Files
                        dbConn.createUpdateFiles(GID);

                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
