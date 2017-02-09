  var pmarkerOptions = {
	    		    radius: 12,
	    		    fillColor: "#fff000",
	    		    color: "#000",
	    		    weight: 1,
	    		    opacity: 1,
	    		    fillOpacity: 0.8
	    		};

  function getTweet(tweet){
  	var id="#"+tweet;
  	$(id).attr("src", "img/in.png");
  	
  }
	     
  
  function tweetsFilter(peak){
	  

	  $("#pictures").html("");

	  $("#tweets").html("");

	  $("#name_entities").html("");

	  $("#hashtags").html("");

	  $("#words").html("");

		
	  $("#tweets").css("background-image","url('img/ajax-loader.gif')");
		$("#tweets").css("background-repeat","no-repeat");
		$("#tweets").css("background-position","center");
	    	 $.ajax({
           		url: "tweetsFilter?peak="+peak, 
   			    method: "GET",
   				dataType: "json",
	     		complete:function(data){
	     			var tweet_count=0;
	     			var hashtag_count=0;
	     			var word_count=0;
	     			var entity_count=0;
	     			$("#ask_event").html("");
	     			var event=peak+2;
	     			if(event>23){
							event=23;
						}
	     			$("#ask_event").append("<h3>Event at "+event+":00. Confirmed? </h3></br> <button id='yes' onclick='yes("+peak+")'>Yes</button> <button id='no' onclick='no("+peak+")'>No</button><button id='done' onclick='done()'>Done</button>");
	     			$.each(data['responseJSON'], function(key, value) {
     					if(key=="hashtags"){
     						$("#hashtags").html("");
	     					$.each(value, function(key, value){
	     						$("#hashtags").append("<div class='word'>"+key+" "+value+"</div><img id='hashtag_"+hashtag_count+"' onclick='getTweet(this.id)' class='word_conferm' src='img/out.png'></br>");
	     						hashtag_count=hashtag_count+1;
	     					});
     					}
     					if(key=="words"){
     						$("#words").html("");
	     					$.each(value, function(key, value){
	     						$("#words").append("<div class='word'>"+key+" "+value+"</div><img id='word_"+word_count+"' onclick='getTweet(this.id)' class='word_conferm' src='img/out.png'></br>");
	     						word_count=word_count+1;
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
     						$("#tweets").html("");
	     					$.each(value, function(key, value){
	     						$("#tweets").append("<div id=tweet><img id='profile_image' src="+value+">"+key+"</div><img id='confirmation_"+tweet_count+"' onclick='getTweet(this.id)' class='conferm' src='img/out.png'></br>");
	     						tweet_count=tweet_count+1;
	     					});
     					}
     					
     					if(key=="names"){
     						$("#name_entities").html("");
	     					$.each(value, function(key, value){
	     						$("#name_entities").append("<div class='word'>"+key+" "+value+"</div><img id='entity_"+entity_count+"' onclick='getTweet(this.id)' class='word_conferm' src='img/out.png'></br>");
	     						entity_count=entity_count+1;
	     					});
     					}
	     			});
	     			$("#tweets").css("background-image","none");
	     			}
	    	 })
	     }
	     
	     
	     