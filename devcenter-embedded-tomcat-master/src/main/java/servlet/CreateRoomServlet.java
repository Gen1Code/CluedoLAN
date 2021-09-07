package servlet;

import Database.Connect;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(
        name = "CreateRoomServlet",
        urlPatterns = {"/CreateRoomServlet"}
)
public class CreateRoomServlet extends HttpServlet {

    JSONObject Player;
    JSONObject Bot;
    JSONParser parser = new JSONParser();
    final String BotName = "Bot";
    Connect dbConn = new Connect("Rooms.db");
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String PlayerName = (String) req.getSession().getValue("name");
        String RoomName = req.getParameter("RoomName").trim();
        String RoomType = req.getParameter("RoomType");
        int RoomSize = Integer.parseInt(req.getParameter("RoomSize"));
        int NoOfBots = Integer.parseInt(req.getParameter("NoOfBots"));
        int MAXRoomID = 0;
        try {
            MAXRoomID = Integer.parseInt(dbConn.max("RoomID","Rooms"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (NumberFormatException ignored){ //Ignores if no rooms are present

        }
        int RoomID = MAXRoomID +1;

        JSONObject ConstructNewRoom = new JSONObject();
        //Read template files REMOVE THIS ONCE GAME IS FINISHED (no need for file apart from ease of debugging)
        FileReader fileR = new FileReader("src/main/webapp/RoomTemplate.json");
        FileReader fileR2 = new FileReader("src/main/webapp/PlayerTemplate.json");
        try {
            ConstructNewRoom = (JSONObject) parser.parse(fileR);
            Player = (JSONObject) parser.parse(fileR2);
            Bot = Player;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        fileR.close();
        fileR2.close();

        //ADD PLAYER
        Player.put("PlayerName",PlayerName);
        ((JSONArray) ConstructNewRoom.get("Players")).add(Player);

        //ADD ROOM SETTINGS
        ConstructNewRoom.put("RoomID",RoomID);
        ConstructNewRoom.put("RoomName",RoomName);
        ConstructNewRoom.put("RoomSize",RoomSize);
        if(RoomType.equals("Big Game")){
            ConstructNewRoom.put("NormalGame",false);
        }

        //ADD BOTS
        for(int i = 0;i<NoOfBots;i++){
            JSONObject temp = new JSONObject(Bot);
            String UNIQUEBotName = BotName + (i+1);
            temp.put("PlayerName",UNIQUEBotName);
            ((JSONArray) ConstructNewRoom.get("Players")).add(temp);
        }

        String dest = "WaitingRoom.jsp";
        boolean singlePlayer = false;
        //If single Player redirect to game and  CALL SETUP ROOM FLAG
        if(((JSONArray) ConstructNewRoom.get("Players")).size() == (int) ConstructNewRoom.get("RoomSize")){
            dest = "Game.jsp";
            singlePlayer = true;
        }

        //create room in database
        try {
            dbConn.CreateRoom(RoomID,ConstructNewRoom);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //SetUp Room, for multiPlayer last person joining tells server to set up
        if(singlePlayer){
            SetUpRoom temp = new SetUpRoom();
            try {
                temp.setUp(RoomID);
            } catch (SQLException | ParseException throwables) {
                throwables.printStackTrace();
            }
        }

        req.getSession().setAttribute("RoomID",RoomID);
        resp.sendRedirect(dest);

    }
}
