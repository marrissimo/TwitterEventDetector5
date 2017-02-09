package tf_idf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Connessione.Connessione;


public class Tf_idf extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		

		Connessione connessione = new Connessione();
	    Connection con = connessione.connetti();
	    HttpSession session=request.getSession();
	    
	    String eventDay=(String) session.getAttribute("day");
		
		String startTileHorizS=(String) session.getAttribute("sth");
		String endTileHorizS=(String) session.getAttribute("eth");
		String startTileVertS=(String) session.getAttribute("stv");
		String endTileVertS=(String) session.getAttribute("etv");

		double startTileHoriz = Double.parseDouble(startTileHorizS);
	    double startTileVert = Double.parseDouble(startTileVertS);
	    double endTileHoriz=Double.parseDouble(endTileHorizS);
	    double endTileVert=Double.parseDouble(endTileVertS);
		
	    Statement selectTweets;
		try {
			selectTweets = con.createStatement();
			ResultSet tweets=selectTweets.executeQuery("SELECT * FROM `tweets` WHERE latitude>="+startTileHoriz+" and longitude>="+startTileVert+" and latitude<"+endTileHoriz+" and longitude<"+endTileVert+" and date='"+eventDay+"'");
		
			List <String[]> allTerms=new ArrayList<String[]>();
		    HashMap<String,Double> word_dfidf = new HashMap<String, Double>();	
		     
		    while(tweets.next()){
				String message=tweets.getString("message");
				message=message.replaceAll("\\W", " ");
				String[] words = message.split("\\s+|,\\s*|\\.\\s*");
				allTerms.add(words);
			}
			
			
			tweets.beforeFirst();
			while(tweets.next()){
				String message=tweets.getString("message");
				message=message.replaceAll("\\W", " ");
				String[] words = message.split("\\s+|,\\s*|\\.\\s*");
				//System.out.println(message);

				for(int i=0;i<words.length;i++){
					
					double tf=tfCalculator(words, words[i]);
					double idf=idfCalculator(allTerms,words[i]);
					double tfIdf=tf*idf;
					
					word_dfidf.put(words[i], tfIdf);
					
				}
				
			}
			Map<String, Double> sortedMap = sortByComparator(word_dfidf);
			
			for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
				

				System.out.println(entry.getValue()+" "+entry.getKey());
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
	}

	public double tfCalculator(String[] totalterms, String termToCheck) {
        double count = 0;  
        for (String s : totalterms) {
            if (s.equalsIgnoreCase(termToCheck)) {
                count++;
            }
        }
        return count / totalterms.length;
    }
	
	 public double idfCalculator(List <String[]> allTerms, String termToCheck) {
	        double count = 0;
	        for (String[] ss : allTerms) {
	            for (String s : ss) {
	                if (s.equalsIgnoreCase(termToCheck)) {
	                    count++;
	                    break;
	                }
	            }
	        }
	        return 1 + Math.log(allTerms.size() / count);
	    }

	 public double countWord(List <String[]> allTerms, String termToCheck) {
	        double count = 0;
	        for (String[] ss : allTerms) {
	            for (String s : ss) {
	                if (s.equalsIgnoreCase(termToCheck)) {
	                    count++;
	                    break;
	                }
	            }
	        }
	        return  count;
	    }

		private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {

			// Convert Map to List
			List<Map.Entry<String, Double>> list = 
				new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

			// Sort list with comparator, to compare the Map values
			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
				public int compare(Map.Entry<String, Double> o1,
	                                           Map.Entry<String, Double> o2) {
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

			// Convert sorted map back to a Map
			Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
			for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
				Map.Entry<String, Double> entry = it.next();
				sortedMap.put(entry.getKey(), entry.getValue());
			}
			return sortedMap;
		}


}
