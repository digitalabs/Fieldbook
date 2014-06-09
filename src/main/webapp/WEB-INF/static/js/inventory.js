function triggerInventoryTableSelection(tableName, sectionContainerDiv, listIdentifier){
	$('#' + sectionContainerDiv + ' #'+tableName+' tr.primaryRow').on('click', function() {	
		//$('#'+tableName).find("*").removeClass('field-map-highlight');
		
			$(this).toggleClass('field-map-highlight');
			var gid = $(this).data('gid') + "";
			if(selectedGidsForAdvance[listIdentifier] == null)
				selectedGidsForAdvance[listIdentifier] = new Array();
			
			if($(this).hasClass('field-map-highlight')){				
				//selectedGids[gid] = gid;
				selectedGidsForAdvance[listIdentifier][gid] = gid;
			}else{
				//selectedGids[gid] = null;
				//selectedGidsForAdvance
				selectedGidsForAdvance[listIdentifier][gid] = null;
			}
		
	});
}
function getCurrentAdvanceTabListIdentifier(){
	var listDivIdentifier  = $('#create-nursery-tab-headers li.active .fbk-close-tab').attr('id');
	var sectionContainerDiv = 'advance-list'+listDivIdentifier;
	var listIdentifier = $('#'+getJquerySafeId(sectionContainerDiv) + ' #listId').val();
	return listIdentifier;
}
function getSelectedInventoryGids(){
	
	var ids = [];
	var listDivIdentifier  = $('#create-nursery-tab-headers li.active .fbk-close-tab').attr('id');
	var sectionContainerDiv = 'advance-list'+listDivIdentifier;
	$('#'+sectionContainerDiv + " .advancingListGid:checked").each(function(){
		ids.push($(this).data('gid'));
	});
	return ids;
}

function addLot(){
	var gids = getSelectedInventoryGids();
	if(gids.length == 0){
		showErrorMessage('page-message', germplasmSelectError);
		return;		
	}
	
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/SeedStoreManager/ajax",
		type: "GET",
	    success: function(data) {
	    	$('#addLotsModalDiv').html(data);
	    	$("#locationId").select2("data", null);
	    	$("#scaleId").select2("data", null);
	    	$("#comments").val("");
	    	$("#page-message-lots").html("");
	    	$("#addLotsModal").modal({ backdrop: 'static', keyboard: true });
	    	initializePossibleValuesComboInventory(inventoryLocationSuggestions, "#inventoryMethodIdAll", true, null);
	    	initializePossibleValuesComboInventory(inventoryFavoriteLocationSuggestions, "#inventoryMethodIdFavorite", false, null);
	  	  	initializePossibleValuesComboScale(scaleSuggestions, "#inventoryScaleId", false, null);
	  	    showCorrectLocationInventoryCombo();
	    	Spinner.toggle();
	    }
	});
	
	
	
}
function initializePossibleValuesComboScale(possibleValues, name, isLocation, defaultValue) {
	var possibleValues_obj = [];
	var defaultJsonVal = null;
	
	$.each(possibleValues, function(index, value) {
		var jsonVal;
		if (value.id != undefined) {
			jsonVal = { 'id' : value.id,
					  'text' : value.name
				};
		}
		
		possibleValues_obj.push(jsonVal);  
		if(defaultValue != null && defaultValue != '' && 
				((defaultValue == value.key || defaultValue == value.locid || defaultValue == value.mid) || 
				 (defaultValue == value.name || defaultValue == value.lname || defaultValue == value.mname))){
			defaultJsonVal = jsonVal;
		}
		
	});
	
	possibleValues_obj = sortByKey(possibleValues_obj, "text");

	
		$(name).select2({
			query: function (query) {	
		      var data = {results: possibleValues_obj}, i, j, s;
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index) {
		        return ($.fn.select2.defaults.matcher(query.term,item.text));
		      
		      });
		      /*
		      if (data.results.length === 0){
		    	  data.results.unshift({id:query.term,text:query.term});	        	 
		      }
		      */
		        query.callback(data);
		    }
	    });
	
	
	if(defaultJsonVal != null){
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}
function initializePossibleValuesComboInventory(possibleValues, name, showAllLocation, defaultValue) {
	var possibleValues_obj = [];
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
		
		possibleValues_obj.push(jsonVal);  
		if(defaultValue != null && defaultValue != '' && 
				((defaultValue == value.key || defaultValue == value.locid) || 
				 (defaultValue == value.name || defaultValue == value.lname))){
			defaultJsonVal = jsonVal;
		}
		
	});
	
	possibleValues_obj = sortByKey(possibleValues_obj, "text");

	if (showAllLocation) {
		$(name).select2({
			minimumInputLength: 2,
			query: function (query) {	
		      var data = {results: possibleValues_obj}, i, j, s;
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index) {
		        return ($.fn.select2.defaults.matcher(query.term,item.text));
		      
		      });		      
		      
		      query.callback(data);
		    }
	    }).on("change", function (){
	    	if($('#'+getJquerySafeId("inventoryLocationId")).length != 0){
	    		$('#'+getJquerySafeId("inventoryLocationId")).val($('#'+getJquerySafeId("inventoryMethodIdAll")).select2("data").id);
	    	}
	    });;
	} else {
		$(name).select2({
			query: function (query) {	
		      var data = {results: possibleValues_obj}, i, j, s;
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index) {
		        return ($.fn.select2.defaults.matcher(query.term,item.text));
		      
		      });
		      
		      query.callback(data);
		    }
	    }).on("change", function (){
	    	//$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdFavorite")).select2("data").id);
	    	if($('#'+getJquerySafeId("inventoryLocationId")).length != 0){
	    		$('#'+getJquerySafeId("inventoryLocationId")).val($('#'+getJquerySafeId("inventoryMethodIdFavorite")).select2("data").id);
	    	}
	    });;
	}
	
	if(defaultJsonVal != null){
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}

function sortByKey(array, key) {
    return array.sort(function(a, b) {
        var x = a[key].toLowerCase(); var y = b[key].toLowerCase();
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    });
}

function saveLots() {
	var gids = getSelectedInventoryGids();
	$("#gidList").val(gids);
	
	if (!$("#locationId").select2("data")) {
		showErrorMessage("page-message-lots", locationRequired);
	} else if (!$("#scaleId").select2("data")) {
		showErrorMessage("page-message-lots", scaleRequired);
	} else {
		var serializedData = $("#add-plot-form").serialize();
		
		$.ajax({
			url: "/Fieldbook/SeedStoreManager/save/lots",
			type: "POST",
			data: serializedData,
		    success: function(data) {
		    	if (data.success == 1) {
			    	showSuccessfulMessage("page-message", data.message);
			    	$("#addLotsModal").modal("hide");
			    	displayGermplasmDetails($("#listIdSelected").val());
		    	} else {
		    		showErrorMessage("page-message-lots", data.message);
		    	}
		    },
			error: function (jqXHR, textStatus, errorThrown) {
				showErrorMessage("page-message", errorThrown);
			}
		});
	}
}

function showCorrectLocationInventoryCombo() {
	var isChecked = $('#showFavoriteLocationInventory').is(':checked');
	//if show favorite location is checked, hide all field locations, else, show only favorite locations
	if(isChecked){
		$('#s2id_inventoryMethodIdFavorite').show();
		$('#s2id_inventoryMethodIdAll').hide();
		if($('#'+getJquerySafeId("inventoryMethodIdFavorite")).select2("data") != null){
			$('#'+getJquerySafeId("inventoryLocationId")).val($('#'+getJquerySafeId("inventoryMethodIdFavorite")).select2("data").id);
			
		}else{
			$('#'+getJquerySafeId("inventoryLocationId")).val(0);
		}
	}else{
		$('#s2id_inventoryMethodIdFavorite').hide();
		$('#s2id_inventoryMethodIdAll').show();
		if($('#'+getJquerySafeId("inventoryMethodIdAll")).select2("data") != null){
			$('#'+getJquerySafeId("inventoryLocationId")).val($('#'+getJquerySafeId("inventoryMethodIdAll")).select2("data").id);
		
		}else{
			$('#'+getJquerySafeId("inventoryLocationId")).val(0);
		
		}
		
	}
}