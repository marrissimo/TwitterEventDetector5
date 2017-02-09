package UpdatePeaks;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.timeseries.TimeSeriesBase.Builder;
import com.fastdtw.util.Distances;
import com.google.gson.Gson;

import Connessione.Connessione;
import Utility.Utility;



public class UpdatePeak2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
  
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session=request.getSession();
		Connessione connessione = new Connessione();
		Utility ut=new Utility();
		
		Calendar cal = Calendar.getInstance();
		String today=ut.correctFormattedData(cal);
		
		String eventDay=request.getParameter("day");
		String area=request.getParameter("area");
		String fromDate=request.getParameter("fromDate");
		String toDate=request.getParameter("toDate");
		
		String dtS=request.getParameter("dt");
		String deltaS=request.getParameter("delta");

		String startTileHorizS=(String) session.getAttribute("sth");
		String endTileHorizS=(String) session.getAttribute("eth");
		String startTileVertS=(String) session.getAttribute("stv");
		String endTileVertS=(String) session.getAttribute("etv");
		
		double dt = Double.parseDouble(dtS);
		double delta = Double.parseDouble(deltaS);

		double startTileHoriz = Double.parseDouble(startTileHorizS);
        double startTileVert = Double.parseDouble(startTileVertS);
        double endTileHoriz=Double.parseDouble(endTileHorizS);
        double endTileVert=Double.parseDouble(endTileVertS);
        
     
        Map<Integer, String> result = new HashMap();
        response.setContentType("application/json");
      
    	Connection con = connessione.connetti();
    	
    	try {
			Statement selectCities = con.createStatement();
			ResultSet cities = selectCities.executeQuery("select * from my_cities where id='"+area+"'");
			
			if(cities.next()){
				 String city = cities.getString("id");
				 Statement selectDays = con.createStatement();
				 ResultSet days = selectDays.executeQuery("select distinct(date) from tweets where area='"+city+"' and date>='"+fromDate+"' and date<='"+toDate+"'");
				 double totDays=0.0;
				 while(days.next()){
					 totDays=totDays+1;
				 }
				 selectDays.close();
				 Statement selectIsTweets=con.createStatement();
				 ResultSet isTweets=selectIsTweets.executeQuery("SELECT * FROM tweets WHERE area='"+city+"' and date='"+eventDay+"'");
				
				 //Statement selectPeaks=con.createStatement();
				// ResultSet peaks=selectPeaks.executeQuery("SELECT * FROM peaks_detected WHERE area='"+city+"' and date='"+eventDay+"' AND up=1");
				 if(!isTweets.next() || eventDay=="" || eventDay==null){
						System.out.println("Nessun picco aggiornato");
						response.getWriter().write("Non ci sono tweets per questo giorno");
						
						
				 }else{
					 HashMap<Integer,Double> checkMax = new HashMap<Integer,Double>();
					 Builder b1 = TimeSeriesBase.builder();
					 Builder b2 = TimeSeriesBase.builder();
					
					 for(int k=0; k<24;k++){
						 String time1=ut.convertSecondsToHhMmSs(k*3600);
						 String time2=ut.convertSecondsToHhMmSs(3599+k*3600);
						 
						 Statement selectTweets = con.createStatement();
						 ResultSet users = selectTweets.executeQuery("select count(distinct(user_id)) num_users "
						 		+ "from tweets where area='"+city+"' AND time>='"+time1+"' "
						 		+ "AND time<='"+time2+"'  AND latitude>='"+startTileHoriz+"' AND latitude<'"+endTileHoriz+"' "
						 		+ "AND longitude>='"+startTileVert+"' AND longitude<'"+endTileVert+"' and "
						 		+ "date>='"+fromDate+"' and date<='"+toDate+"'");
						 
						 double numUsers=0;
						 double mediaUsers=0;
						 if(users.next()){
							 numUsers=users.getDouble("num_users");
							 mediaUsers=numUsers/totDays;
							
							 b1.add(k, mediaUsers);
						 }else{
							 System.out.println("Tabella tweets vuota per "+city);
							 System.exit(0);
						 }
						 selectTweets.close();
						  
						 Statement selectEventDayUsers = con.createStatement();
						 ResultSet eventDayUsers = selectEventDayUsers.executeQuery("select count(distinct(user_id)) nEventDayUsers from tweets where area='"+city+"' AND time>='"+time1+"' AND time<='"+time2+"' AND date='"+eventDay+"' AND latitude>='"+startTileHoriz+"' AND latitude<'"+endTileHoriz+"' AND longitude>='"+startTileVert+"' AND longitude<'"+endTileVert+"'");
						 
						 double nEventDayUsers=0.0;
						 if(eventDayUsers.next()){
							 nEventDayUsers=eventDayUsers.getDouble("nEventDayUsers");
							 b2.add(k,nEventDayUsers);
							 }
						 selectEventDayUsers.close();
						 checkMax.put(k, nEventDayUsers);
						 System.out.println(k+" "+mediaUsers+" "+nEventDayUsers);
					 }
					 com.fastdtw.timeseries.TimeSeries t1=b1.build();
					 com.fastdtw.timeseries.TimeSeries t2=b2.build();
					 double distance = FastDTW.compare(t1, t2, 10, Distances.EUCLIDEAN_DISTANCE)
							 .getDistance();
					
					 
					 boolean localPeak=peak_detection(checkMax,delta,dt);
				
					 //if(!localPeak){
						 //response.getWriter().write(" Cambia parametri Crest Detection");
						
					 //}
					 
					 Statement selectMean = con.createStatement();
					 ResultSet mean=selectMean.executeQuery("SELECT * FROM variance WHERE date='"+today+"' and area='"+area+"' and max_latitude='"+endTileHoriz+"' AND max_longitude='"+endTileVert+"' AND min_latitude='"+startTileHoriz+"' AND min_longitude='"+startTileVert+"'");
					 double media=0.0;
					 double variance=0.0;
					 if(mean.next()){
							media=mean.getDouble("media");
							variance=mean.getDouble("variance");
					 }
					 selectMean.close();
					 double standardDeviation=Math.sqrt(variance);
					 
					 System.out.println();
					 System.out.println("Distanza: "+distance);
					 System.out.println("Media: "+media);
					 System.out.println("Standard Deviation: "+standardDeviation);

					 if((distance>=media+standardDeviation && localPeak) || (distance<=media-standardDeviation)){
						 Statement selectIsPresent = con.createStatement();
		                 ResultSet isPresent = selectIsPresent.executeQuery("select * from peaks_detected where "
		                 		+ "date = '"+eventDay+"' AND max_latitude='"+endTileHoriz+"' "
		                 		+ "AND max_longitude='"+endTileVert+"' AND min_latitude='"+startTileHoriz+"' "
		                 		+ "AND min_longitude='"+startTileVert+"' and from_date='"+fromDate+"' and to_date='"+toDate+"'"
		                 		+ "AND dt='"+dt+"' AND delta='"+delta+"'");
		                
		                 if(!isPresent.next()){
		                	 System.out.println("Nuovo picco");
		                	 String insertPeak = "INSERT IGNORE INTO peaks_detected (id, area, date, max_latitude, max_longitude, min_latitude, min_longitude, distance,up,from_date,to_date,peak,dt,delta) "
		                	 		+ "VALUES ('','"+city+"','"+eventDay+"','"+endTileHoriz+"','"+endTileVert+"','"
		                			+startTileHoriz+"','"+startTileVert+"','"+distance+"',1,'"+fromDate+"','"+toDate+"',1,"+dt+","+delta+");";
		    	             Statement insertPeakStatement = con.createStatement();
		    	             insertPeakStatement.executeUpdate(insertPeak);
		    	             insertPeakStatement.close();
		                 }else{
		                	 System.out.println("Picco già rilevato");
		                	 }
		                 selectIsPresent.close();
		                 //response.getWriter().write("true");
		                 result.put(0, "true");
		                 
					 	}else{
						 System.out.println("Nessun picco rilevato");
						 result.put(0, "Nessun picco rilevato");
						 }
					 
					 
					 String json0= new Gson().toJson(result);
					 
					 response.getWriter().write(json0);
					 }
				 selectIsTweets.close();
			}
			selectCities.close();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	

	}

	public static boolean peak_detection(HashMap<Integer,Double> checkMax, double delta, double dt)
	{
		int maximumPos=0;
		double max=0.0;
		double leftV=0.0;
		double rightV=0.0;

		for (Map.Entry<Integer, Double> entry : checkMax.entrySet()) {
			
			if (entry.getValue() > max  ) {
				max = entry.getValue();
				maximumPos = entry.getKey();
			}
		}
			
		if(checkMax.containsKey(maximumPos+dt)){
			rightV=checkMax.get(maximumPos+dt);
		}
		if(checkMax.containsKey(maximumPos-dt)){
			leftV=checkMax.get(maximumPos-dt);
		}
		//System.out.println(max+" "+leftV+" "+rightV);
		
		if(max-leftV>=delta || max-rightV>=delta){
			
			return true;
		}else{
			return false;
		}
	}
	

}
