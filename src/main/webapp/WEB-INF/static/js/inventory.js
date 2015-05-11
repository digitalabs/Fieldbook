/*globals selectedGidsForAdvance, getJquerySafeId, Spinner, showErrorMessage, triggerInventoryTableSelection*/
/*globals initializePossibleValuesComboInventory, initializePossibleValuesComboScale, germplasmSelectError, inventoryLocationSuggestions*/
/*globals initializePossibleValuesComboScale, scaleSuggestions, initializePossibleValuesComboInventory*/
/*globals saveLots, caleRequired, inventoryFavoriteLocationSuggestions, locationRequired*/
/*exported initializePossibleValuesComboInventory, triggerInventoryTableSelection, getCurrentAdvanceTabListIdentifier, addLot, initializePossibleValuesComboScale*/
function triggerInventoryTableSelection(tableName, sectionContainerDiv, listIdentifier){
	'use strict';
	$('#' + sectionContainerDiv + ' #'+tableName+' tr.primaryRow').on('click', function() {			
			$(this).toggleClass('field-map-highlight');
			var gid = $(this).data('gid') + '';
			if(selectedGidsForAdvance[listIdentifier] == null) {
				selectedGidsForAdvance[listIdentifier] = [];
			}
			
			if($(this).hasClass('field-map-highlight')){				
				selectedGidsForAdvance[listIdentifier][gid] = gid;
			}else{
				selectedGidsForAdvance[listIdentifier][gid] = null;
			}
		
	});
}
function isCurrentTabIdentifierAdvanced(){
	'use strict';
    if($('#create-nursery-tab-headers .tabdrop').hasClass('active')){
  	  //means the active is in the tab drop
  	  return $('#create-nursery-tab-headers .tabdrop li.active').hasClass('crosses-list');
    }else{
  	  return $('#create-nursery-tab-headers li.active').hasClass('crosses-list');
    }    	
}
function getCurrentAdvanceTabTempIdentifier(){
	var listDivIdentifier  = '';
    if($('#create-nursery-tab-headers .tabdrop').hasClass('active')){
  	  //means the active is in the tab drop
  	  listDivIdentifier  = $('#create-nursery-tab-headers .tabdrop li.active .fbk-close-tab').attr('id');
    }else{
  	  listDivIdentifier  = $('#create-nursery-tab-headers li.active .fbk-close-tab').attr('id');
    }
    
	return listDivIdentifier;
}
function getCurrentAdvanceTabListIdentifier(){
	'use strict';
	var sectionContainerDiv = 'advance-list'+getCurrentAdvanceTabTempIdentifier(),
		listIdentifier = $('#'+getJquerySafeId(sectionContainerDiv) + ' #listId').val();
	return listIdentifier;
}
function getSelectedInventoryGids(){
	'use strict';
	var ids = [],
		listDivIdentifier  = getCurrentAdvanceTabTempIdentifier(),
		sectionContainerDiv = 'advance-list'+listDivIdentifier;
	$('#'+sectionContainerDiv + ' .advancingListGid:checked').each(function(){
		ids.push($(this).data('gid'));
	});
	return ids;
}

function addLot(){
	'use strict';
	var gids = getSelectedInventoryGids();
	if(gids.length === 0){
		showErrorMessage('page-message', germplasmSelectError);
		moveToTopScreen();
		return;		
	}
	
	$.ajax({
		url: '/Fieldbook/SeedStoreManager/ajax/'+getCurrentAdvanceTabListIdentifier(),
		type: 'GET',
		cache: false,
	    success: function(data) {
	    	$('#addLotsModalDiv').html(data);
	    	$('#comments').val('');
	    	$('#amount').val('');
	    	$('#page-message-lots').html('');
	    	$('#addLotsModal').modal({ backdrop: 'static', keyboard: true });
	    	initializePossibleValuesComboInventory(inventoryLocationSuggestions, '#inventoryLocationIdAll', true, null);
	    	initializePossibleValuesComboInventory(inventoryFavoriteLocationSuggestions, '#inventoryLocationIdFavorite', false, null);
	  	  	initializePossibleValuesComboScale(scaleSuggestions, '#inventoryScaleId', false, null);
	  	    showCorrectLocationInventoryCombo();
	    }
	});

}

function initializePossibleValuesComboScale(possibleValues, name, isLocation, defaultValue) {
	'use strict';
	var possibleValuesObj = [],
		defaultJsonVal = null;

	$.each(possibleValues, function(index, value) {
		var jsonVal = null;
		if (value.id !== undefined) {
			jsonVal = { 'id' : value.id,
					  'text' : value.displayName
				};
		}

		possibleValuesObj.push(jsonVal);
		if(defaultValue != null && defaultValue != '' && 
				((defaultValue == value.key || defaultValue == value.locid || defaultValue == value.mid) ||
				 (defaultValue == value.name || defaultValue == value.lname || defaultValue == value.mname))){
			defaultJsonVal = jsonVal;
		}

	});

	possibleValuesObj = sortByKey(possibleValuesObj, 'text');


		$(name).select2({
			minimumResultsForSearch: possibleValuesObj.length == 0 ? -1 : 20,
			query: function (query) {	
		      var data = {results: possibleValuesObj};
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index){
		        return ($.fn.select2.defaults.matcher(query.term,item.text));

		      });
		        query.callback(data);
		    }
	    });
	
	
	if(defaultJsonVal != null){
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}
function initializePossibleValuesComboInventory(possibleValues, name, showAllLocation, defaultValue) {
	'use strict';
	var possibleValuesObj = [];
	var defaultJsonVal = null;
	
	$.each(possibleValues, function(index, value) {
		var jsonVal = null;
		if (value.id != undefined) {
			jsonVal = { 'id' : value.id,
					  'text' : value.name
				};
		} else if (value.locid != undefined){
			var locNameDisplay = value.lname;
			if(value.labbr != null && value.labbr != ''){
				locNameDisplay  += ' - ('+value.labbr+')';
			}
			jsonVal = { 'id' : value.locid,
					  'text' : locNameDisplay
				};
		}
		
		possibleValuesObj.push(jsonVal);  
		if(defaultValue != null && defaultValue != '' && 
				((defaultValue == value.key || defaultValue == value.locid) || 
				 (defaultValue == value.name || defaultValue == value.lname))){
			defaultJsonVal = jsonVal;
		}
		
	});
	
	possibleValuesObj = sortByKey(possibleValuesObj, 'text');

	if (showAllLocation) {
		$(name).select2({
			minimumResultsForSearch: possibleValuesObj.length == 0 ? -1 : 20,
			query: function (query) {	
		      var data = {results: possibleValuesObj};
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index) {
		        return ($.fn.select2.defaults.matcher(query.term,item.text));
		      
		      });		      
		      
		      query.callback(data);
		    }
	    }).on('change', function (){
	    	if($('#'+getJquerySafeId('inventoryLocationId')).length !== 0){
	    		$('#'+getJquerySafeId('inventoryLocationId')).val($('#'+getJquerySafeId('inventoryLocationIdAll')).select2(
	    			'data').id);
	    	}
	    });
	} else {
		$(name).select2({
			minimumResultsForSearch: possibleValuesObj.length == 0 ? -1 : 20,
			query: function (query) {					
		      var data = {results: possibleValuesObj};
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index) {
		        return ($.fn.select2.defaults.matcher(query.term,item.text));
		      
		      });
		      
		      query.callback(data);
		    }
	    }).on('change', function (){
	    	if($('#'+getJquerySafeId('inventoryLocationId')).length !== 0){
	    		$('#'+getJquerySafeId('inventoryLocationId')).val($('#'+getJquerySafeId('inventoryLocationIdFavorite')).select2('data').id);
	    	}
	    });
	}
	
	if(defaultJsonVal != null){
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}



function saveLots() {
	'use strict';
	var gids = getSelectedInventoryGids();
	$('#gidList').val(gids);
	if($('#showFavoriteLocationInventory').is(':checked')){
		if($('#'+getJquerySafeId('inventoryLocationIdFavorite')).select2('data') !== null){
			$('#inventoryLocationId').val($('#'+getJquerySafeId('inventoryLocationIdFavorite')).select2('data').id);
		}
	}else{
		if($('#'+getJquerySafeId('inventoryLocationIdAll')).select2('data') !== null){
			$('#inventoryLocationId').val($('#'+getJquerySafeId('inventoryLocationIdAll')).select2('data').id);
		}
	}
	if ($('#inventoryLocationId').val() === '0' || $('#inventoryLocationId').val() === '') {
		showInvalidInputMessage(locationRequired);
		moveToTopScreen();
	} else if (!$('#inventoryScaleId').select2('data')) {
		showInvalidInputMessage(scaleRequired);
		moveToTopScreen();
	} else if ($('#amount').val() === '') {
		showInvalidInputMessage(inventoryAmountRequired);
		moveToTopScreen();
	} else if(isFloatNumber($('#amount').val()) === false || parseFloat($('#amount').val()) < 0) {
		showInvalidInputMessage(inventoryAmountPositiveRequired);
		moveToTopScreen();
	}  else if($('#inventoryComments').val().length > 250) {
		showInvalidInputMessage(commentLimitError);
		moveToTopScreen();
	} else {
		var serializedData = $('#add-plot-form').serialize();
		
		$.ajax({
			url: '/Fieldbook/SeedStoreManager/save/lots',
			type: 'POST',
			data: serializedData,
		    success: function(data) {
		    	if (data.success === 1) {
			    	showSuccessfulMessage('page-message', data.message);
			    	$('#addLotsModal').modal('hide');
			    	if(!isCurrentTabIdentifierAdvanced()){
			    		displayAdvanceGermplasmDetails(getCurrentAdvanceTabListIdentifier());	
			    	}else{
			    		ImportCrosses.displayCrossesGermplasmDetails(getCurrentAdvanceTabListIdentifier());
			    	}
			    	
		    	} else {
		    		showErrorMessage('page-message-lots', data.message);
		    	}
		    }
		});
	}
}

function displayAdvanceGermplasmDetails(listId) {
	'use strict';
	$.ajax({
		url: "/Fieldbook/germplasm/list/advance/" + listId,
		type: "GET",
		cache: false,
		success: function(html) {
			$('#advance-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
		}
	});
}

function showCorrectLocationInventoryCombo() {
	'use strict';
	var isChecked = $('#showFavoriteLocationInventory').is(':checked');
	//if show favorite location is checked, hide all field locations, else, show only favorite locations
	if(isChecked){
		$('#s2id_inventoryLocationIdFavorite').show();
		$('#s2id_inventoryLocationIdAll').hide();
		if($('#'+getJquerySafeId('inventoryLocationIdFavorite')).select2('data') != null){
			$('#'+getJquerySafeId('inventoryLocationId')).val($('#'+getJquerySafeId('inventoryLocationIdFavorite')).select2('data').id);
			
		}else{
			$('#'+getJquerySafeId('inventoryLocationId')).val(0);
		}
	}else{
		$('#s2id_inventoryLocationIdFavorite').hide();
		$('#s2id_inventoryLocationIdAll').show();
		if($('#'+getJquerySafeId('inventoryLocationIdAll')).select2('data') != null){
			$('#'+getJquerySafeId('inventoryLocationId')).val($('#'+getJquerySafeId('inventoryLocationIdAll')).select2('data').id);
		
		}else{
			$('#'+getJquerySafeId('inventoryLocationId')).val(0);
		
		}
		
	}
}