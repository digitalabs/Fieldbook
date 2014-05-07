function triggerInventoryTableSelection(tableName){
	$('#'+tableName+' tr.primaryRow').on('click', function() {	
		//$('#'+tableName).find("*").removeClass('field-map-highlight');
		
			$(this).toggleClass('field-map-highlight');
			var gid = $(this).data('gid') + "";
			if($(this).hasClass('field-map-highlight')){				
				selectedGids[gid] = gid;
				
			}else{
				selectedGids[gid] = null;
			}
		
	});
}

function getSelectedInventoryGids(){
	var ids = [];
	for(var gid in selectedGids) {
		//console.log( index + " : " + selectedTableIds[index]);
		var idVal = selectedGids[gid];
		if(idVal != null){
			ids.push(idVal);
		}			
	}
	return ids;
}

function addLot(){
	var gids = getSelectedInventoryGids();
	if(gids.length == 0){
		showErrorMessage('page-message', germplasmSelectError);
		return;		
	}
	$("#locationId").select2("data", null);
	$("#scaleId").select2("data", null);
	$("#comments").val("");
	$("#addLotsModal").modal("show");
}

function initializePossibleValuesCombo(possibleValues, name, showAllLocation, defaultValue) {
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
	    });
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
	    });
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