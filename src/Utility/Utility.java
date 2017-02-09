package Utility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Utility {

    private static Map<String, String> map = new TreeMap();
    private static boolean exist = false;

    public static String getNameFromProvince(String province) {
        if (!exist) {
            map.put("AG", "Agrigento");
            map.put("AL", "Alessandria");
            map.put("AN", "Ancona");
            map.put("AO", "Aosta");
            map.put("AR", "Arezzo");
            map.put("AP", "Ascoli Piceno");
            map.put("AT", "Asti");
            map.put("AV", "Avellino");
            map.put("BA", "Bari");
            map.put("BT", "Barletta-Andria-Trani");
            map.put("BL", "Belluno");
            map.put("BN", "Benevento");
            map.put("BG", "Bergamo");
            map.put("BI", "Biella");
            map.put("BO", "Bologna");
            map.put("BZ", "Bolzano");
            map.put("BS", "Brescia");
            map.put("BR", "Brindisi");
            map.put("CA", "Cagliari");
            map.put("CL", "Caltanissetta");
            map.put("CB", "Campobasso");
            map.put("CI", "Carbonia-Iglesias");
            map.put("CE", "Caserta");
            map.put("CT", "Catania");
            map.put("CZ", "Catanzaro");
            map.put("CH", "Chieti");
            map.put("CO", "Como");
            map.put("CS", "Cosenza");
            map.put("CR", "Cremona");
            map.put("KR", "Crotone");
            map.put("CN", "Cuneo");
            map.put("EN", "Enna");
            map.put("FM", "Fermo");
            map.put("FE", "Ferrara");
            map.put("FI", "Firenze");
            map.put("FG", "Foggia");
            map.put("FC", "Forlì-Cesena");
            map.put("FR", "Frosinone");
            map.put("GE", "Genova");
            map.put("GO", "Gorizia");
            map.put("GR", "Grosseto");
            map.put("IM", "Imperia");
            map.put("IS", "Isernia");
            map.put("AQ", "L'Aquila");
            map.put("SP", "La Spezia");
            map.put("LT", "Latina");
            map.put("LE", "Lecce");
            map.put("LC", "Lecco");
            map.put("LI", "Livorno");
            map.put("LO", "Lodi");
            map.put("LU", "Lucca");
            map.put("MC", "Macerata");
            map.put("MN", "Mantova");
            map.put("MS", "Massa e Carrara");
            map.put("MT", "Matera");
            map.put("VS", "Medio Campidano");
            map.put("ME", "Messina");
            map.put("MI", "Milano");
            map.put("MO", "Modena");
            map.put("MB", "Monza e Brianza");
            map.put("NA", "Napoli");
            map.put("NO", "Novara");
            map.put("NU", "Nuoro");
            map.put("OG", "Ogliastra");
            map.put("OT", "Olbia-Tempio");
            map.put("OR", "Oristano");
            map.put("PD", "Padova");
            map.put("PA", "Palermo");
            map.put("PR", "Parma");
            map.put("PV", "Pavia");
            map.put("PG", "Perugia");
            map.put("PU", "Pesaro e Urbino");
            map.put("PE", "Pescara");
            map.put("PC", "Piacenza");
            map.put("PI", "Pisa");
            map.put("PT", "Pistoia");
            map.put("PN", "Pordenone");
            map.put("PZ", "Potenza");
            map.put("PO", "Prato");
            map.put("RG", "Ragusa");
            map.put("RA", "Ravenna");
            map.put("RC", "Reggio Calabria");
            map.put("RE", "Reggio Emilia");
            map.put("RI", "Rieti");
            map.put("RN", "Rimini");
            map.put("RM", "Roma");
            map.put("RO", "Rovigo");
            map.put("SA", "Salerno");
            map.put("SS", "Sassari");
            map.put("SV", "Savona");
            map.put("SI", "Siena");
            map.put("SR", "Siracusa");
            map.put("SO", "Sondrio");
            map.put("TA", "Taranto");
            map.put("TE", "Teramo");
            map.put("TR", "Terni");
            map.put("TO", "Torino");
            map.put("TP", "Trapani");
            map.put("TN", "Trento");
            map.put("TV", "Treviso");
            map.put("TS", "Trieste");
            map.put("UD", "Udine");
            map.put("VA", "Varese");
            map.put("VE", "Venezia");
            map.put("VB", "Verbano-Cusio-Ossola");
            map.put("VC", "Vercelli");
            map.put("VR", "Verona");
            map.put("VV", "Vibo Valentia");
            map.put("VI", "Vicenza");
            map.put("VT", "Viterbo");
            map.put("L", "London");
            map.put("NYC", "New York City");
        }
        return map.get(province);
    }

    public static List<String> getItalianProvince() {
        
        List<String> result = new ArrayList();
            result.add( "Agrigento");
            result.add("Alessandria");
            result.add( "Ancona");
            result.add("Aosta");
            result.add("Arezzo");
            result.add( "Ascoli Piceno");
            result.add( "Asti");
            result.add("Avellino");
            result.add( "Bari");
            result.add( "Barletta-Andria-Trani");
            result.add( "Belluno");
            result.add( "Benevento");
            result.add( "Bergamo");
            result.add("Biella");
            result.add( "Bologna");
            result.add( "Bolzano");
            result.add( "Brescia");
            result.add( "Brindisi");
            result.add( "Cagliari");
            result.add( "Caltanissetta");
            result.add( "Campobasso");
            result.add( "Carbonia-Iglesias");
            result.add( "Caserta");
            result.add( "Catania");
            result.add( "Catanzaro");
            result.add( "Chieti");
            result.add( "Como");
            result.add( "Cosenza");
            result.add( "Cremona");
            result.add( "Crotone");
            result.add( "Cuneo");
            result.add( "Enna");
            result.add( "Fermo");
            result.add( "Ferrara");
            result.add( "Firenze");
            result.add( "Foggia");
            result.add( "Forlì-Cesena");
            result.add( "Frosinone");
            result.add( "Genova");
            result.add( "Gorizia");
            result.add( "Grosseto");
            result.add( "Imperia");
            result.add("Isernia");
            result.add( "L'Aquila");
            result.add( "La Spezia");
            result.add( "Latina");
            result.add( "Lecce");
            result.add( "Lecco");
            result.add( "Livorno");
            result.add( "Lodi");
            result.add( "Lucca");
            result.add( "Macerata");
            result.add( "Mantova");
            result.add( "Massa e Carrara");
            result.add( "Matera");
            result.add( "Medio Campidano");
            result.add( "Messina");
            result.add( "Milano");
            result.add( "Modena");
            result.add( "Monza e Brianza");
            result.add( "Napoli");
            result.add( "Novara");
            result.add( "Nuoro");
            result.add( "Ogliastra");
            result.add( "Olbia-Tempio");
            result.add( "Oristano");
            result.add( "Padova");
            result.add( "Palermo");
            result.add( "Parma");
            result.add( "Pavia");
            result.add( "Perugia");
            result.add( "Pesaro e Urbino");
            result.add( "Pescara");
            result.add( "Piacenza");
            result.add( "Pisa");
            result.add( "Pistoia");
            result.add( "Pordenone");
            result.add( "Potenza");
            result.add( "Prato");
            result.add( "Ragusa");
            result.add( "Ravenna");
            result.add( "Reggio Calabria");
            result.add( "Reggio Emilia");
            result.add( "Rieti");
            result.add( "Rimini");
            result.add( "Roma");
            result.add( "Rovigo");
            result.add( "Salerno");
            result.add( "Sassari");
            result.add( "Savona");
            result.add( "Siena");
            result.add( "Siracusa");
            result.add( "Sondrio");
            result.add( "Taranto");
            result.add( "Teramo");
            result.add( "Terni");
            result.add( "Torino");
            result.add( "Trapani");
            result.add( "Trento");
            result.add( "Treviso");
            result.add( "Trieste");
            result.add( "Udine");
            result.add( "Varese");
            result.add( "Venezia");
            result.add("Verbano-Cusio-Ossola");
            result.add( "Vercelli");
            result.add( "Verona");
            result.add("Vibo Valentia");
            result.add( "Vicenza");
            result.add( "Viterbo");
        
        return result;
    }
    public static Map<String, Integer> sortMap(Map<String, Integer> unsortedMap) {
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
                unsortedMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });
        Map<String, Integer> sortedByValues = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedByValues.put(entry.getKey(), entry.getValue());
        }
        return sortedByValues;
    }

    public static Map<String, Double> sortMapDouble(Map<String, Double> unsortedMap) {
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(
                unsortedMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });
        Map<String, Double> sortedByValues = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedByValues.put(entry.getKey(), entry.getValue());
        }
        return sortedByValues;
    }

    public static List getMyAreasList(String username, Connection con) throws IOException, SQLException {
        List<String> myAreasList = new ArrayList();
        Statement selectMyArea = con.createStatement();
        ResultSet myAreas = selectMyArea.executeQuery("select distinct area from places,users_checkins where id=place_id and user_id='" + username + "' and area IS NOT NULL");
        while (myAreas.next()) {
            String myArea = myAreas.getString("area");
            myAreasList.add(myArea);
        }
        selectMyArea.close();
        return myAreasList;
    }
    
    public  String correctFormattedData( Calendar cal ){
        String month = new String();
        String day = new String();
        if(cal.get(Calendar.MONTH)+1<10)
            month= "0"+String.valueOf(cal.get(Calendar.MONTH)+1);
        else
            month=String.valueOf(cal.get(Calendar.MONTH)+1);
        if(cal.get(Calendar.DAY_OF_MONTH)<10)
            day= "0"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        else
            day= String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        
        return cal.get(cal.YEAR)+"-"+month+"-"+day;
    }
    
    public double getDistance(double lat,double lon,double maxlat,double maxlon){

    	double radLat=lat*(Math.PI/180);
    	double radLon=lon*(Math.PI/180);
    	double radMaxLat=maxlat*(Math.PI/180);
    	double radMaxLon=maxlon*(Math.PI/180);

    	
    	return Math.acos( Math.sin(radLat) * Math.sin(radMaxLat) + Math.cos(radLat) * Math.cos(radMaxLat) * Math.cos(radMaxLon-radLon) ) * 6371;
    	
    }
    public String convertSecondsToHhMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h,m,s);
    }
}
