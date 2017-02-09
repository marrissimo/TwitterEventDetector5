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
		<title>Events map</title>
		
		<link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.css" />
		<link rel="stylesheet" href="CSS/colorbox.css" />
        <link rel="stylesheet" href="CSS/topBar.css" type="text/css"/>
        <link rel="stylesheet" href="CSS/popUp.css" type="text/css"/>
        <link rel="stylesheet" href="CSS/font-awesome.min.css">
        <link rel="stylesheet" href="CSS/leaflet.awesome-markers.css">
        <link href='http://fonts.googleapis.com/css?family=PT+Sans' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" href="CSS/clusters.css"/>
        <link rel="stylesheet" href="CSS/jquery-impromptu.css"/>
        <link rel="stylesheet" href="CSS/barChart.css"/>
        <link rel="stylesheet" href="CSS/leaflet-label.css"/>
        <link rel="stylesheet" href="CSS/jquery-ui1.css" />
        <link rel="stylesheet" href="CSS/accordionMenu.css"/>
        <link rel="stylesheet" href="CSS/eventPopUp.css"/>
		<link rel="stylesheet" href="CSS/configuration.css"/>
		<link rel="stylesheet" href="CSS/event.css"/>
		
		<script src="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.js"></script>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js"></script>

		<link rel="stylesheet" type="text/css" href="CSS/pikaday.css" />
		<link rel="stylesheet" type="text/css" href="CSS/theme.css" />
		<link rel="stylesheet" type="text/css" href="CSS/triangle.css" />
		<script type="text/javascript" src="JS/moment.js"></script> 
		<script type="text/javascript" src="JS/pikaday.js"></script>
		
		<script type="text/javascript" src="JS/topbar.js"></script>
		<script type="text/javascript" src="JS/configuration.js"></script>
		
		<style>
            body {
                padding: 0;
                margin: 0;
                
            }
            html, body, #map {
                height: 100%;
            }
        </style>
     <script>
     var maxmarker = null;

	
     </script>
      
</head>
<body>

	<%

	Connessione connessione = new Connessione();
    Connection con = connessione.connetti();
    Utility ut=new Utility();
    String area=request.getParameter("area"); 
    String eventDay=request.getParameter("day");
    
    if(area==null){
    	area="L";
    }
    if(eventDay==null || eventDay==""){
    	Statement selectDay = con.createStatement();
    	ResultSet day = selectDay.executeQuery("SELECT MAX( DATE ) maxDay FROM  `event_detected` where area='"+area+"' and event_confirmed!=0");
    	if(day.next()){
    		eventDay=day.getString("maxDay");
    	}
    	selectDay.close();
    }

    session.setAttribute("eventDay", eventDay);
    
    String cityName=Utility.getNameFromProvince(area);
   
  
	%>
	
         <div class="topbar">
            
          <div id="full_logo">
          <a id="logo" href="#" > Twitter Event Detector:</a>
          <select id="cities"  onChange='menu(this)'>
          <option id="city" value='<%=area %>'><%=cityName %></option>
			 <% 
			 Statement selectStatement = con.createStatement();
			 ResultSet areas = selectStatement.executeQuery("select distinct(area) from event_detected where (area='L' or area='NYC') AND area!='"+area+"'");
			 while(areas.next()){
                 String areaPeaks = areas.getString("area");
                 String areaName = Utility.getNameFromProvince(areaPeaks);
              %>
               <option  value='<%=areaPeaks%>'><%=areaName%></option>
               <%    
               }
			 selectStatement.close();
			 	%>
		</select> 
		</div>
		
		
		  <button id="grid">Build Dataset</button>
		  <div class="divider"></div>
		  <button id="getTweets" value=<%=area %> >Get Events</button>
		  <form id='date'>
	       <input type="text" id="datepicker1" value='<%=eventDay%>' > <!-- onchange='updateInput(this.value)' -->
	      </form>
	      
		 
		   <script>
				var picker1 = new Pikaday({   
	    			field: document.getElementById('datepicker1'),
	    	        format : "YYYY-MM-DD",
	    	        defaultDate: new Date('<%=eventDay%>'),
	    	        minDate: new Date(2015, 11, 21)
	    	        });
	    		
		   </script>
		   <script type="text/javascript" src="JS/calendar.js"></script>
		   
		</div>
		
		<script>
         function menu(links) {
		     location.href = "?area="+links.value;
		  }
       </script>
		 
     
        <div id="map"></div>  
        
        <%
        Statement selectCities=con.createStatement();
		ResultSet cities=selectCities.executeQuery("SELECT lat,lng FROM  `my_cities` where id='"+area+"'");
		double latCity=40.766638;
		double lonCity=-73.979916;
		if(cities.next()){
			latCity=cities.getDouble("lat");
			lonCity=cities.getDouble("lng");
			}
		selectCities.close();
		%>
		
		<script type="text/javascript">
        
         var map = L.map('map').setView([<%=latCity%>,<%=lonCity%>], 12);
         
         L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
             attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
         }).addTo(map);
         
        </script>
      <%
      
      String peaksQuery="SELECT * FROM event_detected WHERE date='"+eventDay+"' and area='"+area+"'";
	  //System.out.println(peaksQuery);     
      Statement selectPeaks = con.createStatement();
	  ResultSet events=selectPeaks.executeQuery(peaksQuery);
	
	  Calendar cal = Calendar.getInstance();
	  String today=ut.correctFormattedData(cal);
    
	  double maxDelta=0.0;
	  int maxTimePeak=0;
      while(events.next()){
			String city=events.getString("area");
			double latitude=events.getDouble("latitude");
			double longitude=events.getDouble("longitude");
			double maxLatitude=events.getDouble("max_latitude");
			double minLatitude=events.getDouble("min_latitude");
			double maxLongitude=events.getDouble("max_longitude");
			double minLongitude=events.getDouble("min_longitude");
			int eventConfirmed=events.getInt("event_confirmed");
			int timePeak=events.getInt("time_peak");
			String timeEvent=ut.convertSecondsToHhMmSs((timePeak)*3600);
			String timeMin=ut.convertSecondsToHhMmSs((timePeak-2)*3600);
		    String timeMax=ut.convertSecondsToHhMmSs((timePeak+2)*3600);
		    if(timePeak>=24){
				timeMax=ut.convertSecondsToHhMmSs(3599+23*3600);
			}
			if(timePeak<=0){
				timeMin=ut.convertSecondsToHhMmSs(0);
				}
			

			double distance=events.getDouble("distance");

			Statement selectMean = con.createStatement();
			ResultSet mean=selectMean.executeQuery("SELECT * FROM variance WHERE date='"+today+"' and area='"+area+"' and max_latitude='"+maxLatitude+"' AND max_longitude='"+maxLongitude+"' AND min_latitude='"+minLatitude+"' AND min_longitude='"+minLongitude+"'");
			double media=0.0;
			double variance=0.0;
			if(mean.next()){
				media=mean.getDouble("media");
				variance=mean.getDouble("variance");
			}
			selectMean.close();
		
			if(media==0.0){
				Statement selectNewMedia = con.createStatement();
				ResultSet nMedia=selectNewMedia.executeQuery("SELECT * FROM variance WHERE date='2016-04-09' and area='"+area+"' and max_latitude='"+maxLatitude+"' AND max_longitude='"+maxLongitude+"' AND min_latitude='"+minLatitude+"' AND min_longitude='"+minLongitude+"'");
				if(nMedia.next()){
					media=nMedia.getDouble("media");
					variance=nMedia.getDouble("variance");
				}
				selectNewMedia.close();
			}
			double standardDeviation=Math.sqrt(variance);
			
			if(distance>=media+standardDeviation){
				
		   %>
		
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
	               	 		"minLatitude":<%=minLatitude%>,
	               	 		"maxLongitude":<%=maxLongitude%>,
	               	 		"minLongitude":<%=minLongitude%>},
	        		    
	        		    "geometry": {
	        		        "type": "Point",
	        		        "coordinates": [<%=longitude %>,<%=latitude %>]
	        		    }
	        		};

		       
			</script>
			<%
			
			
			String event="";
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
			event= event+"<h1 class='title'> Event at "+timeEvent+"</h1></br>";
			event=event+"<div id='tweets' class='column scrollbar'>";
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
			%>
			<script>
			function clickFeature(e) {
				$.ajax({
	     			url: "textMining?timePeak="+<%=timePeak%>+"&sth="+e.target.feature.properties.minLatitude+"&eth="
          				+e.target.feature.properties.maxLatitude+"&stv="+e.target.feature.properties.minLongitude
          				+"&etv="+e.target.feature.properties.maxLongitude, 
	     			method: "GET",
	     			dataType: "json",
	     			complete:function(data){
	     				$.each(data['responseJSON'], function(key, value) {
	     					if(key=="hashtags"){
		     					$.each(value, function(key, value){
		     						$("#hashtags").append("#"+key+" "+value+"</br>");
		     						});
	     					}
	     					if(key=="words"){
		     					$.each(value, function(key, value){
		     						$("#words").append(key+" "+value+"</br>");
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
		     						$("#tweets").append("<div id=tweet> <img id='profile_image' src="+value+">"+key+"</div></br>");
		     						});
	     					}
	     					
	     					if(key=="names"){
		     					$.each(value, function(key, value){
		     						$("#name_entities").append(key+" "+value+"</br>");
		     						});
	     					}
		     			});
	     				
	     				$(".column").css("display","block");
	     				$(".leaflet-popup-content").css("background-image","none");
	     			
	     			}
          		});	
       		}
			
			function onEachFeature(feature, layer) {
       			layer.on({
       				click: clickFeature
       			});
       		}
			
			
			var popup = L.popup({
				maxWidth:2000,
				maxHeight:500,
				className:'event'
				
				})
		    .setContent("<%=event%>");
			
			<%if(eventConfirmed==0){%>
			L.geoJson(pmarker, {
				onEachFeature: onEachFeature,
    		    pointToLayer: function (feature, latlng) {
    		        return L.marker(latlng,{icon: blueIcon});
    		    }
    		}).addTo(map).bindPopup(popup);
			<%}else if(eventConfirmed==1){%>
			L.geoJson(pmarker, {
				onEachFeature: onEachFeature,
    		    pointToLayer: function (feature, latlng) {
    		        return L.marker(latlng,{icon: redIcon});
    		    }
    		}).addTo(map).bindPopup(popup);
			<%}else if(eventConfirmed==2){%>
			L.geoJson(pmarker, {
				onEachFeature: onEachFeature,
    		    pointToLayer: function (feature, latlng) {
    		        return L.marker(latlng,{icon: greenIcon});
    		    }
    		}).addTo(map).bindPopup(popup);
			<%}%>
			</script>
			
			<% 
			
			if(maxDelta<(distance-(media+standardDeviation))){
				maxDelta=distance-(media+standardDeviation);
			%>
			<script>
			  var maxmarker = {
	        		    "type": "Feature",
	        		    "properties": {
	               	 		"maxLatitude": <%=maxLatitude%>,
	               	 		"minLatitude":<%=minLatitude%>,
	               	 		"maxLongitude":<%=maxLongitude%>,
	               	 		"minLongitude":<%=minLongitude%>},
	        		    
	        		    "geometry": {
	        		        "type": "Point",
	        		        "coordinates": [<%=longitude %>,<%=latitude %>]
	        		    }
	        		};
			  var maxPopup = L.popup({
					maxWidth:2000,
					maxHeight:500,
					className:'event'
					
					})
			    .setContent("<%=event%>");

			</script>
			
			<%
			maxTimePeak=timePeak;
			}
			}
			%>
     
      
		<%}%>
		
		
			
			
        
</body>
</html>