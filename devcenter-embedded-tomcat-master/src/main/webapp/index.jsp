<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
    </head>
    <body>
        <form action="LoginServlet" method="post">
            <h1>Login Page</h1>
            <p>Email</p>
            <input type="text" placeholder="Email" name="Email" required>
            <p>Password</p>
            <input type="password" placeholder="Password" name="Password" required>
            <input type="submit" value="Login">
            <a href="Register.jsp">Don&#039;t have Account?</a>
        </form>
        <div></div>
    </body>
</html>
