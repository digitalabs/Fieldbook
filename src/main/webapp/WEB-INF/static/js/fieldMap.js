function validateEnterFieldPage(){
	if($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).select2("data") == null){
		showEnterFieldDetailsMessage(msgLocation);
		return false;
	}
	
	if($('#'+getJquerySafeId('userFieldmap.fieldName')).val() == ""){
		showEnterFieldDetailsMessage(msgFieldName);
		return false;
	}
	
	if($('#'+getJquerySafeId('userFieldmap.blockName')).val() == ""){
		showEnterFieldDetailsMessage(msgBlockName);
		return false;
	}
	
	if($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val() == "" 
		|| !isInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val())
		|| parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) < 1){
		showEnterFieldDetailsMessage(msgRowsInBlock);
		return false;
	}
	if($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val() == "" || 
	!isInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val())
	|| parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val()) < 1){
		showEnterFieldDetailsMessage(msgRangesInBlock);
		return false;
	}
	if(parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) % 
			parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).val()) != 0){
		//we need to check 
		
		showEnterFieldDetailsMessage(msgColError);
		return false;
	}
	
	var totalNoOfBlocks = (parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRowsInBlock")).val())
						/parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRowsPerPlot")).val())) 
						* parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRangesInBlock")).val());
	
	if(totalNoOfPlots > totalNoOfBlocks) {
		$("#enterFieldDetailsModal").modal("toggle");
		return false;
	} else {
		$("#enterFieldDetailsForm").submit();
	}
	return true;
	
}

function showEnterFieldDetailsMessage(msg){
		$('#enter-field-details-message').html("<div class='alert alert-danger'>"+ msg +"</div>");
}



function initializeLocationSelect2(locationSuggestions, locationSuggestions_obj) {

			$.each(locationSuggestions, function( index, value ) {
				locationSuggestions_obj.push({ 'id' : value.locid,
					  'text' : value.lname
				});  
		  		
			});
		
	
		//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
		$('#'+getJquerySafeId('userFieldmap.fieldLocationId')).select2({
			minimumInputLength: 2,
	        query: function (query) {
	          var data = {results: locationSuggestions_obj}, i, j, s;
	          // return the array that matches
	          data.results = $.grep(data.results,function(item,index) {
	            return ($.fn.select2.defaults.matcher(query.term,item.text));
	          
	          });
	            query.callback(data);
	            
	        }
	
	    }).on("change", function (){
	    	$('#'+getJquerySafeId("userFieldmap.locationName")).val($('#'+getJquerySafeId("userFieldmap.fieldLocationId")).select2("data").text);
	    });
	
}

function validatePlantingDetails() {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var plantingOrder = $("input[type='radio']:checked").length;
				
	if (startingCol == "") {
		showMessage(startColError);
		return false;
	}
	
	if (startingRange == "") {
		showMessage(startRangeError);
		return false;
	}
	
	if (!isInt(startingCol)) {
		showMessage(startColNotInt);
		return false;
	} 
	
	if (!isInt(startingRange)) {
		showMessage(startRangeNotInt);
		return false;
	}
	
	if (parseInt(startingCol) > parseInt(rowNum)/parseInt(rowsPerPlot) || parseInt(startingCol) == 0) {
		showMessage(startColInvalid);
		return false;
	}
	
	if (parseInt(startingRange) > parseInt(rangeNum) || parseInt(startingRange) == 0) {
		showMessage(startRangeInvalid);
		return false;
	}
	
	if (plantingOrder == 0) {
		showMessage(plantingOrderError);
		return false;
	}
	
	return true;
}

function showMessage(message) {
	$("#page-message").html(
		    "<div class='alert alert-danger'>"+ message +"</div>"
	);
}

function checkRemainingPlots() {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var plantingOrder = $("input[type='radio']:checked").val();
	var remainingPlots = 0;

	if (plantingOrder == "1") {
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots) - (((startingCol-1)*rangeNum)+(startingRange-1));
	} else {
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots) - getUnavailablePlots(startingCol, startingRange); 
	}
	
	if (totalNoOfPlots > remainingPlots) {
		return true;
	} else {
		return false;
	}
}

function getUnavailablePlots(startingCol, startingRange) {
	if (startingCol%2==0) {
		//even column
		return (startingCol*rangeNum)-startingRange;
	} else {
		//odd column
		return (((startingCol-1)*rangeNum)+(startingRange-1));
	}
}

function checkDeletedPlots(id) {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var plantingOrder = $("input[type='radio']:checked").val();
	
	var col = parseInt(id.split("_")[0]) + 1;
	var range = parseInt(id.split("_")[1]) + 1;
	
	if (plantingOrder == "1") {
		//row/column
		if (col > startingCol || (col >= startingCol && range >= startingRange)) {
			deletedPlots++;
		}
	} else {
		//serpentine
		if (col > startingCol || 
				(col == startingCol && 
						((col%2 == 0 && range <= startingRange) || //down  
								(col%2==1 && range >= startingRange)))) { //up
			deletedPlots++;
		}
	}
}