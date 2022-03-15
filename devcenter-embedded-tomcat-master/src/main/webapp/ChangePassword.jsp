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
        function checkInput(){
            var p = document.getElementById("NewPassword");
            var pc = document.getElementById("ConPassword");
            if(!(p.value == pc.value)){
                alert("Your New Password doesn't match Confirmed Password");
                return false;
            }else{
                return true;
            }
        }
    </script>
</head>
<body>
<form action="ChangePasswordServlet" method="post" onsubmit="return checkInput()">
    <h1>Change Password</h1>
    <p>Current Password</p>
    <input type="password" pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*]).{8,}$" placeholder="Password" name="CurPassword" id="CurPassword"required>
    <p>New Password</p>
    <input type="password" pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*]).{8,}$" placeholder="Password" name="NewPassword" id="NewPassword"required>
    <p>Confirm Password</p>
    <input type="password" placeholder="Confirm Password" name="ConPassword" id="ConPassword" required>
    <input type="submit" value="Change Password">
    <a href="Home.jsp">Go Back</a>
</form>
</body>
</html>