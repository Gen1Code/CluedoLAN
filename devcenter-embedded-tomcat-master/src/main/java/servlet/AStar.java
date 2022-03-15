package servlet;

import java.util.ArrayList;
import java.util.Collections;

class AStar {
    private final ArrayList<Node> openList = new ArrayList<>();
    private final ArrayList<Node> closedList = new ArrayList<>();
    private final ArrayList<String> Directions = new ArrayList<>();
    private String[][] map;
    private Node current;
    private final int startx, starty;
    private int destx, desty;

    //Constructor
    public AStar(String[][] maze_i, int xstart, int ystart) {
        map = maze_i;
        startx = xstart;
        starty = ystart;
        current = new Node(null, startx, starty, 0, 0);
    }

    //Method to be called with goal coordinates
    public ArrayList<String> findPathTo(int xdest, int ydest) {
        destx = xdest;
        desty = ydest;

        //Set the Target Goal as the Only U/D/R/L (makes it easy for validMove function to work)
        map[desty][destx] = DirectionNeededtoReachGoal(map[desty][destx]);

        closedList.add(current);
        addNeighboursToOpenList();

        //While not at destination
        while (current.x != destx || current.y != desty) {
            if (openList.isEmpty()) {
                return null; //No path found
            }
            current = openList.get(0); //get first node (lowest f score)
            openList.remove(0); //remove it
            closedList.add(current); //add to the closedList
            addNeighboursToOpenList(); //add its neighbours to the openList
        }

        //Start from the end
        while (current.x != startx || current.y != starty) {
            //keyCode directions (to mimic how users send data to server)
            String Direction;
            if(current.x > current.parent.x){
                Direction = "ArrowRight";
            }else if(current.x < current.parent.x){
                Direction = "ArrowLeft";
            }else if(current.y > current.parent.y){
                Direction = "ArrowDown";
            }else{
                Direction = "ArrowUp";
            }
            //go to previous node
            current = current.parent;
            Directions.add(0,Direction);//add at 0 index to reverse order
        }
        return Directions;
    }

    //Looks in a given List for a node
    private boolean findNodeInList(ArrayList<Node> array, Node node) {
        return array.stream().anyMatch((n) -> (n.x == node.x && n.y == node.y));
    }

    //Calculate distance between current and goal
    private int distance(int dx, int dy) {
        return Math.abs(current.x + dx - destx) + Math.abs(current.y + dy - desty); //return distance
    }

    //adds valid Neighbouring Nodes to openList
    private void addNeighboursToOpenList() {
        //check around you by changing x,y
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                //either x or y has to equal 0 to look orthogonally , if node is the same x and y is 0
                if ((x != 0 && y != 0)) {
                    continue;
                }
                String Direction;
                if(x == -1){
                    Direction = "L";
                }else if (x == 1){
                    Direction = "R";
                }else if (y==-1){
                    Direction = "U";
                }else{
                    Direction = "D";
                }

                Node node = new Node(current, current.x + x, current.y + y, current.g, distance(x, y));
                if ((x != 0 || y != 0)
                    && current.x + x >= 0 && current.x + x < map[0].length // check maze boundaries (RECTANGLE)
                    && current.y + y >= 0 && current.y + y < map.length
                    && !findNodeInList(openList, node) && !findNodeInList(closedList, node) // check if the Node isn't already in the lists
                    && ValidMove(Direction,map[current.y+y][current.x+x]) // check if square can be moved on
                )
                {
                    node.g = node.parent.g + 1; // Horizontal/vertical cost = 1
                    openList.add(node);
                }
            }
        }
        //sort all nodes for best
        Collections.sort(openList);
    }

    //Calculates what direction is needed to reach the Goal
    public String DirectionNeededtoReachGoal(String Square){
        String Direction = "";
        if(Square.split(",")[0].equals("U")){
            Direction = "U";
        }else if(Square.split(",")[0].equals("D")){
            Direction = "D";
        }else if(Square.split(",")[0].equals("L")){
            Direction = "L";
        }else if(Square.split(",")[0].equals("R")){
            Direction = "R";
        }else if(Integer.parseInt(Square) <=2){
            Direction = "D";
        }else if(Integer.parseInt(Square) <=5){
            Direction = "U";
        }else if(Integer.parseInt(Square) <=9){
            Direction = "L";
        }else{
            System.out.println("Error, Target was Invalid:" +Square);
        }
        return Direction;
    }

    //Checks if a move the algorithm wants to do is valid
    public boolean ValidMove(String Direction, String Square){
        boolean valid;
        if(Square.equals("-")){// free space
            valid = true;
        }else if(Square.equals("#")){
            valid = false;
        }else if(Square.equals(Direction)){//If Square is destination and Direction is correct
            valid = true;
        }else{
            valid = false;
        }
        return valid;
    }

    /*Debugging Usage Only*/
    /*public static void main(String[] args) {
        // - = free
        // # = blocked
        String[][] map = {
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

        AStar as = new AStar(map, 7,0);
        ArrayList<String> path = as.findPathTo(6,3);
        System.out.println(map[18][19]);
        System.out.println(path);

    }*/

    // Node class
    class Node implements Comparable {
        public Node parent;
        public int x, y, g, h;
        public Node(Node parent_i, int xpos, int ypos, int g_i, int h_i) {
            parent = parent_i;
            x = xpos;
            y = ypos;
            g = g_i;
            h = h_i;
        }
        // Make Collections.sort() Compare by g + h
        @Override
        public int compareTo(Object n) {
            Node cmp = (Node) n;
            return (g + h) - (cmp.g + cmp.h);
        }
    }
}
