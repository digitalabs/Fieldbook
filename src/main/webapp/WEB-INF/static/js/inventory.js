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
	
	alert('Show chikkas popup');
}