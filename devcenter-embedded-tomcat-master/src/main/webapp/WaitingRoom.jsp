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
        if(AID == null || AID == -1
        || !request.getSession().getId().equals(testObject.getSessionID(AID))
        || testObject.getPID(AID,GID) == -1
        || !testObject.getGameState(GID).equals("free")
        ){
            response.sendRedirect("index.jsp");
        }

    }catch(NullPointerException | SQLException e){
        response.sendRedirect("index.jsp");
    }
%>
<head>
    <style>
        table, th, td{
            border: 1px solid black;
        }
        #join_btn{
            margin-left: 5px;
            margin-top: 5px;
            margin-bottom: 10px;
        }
        #quit_btn{
            margin-left: 5px;
            margin-top: 5px;
            margin-bottom: 10px;
        }
    </style>
    <script>
        var data;
        var UpdateInterval = null;

        function CallUpdateGame(){
            if(UpdateInterval != null){
                window.clearInterval(UpdateInterval);
            }
            UpdateInterval = window.setInterval(updateGame, 1000);
        }

        function updateGame() {
            var xmlhttp = new XMLHttpRequest();
            var url = "WaitingRoomUpdateServlet";
            xmlhttp.onreadystatechange = function () {
                if (this.readyState == 4 && this.status == 200) {
                    if(this.responseText === "full"){
                        window.location.href = "Game.jsp";
                    }else{
                        data = JSON.parse(this.responseText);
                        displayOccupied();
                    }
                }
            };
            xmlhttp.open("GET", url, true);
            xmlhttp.send();
        }

        //COPIED FROM Home.jsp (mostly)
        function displayOccupied(){
            if(data.length != 0){
                //Create GUI
                var GUI = document.getElementById("OccupiedGamePopUp");

                GUI.innerHTML ="";
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

            }
        }

    </script>
</head>
<body onload="CallUpdateGame();">
<button onclick="window.location.href='quit'">Quit Room</button>
<div id="OccupiedGamePopUp"></div>

</body>
</html>