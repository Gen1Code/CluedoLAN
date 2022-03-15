package servlet;

import Database.DBManager;
import org.json.simple.parser.ParseException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Bot {
    private final int GID;
    private final int AID;
    private final int TID;
    private final int PID;
    private int Rolled;
    private final int NoofPlayerCards;
    private int[] Location;
    private int Room;
    private int Step;
    private ArrayList<Integer> SuspectGoal = new ArrayList<>();
    private ArrayList<Integer> WeaponGoal = new ArrayList<>();
    private ArrayList<Integer> RoomGoal = new ArrayList<>();
    private ArrayList<Integer> GameOrder;
    private final String GameType;
    //Make a null Location
    int[] nullLocation = {-1,-1,-1};


    DBManager dbConn = new DBManager("Cluedo.db");
    Board board = new Board();
    Random rnd = new Random();

    public Bot(int givenGID) throws SQLException {
        GID = givenGID;
        ArrayList<Integer> Turn = dbConn.getCurrentTurnInfoFromGID(GID);
        AID = Turn.get(0);
        PID = dbConn.getPID(AID,GID);
        TID = Turn.get(3);

        ArrayList<String> temp = dbConn.getLocation(TID);
        Room = Integer.parseInt(temp.get(0));
        String[] LocationStringArray = temp.get(1).replaceAll(" ","").split(",");
        Location = new int[]{Integer.parseInt(LocationStringArray[0]), Integer.parseInt(LocationStringArray[1]), Integer.parseInt(LocationStringArray[2])};
        Step = 0;

        //Grab Game configuration
        GameOrder = dbConn.getGameOrder(GID);
        GameType = dbConn.getGameType(GID);
        NoofPlayerCards = dbConn.getPlayerCards(PID).size();
    }

    public void Play() throws SQLException, ParseException, InterruptedException {
        //Bot thinks about CARD Stuff
        //Returns true when Bot has figured out all the Cards
        if(!think()){
            safeTimeWait(300);

            //Roll Dice
            roll();

            safeTimeWait(500);

            //Move using Secret Passageway if possible
            if(!passageway()){
                //Move using Jump if possible
                if(Rolled == 2 || Rolled == 12){
                    jump();
                }else{//Move using Normal Movements
                    path();
                }
            }

            safeTimeWait(800);
            Question();
        }
    }

    private void Question() throws SQLException, ParseException, InterruptedException {
        ArrayList<String> Location = dbConn.getLocation(TID);
        int Room = Integer.parseInt(Location.get(0));

        //If bot is in a Room ask a Question
        if(Room != 0){
            int RandomSuspect = SuspectGoal.get(rnd.nextInt(SuspectGoal.size()));
            int RandomWeapon = WeaponGoal.get(rnd.nextInt(WeaponGoal.size()));

            dbConn.Question(AID,GID,RandomSuspect,RandomWeapon,Room);
        }else{
            dbConn.createNewTurn(AID,GID);
        }
    }

    private ArrayList<ArrayList<String>> blankArray(int Rows, int Columns){
        ArrayList<ArrayList<String>> blank = new ArrayList<>();
        for(int i = 0;i<Rows;i++){
            ArrayList<String> temp = new ArrayList<>();
            for(int j=0;j<Columns;j++){
                temp.add(" ");
            }
            blank.add(temp);
        }
        return blank;
    }

    //Returns true when Bot has figured out all the Cards else returns false
    private boolean think() throws SQLException, ParseException, InterruptedException {
        //States one per Cell
        // "X" - Confirmed not in their Hand
        // " " - Not Discovered
        // "L" - Confirmed in their Hand
        // "O" - Confirmed Hidden Card

        //Create 3 Matrices using Game Order AID as column headers
        int Columns = GameOrder.size();
        int RowsforSuspectandWeapon = 6;
        int RowsforRooms = 9;
        int OwnColumnIndex = GameOrder.indexOf(AID);

        if(GameType.equals("Big Game")){
            RowsforSuspectandWeapon = 10;
            RowsforRooms = 13;
        }
        //Suspect Matrix
        ArrayList<ArrayList<String>> SuspectsMemory = blankArray(RowsforSuspectandWeapon,Columns);

        //Weapon Matrix
        ArrayList<ArrayList<String>> WeaponsMemory = blankArray(RowsforSuspectandWeapon,Columns);

        //Room Matrix
        ArrayList<ArrayList<String>> RoomsMemory = blankArray(RowsforRooms,Columns);

        //Add Community Cards to the Matrices
        ArrayList<Integer> CommunityCards = dbConn.getCommunityCards(GID);
        for(int i=0;i<CommunityCards.size();i++){
            int Row;
            if(CommunityCards.get(i)<=10){
                Row = CommunityCards.get(i) - 1;
                AddCard(SuspectsMemory,Row,OwnColumnIndex);
            }else if(CommunityCards.get(i)<=20){
                Row = CommunityCards.get(i) - 11;
                AddCard(WeaponsMemory,Row,OwnColumnIndex);
            }else{
                Row = CommunityCards.get(i)-21;
                AddCard(RoomsMemory,Row,OwnColumnIndex);
            }
        }

        //Add own Bot's Cards to the Matrices
        ArrayList<Integer> OwnCards = dbConn.getPlayerCards(PID);
        for(int i=0;i<OwnCards.size();i++){
            int Row;
            if(OwnCards.get(i)<=10){
                Row = OwnCards.get(i) - 1;
                AddCard(SuspectsMemory,Row,OwnColumnIndex);
            }else if(OwnCards.get(i)<=20){
                Row = OwnCards.get(i) - 11;
                AddCard(WeaponsMemory,Row,OwnColumnIndex);
            }else{
                Row = OwnCards.get(i)-21;
                AddCard(RoomsMemory,Row,OwnColumnIndex);
            }
        }

        //Pad out own Bot's Column with X's
        for(int j=0;j<SuspectsMemory.size();j++){
            NoCard(SuspectsMemory,j,OwnColumnIndex);
            NoCard(WeaponsMemory,j,OwnColumnIndex);
        }
        for(int j=0;j<RoomsMemory.size();j++){
            NoCard(RoomsMemory,j,OwnColumnIndex);
        }

        //From the Questions asked change Matrices (Only Questions where there is a known Response)
        //[[AnswererAID,SuspectCID,WeaponCID,RoomCID,Response]...]
        ArrayList<ArrayList<Integer>> Questions = dbConn.getQuestions(PID,GID);
        for(int i=0;i<Questions.size();i++){
            ArrayList<Integer> Question = Questions.get(i);
            int Column = GameOrder.indexOf(Question.get(0));
            int Response = Question.get(4);
            if(Response !=0){
                int Row;
                if(Response<=10){
                    Row = Response - 1;
                    AddCard(SuspectsMemory,Row,Column);
                }else if(Response<=20){
                    Row = Response - 11;
                    AddCard(WeaponsMemory,Row,Column);
                }else{
                    Row = Response-21;
                    AddCard(RoomsMemory,Row,Column);
                }
            }else{
                NoCard(SuspectsMemory,Question.get(1)-1,Column);
                NoCard(WeaponsMemory,Question.get(2)-11,Column);
                NoCard(RoomsMemory,Question.get(3)-21,Column);
            }
        }

        //Get Notes using other Questions where Bot doesn't know Response
        ArrayList<ArrayList<Integer>> Notes = dbConn.getOtherQuestions(PID,GID);

        //Check Notes anything Bot can work out
        checkNotes(SuspectsMemory,WeaponsMemory,RoomsMemory, Notes);

        //Fill out X's in columns where all Cards are Known
        checkMaxCardsForPlayer(SuspectsMemory,WeaponsMemory,RoomsMemory);

        //Check For Hidden Cards
        checkForHiddenCards(SuspectsMemory,WeaponsMemory,RoomsMemory);

        System.out.println(SuspectsMemory);
        System.out.println(WeaponsMemory);
        System.out.println(RoomsMemory);
        System.out.println(Notes);

        //If all Cards are found Guess the Cards
        if(SuspectGoal.size() == 1 && WeaponGoal.size() == 1 && RoomGoal.size() == 1){
            System.out.println("This Bot Has Discovered all the Hidden Cards!");
            System.out.println(Arrays.toString(new int[]{SuspectGoal.get(0), WeaponGoal.get(0), RoomGoal.get(0)}));

            //Guess Hidden Cards
            dbConn.GuessHiddenCards(GID,TID,SuspectGoal.get(0),WeaponGoal.get(0),RoomGoal.get(0));

            return true;
        }else{
            if(SuspectGoal.size()==1){
                Optional<Integer> SuspectCommunityCards = CommunityCards.stream().filter(x -> x <= 10).findFirst();
                if(SuspectCommunityCards.isPresent()){
                    SuspectGoal.set(0,SuspectCommunityCards.get());
                }else{
                    Optional<Integer> SuspectOwnCards = OwnCards.stream().filter(x -> x <= 10).findFirst();
                    SuspectOwnCards.ifPresent(integer -> SuspectGoal.set(0, integer));
                }
            }
            if(WeaponGoal.size()==1){
                Optional<Integer> WeaponCommunityCards = CommunityCards.stream().filter(x -> x > 10 && x <= 20).findFirst();
                if(WeaponCommunityCards.isPresent()){
                    WeaponGoal.set(0,WeaponCommunityCards.get());
                }else{
                    Optional<Integer> WeaponOwnCards = OwnCards.stream().filter(x -> x > 10 && x <= 20).findFirst();
                    WeaponOwnCards.ifPresent(integer -> WeaponGoal.set(0, integer));
                }
            }
            //Different for Room, since you want Bot to stay in a Room that doesn't obstruct asking Questions (since Bot can't choose which room to ask in a question)
            if(RoomGoal.size()==1){
                Stream<Integer> RoomCommunityCards = CommunityCards.stream().filter(x -> x > 20);
                Stream<Integer> RoomOwnCards = OwnCards.stream().filter(x -> x > 20);
                RoomCommunityCards.forEach(integer -> RoomGoal.add(integer));
                RoomOwnCards.forEach(integer -> RoomGoal.add(integer));
            }
            return false;
        }
    }

    private void checkForHiddenCards(
            ArrayList<ArrayList<String>> SuspectMemory, ArrayList<ArrayList<String>> WeaponMemory, ArrayList<ArrayList<String>> RoomMemory
    ){

        //Set Goals First so that they can be overwritten Later if a Hidden Card is found
        setGoals(SuspectMemory,WeaponMemory,RoomMemory);

        //If the whole Row is Marked as X's then that Row is a Hidden Card
        for(int i=0;i<SuspectMemory.size();i++){
            if(!SuspectMemory.get(i).contains(" ") && !SuspectMemory.get(i).contains("L") && !SuspectMemory.get(i).contains("O")){
                AddHiddenCard(SuspectMemory,i);
                SuspectGoal.clear();
                SuspectGoal.add(i+1);
            }
            if (!WeaponMemory.get(i).contains(" ") && !WeaponMemory.get(i).contains("L") && !WeaponMemory.get(i).contains("O")){
                AddHiddenCard(WeaponMemory,i);
                WeaponGoal.clear();
                WeaponGoal.add(i+11);
            }
        }
        for(int i=0;i<RoomMemory.size();i++) {
            if (!RoomMemory.get(i).contains(" ") && !RoomMemory.get(i).contains("L") && !RoomMemory.get(i).contains("O")){
                AddHiddenCard(RoomMemory,i);
                RoomGoal.clear();
                RoomGoal.add(i+21);
            }
        }


        //If Y-1 Cards are Found then the last Card is the Hidden Card, check if a Hidden Card has already been found aswell
        ArrayList<Integer> SuspectCardsNotFound = new ArrayList<>();
        ArrayList<Integer> WeaponCardsNotFound = new ArrayList<>();
        ArrayList<Integer> RoomCardsNotFound = new ArrayList<>();
        for(int i=0;i<SuspectMemory.size();i++){
            if(SuspectMemory.get(i).contains(" ") || SuspectMemory.get(i).contains("O")){
                SuspectCardsNotFound.add(i);
            }
            if (WeaponMemory.get(i).contains(" ") || WeaponMemory.get(i).contains("O")){
                WeaponCardsNotFound.add(i);
            }
        }
        for(int i=0;i<RoomMemory.size();i++) {
            if(RoomMemory.get(i).contains(" ") || RoomMemory.get(i).contains("O")){
                RoomCardsNotFound.add(i);
            }
        }

        if(SuspectCardsNotFound.size() == 1){
            AddHiddenCard(SuspectMemory,SuspectCardsNotFound.get(0));
        }
        if(WeaponCardsNotFound.size() == 1){
            AddHiddenCard(WeaponMemory,WeaponCardsNotFound.get(0));
        }
        if(RoomCardsNotFound.size() == 1){
            AddHiddenCard(RoomMemory,RoomCardsNotFound.get(0));
        }
    }

    //When adding a Hidden Card Also check that you can find other cards
    private void AddHiddenCard(ArrayList<ArrayList<String>> Memory, int Row){
        //Fill Hidden Card Row
        for(int i=0; i<Memory.get(Row).size();i++){
            Memory.get(Row).set(i,"O");
        }

        //If x-1 'X' in a row then the column with 0 is turned to 1 as Hidden Card in this Memory table is already found
        for(int i=0;i<Memory.size();i++){
            int counter = 0;
            int IndexOfNotDiscovered =-1;
            for(int j=0;j<Memory.get(0).size();j++){
                if(Memory.get(i).get(j).equals("X")){
                    counter = counter +1;
                }else if(Memory.get(i).get(j).equals(" ")){
                    IndexOfNotDiscovered = j;
                }else{
                    counter = 0;
                    break;
                }
            }
            if (counter == Memory.get(0).size()-1){
                AddCard(Memory,i,IndexOfNotDiscovered);
            }
        }
    }

    //Puts X's in other Players spaces, and a 'L' in correct space
    private void AddCard(ArrayList<ArrayList<String>> Memory, int Row,int Column){
        //Fill Row
        for(int i=0; i<Memory.get(Row).size();i++){
            Memory.get(Row).set(i,"X");
        }
        //Put 1 in correct space
        Memory.get(Row).set(Column,"L");
    }

    private void NoCard(ArrayList<ArrayList<String>> Memory, int Row,int Column){
        //Put X in correct space if it isn't confirmed " "
        if(Memory.get(Row).get(Column).equals(" ")) {
            Memory.get(Row).set(Column,"X");
        }
    }

    private void checkMaxCardsForPlayer(
            ArrayList<ArrayList<String>> SuspectMemory, ArrayList<ArrayList<String>> WeaponMemory, ArrayList<ArrayList<String>> RoomMemory
    ){
        //For each Column
        for(int i=0; i<SuspectMemory.get(0).size();i++){
            //Count the different amount of cards, for that column
            int CardsDiscovered = 0;
            int HiddenCardsDiscovered = 0;
            int NotTheirCardDiscovered = 0;
            for(int j=0;j<SuspectMemory.size();j++){
                if(SuspectMemory.get(j).get(i).equals("L")){
                    CardsDiscovered = CardsDiscovered +1;
                }else if(SuspectMemory.get(j).get(i).equals("X")){
                    NotTheirCardDiscovered = NotTheirCardDiscovered + 1;
                }else if(SuspectMemory.get(j).get(i).equals("O")){
                    HiddenCardsDiscovered = HiddenCardsDiscovered+1;
                }

                if(WeaponMemory.get(j).get(i).equals("L")){
                    CardsDiscovered = CardsDiscovered +1;
                }else if(WeaponMemory.get(j).get(i).equals("X")){
                    NotTheirCardDiscovered = NotTheirCardDiscovered + 1;
                }else if(WeaponMemory.get(j).get(i).equals("O")){
                    HiddenCardsDiscovered = HiddenCardsDiscovered+1;
                }
            }
            for(int j=0;j<RoomMemory.size();j++){
                if(RoomMemory.get(j).get(i).equals("L")){
                    CardsDiscovered = CardsDiscovered +1;
                }else if(RoomMemory.get(j).get(i).equals("X")){
                    NotTheirCardDiscovered = NotTheirCardDiscovered + 1;
                }else if(RoomMemory.get(j).get(i).equals("O")){
                    HiddenCardsDiscovered = HiddenCardsDiscovered+1;
                }
            }

            //If Bot knows all the cards in that column's AID Hand, then put X's in all rest of the space
            if(CardsDiscovered == NoofPlayerCards){
                for(int j=0;j<SuspectMemory.size();j++){
                    NoCard(SuspectMemory,j,i);
                    NoCard(WeaponMemory,j,i);
                }
                for(int j=0;j<RoomMemory.size();j++){
                    NoCard(RoomMemory,j,i);
                }
            }

            //If Bot knows that the Column's AID Hand doesn't have other cards then put L's in the remaining spaces
            int AmountofCards = SuspectMemory.size()+WeaponMemory.size()+RoomMemory.size();
            if(((AmountofCards - HiddenCardsDiscovered) - NotTheirCardDiscovered) == NoofPlayerCards){
                for(int j=0;j<SuspectMemory.size();j++){
                    if(SuspectMemory.get(j).get(i).equals(" ")){
                        AddCard(SuspectMemory,j,i);
                    }
                    if(WeaponMemory.get(j).get(i).equals(" ")){
                        AddCard(WeaponMemory,j,i);
                    }
                }
                for(int j=0;j<RoomMemory.size();j++){
                    if(RoomMemory.get(j).get(i).equals(" ")){
                        AddCard(RoomMemory,j,i);
                    }
                }
            }
        }
    }

    //Check Notes for Additional findings of Cards
    private void checkNotes(
            ArrayList<ArrayList<String>> SuspectMemory, ArrayList<ArrayList<String>> WeaponMemory, ArrayList<ArrayList<String>> RoomMemory, ArrayList<ArrayList<Integer>> Notes
    ){
        for(int i=0; i<Notes.size();i++) {
            int Column = GameOrder.indexOf(Notes.get(i).get(0));
            int Row1 = Notes.get(i).get(1)-1;
            int Row2 = Notes.get(i).get(2)-11;
            int Row3 = Notes.get(i).get(3)-21;
            int CardsConfirmedNot = 0;
            int CardUndiscoveredType = -1;

            if(SuspectMemory.get(Row1).get(Column).equals("L")){
                continue;
            }else if(SuspectMemory.get(Row1).get(Column).equals(" ")){
                CardUndiscoveredType = 1;
            }else{//X or O
                CardsConfirmedNot +=1;
            }

            if(WeaponMemory.get(Row2).get(Column).equals("L")){
                continue;
            }else if(WeaponMemory.get(Row2).get(Column).equals(" ")){
                CardUndiscoveredType = 2;
            }else{
                CardsConfirmedNot +=1;
            }

            if(RoomMemory.get(Row3).get(Column).equals("L")){
                continue;
            }else if(RoomMemory.get(Row3).get(Column).equals(" ")){
                CardUndiscoveredType = 3;
            }else{
                CardsConfirmedNot +=1;
            }

            //If 2 Cards have been Confirmed not in their Hand then last Card is owned by them
            if(CardsConfirmedNot == 2){
                int Row;
                ArrayList<ArrayList<String>> Memory;
                if(CardUndiscoveredType==1){
                    Row = Row1;
                    Memory = SuspectMemory;
                }else if(CardUndiscoveredType==2){
                    Row = Row2;
                    Memory = WeaponMemory;
                }else{
                    Row = Row3;
                    Memory = RoomMemory;
                }
                AddCard(Memory,Row,Column);
            }
        }
    }

    private void setGoals(ArrayList<ArrayList<String>> SuspectsMemory, ArrayList<ArrayList<String>> WeaponsMemory, ArrayList<ArrayList<String>> RoomsMemory){
        for(int i=0;i<SuspectsMemory.size();i++){
            if(!SuspectsMemory.get(i).contains("L")){
                SuspectGoal.add(i+1);
            }
            if(!WeaponsMemory.get(i).contains("L")){
                WeaponGoal.add(i+11);
            }
        }
        for(int i=0;i<RoomsMemory.size();i++){
            if(!RoomsMemory.get(i).contains("L")){
                RoomGoal.add(i+21);
            }
        }
    }

    private void safeTimeWait(int millisecs){
        try{
            TimeUnit.MILLISECONDS.sleep(millisecs);
        }catch (InterruptedException ignore){}
    }

    //Only runs if bot needs to get in a room
    private void roll() throws SQLException, ParseException, InterruptedException {
        //Generate Rolls
        int Roll1 = rnd.nextInt(6) +1;
        int Roll2 = rnd.nextInt(6) +1;

        Rolled = Roll1 + Roll2;

        //Set Rolls
        dbConn.setRolls(TID,Roll1,Roll2);

        //Create Update Files
        dbConn.createUpdateFiles(GID);
    }

    //Bot jumps to a new room
    private void jump() throws SQLException {
        dbConn.Jump(TID,RoomGoal.get(rnd.nextInt(RoomGoal.size())),Rolled);
    }

    private boolean passageway() throws SQLException, ParseException, InterruptedException {
        boolean validRoom = false;
        int NewRoom =-1;
        boolean passageUsed = false;

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

        if(validRoom && RoomGoal.contains(NewRoom) && !RoomGoal.contains(Room)){
            dbConn.PassageWay(TID,GID);
            passageUsed = true;
        }
        return passageUsed;
    }

    private void path() throws SQLException, ParseException, InterruptedException {
        ArrayList<String> path = new ArrayList<>();
        int sizeofPath = 200;
        if(Room==0){
            //For all Targeted Rooms find the shortest path
            for(int i=0;i<RoomGoal.size();i++){
                ArrayList<String> tempPath = board.AStarPathFromLocation(Location,RoomGoal.get(i));
                if(tempPath.size()<=Rolled){
                    path = tempPath;
                    break;
                }else if(tempPath.size()<sizeofPath){
                    path = tempPath;
                    sizeofPath = tempPath.size();
                }
            }

            for(int i=0;i<Rolled && i<path.size();i++){
                safeTimeWait(750);
                move(path.get(i));
            }
        }else if(!RoomGoal.contains(Room)){//If bot isn't already in a Room they want
            //For all Targeted Rooms find the shortest path
            for(int i=0;i<RoomGoal.size();i++){
                ArrayList<String> tempPath = board.AStarPathFromRoom(Room,RoomGoal.get(i));
                if(tempPath.size()<=Rolled+1){//+1 due to Entrance prepended in array
                    path = tempPath;
                    break;
                }else if(tempPath.size()<sizeofPath){
                    path = tempPath;
                    sizeofPath = tempPath.size();
                }
            }

            String[] LocationStringArray = path.get(0).replaceAll(" ","").replaceAll("\\[","").replaceAll("]","").split(",");
            Location = new int[]{Integer.parseInt(LocationStringArray[0]), Integer.parseInt(LocationStringArray[1]), Integer.parseInt(LocationStringArray[2])};

            for(int i=1;i<=Rolled && i<path.size();i++){
                safeTimeWait(750);
                move(path.get(i));
            }
        }
    }

    //move the Bot
    //Direction ArrowUp, ArrowDown etc..
    private void move(String direction) throws SQLException, ParseException, InterruptedException {
        int[] NewLocation = board.ChangeLocation(direction,Location[0], Location[1], Location[2]);

        if((NewLocation[2] == 1 && GameType.equals("Big Game")) || NewLocation[2] == 0){
            dbConn.movePlayer(TID,Step+1,NewLocation,0);

            //Update Bot Attributes
            Step = Step + 1;
            Location = NewLocation;

            //Generate Update Files
            dbConn.createUpdateFiles(GID);

        }else if(NewLocation[2] == -1) {//If user goes into a room
            //Set Room (i=1)
            //+Fill rest of Steps of Turn, since once a player enters a room they aren't allowed to leave in that turn
            for (int i = 1; Step + i <= Rolled; i++) {
                dbConn.movePlayer(TID, Step + i, nullLocation, NewLocation[0]);
            }

            //Update Bot Attributes
            Location = nullLocation;
            Step = Rolled;

            //Generate Update Files
            dbConn.createUpdateFiles(GID);
        }
    }
}
