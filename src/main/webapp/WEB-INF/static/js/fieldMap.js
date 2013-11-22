function validateEnterFieldPage(){
	setValuesForCounts();
	var totalNoOfPlots;
	
	if (!$('#studyTree .field-map-highlight').attr('id')) {
		showEnterFieldDetailsMessage(msgNoSelectedTrial);
		return false;
	}
	
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
	
	if (trial) {
    	totalNoOfPlots = $("#"+getJquerySafeId("userFieldmap.totalNumberOfPlots")).val();
	}
    else {
    	totalNoOfPlots = $("#"+getJquerySafeId("userFieldmap.numberOfEntries")).val();
    }
	
	if(totalNoOfPlots > totalNoOfBlocks) {
		showEnterFieldDetailsMessage(msgBlockSizeError);
		return false;
	} else {
		var id = $('#studyTree .field-map-highlight').attr('id').split("|");
		
		//set selected trial instance and its dataset
		$("#"+getJquerySafeId("userFieldmap.selectedDatasetId")).val(id[1]);
		$("#"+getJquerySafeId("userFieldmap.selectedGeolocationId")).val(id[0]);
		$("#enterFieldDetailsForm").submit();
	}
	
	return true;
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
	
	if (col == startingCol && range == startingRange) {
		return true;
	} 
	return false;
}

function createStudyTree(fieldMapInfo) {
	createRow(getPrefixName("study", fieldMapInfo.fieldbookId), "", fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, "");
	$.each(fieldMapInfo.datasets, function (index, value) {
		createRow(getPrefixName("dataset", value.datasetId), getPrefixName("study", fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, "");
		$.each(value.trialInstances, function (index, childValue) {
			createRow(getPrefixName("trialInstance", childValue.geolocationId), getPrefixName("dataset", value.datasetId), childValue, childValue.geolocationId, value.datasetId);
		});
	});
}

function getPrefixName(cat, id) {
	if (parseInt(id) > 0) {
		return cat + id;
	} else {
		return cat + "n" + (parseInt(id)*-1);
	}
}

function createRow(id, parentClass, value, realId, parentId) {
	var genClassName = "treegrid-";
	var genParentClassName = "";
	var newRow = "";
	var newCell = "";	
	if (parentClass != "") {
		genParentClassName = "treegrid-parent-" + parentClass;
	}
	
	if (id.indexOf("study") > -1 || id.indexOf("dataset") > -1) {
		newRow = "<tr id='" + realId + "' class='"+ genClassName + id + " " + genParentClassName + "'>";
		if (trial) {
			newCell = "<td>" + value + "</td><td></td><td></td><td></td>";
		} else {
			newCell = "<td>" + value + "</td><td></td>";
		}
	} else {
		newRow = "<tr id='" + realId + "|" + parentId + "' class='data-row "+ genClassName + id + " " + genParentClassName + "'>";
		if (trial) {
			newCell = "<td>" + value.siteName + "</td><td>" 
					+ value.entryCount + "</td><td>" 
					+ value.repCount + "</td><td>" 
					+ value.plotCount + "</td>";
		} else {
			newCell = "<td>" + value.siteName + "</td><td>" 
			+ value.entryCount + "</td>"; 
		}
	}
	$("#studyTree").append(newRow+newCell+"</tr>");
}