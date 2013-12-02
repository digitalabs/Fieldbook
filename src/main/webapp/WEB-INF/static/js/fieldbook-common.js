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
		//$('#'+tableName).find("*").removeClass('field-map-highlight');
		if (tableName == "studyTree") {
			$(this).toggleClass("trialInstance");
			$(this).toggleClass('field-map-highlight');
		} else {
			$(this).toggleClass('field-map-highlight');
		}
	});
}

function createFieldMap(tableName){
	if($('#'+tableName+' .field-map-highlight').attr('id') != null){
		var ids = [];
		$('#'+tableName+' .field-map-highlight').each(function(){ ids.push(this.id); });
		var idList = ids.join(",");
		$('#page-message').html("");
		showFieldMapPopUpCreate(tableName, idList);
	} else {
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+fieldMapStudyRequired+"</div>");
	}
}

function checkTrialOptions(id){
	Spinner.toggle();
	$.ajax({ 
		url: "/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/" + id,
	    type: "GET",
	    data: "",
	    success: function(data) {
	    	if (data.nav == '0') {
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

function createNurseryFieldmap(id) {
	Spinner.toggle();
	$.ajax({ 
		url: "/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/" + id,
	    type: "GET",
	    data: "",
	    success: function(data) {
	    	if (data.nav == '0') {
	    		$('#manageTrialConfirmation').modal('show');
	    		$("#fieldmapDatasetId").val(data.datasetId);
	    		$("#fieldmapGeolocationId").val(data.geolocationId);
	    	}
	    	else if (data.nav == '1') {
	    		var fieldMapHref = $('#fieldmap-url').attr("href");	
	    		location.href = fieldMapHref + "/" + id;
	    	}
            Spinner.toggle();
        }
	});
}

function proceedToCreateFieldMap() {
	$('#manageTrialConfirmation').modal('hide');
	var fieldMapHref = $('#fieldmap-url').attr("href");	
	location.href = fieldMapHref + "/" + $("#fieldmapStudyId").val();
}

function proceedToGenerateFieldMap() {
	$('#manageTrialConfirmation').modal('hide');
	location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/" 
		+ $("#fieldmapDatasetId").val() + "/" + $("#fieldmapGeolocationId").val();
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

function selectTrialInstance(tableName) {
	if (tableName == "trial-table") { 
		Spinner.toggle();
		//var fieldMapHref = $('#fieldmap-url').attr("href");	
		$.ajax({ 
			url: "/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance",
		    type: "GET",
		    data: "",
		    success: function(data) {
		    	if (data.fieldMapInfo != null && data.fieldMapInfo != "") {
		    		if (parseInt(data.size) > 1) {
			    		clearStudyTree();
			    		createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap);
			    		$("#selectTrialInstanceModal").modal("toggle");
		    		} else {
		    			//redirect to step 3
		    			var fieldMapInfo = $.parseJSON(data.fieldMapInfo);
		    			var datasetId = fieldMapInfo.datasets[0].datasetId;
		    			var geolocationId = fieldMapInfo.datasets[0].trialInstances[0].geolocationId;	    			
		    			location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/" + datasetId + "/" + geolocationId;
		    		}
		    	}
	            Spinner.toggle();
	        }
		});
	} else {
		//redirect to step 3 for nursery
		var datasetId = $("#fieldmapDatasetId").val();
		var geolocationId = $("#fieldmapGeolocationId").val();
		location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/" + datasetId + "/" + geolocationId;
	}
}

function selectTrialInstanceCreate() { 	
	$.ajax({ 
		url: "/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance",
	    type: "GET",
	    data: "",
	    success: function(data) {
	    	if (data.fieldMapInfo != null && data.fieldMapInfo != "") {
	    		clearStudyTree();
	    		isViewFieldmap = false;
	    		createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap);
	    		$("#selectTrialInstanceModal").modal("toggle");	    		
	    	}
        }
	});
}

function createStudyTree(fieldMapInfoList, hasFieldMap) {
	createHeader(hasFieldMap);
	$.each(fieldMapInfoList, function (index, fieldMapInfo) {
		createRow(getPrefixName("study", fieldMapInfo.fieldbookId), "", fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, hasFieldMap);
		$.each(fieldMapInfo.datasets, function (index, value) {
			createRow(getPrefixName("dataset", value.datasetId), getPrefixName("study", fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, hasFieldMap);
			$.each(value.trialInstances, function (index, childValue) {
				if ((hasFieldMap && childValue.hasFieldMap) || !hasFieldMap) {
					createRow(getPrefixName("trialInstance", childValue.geolocationId), getPrefixName("dataset", value.datasetId), childValue, childValue.geolocationId, hasFieldMap);
				}
			});
		});
	});
	//set bootstrap ui
	$('.tree').treegrid({
        expanderExpandedClass: 'glyphicon glyphicon-minus ',
        expanderCollapsedClass: 'glyphicon glyphicon-plus'
    });
	
	//set as highlightable
	if (hasFieldMap) {
		triggerFieldMapTableSelection('studyTree');
	}
}

function getPrefixName(cat, id) {
	if (parseInt(id) > 0) {
		return cat + id;
	} else {
		return cat + "n" + (parseInt(id)*-1);
	}
}

function triggerExpanderClick(row) {
	if (row.treegrid('isExpanded')) {
		row.treegrid('collapse');
	} else {
		row.treegrid('expand');
	}
}

function createHeader(hasFieldMap) {
	var newRow = "<thead><tr>";
	if (trial) {
		newRow = newRow + "<th style='width:30%'></th>" +
			"<th style='width:20%'>" + entryLabel + "</th>" +
			"<th style='width:20%'>" + repLabel + "</th>" +
			"<th style='width:20%'>" + plotLabel + "</th>";
	} else {
		newRow = newRow + "<th style='width:50%'></th>" +
		"<th style='width:40%'>" + entryPlotLabel + "</th>";
	}
	if (!hasFieldMap) {
		newRow = newRow + "<th style='width:10%'>" + fieldmapLabel + "</th>";
	}
	newRow = newRow + "</tr></thead>";
	$("#studyTree").append(newRow+"<tbody></tbody>");
}

function createRow(id, parentClass, value, realId, withFieldMap) {
	var genClassName = "treegrid-";
	var genParentClassName = "";
	var newRow = "";
	var newCell = "";	
	if (parentClass != "") {
		genParentClassName = "treegrid-parent-" + parentClass;
	}
	
	if (id.indexOf("study") > -1 || id.indexOf("dataset") > -1) {
		//study and dataset level 
		newRow = "<tr id='" + realId + "' class='"+ genClassName + id + " " + genParentClassName + "' onClick='triggerExpanderClick($(this))'>";
		if (trial) {
			newCell = "<td>" + value + "</td><td></td><td></td><td></td>";
		} else {
			newCell = "<td>" + value + "</td><td></td>";
		}
		if (!withFieldMap) {
			newCell = newCell + "<td></td>";
		}
	} else {
		//trial instance level
		if (withFieldMap) {
			//for view fieldmap
			newRow = "<tr id='" + realId + "' class='data-row trialInstance "+ genClassName + id + " " + genParentClassName + "'>";
			newCell = "<td>" + value.siteName + "</td><td>" + value.entryCount + "</td>"; 
			if (trial) {
				newCell = newCell + "<td>" + value.repCount + "</td><td>" + value.plotCount + "</td>";
			}
		} else {
			//for create new fieldmap
			newRow = "<tr class='data-row trialInstance "+ genClassName + id + " " + genParentClassName + "'>";
			var checkBox = "<input class='checkInstance' type='checkbox' id='" + realId + "' /> &nbsp;&nbsp;";
			newCell = "<td>" + checkBox + value.siteName + "</td><td>" + value.entryCount + "</td>";
			if (trial) {
				newCell = newCell + "<td>" + value.repCount + "</td><td>" + value.plotCount + "</td>";
			}
			var hasFieldMap = value.hasFieldMap ? "Yes" : "No";
			newCell = newCell + "<td class='hasFieldMap'>" + hasFieldMap + "</td>";
		}
	}
	$("#studyTree").append(newRow+newCell+"</tr>");
}

function clearStudyTree() {
	$("#studyTree").empty();
}

function showMessage(message) {
	$("#page-message").html(
		    "<div class='alert alert-danger'>"+ message +"</div>"
	);
}

function createLabelPrinting(tableName){	
	
	if($('.field-map-highlight').length != 1){
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+createLabelErrorMsg+"</div>");
		return;
	}
	
	if($('#'+tableName+' .field-map-highlight').attr('id') != null){
		var labelPrintingHref = $('#label-printing-url').attr("href");
		var id = $('#'+tableName+' .field-map-highlight').attr('id');
		Spinner.toggle();
		location.href = labelPrintingHref + "/" + id;
	    Spinner.toggle();
	}else{
		var type = 'Trial';
		if(tableName == 'nursery-table')
			type='Nursery';
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+createLabelErrorMsg+"</div>");
	}
}

function showFieldMap(tableName) {
	if($('#'+tableName+' .field-map-highlight').attr('id') != null){
		if ($('#'+tableName+' .field-map-highlight').size() > 1) {
			$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+fieldMapOneStudyErrorMsg+"</div>");
		} else {
			$("#page-message").html("");
			showFieldMapPopUp(tableName, $('#'+tableName+' .field-map-highlight').attr('id'));
		}
	} else {
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+fieldMapStudyRequired+"</div>");
	}
}

function showFieldMapPopUpCreate(tableName, ids) {
	var link = "";
	if (tableName == "trial-table") {
		link = "/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/";
		trial = true;
	} else {
		link = "/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/";
		trial = false;
	}
	Spinner.toggle();
	$.ajax({ 
		url: link + encodeURIComponent(ids),
	    type: "GET",
	    data: "",
	    success: function(data) {
    		selectTrialInstanceCreate();
        },
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus , errorThrown);
	    }, 
	    complete: function(){ 
		   Spinner.toggle();
	    } 
	});
}

function showFieldMapPopUp(tableName, id) {
	var link = "";
	if (tableName == "trial-table") {
		link = "/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/";
	} else {
		link = "/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/";
	}
	Spinner.toggle();
	$.ajax({ 
		url: link + id,
	    type: "GET",
	    data: "",
	    success: function(data) {
	    	if (data.nav == '0') {
	    		if (tableName == "nursery-table") {
		    		$("#fieldmapDatasetId").val(data.datasetId);
		    		$("#fieldmapGeolocationId").val(data.geolocationId);
	    		}
	    		selectTrialInstance(tableName);
	    	}
	    	else if (data.nav == '1') {
	    		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+noFieldMapExists+"</div>");
	    	}
            Spinner.toggle();
        }
	});
}