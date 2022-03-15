package servlet;

import java.util.ArrayList;
import java.util.Arrays;

public class Board {
    public Board(){}
    //Character Codes, Entrances are as if going from free space into the room
    //x  Stair Unique Code (0-9)
    //D  Down Entrance   //,CID for all entrances
    //L  Left Entrance
    //R  Right Entrance
    //U  Up Entrance
    //-  Free Space
    //#  Blocked Space

    private final String[][] Board = {
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","#"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"#"   ,"-"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"-"   ,"-"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"-"   ,"-"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"U,29","-","-"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"-"   ,"-"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","-"   ,"-","-"   ,"-","-"   ,"-"   ,"-","-"   ,"R,21","#","#"   ,"#"   ,"#","#"   ,"-"   ,"-"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"-","-"   ,"-","-"   ,"-","-"   ,"-"   ,"-","-"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"-"   ,"-"   ,"U,22","#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"-"   ,"-","-"   ,"#"   ,"#","U,21","U,21","#","#"   ,"-"   ,"-"   ,"-"   ,"-","-"   ,"-","-","-","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"-"   ,"-","-"   ,"-"   ,"-","-"   ,"-"   ,"-"   ,"-"   ,"-","-"   ,"-","-","-","-"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"L,28","-","-"   ,"-"   ,"-","-"   ,"-"   ,"-","-"   ,"-"   ,"-"   ,"-"   ,"-","-"   ,"-","-","-","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"#"   ,"0","1"   ,"2"   ,"#","-"   ,"-"   ,"#"   ,"D,23","#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","U,28","#","#"   ,"-"   ,"-","-"   ,"#"   ,"#","#"   ,"#"   ,"#","-"   ,"-"   ,"#"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","-"   ,"-","-"   ,"-","-"   ,"-"   ,"-","-"   ,"#"   ,"#","#"   ,"#"   ,"#","-"   ,"-"   ,"R,23","#"   ,"#","#"   ,"#","#","#","#"},
    {"#","D,27","#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"#"   ,"#","#"   ,"#"   ,"#","-"   ,"-"   ,"#"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"#"   ,"#","#"   ,"#"   ,"#","-"   ,"-"   ,"#"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"#"   ,"3","4"   ,"5"   ,"#","-"   ,"-"   ,"#"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"L,27","-","-"   ,"-"   ,"-","-"   ,"-"   ,"-","-"   ,"-"   ,"-"   ,"-"   ,"-","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"-"   ,"-","-"   ,"-"   ,"-","-"   ,"-"   ,"-"   ,"-"   ,"-","-"   ,"-","-","-","#"},
    {"#","-"   ,"-","-"   ,"-","-"   ,"-"   ,"-","#"   ,"D,25","#","#"   ,"#"   ,"#","D,25","#"   ,"-"   ,"-"   ,"-","-"   ,"-","-","-","-"},
    {"-","-"   ,"-","-"   ,"-","-"   ,"-"   ,"-","#"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"#"   ,"-"   ,"-"   ,"#","D,24","#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","-"   ,"-"   ,"-","R,25","#"   ,"#","#"   ,"#"   ,"#","#"   ,"L,25","-"   ,"-"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","D,26","-"   ,"-","#"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"#"   ,"-"   ,"-"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"-"   ,"-","#"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"#"   ,"-"   ,"-"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"-"   ,"-","#"   ,"#"   ,"#","#"   ,"#"   ,"#","#"   ,"#"   ,"-"   ,"-"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"-","-"   ,"-"   ,"#","#"   ,"#"   ,"#","-"   ,"-"   ,"-"   ,"#"   ,"#","#"   ,"#","#","#","#"},
    {"#","#"   ,"#","#"   ,"#","#"   ,"#"   ,"#","#"   ,"-"   ,"#","#"   ,"#"   ,"#","-"   ,"#"   ,"#"   ,"#"   ,"#","#"   ,"#","#","#","#"}
    };

    private final String[][] Floor1 = {
    {"#","#","#"   ,"#"   ,"#"   ,"#"   ,"#"   ,"#","#","#","#"},
    {"#","#","U,31","U,31","#"   ,"#"   ,"#"   ,"#","#","#","#"},
    {"-","-","-"   ,"-"   ,"-"   ,"-"   ,"-"   ,"-","-","-","-"},
    {"-","-","-"   ,"-"   ,"-"   ,"R,33","#"   ,"#","#","#","#"},
    {"#","#","#"   ,"6"   ,"-"   ,"#"   ,"#"   ,"#","#","#","#"},
    {"#","#","#"   ,"7"   ,"-"   ,"#"   ,"#"   ,"#","#","#","#"},
    {"#","#","#"   ,"8"   ,"-"   ,"#"   ,"#"   ,"#","#","#","#"},
    {"#","#","#"   ,"9"   ,"-"   ,"-"   ,"R,32","#","#","#","#"},
    {"-","-","-"   ,"-"   ,"-"   ,"-"   ,"#"   ,"#","#","#","#"},
    {"#","#","#"   ,"#"   ,"D,30","-"   ,"#"   ,"#","#","#","#"},
    {"#","#","#"   ,"#"   ,"#"   ,"-"   ,"#"   ,"#","#","#","#"},
    {"#","#","#"   ,"#"   ,"#"   ,"-"   ,"#"   ,"#","#","#","#"},
    {"#","#","#"   ,"#"   ,"#"   ,"-"   ,"#"   ,"#","#","#","#"}
    };

    int BoardRowMax = Board.length - 1;
    int BoardColMax = Board[0].length - 1;

    int FloorRowMax = Floor1.length - 1;
    int FloorColMax = Floor1[0].length - 1;

    //Entrances[CID-21][No. of Exit][row/col/floor]
    final int[][][] Entrances = {
        {{4,9,0},{6,11,0},{6,12,0}}, //Hall
        {{5,17,0}}, //Lounge
        {{9,17,0},{11,16,0}},//Dining Room
        {{18,19,0}}, //Kitchen
        {{19,8,0},{17,9,0},{17,14,0},{19,15,0}},//Ball Room
        {{20,5,0}}, //Conservatory
        {{12,1,0},{15,6,0}},//Billiard Room
        {{8,6,0},{10,3,0}},//Library
        {{3,6,0}},//Study
        {{9,4,1}},//Bedroom
        {{1,2,1},{1,2,1}},//Balcony
        {{7,6,1}},//Fitness Room
        {{3,5,1}}//Play Room
    };

    //Returns the Location of the Stair space provided
    public int[] StairIDLocation(String StairID){
        if(StairID.equals("0")){
            return new int[]{9, 10, 0};
        }else if(StairID.equals("1")){
            return new int[]{9, 11, 0};
        }else if(StairID.equals("2")){
            return new int[]{9, 12, 0};
        }else if(StairID.equals("3")){
            return new int[]{14, 10, 0};
        }else if(StairID.equals("4")){
            return new int[]{14, 11, 0};
        }else if(StairID.equals("5")){
            return new int[]{14, 12, 0};
        }else if(StairID.equals("6")){
            return new int[]{4, 3, 1} ;
        }else if(StairID.equals("7")){
            return new int[]{5, 3, 1};
        }else if(StairID.equals("8")){
            return new int[]{6, 3, 1};
        }else if(StairID.equals("9")){
            return new int[]{7, 3, 1};
        }else{
            return new int[]{0,0,-2};
        }
    }

    public boolean validEntrance(int row, int col, int floor, int Room){
        int[][] RoomEntrances = Entrances[Room-21];
        int[] checkLocation = {row,col,floor};

        boolean valid = false;
        for(int i=0;i<RoomEntrances.length;i++){
            if (Arrays.equals(checkLocation, RoomEntrances[i])){
                valid = true;
                break;
            }
        }
        return valid;
    }
    public boolean correctDirection(String Direction, int row, int col, int floor){
        String[][] currentBoard;
        if (floor == 0){
            currentBoard = Board;
        }else{
            currentBoard = Floor1;
        }
        Direction = Direction.replace("Arrow","");

        return currentBoard[row][col].split(",")[0].equals(reverseDirection(Direction));
    }


    //(errors in corners possibly)
    //Includes Stair movement, for Normal Game this needs to be checked and rejected accordingly to the floor
    //Changes Location of Player and returns the New Location
    //If invalid movement then floor == -2
    //If user moves into a Room it returns the CID of the room in index 0 and floor == -1
    public int[] ChangeLocation(String Direction, int row, int col, int floor){
        String[][] currentBoard;
        int RowMax;
        int ColMax;

        int[] NewLocation = new int[3];
        NewLocation[2] = -2; //[0,0,-2]

        //Set Board and limits to them
        if (floor == 0){
            currentBoard = Board;
            RowMax = BoardRowMax;
            ColMax = BoardColMax;
        }else{
            currentBoard = Floor1;
            RowMax = FloorRowMax;
            ColMax = FloorColMax;
        }

        if(Direction.equals("ArrowUp")){
            if(row!=0){
                if(currentBoard[row-1][col].equals("-")){
                    NewLocation[0] = row -1;
                    NewLocation[1] = col;
                    NewLocation[2] = floor;
                }else if(currentBoard[row-1][col].split(",")[0].equals("U")){
                    NewLocation[0] = Integer.parseInt(currentBoard[row-1][col].split(",")[1]);
                    NewLocation[2] = -1;
                }else{
                    //Returns {0,0,-2} if not a Stair space
                    NewLocation = StairUsage(currentBoard[row-1][col]);
                }
            }
        }else if(Direction.equals("ArrowDown")){
            if(row!=RowMax){
                if(currentBoard[row+1][col].equals("-") ){
                    NewLocation[0] = row + 1;
                    NewLocation[1] = col;
                    NewLocation[2] = floor;
                }else if(currentBoard[row+1][col].split(",")[0].equals("D")){
                    NewLocation[0] = Integer.parseInt(currentBoard[row+1][col].split(",")[1]);
                    NewLocation[2] = -1;
                }else{
                    //Returns {0,0,-2} if not a Stair space
                    NewLocation = StairUsage(currentBoard[row+1][col]);
                }
            }
        }else if(Direction.equals("ArrowRight")){
            if(col!=ColMax){
                if(currentBoard[row][col+1].equals("-")){
                    NewLocation[0] = row;
                    NewLocation[1] = col + 1;
                    NewLocation[2] = floor;
                }else if(currentBoard[row][col+1].split(",")[0].equals("R")){
                    NewLocation[0] = Integer.parseInt(currentBoard[row][col+1].split(",")[1]);
                    NewLocation[2] = -1;
                }//No stairs have input from the right
            }
        }else if(Direction.equals("ArrowLeft")){
            if(col!=0){
                if(currentBoard[row][col-1].equals("-")){
                    NewLocation[0] = row;
                    NewLocation[1] = col - 1;
                    NewLocation[2] = floor;
                }else if(currentBoard[row][col-1].split(",")[0].equals("L")){
                    NewLocation[0] = Integer.parseInt(currentBoard[row][col-1].split(",")[1]);
                    NewLocation[2] = -1;
                }else{
                    //Returns {0,0,-2} if not a Stair space
                    NewLocation = StairUsage(currentBoard[row][col-1]);
                }
            }
        }
        return NewLocation;
    }

    //Return [0,0,-2] if invalid usage   i.e. .equals("#")
    public int[] StairUsage(String StairID){
        if(StairID.equals("0")){
            return new int[]{5, 4, 1}; //0 to 7
        }else if(StairID.equals("1") || StairID.equals("2")) {
            return new int[]{4, 4, 1};//1,2 to 6
        }else if(StairID.equals("3")){
            return new int[]{6, 4, 1};//3 to 8
        }else if(StairID.equals("4") || StairID.equals("5")){
            return new int[]{7, 4, 1};//4,5 to 9
        }else if(StairID.equals("6")){
            return new int[]{8, 11, 0};//6 to 1
        }else if(StairID.equals("7")){
            return new int[]{8, 10, 0};//7 to 0
        }else if(StairID.equals("8")){
            return new int[]{15, 10, 0};//8 to 3
        }else if(StairID.equals("9")){
            return new int[]{15, 11, 0};//9 to 4
        }else{
            return new int[]{0,0,-2};
        }
    }

    //Transforms Up/Down/.. to D/U/...
    public String reverseDirection(String Direction){
        if(Direction.equals("Up")){
            return "D";
        }else if(Direction.equals("Down")){
            return "U";
        }else if(Direction.equals("Left")){
            return "R";
        }else if(Direction.equals("Right")){
            return "L";
        }else{
            return "error";
        }
    }

    //Things to be aware of/Still do
    //Getting to a different 'map' to access room     DONE
    //Exiting a room                            DONE
    //Exiting a room with several entrances     DONE
    //When using top Stairs to go down algorithm can cheat on edges  DONE
    //Change wanted entrance to only Direction needed remove ,XX  DONE


    //Returns the shortest path from a starting Room to an End Room with the starting Entrance chosen at index 0
    public ArrayList<String> AStarPathFromRoom(int startRoom, int endRoom){
        ArrayList<String> path = new ArrayList<>();
        int sizeOfPath = 200;
        int IndexofSmallestPathEntrance = 0;

        //For every start Entrance
        for(int i=0;i<Entrances[startRoom-21].length;i++){
            int[] startLocation = Entrances[startRoom-21][i];
            ArrayList<String> tempPath = AStarPathFromLocation(startLocation,endRoom);
            if(tempPath.size() < sizeOfPath){
                path = tempPath;
                sizeOfPath = path.size();
                IndexofSmallestPathEntrance = i;
            }
        }

        //Prepend The starting Entrance to the path
        path.add(0, Arrays.toString(Entrances[startRoom-21][IndexofSmallestPathEntrance]));

        return path;
    }


    //Returns the shortest path from a starting Location to an End Room
    public ArrayList<String> AStarPathFromLocation(int[] startLocation, int endRoom){
        ArrayList<String> path = new ArrayList<>();
        int sizeOfPath = 200;

        //Set correct Board, copy it as it will change values
        String[][] map =  Arrays.stream(Board).map(String[]::clone).toArray(String[][]::new);
        if(startLocation[2]==1){
            map = Arrays.stream(Floor1).map(String[]::clone).toArray(String[][]::new);
        }

        //Split AStar search if on different Floors
        if(startLocation[2]==1 && endRoom<30){
            //for loop for each stair
            for(int i=6;i<=9;i++){
                ArrayList<String> tempPath = DifferentFloorLocation(i, startLocation, endRoom);
                if(tempPath.size() < sizeOfPath){
                    path = tempPath;
                    sizeOfPath = path.size();
                }
            }
        }else if(startLocation[2]==0 && endRoom>=30){
            //for loop for each stair
            for(int i=0;i<=5;i++){
                ArrayList<String> tempPath = DifferentFloorLocation(i, startLocation, endRoom);
                if(tempPath.size() < sizeOfPath){
                    path = tempPath;
                    sizeOfPath = path.size();
                }
            }
        }else{
            path = sameFloorLocation(map,startLocation,endRoom);
        }

        return path;
    }

    //Optimisable copy the map in AStar when using findPathto instead of using/changing Object attribute
    //This allows several Target searches from same starting destination, no need to create several objects for each entrance to end Room

    //Checks a map and finds the shortest path to every exit, returns the shortest path
    public ArrayList<String> sameFloorLocation(String[][] map, int[] startLocation, int endRoom){
        ArrayList<String> path = new ArrayList<>();
        int sizeOfPath = 200;
        for(int i=0;i<Entrances[endRoom-21].length;i++){
            AStar as = new AStar(map, startLocation[1], startLocation[0]);
            ArrayList<String> tempPath = as.findPathTo(Entrances[endRoom-21][i][1], Entrances[endRoom-21][i][0]);
            if(tempPath.size() < sizeOfPath){
                path = tempPath;
                sizeOfPath = path.size();
            }
        }
        return path;
    }

    //Checks a map and finds the shortest path to a given Stair, Uses Stair and then uses sameFloorLocation
    public ArrayList<String> DifferentFloorLocation(int StarID, int[] startLocation, int endRoom){
        String[][] map1;
        String[][] map2;
        //Set Maps
        if(StarID<=5){
            map1 = Arrays.stream(Board).map(String[]::clone).toArray(String[][]::new);
            map2 = Arrays.stream(Floor1).map(String[]::clone).toArray(String[][]::new);
        }else{
            map1 = Arrays.stream(Floor1).map(String[]::clone).toArray(String[][]::new);
            map2 = Arrays.stream(Board).map(String[]::clone).toArray(String[][]::new);
        }

        //go from startLocation to Stair
        int[] targetLocation = StairIDLocation(Integer.toString(StarID));
        AStar as = new AStar(map1, startLocation[1], startLocation[0]);
        ArrayList<String> tempPath = as.findPathTo(targetLocation[1],targetLocation[0]);

        //Go from newLocation after using stairs to Room
        int[] newFloorLocation = StairUsage(Integer.toString(StarID));
        ArrayList<String> tempPath2 = sameFloorLocation(map2,newFloorLocation,endRoom);

        //Merge both paths into one path
        tempPath.addAll(tempPath2);

        return tempPath;
    }


}
