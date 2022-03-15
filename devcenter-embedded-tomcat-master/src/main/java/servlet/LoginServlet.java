package servlet;

import Database.DBManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

@WebServlet(
        name = "LoginServlet",
        urlPatterns = {"/LoginServlet"}
)
public class LoginServlet extends HttpServlet {
    DBManager testObject = new DBManager("Cluedo.db");
    private final int TIMEOUT_MIN = 30;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        //Get Parameters
        String email = req.getParameter("Email").trim().toLowerCase();
        String pass = req.getParameter("Password").trim();

        //dest TDL
        String dest = "index.jsp";

        try {
            int loginFail;
            long Timeout;
            //Get amount of failed attempts for this session, if no attribute then set to 0
            try{
                loginFail = (Integer) req.getSession().getAttribute("FailedAttempts");
            }catch(NullPointerException throwable){
                loginFail = 0;
            }

            //Get Timeout for this session, if no attribute set then ignore
            try {
                //Calculate time since Timeout was set
                Timeout = (long) req.getSession().getAttribute("Timeout");
                int timDif = (int) (req.getSession().getLastAccessedTime() - Timeout);

                //if longer than 30 minutes and Timeout hasn't been reset, then reset attempts and Timeout
                if(timDif>TIMEOUT_MIN*60*1000 && Timeout !=0){
                    loginFail =0;
                    //Reset Timeout
                    req.getSession().setAttribute("Timeout", (long) 0);
                }
            }catch (NullPointerException ignored){
            }

            //If amount failed is less than 10 then authenticate
            if(loginFail <10){
                //Authenticate User
                int AID = -1;
                AID = testObject.authenticate(email,pass);

                //If valid user set them an attribute and SessionID Holder and redirect to Home Page
                if(AID != -1){
                    req.getSession().setAttribute("AID", AID);
                    testObject.setSessionID(AID,req.getSession().getId());
                    dest = "Home.jsp";
                }else{//Invalid user credentials, keep track of attempts
                    loginFail = loginFail +1;
                    req.getSession().setAttribute("FailedAttempts",loginFail);
                }
            }else if(loginFail==10){//User has reached limit of attempts for TIMEOUT_MIN time
                //Set Timeout
                req.getSession().setAttribute("Timeout",req.getSession().getLastAccessedTime());

                //Stay stuck at login Fail = 11 until Timeout is reset
                loginFail = loginFail +1;
                req.getSession().setAttribute("FailedAttempts",loginFail);
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ignore) {}
        resp.sendRedirect(dest);
    }
}
