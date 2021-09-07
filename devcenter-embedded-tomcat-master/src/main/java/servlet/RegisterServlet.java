package servlet;

import Database.Connect;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet(
        name = "RegisterServlet",
        urlPatterns = {"/RegisterServlet"}
)
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("UserName").trim();
        String email = req.getParameter("Email").trim();
        String pass = req.getParameter("Password").trim();
        String regexv = ".*[\",\\[\\]].*";

        boolean matches = Pattern.matches(regexv, name);
        boolean matches2 = Pattern.matches(regexv, email);
        boolean matches3 = Pattern.matches(regexv, pass);

        if(matches || matches2 || matches3) {
            //Don't allow these chars
            resp.sendRedirect("RegFailedSC.jsp");
        }else{
            Connect testObject = new Connect("Accounts.db");
            String[] columns = {"PlayerName","Email", "Password","Wins","Losses","WrongGuesses","GamesPlayed"};
            String[] values = {name, email, pass,"0","0","0","0"};

            try {
                testObject.insert("TESTAccountDetails", columns, values);
                resp.sendRedirect("RegSuccess.jsp");
            }catch (SQLException e){
                //redirect to correct error pages
                if(e.getErrorCode() == 19){
                    resp.sendRedirect("RegFailedSE.jsp");
                }else{
                    //Debugging usage change to a general error message
                    PrintWriter out = resp.getWriter();
                    out.println("<html>");
                    out.println("<body>");
                    out.println("<h3> Registration Failed<div></div>"+e.getMessage()+"<div></div><a href=Register.jsp>Try Again</a></h3>");
                    out.println("</body>");
                    out.println("</html>");
                    out.flush();
                    out.close();
                }

            }
        }
    }
}
