function validateEnterFieldPage(){
	var totalNoOfPlots;
	
	if($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val() == 0){
		showEnterFieldDetailsMessage(msgLocation);
		return false;
	}
	
	if($('#'+getJquerySafeId('userFieldmap.fieldId')).val() == ""){
		showEnterFieldDetailsMessage(msgFieldName);
		return false;
	}
	
	if($('#'+getJquerySafeId('userFieldmap.blockId')).val() == ""){
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
			parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data').id) != 0){
		//we need to check 
		
		showEnterFieldDetailsMessage(msgColError);
		return false;
	}
	
	if (parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) > 255) {
		showEnterFieldDetailsMessage(noOfRowsLimitError);
		return false;
	}
	
	var totalNoOfBlocks = (parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRowsInBlock")).val())
						/parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRowsPerPlot")).select2('data').id)) 
						* parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRangesInBlock")).val());
	
	
    totalNoOfPlots = totalNumberOfSelectedPlots;    
	
	if(isNewBlock == true && totalNoOfPlots > totalNoOfBlocks) {
		showEnterFieldDetailsMessage(msgBlockSizeError);
		return false;
	} else {
		//no error in validation, proceed to the next step
		setTrialInstanceOrder();
		$('.block-details input').attr('disabled', false);
		$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('enable', true);
		$('#'+getJquerySafeId('numberOfRowsPerPlot')).val($('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data').id);
		$('#'+getJquerySafeId('userFieldmap.new')).val(isNewBlock ? "true" : "false");
		$("#enterFieldDetailsForm").submit();
	}
	
	return true;
}

function calculateTotalPlots(){
	var numberOrRowsPerBlock = parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRowsInBlock")).val());
	var numberOfRowsPerPlot = parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRowsPerPlot")).select2('data').id);
	var numberOfRangesInBlock = parseInt($("#"+getJquerySafeId("userFieldmap.numberOfRangesInBlock")).val());
	
	if($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val() == "" 
		|| !isInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val())
		|| parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) < 1){
		$('#calculatedPlots').html("-");
		//console.log("1");
		return false;
	}
	if($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val() == "" || 
	!isInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val())
	|| parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val()) < 1){
		$('#calculatedPlots').html("-");
		//console.log("2");
		return false;
	}
	
	if(parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) % 
			parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data').id) != 0){
		//we need to check 
		
		$('#calculatedPlots').html("-");
		//console.log("3");
		return false;
	}
	
	if(isNaN(numberOrRowsPerBlock) || isNaN(numberOrRowsPerBlock) || isNaN(numberOrRowsPerBlock)){
		$('#calculatedPlots').html("-");
		//console.log("4");
	}else{
		var totalNoOfBlocks = (numberOrRowsPerBlock / numberOfRowsPerPlot) * numberOfRangesInBlock;
		$('#calculatedPlots').html(totalNoOfBlocks);
		//console.log("5");
	}    	    	
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
    	loadFieldsDropdown($('#'+getJquerySafeId("userFieldmap.fieldLocationId")).val(), "");
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
	loadFieldsDropdown($('#'+getJquerySafeId("userFieldmap.fieldLocationId")).val(), "");
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
	//check if starting coordinate is marked as deleted
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

//check if the remaining plots is enough to accommodate the 
//total no. of plots given the deleted plots and starting coordinates
//using horizontal layout
function checkRemainingPlotsHorizontal() {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var plantingOrder = $("input[type='radio']:checked").val();
	var remainingPlots = 0;

	if (plantingOrder == "1") {
		//row/column
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots) - (((startingRange-1)*(rowNum/rowsPerPlot))+(startingCol-1));
	} else {
		//serpentine
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots) - getUnavailablePlotsHorizontal(startingCol, startingRange); 
	}
	
	if (totalNoOfPlots > remainingPlots) {
		return true;
	} else {
		return false;
	}
}

//check if the remaining plots is enough to accommodate the 
//total no. of plots given the deleted plots and starting coordinates
//using vertical layout
function checkRemainingPlotsVertical() {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var plantingOrder = $("input[type='radio']:checked").val();
	var remainingPlots = 0;

	if (plantingOrder == "1") {
		//row/column
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots) - (((startingCol-1)*rangeNum)+(startingRange-1));
	} else {
		//serpentine
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots) - getUnavailablePlotsVertical(startingCol, startingRange); 
	}
	
	if (totalNoOfPlots > remainingPlots) {
		return true;
	} else {
		return false;
	}
}

function getUnavailablePlotsHorizontal(startingCol, startingRange) {
	//get number of unavailable plots based on starting coordinates
	if (startingRange%2==0) {
		//even range
		return (startingRange*(rowNum/rowsPerPlot))-startingCol;
	} else {
		//odd range
		return (((startingRange-1)*(rowNum/rowsPerPlot))+(startingCol-1));
	}
}

function getUnavailablePlotsVertical(startingCol, startingRange) {
	//get number of unavailable plots based on starting coordinates
	if (startingCol%2==0) {
		//even column
		return (startingCol*rangeNum)-startingRange;
	} else {
		//odd column
		return (((startingCol-1)*rangeNum)+(startingRange-1));
	}
}

//count the no. of plots marked as deleted, starting coordinates are considered
function checkDeletedPlotsHorizontal(id, isDelete) {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var plantingOrder = $("input[type='radio']:checked").val();
	
	var col = parseInt(id.split("_")[0]) + 1;
	var range = parseInt(id.split("_")[1]) + 1;
	
	if (plantingOrder == "1") {
		//row/column
		if (range > startingRange || (range == startingRange && col >= startingCol)) {
			if (isDelete) {
				deletedPlots++;
			} else {
				plantedPlots++;
			}
		}
	} else {
		//serpentine
		if (range > startingRange || 
				(range == startingRange && 
					((col <=startingCol && range%2 == 0) || //left
						(col >=startingCol && range%2 == 1 )))) { // right
			if (isDelete) {
				deletedPlots++;
			} else {
				plantedPlots++;
			}
		}
	}
}

//count the no. of plots marked as deleted, starting coordinates are considered
function checkDeletedPlotsVertical(id, isDelete) {
	var startingCol = $('#'+getJquerySafeId("userFieldmap.startingColumn")).val();
	var startingRange = $('#'+getJquerySafeId("userFieldmap.startingRange")).val();
	var plantingOrder = $("input[type='radio']:checked").val();
	
	var col = parseInt(id.split("_")[0]) + 1;
	var range = parseInt(id.split("_")[1]) + 1;
	
	if (plantingOrder == "1") {
		//row/column
		if (col > startingCol || (col >= startingCol && range >= startingRange)) {
			if (isDelete) {
				deletedPlots++;
			} else {
				plantedPlots++;
			}
		}
	} else {
		//serpentine
		if (col > startingCol || 
				(col == startingCol && 
						((col%2 == 0 && range <= startingRange) || //down  
								(col%2==1 && range >= startingRange)))) { //up
			if (isDelete) {
				deletedPlots++;
			} else {
				plantedPlots++;
			}
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

function openManageLocations() {
	$('#manageLocationModal').modal({ backdrop: 'static', keyboard: true });
	$("#manageLocationModal").modal("show");
	if(locationIframeOpened == false){
		locationIframeOpened = true;
		$('#locationFrame').attr('src', programLocationUrl + $('#projectId').val());
	}
}

function recreateLocationCombo() {
	var selectedLocationAll = $("#fieldLocationIdAll").val();
	var selectedLocationFavorite = $("#fieldLocationIdFavorite").val();
	
	Spinner.toggle();
	$.ajax(
	{ url: "/Fieldbook/NurseryManager/advance/nursery/getLocations",
       type: "GET",
       cache: false,
       data: "",
       success: function(data) {
    	   if (data.success == "1") {
    		   //recreate the select2 combos to get updated list of locations
    		   recreateLocationComboAfterClose("fieldLocationIdAll", $.parseJSON(data.allLocations));
    		   recreateLocationComboAfterClose("fieldLocationIdFavorite", $.parseJSON(data.favoriteLocations));
    		   showCorrectLocationCombo();
    		   //set previously selected value of location
    		   if ($("#showFavoriteLocation").prop("checked")) {
    			   setComboValues(locationSuggestionsFav_obj, selectedLocationFavorite, "fieldLocationIdFavorite");
    		   } else {
    			   setComboValues(locationSuggestions_obj, selectedLocationAll, "fieldLocationIdAll");
    		   }
    	   } else {
    		   showErrorMessage("page-message", data.errorMessage);
    	   }
       },
       error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
	   }, 
	   complete: function(){  
		   Spinner.toggle();
	   } 
     }
 );
}

function recreatePopupLocationCombo() {
	
	Spinner.toggle();
	$.ajax(
	{ url: "/Fieldbook/NurseryManager/advance/nursery/getLocations",
       type: "GET",
       cache: false,
       data: "",
       success: function(data) {
    	   if (data.success == "1") {
    		   //recreate the select2 combos to get updated list of locations
    		  
    		   var popuplocationSuggestions =  $.parseJSON(data.allLocations);
    		   var popuplocationSuggestions_obj = [];
    		   
    		   $.each(popuplocationSuggestions, function( index, value ) {
    			   popuplocationSuggestions_obj.push({ 'id' : value.locid,
    					  'text' : value.lname
    				});  
    				
    			});		
    			
    			//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
    			$('#'+getJquerySafeId('parentLocationId')).select2({
    				minimumInputLength: 2,
    		        query: function (query) {
    		          var data = {results: popuplocationSuggestions_obj}, i, j, s;
    		          // return the array that matches
    		          data.results = $.grep(data.results,function(item,index) {
    		            return ($.fn.select2.defaults.matcher(query.term,item.text));
    		          
    		          });
    		            query.callback(data);
    		            
    		        }

    		    });
    			
    	   } else {
    		   showErrorMessage("page-message", data.errorMessage);
    	   }
       },
       error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
	   }, 
	   complete: function(){  
		   Spinner.toggle();
	   } 
     }
 );
}

function recreatePopupFieldCombo() {
	
	Spinner.toggle();
	$.ajax(
	{ url: "/Fieldbook/Fieldmap/enterFieldDetails/getFields",
       type: "GET",
       cache: false,
       data: "",
       success: function(data) {
    	   if (data.success == "1") {
    		   //recreate the select2 combos to get updated list of locations
    		  
    		   var popupFieldlocationSuggestions =  $.parseJSON(data.allFields);
    		   var popupFieldlocationSuggestions_obj = [];
    		   
    		   
    			   $.each(popupFieldlocationSuggestions, function( index, value ) {
        			   popupFieldlocationSuggestions_obj.push({ 'id' : value.locid,
        					  'text' : value.lname
        				});  
        				
        			});
    			   
    			 //if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
       			$('#'+getJquerySafeId('parentFieldId')).select2({
       				//minimumInputLength: 2,
       		        query: function (query) {
       		          var data = {results: popupFieldlocationSuggestions_obj}, i, j, s;
       		          // return the array that matches
       		          data.results = $.grep(data.results,function(item,index) {
       		            return ($.fn.select2.defaults.matcher(query.term,item.text));
       		          
       		          });
       		            query.callback(data);
       		            
       		        }

       		    });
       			
    		   
    		   		
    			
    			
    	   } else {
    		   showErrorMessage("page-message", data.errorMessage);
    	   }
       },
       error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
	   }, 
	   complete: function(){  
		   Spinner.toggle();
	   } 
     }
 );
}

function showCorrectLocationCombo() {
	var isChecked = $('#showFavoriteLocation').is(':checked');
	//if show favorite location is checked, hide all field locations, else, show only favorite locations
	if(isChecked){
		$('#s2id_fieldLocationIdFavorite').show();
		$('#s2id_fieldLocationIdAll').hide();
		if($('#'+getJquerySafeId("fieldLocationIdFavorite")).select2("data") != null){
			$('#'+getJquerySafeId("fieldLocationId")).val($('#'+getJquerySafeId("fieldLocationIdFavorite")).select2("data").id);
			$('#'+getJquerySafeId("fieldLocationName")).val($('#'+getJquerySafeId("fieldLocationIdFavorite")).select2("data").text);
			$('#'+getJquerySafeId("fieldLocationAbbreviation")).val($('#'+getJquerySafeId("fieldLocationIdFavorite")).select2("data").abbr);
			
		}else{
			$('#'+getJquerySafeId("fieldLocationId")).val(0);
			$('#'+getJquerySafeId("fieldLocationName")).val("");
			$('#'+getJquerySafeId("fieldLocationAbbreviation")).val("");
		}
	}else{
		$('#s2id_fieldLocationIdFavorite').hide();
		$('#s2id_fieldLocationIdAll').show();
		if($('#'+getJquerySafeId("fieldLocationIdAll")).select2("data") != null){
			$('#'+getJquerySafeId("fieldLocationId")).val($('#'+getJquerySafeId("fieldLocationIdAll")).select2("data").id);
			$('#'+getJquerySafeId("fieldLocationName")).val($('#'+getJquerySafeId("fieldLocationIdAll")).select2("data").text);
			$('#'+getJquerySafeId("fieldLocationAbbreviation")).val($('#'+getJquerySafeId("fieldLocationIdFavorite")).select2("data").abbr);
		}else{
			$('#'+getJquerySafeId("fieldLocationId")).val(0);
			$('#'+getJquerySafeId("fieldLocationName")).val("");
			$('#'+getJquerySafeId("fieldLocationAbbreviation")).val("");
		}
		
	}
}
function setComboValues(suggestions_obj, id, name) {
	var dataVal = {id:'',text:'',description:''}; //default value
	if(id != ''){
		var count = 0;
		//find the matching value in the array given
    	for(count = 0 ; count < suggestions_obj.length ; count++){
    		if(suggestions_obj[count].id == id){
    			dataVal = suggestions_obj[count];			    			
    			break;
    		}			    			
    	}
	}
	//set the selected value of the combo
	$("#" + name).select2('data', dataVal);
	console.log("set data : " + name);
	console.log(dataVal);
}

function recreateLocationComboAfterClose(comboName, data) {	
	if (comboName == "fieldLocationIdAll") {
		//clear all locations dropdown
		locationSuggestions = [];
		locationSuggestions_obj = [];
		
		initializeLocationSelect2(locationSuggestions, locationSuggestions_obj);
		//reload the data retrieved
		locationSuggestions = data;
		initializeLocationSelect2(locationSuggestions, locationSuggestions_obj);
	} else {
		//clear the favorite locations dropdown
		locationSuggestionsFav = [];
		locationSuggestionsFav_obj = [];
		initializeLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);
		//reload the data
		locationSuggestionsFav = data;
		initializeLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);
	}

}

function initializeFieldSelect2(suggestions, suggestions_obj, addOnChange, currentFieldId) {
	var defaultData = null;
	$.each(suggestions, function( index, value ) {
		var dataObj = { 'id' : value.locid,
			  'text' : value.lname
		};
		suggestions_obj.push(dataObj);  
		
		if(currentFieldId != '' && currentFieldId == value.locid){
			defaultData = dataObj;
		}
	});		
	var defaulData = {'id': 0, 'text': ''};
	$('#'+getJquerySafeId('userFieldmap.fieldId')).select2('data', defaulData);
	$('#'+getJquerySafeId('userFieldmap.fieldId')).val('');
	//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('userFieldmap.fieldId')).select2({
		//minimumInputLength: 2,
        query: function (query) {
          var data = {results: suggestions_obj}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
            query.callback(data);
            
        }

    });
	
	if(addOnChange){
		$('#'+getJquerySafeId('userFieldmap.fieldId')).on("change", function (){
	    	
	    	loadBlockDropdown($('#'+getJquerySafeId("userFieldmap.fieldId")).val(), $('#'+getJquerySafeId("userFieldmap.blockId")).val());
	    	
	    })
	}
	if(defaultData != null)
		$('#'+getJquerySafeId('userFieldmap.fieldId')).select2('data', defaultData).trigger('change');
	
	
}
function initializeBlockSelect2(suggestions, suggestions_obj, addOnChange, currentBlockId) {
	var defaultData = null;
	$.each(suggestions, function( index, value ) {
		var dataObj = { 'id' : value.locid,
				  'text' : value.lname
			};
		
		suggestions_obj.push(dataObj);  
		
		if(currentBlockId != '' && currentBlockId == value.locid){
			defaultData = dataObj;
		}
	});
	
	var defaulData = {'id': 0, 'text': ''};
	$('#'+getJquerySafeId('userFieldmap.blockId')).select2('data', defaulData);
	$('#'+getJquerySafeId('userFieldmap.blockId')).val('');
	//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('userFieldmap.blockId')).select2({
		//minimumInputLength: 2,
        query: function (query) {
          var data = {results: suggestions_obj}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
            query.callback(data);
            
        }

    });
	
	if(addOnChange){
		$('#'+getJquerySafeId('userFieldmap.blockId')).on("change", function (){
	    	
	    	loadBlockInformation($('#'+getJquerySafeId("userFieldmap.blockId")).val());	    	
	    })
	}
	
	if(defaultData != null)
		$('#'+getJquerySafeId('userFieldmap.blockId')).select2('data', defaultData).trigger('change');
}
function loadFieldsDropdown(locationId, currentFieldId){
	//console.log('reload fields ' + locationId);
	
	
	//loadField = true;
	showBlockDetails(true, null);
    
    
		Spinner.toggle();
    	$.ajax(
	    	{ url: "/Fieldbook/Fieldmap/enterFieldDetails/getFields/"+locationId,
	           type: "GET",
	           cache: false,
	           data: "",
	           success: function(data) {	        	   
	        		   //recreate the select2 combos to get updated list of locations
	        		   $('#'+getJquerySafeId('userFieldmap.fieldId')).select2('destroy');
	        		   initializeFieldSelect2($.parseJSON(data.allFields), [], false, currentFieldId);	   
	        		   initializeBlockSelect2({}, [], false, "");
	        	   	   Spinner.toggle();
	        	   	//console.log('here close 1');
	        	   	//loadField = false;
	           }
	         }
	     );
	
	
}
function loadBlockDropdown(fieldId, currentBlockId){
	//console.log('reload block ' + fieldId);
	showBlockDetails(true, null);
	
	//loadBlock = true;
	Spinner.toggle();
	$.ajax(
			{ url: "/Fieldbook/Fieldmap/enterFieldDetails/getBlocks/"+fieldId,
           type: "GET",
           cache: false,
           data: "",
           success: function(data) {	  
        	   $('#'+getJquerySafeId('userFieldmap.blockId')).select2('destroy');
        		   initializeBlockSelect2($.parseJSON(data.allBlocks), [], false, currentBlockId);
        	   	   Spinner.toggle();
        	   	   //console.log('here close 2');
        	   		//loadBlock = false;
           }
         }
     );
}

function loadBlockInformation(blockId){
	//console.log('load block info' + blockId);
	
	//loadBlockInfo = true;
	Spinner.toggle();
	$.ajax(
			{ url: "/Fieldbook/Fieldmap/enterFieldDetails/getBlockInformation/"+blockId,
           type: "GET",
           cache: false,
           data: "",
           success: function(data) {	        	   
        		   var blockInfo = $.parseJSON(data.blockInfo);
        		   //alert('show determine if its new or used block');
        		   showBlockDetails(false, blockInfo);
        		   //console.log(blockInfo);
        		   Spinner.toggle();
        	   	   //loadBlockInfo = false;
           }
         }
     );
}
function showBlockDetails(isHide, blockInfo){
	if(isHide){
		$('.block-details').slideUp( "slow", function() {
			// Animation complete.
			  
		   });
	}else{
		$('.block-details').slideDown( "slow", function() {
			// Animation complete.
			/*
			sample data: 	
				blockId 15471	
				machineRowCapacity 0	
				new false	
				numberOfRowsInPlot 20	
				plantingOrder null	
				rangesInBlock 20	
				rowsInBlock 20
			 */
			if(blockInfo.new == false){
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).val(blockInfo.numberOfRowsInPlot);
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val(blockInfo.rowsInBlock);
				$('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val(blockInfo.rangesInBlock);	
				
				$('.block-details input').attr('disabled', true);
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('enable', false);
				isNewBlock = false;				
			}else{
				//has fieldmap already
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).val(1);
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val(0);
				$('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val(0);
				$('.block-details input').attr('disabled', false);
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('enable', true);
				isNewBlock = true;
			}
			calculateTotalPlots();
		   });
	}
	
}