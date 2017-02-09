$(document).ready(function() {	


	$("#grid").click(function(){
		var area=$("#city").val();
		
		var day=$("#datepicker1").val();
		location.href = "buildMap.jsp?area="+area+"&day="+day;
		});
	$("#no_grid").click(function(){
		var area=$("#city").val();
		var day=$("#datepicker1").val();
		location.href = "peaksMap.jsp?area="+area+"&day="+day;;
	});
	

	$("#peaks").click(function(){
		var area=$("#city").val();
		location.href = "peaksMap.jsp?area="+area;
		});



});
