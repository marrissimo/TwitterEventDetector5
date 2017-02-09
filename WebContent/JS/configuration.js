$(document).ready(function() {	 

	
	$("#update").click(function(){
		
		var eventDay=$("#datepicker1").val();
		var area=$("#city").val();
		var from=$("#start").val();
		var to=$("#end").val();
		var dt=$("#dt").val();
		var delta=$("#delta").val();

				
		$.ajax({
			url: "updatePeak?day="+eventDay+"&area="+area+"&fromDate="+from+"&toDate="+to
			+"&dt="+dt+"&delta="+delta, 
			method: "GET",
			dataType: "json",
			complete:function(data){
				 $.each(data['responseJSON'], function(key, value) {
                 //if(key==0){
					if(value=="true"){
					location.href = "tileMap.jsp?day="+eventDay+"&area="+area;
					}else{
						alert(value);
					}
                 //}
                 //if(key==1){
                //	 alert(value);
                 //}
				 
				});
			}
		})
		});
	
	$("#getTweets").click(function(){
		var eventDay=$("#datepicker1").val();
		var area=$("#getTweets").val();
		
		location.href = "?day="+eventDay+"&area="+area;
	  		  
		});
	
	$("#build").click(function(){
		
		var eventDay=$("#datepicker1").val();
		var area=$("#build").val();
		var from=$("#start").val();
		var to=$("#end").val();
		var dt=$("#dt").val();
		var delta=$("#delta").val();
		var fromV=$("#startV").val();
		var toV=$("#endV").val();

				
		$.ajax({
			url: "buildPeak?day="+eventDay+"&area="+area+"&fromDate="+from+"&toDate="+to
			+"&dt="+dt+"&delta="+delta+"&fromVariance="+fromV+"&toVariance="+toV, 
			method: "GET",
			dataType: "json",
			complete:function(data){
				 $.each(data['responseJSON'], function(key, value) {
					 
					if(value=="true"){
					location.href = "tileMap.jsp?day="+eventDay+"&area="+area;
					}else{
						alert(value);
					}
				 
				});
			}
		})
		});
	
	

	
	
});