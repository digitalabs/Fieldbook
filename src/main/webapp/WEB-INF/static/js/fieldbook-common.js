function showPage(paginationUrl, pageNum, sectionDiv){
	//$('#imported-germplasm-list').html(pageNum); 	
	Spinner.toggle();
 	$.ajax(
         { url: paginationUrl+pageNum,
           type: "GET",
           data: "",
           success: function(html) {
        	   
             $("#"+sectionDiv).empty().append(html);
             Spinner.toggle();  
           }
         }
       );
}

function triggerFieldMapTableSelection(tableName){
	$('#'+tableName+' tr.data-row').on('click', function() {	
		$('#'+tableName).find("*").removeClass('field-map-highlight');
		$(this).addClass('field-map-highlight');
	});
}

function createFieldMap(tableName){
	var fieldMapHref = $('#fieldmap-url').attr("href");	
	if($('#'+tableName+' .field-map-highlight').attr('id') != null){
		var id = $('#'+tableName+' .field-map-highlight').attr('id');
		//console.log(fieldMapHref+id);
		location.href=fieldMapHref+id;
//		$('#fieldmap-url').attr("href", fieldMapHref+id);
//		$('#fieldmap-url').trigger('click');
	}else{
		var type = 'Trial';
		if(tableName == 'nursery-table')
			type='Nursery';
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>Please choose a "+type+"</div>");
	}
	
	
}