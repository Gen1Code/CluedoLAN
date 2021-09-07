package servlet;

import Database.Connect;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(
        name = "LoginServlet",
        urlPatterns = {"/LoginServlet"}
)
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String email = req.getParameter("Email").trim();
        String pass = req.getParameter("Password").trim();

        Connect testObject = new Connect("Accounts.db");

        String[] columns = {"ID","PlayerName","Email", "Password","Wins","Losses","WrongGuesses","GamesPlayed","Points"};
        String[] values = {email, pass};

        try {
            ArrayList<String[]> result;

            result = testObject.query(columns, "TESTAccountDetails","Email =\""+ values[0]+"\" AND Password =\""+values[1]+"\"");
            if(result.size() != 0) {
                //Make a current User
                int id = Integer.parseInt(result.get(0)[0]);
                String name = result.get(0)[1];
                System.out.println("Welcome "+name);
                req.getSession().setAttribute("name", name);
                resp.sendRedirect("Home.jsp");

            }else{
                resp.sendRedirect("FailedLogin.jsp");
            }
        }catch (SQLException e){
            resp.sendRedirect("FailedLogin.jsp");
        }
    }


}
