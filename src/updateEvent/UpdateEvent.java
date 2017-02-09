package updateEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Connessione.Connessione;


public class UpdateEvent extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connessione connessione = new Connessione();
	    Connection con = connessione.connetti();
	    HttpSession session=request.getSession();
	    
	    String startTileHoriz=(String) session.getAttribute("sth");
 		String endTileHoriz=(String) session.getAttribute("eth");
 		String startTileVert=(String) session.getAttribute("stv");
 		String endTileVert=(String) session.getAttribute("etv");
 		String eventDay=(String) session.getAttribute("eventDay");
 		
 		String peakS=request.getParameter("peak");
 		int peak=Integer.parseInt(peakS);
 		String isEvent=request.getParameter("is_event");
 		Statement updateStatement;
		try {
			
			updateStatement = con.createStatement();
			String updateEvent="UPDATE  `event_detected` SET  `event_confirmed` =  '"+isEvent+"' WHERE  date='"+eventDay+
					"' and max_latitude='"+endTileHoriz+"' and max_longitude='"+endTileVert+"' and min_longitude='"+
					startTileVert+"' and min_latitude='"+startTileHoriz+"' and time_peak="+peak;
		    System.out.println(updateEvent);
		    updateStatement.executeUpdate(updateEvent);
		    updateStatement.close();
			
		} catch (SQLException e) {
		}
	}

	
}
