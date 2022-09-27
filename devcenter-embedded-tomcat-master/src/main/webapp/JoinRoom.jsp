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
        table{
            display: inline-block;
        }
        table, th, td{
            border: 1px solid black;
            text-align: center;
            margin-top: 10px;
        }

        form{
            display: inline-block;
            left:500px;
            position: absolute;
            margin-top: 10px;
        }
    </style>
    <script>
        var data;
        function GETFreeGames(){
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.open("GET", "JoinRoomServlet", true);
            xmlhttp.send();
            xmlhttp.onreadystatechange = function() {
                if (this.readyState == 4 && this.status == 200) {
                    data = JSON.parse(this.responseText);
                    draw(data);
                }
            }
        }

        function draw(data){
            //get GUI
            var MainScreen = document.getElementById("FreeGames");

            //clear current data
            MainScreen.innerHTML="";

            //Show Each Game
            for(var amountOfGames = 0;amountOfGames<data.length;amountOfGames++){

                var div = document.createElement('div');
                //div.align="center";

                //Show Game settings
                var table = document.createElement('table');
                var Row1 = table.insertRow(0);
                var Row1Col1 = Row1.insertCell(0);
                Row1Col1.innerHTML = "Game Name: "+data[amountOfGames][0][2];
                var Row1Col2 = Row1.insertCell(1);
                Row1Col2.innerHTML = "Game Type: "+data[amountOfGames][0][0];
                var Row1Col3 = Row1.insertCell(2);
                Row1Col3.innerHTML = "Max Players: "+data[amountOfGames][0][1];

                //Show Each Player
                for(var amountOfPlayers = 0; amountOfPlayers<data[amountOfGames][1].length;amountOfPlayers++){
                    var tempRowX = table.insertRow();
                    var tempRowXCol1 = tempRowX.insertCell();
                    tempRowXCol1.innerHTML = data[amountOfGames][1][amountOfPlayers][0];
                    var tempRowXCol2 = tempRowX.insertCell();
                    tempRowXCol2.innerHTML = data[amountOfGames][1][amountOfPlayers][1];
                }

                //create a form that contains only a button that holds the GID value
                var form = document.createElement('form');
                form.method="post";
                form.action="JoinRoomServlet";

                var join_button = document.createElement('button');
                join_button.id = "join_btn";
                var GID = data[amountOfGames][0][3];
                join_button.name = "GID";
                join_button.value = GID
                join_button.style.fontSize = "18px";
                join_button.innerHTML = "Join Game";

                //add button to form
                form.appendChild(join_button);

                //add table and Form in a div
                div.appendChild(table);
                div.appendChild(form);

                //Add div to Main Screen
                MainScreen.appendChild(div);
            }
        }
    </script>
</head>

<body onload="GETFreeGames();">
<a href="Home.jsp">Go Back</a>
<button onclick="GETFreeGames();">Refresh</button>
<button onclick="window.location.href='LogoutServlet'" style="display:block; position: absolute; left:430px; top:7px">Log Out</button>
<div id="FreeGames"></div>
</body>
</html>