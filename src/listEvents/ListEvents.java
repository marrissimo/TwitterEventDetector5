package listEvents;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import Connessione.Connessione;
import Utility.Utility;


public class ListEvents extends HttpServlet {
	
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		response.setContentType("application/json");
		Connessione connessione = new Connessione();
	    Connection con = connessione.connetti();
	    Utility ut=new Utility();
	    HttpSession session=request.getSession();
	    Map<String,  Map<Object, Object>> result = new HashMap<String, Map<Object, Object>>();
	    Map<Object, Object> peaks = new HashMap<Object, Object>();

	    
	    String startTileHorizS=request.getParameter("sth");
		String endTileHorizS=request.getParameter("eth");
		String startTileVertS=request.getParameter("stv");
		String endTileVertS=request.getParameter("etv");
		
		session.setAttribute("sth", startTileHorizS);
		session.setAttribute("eth", endTileHorizS);
		session.setAttribute("stv", startTileVertS);
		session.setAttribute("etv", endTileVertS);
		
		double startTileHoriz = Double.parseDouble(startTileHorizS);
	    double startTileVert = Double.parseDouble(startTileVertS);
	    double endTileHoriz=Double.parseDouble(endTileHorizS);
	    double endTileVert=Double.parseDouble(endTileVertS);
	    String eventDay=(String) session.getAttribute("eventDay");
	    System.out.println(eventDay);
	  
	    try{
	    	 
	         String peaksQuery="SELECT * FROM event_detected WHERE date='"+eventDay
	        		 +"' and min_latitude='"+startTileHoriz+"' AND max_latitude='"+endTileHoriz
	        		 +"' AND min_longitude='"+startTileVert+"' AND max_longitude='"+endTileVert+"'";
	         
			  //System.out.println(peaksQuery);     
		      Statement selectPeaks = con.createStatement();
			  ResultSet events=selectPeaks.executeQuery(peaksQuery);
			  int count=0;
			  while(events.next()){
				  int timePeak=events.getInt("time_peak");
				  peaks.put(count, timePeak);
				  count++;
			  }
			  
				 
			  result.put("peaks", peaks);
			  
			  String json= new Gson().toJson(result);
			  response.getWriter().write(json);
			  
			
	    
	    }catch(Exception e){
	    	
	    }
	}

	
}
