<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@page import="java.sql.Connection"%>
<%@page import="Connessione.*"%>
<%@page import="Utility.*"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.util.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Build Map</title>

<link rel="stylesheet"
	href="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.css" />
<link rel="stylesheet" href="CSS/colorbox.css" />
<link rel="stylesheet" href="CSS/topBar.css" type="text/css" />
<link rel="stylesheet" href="CSS/font-awesome.min.css">
<link rel="stylesheet" href="CSS/leaflet.awesome-markers.css">
<link href='http://fonts.googleapis.com/css?family=PT+Sans'
     	rel='stylesheet' type='text/css'>
<link rel="stylesheet" href="CSS/clusters.css" />
<link rel="stylesheet" href="CSS/jquery-impromptu.css" />
<link rel="stylesheet" href="CSS/barChart.css" />
<link rel="stylesheet" href="CSS/leaflet-label.css" />
<link rel="stylesheet" href="CSS/jquery-ui1.css" />
<link rel="stylesheet" href="CSS/accordionMenu.css" />
<link rel="stylesheet" href="CSS/eventPopUp.css" />
<link rel="stylesheet" href="CSS/configuration.css" />

<script src="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.js"></script>
<script	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js"></script>

<link rel="stylesheet" type="text/css" href="CSS/pikaday.css" />
<link rel="stylesheet" type="text/css" href="CSS/theme.css" />
<link rel="stylesheet" type="text/css" href="CSS/triangle.css" />
<link rel="stylesheet" type="text/css" href="CSS/eventBuilder.css" />
<link rel="stylesheet" type="text/css" href="CSS/draw.css" />

<link rel="stylesheet"
	href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<link rel="stylesheet" href="/resources/demos/style.css">


<script type="text/javascript" src="JS/moment.js"></script>
<script type="text/javascript" src="JS/pikaday.js"></script>
<script type="text/javascript" src="JS/topbar.js"></script>
<script type="text/javascript" src="JS/configuration.js"></script>
<script type="text/javascript" src="JS/builder.js"></script>

<script type="text/javascript" src="JS/Chart.Bar.js"></script>
<script type="text/javascript" src="JS/Chart.Core.js"></script>
<script type="text/javascript" src="JS/Chart.Doughnut.js"></script>
<script type="text/javascript" src="JS/Chart.Line.js"></script>
<script type="text/javascript" src="JS/Chart.PolarArea.js"></script>
<script type="text/javascript" src="JS/Chart.Radar.js"></script>

<script src="src/Leaflet.draw.js"></script>
<script src="src/Leaflet.Editable.js"></script>
<script src="src/Path.Drag.js"></script>

<link rel="stylesheet" href="dist/leaflet.draw.css" />
<script src="src/Toolbar.js"></script>
<script src="src/Tooltip.js"></script>
<script src="src/ext/GeometryUtil.js"></script>
<script src="src/ext/LatLngUtil.js"></script>
<script src="src/ext/LineUtil.Intersect.js"></script>
<script src="src/ext/Polygon.Intersect.js"></script>
<script src="src/ext/Polyline.Intersect.js"></script>
<script src="src/draw/DrawToolbar.js"></script>
<script src="src/draw/handler/Draw.Feature.js"></script>
<script src="src/draw/handler/Draw.SimpleShape.js"></script>
<script src="src/draw/handler/Draw.Polyline.js"></script>
<script src="src/draw/handler/Draw.Circle.js"></script>
<script src="src/draw/handler/Draw.Marker.js"></script>
<script src="src/draw/handler/Draw.Polygon.js"></script>
<script src="src/draw/handler/Draw.Rectangle.js"></script>
<script src="src/edit/EditToolbar.js"></script>
<script src="src/edit/handler/EditToolbar.Edit.js"></script>
<script src="src/edit/handler/EditToolbar.Delete.js"></script>
<script src="src/Control.Draw.js"></script>
<script src="src/edit/handler/Edit.Poly.js"></script>
<script src="src/edit/handler/Edit.SimpleShape.js"></script>
<script src="src/edit/handler/Edit.Circle.js"></script>
<script src="src/edit/handler/Edit.Rectangle.js"></script>
<script src="src/edit/handler/Edit.Marker.js"></script>

<style>
body {	padding: 0;	margin: 0;}
html, body, #map {height: 100%;}
</style>

<script src="https://code.jquery.com/jquery-1.12.4.js"></script>
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>

</head>
<body>
	<%	//DIMENSIONE CASELLA  GRIGLIA
	double TILE_EDGE = 0.006;

	Connessione connessione = new Connessione();
	Connection con = connessione.connetti();
	
	Utility ut=new Utility();
	
	String area = request.getParameter("area"); 
	String eventDay = request.getParameter("day");
	String from = request.getParameter("fromDate");
	String to = request.getParameter("toDate");
	
	session.setAttribute("sth", "");
	session.setAttribute("eth", "");
	session.setAttribute("stv", "");
	session.setAttribute("etv", "");

	if(area==null){area="L";}
	if(eventDay==null || eventDay=="")
	{
		Statement selectDay = con.createStatement();
    	ResultSet day = selectDay.executeQuery("SELECT MAX( DATE ) maxDay FROM  `event_detected` where area='"+area+"'");
    	if(day.next()){
    		
    		eventDay=day.getString("maxDay");
    	}
    	selectDay.close();
	}
	
	session.setAttribute("eventDay", eventDay);
	
	// DATA min e Max selezionabile
	if(from=="" || from==null){from="2015-12-21";}
	if(to=="" || to==null){to=eventDay;}
	
	String cityName=Utility.getNameFromProvince(area);
	%>

	<div class="topbar">
		<div id="full_logo">
			<a id="logo" href="#"> Twitter Event Detector: </a> <select
			   id="cities" onChange='menu(this)'>
				<option id="city" value='<%=area %>'><%=cityName %></option>
				<% 
				// SELEZIONO L'AREA
				 Statement selectStatement = con.createStatement();
				 ResultSet areas = selectStatement.executeQuery("select distinct(area) from event_detected where (area='L' or area='NYC') AND area!='"+area+"'");
			
				 while(areas.next()){
               	  String areaPeaks = areas.getString("area");
                  String areaName = Utility.getNameFromProvince(areaPeaks);
                 %>
				<option value='<%=areaPeaks%>'><%=areaName%></option>
				<%    
              	}
			 	selectStatement.close();
			 	%>
			</select>
		</div>
		
		<button id="no_grid"> Remove grid</button>
		 
		<div class="divider"></div>
		
		<input type="checkbox" id="resize" value="resize"/> Resize Mode 
		
		<button id="getTweets" value=<%=area %>>Get Events</button>
		<form id='date'>
			<input type="text" id="datepicker1" value='<%=eventDay%>'>
			<!-- onchange='updateInput(this.value)' -->
		</form>
		
		
		<script>
		  
		  // FORMATTAZIONE DATA del Picker
				var picker1 = new Pikaday({   
	    			field: document.getElementById('datepicker1'),
	    	        format : "YYYY-MM-DD",
	    	        defaultDate: new Date('<%=eventDay%>'),
	    	        minDate: new Date(2015, 11, 21) // data DEFAULT
	    	        });
	    		
		</script>

		<script type="text/javascript" src="JS/calendar.js"></script>
	</div> 

	<script>
		// MENU A TENDINA
         function menu(links) { location.href = "?area="+links.value; }
    </script>

<!-- FINE TOOLBAR : INIZIO MAPPA -->

	<div id="map"></div>
	<%
	  // SELEZIONA CITTA'
        Statement selectArea = con.createStatement();
		ResultSet latlng = selectArea.executeQuery("SELECT lat,lng FROM  `my_cities` where id='"+area+"'");
		
	  //Predefinite  di LONDRA:
		double latCity =  40.766638;
		double lonCity = -73.979916;
		if( latlng.next() )
		{
			latCity=latlng.getDouble("lat");
			lonCity=latlng.getDouble("lng");
		}
		selectArea.close();
		%>

	<script type="text/javascript">
        // CARICA MAPPA DA OPENMAP 
         var map = L.map('map').setView([<%=latCity%>, <%=lonCity%>], 14);
       
         L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', 
         	{ attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'}).addTo(map);
         
    </script>
    
	<%
         // CARICAMENTO TUTTI EVENTI GIA IDENTIFICATI DAL DATABASE PER  TUTTA LA MAPPA :  events contiene tutti  gli  eventi del DB del giorno e CITTA selezionata
         
          String peaksQuery="SELECT * FROM event_detected WHERE date='"+eventDay+"' and area='"+area+"' and from_date='"+from+"' and to_date='"+to+"'";
		  //System.out.println(peaksQuery);     
	      Statement selectPeaks = con.createStatement();
		  ResultSet events=selectPeaks.executeQuery(peaksQuery);
		
		  // Selezione Data odierna
		  Calendar cal = Calendar.getInstance();
		  String today = ut.correctFormattedData(cal);
	    
	      while ( events.next() ) {
	    	    // Cicla su events e prende solo eventi della data selezionata
				String city=events.getString("area");
	    	  
				double latitude = events.getDouble("latitude");
				double longitude = events.getDouble("longitude");
				
				double maxLatitude = events.getDouble("max_latitude");
				double minLatitude = events.getDouble("min_latitude");
				
				double maxLongitude = events.getDouble("max_longitude");
				double minLongitude = events.getDouble("min_longitude");
				
				int eventConfirmed = events.getInt("event_confirmed");
				int timePeak = events.getInt("time_peak");
				
				String timeMin = ut.convertSecondsToHhMmSs((timePeak-2)*3600);
			    String timeMax = ut.convertSecondsToHhMmSs((timePeak+2)*3600);
			    
			    if(timePeak >= 24) {timeMax = ut.convertSecondsToHhMmSs(3599+23*3600);}
				if(timePeak <=  0) {timeMin = ut.convertSecondsToHhMmSs(0);}
				
			// BOOOOOOOOOOOO, probabilmente cerca di fare una media tra i twitter per aver un unico punto
				double distance = events.getDouble("distance");
	
				Statement selectMean = con.createStatement();
				ResultSet mean=selectMean.executeQuery("SELECT * FROM variance WHERE date='"+today+"' and area='"+area+"' and max_latitude='"+maxLatitude+"' AND max_longitude='"+maxLongitude+"' AND min_latitude='"+minLatitude+"' AND min_longitude='"+minLongitude+"'");
				double media=0.0;
				double variance=0.0;
				if( mean.next() )
				{
					media=mean.getDouble("media");
					variance=mean.getDouble("variance");
				}
				selectMean.close();
				
				// ECCEZIONE ?? se la media è 0? prendiamo a caso  da  un giorno????
				if(media==0.0){
					Statement selectNewMedia = con.createStatement();
					ResultSet nMedia=selectNewMedia.executeQuery("SELECT * FROM variance WHERE date='2016-04-12' and area='"+area+"' and max_latitude='"+maxLatitude+"' AND max_longitude='"+maxLongitude+"' AND min_latitude='"+minLatitude+"' AND min_longitude='"+minLongitude+"'");
					if(nMedia.next()){
						media=nMedia.getDouble("media");
						variance=nMedia.getDouble("variance");
					}
					selectNewMedia.close();
				}
			
				double standardDeviation=Math.sqrt(variance);
				if((distance>=media+standardDeviation) || (distance<=media-standardDeviation)){
					
		   %>
		   
	<!-- 		// MARKER EVENTI  -->
	
	<script>
			var blueIcon = new L.Icon({

			  	iconUrl: 'img/blue-marker.png',
			  	iconAnchor:[12, 41],
			  	popupAnchor: [10, -30]
			  	});
		
			 var greenIcon = new L.Icon({
		  	     
		  	     iconUrl: 'img/green-marker.png',
		  		 iconAnchor:[12, 41],
		  		 popupAnchor: [10, -30]
		  	 });
		  	 
		  	var redIcon = new L.Icon({
		  	     
		  	    iconUrl: 'img/red-marker.png',
		  		iconAnchor:[12, 41],
		  		popupAnchor: [10, -30]
		  	 });
		  	
		    var pmarker = {
	        		    "type": "Feature",
	        		    "properties": {
	               	 		"maxLatitude": <%=maxLatitude%>,
	               	 		"minLatitude": <%=minLatitude%>,
	               	 		"maxLongitude":<%=maxLongitude%>,
	               	 		"minLongitude":<%=minLongitude%>},
	        		    
	        		    "geometry": {
	        		        "type": "Point",
	        		        "coordinates": [<%=longitude %>,<%=latitude %>]
	        		    }
	        		};
			</script>
	<script type="text/javascript">
		    
			// Se evento è da ANALIZZARE -> BLUE
			// Se evento non è CONFERMATO -> ROSSO
			// Se evento è CONFERMATO -> VERDE
			
			<%if(eventConfirmed==0){%>
			L.geoJson(pmarker, {
    		    pointToLayer: function (feature, latlng) {
    		        return L.marker(latlng,{icon: blueIcon,clickable:false,keyboard:false});
    		    }
    		}).addTo(map);
			
			<%}else if(eventConfirmed==1){%>
			L.geoJson(pmarker, {
    		    pointToLayer: function (feature, latlng) {
    		        return L.marker(latlng,{icon: redIcon,clickable:false,keyboard:false});
    		    }
    		}).addTo(map);
			
			<%}else if(eventConfirmed==2){%>
			L.geoJson(pmarker, {
    		    pointToLayer: function (feature, latlng) {
    		        return L.marker(latlng,{icon: greenIcon,clickable:false,keyboard:false});
    		    }
    		}).addTo(map);
			
			<%}%>
	</script>
	
	<%}// si chiude il  while sugli eventi}%>
	
	<% } // HTML -  POPUP EVENTO -> dentro una stringa 'event'
		String event="";
		event=event+"<div id='up' >";
		
		event=event+"<div id='conf' >";
		event=event+"<h1 class='title'>Events detected</h1>";
		event=event+"<div id='events' class='scrollbar' >";
		event=event+"</div>";
		event=event+"<div id='ask_event'>";
		event=event+"</div>";
		event=event+"</div>";
		event=event+"<div id='chart' >";
		
		event=event+"<h1 class='title'>Time series of Twitter users</h1>";
		event=event+"<canvas id='canvasUsers'";
		event=event+"</canvas>";
		event=event+"</div>";
		
		event=event+"</div>";

		event=event+"<div id='down'>";
		
		event=event+"<div id='left' class='column'>";
		event=event+"<h1 class='title'>Category</h1></br>";
		event=event+"<div id='category' class='scrollbar'>";
		event=event+"<img class='no_found' src='img/noCategory.png'></img>";
		event=event+"</div>";
		event=event+"<h1 class='title'>Pictures</h1></br>";
		event=event+"<div id='pictures' class='scrollbar'>";
		event=event+"<img class='no_found' src='img/noImage.png'></img>";
		event=event+"</div>";
		event=event+"</div>";
		
		event=event+"<div class='column'>";
		event=event+"<h1 class='title'>Tweets</h1></br>";
		event=event+"<div id='tweets' class='scrollbar'>";
		event=event+"</div>";
		event=event+"</div>";
		
		event=event+"<div id='right' class='column'>";
		event=event+"<h1 class='title'>Words occurrences</h1></br>";
		event=event+"<div id='words' class='scrollbar'>";
		event=event+"</div>";
		event=event+"<h1 class='title'>Hashtags</h1></br>";
		event=event+"<div id='hashtags' class='scrollbar'>";
		event=event+"</div>";
		event=event+"<h1 class='title'>Name Entities</h1></br>";
		event=event+"<div id='name_entities' class='scrollbar'>";
		event=event+"</div>";
		
		event=event+"</div>";
		
		event=event+"</div>";

			
		Statement selectCities=con.createStatement();
		ResultSet cities=selectCities.executeQuery("SELECT * FROM  `my_cities` where id='"+area+"'");
		
		if(cities.next())
		{	
			double minLatitude = cities.getDouble("min_latitude");
	        double maxLatitude = cities.getDouble("max_latitude");
	        double minLongitude = cities.getDouble("min_longitude");
	        double maxLongitude = cities.getDouble("max_longitude");
	        
	//COSTRUZIONE GRIGLIA: CALCOLO ALTEZZA E LARGHEZZA TOTALE e poi CALCOLO DIMENSIONE GRIGLIA (errore width e height invertite...)
	        double width = maxLatitude - minLatitude;
	        double height = maxLongitude - minLongitude;
	        //parametro  per allargare ogni singola cella e riga ,0 rettanfolo standard.
	        double spost_col=0.00;
	        double spost_rig=0.0;

	//STIMO NUMERO RIGHE E COLONNE GRIGLIA
	        double tilesHorizontal = width/(TILE_EDGE+spost_rig) - width%(TILE_EDGE+spost_rig) + 1;
	        double tilesVertical = height/(TILE_EDGE+spost_col) - height%(TILE_EDGE+spost_col) + 1;
	        if( width%TILE_EDGE == 0)
	            tilesHorizontal = width/(TILE_EDGE+spost_rig) ;
	        if( height%TILE_EDGE == 0)
	            tilesVertical = height/(TILE_EDGE+spost_col) ;
		
        %>

	<script type="text/javascript">
	
	<%  // JAVA ALL INTERNO DI UNO SCRIPT
		// Ciclo su ogni cella:
			
        for (int i=0 ; i<tilesHorizontal ; i++)
        {
            for(int j=0; j<tilesVertical; j++ )
            {
            	
            	// WIDTH e HEIGHT di inizio griglia
            	double startTileVert =  minLatitude+i*(TILE_EDGE+spost_rig);
                double startTileHoriz = minLongitude+j*(TILE_EDGE+spost_col);
                double endTileHoriz;         
                double endTileVert;

                // se è l'ultima cella orizz/verti -> lat./long. max
                if( (i+1)==tilesHorizontal )
                    endTileVert= maxLatitude;
                else // altrimenti imposto dimensione come i+1 * tile_edge
                    endTileVert= minLatitude + (i+1)*(TILE_EDGE+spost_rig);
                if( (j+1)==tilesVertical )
                    endTileHoriz = maxLongitude;
                else
                    endTileHoriz = minLongitude + (j+1)*(TILE_EDGE+spost_col);
        %>  
        // TORNA SCRIPT
  
        // Stile Griglia ,o meglio singola cella
               var myStyle = {
            		weight: 2,
       				opacity: 0.9,
       				color: 'white',
       				fillOpacity: 0.2,
       				fillColor: '#6DADFC'
       				
           		};
               
               var tiles = 
               [{
           	    "type": "Feature",
           	 	"properties": {
           	 		"startTileVert": <%=startTileVert%>,
           	 		"startTileHoriz":<%=startTileHoriz%>,
           	 		"endTileVert":<%=endTileVert%>,
           	 		"endTileHoriz":<%=endTileHoriz%>},
           	        "geometry": {
           	      	  "type": "Polygon",
           	      	  "coordinates":[ [
           	            [<%=startTileHoriz%>,<%=startTileVert%>],
           	         	[ <%=endTileHoriz%>,<%=startTileVert%>],
           	         	[<%=endTileHoriz%>,<%=endTileVert%>],
           	         	[<%=startTileHoriz%>,<%=endTileVert%>]  ]]
           	   					 }
           	    }];
			
			
			function createPopup(e,bounds){ 
	       		// istanzio popup 
	       		var popup = L.popup(
	       			{
	       				maxWidth:2000,
						maxHeight:500,
						className:'builder'
					}).setContent( "<%=event%>");
	       		
				// apro popup
					cella=e.target;
				    cella.bindPopup(popup).openPopup();
				    
	       		    var users = [];
	       		    var peaks = [];
	       		 	var frequency = [];
	       		 		       		 	
	       		 	var sh = bounds[0] [0] [1];
				    var eh = bounds[0] [1] [1];
				    var sv = bounds[0] [0] [0];
	 			    var ev = bounds[0] [2] [0];
	       		 	
	       		 // CARICO CONTENUTO POPUP :
	       		 		// se la cella  è stata ridimensionata dobbiamo fare in modo  di ricalcolare la varianza e di conseguenza i picchi sull'area indicata dai bounds
	       		 		
	       		 	<%
	       		 	// CARICO DAL DB EVENTI REGISTRATI NELL'AREA
	       		 Statement selectEvents = con.createStatement();
	       		 
	       		 String queryEvent="SELECT * FROM event_detected WHERE date='"+today+"' and max_latitude='"+maxLatitude+"' AND max_longitude='"+maxLongitude+"' AND min_latitude='"+minLatitude+"' AND min_longitude='"+minLongitude+"'";
				 ResultSet eventi=selectEvents.executeQuery(queryEvent);
				 
				 while(eventi.next()){
					 int timePeak=eventi.getInt("time_peak");
					 %>
					 $("#events").append("<button id='event' onclick='tweetsFilter("+<%=timePeak%>+")'>Event at "+<%=timePeak%>+":00</button></br>");
					 <%}
					 selectEvents.close();%>
	       		 	
	       		 $.ajax({
		     			url: "allTextMining?sth="+sh+"&eth="+eh+"&stv="+sv+"&etv="+ev, 
		     			method: "GET",
		     			dataType: "json",
		     			complete: function(data)
		     			{  				
			     					
		     				$.each(data['responseJSON'], function(key, value) {
		     					if(key=="hashtags"){
			     					$.each(value, function(key, value){
			     						$("#hashtags").append("<div class='word'>#"+key+" "+value+"</div></br>");
			     						
			     					});
		     					}
		     					if(key=="words"){
			     					$.each(value, function(key, value){
			     						$("#words").append("<div class='word'>"+key+" "+value+"</div></br>");
			     						});
		     					}
		     					if(key=="pictures"){
		     						var first=true;
			     					$.each(value, function(key, value){
			     						if(first){
			     							$("#pictures").html("");
			     							first=false;
			     						}
			     						$("#pictures").append("<img id='picture' src="+key+">");
			     						});
		     					}
		     					if(key=="tweets"){
			     					$.each(value, function(key, value){
			     						$("#tweets").append("<div id=tweet><img id='profile_image' src="+value+">"+key+"</div></br>");
			     						
			     					});
		     					}
		     					
		     					if(key=="names"){
			     					$.each(value, function(key, value){
			     						$("#name_entities").append("<div class='word'>"+key+" "+value+"</div></br>");
			     						});
		     					}
			     			});
		     			
		     				$(".column").css("display","block");
		     				$("#down").css("background-image","none");
		     			}
	       		});	
	       		
	       	   $.ajax({
	         		url: "listEvents?sth="+sh+"&eth="+eh+"&stv="+sv+"&etv="+ev, 
	 			    method: "GET",
	 				dataType: "json",
		     		complete:function(data){
		     			$.each(data['responseJSON'], function(key, value) {
		     				
		     				if(key=="peaks"){
		     					$.each(value, function(key, value){
		     						var peak=value+2;
		     						if(peak>23){
		     							peak=23;
		     						}
		     						$("#events").append("<button id='event' onclick='tweetsFilter("+value+")'>Event at "+peak+":00</button></br>");	     						
		     					});
	     					}
		     			})
		     		}
	         	
		    })
	        		    
	       		    $.ajax({
	  		            		url: "popupBuilder?sth="+sh+"&eth="+eh+"&stv="+sv+"&etv="+ev, 
	  		    			    method: "GET",
	  		    				dataType: "json",
	  			     			complete:function(data){
	  			     				
	  			     				$.each(data['responseJSON'], function(key, value) {
	  			     					if(key=="timeSeries"){
	  			     						users.push(0);
	  			     						users.push(0);
	  				     					$.each(value, function(key, value){
	  				     						users.push(value);
	  				     						});
	  				     					users.push(0);
	  				     					users.push(0);
	  				     					
	  				     					// RISOLTO ERRORE  NEL GRAFICO DEI PICCHI -> ORA  TRASLATA
	  				     					for	(i = 2; i < 25; i++) {
	  				     					 if ((users[i]-users[i-2])>=2 && (users[i]-users[i+2])>=2) {
	  				     						peaks.push(i);
	  				     					    i=i+2;
	  				     					    }
	  				     					} 
	  				     					
	  				     					
	  				     					var lineChartData = {
	  				     							labels : ["00:00","01:00","02:00","03:00","04:00","05:00","06:00","07:00",
	  				     							          "08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00",
	  				     							          "16:00","17:00","18:00","19:00","20:00","21:00","22:00","23:00"],
	  				     							datasets : [
	  				     								{
	  				     									label: "Users by time",
	  				     									fillColor : "rgba(151,187,205,0.2)",
	  				     									strokeColor : "rgba(151,187,205,1)",
	  				     									pointColor : "rgba(151,187,205,1)",
	  				     									pointStrokeColor : "#fff",
	  				     									pointHighlightFill : "#fff",
	  				     									pointHighlightStroke : "rgba(151,187,205,1)",
	  				     									data : [users[2],users[3],users[4],users[5],users[6],users[7],users[8],
	  				     									        users[9],users[10],users[11],users[12],users[13],users[15],users[13],
	  				     									        users[16],users[17],users[18],users[19],users[20],users[21],users[22],
	  				     									        users[23],users[24],users[25]]
	  				     								}
	  				     							]
	  				     		   		 
	  				     						}
	  				     		   		    getChart(lineChartData,"canvasUsers");
	  			     					}
	  			     					
	  			     					});
	  			     				$("#up").css("background-image","none");
	  			     			}
	       		    })
       		    }
               
            // EVIDENZIA RIQUADRO al Passaggio del Mouse - > BLUE
       		function highlightFeature(e)
       		 {
       		    var layer = e.target;
       		    layer.setStyle({
       		        weight: 5,
       		        color: '#666',
       		        dashArray: '',
       		        fillOpacity: 0.7
       		    });

       		    if (!L.Browser.ie && !L.Browser.opera)
       		     { layer.bringToFront();}
       		 }
       		
      		// Carica Grafico tweets per  ora del pop-up 
           function getChart(lineChartData,place){
          			var ctx = document.getElementById(place).getContext("2d");
 					window.myLine = new Chart(ctx).Line(lineChartData, {responsive: true}); 
          	}

       		
       		function click(e) {
			
       		    cella = e.target
				var punti = cella.getBounds().toBBoxString().split(',');

       		    // se attiva la Resize mode, la  cella cliccata  diventa modificabile e col bordo rosso 
				if (document.getElementById("resize").checked)
				{
					cella.bringToFront();
					cella.editing.enable();
					
					cella.setStyle ({
       		        weight: 5,
       		        color: 'red',
       		        dashArray: '',
       		        fillOpacity: 0.7 });
					
					}		
				else 
				{  
					cella.editing.disable();
					
					var punti2 = cella.getBounds().toBBoxString().split(',');
						
					
					s_lat = cella.getBounds().toBBoxString().split(',') [1];
					s_lon = cella.getBounds().toBBoxString().split(',') [0];
					e_lat = cella.getBounds().toBBoxString().split(',') [3];
					e_lon = cella.getBounds().toBBoxString().split(',') [2];
					
					bounds=[[ [s_lon,s_lat],[s_lon,e_lat],[e_lon,e_lat],[e_lon,s_lat] ]];
					
					
					//document.write(punti);
					//if (resized){
						//updatePeak(e,bounds);
					//	}
						
         	    	createPopup(e, bounds);
         	    }
       		}
     		
       		
       		
       		//function updateArea(bounds){
			
			//}
			
       		
       		
       		var geojson;
         	function resetHighlight(e) { geojson.resetStyle(e.target); }
       		function onEachFeature(feature, layer) {
       			layer.on({
       				mouseover: highlightFeature,
       				mouseout: resetHighlight,
       				click:  click,
       			});
       		}
       		
		geojson = L.geoJson(tiles,
		 {
		 	style : myStyle,
			onEachFeature : onEachFeature
		  }).addTo(map);
		
          <%} //chiude primo for j
		} // chiude secondo for i%>
		
	</script>
	<%
		} //chiude if (cities.next()) linea 416
		
	%>
	
	
	<script>

		function yes(peak) 
		{
			$(".conferm").css("pointer-events", "auto");
			$(".word_conferm").css("pointer-events", "auto");
			$("#yes").css("background-color", "#6DADFC");
			$("#no").css("background-color", "initial");

			/// CATEGORIE: TASSONOMIA DA  UNCOMMENTARE E RENDERE EFFICACE
			$("#category")
					.html(
							"<select id='select_category'><option value='Animali' >Animali</option><option value='Conferenze'>Conferenze</option><option value='Cibo' >Cibo</option><option value='Cinema e Spettacoli'>Cinema e Spettacoli</option><option value='Cronaca' >Cronaca</option><option value='Formazione ed educazione' >Formazione ed educazione</option><option value='Gallerie e Mostre'>Gallerie e Mostre</option><option value='Letteratura' >Letteratura</option><option value='Musica e performances'>Musica e performances</option><option value='Organizzazioni'>Organizzazioni</option><option value='Politica'>Politica</option><option value='Salute e Tempo libero'>Salute e Tempo libero</option><option value='Scienza e tecnologia'>Scienza e tecnologia</option><option value='Sociale'>Sociale</option><option value='Spiritualità'>Spiritualità</option><option value='Sport' >Sport</option><option value='Vacanze' >Vacanze</option><option value='Altro' >Altro</option></select>");
			// $.get("taxonomy/Taxonomy.xml",function(data){
			// spl_data=data;
			//var that=$('#select_category');
			//$('category',spl_data).each(function(){
			//	 $('<option/>',{
			//		 value: $(this).attr('label')

			// 	}).appendTo(that);
			// });

			// });

			$.ajax({
				url : "updateEvent?peak=" + peak + "&is_event=2",
				method : "GET",
				dataType : "json",
				complete : function(data) {
				}
			});
		}

		function no(peak)
		{
			$("#no").css("background-color", "#6DADFC");
			$("#yes").css("background-color", "initial");
			$(".conferm").css("pointer-events", "none");
			$(".word_conferm").css("pointer-events", "none");
			$.ajax({
				url : "updateEvent?peak=" + peak + "&is_event=1",
				method : "GET",
				dataType : "json",
				complete : function(data) {
				}
			});
		}

		function done() {
			location.reload();
		}

		//var tweet=$("#tweet").html();
		//alert(tweet);
	</script>
</body>
</html>