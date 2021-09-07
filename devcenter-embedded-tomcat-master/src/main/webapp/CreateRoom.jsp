<html>
<head>
<%
    String nameS = (String) session.getAttribute("name");
    try{
        if(nameS.equals("")){
            response.sendRedirect("index.jsp");
        }
    }catch(NullPointerException e){
        response.sendRedirect("index.jsp");
    }
%>

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
</script>
</head>
<body onload="changeFormType();changeFormSize();">
<a href="Home.jsp">Go Back</a>
<a href="LogoutServlet">Log Out</a>
<form method="post" action="CreateRoomServlet">
    <p>Room Name</p>
    <input type="text" pattern="[A-Z0-9a-z]+" minlength="2" maxlength="12" placeholder="Name" name="RoomName" required>

    <p>Type of Game</p>
    <select name="RoomType" id="ChosenType" onchange="changeFormType()">
        <option selected>Normal Game</option>
        <option>Big Game</option>
    </select>

    <p>Room Size</p>
    <select name="RoomSize" id="ChosenSize" onchange="changeFormSize()">
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
    <input type="submit" value="Create Room">
</form>

</body>
</html>