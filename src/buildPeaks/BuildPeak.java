package buildPeaks;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

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

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Map<Integer, String> result = new HashMap<Integer, String>();

	 	String eventDay ="04-12-2016"; //request.getParameter("day");
		String area ="L"; //  request.getParameter("area");
		

		// String fromDate=request.getParameter("fromDate");
		// String toDate=request.getParameter("toDate");
		// String fromVariance=request.getParameter("fromVariance");
		// String toVariance=request.getParameter("toVariance");
		// String dtS=request.getParameter("dt");
		// String deltaS=request.getParameter("delta");

	    String startTileHorizS=request.getParameter("sth");
		String endTileHorizS=request.getParameter("eth");
		String startTileVertS=request.getParameter("stv");
		String endTileVertS=request.getParameter("etv");
		
		if(startTileHorizS!=null&&endTileHorizS!=null&&startTileVertS!=null&&endTileVertS!=null)
		{
			double dt = 2.0;
			double delta = 2.0;
			/*	
			double sh = 52;
			double eh = 52.6 ;
			double sv = 0;
			double ev = 0.6;
	
		double sh = (double) request.getAttribute("sh");
			double eh = (double) request.getAttribute("eh");
			double sv = (double) request.getAttribute("sv");
			double ev = (double) request.getAttribute("ev");*/
			
			double sh = (double) Double.parseDouble(startTileHorizS);
			double sv = (double) Double.parseDouble(startTileVertS);
			double eh = (double) Double.parseDouble(endTileHorizS);
			double ev = (double) Double.parseDouble(endTileVertS);
	
			response.setContentType("application/json");
	
			// UPDATE VARIANZA Nella zona
			try {
				updateVariance(area, sh, sv, eh, ev);
			} catch (IOException | SQLException e1) {
				e1.printStackTrace();
			}
	
			Connessione connessione = new Connessione();
			Utility ut = new Utility();
	
			try {
				Connection con = connessione.connetti();
				Statement selectCities;
				try {
					selectCities = con.createStatement();
					ResultSet cities = selectCities.executeQuery("select * from my_cities where  id='" + area + "'");
	
					while (cities.next()) {
	
						String city = cities.getString("id");
	
						Statement selectDays = con.createStatement();
						ResultSet days = selectDays
								.executeQuery("select distinct(date) from tweets where area='" + city + "'");
	
						double totDays = 0.0;
						while (days.next()) {
							totDays = totDays + 1;
						}
	
						System.out.println("-----" + city + "-----");
	
						Statement selectPeaks = con.createStatement();
						ResultSet peaks = selectPeaks.executeQuery(
								"SELECT * FROM event_detected WHERE area='" + city + "' and date='" + eventDay + "'");
	
						Statement selectIsTweets = con.createStatement();
						ResultSet isTweets = selectIsTweets
								.executeQuery("SELECT * FROM tweets WHERE area='" + city + "' and date='" + eventDay + "'");
	
						if (peaks.next() || !isTweets.next()) {
							System.out.println("Tabella già aggiornata per il giorno " + eventDay + " di " + city);
	
						} else {
							selectPeaks.close();
	
							Builder b1 = TimeSeriesBase.builder();
							Builder b2 = TimeSeriesBase.builder();
	
							double[] peaksDetection = new double[28];
							peaksDetection[0] = 0.0;
							peaksDetection[1] = 0.0;
							peaksDetection[26] = 0.0;
							peaksDetection[27] = 0.0;
	
							// PER OGNI ORA
							for (int k = 0; k < 24; k++) {
								String time1 = ut.convertSecondsToHhMmSs(k * 3600);
								String time2 = ut.convertSecondsToHhMmSs(3599 + k * 3600);
	
								// NUMERO UTENTI CHE TWITTANO NELLA ZONA A QUEST'ORA
								// IN MEDIA
								Statement selectTweets = con.createStatement();
								ResultSet users = selectTweets
										.executeQuery("select count(distinct(user_id)) num_users from tweets where area='"
												+ city + "' AND time>='" + time1 + "' AND time<='" + time2
												+ "'  AND latitude>='" + sh + "' AND latitude<'" + eh + "' AND longitude>='"
												+ sv + "' AND longitude<'" + ev + "'");
	
								if (users.next()) {
									double numUsers = users.getInt("num_users");
									double mediaUsers = numUsers / totDays;
	
									b1.add(k, mediaUsers);
								} else {
									System.out.println("Tabella tweets vuota per " + city);
									System.exit(0);
								}
	
								// NUMERO UTENTI CHE TWITTANO NELLA ZONA A QUEST'ORA
								// IL GIORNO SELEZIONATO
								Statement selectYesterdayUsers = con.createStatement();
								ResultSet yesterdayUsers = selectYesterdayUsers.executeQuery(
										"select count(distinct(user_id)) nYesterdayUsers from tweets where area='" + city
												+ "' AND time>='" + time1 + "' AND time<='" + time2 + "' AND date='"
												+ eventDay + "' AND latitude>='" + sh + "' AND latitude<'" + eh
												+ "' AND longitude>='" + sv + "' AND longitude<'" + ev + "'");
	
								double nYesterdayUsers = 0.0;
								if (yesterdayUsers.next()) {
									nYesterdayUsers = yesterdayUsers.getDouble("nYesterdayUsers");
									b2.add(k, nYesterdayUsers);
								}
	
								selectTweets.close();
								selectYesterdayUsers.close();
								peaksDetection[k + 2] = nYesterdayUsers;
	
							}
							com.fastdtw.timeseries.TimeSeries t1 = b1.build();
							com.fastdtw.timeseries.TimeSeries t2 = b2.build();
							double distance = FastDTW.compare(t1, t2, 10, Distances.EUCLIDEAN_DISTANCE).getDistance();
	
							// RESTITUISCE UNA LISTA DI INTERI CHE INDICANO LE ORE
							// DEI PICCHI, SE VUOTA NO PICCHI
							List<Integer> peaksList = peak_detection(peaksDetection, 2.0);
							System.out.println(distance);
	
							if (distance >= 1 && !peaksList.isEmpty()) {
								result.put(0, "Almeno un picco rilevato");
	
								System.out.println("Distanza " + distance + " Coordinate tra " + sh + "," + sv + " e " + eh
										+ "," + ev);
	
								// PER OGNI PICCO, DEFNISCE ORA INIZIO E ORA FINE
								// PICCO, e carica i tweet nell'intervallo di tempo
								for (Integer peak : peaksList) {
									String timeMin = ut.convertSecondsToHhMmSs(peak * 3600);
									String timeMax = ut.convertSecondsToHhMmSs(3599 + peak * 3600);
									System.out.println(timeMin + " " + timeMax);
	
									if (peak >= 24) {
										timeMax = ut.convertSecondsToHhMmSs(3599 + 23 * 3600);
									}
									if (peak <= 0) {
										timeMin = ut.convertSecondsToHhMmSs(0);
									}
	
									// Carico Tweet nell'intervallo di tempo
									Statement peakTweetsStatement = con.createStatement();
									ResultSet peakTweets = peakTweetsStatement
											.executeQuery("SELECT * FROM `tweets` WHERE latitude>=" + sh
													+ " and longitude>=" + sv + " and latitude<" + eh + " and longitude<"
													+ ev + " and date='" + eventDay + "' AND time<='" + timeMax
													+ "' AND time>='" + timeMin + "'");
	
									// ???
									DBSCANClusterer<DoublePoint> dbs = new DBSCANClusterer<DoublePoint>(0.005, 3);
									List<DoublePoint> points = new ArrayList<DoublePoint>();
	
									while (peakTweets.next()) {
										double lat = peakTweets.getDouble("latitude");
										double lon = peakTweets.getDouble("longitude");
										double[] coordinates = new double[2];
										coordinates[0] = lat;
										coordinates[1] = lon;
										// aggiunge al clusterer tutti i punti
										// lat,lon di ogni tweet in quell'ora
										points.add(new DoublePoint(coordinates));
									}
	
									List<Cluster<DoublePoint>> cluster = dbs.cluster(points);
									double lat = 0.0;
									double lon = 0.0;
									for (Cluster<DoublePoint> c : cluster) {
										lat = c.getPoints().get(0).getPoint()[0];
										lon = c.getPoints().get(0).getPoint()[1];
									}
	
									// TUTTI GLI EVENTI GIA' RIVELATI in quel punto
									// LAT,LON e SALAVATI NEL DB
									Statement selectIsPresent = con.createStatement();
									String isPresentQuery = "select * from event_detected where date = '" + eventDay
											+ "' AND latitude='" + lat + "' AND longitude='" + lon + "'";
									ResultSet isPresent = selectIsPresent.executeQuery(isPresentQuery);
	
									// NUOVO PICCO: LO INSERISCO in una nuova
									// tabella di resized
									if (!isPresent.next() && lat != 0.0 && lon != 0.0) {
										System.out.println("Nuovo picco");
										// String insertEvent="INSERT IGNORE INTO
										// event_detected (id, area, date,
										// max_latitude, max_longitude,
										// min_latitude, min_longitude, latitude,
										// longitude,
										// distance,from_date,to_date,dt,delta,time_peak,up)
										// VALUES
										// ('','"+city+"','"+eventDay+"','"+endTileHoriz+"','"+endTileVert+"','"+startTileHoriz+"','"+startTileVert+"','"+lat+"','"+lon+"','"+distance+"','2015-12-21','"+eventDay+"',2,2,'"+peak+"',0);";
										String insertEvent = "INSERT IGNORE INTO event_resized (id, area, date, max_latitude, max_longitude, min_latitude, min_longitude, latitude, longitude, distance,from_date,to_date,dt,delta,time_peak,up) VALUES ('','"
												+ city + "','" + eventDay + "','" + eh + "','" + ev + "','" + sh + "','"
												+ sv + "','" + lat + "','" + lon + "','" + distance + "','2015-12-21','"
												+ eventDay + "',2,2,'" + peak + "',0);";
	
										System.out.println(insertEvent);
										Statement insertPeakStatement = con.createStatement();
										insertPeakStatement.executeUpdate(insertEvent);
										insertPeakStatement.close();
									}
									selectIsPresent.close();
								}
							} else {// SE PRESENTE NEL DB, AGGIORNO PICCO -> up=1
								System.out.println("--");
								result.put(0, "Nessun picco rilevato");
	
							}
	
							selectDays.close();
							// Statement updateStatement=con.createStatement();
							// String updateTweets="UPDATE `event_detected` SET `up`
							// = '1' WHERE `event_detected`.`date` ='"+eventDay+"'
							// and `event_detected`.`area` ='"+city+"';";
							// System.out.println(updateTweets);
							// updateStatement.executeUpdate(updateTweets);
							// updateStatement.close();
	
						}
						String json0 = new Gson().toJson(result);
						response.getWriter().write(json0);
					}
	
				} catch (SQLException e) {
					e.printStackTrace();
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			result.put(0, "Avvio o cordinate nulle");
			String json0 = new Gson().toJson(result);
			response.getWriter().write(json0);
		}
		
	}

	private static void updateVariance(String area, double sh, double sv, double eh, double ev)
			throws IOException, SQLException {

		Connessione connessione = new Connessione();
		Connection con = connessione.connetti();
		Statement selectCities = con.createStatement();
		ResultSet cities = selectCities.executeQuery("select * from my_cities where id='" + area + "'");

		while (cities.next()) {

			System.out.println("inizio aggiornamento " + area);

			Statement selectPeak;
			Statement selectNDays;
			Statement selectIsVariance;
			Calendar cal = Calendar.getInstance();
			Utility ut = new Utility();
			String today = ut.correctFormattedData(cal);

			try {
				selectIsVariance = con.createStatement();
				ResultSet isVariance = selectIsVariance.executeQuery("SELECT * FROM  `variance` WHERE date='" + today
						+ "' and max_latitude='" + eh + "' AND max_longitude='" + ev + "' AND min_latitude='" + sh
						+ "' AND min_longitude='" + sv + "'");

				if (!isVariance.next()) {
					selectPeak = con.createStatement();
					ResultSet peak = selectPeak.executeQuery(
							"SELECT * FROM  `event_detected` WHERE max_latitude='" + eh + "' AND max_longitude='" + ev
									+ "' AND min_latitude='" + sh + "' AND min_longitude='" + sv + "'");
					selectNDays = con.createStatement();
					ResultSet NDays = selectNDays
							.executeQuery("SELECT count(distinct(date)) nDays FROM  tweets WHERE area='" + area + "'");

					double nDays = 0;
					if (NDays.next()) {
						nDays = NDays.getDouble("nDays");
					}

					double sumDistance = 0;
					double distance = 0;
					while (peak.next()) {
						distance = peak.getDouble("distance");
						sumDistance += distance;
					}
					double media = sumDistance / nDays;

					peak.beforeFirst();
					double sommaScartiQuad = 0;
					while (peak.next()) {
						distance = peak.getDouble("distance");
						sommaScartiQuad += (distance - media) * (distance - media);
					}

					// ma non dovrebbe essere la radice quadrata dello scarto
					// quadratico?
					double variance = sommaScartiQuad / nDays;

					// System.out.println("variance: "+variance+" media:
					// "+media);

					String insertVariance = "INSERT IGNORE INTO variance (id, area, date, max_latitude, max_longitude, min_latitude, min_longitude, media,variance) VALUES ('','"
							+ area + "','" + today + "','" + eh + "','" + ev + "','" + sh + "','" + sv + "','" + media
							+ "','" + variance + "');";

					Statement insertVarianceStatement = con.createStatement();
					insertVarianceStatement.executeUpdate(insertVariance);
					insertVarianceStatement.close();
					selectPeak.close();
				}
				isVariance.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			System.out.println("Fine aggiornamento " + area);

		}
	}

	public static List<Integer> peak_detection(double[] f, double delta) {

		List<Integer> ext = new ArrayList<Integer>();

		for (int i = 2; i < f.length - 2; i++) {
			// System.out.println();
			// System.out.print(i-2+" "+f[i]+" | ");
			if ((f[i] - f[i - 2]) >= 2 && (f[i] - f[i + 2]) >= 2) {
				ext.add(i - 2);
				i = i + 2;
			}
			// System.out.print(i+" ");

		}
		for (Integer entry : ext) {
			System.out.println("Picco in " + entry);
		}

		return ext;
	}

}
