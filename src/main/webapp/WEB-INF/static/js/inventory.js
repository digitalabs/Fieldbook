function triggerInventoryTableSelection(tableName){
	$('#'+tableName+' tr.primaryRow').on('click', function() {	
		//$('#'+tableName).find("*").removeClass('field-map-highlight');
		
			$(this).toggleClass('field-map-highlight');
			var index = $(this).data('index') + "";
			if($(this).hasClass('field-map-highlight')){				
				selectedIndexIds[index] = index;
				
			}else{
				selectedIndexIds[index] = null;
			}
		
	});
}

function getSelectedInventoryIndexes(){
	var ids = [];
	for(var index in selectedIndexIds) {
		//console.log( index + " : " + selectedTableIds[index]);
		var idVal = selectedIndexIds[index];
		if(idVal != null){
			ids.push(idVal);
		}			
	}
	return ids;
}