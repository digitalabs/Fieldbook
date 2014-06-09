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
function getCurrentAdvanceTabListIdentifier(){
	'use strict';
	var listDivIdentifier  = $('#create-nursery-tab-headers li.active .fbk-close-tab').attr('id'),
		sectionContainerDiv = 'advance-list'+listDivIdentifier,
		listIdentifier = $('#'+getJquerySafeId(sectionContainerDiv) + ' #listId').val();
	return listIdentifier;
}
function getSelectedInventoryGids(){
	'use strict';
	var ids = [],
		listDivIdentifier  = $('#create-nursery-tab-headers li.active .fbk-close-tab').attr('id'),
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
	
	Spinner.toggle();
	$.ajax({
		url: '/Fieldbook/SeedStoreManager/ajax',
		type: 'GET',
		cache: false,
	    success: function(data) {
	    	$('#addLotsModalDiv').html(data);
	    	$('#locationId').select2('data', null);
	    	$('#scaleId').select2('data', null);
	    	$('#comments').val('');
	    	$('#amount').val('');
	    	$('#page-message-lots').html('');
	    	$('#addLotsModal').modal({ backdrop: 'static', keyboard: true });
	    	initializePossibleValuesComboInventory(inventoryLocationSuggestions, '#inventoryMethodIdAll', true, null);
	    	initializePossibleValuesComboInventory(inventoryFavoriteLocationSuggestions, '#inventoryMethodIdFavorite', false, null);
	  	  	initializePossibleValuesComboScale(scaleSuggestions, '#inventoryScaleId', false, null);
	  	    showCorrectLocationInventoryCombo();
	    	Spinner.toggle();
	    }
	});
	
}
function sortByKey(array, key) {
	'use strict';
    return array.sort(function(a, b) {
        var x = a[key].toLowerCase(); var y = b[key].toLowerCase();
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
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
					  'text' : value.name
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
			jsonVal = { 'id' : value.locid,
					  'text' : value.lname
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
			minimumInputLength: 2,
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
	    		$('#'+getJquerySafeId('inventoryLocationId')).val($('#'+getJquerySafeId('inventoryMethodIdAll')).select2(
	    			'data').id);
	    	}
	    });
	} else {
		$(name).select2({
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
	    		$('#'+getJquerySafeId('inventoryLocationId')).val($('#'+getJquerySafeId('inventoryMethodIdFavorite')).select2('data').id);
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
	
	if ($('#inventoryLocationId').val() === '0') {
		showErrorMessage('page-message-lots', locationRequired);
		moveToTopScreen();
	} else if (!$('#inventoryScaleId').select2('data')) {
		showErrorMessage('page-message-lots', scaleRequired);
		moveToTopScreen();
	} else if ($('#amount').val() === '') {
		showErrorMessage('page-message-lots', inventoryAmountRequired);
		moveToTopScreen();
	} else if(isFloatNumber($('#amount').val()) === false || parseFloat($('#amount').val()) < 0) {
		showErrorMessage('page-message-lots', inventoryAmountPositiveRequired);
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
			    	displayAdvanceGermplasmDetails(getCurrentAdvanceTabListIdentifier());
		    	} else {
		    		showErrorMessage('page-message-lots', data.message);
		    	}
		    }
		});
	}
}

function displayAdvanceGermplasmDetails(listId) {
	'use strict';
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/SeedStoreManager/advance/displayGermplasmDetails/" + listId,
		type: "GET",
		cache: false,
		success: function(html) {
			$('#inventory-germplasm-list').html(html);
			Spinner.toggle();
		}
	});
}

function showCorrectLocationInventoryCombo() {
	'use strict';
	var isChecked = $('#showFavoriteLocationInventory').is(':checked');
	//if show favorite location is checked, hide all field locations, else, show only favorite locations
	if(isChecked){
		$('#s2id_inventoryMethodIdFavorite').show();
		$('#s2id_inventoryMethodIdAll').hide();
		if($('#'+getJquerySafeId('inventoryMethodIdFavorite')).select2('data') != null){
			$('#'+getJquerySafeId('inventoryLocationId')).val($('#'+getJquerySafeId('inventoryMethodIdFavorite')).select2('data').id);
			
		}else{
			$('#'+getJquerySafeId('inventoryLocationId')).val(0);
		}
	}else{
		$('#s2id_inventoryMethodIdFavorite').hide();
		$('#s2id_inventoryMethodIdAll').show();
		if($('#'+getJquerySafeId('inventoryMethodIdAll')).select2('data') != null){
			$('#'+getJquerySafeId('inventoryLocationId')).val($('#'+getJquerySafeId('inventoryMethodIdAll')).select2('data').id);
		
		}else{
			$('#'+getJquerySafeId('inventoryLocationId')).val(0);
		
		}
		
	}
}