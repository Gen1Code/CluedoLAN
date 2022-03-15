<!DOCTYPE html>
<html>
    <head>
        <script>
            var re1 = new RegExp("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$");
            var re2 = new RegExp("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*]).{8,}$");
            var re3 = new RegExp("^(?=.*[\",\\[\\]].*)$");

            function checkInput() {
                var n = document.getElementById("UserName").value;
                var u = document.getElementById("Email").value;
                var uc = document.getElementById("ConEmail").value;
                var p = document.getElementById("Password").value;
                var pc = document.getElementById("ConPassword").value;
                if (!(u == uc)) {
                    alert("Your Email doesn't match Confirmed Email");
                    return false;
                } else if (!(p == pc)) {
                    alert("Your Password doesn't match Confirmed Password");
                    return false;
                } else if (!re1.test(u)) {
                    alert("Your Email isn't valid");
                    return false;
                } else if (!re2.test(p)) {
                    alert("Your password should contain:\n" +
                        "At least a capital letter\n" +
                        "At least a small letter\n" +
                        "At least a number\n" +
                        "At least a special character\n" +
                        "And a minimum length of 8");
                    return false;
                }else if(re3.test(n) || re3.test(u) || re3.test(p)){
                    alert("None of these characters are Allowed: \",[]");
                    return false;
                }else if(u.length<=2){
                    alert("Your User Name is too short");
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
            <input type="text" id="UserName" placeholder="Username" name="UserName" required>
            <p>Email</p>
            <input type="text" placeholder="Email" name="Email" id="Email" required>
            <p>Confirm Email</p>
            <input type="text" placeholder="Confirm Email" name="ConEmail" id="ConEmail" required>
            <p>Password</p>
            <input type="password" placeholder="Password" name="Password" id="Password" required>
            <p>Confirm Password</p>
            <input type="password" placeholder="Confirm Password" name="ConPassword" id="ConPassword" required>
            <input type="submit" value="Register">
            <a href="index.jsp">Already have Account?</a>
        </form>
        <div>
        </div>
    </body>
</html>
