<%@ page import="Database.DBManager" %>
<%@ page import="java.sql.SQLException" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<%
    DBManager testObject = new DBManager("Cluedo.db");
    Integer ID = (Integer) session.getAttribute("AID");
    try{
        //Reject invalid ID
        //Only allow current database marked session to be logged in, blocks multiple sessions
        if(request.getSession().getId().equals(testObject.getSessionID(ID)) && ID !=-1){
            response.sendRedirect("Home.jsp");
        }
    }catch(NullPointerException | SQLException ignore){}
%>
    <head>
        <script>
            var re1 = new RegExp("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$");
            var re2 = new RegExp("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*]).{8,}$");
            var re3 = new RegExp("^(?=.*[\",\\[\\]].*)$");
            var fail = 0;

            function checkInput(){
                var u = document.getElementById("Email").value;
                var p = document.getElementById("Password").value;
                try{
                    let func = new Function('fail=${FailedAttempts};');
                    func();
                }catch(e){}

                if(fail>10){
                    alert("You have Failed to login in too many times\nWait 30 minutes");
                    return false;
                }else if(!re1.test(u)){
                    alert("Your Email isn't valid");
                    return false;
                }else if(!re2.test(p)){
                    alert("Your password should contain:\n" +
                        "At least a capital letter\n" +
                        "At least a small letter\n" +
                        "At least a number\n" +
                        "At least a special character\n" +
                        "And a minimum length of 8");
                    return false;
                }else if(re3.test(p) || re3.test(u)){
                    alert("None of these characters are Allowed: \",[]");
                    return false;
                }else{
                    return true;
                }
            }
        </script>
    </head>
    <body >
        <form action="LoginServlet" method="post" onsubmit="return checkInput();">
            <a href="LeaderBoard.jsp" style="left: 60%; top:15px;position: absolute;font-size: 20px;">Leader Board</a>
            <h1>Login Page</h1>
            <p>Email</p>
            <input type="text" placeholder="Email" name="Email" id="Email" required>
            <p>Password</p>
            <input type="password" placeholder="Password" name="Password" id="Password" required>
            <input type="submit" value="Login">
            <a href="Register.jsp" style="font-size: 18px;">Don&#039;t have Account?</a>
            <div></div>

        </form>
        <div></div>
    </body>
</html>
