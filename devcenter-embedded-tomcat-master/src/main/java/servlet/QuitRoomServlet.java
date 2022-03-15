package servlet;

import Database.DBManager;
import org.json.simple.parser.ParseException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(
        name = "QuitRoomServlet",
        urlPatterns = {"/quit"}
)
public class QuitRoomServlet extends HttpServlet {
    DBManager dbCon = new DBManager("Cluedo.db");
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            int AID = (int) req.getSession().getAttribute("AID");
            int GID = (int) req.getSession().getAttribute("GID");
            int PID = dbCon.getPID(AID,GID);
            if(PID!=-1) {
                String GameState = dbCon.getGameState(GID);
                if(GameState.equals("free")){
                    dbCon.removeParticipant(AID, GID);
                }
            }
        } catch (SQLException | NullPointerException | ParseException | InterruptedException ignore) {}
        resp.sendRedirect("WaitingRoom.jsp");
    }
}
