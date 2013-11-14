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
	
	if($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val() == "" || !isInt($('#'+getJquerySafeId('userFieldmap.numberOfRowsInBlock')).val())){
		showEnterFieldDetailsMessage(msgRowsInBlock);
		return false;
	}
	if($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val() == "" || !isInt($('#'+getJquerySafeId('userFieldmap.numberOfRangesInBlock')).val())){
		showEnterFieldDetailsMessage(msgRangesInBlock);
		return false;
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
	
	    });
	
}