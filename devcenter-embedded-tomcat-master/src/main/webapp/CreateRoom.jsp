<%@ page import="Database.DBManager" %>
<%@ page import="java.sql.SQLException" %>
<html>
<%
    DBManager testObject = new DBManager("Cluedo.db");
    Integer ID = (Integer) session.getAttribute("AID");
    try{
        //Reject invalid ID
        //Only allow current database marked session to be logged in, blocks multiple sessions
        if(ID == null || ID == -1 || !request.getSession().getId().equals(testObject.getSessionID(ID))){
            response.sendRedirect("index.jsp");
        }
    }catch(NullPointerException | SQLException e){
        response.sendRedirect("index.jsp");
    }
%>
<head>
<script>
    function changeFormType(){
        if(document.getElementById("ChosenType").value == "Big Game"){
            document.getElementById("BGOption").style.display = "block";
            document.getElementById("BGOption2").style.display = "block";
            document.getElementById("NGOption").style.display = "none";
            document.getElementById("NGOption2").style.display = "none";
            if(document.getElementById("ChosenSize").value<5){
                document.getElementById("ChosenSize").value = 5;
            }
        }else{
            document.getElementById("BGOption").style.display = "none";
            document.getElementById("BGOption2").style.display = "none";
            document.getElementById("NGOption").style.display = "block";
            document.getElementById("NGOption2").style.display = "block";
            if(document.getElementById("ChosenSize").value>5){
                document.getElementById("ChosenSize").value = 5;
            }
        }
    }
    function changeFormSize(){
        if(document.getElementById("ChosenSize").value ==3){
            document.getElementById("size3").style.display = "none";
            document.getElementById("size4").style.display = "none";
            document.getElementById("size5").style.display = "none";
            document.getElementById("size6").style.display = "none";
        }else if(document.getElementById("ChosenSize").value ==4){
            document.getElementById("size3").style.display = "block";
            document.getElementById("size4").style.display = "none";
            document.getElementById("size5").style.display = "none";
            document.getElementById("size6").style.display = "none";
        }else if(document.getElementById("ChosenSize").value ==5){
            document.getElementById("size3").style.display = "block";
            document.getElementById("size4").style.display = "block";
            document.getElementById("size5").style.display = "none";
            document.getElementById("size6").style.display = "none";
        }else if(document.getElementById("ChosenSize").value ==6){
            document.getElementById("size3").style.display = "block";
            document.getElementById("size4").style.display = "block";
            document.getElementById("size5").style.display = "block";
            document.getElementById("size6").style.display = "none";
        }else{
            document.getElementById("size3").style.display = "block";
            document.getElementById("size4").style.display = "block";
            document.getElementById("size5").style.display = "block";
            document.getElementById("size6").style.display = "block";
        }
    }

    var re1 = new RegExp("^[A-Z0-9a-z ]{2,12}$");

    function checkInput(){
        var n = document.getElementById("GameName").value;
        var t = document.getElementById("ChosenType").value;
        var s = document.getElementById("ChosenSize").value
        var b = document.getElementById("BotNumber").value;
        if(!re1.test(n)){
            alert("Name must be between 2 and 12 characters and only contain alphanumeric characters");
            return false;
        }else if((t == "Big Game" && (s<5 || s>7)) || (t == "Normal Game" && (s<3 || s>5))){
            alert("Your Room Size is incompatible with the Type of Game");
            return false;
        }else if(b+1 > s){
            alert("You have too many Bots for the Game Size you chose");
            return false;
        }else{
            return true;
        }
    }
</script>
</head>
<body onload="changeFormType();changeFormSize();">
<a href="Home.jsp">Go Back</a>
<button onclick="window.location.href='LogoutServlet'">Log Out</button>
<form method="post" action="CreateRoomServlet" onsubmit="return checkInput();">
    <p>Game Name</p>
    <input type="text" placeholder="Name" name="GameName" id="GameName" required>

    <p>Type of Game</p>
    <select name="GameType" id="ChosenType" onchange="changeFormType()">
        <option selected>Normal Game</option>
        <option>Big Game</option>
    </select>

    <p>Game Size</p>
    <select name="GameSize" id="ChosenSize" onchange="changeFormSize()">
        <option id="NGOption">3</option>
        <option id="NGOption2">4</option>
        <option selected>5</option>
        <option id="BGOption">6</option>
        <option id="BGOption2">7</option>
    </select>

    <p>Number of bots</p>
    <select name="NoOfBots" id="BotNumber">
        <option selected>0</option>
        <option>1</option>
        <option>2</option>
        <option id="size3">3</option>
        <option id="size4">4</option>
        <option id="size5">5</option>
        <option id="size6">6</option>
    </select>
    <input type="submit" value="Create Game">
</form>

</body>
</html>