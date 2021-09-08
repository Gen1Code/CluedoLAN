package servlet;

import Database.Connect;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class SetUpRoom {

    Random rnd = new Random();
    JSONParser parser = new JSONParser();
    Connect dbConn = new Connect("Rooms.db");
    public void setUp(int RoomID) throws IOException, SQLException, ParseException {

        JSONObject Room = new JSONObject();

        //Read
        String jsonText = dbConn.ReadRoom(RoomID);
        Room = (JSONObject) parser.parse(jsonText);

        //Set/Get Useful attributes
        int RoomSize = ((Long) Room.get("RoomSize")).intValue();
        boolean NormalGame = (boolean) Room.get("NormalGame");
        int cardsinStackT1;
        int cardsinStackT2;
        int colourLimit;
        if (NormalGame){
            cardsinStackT1 = 6;
            cardsinStackT2 = 9;
            colourLimit = 6;
        }else{
            cardsinStackT1 = 10;
            cardsinStackT2 = 13;
            colourLimit = 8;
        }

        //Create 3 types of cards
        Deck Stack1 = new Deck(cardsinStackT1);
        Deck Stack2 = new Deck(cardsinStackT1);
        Deck Stack3 = new Deck(cardsinStackT2);

        //Close Room
        Room.put("closed",true);

        //Choose Hidden Card, Offset used to differentiate
        int[] HiddenCards = new int[3];
        HiddenCards[0] = Stack1.giveCard();
        HiddenCards[1] = Stack2.giveCard() + cardsinStackT1;
        HiddenCards[2] = Stack3.giveCard() + cardsinStackT1 + cardsinStackT1;

        Deck NStack = new Deck(2*(cardsinStackT1) +cardsinStackT2);
        NStack.removeCard(HiddenCards[0]);
        NStack.removeCard(HiddenCards[1]);
        NStack.removeCard(HiddenCards[2]);
        Room.put("HiddenCards",HiddenCards);

        //Distribute Remaining Cards to each Player
        while(NStack.deckSize() >= RoomSize){
            for(int i =0;i<RoomSize; i++){
                int Card = NStack.giveCard();
                ((JSONArray)((JSONObject)((JSONArray)Room.get("Players")).get(i)).get("cards")).add(Card);
            }
        }
        //Set Community Cards     i++???? TDL
        for(int i =0;i< NStack.deckSize();){
            int Card = NStack.giveCard();
            ((JSONArray)Room.get("CommunityCards")).add(Card);
        }
        //Give Players a unique random color
        //Set their position according to colour
        ArrayList<String> Colours = new ArrayList<>();
        Colours.add("#FF0000");Colours.add("#00FF00");//Red,Green
        Colours.add("#0000FF");Colours.add("#FFFFFF");//Blue,White
        Colours.add("#FFFF00");Colours.add("#EE82EE");//Yellow,Violet
        Colours.add("#FFA500");Colours.add("#FFFFFF"); //Orange,Black

        ArrayList<String> StartPos = new ArrayList<>();
        StartPos.add("0,16,0");StartPos.add("24,9,0");
        StartPos.add("18,0,0");StartPos.add("24,14,0");
        StartPos.add("7,23,0");StartPos.add("5,0,0");//Add correct starting position Row,Column
        StartPos.add("2,11,1");StartPos.add("11,6,1");

        for(int i = 0;i<RoomSize;i++){
            int Index = rnd.nextInt(colourLimit); //Index,colourLimit also serve to get starting position
            String colour = Colours.get(Index);
            String[] rowColFlo = StartPos.get(Index).split(",");
            Colours.remove(Index);
            StartPos.remove(Index);
            colourLimit = colourLimit - 1;
            ((JSONObject)((JSONArray)Room.get("Players")).get(i)).put("colour",colour);
            ((JSONObject)((JSONArray)Room.get("Players")).get(i)).put("row",rowColFlo[0]);
            ((JSONObject)((JSONArray)Room.get("Players")).get(i)).put("column",rowColFlo[1]);
            ((JSONObject)((JSONArray)Room.get("Players")).get(i)).put("floor",rowColFlo[2]);

        }

        //Random Turn (index value in Players Array)
        int Turn = rnd.nextInt(RoomSize);
        Room.put("Turn",Turn);

        //TEXT THING TDL



        //Write
        dbConn.WriteRoom(RoomID, Room);

    }
}
