package Database;

import org.apache.tomcat.util.buf.HexUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import servlet.Bot;
import servlet.Deck;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DBManager {
    //Constructor, filename.db
    public DBManager(String DatabaseName) {
        url = url+DatabaseName;
    }

    //Set sqlite database path
    private final String contentPath = "src/main/java/Database/db";
    private String url = "jdbc:sqlite:" + contentPath + "/";

    //Create Hash Object
    PBKDF2WithHmacSHA512 Hash = new PBKDF2WithHmacSHA512();

    //Create Random Object
    Random rnd = new Random();

    //Create Parser Object
    JSONParser parser = new JSONParser();

    //Open a connection to the database
    public Connection openConnection(){
        try {
            return DriverManager.getConnection(url);
        }catch(SQLException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Adds a user Account given a Player Name, Email and Password
    public void addUser(String PlayerName, String Email, String Password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        //Get a salt and then hash password
        byte[] Saltb = Hash.salt();
        byte[] HashedPasswordb = Hash.hash(Password,Saltb);

        //Convert to HexString for easier database storage
        String Salt = HexUtils.toHexString(Saltb);
        String HashedPassword = HexUtils.toHexString(HashedPasswordb);

        //Add Account to database
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("insert into Accounts(\"PlayerName\",\"Email\",\"Password\",\"Salt\") values(\""+PlayerName+"\",\""+Email.toLowerCase()+"\",\""+HashedPassword+"\",\""+Salt+"\");");
        stmt.executeUpdate();
        con.close();
    }

    //returns -1 if user is not authenticated and their Account ID if they are
    public int authenticate(String Email, String attemptedPassword) throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        //Get Account ID the Hashed Password and Salt for the particular Email
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select AID, Password, Salt from Accounts where Email=\""+Email+"\";");
        ResultSet rs = stmt.executeQuery();

        //Grab values from query, skip if no values
        if(rs.isBeforeFirst()){
            String hashedPassword = rs.getString("Password");
            String salt = rs.getString("Salt");
            int AID = rs.getInt("AID");
            conn.close();
            //Calculate New Hash using correct Salt
            String NewHash = HexUtils.toHexString(Hash.hash(attemptedPassword, HexUtils.fromHexString(salt)));

            //If hashes are equal return the Account ID
            if(NewHash.equals(hashedPassword)){
                return AID;
            }else{
                return -1;
            }
        }else{
            conn.close();
            //If invalid credentials return -1
            return -1;
        }
    }

    //Get given a password and AID, Hash it and store it + salt, for the user
    public void setPassword(int AID, String Password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        //Get a salt and then hash password
        byte[] Saltb = Hash.salt();
        byte[] HashedPasswordb = Hash.hash(Password,Saltb);

        //Convert to HexString for easier database storage
        String Salt = HexUtils.toHexString(Saltb);
        String HashedPassword = HexUtils.toHexString(HashedPasswordb);

        //Change Password and Salt in database
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("update Accounts set Password=\""+HashedPassword+"\", Salt=\""+Salt+"\" where AID="+AID+";");
        stmt.executeUpdate();
        con.close();
    }

    public String getEmail(int AID) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select Email from Accounts where AID="+AID+";");
        ResultSet rs = stmt.executeQuery();
        String Email = rs.getString("Email").toLowerCase();
        rs.close();
        conn.close();
        return Email;
    }

    public int getAIDFromPID(int PID) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select AID from Participation where PID="+PID+";");
        ResultSet rs = stmt.executeQuery();
        int AID = rs.getInt("AID");
        conn.close();
        return AID;
    }

    public void setSessionID(int AID, String sessionID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("update Accounts set SessionID=\""+sessionID+"\" where AID="+AID+";");
        stmt.executeUpdate();
        con.close();
        System.out.println("Set sessionID "+sessionID+" to "+AID);
    }

    public String getSessionID(int AID) throws SQLException, NullPointerException{
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select SessionID from Accounts where AID="+AID+";");
        ResultSet rs = stmt.executeQuery();
        String sessionID = rs.getString("SessionID");
        conn.close();
        return sessionID;
    }

    //return PID or -1 if no PID found
    public int getPID(int AID, int GID) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select PID from Participation where AID="+AID+" and GID="+GID+";");
        ResultSet rs = stmt.executeQuery();

        //Grab value from query, skip if no value
        if(rs.isBeforeFirst()){
            //Grab PID and return it
            int PID = rs.getInt("PID");
            conn.close();
            return PID;
        }
        conn.close();
        //If invalid IDs return -1
        return -1;
    }

    //Add a Game Row and return the GID created
    public int addGame(String GameName, String GameType, int GameSize) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("insert into Game(\"GameName\",\"GameType\",\"GameSize\") values(\""+GameName+"\",\""+GameType+"\","+GameSize+");");
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        int GID = rs.getInt(1);
        con.close();
        return GID;
    }

    //Add a Participant Row
    public void addParticipant(int AID, int GID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("insert into Participation(\"AID\",\"GID\") values("+AID+","+GID+");");
        stmt.executeUpdate();
        con.close();
    }

    //Remove a Participant Row, if only player participant delete game + bots
    public void removeParticipant(int AID, int GID) throws SQLException, ParseException, InterruptedException {
        Connection con = openConnection();
        PreparedStatement checkEmpty = con.prepareStatement("select AID from Participation where GID="+GID+";");
        ResultSet rs = checkEmpty.executeQuery();

        //If rs is of size 1 (excludes bots) delete game
        boolean DeleteGameFlag = true;
        int size = 0;
        int greatestBotAID=-1;
        while(rs.next()){
            //Only AIDs greater than 5 are players
            int tempAID = rs.getInt("AID");
            if(tempAID>5){
                size = size +1;
                if(size >= 2){
                    DeleteGameFlag = false;
                    break;
                }
            }else{
                if(tempAID>greatestBotAID){
                    greatestBotAID = tempAID;
                }
            }
        }
        String GameState = getGameState(GID,con);

        //Don't delete games which are finished
        if(DeleteGameFlag && !GameState.equals("finished")){

            //Remove Update Files
            deleteGameUpdateFiles(con,GID);

            //Create Statements ready to execute
            PreparedStatement deleteLocations = con.prepareStatement(
            "delete from Location where TID in ("+
                    "select Location.TID from Participation join Turn on (Participation.PID=Turn.PID) join Location on (Turn.TID=Location.TID) where Participation.GID="+GID+
                ");"
            );
            PreparedStatement deleteQuestions = con.prepareStatement(
            "delete from Question where TID in ("+
                    "select TID from Participation join Turn on (Participation.PID=Turn.PID) where Participation.GID="+GID+
                ");"
            );
            PreparedStatement deletePlayerCards = con.prepareStatement(
            "delete from PlayerCards where PID in (" +
                    "select PlayerCards.PID from Participation join PlayerCards on (Participation.PID=PlayerCards.PID) where Participation.GID=" + GID +
                ");"
            );
            PreparedStatement deleteCommunityCards = con.prepareStatement(
            "delete from CommunityCards where GID="+GID+";"
            );
            PreparedStatement deleteHiddenCards = con.prepareStatement(
                    "delete from HiddenCards where GID="+GID+";"
            );
            PreparedStatement deleteTurns = con.prepareStatement(
            "delete from Turn where TID in ("+
                    "select TID from Participation join Turn on (Participation.PID=Turn.PID) where Participation.GID="+GID+
                ");"
            );
            PreparedStatement deleteParticipants = con.prepareStatement(
            "delete from Participation where GID="+GID+";"
            );
            PreparedStatement deleteGame = con.prepareStatement(
            "delete from Game where GID="+GID+";"
            );

            //Execute Prepared Statements (Important: IN ORDER)
            deleteLocations.executeUpdate();
            deleteQuestions.executeUpdate();
            deletePlayerCards.executeUpdate();
            deleteCommunityCards.executeUpdate();
            deleteHiddenCards.executeUpdate();
            deleteTurns.executeUpdate();
            deleteParticipants.executeUpdate();
            deleteGame.executeUpdate();
            con.close();
        }else{
            if(GameState.equals("free")){
                PreparedStatement stmt = con.prepareStatement("delete from Participation where AID="+AID+" and GID="+GID+";");
                stmt.executeUpdate();
                con.close();
            }else if (GameState.equals("playing")){
                //Grab Current Turn AID
                int TurnAID = getCurrentTurnInfoFromGID(GID).get(0);

                //Change Game Order (since relies on AID)
                ArrayList<Integer> GameOrder = getGameOrder(GID,con);
                GameOrder.set(GameOrder.indexOf(AID),greatestBotAID+1);
                int[] Order = new int[GameOrder.size()];
                for(int i=0;i<GameOrder.size();i++){
                    Order[i] = GameOrder.get(i);
                }
                setGameOrder(GID,Arrays.toString(Order),con);

                //Change Player to Bot
                PreparedStatement stmt = con.prepareStatement("update Participation set AID="+(greatestBotAID+1)+" where AID="+AID+" and GID="+GID+";");
                stmt.executeUpdate();
                con.close();

                //Delete Update File
                File f= new File("src/main/java/Database/GameUpdateFiles/" + GID + "," + AID + ".txt");
                f.delete();

                //change Turn ONLY if the player calling this method is currently playing
                if(AID == TurnAID){
                    createNewTurn(greatestBotAID+1,GID);
                }
            }
        }

    }


    private void deleteGameUpdateFiles(Connection con,int GID) throws SQLException {
        PreparedStatement AIDs = con.prepareStatement("select AID from Participation where GID="+GID+" and AID >5;");
        ResultSet rs2 = AIDs.executeQuery();
        while(rs2.next()){
            //Delete Update File
            File f = new File("src/main/java/Database/GameUpdateFiles/" + GID + "," + rs2.getInt(1) + ".txt");
            f.delete();
        }
    }

    //Get Size of game
    public int getGameSize(int GID,Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("select GameSize from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();
        return rs.getInt("GameSize");
    }
    public int getGameSize(int GID) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select GameSize from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();
        int GameSize = rs.getInt("GameSize");
        conn.close();
        return GameSize;
    }

    //Get the Type of Game
    public String getGameType(int GID, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("select GameType from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();
        return rs.getString("GameType");
    }
    public String getGameType(int GID) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select GameType from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();
        String GameType = rs.getString("GameType");
        conn.close();
        return GameType;
    }


    public String getGameState(int GID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select GameState from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();
        String GameState = rs.getString("GameState");
        con.close();
        return GameState;
    }

    public String getGameState(int GID, Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("select GameState from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();
        return rs.getString("GameState");
    }

    public ArrayList<Integer> getGameOrder(int GID) throws SQLException{
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select GameOrder from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();

        if(rs.next()){
            String[] GameOrder = rs.getString("GameOrder").replace("[","").replace("]","").split(",");
            con.close();
            ArrayList<Integer> GameOrderArrayList = new ArrayList<>();
            for (int i = 0; i < GameOrder.length; i++) {
                GameOrderArrayList.add(Integer.parseInt(GameOrder[i].trim()));
            }
            return GameOrderArrayList;
        }else{
            con.close();
            throw new SQLException();
        }
    }
    public ArrayList<Integer> getGameOrder(int GID, Connection con) throws SQLException{
        PreparedStatement stmt = con.prepareStatement("select GameOrder from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();
        if(rs.next()){
            String[] GameOrder = rs.getString("GameOrder").replace("[","").replace("]","").split(",");
            ArrayList<Integer> GameOrderArrayList = new ArrayList<>();
            for (int i = 0; i < GameOrder.length; i++) {
                GameOrderArrayList.add(Integer.parseInt(GameOrder[i].trim()));
            }
            return GameOrderArrayList;
        }else{
            throw new SQLException();
        }
    }

    public int getStep(int TID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select max(Step) as LStep from Location where TID="+TID+";");
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int Step = rs.getInt("LStep");
        con.close();
        return Step;
    }

    //Set the Game State
    public void setGameState(int GID, String GameState) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("update Game set GameState=\""+GameState+"\" where GID="+GID+";");
        stmt.executeUpdate();
        con.close();
    }

    //Set the Game Order
    public void setGameOrder(int GID, String GameOrder, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("update Game set GameOrder=\""+GameOrder+"\" where GID="+GID+";");
        stmt.executeUpdate();
    }


    public ArrayList<Integer> getPIDs(int GID) throws SQLException {

        Connection conn = openConnection();
        //Get all players in the game
        PreparedStatement getAIDPIDQuery = conn.prepareStatement("select PID from Participation where GID="+GID+";");
        ResultSet rs = getAIDPIDQuery.executeQuery();
        ArrayList<Integer> PIDs= new ArrayList<>();
        while (rs.next()){
            int TempPID = rs.getInt("PID");
            PIDs.add(TempPID);
        }
        conn.close();
        return PIDs;

    }

    public ArrayList<Integer> getGIDs(String GameState) throws SQLException {
        Connection conn = openConnection();
        //Get all players in the game
        PreparedStatement getAllGIDFromGameStateQuery = conn.prepareStatement("select GID from Game where GameState=\""+GameState+"\";");
        ResultSet rs = getAllGIDFromGameStateQuery.executeQuery();
        ArrayList<Integer> GIDs = new ArrayList<>();
        while (rs.next()){
            int TempGID = rs.getInt("GID");
            GIDs.add(TempGID);
        }
        conn.close();
        return GIDs;
    }

    public ArrayList<Integer> getTIDs(int GID) throws SQLException {
        ArrayList<Integer> TIDs = new ArrayList<>();
        ArrayList<Integer> PIDs = getPIDs(GID);
        String PIDsFormatted = Arrays.toString(PIDs.toArray()).replaceAll("]","").replaceAll("\\[","");
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select TID from Turn where PID in ("+PIDsFormatted+")");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()){
            TIDs.add(rs.getInt("TID"));
        }

        return TIDs;
    }

    public void movePlayer(int TID, int NewStep, int[] Location, int Room) throws SQLException {
        Connection conn = openConnection();
        String LocationFormatted = Arrays.toString(Location).replaceAll("]","").replaceAll("\\[","");
        PreparedStatement LocationInsert = conn.prepareStatement(
                "insert into Location(\"TID\",\"Room\",\"Location\",\"Step\") " +
                        "values(?,?,?,?);");

        LocationInsert.setInt(1,TID);
        LocationInsert.setInt(2,Room);
        LocationInsert.setString(3,LocationFormatted);
        LocationInsert.setInt(4,NewStep);
        LocationInsert.executeUpdate();
        conn.close();
    }

    //Check if an account is occupied or not, returns -1 if not occupied and Gid if occupied
    public int getGIDOccupiedFromAID(int AID) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement getAllGIDFromGameStateQuery = conn.prepareStatement("select Participation.GID from Participation join Game on (Participation.GID = Game.GID) where (GameState=\"free\" or GameState=\"playing\")and AID="+AID+";");
        ResultSet rs = getAllGIDFromGameStateQuery.executeQuery();
        if (rs.next()) {
            int GID = rs.getInt("GID");
            conn.close();
            return GID;
        }else{
            conn.close();
            return -1;
        }
    }

    public ArrayList<Integer> getAIDsFromPIDs(ArrayList<Integer> PIDs) throws SQLException {
        Connection conn = openConnection();
        String PIDsFormatted = Arrays.toString(PIDs.toArray()).replaceAll("]","").replaceAll("\\[","");
        ArrayList<Integer> AIDs = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("select AID from Participation where PID in ("+PIDsFormatted+");");
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            AIDs.add(rs.getInt("AID"));
        }
        conn.close();
        return AIDs;
    }

    public ArrayList<ArrayList> getJSONGameInfo(int GID) throws SQLException {
        ArrayList<ArrayList> data = new ArrayList<>();
        ArrayList<ArrayList> Players = new ArrayList();
        ArrayList Game = new ArrayList();

        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select PlayerName, Points from Participation join Accounts on (Participation.AID=Accounts.AID) where GID="+GID+";");
        PreparedStatement stmt2 = con.prepareStatement("select GameType, GameSize, GameName, GID, GameState from Game where GID="+GID+";");

        ResultSet rs = stmt.executeQuery();
        while (rs.next()){
            ArrayList temp = new ArrayList();
            temp.add("\""+rs.getString("PlayerName")+"\"");
            temp.add(rs.getInt("Points"));
            Players.add(temp);
        }

        ResultSet rs2 = stmt2.executeQuery();
        Game.add("\""+rs2.getString("GameType")+"\"");
        Game.add(rs2.getInt("GameSize"));
        Game.add("\""+rs2.getString("GameName")+"\"");
        Game.add(rs2.getInt("GID"));
        Game.add("\""+rs2.getString("GameState")+"\"");
        con.close();

        data.add(Game);
        data.add(Players);
        return data;
    }

    public ArrayList<ArrayList> getGameInfo(int GID) throws SQLException {
        ArrayList<ArrayList> data = new ArrayList<>();
        ArrayList<ArrayList> Players = new ArrayList();
        ArrayList Game = new ArrayList();

        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select PlayerName, Points from Participation join Accounts on (Participation.AID=Accounts.AID) where GID="+GID+";");
        PreparedStatement stmt2 = con.prepareStatement("select GameType, GameSize, GameName, GID, GameState from Game where GID="+GID+";");

        ResultSet rs = stmt.executeQuery();
        while (rs.next()){
            ArrayList temp = new ArrayList();
            temp.add("\""+rs.getString("PlayerName")+"\"");
            temp.add(rs.getInt("Points"));
            Players.add(temp);
        }

        ResultSet rs2 = stmt2.executeQuery();
        Game.add(rs2.getString("GameType"));
        Game.add(rs2.getInt("GameSize"));
        Game.add(rs2.getString("GameName"));
        Game.add(rs2.getInt("GID"));
        Game.add(rs2.getString("GameState"));
        con.close();

        data.add(Game);
        data.add(Players);
        return data;
    }

    public ArrayList<ArrayList> getAllFreeGameInfo() throws SQLException{
        ArrayList<ArrayList> data = new ArrayList<>();
        ArrayList<Integer> GIDs = getGIDs("free");
        for(int i=0;i<GIDs.size();i++){
            ArrayList<ArrayList> temp = getJSONGameInfo(GIDs.get(i));
            data.add(temp);
        }

        return data;
    }

    //Returns a GID that matches settings wanted
    //Returns -1 if no Available Game
    public int getGameWithSettings(int GameSize, int BotNumber, String GameType) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement(
        "select GID from Game "+
            "where GameSize = "+GameSize+" and GID in ("+
                "select GID from ("+
                    "select count(case when AID < 5 then 1 end) as BotNumber, GameSize, Game.GID "+
                    "from Game join Participation on (Participation.GID = Game.GID) group by Game.GID"+
                ") x "+
                "where x.BotNumber = "+ BotNumber +
            " ) and GameState=\"free\" and GameType=\""+GameType+"\";"
        );
        ResultSet rs = stmt.executeQuery();
        if(rs.next()){
            int GID = rs.getInt(1);
            con.close();
            return GID;
        }else {
            con.close();
            return -1;
        }
    }

    //Returns a GID that matches the generic settings wanted
    //Returns -1 if no Available Game
    public int getGameWithGenericSettings(boolean GSAny, boolean NBAny, int GameSize, int BotNumber, String GameType) throws SQLException {
        String GameSizeSQLString = "";
        if(!GSAny){
            GameSizeSQLString = "GameSize = "+GameSize+" and ";
        }
        String NumBotSQLString = "";
        if(!NBAny){
            NumBotSQLString = "where x.BotNumber = "+BotNumber;
        }
        String GameTypeSQLString = "";
        if(!GameType.equals("Any")){
            GameTypeSQLString = "and GameType=\""+GameType+"\"";
        }

        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement(
                "select GID from Game "+
                        "where "+GameSizeSQLString+"GID in ("+
                        "select GID from ("+
                        "select count(case when AID < 5 then 1 end) as BotNumber, GameSize, Game.GID "+
                        "from Game join Participation on (Participation.GID = Game.GID) group by Game.GID"+
                        ") x "+
                         NumBotSQLString +") and "+
                        "GameState=\"free\""+GameTypeSQLString+";"
        );
        ResultSet rs = stmt.executeQuery();
        if(rs.next()){
            int GID = rs.getInt(1);
            con.close();
            return GID;
        }else {
            con.close();
            return -1;
        }
    }


    public String getStartingLocation(int PID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select StartingLocation from Participation where PID="+PID+";");
        ResultSet rs = stmt.executeQuery();
        String Location = rs.getString("StartingLocation");
        con.close();
        return Location;
    }

    public boolean isOccupied(int AID) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select count(*) from Participation join Game on (Participation.GID=Game.GID) where AID="+AID+" and (GameState=\"free\" or GameState=\"playing\");");
        ResultSet rs = stmt.executeQuery();

        boolean Occupied;
        if(rs.getInt(1)==1){
            Occupied = true;
        }else{
            Occupied = false;
        }
        conn.close();
        return Occupied;
    }

    public int getAIDFromTID(int TID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select AID from Turn join Participation on (Turn.PID=Participation.PID) where TID="+TID+";");
        ResultSet rs = stmt.executeQuery();
        int AID = rs.getInt("AID");
        con.close();
        return AID;
    }

    //BUG FIX TDL 7 player 6 bots, player not getting 1st Turn
    //Setup A game, called by last player joining a Game
    public void setUpGame(int GID) throws SQLException, ParseException, InterruptedException {
        //Set Colours list
        ArrayList<String> Colours = new ArrayList<>();
        Colours.add("#FF0000");Colours.add("#00FF00");//Red,Green
        Colours.add("#0000FF");Colours.add("#FFFFFF");//Blue,White
        Colours.add("#FFFF00");Colours.add("#EE82EE");//Yellow,Violet
        Colours.add("#FFA500");Colours.add("#000000"); //Orange,Black

        //Set Starting Position list
        ArrayList<String> StartPos = new ArrayList<>();
        StartPos.add("0,16,0");StartPos.add("24,9,0");
        StartPos.add("18,0,0");StartPos.add("24,14,0");
        StartPos.add("7,23,0");StartPos.add("5,0,0");//Add correct starting position Row,Column
        StartPos.add("2,10,1");StartPos.add("12,5,1");

        //Open Database Connection
        Connection conn = openConnection();

        //Get useful Game attributes
        int GameSize = getGameSize(GID,conn);
        String GameType = getGameType(GID,conn);

        //Get all players in the game
        PreparedStatement getAIDPIDQuery = conn.prepareStatement("select AID,PID from Participation where GID="+GID+";");
        ResultSet rs = getAIDPIDQuery.executeQuery();
        ArrayList<Integer> AIDs= new ArrayList<>();
        ArrayList<Integer> PIDs= new ArrayList<>();
        while (rs.next()){
            int TempAID = rs.getInt("AID");
            AIDs.add(TempAID);
            int TempPID = rs.getInt("PID");
            PIDs.add(TempPID);
        }

        /*Game Order Setup*/
        //Random First Turn, BOTS CANNOT go FIRST so keep going to next player if chosen
        int Turn = rnd.nextInt(GameSize);
        while(AIDs.get(Turn)<=5){
            Turn = (Turn+1)%GameSize;
        }

        //Create a list to put Game Order, add first turn
        int[] Order = new int[GameSize];
        Order[0] = AIDs.get(Turn);

        //Create a copy of all AIDs, remove first turn
        ArrayList<Integer> AIDsCOPY = AIDs;
        AIDsCOPY.remove(Turn);

        //Shuffle remaining Account IDs and then set GameOrder
        Collections.shuffle(AIDsCOPY);
        for(int i=1;i<GameSize;i++){
            Order[i] = AIDs.get(i-1);
        }
        setGameOrder(GID,Arrays.toString(Order),conn);


        /*Location and Colour SETUP*/
        //Depending on type of game change allowed colours
        int colourLimit =6;
        if(GameType.equals("Big Game")){
            colourLimit =8;
        }

        //Colour and Starting position SQL prepared Statements
        String colourUpdateSQL = "update Participation set Colour=? where PID=?;";
        String startingLocationUpdateSQL = "update Participation set StartingLocation=? where PID=?;";

        PreparedStatement colourUpdate = conn.prepareStatement(colourUpdateSQL);
        PreparedStatement startingLocationUpdate = conn.prepareStatement(startingLocationUpdateSQL);

        String tempStoreOfLocation = "";

        //for every player give them a colour and a starting location
        for(int i = 0;i<GameSize;i++){
            int PID = PIDs.get(i);
            int Index = rnd.nextInt(colourLimit); //colourLimit is stops unwanted colours to be chosen

            colourUpdate.setString(1,Colours.get(Index));
            colourUpdate.setInt(2,PID);
            colourUpdate.addBatch();

            startingLocationUpdate.setString(1,StartPos.get(Index));
            startingLocationUpdate.setInt(2,PID);
            startingLocationUpdate.addBatch();

            //Get Location of 1st Player for first Location insert
            if(i == Turn){
                tempStoreOfLocation = StartPos.get(Index);
            }

            Colours.remove(Index);
            StartPos.remove(Index);
            colourLimit = colourLimit - 1;
        }

        //Add starting Turn and their starting Location
        PreparedStatement turnStartInsert = conn.prepareStatement("insert into Turn(\"TurnNumber\",\"PID\") values(0,"+PIDs.get(Turn)+");");
        turnStartInsert.executeUpdate();
        ResultSet generatedTID = turnStartInsert.getGeneratedKeys();
        PreparedStatement startingLocationInsert = conn.prepareStatement("insert into Location(\"TID\",\"Step\",\"Location\") values("+generatedTID.getInt(1)+",0,\""+tempStoreOfLocation+"\");");


        /*CARD SETUP*/
        //Set the amount of cards for each type of card according GameType
        int cardsinStackT1 = 6;
        int cardsinStackT2 = 9;
        if (GameType.equals("Big Game")){
            cardsinStackT1 = 10;
            cardsinStackT2 = 13;
        }

        //Create 3 types of decks
        Deck Suspects = new Deck(1,cardsinStackT1);
        Deck Weapons = new Deck(11,cardsinStackT1);
        Deck Rooms = new Deck(21,cardsinStackT2);

        //Choose Hidden Cards, these are removed from their decks
        int[] HiddenCards = new int[3];
        HiddenCards[0] = Suspects.giveCard();
        HiddenCards[1] = Weapons.giveCard();
        HiddenCards[2] = Rooms.giveCard();

        //Merge all 3 Decks
        Deck everyCard = new Deck(Suspects.returnList(), Weapons.returnList(), Rooms.returnList());

        //All Card SQL prepared Statements
        String hiddenCardInsertSQL = "insert into HiddenCards(\"GID\",\"CID\") values("+GID+",?);";
        String playerCardInsertSQL = "insert into PlayerCards(\"PID\",\"CID\") values(?,?);";
        String communityCardInsertSQL = "insert into CommunityCards(\"GID\",\"CID\") values("+GID+",?);";

        //Set Hidden Cards
        PreparedStatement hiddenCardsInsert = conn.prepareStatement(hiddenCardInsertSQL);
        for (int i = 0;i<3;i++){
            hiddenCardsInsert.setInt(1,HiddenCards[i]);
            hiddenCardsInsert.addBatch();
        }

        //Distribute Remaining Cards to each Player
        PreparedStatement playerCardsInsert = conn.prepareStatement(playerCardInsertSQL);
        while(everyCard.deckSize() >= GameSize){
            for(int i =0;i<GameSize; i++){
                int CID = everyCard.giveCard();
                playerCardsInsert.setInt(1, PIDs.get(i));
                playerCardsInsert.setInt(2,CID);
                playerCardsInsert.addBatch();
            }
        }

        //Set Community Cards
        PreparedStatement communityCardsInsert = conn.prepareStatement(communityCardInsertSQL);
        for(int i =0;i< everyCard.deckSize();){
            int CID = everyCard.giveCard();
            communityCardsInsert.setInt(1,CID);
            communityCardsInsert.addBatch();
        }


        //Set GameState which stops players joining
        PreparedStatement gameStateUpdate = conn.prepareStatement("update Game set GameState=\"playing\" where GID="+GID+";");

        //execute all Prepared Statements
        gameStateUpdate.executeUpdate();
        colourUpdate.executeBatch();
        startingLocationUpdate.executeBatch();
        startingLocationInsert.executeUpdate();
        hiddenCardsInsert.executeBatch();
        playerCardsInsert.executeBatch();
        communityCardsInsert.executeBatch();
        conn.close();

        //Create files for users to get Game information from
        createUpdateFiles(GID);
    }

    //Returns true if full, false if not full
    public boolean isGameFull(int GID) throws SQLException {
        Connection conn = openConnection();
        int GameSize = getGameSize(GID, conn);
        PreparedStatement stmt = conn.prepareStatement("select count(*) from Participation where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();

        boolean Full;
        Full = rs.getInt(1) == GameSize;
        conn.close();
        return Full;
    }

    public ArrayList<Integer> getPlayerCards(int PID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select CID from PlayerCards where PID="+PID+";");
        ResultSet rs = stmt.executeQuery();

        ArrayList<Integer> Cards = new ArrayList<>();
        while (rs.next()){
            Cards.add(Integer.parseInt(rs.getString("CID")));
        }
        con.close();
        return Cards;
    }

    public ArrayList<Integer> getCommunityCards(int GID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select CID from CommunityCards where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();

        ArrayList<Integer> Cards = new ArrayList<>();
        while (rs.next()){
            Cards.add(Integer.parseInt(rs.getString("CID")));
        }
        con.close();
        return Cards;
    }

    //[AID,Roll1,Roll2,TID]
    //Returns Dice Rolls of Current turn being played in the game, returns -1,-1,-1,-1 if GID is not found
    public ArrayList<Integer> getCurrentTurnInfoFromGID(int GID) throws SQLException {
        ArrayList<Integer> Rolls = new ArrayList<>();
        ArrayList<Integer> GameOrder = getGameOrder(GID);
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement(
        "select TID, AID, Roll1, Roll2 from Turn join Participation on (Turn.PID=Participation.PID) " +
            "where TurnNumber=(" +
                "select max(TurnNumber) as MaxTurn from Turn join Participation on (Turn.PID=Participation.PID) " +
                "where Participation.GID="+GID+
            ") and Participation.GID="+GID+";"
        );
        ResultSet rs = stmt.executeQuery();

        int tempRoll1 = -1;
        int tempRoll2 = -1;
        int tempAID = -1;
        int tempIndex = -1;
        int indexOfAID;
        int tempTID =-1;
        while (rs.next()){
            indexOfAID = GameOrder.indexOf(rs.getInt("AID"));
            if(indexOfAID > tempIndex){
                tempIndex = indexOfAID;
                tempAID = rs.getInt("AID");
                tempRoll1 = rs.getInt("Roll1");
                tempRoll2 = rs.getInt("Roll2");
                tempTID = rs.getInt("TID");
            }
        }
        con.close();
        Rolls.add(tempAID);
        Rolls.add(tempRoll1);
        Rolls.add(tempRoll2);
        Rolls.add(tempTID);
        return Rolls;
    }

    public void setRolls(int TID, int Roll1, int Roll2) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("update Turn set Roll1="+Roll1+", Roll2="+Roll2+" where TID="+TID+";");
        stmt.executeUpdate();
        con.close();
    }

    public ArrayList<ArrayList> getLocationsColoursWithAIDsFromGID(int GID) throws SQLException {
        ArrayList<ArrayList> data = new ArrayList<>();
        ArrayList<Integer> PIDs = getPIDs(GID);
        ArrayList<Integer> AIDs = getAIDsFromPIDs(PIDs);
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement(
        "select AID, Room, Location, Colour, PlayerName from ("+
                "select AID, x.TID, Room, Location, max(Step), Colour, PlayerName from Location x inner join ("+
                    "select Participation.AID as AID, TID, max(TurnNumber), Colour, PlayerName "+
                    "from Turn join Participation on (Turn.PID=Participation.PID) join Accounts on (Participation.AID=Accounts.AID)"+
                    "where Participation.GID="+GID+" "+
                    "group by Participation.AID"+
                ") y on (y.TID = x.TID) group by x.TID"+
            ") z join Turn on (Turn.TID=z.TID);"
        );
        ResultSet rs = stmt.executeQuery();

        while (rs.next()){
            ArrayList<String> Row = new ArrayList<>();
            AIDs.remove((Integer) rs.getInt("AID"));
            Row.add(Integer.toString(rs.getInt("AID")).trim());
            Row.add(Integer.toString(rs.getInt("Room")));
            Row.add(rs.getString("Location").trim());
            Row.add(rs.getString("Colour").trim());
            Row.add(rs.getString("PlayerName").trim());
            data.add(Row);
        }

        PreparedStatement stmt2 = con.prepareStatement(
        "select StartingLocation, Colour, PlayerName " +
            "from Participation join Accounts on (Participation.AID=Accounts.AID)" +
            "where Participation.AID = ? and GID="+GID+";"
        );

        for(int i=0;i<AIDs.size();i++){
            stmt2.setInt(1,AIDs.get(i));
            ResultSet temp = stmt2.executeQuery();
            temp.next();
            ArrayList<String> Row = new ArrayList<>();
            Row.add(String.valueOf(AIDs.get(i)).trim());
            Row.add(String.valueOf(0));
            Row.add(temp.getString("StartingLocation").trim());
            Row.add(temp.getString("Colour").trim());
            Row.add(temp.getString("PlayerName").trim());
            data.add(Row);
        }
        con.close();
        return data;
    }

    public int getStartingRoom(int TID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select Room from Location where TID="+TID+" and Step=0;");
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int Room = rs.getInt("Room");
        con.close();
        return Room;
    }

    //Create an Action list and return the actions for a given GID and AID
    //Ordered from present to past for turns and, start of Turn to end of Turn for Actions
    public ArrayList<ArrayList> getActionList(int GID, int PlayerAID) throws SQLException {
        //[[ClassOfAction,AID, OtherData...],[...],...]

        //Action Class                 //Other Data Format
        //0 - New Turn                 N/A                                    DONE   WORKS
        //1 - Dice Roll                [roll1,roll2]                          DONE   WORKS
        //2 - Question 2/3/..          [AID2,Response]                        DONE   WORKS  //No need to resend Cards since question will be the same
        //3 - Movement                 [in/out,Room]   out = 0, in = 1        DONE   WORKS
        //4 - Question                 [Card1,Card2,Card3,AID2,Response]      DONE   WORKS
        //5 - Guessed Hidden Cards     [Card1,Card2,Card3]
        //6 - End Game                 to be determined
        //7 - End Turn                 N/A                                    DONE    WORKS

        //Client does Colour using Board data already sent
        //Send CID not CardName, Client does Conversion

        ArrayList<ArrayList> ActionList = new ArrayList<>();
        int GameSize = getGameSize(GID);

        //get all TIDs from Game, Order them in descending order
        ArrayList<Integer> TIDs = getTIDs(GID);
        Collections.sort(TIDs);
        Collections.reverse(TIDs);

        int loops = GameSize;
        //If not every player has played yet, don't look for their Turns by reducing index
        if(TIDs.size() < GameSize){
            loops = TIDs.size();
        }

        //For a loop amount of Turns
        for (int i=0; i<loops;i++){
            ArrayList<ArrayList> Turn = new ArrayList<>();

            //Get current TID & AID
            int TID = TIDs.get(i);
            int AID = getAIDFromTID(TID);

            //New Turn
            ArrayList<Integer> NewTurn = new ArrayList<>();
            NewTurn.add(0);//Action Class
            NewTurn.add(AID);
            Turn.add(NewTurn);

            //Rolling the Dice
            ArrayList<Integer> Rolls = getRollsFromTID(TID);
            //If player has Rolled the dice add in Rolls to Action List
            if(Rolls.get(0)!=0){
                //ArrayList add moves elements to the right
                Rolls.add(0,AID);
                Rolls.add(0,1);
                Turn.add(Rolls);
            }

            //Room Movement
            int StartingRoom = getStartingRoom(TID);
            //[Room,Location,Step]
            ArrayList<String> CurrentLocation = getLocation(TID);
            int CurrentRoom = Integer.parseInt(CurrentLocation.get(0));
            int Step = Integer.parseInt(CurrentLocation.get(2));

            //If Player changes 'Room', going into a non-room space also counts as a change
            if(StartingRoom != CurrentRoom){
                ArrayList<Integer> RoomChange = new ArrayList<>();

                //0 is non-room
                if(StartingRoom == 0){
                    //Goes into Room
                    RoomChange.add(3);
                    RoomChange.add(AID);
                    RoomChange.add(1);
                    RoomChange.add(CurrentRoom);
                    Turn.add(RoomChange);
                }else if(CurrentRoom == 0){
                    //Goes out of Room
                    RoomChange.add(3);
                    RoomChange.add(AID);
                    RoomChange.add(0);
                    RoomChange.add(StartingRoom);
                    Turn.add(RoomChange);
                }else{
                    //Goes out then into a room
                    RoomChange.add(3);
                    RoomChange.add(AID);
                    RoomChange.add(0);
                    RoomChange.add(StartingRoom);
                    Turn.add(RoomChange);

                    ArrayList<Integer> RoomChange2 = new ArrayList<>();
                    RoomChange2.add(3);
                    RoomChange2.add(AID);
                    RoomChange2.add(1);
                    RoomChange2.add(CurrentRoom);
                    Turn.add(RoomChange2);

                }
            }

            //Questions
            ArrayList<ArrayList> Questions = getQuestionsFromTID(TID);

            for(int j=0; j<Questions.size();j++){
                ArrayList<Integer> Question = new ArrayList<>();
                int AnswererAID = getAIDFromPID((Integer) Questions.get(j).get(3));
                int Response = (Integer) Questions.get(j).get(4);

                //Only send Cards once, otherwise send only AID2 and Response
                if(j==0){
                    Question.add(4);
                    Question.add(AID);
                    Question.add((Integer) Questions.get(j).get(0));
                    Question.add((Integer) Questions.get(j).get(1));
                    Question.add((Integer) Questions.get(j).get(2));
                    Question.add(AnswererAID);
                }else{
                    Question.add(2);
                    Question.add(AID);
                    Question.add(AnswererAID);
                }

                //If Player is Answerer or Questioner show Response
                if(PlayerAID == AID || PlayerAID == AnswererAID){
                    Question.add(Response);
                }else{
                    //Other Players only see if a card was shown or not
                    if(Response!=0){
                        Question.add(1);
                    }else{
                        Question.add(0);
                    }
                }
                Turn.add(Question);
            }

            //End Turn
            if(i!=0){
                ArrayList<Integer> EndTurn = new ArrayList<>();
                EndTurn.add(7);
                EndTurn.add(AID);
                Turn.add(EndTurn);
            }

            //Add Turn To Action List
            ActionList.add(Turn);
        }
        return ActionList;
    }

    public void createUpdateFiles(int GID) throws SQLException, ParseException, InterruptedException {

        String templateString = "{" +
                "  \"Board\": []," +
                "  \"ActionList\": []," +
                "  \"Dices\": []," +
                "  \"PlayerCards\": []," +
                "  \"Turn\": -1, " +
                "  \"StepsLeft\": -1,"+
                "  \"Game\": {" +
                "      \"Name\": \"\"," +
                "      \"Size\": 0," +
                "      \"Type\": \"Normal Game\"," +
                "      \"CommunityCards\": []," +
                "      \"Order\": []," +
                "      \"State\": \"playing\"" +
                "  }" +
                "}";

        ArrayList<ArrayList> GameInfo = getGameInfo(GID);
        String GameState = (String) GameInfo.get(0).get(4);
        String GameType = (String) GameInfo.get(0).get(0);
        int GameSize = (int) GameInfo.get(0).get(1);
        String GameName = (String) GameInfo.get(0).get(2);
        ArrayList<Integer> GameOrder = getGameOrder(GID);

        //For every Player
        for (int PlayerIndex=0; PlayerIndex<GameOrder.size();PlayerIndex++){
            int AID = GameOrder.get(PlayerIndex);
            //If user is a Bot no need to create update Files
            if(AID>5) {
                int PID = getPID(AID, GID);

                //If no PID found or Game State isn't 'playing' then redirect to Home Page
                if ((PID == -1) || (!GameState.equals("playing"))) {
                    return;
                }

                //Get Current Turn AID + Die Rolls [AID,x,y,TID]
                ArrayList<Integer> DieRolls = getCurrentTurnInfoFromGID(GID);

                int CurrentStep = getStep(DieRolls.get(3));

                //Get Player Cards CID of Cards [x,y,..]
                ArrayList<Integer> PlayerCards = getPlayerCards(PID);

                //Get Community Cards CID of Cards [x,y,..]
                ArrayList<Integer> CommunityCards = getCommunityCards(GID);

                //Get Locations of each Player [[AID,Location,Colour],..]  AID is a String & Location is a String format: Row,Column,Floor
                ArrayList<ArrayList> LocationsColour = getLocationsColoursWithAIDsFromGID(GID);

                //Get Action List
                ArrayList<ArrayList> ActionList = getActionList(GID, AID);

                JSONObject ConstructNewUpdate;
                ConstructNewUpdate = (JSONObject) parser.parse(templateString);

                //Set Game Info
                ((JSONObject) ConstructNewUpdate.get("Game")).put("Name", GameName);
                ((JSONObject) ConstructNewUpdate.get("Game")).put("Size", GameSize);
                ((JSONObject) ConstructNewUpdate.get("Game")).put("Type", GameType);
                ((JSONObject) ConstructNewUpdate.get("Game")).put("Order", GameOrder);

                //Set Board
                for (int i = 0; i < LocationsColour.size(); i++) {
                    ((JSONArray) ConstructNewUpdate.get("Board")).add(LocationsColour.get(i));
                }

                //Set Die Rolls
                ((JSONArray) ConstructNewUpdate.get("Dices")).add(0, DieRolls.get(1));
                ((JSONArray) ConstructNewUpdate.get("Dices")).add(1, DieRolls.get(2));

                ConstructNewUpdate.put("Turn", DieRolls.get(0));

                //If this Player is playing
                if(DieRolls.get(0) == AID){
                    ConstructNewUpdate.put("StepsLeft", DieRolls.get(1) +  DieRolls.get(2) - CurrentStep);
                }

                //Set Player Cards
                for (int i = 0; i < PlayerCards.size(); i++) {
                    ((JSONArray) ConstructNewUpdate.get("PlayerCards")).add(i, PlayerCards.get(i));
                }

                //Set Community Cards
                for (int i = 0; i < CommunityCards.size(); i++) {
                    ((JSONArray) ((JSONObject) ConstructNewUpdate.get("Game")).get("CommunityCards")).add(i, CommunityCards.get(i));
                }

                //Set ActionList
                for (int i = 0; i <ActionList.size(); i++) {
                    ((JSONArray) ConstructNewUpdate.get("ActionList")).add(ActionList.get(i));
                }

                //Write Data
                writeJSONFile(GID, AID, ConstructNewUpdate);
            }
        }
    }

    //[TID,TurnNumber]
    public ArrayList<Integer> getTurn(int PID) throws SQLException {
        ArrayList<Integer> data = new ArrayList<>();
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select TID, max(TurnNumber) as TurnNumber from Turn where PID="+PID+";"); //TESTING
        ResultSet rs = stmt.executeQuery();

        //If PID has no TID then return [-1]
        rs.next();
        rs.getInt("TID");
        if(!rs.wasNull()) {
            data.add(rs.getInt("TID"));
            data.add(rs.getInt("TurnNumber"));
        }else{
            data.add(-1);
        }
        con.close();
        return data;
    }

    //[Roll1,Roll2]
    public ArrayList<Integer> getRollsFromTID(int TID) throws SQLException {
        ArrayList<Integer> data = new ArrayList<>();
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select Roll1, Roll2 from Turn where TID="+TID+";"); //TESTING
        ResultSet rs = stmt.executeQuery();
        data.add(rs.getInt("Roll1"));
        data.add(rs.getInt("Roll2"));
        con.close();
        return data;
    }

    //[Room,Location,Step]
    public ArrayList<String> getLocation(int TID) throws SQLException {
        ArrayList<String> data = new ArrayList<>();
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select Room, Location, max(Step) as Step from Location where TID="+TID+";");
        ResultSet rs = stmt.executeQuery();
        data.add(Integer.toString(rs.getInt("Room")));
        data.add(rs.getString("Location"));
        data.add(Integer.toString(rs.getInt("Step")));
        con.close();
        return data;
    }

    //checks if session attributes are correct, does Location step 0 and new Turn with TID
    public void createNewTurn(int AID, int GID) throws SQLException, ParseException, InterruptedException {
        //Wait
        TimeUnit.MILLISECONDS.sleep(500);

        //Check Session Attributes are correct
        int PID = getPID(AID,GID);
        if (PID != -1){

            //Grab Game Attributes
            int GameSize = getGameSize(GID);
            ArrayList<Integer> GameOrder = getGameOrder(GID);

            //Grab Index of Current Player in Game Order
            int PlayerIndex= GameOrder.indexOf(AID);

            //Grab Index of Next Player in Game Order + other info about them
            int newIndex = (PlayerIndex + 1)%GameSize;
            int newAID = GameOrder.get(newIndex);
            int newPID = getPID(newAID,GID);

            //Grab Old Values of Turn of Next Player
            ArrayList<Integer> OldTurnofNewPID = getTurn(newPID);
            int OldTID = OldTurnofNewPID.get(0); //-1 if no TID

            //Initialise variables
            int NewTurnNumber;
            ArrayList<String> OldLocation = new ArrayList<>();

            //If no Old Values
            if(OldTID == -1){
                NewTurnNumber = 0;
                String StartingLocation = getStartingLocation(newPID);
                OldLocation.add("0");
                OldLocation.add(StartingLocation);
            }else{
                NewTurnNumber = OldTurnofNewPID.get(1) + 1;
                OldLocation = getLocation(OldTID);
            }

            //Insert New Turn into Database
            Connection conn = openConnection();
            PreparedStatement turnStartInsert = conn.prepareStatement("insert into Turn(\"TurnNumber\",\"PID\") values("+NewTurnNumber+","+newPID+");");
            turnStartInsert.executeUpdate();
            ResultSet generatedTID = turnStartInsert.getGeneratedKeys();

            PreparedStatement LocationInsert = conn.prepareStatement(
                    "insert into Location(\"TID\",\"Room\",\"Location\",\"Step\") " +
                        "values(?,?,?,0);");

            LocationInsert.setInt(1,generatedTID.getInt(1));
            LocationInsert.setInt(2,Integer.parseInt(OldLocation.get(0)));
            LocationInsert.setString(3,OldLocation.get(1));

            LocationInsert.executeUpdate();
            conn.close();

            createUpdateFiles(GID);
            //If next user is a bot call the Bot
            if(newAID<=5){
                Bot NewBot = new Bot(GID);
                NewBot.Play();
            }
        }
    }

    public ArrayList<ArrayList> getQuestionsFromTID(int TID) throws SQLException {
        ArrayList<ArrayList> data = new ArrayList<>();
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select CardSuspect, CardWeapon, CardRoom, Answerer, Response from Question where TID="+TID+" order by QID ASC;");
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            ArrayList<Integer> temp = new ArrayList<>();
            temp.add(rs.getInt("CardSuspect"));
            temp.add(rs.getInt("CardWeapon"));
            temp.add(rs.getInt("CardRoom"));
            temp.add(rs.getInt("Answerer"));
            temp.add(rs.getInt("Response"));
            data.add(temp);
        }
        con.close();
        return data;
    }

    //Question Run
    public void Question(int AID, int GID, int CID1, int CID2, int CID3) throws SQLException, ParseException, InterruptedException {
        //Get Game Order
        ArrayList<Integer> GameOrder = getGameOrder(GID);
        int startIndex = GameOrder.indexOf(AID);
        int GameSize = GameOrder.size();
        int TID = getTurn(getPID(AID,GID)).get(0);

        //for loop in order from person after Questioner to person before Questioner
        for(int i = (startIndex + 1)%GameSize; i!=startIndex; i=(i+1)%GameSize){
            //Get PID
            int tempPID = getPID(GameOrder.get(i),GID);

            //Queries database and then inserts a Question accordingly
            boolean CardFound = QuestionPlayer(TID,tempPID,CID1,CID2,CID3);

            //Create Update Files
            createUpdateFiles(GID);

            //Wait
            TimeUnit.MILLISECONDS.sleep(6000);

            //Break if user has one of the cards
            if(CardFound){
                break;
            }
        }

        //create New Turn
        createNewTurn(AID,GID);
    }

    //Queries and inserts A proper response for 1 Question to 1 player, returns True if there was a Response false otherwise
    public boolean QuestionPlayer(int QuestionerTID, int AnswererPID, int CID1, int CID2, int CID3) throws SQLException {
        String CardsFormatted = CID1+","+CID2+","+CID3;
        boolean CardFound;
        Connection con = openConnection();
        PreparedStatement QueryStmt = con.prepareStatement("select CID from PlayerCards where PID="+AnswererPID+" and CID in ("+CardsFormatted+");");
        PreparedStatement InsertStmt = con.prepareStatement("insert into Question(\"TID\",\"CardSuspect\",\"CardWeapon\",\"CardRoom\",\"Answerer\",\"Response\") values("+QuestionerTID+","+CardsFormatted+","+AnswererPID+",?);");
        ResultSet rs = QueryStmt.executeQuery();

        if(rs.next()){
            InsertStmt.setInt(1,rs.getInt("CID"));
            CardFound = true;
        }else{
            InsertStmt.setInt(1,0);
            CardFound = false;
        }
        InsertStmt.executeUpdate();
        con.close();
        return CardFound;
    }

    public boolean PassageWay(int TID, int GID) throws SQLException, ParseException, InterruptedException {
        int[] nullLocation = {-1,-1,-1};

        //Get Current Location
        ArrayList<String> Location = getLocation(TID);
        int Room = Integer.parseInt(Location.get(0));
        int Step = Integer.parseInt(Location.get(2));

        //Get Rolls
        ArrayList<Integer> Rolls = getRollsFromTID(TID);
        int maxStep = Rolls.get(0) + Rolls.get(1);

        //If player hasn't rolled dice, make it so they can still change rooms
        if(maxStep == 0){
            maxStep = 1;
        }

        boolean validRoom = false;
        int NewRoom = 0;

        //If rooms are valid fill up steps and change rooms
        if(Room == 22){
            NewRoom = 26;
            validRoom = true;
        }else if(Room == 24){
            NewRoom = 29;
            validRoom = true;
        }else if(Room == 26){
            NewRoom = 22;
            validRoom = true;
        }else if(Room == 29){
            NewRoom = 24;
            validRoom = true;
        }

        if(validRoom){
            for (int i = 1; Step + i <= maxStep; i++) {
                movePlayer(TID, Step + i, nullLocation, NewRoom);
            }
            createUpdateFiles(GID);
        }
        return validRoom;
    }

    //Set Location to nullLocation and set Room to RoomCID for Step 1
    //+Fill out rest of Steps
    public void Jump(int TID, int RoomCID, int totalRoll) throws SQLException {
        for (int i = 1; i <= totalRoll; i++) {
            movePlayer(TID, i, new int[]{-1,-1,-1}, RoomCID);
        }
    }

    //Grab all Questions where the Response in Known (except when Answerer is Participant)
    //[[AnswererAID,SuspectCID,WeaponCID,RoomCID,Response]...]
    public ArrayList<ArrayList<Integer>> getQuestions(int PID, int GID) throws SQLException {
        ArrayList<ArrayList<Integer>> data = new ArrayList<>();
        Connection con = openConnection();
        //Grab QIDs where caller is Questioner
        //Grab All Questions in the game where there was no Response
        //Don't return Questions where the caller of this function is responding (they know their cards already)
        //Grab AnswererAID and their Response
        PreparedStatement stmt = con.prepareStatement(
        "select Participation.AID as AnswererAID, CardSuspect, CardWeapon, CardRoom, Response from Question " +
            "join Participation on (Participation.PID=Question.Answerer) " +
            "where (" +
                "QID in ("+
                    "select QID from Question " +
                    "join Turn on (Turn.TID=Question.TID)" +
                    "where Turn.PID="+PID+
                ") and Answerer!="+PID+
            ") or (Response = 0 and Answerer!="+PID+" and Participation.GID="+GID+");"
        );
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            ArrayList<Integer> temp = new ArrayList<>();
            temp.add(rs.getInt("AnswererAID"));
            temp.add(rs.getInt("CardSuspect"));
            temp.add(rs.getInt("CardWeapon"));
            temp.add(rs.getInt("CardRoom"));
            temp.add(rs.getInt("Response"));
            data.add(temp);
        }
        con.close();
        return data;
    }

    //Grab All Questions in the game where there was a Response and Participant was not part of Q/A (used to create Bot Memory Notes)
    //Doesn't return the Response
    public ArrayList<ArrayList<Integer>> getOtherQuestions(int PID, int GID) throws SQLException {
        ArrayList<ArrayList<Integer>> data = new ArrayList<>();
        Connection con = openConnection();
        //Grab All Questions in the game where there was a Response and Participant was not part of Q/A
        //Grab AnswererAID and Cards asked
        PreparedStatement stmt = con.prepareStatement(
        "select Participation.AID as AnswererAID, CardSuspect, CardWeapon, CardRoom from Question " +
            "join Participation on (Participation.PID=Question.Answerer) " +
            "join Turn on (Turn.TID=Question.TID)"+
            "where Answerer!="+PID+" and Turn.PID!="+PID+" and Response != 0 and Participation.GID = "+GID+";"
        );
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            ArrayList<Integer> temp = new ArrayList<>();
            temp.add(rs.getInt("AnswererAID"));
            temp.add(rs.getInt("CardSuspect"));
            temp.add(rs.getInt("CardWeapon"));
            temp.add(rs.getInt("CardRoom"));
            data.add(temp);
        }
        con.close();
        return data;
    }

    //ENDs GAME i.e. close + set Points
    public void GuessHiddenCards(int GID,int TID, int CID1, int CID2, int CID3) throws SQLException, ParseException, InterruptedException {
        ArrayList<Integer> HiddenCards = new ArrayList<>();
        Connection con = openConnection();
        PreparedStatement QueryStmt = con.prepareStatement("select CID from HiddenCards where GID="+GID+";");
        PreparedStatement UpdateStmt = con.prepareStatement(
            "insert into Question(\"TID\",\"CardSuspect\",\"CardWeapon\",\"CardRoom\",\"Answerer\",\"Response\") "+
                "values("+TID+","+CID1+","+CID2+","+CID3+",0,?);");

        ResultSet rs = QueryStmt.executeQuery();
        while(rs.next()){
            HiddenCards.add(rs.getInt("CID"));
        }
        Collections.sort(HiddenCards);

        boolean won;
        //If Guessed All Hidden Cards Correctly set Response as -1 otherwise -2 (negatives used to differentiate between Actual Questions)
        if(HiddenCards.get(0)==CID1 && HiddenCards.get(1)==CID2 && HiddenCards.get(2)==CID3){
            UpdateStmt.setInt(1,-1);
            UpdateStmt.executeUpdate();
            won = true;
        }else{
            UpdateStmt.setInt(1,-2);
            UpdateStmt.executeUpdate();
            won = false;
        }
        con.close();

        setGameState(GID,"finished");
        int AID = getAIDFromTID(TID);

        //Work out how points are distributed
        PointDistribution(GID,AID,won);

        ArrayList<Integer> GuessedCards = new ArrayList<>();
        GuessedCards.add(CID1);
        GuessedCards.add(CID2);
        GuessedCards.add(CID3);

        //Generate End Update Files
        createEndUpdateFiles(AID,GID,GuessedCards,HiddenCards,won);
    }

    //Generates the Update Files that tells the client the Game is Over and The Result
    public void createEndUpdateFiles(int guesserAID, int GID, ArrayList<Integer> GuessedCards, ArrayList<Integer> HiddenCards, boolean Won) throws SQLException, ParseException, InterruptedException {
        JSONParser parser = new JSONParser();
        String templateString = "{" +
                "  \"Board\": []," +
                "  \"PointsDistributed\": []," +
                "  \"Won\": false," +
                "  \"HiddenCards\": []," +
                "  \"Guesser\": -1, " +
                "  \"GameOrder\": [],"+
                "  \"GuessedCards\": []" +
                "}";


        String GameState = getGameState(GID);
        ArrayList<Integer> GameOrder = getGameOrder(GID);
        ArrayList<Integer> PointsDistributed = getPointsDistributed(GID);

        //For every Player
        for (int PlayerIndex=0; PlayerIndex<GameOrder.size();PlayerIndex++){
            int AID = GameOrder.get(PlayerIndex);
            //If user is a Bot no need to create update Files
            if(AID>5) {
                int PID = getPID(AID, GID);

                //If no PID found or Game State isn't 'finished' then return having done nothing
                if ((PID == -1) || (!GameState.equals("finished"))) {
                    return;
                }

                //Get Locations of each Player [[AID,Location,Colour],..]  AID is a String & Location is a String format: Row,Column,Floor
                ArrayList<ArrayList> LocationsColour = getLocationsColoursWithAIDsFromGID(GID);

                JSONObject ConstructNewUpdate;
                ConstructNewUpdate = (JSONObject) parser.parse(templateString);

                //Set Guesser
                ConstructNewUpdate.put("Guesser",guesserAID);

                //Set Outcome of Guess
                ConstructNewUpdate.put("Won",Won);

                //Set Game Order
                for (int i = 0; i < GameOrder.size(); i++) {
                    ((JSONArray) ConstructNewUpdate.get("GameOrder")).add(GameOrder.get(i));
                }

                //Set Points Distributed
                for (int i = 0; i < PointsDistributed.size(); i++) {
                    ((JSONArray) ConstructNewUpdate.get("PointsDistributed")).add(PointsDistributed.get(i));
                }

                //Set Board
                for (int i = 0; i < LocationsColour.size(); i++) {
                    ((JSONArray) ConstructNewUpdate.get("Board")).add(LocationsColour.get(i));
                }

                //Set Hidden Cards
                for (int i = 0; i < HiddenCards.size(); i++) {
                    ((JSONArray) ConstructNewUpdate.get("HiddenCards")).add(i, HiddenCards.get(i));
                }

                //Set Guessed Cards
                for (int i = 0; i < GuessedCards.size(); i++) {
                    ((JSONArray) ConstructNewUpdate.get("GuessedCards")).add(i, GuessedCards.get(i));
                }

                //Write Data
                writeJSONFile(GID, AID, ConstructNewUpdate);

                //Wait a reasonable amount of time, before deleting unnecessary data
                TimeUnit.SECONDS.sleep(90);

                //Remove Update Files
                Connection c = openConnection();
                deleteGameUpdateFiles(c,GID);
                c.close();
            }
        }
    }

    private void writeJSONFile(int GID, int AID, JSONObject constructNewUpdate) {
        boolean written = false;
        while (!written) {
            try {
                FileWriter myWriter = new FileWriter("src/main/java/Database/GameUpdateFiles/" + GID + "," + AID + ".txt");
                myWriter.write(String.valueOf(constructNewUpdate));
                myWriter.close();
                written = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Integer> getPointsDistributed(int GID) throws SQLException {
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select PointsDistributed from Game where GID="+GID+";");
        ResultSet rs = stmt.executeQuery();

        if(rs.next()){
            String[] GameOrder = rs.getString("PointsDistributed").replace("[","").replace("]","").split(",");
            con.close();
            ArrayList<Integer> GameOrderArrayList = new ArrayList<>();
            for (int i = 0; i < GameOrder.length; i++) {
                GameOrderArrayList.add(Integer.parseInt(GameOrder[i].trim()));
            }
            return GameOrderArrayList;
        }else{
            con.close();
            throw new SQLException();
        }
    }

    //Returns the Number of Points an AID currently has
    public int getPoints(int AID) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select Points from Accounts where AID="+AID+";");
        ResultSet rs = stmt.executeQuery();
        int Points = rs.getInt("Points");
        conn.close();
        return Points;
    }
    public void setPoints(int AID,int Points) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("update Accounts set Points="+Points+" where AID="+AID+";");
        stmt.executeUpdate();
        conn.close();
    }

    // Valid Strings: Wins, Losses, GamesPlayed, WrongGuesses
    private int getAccountGameEndings(int AID, String TypeofGameEnd) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("select "+TypeofGameEnd+" from Accounts where AID="+AID+";");
        ResultSet rs = stmt.executeQuery();
        int GameEnd = rs.getInt(TypeofGameEnd);
        conn.close();
        return GameEnd;
    }
    private void setAccountGameEndings(int AID, String TypeofGameEnd, int setGameEnd) throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("update Accounts set "+TypeofGameEnd+"="+setGameEnd+" where AID="+AID+";");
        stmt.executeUpdate();
        conn.close();
    }
    public void increaseAccountGameEndings(int AID, String TypeofGameEnd) throws SQLException {
        setAccountGameEndings(AID,TypeofGameEnd,getAccountGameEndings(AID,TypeofGameEnd)+1);
    }

    //Sets an Array to the Game showing the Change in points for each player
    private void setGamePoints(int GID, ArrayList<Integer> ChangePoints) throws SQLException {
        String FormattedChangePoints = Arrays.toString(ChangePoints.toArray());
        Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement("update Game set PointsDistributed=\""+FormattedChangePoints+"\" where GID="+GID+";");
        stmt.executeUpdate();
        conn.close();
    }

    private void PointDistribution(int GID, int GuesserAID, boolean won) throws SQLException {
        //Create Array that mimics Game Order Except shows the change in points
        ArrayList<Integer> ChangePoints = new ArrayList<>();

        ArrayList<Integer> GameOrder = getGameOrder(GID);
        int GameSize = GameOrder.size();

        int noofBots = 0;
        for(int i=0;i<GameSize;i++){
            if(GameOrder.get(i)<=5){
                noofBots = noofBots +1;
            }
        }

        int z = GameSize - noofBots;

        //Reward For Playing a Game
        int PlayingGameReward = 2;

        for(int i=0;i<GameSize;i++){
            int tempChangePoints;
            int CurrentPoints = getPoints(GameOrder.get(i));

            //if Gueseser got all the Hidden Cards Correct
            if(won){
                if(GameOrder.get(i)==GuesserAID){
                    tempChangePoints = ((int) ((40*Math.sqrt(z))/(1+(0.001*CurrentPoints)))) + PlayingGameReward;
                    ChangePoints.add(tempChangePoints);
                    increaseAccountGameEndings(GameOrder.get(i),"Wins");
                }else{
                    tempChangePoints = (100/(z+7)) + PlayingGameReward;
                    if(CurrentPoints < tempChangePoints){
                        ChangePoints.add(-1*CurrentPoints);
                    }else{
                        ChangePoints.add(-1*tempChangePoints);
                    }
                    increaseAccountGameEndings(GameOrder.get(i),"Losses");
                }
            }else{
                if(GameOrder.get(i)==GuesserAID){
                    tempChangePoints = PlayingGameReward - (10 * z);
                    if(CurrentPoints < tempChangePoints){
                        ChangePoints.add(-1*CurrentPoints);
                    }else{
                        ChangePoints.add(tempChangePoints);
                    }
                    increaseAccountGameEndings(GameOrder.get(i),"WrongGuesses");
                }else{
                    tempChangePoints = PlayingGameReward;
                    ChangePoints.add(tempChangePoints);
                }
            }

            //Increment amount of Games Played
            increaseAccountGameEndings(GameOrder.get(i),"GamesPlayed");

            //Update Individual Account Points
            setPoints(GameOrder.get(i),CurrentPoints+ChangePoints.get(i));
        }

        //Set Change Points of the Game
        setGamePoints(GID,ChangePoints);
    }

    //Returns an ArrayList filled with Player data in Order of Points (Descending)
    //Return a Blank ArrayList if no Players
    public ArrayList getLeaderBoard(int start) throws SQLException {
        ArrayList<ArrayList> data = new ArrayList();
        Connection con = openConnection();
        PreparedStatement stmt = con.prepareStatement("select PlayerName, Wins, Losses, WrongGuesses, GamesPlayed, Points from Accounts where AID > 5 order by Points DESC, Wins DESC, WrongGuesses ASC;");
        ResultSet rs = stmt.executeQuery();

        //Put pointer from before Row 1 to Row before {start}
        for(int i=1;i<start;i++){
            if(!rs.next()){
                break;
            }
        }

        //Grab Next 20 Players from and including the start Row
        for(int i=0;i<20;i++){
            if(!rs.next()){
                break;
            }
            ArrayList temp = new ArrayList();
            temp.add("\""+rs.getString("PlayerName")+"\"");
            temp.add(rs.getInt("Wins"));
            temp.add(rs.getInt("Losses"));
            temp.add(rs.getInt("WrongGuesses"));
            temp.add(rs.getInt("GamesPlayed"));
            temp.add(rs.getInt("Points"));
            data.add(temp);
        }
        con.close();
        return data;
    }
}
