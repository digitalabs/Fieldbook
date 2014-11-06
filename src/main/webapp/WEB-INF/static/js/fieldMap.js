function validateEnterFieldPage(){
    'use strict';

	var totalNoOfPlots;
	
	if($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val() == 0){
		showInvalidInputMessage(msgLocation);
		return false;
	}
	
	if($('#'+getJquerySafeId('userFieldmap.fieldId')).val() == ''){
		showInvalidInputMessage(msgFieldName);
		return false;
	}
	
	if($('#'+getJquerySafeId('userFieldmap.blockId')).val() == ''){
		showInvalidInputMessage(msgBlockName);
		return false;
	}
	
	if($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val() == '' ||
        !isInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) ||
        parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) < 1){
		showInvalidInputMessage(msgRowsInBlock);
		return false;
	}
	if($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val() == '' ||
	!isInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val()) ||
        parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val()) < 1){
		showInvalidInputMessage(msgRangesInBlock);
		return false;
	}
	if(parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) % 
			parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data').id) != 0){
		//we need to check 
		
		showInvalidInputMessage(msgColError);
		return false;
	}
	
	if (parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) > 255) {
		showInvalidInputMessage(noOfRowsLimitError);
		return false;
	}
	
	var totalNoOfBlocks = (parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) /
        parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data').id)) *
        parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val());
	
	
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
		$('#'+getJquerySafeId('userFieldmap.new')).val(isNewBlock ? 'true' : 'false');
		$('#'+getJquerySafeId('userFieldmap.fieldName')).val($('#'+getJquerySafeId('userFieldmap.fieldId')).select2('data').text);
		$('#'+getJquerySafeId('userFieldmap.blockName')).val($('#'+getJquerySafeId('userFieldmap.blockId')).select2('data').text);
		$('#enterFieldDetailsForm').submit();
	}
	
	return true;
}

function calculateTotalPlots(){
	var numberOrRowsPerBlock = parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val());
	var numberOfRowsPerPlot = parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data').id);
	var numberOfRangesInBlock = parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val());
	
	if($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val() == ''
		|| !isInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val())
		|| parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) < 1){
		$('#calculatedPlots').html('-');
		return false;
	}
	if($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val() == '' ||
	!isInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val())
	|| parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val()) < 1){
		$('#calculatedPlots').html('-');
		return false;
	}
	
	if(parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val()) % 
			parseInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data').id) != 0){

		$('#calculatedPlots').html('-');
		return false;
	}
	
	if(isNaN(numberOrRowsPerBlock) || isNaN(numberOrRowsPerBlock) || isNaN(numberOrRowsPerBlock)){
		$('#calculatedPlots').html('-');
	}else{
		var totalNoOfBlocks = (numberOrRowsPerBlock / numberOfRowsPerPlot) * numberOfRangesInBlock;
		$('#calculatedPlots').html(totalNoOfBlocks);
    }
}

function setTrialInstanceOrder() {
    'use strict';
	var order = [];
	$('#selectedTrials .trialOrder').each(function(){
		var orderId = $(this).parent().parent().attr('id');
		order.push(orderId+'|'+$(this).val());
	});
	$('#'+getJquerySafeId('userFieldmap.order')).val(order.join(','));
}

function setValuesForCounts() {
	'use strict';
	//set values for counts
	$('#'+getJquerySafeId('userFieldmap.numberOfEntries')).val($('#studyFieldMapTree .field-map-highlight td:nth-child(2)').html());
	if (trial) {
		$('#'+getJquerySafeId('userFieldmap.numberOfReps')).val($('#studyFieldMapTree .field-map-highlight td:nth-child(3)').html());
		$('#'+getJquerySafeId('userFieldmap.totalNumberOfPlots')).val($('#studyFieldMapTree .field-map-highlight td:nth-child(4)').html());
	}
}

function showEnterFieldDetailsMessage(msg){
    'use strict';
	createErrorNotification(errorMsgHeader,msg);
}

function initializeLocationSelect2(locationSuggestions, locationSuggestions_obj) {
    'use strict';
	$.each(locationSuggestions, function( index, value ) {
		locationSuggestions_obj.push({ 'id' : value.locid,
            'text' : value.lname
		});  
		
	});		
	
	//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('fieldLocationIdAll')).select2({
		minimumResultsForSearch: locationSuggestions_obj.length == 0 ? -1 : 20,
        query: function (query) {
          var data = {results: locationSuggestions_obj}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
            query.callback(data);
            
        }

    }).on('change', function (){
        $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data').id);
        $('#'+getJquerySafeId('userFieldmap.locationName')).val($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data').text);
        loadFieldsDropdown($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val(), '');
    });
	
}

function initializeLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj) {
    'use strict';
	$.each(locationSuggestionsFav, function( index, value ) {
		locationSuggestionsFav_obj.push({ 'id' : value.locid,
            'text' : value.lname
		});
	});

    //if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
    $('#'+getJquerySafeId('fieldLocationIdFavorite')).select2({
        minimumResultsForSearch: locationSuggestionsFav_obj.length == 0 ? -1 : 20,
        query: function (query) {
          var data = {results: locationSuggestionsFav_obj}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));

          });
            query.callback(data);

        }

    }).on('change', function (){
        $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').id);
        $('#'+getJquerySafeId('userFieldmap.locationName')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').text);
        loadFieldsDropdown($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val(), '');
    });

}

function validatePlantingDetails() {
    'use strict';
	var startingCol = $('#'+getJquerySafeId('userFieldmap.startingColumn')).val();
	var startingRange = $('#'+getJquerySafeId('userFieldmap.startingRange')).val();
	var plantingOrder = $('input[type=radio]:checked').length;
				
	if (startingCol == '') {
		showInvalidInputMessage(startColError);
		return false;
	}
	
	if (startingRange == '') {
		showInvalidInputMessage(startRangeError);
		return false;
	}
	
	if (!isInt(startingCol)) {
		showInvalidInputMessage(startColNotInt);
		return false;
	} 
	
	if (!isInt(startingRange)) {
		showInvalidInputMessage(startRangeNotInt);
		return false;
	}
	
	if (parseInt(startingCol) > parseInt(rowNum)/parseInt(rowsPerPlot) || parseInt(startingCol) <= 0) {
		showInvalidInputMessage(startColInvalid);
		return false;
	}
	
	if (parseInt(startingRange) > parseInt(rangeNum) || parseInt(startingRange) <= 0) {
		showInvalidInputMessage(startRangeInvalid);
		return false;
	}
	
	if (plantingOrder == 0) {
		showInvalidInputMessage(plantingOrderError);
		return false;
	}
	
	if (checkStartingCoordinates()) {
		showInvalidInputMessage(deletedPlotError);
		return false;
	}
	
	if (checkStartingCoordinatesPlanted()) {
		showMessage(plantedPlotError);
		return false;
	}
	
	return true;
}

function checkStartingCoordinates() {
	'use strict';
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

function checkStartingCoordinatesPlanted() {
    'use strict';
    var isPlanted = 0;
	//check if starting coordinate is marked as deleted
	$('#field-map td.plot.planted').each(function(){
		if (isDeletedPlotAtStartCoord($(this).attr('id'))) {
			isPlanted = 1;
			return false;
		}
	});
	
	if (isPlanted == 1) {
		return true;
	}
	return false;
}

//check if the remaining plots is enough to accommodate the 
//total no. of plots given the deleted plots and starting coordinates
//using horizontal layout
function checkRemainingPlotsHorizontal() {
    'use strict';
    var startingCol = $('#'+getJquerySafeId('userFieldmap.startingColumn')).val();
	var startingRange = $('#'+getJquerySafeId('userFieldmap.startingRange')).val();
	var plantingOrder = $('input[type=radio]:checked').val();
	var remainingPlots = 0;

	if (plantingOrder == '1') {
		//row/column
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots-plantedPlots) - (((startingRange-1)*(rowNum/rowsPerPlot))+(startingCol-1));
	} else {
		//serpentine
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots-plantedPlots) - getUnavailablePlotsHorizontal(startingCol, startingRange); 
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
    'use strict';
    var startingCol = $('#'+getJquerySafeId('userFieldmap.startingColumn')).val();
	var startingRange = $('#'+getJquerySafeId('userFieldmap.startingRange')).val();
	var plantingOrder = $('input[type=radio]:checked').val();
	var remainingPlots = 0;

	if (plantingOrder == '1') {
		//row/column
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots-plantedPlots) - (((startingCol-1)*rangeNum)+(startingRange-1));
	} else {
		//serpentine
		remainingPlots = (((parseInt(rowNum)/parseInt(rowsPerPlot))*rangeNum)-deletedPlots-plantedPlots) - getUnavailablePlotsVertical(startingCol, startingRange); 
	}
	
	if (totalNoOfPlots > remainingPlots) {
		return true;
	} else {
		return false;
	}
}

function getUnavailablePlotsHorizontal(startingCol, startingRange) {
    'use strict';
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
    'use strict';
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
    'use strict';
    var startingCol = $('#'+getJquerySafeId('userFieldmap.startingColumn')).val();
	var startingRange = $('#'+getJquerySafeId('userFieldmap.startingRange')).val();
	var plantingOrder = $('input[type=radio]:checked').val();
	
	var col = parseInt(id.split('_')[0]) + 1;
	var range = parseInt(id.split('_')[1]) + 1;
	
	if (plantingOrder == '1') {
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
    'use strict';
	var startingCol = $('#'+getJquerySafeId('userFieldmap.startingColumn')).val();
	var startingRange = $('#'+getJquerySafeId('userFieldmap.startingRange')).val();
	var plantingOrder = $('input[type=radio]:checked').val();
	
	var col = parseInt(id.split('_')[0]) + 1;
	var range = parseInt(id.split('_')[1]) + 1;
	
	if (plantingOrder == '1') {
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
    'use strict';
    var startingCol = $('#'+getJquerySafeId('userFieldmap.startingColumn')).val();
	var startingRange = $('#'+getJquerySafeId('userFieldmap.startingRange')).val();
	var col = parseInt(id.split('_')[0]) + 1;
	var range = parseInt(id.split('_')[1]) + 1;
	//check if starting coordinates is marked as deleted
	if (col == startingCol && range == startingRange) {
		return true;
	} 
	return false;
}

function recreatePopupLocationCombo() {
	'use strict';
	$.ajax(
	{ url: '/Fieldbook/NurseryManager/advance/nursery/getLocations',
        type: 'GET',
        cache: false,
        data: '',
        success: function(data) {
           if (data.success == '1') {
               //recreate the select2 combos to get updated list of locations
               //we check if the favorite is check then we use favorite locations

               var popuplocationSuggestions =  $.parseJSON($('#showFavoriteLocation').is(':checked') ? data.favoriteLocations : data.allBreedingLocations);
               var popuplocationSuggestions_obj = [];
               var defaultData = null;
               var currentLocId = $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val();
               $.each(popuplocationSuggestions, function( index, value ) {
                   var tempData = { 'id' : value.locid,
                          'text' : value.lname
                   };
                   if(currentLocId != '' && currentLocId == value.locid){
                       defaultData = tempData;
                   }


                   popuplocationSuggestions_obj.push(tempData);
               });

                //if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
                $('#'+getJquerySafeId('parentLocationId')).select2({
                    minimumResultsForSearch: popuplocationSuggestions_obj.length == 0 ? -1 : 20,
                    query: function (query) {
                      var data = {results: popuplocationSuggestions_obj}, i, j, s;
                      // return the array that matches
                      data.results = $.grep(data.results,function(item,index) {
                        return ($.fn.select2.defaults.matcher(query.term,item.text));

                      });
                        query.callback(data);
                    }
                });

                if(defaultData != null) {
                    $('#'+getJquerySafeId('parentLocationId')).select2('data', defaultData);
                }

           } else {
               showErrorMessage('page-message', data.errorMessage);
           }
        }
    }
 );
}

function formatFieldResult(myItem) {
    'use strict';
    return escapeHtml(myItem.text);
 }

function formatField(myItem) {
    'use strict';
	return '<p><strong>'+escapeHtml(myItem.text)+'</strong> <br /> Location: '+escapeHtml(myItem.location)+'</p>';
}

function escapeHtml(unsafe) {
    return unsafe
         .replace(/&/g, '&amp;')
         .replace(/</g, '&lt;')
         .replace(/>/g, '&gt;')
         .replace(/"/g, '&quot;')
         .replace(/'/g, '&#039;');
 }

function recreatePopupFieldCombo() {
	'use strict';
	$.ajax(
	{ url: '/Fieldbook/Fieldmap/enterFieldDetails/getFields',
        type: 'GET',
        cache: false,
        data: '',
        success: function(data) {
           if (data.success == '1') {
               //recreate the select2 combos to get updated list of locations

               var popupFieldlocationSuggestions =  $.parseJSON(data.allFields);
               var popupFieldlocationSuggestions_obj = [];
               var defaultData = null;
               var currentLocId = $('#'+getJquerySafeId('userFieldmap.fieldId')).val();

                   $.each(popupFieldlocationSuggestions, function( index, value ) {
                       var parentLocation = value.parentLocationName != null ? value.parentLocationName : '';
                       var tempData = { 'id' : value.locid,
                              'text' : value.lname,
                              'location' : parentLocation
                        };
                       popupFieldlocationSuggestions_obj.push(tempData);

                       if(currentLocId == value.locid) {
                           defaultData = tempData;
                       }
                    });

                 //if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
                $('#'+getJquerySafeId('parentFieldId')).select2({
                    minimumResultsForSearch: popupFieldlocationSuggestions_obj.length == 0 ? -1 : 20,
                    query: function (query) {
                      var data = {results: popupFieldlocationSuggestions_obj}, i, j, s;
                      // return the array that matches
                      data.results = $.grep(data.results,function(item,index) {
                        return ($.fn.select2.defaults.matcher(query.term,item.text));

                      });
                        query.callback(data);

                    },
                    escapeMarkup: function(m) {
                        // Do not escape HTML in the select options text
                        return m;
                     },
                     formatResult: formatField,
                     formatSelection: formatFieldResult

                });


                if(defaultData != null) {
                    $('#'+getJquerySafeId('parentFieldId')).select2('data', defaultData);
                }


           } else {
               showErrorMessage('page-message', data.errorMessage);
           }
        },
        error: function(jqXHR, textStatus, errorThrown){
            console.log('The following error occured: ' + textStatus, errorThrown);
        },
        complete: function(){
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
		if($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data') != null){
			$('#'+getJquerySafeId('fieldLocationId')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').id);
			$('#'+getJquerySafeId('fieldLocationName')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').text);
			$('#'+getJquerySafeId('fieldLocationAbbreviation')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').abbr);
			
		} else {
			$('#'+getJquerySafeId('fieldLocationId')).val(0);
			$('#'+getJquerySafeId('fieldLocationName')).val('');
			$('#'+getJquerySafeId('fieldLocationAbbreviation')).val('');
		}
	} else {
		$('#s2id_fieldLocationIdFavorite').hide();
		$('#s2id_fieldLocationIdAll').show();
		if($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data') != null){
			$('#'+getJquerySafeId('fieldLocationId')).val($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data').id);
			$('#'+getJquerySafeId('fieldLocationName')).val($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data').text);
			$('#'+getJquerySafeId('fieldLocationAbbreviation')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').abbr);
		} else {
			$('#'+getJquerySafeId('fieldLocationId')).val(0);
			$('#'+getJquerySafeId('fieldLocationName')).val('');
			$('#'+getJquerySafeId('fieldLocationAbbreviation')).val('');
		}
		
	}
}
function setComboValues(suggestions_obj, id, name) {
    'use strict';
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
	$('#' + name).select2('data', dataVal);
}

function recreateLocationComboAfterClose(comboName, data) {
    'use strict';
	if (comboName == 'fieldLocationIdAll') {
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
    'use strict';
	var defaultData = null;
	
	var newlyAddedField = $('#newFieldName').val();
	
	$.each(suggestions, function( index, value ) {
		var dataObj = { 'id' : value.locid,
			  'text' : value.lname
		};
		suggestions_obj.push(dataObj);  
		
		if(newlyAddedField == '' && currentFieldId != '' && currentFieldId == value.locid){
			defaultData = dataObj;
		} else if(newlyAddedField != '' && newlyAddedField == value.lname) {
			defaultData = dataObj;
		}
	});		
	var defaulData = {'id': 0, 'text': ''};
	$('#'+getJquerySafeId('userFieldmap.fieldId')).select2('data', defaulData);
	$('#'+getJquerySafeId('userFieldmap.fieldId')).val('');
	//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('userFieldmap.fieldId')).select2({
		minimumResultsForSearch: suggestions_obj.length == 0 ? -1 : 20,
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
		$('#'+getJquerySafeId('userFieldmap.fieldId')).on('change', function (){
	    	loadBlockDropdown($('#'+getJquerySafeId('userFieldmap.fieldId')).val(), $('#'+getJquerySafeId('userFieldmap.blockId')).val());
	    });
	}
	if(defaultData != null) {
        $('#'+getJquerySafeId('userFieldmap.fieldId')).select2('data', defaultData).trigger('change');
    }
}

function initializeBlockSelect2(suggestions, suggestions_obj, addOnChange, currentBlockId) {
	'use strict';
	var defaultData = null;
	var newlyAddedBlock = $('#newBlockName').val();
	$.each(suggestions, function( index, value ) {
		var dataObj = { 'id' : value.locid,
				  'text' : value.lname
			};
		
		suggestions_obj.push(dataObj);  
		
		if(newlyAddedBlock == '' && currentBlockId != '' && currentBlockId == value.locid) {
			defaultData = dataObj;
		} else if(newlyAddedBlock != '' && newlyAddedBlock == value.lname) {
			defaultData = dataObj;
		}
	});
	
	var defaulData = {'id': 0, 'text': ''};
	$('#'+getJquerySafeId('userFieldmap.blockId')).select2('data', defaulData);
	$('#'+getJquerySafeId('userFieldmap.blockId')).val('');
	//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('userFieldmap.blockId')).select2({
		minimumResultsForSearch: suggestions_obj.length == 0 ? -1 : 20,
        query: function (query) {
          var data = {results: suggestions_obj}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
            query.callback(data);
            
        }

    });
	
	if(addOnChange) {
		$('#'+getJquerySafeId('userFieldmap.blockId')).on('change', function (){
	    	
	    	loadBlockInformation($('#'+getJquerySafeId('userFieldmap.blockId')).val());
	    });
	}
	
	if(defaultData != null) {
		$('#'+getJquerySafeId('userFieldmap.blockId')).select2('data', defaultData).trigger('change');
	}
}
function loadFieldsDropdown(locationId, currentFieldId){
	'use strict';
	showBlockDetails(true, null);
    
    
    	$.ajax(
	    	{ url: '/Fieldbook/Fieldmap/enterFieldDetails/getFields/'+locationId,
	           type: 'GET',
	           cache: false,
	           data: '',
	           async: false,
	           success: function(data) {	        	   
	        		   //recreate the select2 combos to get updated list of locations
	        		   $('#'+getJquerySafeId('userFieldmap.fieldId')).select2('destroy');
	        		   if(locationId != '') {
                           initializeFieldSelect2($.parseJSON(data.allFields), [], false, currentFieldId);
                       } else {
                           initializeFieldSelect2({}, [], false, '');
                       }
	        			initializeBlockSelect2({}, [], false, '');

	           }
	         }
	     );
	
	
}
function loadBlockDropdown(fieldId, currentBlockId){
	showBlockDetails(true, null);
	
	$.ajax(
			{ url: '/Fieldbook/Fieldmap/enterFieldDetails/getBlocks/'+fieldId,
           type: 'GET',
           cache: false,
           data: '',
           async: false,
           success: function(data) {	  
        	   $('#'+getJquerySafeId('userFieldmap.blockId')).select2('destroy');
        		   initializeBlockSelect2($.parseJSON(data.allBlocks), [], false, currentBlockId);

           }
         }
     );
}

function loadBlockInformation(blockId){
	'use strict';
	$.ajax(
			{ url: '/Fieldbook/Fieldmap/enterFieldDetails/getBlockInformation/'+blockId,
           type: 'GET',
           cache: false,
           data: '',
           async: false,
           success: function(data) {	        	   
        		   var blockInfo = $.parseJSON(data.blockInfo);
        		   showBlockDetails(false, blockInfo);
        		   $('body').data('previousFmapData', '0');
           }
         }
     );
}
function showBlockDetails(isHide, blockInfo){
	'use strict';
	if(isHide){
		$('.block-details').slideUp( 'slow', function() {
			// Animation complete.
			  
		   });
	} else {
		$('.block-details').slideDown( 'slow', function() {
			// Animation complete.
			if($('body').data('previousFmapData') === '1'){
				if(blockInfo.newBlock == false){
					isNewBlock = false;
					$('.block-details input').attr('disabled', true);
					$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('enable', false);
				} else {
					isNewBlock = true;
					$('.block-details input').attr('disabled', false);
					$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('enable', true);
				}
				return;
			}
			if(blockInfo.newBlock == false){
				var rowsPerPlotData = {'id': blockInfo.numberOfRowsInPlot , 'text' : blockInfo.numberOfRowsInPlot};
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data', rowsPerPlotData);
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val(blockInfo.rowsInBlock);
				$('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val(blockInfo.rangesInBlock);	
				
				$('.block-details input').attr('disabled', true);
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('enable', false);
				isNewBlock = false;			
				
			} else {
				//has fieldmap already
				var rowsPerPlotData = {'id': 1 , 'text' : 1};
				$('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data', rowsPerPlotData);
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

/* The following is needed by enterFieldDetails.html */
function doEnterFieldDetailsPageLoad() {
    'use strict';
    $('.calculate-plot').on('change', function(){
        calculateTotalPlots();
    });

    $('.calculate-plot').on('keyup', function(){
        calculateTotalPlots();
    });

    var prevFieldLocationId = $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val(),
        prevFieldId = $('#'+getJquerySafeId('userFieldmap.fieldId')).val(),
        prevBlockId = $('#'+getJquerySafeId('userFieldmap.blockId')).val();


    $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val('');
    programLocationUrl = $('#programLocationUrl').val();
    setSelectedTrialsAsDraggable();
    calculateTotalPlots();

    initializeLocationSelect2(locationSuggestions, locationSuggestions_obj);
    initializeLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);

    initializeFieldSelect2({}, [], true);
    initializeBlockSelect2({}, [], true);

    showBlockDetails(true, null);

    // remove any other listeners for the location update
    $(document).off('location-update');
    $(document).on('location-update', recreateLocationCombo);

    $('#s2id_fieldLocationIdFavorite').hide();
    $('#s2id_fieldLocationIdAll').show();
    $('#showFavoriteLocation').removeAttr('checked');
    $('#showFavoriteLocation').on('change', function(){
        var isChecked = $('#showFavoriteLocation').is(':checked');
        //if show favorite location is checked, hide all field locations, else, show only favorite locations
        if(isChecked){
            $('#s2id_fieldLocationIdFavorite').show();
            $('#s2id_fieldLocationIdAll').hide();
            if($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data') != null){
                $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').id);
                $('#'+getJquerySafeId('userFieldmap.locationName')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').text);
            }else{
                $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val('');
                $('#'+getJquerySafeId('userFieldmap.locationName')).val('');
            }
        }else{
            $('#s2id_fieldLocationIdFavorite').hide();
            $('#s2id_fieldLocationIdAll').show();
            if($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data') != null){
                $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data').id);
                $('#'+getJquerySafeId('userFieldmap.locationName')).val($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data').text);
            }else{
                $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val('');
                $('#'+getJquerySafeId('userFieldmap.locationName')).val('');
            }

        }
        loadFieldsDropdown($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val());
    });

    var numRowPerPlot = $('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).val();
    $('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).val(defaultRowsPerPlot);

    if($('#'+getJquerySafeId('userFieldmap.fieldId')).val() != '') {
        var favLocationChkElem =  $('#showFavoriteLocation');
        favLocationChkElem.prop('checked',!favLocationChkElem.prop('checked'));

        var isChecked = favLocationChkElem.is(':checked');
        var locId = $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val();

        $('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).val(numRowPerPlot);

        if(isChecked) {
            $('#s2id_fieldLocationIdFavorite').show();
            $('#s2id_fieldLocationIdAll').hide();
            for(var index in locationSuggestionsFav_obj) {
                if(locationSuggestionsFav_obj[index].id == locId){

                    $('#fieldLocationIdFavorite').select2('data', locationSuggestionsFav_obj[index]);
                    $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').id);
                    $('#'+getJquerySafeId('userFieldmap.locationName')).val($('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data').text);
                    break;
                }
            }

        } else {
        	//not checked
            $('#s2id_fieldLocationIdFavorite').hide();
            $('#s2id_fieldLocationIdAll').show();
            for(var index in locationSuggestions_obj) {
                if(locationSuggestions_obj[index].id == locId) {
                    $('#fieldLocationIdAll').select2('data', locationSuggestions_obj[index]);
                    $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data').id);
                    $('#'+getJquerySafeId('userFieldmap.locationName')).val($('#'+getJquerySafeId('fieldLocationIdAll')).select2('data').text);
                    break;
                }
            }

        }
    }

    $('#'+getJquerySafeId('fieldLocationIdAll')).val('');
    $('#'+getJquerySafeId('fieldLocationIdFavorite')).val('');
    if($('#'+getJquerySafeId('userFieldmap.fieldId')).val() != '') {
        loadFieldsDropdown($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val());
    }

    $('#addFieldsModal').on('hidden.bs.modal', function (e) {
        if($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val() != '' && $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val() == $('#parentLocationId').val()){
            loadFieldsDropdown($('#parentLocationId').val(), $('#'+getJquerySafeId('userFieldmap.fieldId')).val());
        }

    });
    $('#addBlocksModal').on('hidden.bs.modal', function (e) {
        if($('#'+getJquerySafeId('userFieldmap.fieldId')).val() != '' && $('#'+getJquerySafeId('userFieldmap.fieldId')).val() == $('#parentFieldId').val()){
            loadBlockDropdown($('#parentFieldId').val(), $('#'+getJquerySafeId('userFieldmap.blockId')).val());
        }

    });

    if (locationSuggestionsFav.length > 0) {
        $('#showFavoriteLocation').click();
    }

    doPreselectValues(prevFieldLocationId, prevFieldId, prevBlockId);
}

function doPreselectValues(locationId, fieldId, blockId) {
    'use strict';
    var prevNumberOfRowsPerPlot = $('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data').id,
        prevNumberOfRowsInBlock = $('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val(),
        prevNumberOfRangesInBlock = $('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val();

    if(locationId !== '' && locationId !== '0'){
        //we preload it
        var isFound = false,
            isFoundInFav = false,
            locationDefaultData = [];
        for(var index = 0 ; index < locationSuggestions_obj.length ; index++){
            if(locationSuggestions_obj[index].id == locationId){
                isFound = true;
                locationDefaultData = locationSuggestions_obj[index];
                break;
            }
        }
        if(!isFound){
            for(var index = 0 ; index < locationSuggestionsFav_obj.length ; index++){
                if(locationSuggestionsFav_obj[index].id == locationId){
                    isFound = true;
                    isFoundInFav = true;
                    locationDefaultData = locationSuggestionsFav_obj[index];
                    break;
                }
            }
        }
        //we now pre select it
        if(isFound){
            if(isFoundInFav){
                $('#showFavoriteLocation').prop('checked', true);
                $('#showFavoriteLocation').change();
                $('#'+getJquerySafeId('fieldLocationIdFavorite')).select2('data', locationDefaultData);
            }else{
                $('#showFavoriteLocation').prop('checked', false);
                $('#showFavoriteLocation').change();
                $('#'+getJquerySafeId('fieldLocationIdAll')).select2('data', locationDefaultData);
            }
            $('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val(locationDefaultData.id);
            //we preselect the fieldId
            loadFieldsDropdown(locationId, fieldId);
            loadBlockDropdown(fieldId, blockId);
            $('body').data('previousFmapData', '1');
            var rowsPerPlotData = {'id': prevNumberOfRowsPerPlot , 'text' : prevNumberOfRowsPerPlot};
            $('#'+getJquerySafeId('userFieldmap.numberOfRowsPerPlot')).select2('data', rowsPerPlotData);
            $('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val(prevNumberOfRowsInBlock);
            $('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val(prevNumberOfRangesInBlock);
            calculateTotalPlots();

        }
    }
}
