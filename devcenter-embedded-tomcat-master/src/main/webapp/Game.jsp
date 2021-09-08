<html>
<%
        try{
        String nameS = (String) session.getAttribute("name");
                int IDS = (int) session.getAttribute("RoomID");
        if(nameS.equals("null") || nameS.equals("") || IDS == -1){
            response.sendRedirect("index.jsp");
        }
        }catch(NullPointerException e){
            response.sendRedirect("index.jsp");
        }
%>
<script src="https://cdn.jsdelivr.net/npm/lodash@4.17.10/lodash.min.js"></script>

<script>
    var data = {};
    var Board = {};
    var Dices = [];
    var UpdateInterval = null;

    function updateMe(){
        if(UpdateInterval != null){
            window.clearInterval(UpdateInterval);
        }
        UpdateInterval = window.setInterval(update, 1000);
    }

    function update(){
        var xmlhttp = new XMLHttpRequest();
        var url = "update";
        xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            data = JSON.parse(this.responseText);
            var canvas = document.getElementById('canv');
            var ctx = canvas.getContext('2d');

            //Imported _.isEqual to compare 2 JSON Objects
            if(!(_.isEqual(Board,data.Board))){
                drawBoard(data.Board,ctx);
                Board = data.Board;
            }
            if(!(_.isEqual(Dices,data.Dices))){
                drawDices(data.Dices,ctx);
                Dices = data.Dices;

            }
        }
        };
        xmlhttp.open("GET", url, true);
        xmlhttp.send();
    }
    function drawDices(DicesData,ctx){
        var img= new Image();
        var img2 = new Image();
        img.src = "assets/Dice_"+DicesData[0]+".jpg";
        img2.src = "assets/Dice_"+DicesData[1]+".jpg";
        img.onload = function() {ctx.drawImage(img, 100, 15,27, 27);}
        img2.onload = function() {ctx.drawImage(img2, 130, 15,27, 27);}
    }

    function drawBoard(BoardData,ctx){
        for(var i=0;i<BoardData.length;i++){
            drawPlayer(BoardData[i].row,BoardData[i].column,BoardData[i].floor,BoardData[i].colour,ctx);
        }
    }

    function drawPlayer(row,column,floor,colour,ctx){
        ctx.beginPath();
        if(floor==0){
            ctx.arc(column*20.9 +61,row*19.93 +62, 8, 0, 2 * Math.PI);
        }
        ctx.fillStyle = colour;
        ctx.fill();
    }

    //drawimages and other starting
    function drawStart(){
        var canvas = document.getElementById('canv');
        var ctx = canvas.getContext('2d');
        var img = new Image();
        img.src = "assets/Board.png";
        img.onload = function(){
            ctx.drawImage(img,50,50,500,500);
        }
    }

    function ajaxGETReq(url){
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open("GET", url, true);
        xmlhttp.send();
    }

    function ajaxPOSTReq(url,Sdata){
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open("POST", url, true);
        xmlhttp.send(Sdata);
    }

</script>
<body onload="updateMe();drawStart();">

    <canvas id="canv" width="2000" height="2000"></canvas>
    <form id="Form"></form>
</body>
</html>