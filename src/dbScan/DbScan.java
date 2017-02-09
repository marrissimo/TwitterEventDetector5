package dbScan;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import com.google.gson.Gson;

import Connessione.Connessione;


public class DbScan extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		

		Connessione connessione = new Connessione();
	    Connection con = connessione.connetti();
	    HttpSession session=request.getSession();
	    
	    String eventDay=(String) session.getAttribute("day");
		String timeMin=request.getParameter("timeMin");
		String timeMax=request.getParameter("timeMax");
		
		String nTweetsS=request.getParameter("nTweets");
		int nTweets=Integer.parseInt(nTweetsS);
		
		String startTileHorizS=(String) session.getAttribute("sth");
		String endTileHorizS=(String) session.getAttribute("eth");
		String startTileVertS=(String) session.getAttribute("stv");
		String endTileVertS=(String) session.getAttribute("etv");

		double startTileHoriz = Double.parseDouble(startTileHorizS);
	    double startTileVert = Double.parseDouble(startTileVertS);
	    double endTileHoriz=Double.parseDouble(endTileHorizS);
	    double endTileVert=Double.parseDouble(endTileVertS);
	    
	    Map<Integer, double[]> result = new HashMap();
	    response.setContentType("application/json");
		
	    DBSCANClusterer<DoublePoint> dbs=new DBSCANClusterer<DoublePoint>(0.005,3); //epsilon=5 metri
	    
	    List<DoublePoint> points = new ArrayList<DoublePoint>();
	   
	    
	    Statement selectTweets;
		try {
			selectTweets = con.createStatement();
			ResultSet tweets=selectTweets.executeQuery("SELECT * FROM `tweets` WHERE time>="+timeMin+" AND time<="+timeMax+" AND latitude>="+startTileHoriz+" and longitude>="+startTileVert+" and latitude<"+endTileHoriz+" and longitude<"+endTileVert+" and date='"+eventDay+"'");
		
			System.out.println("coordinate:");
		    while(tweets.next()){
		    	double[] coordinates=new  double[2];
				double latitude=tweets.getDouble("latitude");
				double longitude=tweets.getDouble("longitude");
				System.out.println(latitude+" "+longitude);
				coordinates[0]=latitude;
				coordinates[1]=longitude;
				points.add(new DoublePoint(coordinates));
				}
			
		    List<Cluster<DoublePoint>> cluster= dbs.cluster(points);
		    
		int count=0;
		System.out.println("cluster:");

	    for(Cluster<DoublePoint> c: cluster){
		        result.put(count, c.getPoints().get(0).getPoint());
		        System.out.println(c.getPoints().get(0).getPoint()[0]+" "+c.getPoints().get(0).getPoint()[1]);
		        count++;
		    }    
	    String json= new Gson().toJson(result);
	    response.getWriter().write(json);
		
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	
		
	}

	
}
