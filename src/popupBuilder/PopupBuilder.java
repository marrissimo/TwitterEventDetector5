package popupBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import Connessione.Connessione;
import Utility.Utility;


public class PopupBuilder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String[] tagsToDelete = {
			"CC","PRP","PRP$","RBR","RBS","TO","WDT","WP","WP$","WRB","UH",".","DT",",","IN",":"," ","-RRB-","SYM","VBZ","CD"
		};   

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		Connessione connessione = new Connessione();
	    Connection con = connessione.connetti();
	    Utility ut=new Utility();
	    HttpSession session=request.getSession();
	    
	    Map<String,  Map<Object, Object>> result = new HashMap<String, Map<Object, Object>>();
	    Map<Object, Object> timeSeries = new HashMap<Object, Object>();

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
	    
	    
	    
	  // CARICA TWEET NELL'AREA SELEZIONATA
	    try{
		    for(int k=0; k<24;k++){
		    	 String timeMin=ut.convertSecondsToHhMmSs(k*3600);
				 String timeMax=ut.convertSecondsToHhMmSs(3599+k*3600);
				 Statement selectUsers = con.createStatement();
				 ResultSet users = selectUsers.executeQuery("select count(distinct(user_id)) nUsers from tweets where time>='"+timeMin+"' AND time<='"+timeMax+"' AND date='"+eventDay+"' AND latitude>='"+startTileHoriz+"' AND latitude<'"+endTileHoriz+"' AND longitude>='"+startTileVert+"' AND longitude<'"+endTileVert+"'");
				 
				 int nUsers=0;
				 if(users.next()){
					 nUsers=users.getInt("nUsers");
					 System.out.println(k+" "+nUsers);
				 }
				 timeSeries.put(k, nUsers);
				 selectUsers.close();
			} 
	    result.put("timeSeries", timeSeries);
	    
	    String json= new Gson().toJson(result);
	    response.getWriter().write(json);
	   
	    
	    }catch(Exception e){
	    	
	    }
		
	}
	
	public int countWord(Map<Integer, List<String>>  wordsList, String termToCheck) {
        int count = 0;
        
        for (Entry<Integer, List<String>> entry : wordsList.entrySet()) {
			 for (String word : entry.getValue()) {
				 
                if (word.equalsIgnoreCase(termToCheck)) {
                    count++;
                }
        	}

        }
        
        return  count;
    }

	public int countWord(List<List<String>>  allTerms, String termToCheck) {
        int count = 0;
        
        for (List<String> hashtag : allTerms) {
        	for (String tag : hashtag) {
                if (tag.equalsIgnoreCase(termToCheck)) {
                    count++;
                }
        	}
        }
        
        return  count;
    }
	
	 private static Map<String, Integer> sortByComparatorInt(Map<String, Integer> unsortMap) {

			// Convert Map to List
			List<Map.Entry<String, Integer>> list = 
				new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

			// Sort list with comparator, to compare the Map values
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1,
	                                           Map.Entry<String, Integer> o2) {
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

			Collections.reverse(list);
			// Convert sorted map back to a Map
			Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
			for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
				Map.Entry<String, Integer> entry = it.next();
				sortedMap.put(entry.getKey(), entry.getValue());
			}
			return sortedMap;
		}



}
