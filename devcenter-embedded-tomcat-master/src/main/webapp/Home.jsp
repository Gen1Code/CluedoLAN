<%@ page import="Database.DBManager" %>
<%@ page import="java.sql.SQLException" %>
<html>
<%
    DBManager testObject = new DBManager("Cluedo.db");
    Integer ID = (Integer) session.getAttribute("AID");
    try{
        //Reject invalid ID
        //Only allow current database marked session to be logged in, blocks multiple sessions
        if(ID == null || ID == -1
        || !request.getSession().getId().equals(testObject.getSessionID(ID))
        ){
            response.sendRedirect("index.jsp");
        }
    }catch(NullPointerException | SQLException e){
        response.sendRedirect("index.jsp");
    }
%>
<head>
    <style>
        form {
            width: 300px;
            margin: 0 auto;
            text-align: center;
            padding-top: 0px;
        }

        .value-button {
            display: inline-block;
            border: 1px solid #ddd;
            margin: 0px;
            width: 40px;
            height: 20px;
            text-align: center;
            vertical-align: middle;
            padding: 12px 0;
            background: #eee;
        }

        input[type=number]::-webkit-inner-spin-button,
        input[type=number]::-webkit-outer-spin-button {
            -webkit-appearance: none;
            margin: 0;
        }

        .value-button:hover {
            cursor: pointer;
        }

        table, th, td{
            border: 1px solid black;
        }

        .logOut-button{
            margin-left: 5px;
            margin-top: 5px;
            margin-bottom: 10px;
        }
        .Menu-btn{
            font-size: 30px;
            margin-top: 30px;
        }
        .SettingsPopup {
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

        .SettingsOverlay {
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
            font-weight: 900;
            border: 1px solid #44B78B;
            border-radius: 10px;
            height: 30px;
            width: 30%;
            margin-left: 5px;
            margin-top: 5px;
            margin-bottom: 10px;
        }

        .dialog-btn:hover {
            background-color:#015249;
            cursor: pointer;
        }

        .checkBox{
            padding-left: 15px;
        }

    </style>

    <script>
        var data;
        function ajaxCheckOccupied(){
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function() {
                if (this.readyState == 4 && this.status == 200) {
                    data = JSON.parse(this.responseText);
                    displayOccupied();
                }
            }
            xmlhttp.open("GET", 'OccupiedGameServlet', true);
            xmlhttp.send();
        }

        //display Occupied Data
        function displayOccupied(){
            if(data.length != 0){
                //Remove all options
                document.getElementById("Force").style.display="none";

                //Create GUI
                var GUI = document.getElementById("OccupiedGamePopUp");

                //Show Game settings
                var temp = document.createElement('table');
                var Row1 = temp.insertRow(0);
                var Row1Col1 = Row1.insertCell(0);
                Row1Col1.innerHTML = "Game Name: "+data[0][2];
                var Row1Col2 = Row1.insertCell(1);
                Row1Col2.innerHTML = "Game Type: "+data[0][0];
                var Row1Col3 = Row1.insertCell(2);
                Row1Col3.innerHTML = "Max Players: "+data[0][1];

                //Show Each Player
                for(var amountOfPlayers = 0; amountOfPlayers<data[1].length;amountOfPlayers++){
                    var tempRowX = temp.insertRow();
                    var tempRowXCol1 = tempRowX.insertCell();
                    tempRowXCol1.innerHTML = data[1][amountOfPlayers][0];
                    var tempRowXCol2 = tempRowX.insertCell();
                    tempRowXCol2.innerHTML = data[1][amountOfPlayers][1];
                    var tempRowXCol3 = tempRowX.insertCell();
                }

                //Add Game to GUI
                GUI.appendChild(temp);

                //create a form that contains two buttons that holds the GID value
                var GID = data[0][3];
                var form = document.createElement('form');
                form.method="post";
                form.action="OccupiedGameServlet";

                //Set GID attribute button
                var join_button = document.createElement('button');
                join_button.id = "join_btn";
                join_button.name = "SetGID";
                join_button.value = GID
                join_button.innerHTML = "Rejoin Game";

                //Remove Participant button
                var quit_button = document.createElement('button');
                quit_button.id = "quit_btn";
                quit_button.name = "QuitGID";
                quit_button.value = GID
                quit_button.innerHTML = "Quit Game";

                //add button to form
                form.appendChild(join_button);
                form.appendChild(quit_button);

                //Add form to div
                GUI.appendChild(form);
            }
        }

        function popup(){
            var Container = document.getElementById("dialog-container");
            Container.style.display = "block";
        }
        function closePopup(){
            document.getElementById("dialog-container").style.display = "none";
        }


        function changeGSValue(up){
            if(up){
                if(document.getElementById("GameSize").value <7){
                    increaseValue("GameSize");
                }
            }else{
                if(document.getElementById("GameSize").value >3){
                    decreaseValue("GameSize");
                }
            }
        }
        function changeNBValue(up){
            if(up){
                if(document.getElementById("Bots").value <6){
                    increaseValue("Bots");
                }
            }else{
                if(document.getElementById("Bots").value >0){
                    decreaseValue("Bots");
                }
            }
        }

        function increaseValue(id) {
            var value = parseInt(document.getElementById(id).value, 10);
            value = isNaN(value) ? 5 : value;
            value++;
            document.getElementById(id).value = value;
        }
        function decreaseValue(id) {
            var value = parseInt(document.getElementById(id).value, 10);
            value = isNaN(value) ? 5 : value;
            value--;
            document.getElementById(id).value = value;
        }

        //Checks if Setting are appropriate to create a Game
        function CheckInput(){
            var NB = document.getElementById("Bots").value;
            var GS = document.getElementById("GameSize").value;

            if(isNaN(NB) || isNaN(GS) || NB =="" || GS == "" || NB.includes("e") || GS.includes("e") || GS>7 || GS<3){
                alert("Enter Valid Numbers");
                return false;
            }else if(NB >= GS && !document.getElementById("NBAny").checked && !document.getElementById("GSAny").checked){
                alert("You chose too many Bots for the Game Size allocated");
                return false;
            }else if(GS>5 && document.getElementById("GT2").checked && !document.getElementById("GSAny").checked){
                alert("You chose too big a Game Size for a Normal Game");
                return false;
            }else if(GS<5 && document.getElementById("GT3").checked && !document.getElementById("GSAny").checked){
                alert("You chose too small a Game Size for a Big Game");
                return false;
            }
            return true;
        }

    </script>
</head>
<body onload="ajaxCheckOccupied();">
    <div id="Force">
        <div>
            <a href="LeaderBoard.jsp">Leader Board</a>
            <a href="ChangePassword.jsp">Change Password</a>
            <button class="logOut-button" onclick="window.location.href='LogoutServlet'">Log Out</button>
        </div>
        <div align="center">
            <div>
                <button class="Menu-btn" onclick="window.location.href='CreateRoom.jsp'">Create Room</button>
            </div>
            <div>
                <button class="Menu-btn" onclick="window.location.href='JoinRoom.jsp'">Join Room Manually</button>
            </div>
            <div>
                <button class="Menu-btn" onclick="popup();">Quick Join</button>
            </div>
       </div>
    </div>

   <div id="OccupiedGamePopUp"></div>

    <div class="SettingsOverlay" id="dialog-container">
        <div class="SettingsPopup">
            <div>
                <button class="dialog-btn" style="align-self: right;" onclick="closePopup();">Close</button>
            </div>
            <div>
                <form method="post" action="JoinAutoRoomServlet" onsubmit="return CheckInput();">
                    <div>
                        <p style="font-weight: 800;">Type of Game</p>
                        <label for="GT1">Any</label> <input type="radio" name="GameType" id="GT1" value="Any" required>
                        <label for="GT2">Normal Game</label> <input type="radio" name="GameType" id="GT2" value="Normal Game">
                        <label for="GT3">Big Game</label> <input type="radio" name="GameType" id="GT3" value="Big Game">
                    </div>
                    <div>
                        <p style="font-weight: 800;">Game Size</p>
                        <div class="value-button" style="margin-right: -4px;border-radius: 8px 0 0 8px;" onclick="changeGSValue(false)">-</div>
                        <input type="number" name="GameSize" style="text-align: center;border: none;border-top: 1px solid #ddd;border-bottom: 1px solid #ddd;margin: 0px;width: 40px;height: 46px;" id="GameSize" value="3" onKeyDown="return false"/>
                        <div class="value-button" style="margin-left: -4px;border-radius: 0 8px 8px 0;" onclick="changeGSValue(true)" >+</div>
                        <input type="checkbox" id="GSAny" name="GSAny" class="checkBox">
                        <label for="GSAny">Any</label>
                    </div>
                    <div>
                        <p style="font-weight: 800;">Number of Bots</p>
                        <div class="value-button" style="margin-right: -4px;border-radius: 8px 0 0 8px;" onclick="changeNBValue(false)">-</div>
                        <input type="number" name="Bots" style="text-align: center;border: none;border-top: 1px solid #ddd;border-bottom: 1px solid #ddd;margin: 0px;width: 40px;height: 46px;" id="Bots" value="0" onKeyDown="return false"/>
                        <div class="value-button" style="margin-left: -4px;border-radius: 0 8px 8px 0;" onclick="changeNBValue(true)">+</div>
                        <input type="checkbox" id="NBAny" name="NBAny" class="checkBox">
                        <label for="NBAny">Any</label>
                    </div>
                    <div>
                        <input type="submit" style="margin-top: 10px;" class="dialog-btn">
                    </div>
                </form>
            </div>
        </div>
    </div>
</body>
</html>