<%@ page import="Database.DBManager" %>
<%@ page import="java.sql.SQLException" %>
<html>
<%
    DBManager testObject = new DBManager("Cluedo.db");
    Integer AID = (Integer) session.getAttribute("AID");
    Integer GID = (Integer) session.getAttribute("GID");
    try{
        //Reject invalid ID
        //Only allow current database marked session to be logged in, blocks multiple sessions
        //Check Account is Participating in the Game
        //Check the Game is being played
        if(AID == null || AID == -1
                || !request.getSession().getId().equals(testObject.getSessionID(AID))
                || testObject.getPID(AID,GID) == -1
                || !testObject.getGameState(GID).equals("playing")
        ){
            response.sendRedirect("index.jsp");
        }

    }catch(NullPointerException | SQLException e){
        response.sendRedirect("index.jsp");
    }
%>
<style>
    .GameEndPopup {
        width: 60%;
        padding: 5px;
        left: 0;
        margin-left: 20%;
        border: 1px solid rgb(1,82,73);
        border-radius: 10px;
        background: #d39b75;
        position: absolute;
        top: 2%;
        box-shadow: 5px 5px 5px #000;
        z-index: 10001;
        font-weight: 700;
        text-align: center;
    }

    .GameEndOverlay {
        position: fixed;
        width: 100%;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0,0,0,.85);
        z-index: 10000;
        display :none;
    }


    .dialog-btn {
        background-color:#44B78B;
        color: white;
        font-weight: 700;
        border: 1px solid #44B78B;
        border-radius: 10px;
        height: 30px;
        width: 30%;
    }

    .dialog-btn:hover {
        background-color:#015249;
        cursor: pointer;
    }

    .cards {
        height:88;
        width:62;
        background: white;
    }

    .EndTable{
        border: 1px solid black;
        margin: 12px 7px 10px 30%;
    }

    th, td{
        text-align: center;
        border: 1px solid black;
        font-size: 18px;
    }

    .EndTitle{
        font-size: 28px;
        font-weight: bold;
    }

</style>

<script src="https://cdn.jsdelivr.net/npm/lodash@4.17.10/lodash.min.js"></script>
<script>
    //Additions
    //WASD Support + change in Secret Passageway key
    //Create Form to fill in Auto Joining

    var UpdateInterval = null;

    //AID of Player
    var PlayerAID = ${AID};

    //Real In time data
    var data = {};

    //Temporary placeholders that change and hold past specific 'data'
    var Board = {};
    var ActionList = [];
    var Dices = [];
    var Turn = "";
    var Step ="";

    //Same as above except more permanent (special case when a player leaves mid-game this should also change)
    var Game = {};

    //Keeps Letter of Last Form, so Form B can toggle on/aff keeping same Form than before
    var lastForm = "";

    //Floor this player is currently viewing
    var viewingFloor = 0;

    //Normal Cards
    var Suspects = ["Col. Mustard","Prof. Violet","Mr. Green","Mrs Peacock","Miss Scarlet","Mrs White"];
    var Weapons = ["Knife","Candlestick","Revolver","Rope","Lead Pipe","Wrench"];
    var Rooms = ["Hall","Lounge","Dining Room","Kitchen","Ball Room","Conservatory","Billiard Room","Library","Study"];

    //Big Game Cards
    var SuspectsExtra = ["M Dubois","Ayane Kuroki","Miss Carrot","Sr. Lapis"];
    var WeaponsExtra = ["Flower Pot","Poison", "Dumbbell","Shuriken"];
    var RoomsExtra = ["Bedroom","Balcony","Fitness Room","Play Room"];

    //Used to change CID to the correct Name
    //Since CID in DB starts at 1 and index here starts at 0, do -1 when converting CID to Card Name
    var AllCards = Suspects.concat(SuspectsExtra).concat(Weapons).concat(WeaponsExtra).concat(Rooms).concat(RoomsExtra);

    //Changes CID to Pixel Position on Board
    //Since CID in DB for Room Cards start at 21 and index here starts at 0, do -21 when converting CID to Position
    const RoomPosition = [
         [298,225,0], //Hall
         [476,207,0], //Lounge
         [468,382,0], //Dining Room
         [482,563,0], //Kitchen
         [301,543,0], //Ball Room
         [112,570,0], //Conservatory
         [118,424,0], //Billiard Room
         [124,307,0], //Library
         [118,180,0], //Study
         [298,477,1], //Bedroom
         [308,265,1], //Balcony
         [418,453,1], //Fitness Room
         [405,360,1]  //Play Room
    ];

    //CID to a list of Entrances
    const Entrances = {
        21:[[4,9,0],[6,11,0],[6,12,0]],//Hall
        22:[[5,17,0]],//Lounge
        23:[[9,17,0],[11,16,0]],//Dining Room
        24:[[18,19,0]],//Kitchen
        25:[[19,8,0],[17,9,0],[17,14,0],[19,15,0]],//Ballroom
        26:[[20,5,0]],//Conservatory
        27:[[12,1,0],[15,6,0]],//Billiard Room
        28:[[8,6,0],[10,3,0]],//Library
        29:[[3,6,0]],//Study
        30:[[9,4,1]],//Bedroom
        31:[[1,2,1],[1,3,1]],//Balcony
        32:[[7,6,1]],//Fitness Room
        33:[[3,5,1]]//Play Room
    };


    //Sets the First Data and draws the data
    function GetFirstData(){
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                data = JSON.parse(this.responseText);

                //Changes configuration according to data
                BigGameSupport();

                //Initial Drawing of Board
                drawBoard();

                //Initial drawing of Community Cards
                drawCommunityCards();

                //Initial drawing of Player Cards
                drawPlayerCards();

                //Initial drawing of Tick Card
                drawTickCard();

                //Draw First data received, regardless if it was already received earlier (refreshed page)
                draw();
            }
        };
        xmlhttp.open("POST", "update", true);
        xmlhttp.send('');
    }

    //Recursively calls update() every 500ms
    function updateMe(){
        if(UpdateInterval != null){
            window.clearInterval(UpdateInterval);
        }
        UpdateInterval = window.setInterval(update, 300);
    }

    //Grabs data from the database and sets it to data, proceeds to 'draw' the information
    function update(){
        var xmlhttp = new XMLHttpRequest();
        var url = "update";
        xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            //Check if data is sent is not null
            if(this.responseText!=="{}") {
                //Log and set Data
                console.log(this.responseText);
                data = JSON.parse(this.responseText);

                //If data contains Guesser property it is declaring the End of a Game
                if(data.hasOwnProperty('Guesser')){
                    //Stop updating every 0.3s
                    window.clearInterval(UpdateInterval);

                    //draw Result of Game
                    drawEndGame();
                }else{
                    //Call Draw function
                    draw();
                }
            }
        }
        };
        xmlhttp.open("GET", url, true);
        xmlhttp.send();
    }

    //Event Listeners for Player Input
    window.addEventListener("keyup", function(event) {
        if (event.defaultPrevented) {
            return;
        }
        if (event.code === "ArrowDown" ||
            event.code === "ArrowUp" ||
            event.code === "ArrowLeft" ||
            event.code === "ArrowRight")
        {
            var Room = AIDtoRoom(PlayerAID);
            //Hijack Normal ajax POST if player is in a room
            if(Room != 0){
                var RoomEntrances = Entrances[Room];
                //If only one Entrance
                if(RoomEntrances.length == 1){
                    ajaxPOSTReq("move",event.code+","+RoomEntrances[0].join(" "));
                    //data example: ArrowUp,12 13 1
                }else {
                    //If tempLocation is default value
                    if (tempLocation[0] == -1) {
                        //Call method to set tempLocation to correct Entrance
                        setTempLocation(Room,event.code)
                        var canvas = document.getElementById('canv');
                        var ctx = canvas.getContext('2d');
                        drawPlayers(ctx);
                    }
                    //If an entrance to entrance movement, dont post
                    else if(entrancetoEntranceBoolean(Room,event.code)) {
                        //Change tempLocation depending on input
                        changeTempLocation(Room,event.code);
                        var canvas = document.getElementById('canv');
                        var ctx = canvas.getContext('2d');
                        drawPlayers(ctx);
                    }
                    //If several entrances and keycode leads to no other entrance then post action
                    else {
                        var copy = tempLocation;
                        tempLocation = [-1,-1,-1];//Set back to default value
                        ajaxPOSTReq("move",event.code+","+copy.join(" "));
                    }
                }
            }else{
                ajaxPOSTReq("move",event.code);
            }
        }else if(event.code === "Space"){
            //rolls the dice
            ajaxGETReq("roll");
        }else if (event.code === "KeyS"){
            //Uses secret passageway
            ajaxGETReq("passage")
        }
        event.preventDefault();
        }, true);

    //Prevent Scrolling
    window.addEventListener('keydown', function(event) {
        if((event.code === "Space" || event.code === "ArrowDown" || event.code === "ArrowUp" || event.code === "ArrowRight") && event.target == document.body) {
            event.preventDefault();
        }
    });

    function entrancetoEntranceBoolean(Room,code){
        var RoomEntrancesForThatRoom = Entrances[Room];
        var entrancefound = false;
        for(var i=0;i<RoomEntrancesForThatRoom.length;i++){
            if(
                (code == "ArrowUp" && RoomEntrancesForThatRoom[i][0] < tempLocation[0]) ||
                (code == "ArrowDown" && RoomEntrancesForThatRoom[i][0] > tempLocation[0]) ||
                (code == "ArrowLeft" && RoomEntrancesForThatRoom[i][1] < tempLocation[1]) ||
                (code == "ArrowRight" && RoomEntrancesForThatRoom[i][1] > tempLocation[1])
            ){
                entrancefound = true;
            }
        }
        return entrancefound;
    }

    const RestingPositionSeveralEntrances = {
        21:[4,11,0],//Hall
        23:[11,20,0],//Dining Room
        25:[19,12,0],//Ballroom
        27:[14,3,0],//Billiard Room
        28:[8,3,0],//Library
        31:[0,2,1]//Balcony
    }

    function findExtremeEntrance(ColChosenBoolean,smallestBoolean, RoomEntrances){
        var index = 0;
        var roworcol = 0;
        if(ColChosenBoolean){
            roworcol = 1;
        }
        var mostExtreme = RoomEntrances[index][roworcol];
        for(var i=1;i<RoomEntrances.length;i++){
            if(RoomEntrances[i][roworcol] > mostExtreme && !smallestBoolean){
                index = i;
            }else if(RoomEntrances[i][roworcol] < mostExtreme && smallestBoolean){
                index = i;
            }
        }
        return RoomEntrances[index];
    }
    function findNextClosestEntrance(ColChosenBoolean,smallestBoolean,RoomEntrances){
        var currentLocation = tempLocation;
        var split = RoomEntrances.indexOf(currentLocation);
        var copy = RoomEntrances;
        var otherEntrances = RoomEntrances.slice(0,split).concat(copy.slice(split+1,copy.length));
        var leastExtremeIndex;
        var difference =99;
        var roworcol = 0;
        if(ColChosenBoolean){
            roworcol = 1;
        }
        for(var i=0;i<otherEntrances.length;i++){
            if(((otherEntrances[i][roworcol] - currentLocation[roworcol]) < difference)
                    && ((otherEntrances[i][roworcol] - currentLocation[roworcol]) >0) && !smallestBoolean){
                difference = otherEntrances[i][roworcol] - currentLocation[roworcol];
                leastExtremeIndex = i;
            }else if(((currentLocation[roworcol] - otherEntrances[i][roworcol]) < difference)
                    && ((currentLocation[roworcol] - otherEntrances[i][roworcol]) >0) && smallestBoolean){
                difference = currentLocation[roworcol] - otherEntrances[i][roworcol];
                leastExtremeIndex = i;
            }
        }
        return otherEntrances[leastExtremeIndex];
    }

    //Sets/Changes a temporary location (purely client-side) to show choosing of exits out of a room
    function setTempLocation(Room,code){
        var RoomEntrances = Entrances[Room];
        var RestingPosition = RestingPositionSeveralEntrances[Room];
        var temporary;

        if(code == "ArrowUp"){
            temporary = findExtremeEntrance(false,true,RoomEntrances);
            if(temporary[0] < RestingPosition[0]){
                tempLocation = temporary;
            }
        }else if(code == "ArrowDown"){
            temporary = findExtremeEntrance(false,false,RoomEntrances);
            if(temporary[0] > RestingPosition[0]){
                tempLocation = temporary;
            }
        }else if(code == "ArrowLeft"){
            temporary = findExtremeEntrance(true,true,RoomEntrances);
            if(temporary[1] < RestingPosition[1]){
                tempLocation = temporary;
            }
        }else if(code == "ArrowRight"){
            temporary = findExtremeEntrance(true,false,RoomEntrances);
            if(temporary[1] > RestingPosition[1]){
                tempLocation = temporary;
            }
        }
    }

    function changeTempLocation(Room,code){
        var RoomEntrances = Entrances[Room];
        if(code == "ArrowUp"){
            tempLocation = findNextClosestEntrance(false,true,RoomEntrances);
        }else if(code == "ArrowDown"){
            tempLocation = findNextClosestEntrance(false,false,RoomEntrances);
        }else if(code == "ArrowLeft"){
            tempLocation = findNextClosestEntrance(true,true,RoomEntrances);
        }else if(code == "ArrowRight"){
            tempLocation = findNextClosestEntrance(true,false,RoomEntrances);
        }
    }


    //Pop-up that Shows Game Scores
    function drawEndGame(){
        var Container = document.getElementById("dialog-container");
        Container.style.display = "block";

        document.getElementById("EndCards").innerHTML = '<p style="font-size:22px; font-weight: bold;">Guessed Cards</p>'+
            '<img src="assets/' + AllCards[data.GuessedCards[0]-1] + ' Card.png" class="cards" style="">' +
            '<img src="assets/' + AllCards[data.GuessedCards[1]-1] + ' Card.png" class="cards" style="">' +
            '<img src="assets/' + AllCards[data.GuessedCards[2]-1] + ' Card.png" class="cards" style="">';

        var tableHTML = "<tr><th>Name</th><th>Points Change</th></tr>";

        for(var i=0; i<data.GameOrder.length;i++){
            var tempNameColour = AIDtoPlayerNameAndColour(data.GameOrder[i]);
            var tempName = tempNameColour[0];
            var tempColour = tempNameColour[1];
            var tempPointsChange = data.PointsDistributed[i];

            if(data.GameOrder[i]==data.Guesser) {
                //If Guesser Won
                if (data.Won == true) {
                    document.getElementById("EndTitle").innerHTML = '<span style="color:' + tempColour + '">' + tempName + '</span> Won!!';
                } else {
                    document.getElementById("EndTitle").innerHTML = '<span style="color:' + tempColour + '">' + tempName + '</span> Guessed Incorrectly';
                    //Also Show Hidden Cards
                    document.getElementById("EndCards").innerHTML += '<p style="font-size:22px; font-weight: bold;">Hidden Cards</p>'+
                        '<img src="assets/' + AllCards[data.HiddenCards[0] - 1] + ' Card.png" class="cards" style="">' +
                        '<img src="assets/' + AllCards[data.HiddenCards[1] - 1] + ' Card.png" class="cards" style="">' +
                        '<img src="assets/' + AllCards[data.HiddenCards[2] - 1] + ' Card.png" class="cards" style="">';
                }
            }

            //Points Stuff
            tableHTML += '<tr><td><span style="color:'+tempColour+'">'+tempName+'</span></td>'+
                         '<td>'+tempPointsChange+'</td></tr>';
        }

        document.getElementById("EndTable").innerHTML = tableHTML;
    }


    //Draws data, if the specific data has changed since last drawing
    function draw(){
        //Get canvas drawer
        var canvas = document.getElementById('canv');
        var ctx = canvas.getContext('2d');

        //Imported _.isEqual to compare 2 JSON Objects

        //Sets Game Attributes (if they have changed since first data load)
        if (!(_.isEqual(Game, data.Game))) {
            Game = data.Game;
        }

        //Draw Players & the correct Form if it is the players Turn
        if (!(_.isEqual(Board, data.Board))) {
            Board = data.Board;
            drawPlayers(ctx);
            if (data.Turn == PlayerAID) {
                drawForm();
            }
        }

        //Draw Dice
        if (!(_.isEqual(Dices, data.Dices))) {
            drawDices(ctx);
            Dices = data.Dices;
        }

        //Draw Turn
        if (Turn != data.Turn) {
            Turn = data.Turn;
            drawTurn(ctx);
        }

        //Draw Action List
        if(!(_.isEqual(ActionList,data.ActionList))){
            ActionList = data.ActionList;
            drawActionList();
        }

        //Draw Steps
        if (Step != data.StepsLeft) {
            Step = data.StepsLeft;
            drawSteps(ctx);
        }
    }

    //Hides Buttons and Forms IF NOT player's turn, Shows them IF it is the player's turn
    //Also Updates Visual Order drawing
    function drawTurn(ctx){
        if (data.Turn == PlayerAID) {
            document.getElementById("Skip").style.display = "block";
            document.getElementById("guessCards").style.display = "block";
            drawForm();
        } else {
            document.getElementById("Skip").style.display = "none";
            document.getElementById("guessCards").style.display = "none";
            NoForm();
        }

        //Create Background
        ctx.fillStyle = '#EFBC9F';
        ctx.beginPath();
        ctx.rect(318, 3, 150, 45);
        ctx.fill();
        ctx.closePath();

        //Create arrow for Order
        ctx.fillStyle = '#000000';
        canvas_arrow(ctx, 330,13,330,19,6);
        ctx.beginPath();
        ctx.rect(328, 5, 3, 12);
        ctx.fill();
        ctx.closePath();

        //FOR every player in the Game draw them in Order
        var startingGameOrderIndex = data.Game.Order.indexOf(data.Turn);
        for(var i=0;i<data.Game.Size;i++){
            var temporaryGameOrderIndex = (startingGameOrderIndex+i)%data.Game.Size;
            var colour = getColourFromGameOrderIndex(temporaryGameOrderIndex);
            ctx.beginPath();
            ctx.arc(i*20.9 +330,34, 8, 0, 2 * Math.PI);
            ctx.fillStyle = colour;
            ctx.fill();
            ctx.closePath();
        }
    }

    function drawSteps(ctx){
        //Draws Jump Form  on Refresh
        var total = data.Dices[0] + data.Dices[1];

        //Remove Step Count
        ctx.clearRect(410,100-48,80,80);

        if(data.Turn == PlayerAID){
            if((total == 2 &&  data.StepsLeft == 2) ||
                (total == 12 &&  data.StepsLeft == 12))
            {
                FormC();
            }
            if(data.StepsLeft != -1 && data.StepsLeft !=0){
                ctx.fillStyle = "#000000";
                ctx.font = '48px serif';
                ctx.fillText(data.StepsLeft, 411, 100);
            }
        }
    }

    function getColourFromGameOrderIndex(GameOrderIndex){
        var colourFound = false;
        var index = 0;
        var colour;
        while(!colourFound){
            if(data.Board[index][0]==data.Game.Order[GameOrderIndex]){
                colourFound = true;
                colour = data.Board[index][3];
            }
            index = index + 1;
        }
        return colour;
    }

    //Defines an arrow using coords given
    function canvas_arrow(ctx, fromx, fromy, tox, toy, r){
        var x_center = tox;
        var y_center = toy;
        var angle;
        var x;
        var y;
        ctx.beginPath();
        angle = Math.atan2(toy-fromy,tox-fromx)
        x = r*Math.cos(angle) + x_center;
        y = r*Math.sin(angle) + y_center;
        ctx.moveTo(x, y);
        angle += (1/3)*(2*Math.PI)
        x = r*Math.cos(angle) + x_center;
        y = r*Math.sin(angle) + y_center;
        ctx.lineTo(x, y);
        angle += (1/3)*(2*Math.PI)
        x = r*Math.cos(angle) + x_center;
        y = r*Math.sin(angle) + y_center;
        ctx.lineTo(x, y);
        ctx.fill();
        ctx.closePath();
    }

    //Ajax Get and Post Requests, used for sending player actions to server
    function ajaxGETReq(url){
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open("GET", url, true);
        xmlhttp.send();
    }
    function ajaxPOSTReq(url,Sdata){
        console.log(Sdata);
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open("POST", url, true);
        xmlhttp.send(Sdata);
    }

    //Draw Dices
    function drawDices(ctx){
        if(!(data.Dices[0] == 0)) {
            var img = new Image();
            var img2 = new Image();
            img.src = "assets/Dice_" + data.Dices[0] + ".jpg";
            img2.src = "assets/Dice_" + data.Dices[1] + ".jpg";
            img.onload = function () {
                ctx.drawImage(img, 210, 54, 43, 43);
            };
            img2.onload = function (){
                ctx.drawImage(img2, 260, 54, 43, 43);
            };
        }
    }

    //Draw the Board being viewed
    function drawBoard(){
        if(viewingFloor == 0 && data.Game.Type == "Big Game"){
            var Image = document.getElementById('Board');
            Image.src="assets/BoardWStairs.png";
            Image.style.height="500";
            Image.style.width="500";
            Image.style.top = "140";
            Image.style.left = "50";
        }else if(viewingFloor == 1 && data.Game.Type == "Big Game"){
            var Image = document.getElementById('Board');
            Image.src="assets/Floor1.png";
            Image.style.height="294";
            Image.style.width="243";
            Image.style.top = "240";
            Image.style.left = "240";
        }else{
            document.getElementById('Board').src="assets/Board.png";
        }
    }

    //Draws the Action List
    function drawActionList(){
        var ActionList = document.getElementById("ActionListContainer");
        var innerString = "";
        for(var i=data.ActionList.length-1;i>=0;i--){
            var tempString = TurnArraytoString(data.ActionList[i]);
            innerString = innerString + tempString +"<br>";
        }
        ActionList.innerHTML = innerString;
    }
    function TurnArraytoString(Turn){
        var String = "";
        for(var i=0;i<Turn.length;i++){
            String = String + ActionArraytoString(Turn[i]) +"<br>";
        }
        return String;
    }
    function ActionArraytoString(Action){
        var String = "";
        var FirstAID = AIDtoPlayerNameAndColour(Action[1]);
        var PlayerName = FirstAID[0];
        var Colour = FirstAID[1];
        if(Action[0]==0){
            String = '<span style="color: '+Colour+'; font-weight:bold;">'+PlayerName+'</span> started their Turn';
        }else if(Action[0]==1){
            String = '<span style="color: '+Colour+'; font-weight:bold;">'+PlayerName+'</span> rolled '+Action[2]+
                ' and '+Action[3];
        }else if(Action[0]==3){
            var tempString="";
            if(Action[2]==0){
                tempString = "out of";
            }else{
                tempString = "into";
            }
            String = '<span style="color: '+Colour+'; font-weight:bold;">'+PlayerName+'</span> moved '+tempString+
                ' the '+AllCards[Action[3]-1];
        }else if(Action[0]==4){
            var SecondAID = AIDtoPlayerNameAndColour(Action[5]);
            var PlayerName2 = SecondAID[0];
            var Colour2 = SecondAID[1];

            var tempString="";

            if(Action[6]==0){
                tempString = "didn't have any of those cards";
            }else{
                if(Action[1] == PlayerAID || Action[5] == PlayerAID){
                    tempString = "responded with "+AllCards[Action[6]-1];
                }else{
                    tempString = "owned at least one of those cards";
                }
            }

            String = '<span style="color: '+Colour+'; font-weight:bold;">'+PlayerName+'</span> asked for '+
                 AllCards[Action[2]-1]+', '+AllCards[Action[3]-1]+', '+AllCards[Action[4]-1]+' to:'+
                '<br><span style="color: '+Colour2+'; font-weight:bold;">'+PlayerName2+'</span> '+tempString;

        }else if(Action[0]==2){
            var SecondAID = AIDtoPlayerNameAndColour(Action[2]);
            var PlayerName2 = SecondAID[0];
            var Colour2 = SecondAID[1];
            var tempString="";

            if(Action[3]==0){
                tempString = "didn't have any of those cards";
            }else{
                if(Action[1] == PlayerAID || Action[2] == PlayerAID){
                    tempString = "responded with "+AllCards[Action[3]-1];
                }else{
                    tempString = "owned at least one of those cards";
                }
            }

            String = '<span style="color: '+Colour2+'; font-weight:bold;">'+PlayerName2+'</span> '+tempString;

        }else if(Action[0]==7){
            String = '<span style="color: '+Colour+'; font-weight:bold;">'+PlayerName+'</span> ended their Turn';
        }else{
            String = "Invalid";
        }
        return String;
    }

    //Draws Community Cards
    function drawCommunityCards(){
        for(var i=0;i<data.Game.CommunityCards.length;i++) {
            document.getElementById('CommunityCardContainer').innerHTML += '<img src="assets/' + AllCards[data.Game.CommunityCards[i]-1] + ' Card.png" class="cards" style="left:150; top:0;">';
        }
    }

    //Draws Player Cards
    function drawPlayerCards(){
        for(var i=0;i<data.PlayerCards.length;i++) {
            document.getElementById('PlayerCardContainer').innerHTML += '<img src="assets/' + AllCards[data.PlayerCards[i]-1] + ' Card.png" class="cards" style="left:150; top:0;">';
        }
    }

    //Draws Tick Card
    function drawTickCard() {
        //Redraws ticks if player refreshed their page
        if(window.name != null && window.name != ''){
            YposToCheckMark = JSON.parse(window.name);
            var correctposx = 685.0;
            if (data.Game.Type == "Big Game"){
                correctposx = 715;
            }
            for(var key in YposToCheckMark){
                if(YposToCheckMark[key] == true){
                    var canvas = document.getElementById('canv');
                    var ctx = canvas.getContext('2d');
                    canvas_checkmark(ctx,correctposx,key);
                }
            }
        }

        if (data.Game.Type == "Normal Game") {
            document.getElementById('TickCardContainer').innerHTML += '<img src="assets/Normal Tick Card.png" style="height:390; width:130;">';
        }else{
            document.getElementById('TickCardContainer').innerHTML += '<img src="assets/Big Tick Card.png" style="height:583; width:160;">';
        }

    }

    //Saves a boolean for each check mark Y Position
    var YposToCheckMark = {};
    function ClickTickCard(e){
        var posx = e.pageX;
        var posy = e.pageY;
        var canvas = document.getElementById('canv');
        var ctx = canvas.getContext('2d');

        //Check sent data has a Game Property
        if(data.hasOwnProperty('Game')){
            //If posx&y are in valid positions for the type of Game
            if(data.Game.Type == "Normal Game" && posx <= 708 && posx >= 676 && posy<=467 && posy>=97
                    &&!(posy<=209 && posy>=194) &&!(posy<=323 && posy>=308)){
                var correctposx = 685.0;
                var fromtopy = posy - 98.0;
                canvas_logic_checkmark(ctx,correctposx,fromtopy);
            }else if (data.Game.Type == "Big Game" && posx<=735 && posx>=708 && posy<=660 && posy>=99
                    &&!(posy<=453 && posy>=437) &&!(posy<=275 && posy>=260)){
                var correctposx = 715.0;
                var fromtopy = posy - 100.0;
                canvas_logic_checkmark(ctx,correctposx,fromtopy);
            }
            //Set key-value of ticks to a client stored place so that if page refreshes ticks are kept
            window.name = JSON.stringify(YposToCheckMark);
        }else{
            window.name = '';
        }
    }

    //Canvas Drawer functions
    function canvas_logic_checkmark(ctx, posx, posy){
        var newy = (Math.round(((Math.round((posy-7)/16.1)*16.1) +98 +7)*10)/10).toFixed(1);
        if(!YposToCheckMark[newy]){
            canvas_checkmark(ctx,posx,newy);
            YposToCheckMark[newy] = true;
        }else{
            canvas_remove_checkmark(ctx,posx,newy);
            YposToCheckMark[newy] = false;
        }
    }
    function canvas_checkmark(ctx,fromx,fromy){
        fromx = parseInt(fromx);
        fromy = parseInt(fromy);
        ctx.beginPath();
        ctx.moveTo(fromx,fromy);
        ctx.lineTo(fromx+6.0,fromy+6.0);
        ctx.lineTo(fromx+18.0,fromy-5.0);
        ctx.lineWidth = 5;
        ctx.strokeStyle = '#000000';
        ctx.stroke();
        ctx.closePath();
    }
    function canvas_remove_checkmark(ctx,checkmarkx,checkmarky){
        ctx.beginPath();
        ctx.clearRect(checkmarkx-10,checkmarky-7.5,30,17);
        ctx.closePath();
    }

    var tempLocation = [-1,-1,-1];

    //Draws Players if they are being viewed
    function drawPlayers(ctx){
        var BoardData = data.Board;
        ctx.clearRect(40,140,550,640);
        for(var i=0;i<BoardData.length;i++){
            if(BoardData[i][1] == 0){
                var position = BoardData[i][2].split(",");
                drawPlayer(position[0],position[1],position[2],BoardData[i][3],ctx);
            }else if(BoardData[i][0]==PlayerAID && tempLocation[0]!=-1){
                var position = tempLocation;
                drawPlayer(position[0],position[1],position[2],BoardData[i][3],ctx);
            }else{
                drawPlayerInRoom(BoardData[i][1],BoardData[i][3],ctx);
            }
        }
    }
    function drawPlayerInRoom(room,colour,ctx){
        //21 cards before Room cards, to match up index to CID
        var position = RoomPosition[room-21];
        if(position[2]==0 && viewingFloor == 0){
            ctx.beginPath();
            ctx.arc(position[0],position[1], 8, 0, 2 * Math.PI);
            ctx.fillStyle = colour;
            ctx.fill();
            ctx.closePath();
        }else if(position[2]==1 && viewingFloor ==1){
            ctx.beginPath();
            ctx.arc(position[0],position[1], 8, 0, 2 * Math.PI);
            ctx.fillStyle = colour;
            ctx.fill();
            ctx.closePath();
        }
    }
    function drawPlayer(row,column,floor,colour,ctx){
        if(floor==0 && viewingFloor == 0){
            ctx.beginPath();
            ctx.arc(column*20.2 +68,row*19.93 +152, 8, 0, 2 * Math.PI);
            ctx.fillStyle = colour;
            ctx.fill();
            ctx.closePath();
        }else if(floor==1 && viewingFloor ==1){
            ctx.beginPath();
            ctx.arc(column*21.7 +255.2,row*21.8 +261, 8, 0, 2 * Math.PI); //Positions could be changed
            ctx.fillStyle = colour;
            ctx.fill();
            ctx.closePath();
        }
    }

    //Draw Arrow if a Big Game
    function BigGameSupport(){
        if (data.Game.Type == "Big Game") {
            //Add changing floor viewing arrow
            var ArrowDiv = document.getElementById("ArrowContainer");
            var Arrow = document.createElement("img");
            Arrow.id = "Arrow";
            Arrow.src = "assets/UpArrow.png";
            Arrow.onclick = function temp(){changeViewingFloor()};
            Arrow.style.width = "40";
            Arrow.style.height = "40";
            Arrow.style.zIndex = "2";
            Arrow.style.position = "absolute";
            Arrow.style.left = "510";
            Arrow.style.top = "70";
            ArrowDiv.append(Arrow);

            //Change Game Cards to include extra ones
            Suspects = Suspects.concat(SuspectsExtra);
            Weapons = Weapons.concat(WeaponsExtra);
            Rooms = Rooms.concat(RoomsExtra);
        }
    }
    function changeViewingFloor(){
        //Get canvas drawer
        var canvas = document.getElementById('canv');
        var ctx = canvas.getContext('2d');
        var ArrowImage = document.getElementById("Arrow");

        if(viewingFloor == 1){
            viewingFloor = 0;
            ArrowImage.src="assets/UpArrow.png";
        }else{
            viewingFloor = 1;
            ArrowImage.src="assets/DownArrow.png";
        }
        drawBoard();
        drawPlayers(ctx);
    }

    function AIDtoPlayerNameAndColour(AID){
        var colour;
        var PlayerName;
        for(var i=0;i<data.Board.length;i++){
            if(Board[i][0] == AID){
                PlayerName = Board[i][4];
                colour = Board[i][3];
            }
        }
        return [PlayerName,colour];
    }

    //AID to CID
    function AIDtoRoom(AID){
        var Room;
        for(var i=0;i<data.Board.length;i++){
            if(Board[i][0] == AID){
                Room = Board[i][1];
            }
        }
        return Room;
    }

    //Uses global variable LastForm to make the Guess Hidden Cards button put back the correct Form
    //Only one Form at a time
    //3 different forms, A:Question, B:Guess, C:Jump
    function FormA(){
        var form = document.getElementById("Form");
        form.style.display = "block";
        document.getElementById("NOJump").style.display = "none";

        var selectE = document.createElement("select");
        selectE.name = "Suspect";
        var selectE2 = document.createElement("select");
        selectE2.name = "Weapon";
        var labelE = document.createElement("label");
        var submitE = document.createElement("input");

        var title = "Ask a Question:";
        labelE.innerHTML = "<b>Suspect:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Weapon:</b><div><div>";
        form.innerHTML = "<b>"+title+"</b><div></div>";
        submitE.type = "submit";
        submitE.value = "Submit";

        for (var i = 0; i < Suspects.length; i++){
            var optionE = document.createElement("option");
            optionE.innerText = Suspects[i];
            selectE.append(optionE);
        }
        for (var i = 0; i < Weapons.length; i++){
            var optionE = document.createElement("option");
            optionE.innerText = Weapons[i];
            selectE2.append(optionE);
        }

        form.append(labelE);
        form.append(selectE);
        form.append(selectE2);
        form.append(submitE);
        document.Form.action = "";
        document.Form.onsubmit = function temp(){Question();return false;};
        lastForm="A";
    }
    function FormB(){
        var form = document.getElementById("Form");
        form.style.display = "block";
        document.getElementById("NOJump").style.display = "none";

        var selectE = document.createElement("select");
        selectE.name = "Suspect";
        var selectE2 = document.createElement("select");
        selectE2.name = "Weapon";
        var selectE3 = document.createElement("select");
        selectE3.name = "Room";

        var labelE = document.createElement("label");
        var submitE = document.createElement("input");

        labelE.innerHTML = "<b>Suspect: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Weapon:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Room:</b><div></div>"
        form.innerHTML = "<b>Guess the Hidden Cards:</b><div></div>";
        submitE.type = "submit";
        submitE.value = "Submit";

        for (var i = 0; i < Suspects.length; i++){
            var optionE = document.createElement("option");
            optionE.innerText = Suspects[i];
            selectE.append(optionE);
        }
        for (var i = 0; i < Weapons.length; i++){
            var optionE = document.createElement("option");
            optionE.innerText = Weapons[i];
            selectE2.append(optionE);
        }
        for (var i = 0; i < Rooms.length; i++){
            var optionE = document.createElement("option");
            optionE.innerText = Rooms[i];
            selectE3.append(optionE);
        }

        form.append(labelE);
        form.append(selectE);
        form.append(selectE2);
        form.append(selectE3);
        form.append(submitE);
        document.Form.onsubmit = function temp(){Guess();return false;};
        //Don't set LastForm
    }
    function FormC(){
        var form = document.getElementById("Form");
        form.style.display = "block";
        document.getElementById("NOJump").style.display = "block";

        var selectE = document.createElement("select");
        selectE.name = "Room";
        var submitE = document.createElement("input");
        var labelE = document.createElement("label");

        form.innerHTML = "<b>Which Room do you want to Jump to?</b><div></div>";
        labelE.innerHTML = "<b>Rooms:</b><div></div>";
        submitE.type = "submit";
        submitE.value = "Jump";

        for (var i = 0; i < Rooms.length; i++){
            var optionE = document.createElement("option");
            optionE.innerText = Rooms[i];
            selectE.append(optionE);
        }

        form.append(labelE);
        form.append(selectE);
        form.append(submitE);
        document.Form.onsubmit = function temp(){Jump();return false;};
        lastForm="C";
    }
    function NoForm(){
        var form = document.getElementById("Form");
        var NoJump = document.getElementById("NOJump");
        NoJump.style.display = 'none';
        form.style.display = "none";
        lastForm = "";
    }

    function FormBButtonLogic(){
        var button = document.getElementById("guessCards");
        if(button.innerHTML == "Don't Guess"){
            button.innerHTML = "Guess Hidden Cards";
            if(lastForm == ""){
                NoForm();
            }else if(lastForm == "A"){
                FormA();
            }else if(lastForm == "C"){
                FormC();
            }
        }else{
            button.innerHTML = "Don't Guess";
            FormB();
        }
    }

    //Called By the NOJump button OR when a update with a different Turn which is this players Turn
    function drawForm(){
        if(AIDtoRoom(PlayerAID)!=0){
            FormA();
        }else{
            NoForm();
        }
    }

    function Question(){
        document.getElementById("guessCards").style.display = "none";
        document.getElementById("Skip").style.display = "none";

        var Suspect = document.getElementById("Form").elements.namedItem("Suspect").value;
        var Weapon = document.getElementById("Form").elements.namedItem("Weapon").value;
        NoForm();
        ajaxPOSTReq("question",(AllCards.indexOf(Suspect)+1) + "," + (AllCards.indexOf(Weapon)+1));
    }
    function Guess(){
        var Suspect = document.getElementById("Form").elements.namedItem("Suspect").value;
        var Weapon = document.getElementById("Form").elements.namedItem("Weapon").value;
        var Room = document.getElementById("Form").elements.namedItem("Room").value;
        NoForm();
        ajaxPOSTReq("guess",(AllCards.indexOf(Suspect)+1) + "," + (AllCards.indexOf(Weapon)+1)+ "," +(AllCards.indexOf(Room)+1));
    }
    function Jump(){
        var Room = document.getElementById("Form").elements.namedItem("Room").value;
        FormA();
        ajaxPOSTReq("jump",(AllCards.indexOf(Room)+1));
    }
    function Skip(){
        document.getElementById("Skip").style.display = "none";
        document.getElementById("guessCards").style.display = "none";
        NoForm();
        ajaxGETReq('skip');
    }

</script>
<body onload="GetFirstData();updateMe();" onmousedown="ClickTickCard(event);">
    <img src="" id="Board" width="500" height="500" style="z-index:0; position:absolute; top:140; left:50;">
    <canvas id="canv" width="1000" height="1000" style="z-index:1; position:absolute; top:0; left:0;"></canvas>

    <input type="button" style="z-index:2; position:absolute; top:3; left:7;" value="Leader Board" onclick="window.location.href='LeaderBoard.jsp'"/>
    <input type="button" id="Skip" style="z-index:2; position:absolute; top:20; left:530; text-align:center; width:80px; display:none;" value="Skip Turn" onclick="Skip();"/>
    <form method="post" action="OccupiedGameServlet">
        <button id="quit_btn" name="QuitGID" value="${GID}" onclick="window.name='';" style="z-index:2; position:absolute; top:20; left:800;">Quit Game</button>
    </form>

    <button id="guessCards" style="z-index:2; position:absolute; top:20; left:620; text-align:center; white-space: normal; width:100px; display:none;"  onclick="FormBButtonLogic();">Guess Hidden Cards</button>
    <button id="NOJump" onclick="drawForm();" style="z-index:3; position:absolute; top:706; left:170; display:none;">Don&apos;t Jump</button>
    <form id="Form" name="Form" action="#" style="z-index:2; position:absolute; display:none; top:670; left:10;"></form>
    
    <div id="CommunityCardContainer" style="position: absolute; left:10;top:37;"></div>
    <div id="PlayerCardContainer" style="position: absolute; left:760;top:550;"></div>
    <div id="TickCardContainer" style="position: absolute; left: 580; top:80;"></div>
    <div id="ArrowContainer"></div>
    <div id="ActionListContainer" style="height:380px; width: 320px; overflow-y:scroll; position: absolute; top:100; left:760; z-index: 2; background-color: lightgray;"></div>

    <div class="GameEndOverlay" id="dialog-container">
        <div class="GameEndPopup">
            <p class="EndTitle" id="EndTitle"></p>
            <div class="EndCards" id="EndCards"></div>
            <table class="EndTable" id="EndTable"></table>
            <div>
                <button class="dialog-btn" onclick="window.location.href='Home.jsp'">Ok</button>
            </div>
        </div>
    </div>


</body>
</html>