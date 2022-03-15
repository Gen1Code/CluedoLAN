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
        name = "ChangePasswordServlet",
        urlPatterns = {"/ChangePasswordServlet"}
)
public class ChangePasswordServlet extends HttpServlet {
    //Connection to Database Object
    DBManager testObject = new DBManager("Cluedo.db");

    //Define regexes
    private final String badCharRegex = ".*[\",\\[\\]].*";
    private final String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*]).{8,}$";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //Get Post Parameters and session attributes
        String Curpass = req.getParameter("CurPassword").trim();
        String Newpass = req.getParameter("NewPassword").trim();
        int AID = (int) req.getSession().getAttribute("AID");


        //matches bad characters
        boolean matches = Pattern.matches(badCharRegex, Newpass);

        //Strong Password matches as true
        boolean matches2 = Pattern.matches(passwordRegex, Newpass);

        //String changes to form correct error pages
        String message = null;

        if(matches) {
            //Don't allow these chars
            message = "None of these characters are Allowed: \",[]";
        }else if(!matches2){
            //Weak Password
            message = "Your password should be contain:<div></div>Should contain at least a capital letter<div></div>" +
                    "Should contain at least a small letter<div></div>" +
                    "Should contain at least a number<div></div>" +
                    "Should contain at least a special character<div></div>" +
                    "And minimum length 8";
        }else{
            try {
                //Get Email from AID, authenticate user using Email and given password
                String Email = testObject.getEmail(AID);
                int ReturnedAID = testObject.authenticate(Email,Curpass);

                //If Password is valid
                if(ReturnedAID!=-1){
                    //Change user password in database and redirect to Home page
                    testObject.setPassword(AID,Newpass);
                    resp.sendRedirect("Home.jsp");
                }else{
                    message = "Incorrect Current Password";
                }
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }

        //If an error occurs code follows through to here, where message is displayed
        PrintWriter out = resp.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<h3> Password Change Failed<div></div>" + message + "<div></div><a href=ChangePassword.jsp>Try Again</a></h3>");
        out.println("</body>");
        out.println("</html>");
        out.flush();
        out.close();
    }
}
