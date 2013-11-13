function validateEnterFieldPage(){
	if($('#'+getJquerySafeId('userFieldmap.fieldLocationId')).val() == "0"){
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