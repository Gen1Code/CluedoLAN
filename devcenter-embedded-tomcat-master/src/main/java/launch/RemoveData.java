package launch;

import Database.Connect;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.RowId;
import java.sql.SQLException;
import java.util.ArrayList;

public class RemoveData {
    JSONParser parser = new JSONParser();
    public void RemoveData() throws SQLException, ParseException {

        Connect dbConn = new Connect("Rooms.db");
        ArrayList<String[]> result;
        //Grab every Room
        result = dbConn.query("select JSONString from Rooms;",new String[] {"JSONString"});
        //For every room turn into JSOn Object
        for(int i = 0; i<result.size();i++){
            JSONObject temp = (JSONObject) parser.parse(result.get(i)[0]);
            // Check if Room has finished the game IF it hasn't then delete that Room
            if(!(boolean) temp.get("GameFinished")){
                dbConn.Delete("Rooms","RoomID="+ temp.get("RoomID"));
            }
        }



    }
}
