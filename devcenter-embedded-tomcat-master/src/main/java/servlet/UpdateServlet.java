package servlet;

import Database.Connect;
import org.apache.catalina.Role;
import org.apache.catalina.User;
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
import java.util.ArrayList;

@WebServlet(
        name = "UpdateServlet",
        urlPatterns = {"/update"}
)
public class UpdateServlet extends HttpServlet {
    Connect dbConn = new Connect("Rooms.db");
    JSONParser parser = new JSONParser();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        //Grab Session Attributes
        int UserRoomID = -1;
        String Username= "";

        try {
            UserRoomID = (int) req.getSession().getAttribute("RoomID");
            Username = ((String) req.getSession().getAttribute("name")).trim();
        }catch (Exception e){
            resp.sendRedirect("index.jsp");
            return;
        }

        boolean validID = false;
        boolean validName = false;

        JSONObject Room = new JSONObject();
        try {
            String RoomString = dbConn.ReadRoom(UserRoomID);
             Room = (JSONObject) parser.parse(RoomString);
        } catch (SQLException | ParseException | IndexOutOfBoundsException throwables ) {
            System.out.println("ID Doesn't exist");
            resp.sendRedirect("index.jsp");
            return;
        }
        //Grab useful attributes
        int RoomSize = ((Long) Room.get("RoomSize")).intValue();

        if(!((boolean) Room.get("GameFinished"))){
            validID = true;
        }
        for(int i = 0;i<RoomSize;i++){
            String tempName = ((String) ((JSONObject)((JSONArray)Room.get("Players")).get(i)).get("PlayerName")).trim();
            if(tempName.equals(Username)){
                validName = true;
            }
        }

        if(validID && validName){
            JSONObject ConstructNewUpdate = new JSONObject();
            //Read template file REMOVE THIS ONCE GAME IS FINISHED (no need for file apart from ease of debugging)
            FileReader fileR = new FileReader("src/main/webapp/UpdateTemplate.json");
            try {
                ConstructNewUpdate = (JSONObject) parser.parse(fileR);
            }catch (ParseException e){
                e.printStackTrace();
            }

            //Board  REMOVE CARDS
            for(int i=0;i<RoomSize;i++){
                ((JSONArray)ConstructNewUpdate.get("Board")).add(((JSONArray) Room.get("Players")).get(i));
                ((JSONObject)((JSONArray)ConstructNewUpdate.get("Board")).get(i)).remove("cards");
            }

            //Dice
            ((JSONArray)ConstructNewUpdate.get("Dices")).add(((JSONArray)Room.get("Dices")).get(0));
            ((JSONArray)ConstructNewUpdate.get("Dices")).add(((JSONArray)Room.get("Dices")).get(1));

            //Community Cards
            for(int i =0;i<((JSONArray)Room.get("CommunityCards")).size();i++){
                int Card = ((Long)((JSONArray)Room.get("CommunityCards")).get(i)).intValue();
                ((JSONArray)ConstructNewUpdate.get("CommunityCards")).add(Card);
            }

            //Personal Cards
            //Get your index
            int Index = -1;
            for(int i =0;i<RoomSize; i++){
                if((((JSONObject)((JSONArray)Room.get("Players")).get(i)).get("PlayerName")).equals(Username)){
                    Index = i;
                }
            }
            //Add your cards
            ConstructNewUpdate.put("PlayerCards",((JSONObject)((JSONArray)Room.get("Players")).get(Index)).get("cards"));

            System.out.println("Valid Update Get by "+Username+" ID: "+UserRoomID);
            //Send Data
            resp.getWriter().write(String.valueOf(ConstructNewUpdate));
        }else{
            resp.sendRedirect("index.jsp");
        }
    }
}
