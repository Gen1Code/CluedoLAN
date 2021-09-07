<html>
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
<head>

</head>
<body>
<a href="">Leader Board</a>
<a href="LogoutServlet">Log Out</a>
<a href="CreateRoom.jsp">Create Room</a>
<a href="">Join Room</a>


</body>
</html>