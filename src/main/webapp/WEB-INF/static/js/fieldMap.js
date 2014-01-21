function validateEnterFieldPage(){
	var totalNoOfPlots;
	
	if($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val() == 0){
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
	
	if (parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) > 255) {
		showEnterFieldDetailsMessage(noOfRowsLimitError);
		return false;
	}
	
	var totalNoOfBlocks = (parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRowsInBlock")).val())
						/parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRowsPerPlot")).val())) 
						* parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRangesInBlock")).val());
	
	
    totalNoOfPlots = totalNumberOfSelectedPlots;    
	
	if(totalNoOfPlots > totalNoOfBlocks) {
		showEnterFieldDetailsMessage(msgBlockSizeError);
		return false;
	} else {
		setTrialInstanceOrder();
		$("#enterFieldDetailsForm").submit();
	}
	
	return true;
}

function setTrialInstanceOrder() {
	var order = [];
	$("#selectedTrials .trialOrder").each(function(){
		var orderId = $(this).parent().parent().attr("id");
		order.push(orderId+"|"+$(this).val());
	});
	$("#"+getJquerySafeId("userFieldmap.order")).val(order.join(","));
}

function setValuesForCounts() {
	//set values for counts
	$("#"+getJquerySafeId("userFieldmap.numberOfEntries")).val($('#studyTree .field-map-highlight td:nth-child(2)').html());
	if (trial) {
		$("#"+getJquerySafeId("userFieldmap.numberOfReps")).val($('#studyTree .field-map-highlight td:nth-child(3)').html());
		$("#"+getJquerySafeId("userFieldmap.totalNumberOfPlots")).val($('#studyTree .field-map-highlight td:nth-child(4)').html());
	}
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
		$('#'+getJquerySafeId('fieldLocationIdAll')).select2({
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
	    	$('#'+getJquerySafeId("userFieldmap.fieldLocationId")).val($('#'+getJquerySafeId("fieldLocationIdAll")).select2("data").id);
	    	$('#'+getJquerySafeId("userFieldmap.locationName")).val($('#'+getJquerySafeId("fieldLocationIdAll")).select2("data").text);
	    });
	
}

function initializeLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj) {

	$.each(locationSuggestionsFav, function( index, value ) {
		locationSuggestionsFav_obj.push({ 'id' : value.locid,
			  'text' : value.lname
		});  
  		
	});


//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
$('#'+getJquerySafeId('fieldLocationIdFavorite')).select2({
    query: function (query) {
      var data = {results: locationSuggestionsFav_obj}, i, j, s;
      // return the array that matches
      data.results = $.grep(data.results,function(item,index) {
        return ($.fn.select2.defaults.matcher(query.term,item.text));
      
      });
        query.callback(data);
        
    }

}).on("change", function (){
	$('#'+getJquerySafeId("userFieldmap.fieldLocationId")).val($('#'+getJquerySafeId("fieldLocationIdFavorite")).select2("data").id);
	$('#'+getJquerySafeId("userFieldmap.locationName")).val($('#'+getJquerySafeId("fieldLocationIdFavorite")).select2("data").text);
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
	
	if (parseInt(startingCol) > parseInt(rowNum)/parseInt(rowsPerPlot) || parseInt(startingCol) <= 0) {
		showMessage(startColInvalid);
		return false;
	}
	
	if (parseInt(startingRange) > parseInt(rangeNum) || parseInt(startingRange) <= 0) {
		showMessage(startRangeInvalid);
		return false;
	}
	
	if (plantingOrder == 0) {
		showMessage(plantingOrderError);
		return false;
	}
	
	if (checkStartingCoordinates()) {
		showMessage(deletedPlotError);
		return false;
	}
	
	return true;
}

function checkStartingCoordinates() {
	var isDeleted = 0;
	$('#field-map td.plot.deleted').each(function(){
		if (isDeletedPlotAtStartCoord($(this).attr('id'))) {
			isDeleted = 1;
			return false;
		}
	});
	
	if (isDeleted == 1) {
		return true;
	}
	return false;
}

function checkRemainingPlots() {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var plantingOrder = $("input[type='radio']:checked").val();
	var remainingPlots = 0;

	if (plantingOrder == "1") {
		//row/column
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots) - (((startingCol-1)*rangeNum)+(startingRange-1));
	} else {
		//serpentine
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots) - getUnavailablePlots(startingCol, startingRange); 
	}
	
	if (totalNoOfPlots > remainingPlots) {
		return true;
	} else {
		return false;
	}
}

function getUnavailablePlots(startingCol, startingRange) {
	//get number of unavailable plots based on starting coordinates
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

function isDeletedPlotAtStartCoord(id) {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var col = parseInt(id.split("_")[0]) + 1;
	var range = parseInt(id.split("_")[1]) + 1;
	//check if starting coordinates is marked as deleted
	if (col == startingCol && range == startingRange) {
		return true;
	} 
	return false;
}