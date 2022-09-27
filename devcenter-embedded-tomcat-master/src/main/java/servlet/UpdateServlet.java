package servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

@WebServlet(
        name = "UpdateServlet",
        urlPatterns = {"/update"}
)
public class UpdateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        //If No AID and GID ignore request
        try{
            //Grab Session Attributes
            int GID = (int) req.getSession().getAttribute("GID");
            int AID = (int) req.getSession().getAttribute("AID");

            File myObj = new File("src/main/java/Database/GameUpdateFiles/" +GID+ "," +AID+ ".txt");


            //Gets Last modified Timestamp
            long timestamp = myObj.lastModified();
            long timestamp2 = 0;

            //If no TimeStamp Attribute then ignore error and continue as if timestamps are different
            try{
                timestamp2 = (long) req.getSession().getAttribute("DataTimeStamp");
            }catch (NullPointerException ignore){}

            if(timestamp == timestamp2){
                //Don't send data
                resp.getWriter().write("{}");
            }else{
                Scanner myReader = new Scanner(myObj);
                //Update Timestamp
                req.getSession().setAttribute("DataTimeStamp",timestamp);

                //Get Data
                String data = myReader.nextLine();

                //Send Data
                resp.getWriter().write(data);
                myReader.close();
            }
        }catch(FileNotFoundException | NullPointerException | NoSuchElementException ignore){}
    }


    //First time loading the page don't care about timestamp
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try{
            //Grab Session Attributes
            int GID = (int) req.getSession().getAttribute("GID");
            int AID = (int) req.getSession().getAttribute("AID");

            File myObj = new File("src/main/java/Database/GameUpdateFiles/" +GID+ "," +AID+ ".txt");
            Scanner myReader = new Scanner(myObj);

            //Get Data
            String data = myReader.nextLine();
            myReader.close();

            //Send Data
            resp.getWriter().write(data);
        }catch(FileNotFoundException | NullPointerException ignore){}
    }
}
