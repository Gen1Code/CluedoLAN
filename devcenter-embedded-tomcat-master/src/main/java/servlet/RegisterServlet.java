package servlet;

import Database.DBManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet(
        name = "RegisterServlet",
        urlPatterns = {"/RegisterServlet"}
)
public class RegisterServlet extends HttpServlet {
    //Connection to Database Object
    DBManager testObject = new DBManager("Cluedo.db");

    //Define regexes
    private final String badCharRegex = ".*[\",\\[\\]].*";
    private final String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*]).{8,}$";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        //Get Post Parameters
        String name = req.getParameter("UserName").trim();
        String email = req.getParameter("Email").trim().toLowerCase();
        String pass = req.getParameter("Password").trim();

        //matches bad characters
        boolean matches = Pattern.matches(badCharRegex, name);
        boolean matches2 = Pattern.matches(badCharRegex, email);
        boolean matches3 = Pattern.matches(badCharRegex, pass);

        //Strong Password matches as true
        boolean matches4 = Pattern.matches(passwordRegex, pass);

        //String changes to form correct error pages
        String message = null;

        try {
            if(matches || matches2 || matches3) {
                //Don't allow these chars
                message = "None of these characters are Allowed: \",[]";
            }else if(!matches4){
                //Weak Password
                message = "Your password should contain:<div></div>At least a capital letter<div></div>" +
                        "At least a small letter<div></div>" +
                        "At least a number<div></div>" +
                        "At least a special character<div></div>" +
                        "And a minimum length of 8";
            }else{
                //Add user to database and redirect to login page
                testObject.addUser(name,email,pass);
                resp.sendRedirect("index.jsp");
            }
        }catch (SQLException e) {
            if (e.getErrorCode() == 19) {
                message = "This Email has already been used<div></div>Please use a different one";
            }else{
                message = "General Database Error, Try Again";
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        //If an error occurs code follows through to here, where message is displayed
        PrintWriter out = resp.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<h3> Registration Failed<div></div>" + message + "<div></div><a href=Register.jsp>Try Again</a></h3>");
        out.println("</body>");
        out.println("</html>");
        out.flush();
        out.close();



    }
}
