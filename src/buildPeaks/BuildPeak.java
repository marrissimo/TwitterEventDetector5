package buildPeaks;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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


public class BuildPeak extends HttpServlet {
	private static final long serialVersionUID = 1L;


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		HttpSession session=request.getSession();
		Connessione connessione = new Connessione();
		Utility ut=new Utility();
		
		String eventDay=request.getParameter("day");
		String area=request.getParameter("area");
		String fromDate=request.getParameter("fromDate");
		String toDate=request.getParameter("toDate");
		String fromVariance=request.getParameter("fromVariance");
		String toVariance=request.getParameter("toVariance");
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
        
        Connection con = connessione.connetti();
        double distanceEventDay=0.0;
        HashMap<Integer,Double> checkMax = new HashMap<Integer,Double>();
        boolean localPeak=false;
        
        Map<Integer, String> result = new HashMap();
        response.setContentType("application/json");
        
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
					 if(!isTweets.next() || eventDay=="" || eventDay==null){
							System.out.println("Nessun picco aggiornato");
							response.getWriter().write("Non ci sono tweets per questo giorno");
					 }else{
						 Builder b1 = TimeSeriesBase.builder();
						 Builder eDay = TimeSeriesBase.builder();
						 
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
							 
							 Statement selectEventDayUsers = con.createStatement();
							 ResultSet eventDayUsers = selectEventDayUsers.executeQuery("select count(distinct(user_id)) nEventDayUsers from tweets where area='"+city+"' AND time>='"+time1+"' AND time<='"+time2+"' AND date='"+eventDay+"' AND latitude>='"+startTileHoriz+"' AND latitude<'"+endTileHoriz+"' AND longitude>='"+startTileVert+"' AND longitude<'"+endTileVert+"'");
							 
							 double nEventDayUsers=0.0;
							 if(eventDayUsers.next()){
								 nEventDayUsers=eventDayUsers.getDouble("nEventDayUsers");
								 eDay.add(k,nEventDayUsers);
								 }
							 selectEventDayUsers.close();
							 checkMax.put(k, nEventDayUsers);
						 }
						 
						 com.fastdtw.timeseries.TimeSeries t1=b1.build();
						 com.fastdtw.timeseries.TimeSeries tED=eDay.build();
						 distanceEventDay = FastDTW.compare(t1, tED, 10, Distances.EUCLIDEAN_DISTANCE)
								 .getDistance();
						 localPeak=peak_detection(checkMax,delta,dt);
						 System.out.println("Distanza del giorno "+eventDay+": "+distanceEventDay);
						 
						 Statement selectEachDays = con.createStatement();
						 ResultSet eachDays = selectEachDays.executeQuery("select distinct(date) from tweets where area='"+city+"' and date>='"+fromVariance+"' and date<='"+toVariance+"'");
						 double sumDistance=0.0;
						 ArrayList<Double> distances= new ArrayList<Double> ();
						 while(eachDays.next()){
							 String pastDay=eachDays.getString("date");
							 Builder b2 = TimeSeriesBase.builder();
							 
							 for(int k=0; k<24;k++){
								 String time1=ut.convertSecondsToHhMmSs(k*3600);
								 String time2=ut.convertSecondsToHhMmSs(3599+k*3600);
								 Statement selectEventDayUsers = con.createStatement();
								 ResultSet eventDayUsers = selectEventDayUsers.executeQuery("select count(distinct(user_id)) nEventDayUsers from tweets where area='"+city+"' AND time>='"+time1+"' AND time<='"+time2+"' AND date='"+pastDay+"' AND latitude>='"+startTileHoriz+"' AND latitude<'"+endTileHoriz+"' AND longitude>='"+startTileVert+"' AND longitude<'"+endTileVert+"'");
								 
								 double nEventDayUsers=0.0;
								 if(eventDayUsers.next()){
									 nEventDayUsers=eventDayUsers.getDouble("nEventDayUsers");
									 b2.add(k,nEventDayUsers);
									 }
								 selectEventDayUsers.close();
								
							 }
							 
							 com.fastdtw.timeseries.TimeSeries t2=b2.build();

							 double distance = FastDTW.compare(t1, t2, 10, Distances.EUCLIDEAN_DISTANCE)
									 .getDistance();
							 
							 System.out.println(pastDay+"  "+distance);
							 
							 distances.add(distance);
							 sumDistance+=distance;
						 }
						 selectEachDays.close();
						 double media=sumDistance/distances.size();
						 
						 double sommaScartiQuad=0.0;
						 for(double distance :distances){
							 sommaScartiQuad+=(distance-media)*(distance-media);
						 }
						 
						 double variance=sommaScartiQuad/distances.size();
						 double standardDeviation=Math.sqrt(variance);
						 
						 System.out.println();
						 System.out.println("Distanza: "+distanceEventDay);
						 System.out.println("Media: "+media);
						 System.out.println("Standard Deviation: "+standardDeviation);
						 
						 if(distanceEventDay>=media+standardDeviation && localPeak){
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
			                			+startTileHoriz+"','"+startTileVert+"','"+distanceEventDay+"',1,'"+fromDate+"','"+toDate+"',1,"+dt+","+delta+");";
			    	             Statement insertPeakStatement = con.createStatement();
			    	             insertPeakStatement.executeUpdate(insertPeak);
			    	             insertPeakStatement.close();
			                 }else{
			                	 System.out.println("Picco già rilevato");
			                	 }
			                 selectIsPresent.close();
			                 result.put(0, "true");
			                 
						 	}else{
							 System.out.println("Nessun picco rilevato");
							 result.put(0, "Nessun picco rilevato");
							 
						 	}
						 String json0= new Gson().toJson(result);
						 
						 response.getWriter().write(json0);
						 
					 }
				
					
				}
			
		} catch (SQLException e) {
			
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
