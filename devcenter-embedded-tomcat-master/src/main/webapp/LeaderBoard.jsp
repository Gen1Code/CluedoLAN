<html>
<head>
    <style>
        th, td{
            border: 1px solid #000000;
            text-align: center;
        }
    </style>

    <script>
        var data = [];
        var AID = -1;

        function showButton(){
            try{
                let func = new Function('AID=${AID}');
                func();
            }catch(e){}
            if(AID != -1) {
                document.getElementById("btn").style.display = "block";
            }
        }

        function ajaxGetLeaderBoard(){
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function() {
                if (this.readyState == 4 && this.status == 200) {
                    data = JSON.parse(this.responseText);
                    displayLeaderBoard();
                }
            }
            xmlhttp.open("GET", 'LeaderBoardServlet', true);
            xmlhttp.send();
        }

        function displayLeaderBoard(){
            var innertHtmlString = "<tr><th>Rank</th><th>Name</th><th>Wins</th><th>Losses</th><th>WrongGuesses</th><th>GamesPlayed</th><th>Points</th><th>% Won</th></tr>";
            for(var i=0; i<data.length;i++){
                innertHtmlString += "<tr>";
                innertHtmlString += "<td>"+(i+1)+"</td><td>"+data[i][0]+"</td><td>"+data[i][1]+"</td><td>"+data[i][2]+"</td><td>"+data[i][3]+"</td><td>"+data[i][4]+"</td><td>"+data[i][5]+"</td><td>"+(isNaN(data[i][1]/data[i][4])? 0: Math.round(data[i][1]/data[i][4]*100))+"</td>";
                innertHtmlString += "</tr>";
            }
            document.getElementById("LeaderBoardTable").innerHTML = innertHtmlString;
        }

    </script>
</head>
<body onload="showButton();ajaxGetLeaderBoard();">
    <a href="javascript:history.back()">Go Back</a>
    <button id="btn" onclick="window.location.href='LogoutServlet'" style="display:none; position: absolute; left:55%; top:7px" >Log Out</button>
    <button onclick="ajaxGetLeaderBoard();" style="display:block; position: absolute; left:72px; top:7px">Refresh</button>
    <table id="LeaderBoardTable" style="top: 33px; left:10px; position: absolute;"></table>
</body>
</html>