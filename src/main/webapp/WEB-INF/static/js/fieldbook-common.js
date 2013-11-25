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
		$("#fieldmapStudyId").val(id);
		
//		console.log(fieldMapHref+id);
		if (tableName == 'trial-table') {
			checkTrialOptions(id);
		}
		else {
			location.href=fieldMapHref+"/"+id;
		}
//		$('#fieldmap-url').attr("href", fieldMapHref+id);
//		$('#fieldmap-url').trigger('click');
	}else{
		var type = 'Trial';
		if(tableName == 'nursery-table')
			type='Nursery';
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>Please choose a "+type+"</div>");
	}
}

function checkTrialOptions(id){
	Spinner.toggle();
	$.ajax({ 
		url: "/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/" + id,
	    type: "GET",
	    data: "",
	    success: function(data) {
//        	$("#manageOntologyModal"+" .modal-content").empty().append(html);
//            $('#manageOntologyModal').modal('show');
//            $.fn.modal.Constructor.prototype.enforceFocus = function () {};
	    	
	    	if (data.nav == '0') {
	    		//show confirmation popup
	    		$('#manageTrialConfirmation').modal('show');
	    	}
	    	else if (data.nav == '1') {
	    		var fieldMapHref = $('#fieldmap-url').attr("href");	
	    		location.href = fieldMapHref + "/" + id;
	    	}
//	    	else if (data.nav == '3') {
//	    		location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap";
//	    	}
	    	
            Spinner.toggle();
        }
	});
}

function proceedToCreateFieldMap() {
	$('#manageTrialConfirmation').modal('hide');
	var fieldMapHref = $('#fieldmap-url').attr("href");	
	location.href = fieldMapHref + "/" + $("#fieldmapStudyId").val();
}

function getJquerySafeId(fieldId){

    //return fieldId.replace(".", "\\.")
    return replaceall(fieldId, ".", "\\.");
}

function replaceall(str,replace,with_this)
{
    var str_hasil ="";
    var temp;

    for(var i=0;i<str.length;i++) // not need to be equal. it causes the last change: undefined..
    {
        if (str[i] == replace)
        {
            temp = with_this;
        }
        else
        {
                temp = str[i];
        }

        str_hasil += temp;
    }

    return str_hasil;
}
function isInt(value) {
    if ((undefined === value) || (null === value) || (value === "")) {
    	
        return false;
    }
    return value % 1 == 0;
}

function selectTrialInstance(id) {
	$('#manageTrialConfirmation').modal('hide');
	Spinner.toggle();
	//var fieldMapHref = $('#fieldmap-url').attr("href");	
	$.ajax({ 
		url: "/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance/" + id,
	    type: "GET",
	    data: "",
	    success: function(data) {
	    	if (data.fieldMapInfo != null && data.fieldMapInfo != "") {
	    		createStudyTree($.parseJSON(data.fieldMapInfo), true);
	    		$('.tree').treegrid({
	                expanderExpandedClass: 'glyphicon glyphicon-minus',
	                expanderCollapsedClass: 'glyphicon glyphicon-plus'
	            });
	    		triggerFieldMapTableSelection('studyTree');
	    	}
            Spinner.toggle();
        }
	});
	$("#selectTrialInstanceModal").modal("toggle");
}

function createStudyTree(fieldMapInfo, hasFieldMap) {
	createRow(getPrefixName("study", fieldMapInfo.fieldbookId), "", fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, "");
	$.each(fieldMapInfo.datasets, function (index, value) {
		createRow(getPrefixName("dataset", value.datasetId), getPrefixName("study", fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, "");
		$.each(value.trialInstances, function (index, childValue) {
			if ((hasFieldMap && childValue.hasFieldMap) || !hasFieldMap) {
				createRow(getPrefixName("trialInstance", childValue.geolocationId), getPrefixName("dataset", value.datasetId), childValue, childValue.geolocationId, value.datasetId);
			}
		});
	});
	
	//set bootstrap ui
	$('.tree').treegrid({
        expanderExpandedClass: 'glyphicon glyphicon-minus',
        expanderCollapsedClass: 'glyphicon glyphicon-plus'
    });
	
	//set as highlightable
	triggerFieldMapTableSelection('studyTree');
}

function getPrefixName(cat, id) {
	if (parseInt(id) > 0) {
		return cat + id;
	} else {
		return cat + "n" + (parseInt(id)*-1);
	}
}

function createRow(id, parentClass, value, realId, parentId) {
	var genClassName = "treegrid-";
	var genParentClassName = "";
	var newRow = "";
	var newCell = "";	
	if (parentClass != "") {
		genParentClassName = "treegrid-parent-" + parentClass;
	}
	
	if (id.indexOf("study") > -1 || id.indexOf("dataset") > -1) {
		newRow = "<tr id='" + realId + "' class='"+ genClassName + id + " " + genParentClassName + "'>";
		if (trial) {
			newCell = "<td>" + value + "</td><td></td><td></td><td></td>";
		} else {
			newCell = "<td>" + value + "</td><td></td>";
		}
	} else {
		newRow = "<tr id='" + realId + "|" + parentId + "' class='data-row "+ genClassName + id + " " + genParentClassName + "'>";
		if (trial) {
			newCell = "<td>" + value.siteName + "</td><td>" 
					+ value.entryCount + "</td><td>" 
					+ value.repCount + "</td><td>" 
					+ value.plotCount + "</td>";
		} else {
			newCell = "<td>" + value.siteName + "</td><td>" 
			+ value.entryCount + "</td>"; 
		}
	}
	$("#studyTree").append(newRow+newCell+"</tr>");
}
