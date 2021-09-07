<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <script>
            function checkInput(){
                var u = document.getElementById("Email");
                var uc = document.getElementById("ConEmail");
                if(!(u.value == uc.value)){
                    alert("Your Email doesn't match Confirmed Email");
                    return false;
                }else{
                    return true;
                }
            }
        </script>
    </head>
    <body>
        <form action="RegisterServlet" method="post" onsubmit="return checkInput()">
            <h1>Register</h1>
            <p>Username</p>
            <input type="text" minlength="2" placeholder="Username" name="UserName" required>
            <p>Email</p>
            <input type="text" pattern="^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$" placeholder="Email" name="Email" id="Email" required>
            <p>Confirm Email</p>
            <input type="text" placeholder="Confirm Email" name="ConEmail" id="ConEmail" required>
            <p>Password</p>
            <input type="password" minlength="8" placeholder="Password" name="Password" required>
            <input type="submit" value="Register">
            <a href="index.jsp">Already have Account?</a>
        </form>
        </div>
        </div>
    </body>
</html>
