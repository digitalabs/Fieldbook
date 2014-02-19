$(function() {

    var newHash      = "";
                
    $(window).bind('hashchange', function(){
    
        newHash = window.location.hash.substring(1);
        
        if (newHash) {
        	
           //console.log('newHash'+newHash);
           Spinner.toggle();	
	   		$.ajax({
	   			url: newHash,
	   			type: "GET", 					
	   			success: function (html) {
	   				//we just paste the whole html
	   				$('.container .row').first().html(html);				
	   				Spinner.toggle();
	   			}
	   		});
        };
        
    });
    
    $(window).trigger('hashchange');

    
});

function doAjaxMainSubmit(pageMessageDivId, successMessage, overrideAction){
	Spinner.toggle();
	var form = $("form");
	var action = form.attr('action');
	var serializedData = form.serialize();
	
	if(overrideAction != null)
		action = overrideAction;
	
	$.ajax({
		url: action,
		type: "POST", 	
		data: serializedData,
		success: function (html) {
			//we just paste the whole html
			$('.container .row').first().html(html);
			
		    showSuccessfulMessage(pageMessageDivId, successMessage);				
			Spinner.toggle();
		}
	}); 							
}

function showPage(paginationUrl, pageNum, sectionDiv){
	//$('#imported-germplasm-list').html(pageNum); 	
	Spinner.toggle();
 	$.ajax(
         { url: paginationUrl+pageNum,
           type: "GET",
           data: "",
           cache: false,
           success: function(html) {
        	   
             $("#"+sectionDiv).empty().append(html);
             
             if(sectionDiv == 'trial-details-list' || sectionDiv == 'nursery-details-list'){
            	 //we highlight the previously clicked
            	 for(var index in selectedTableIds) {
         			//console.log( index + " : " + selectedTableIds[index]);
         			var idVal = selectedTableIds[index];
         			if(idVal != null){
         				//we need to highlight
         				$('tr.data-row#'+idVal).addClass('field-map-highlight');
         			}			
         		 }
             }
             
             Spinner.toggle();  
           }
         }
       );
}

function showPostPage(paginationUrl,previewPageNum, pageNum, sectionDiv, formName){
	//$('#imported-germplasm-list').html(pageNum); 	
	var $form = $("#"+formName);
	
	var serializedData = $form.serialize();

	Spinner.toggle();
 	$.ajax(
         { url: paginationUrl+pageNum+"/"+previewPageNum+'?r=' + (Math.random() * 999),
           type: "POST",
           data: serializedData,
           cache: false,
           timeout: 70000,
           success: function(html) {
        	   
             $("#"+sectionDiv).empty().append(html);
             
             if(sectionDiv == 'trial-details-list' || sectionDiv == 'nursery-details-list'){
            	 //we highlight the previously clicked
            	 for(var index in selectedTableIds) {
         			//console.log( index + " : " + selectedTableIds[index]);
         			var idVal = selectedTableIds[index];
         			if(idVal != null){
         				//we need to highlight
         				$('tr.data-row#'+idVal).addClass('field-map-highlight');
         			}			
         		 }
             }
             
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
			var id = $(this).attr('id') + "";
			if($(this).hasClass('field-map-highlight')){				
				selectedTableIds[id] = id;
			}else{
				selectedTableIds[id] = null;
			}
			
			//console.log(selectedTableIds);
		}
	});
}

function createFieldMap(tableName){
	if($('#'+tableName+' .field-map-highlight').attr('id') != null){
		var ids = [];
		//$('#'+tableName+' .field-map-highlight').each(function(){ ids.push(this.id); });
		//get selected studies
		for(var index in selectedTableIds) {
			//console.log( index + " : " + selectedTableIds[index]);
			var idVal = selectedTableIds[index];
			if(idVal != null){
				ids.push(idVal);
			}			
		}
		var idList = ids.join(",");
		$('#page-message').html("");
		
		//show pop up to select instances/dataset for field map creation
		showFieldMapPopUpCreate(tableName, idList);
	} else {
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+fieldMapStudyRequired+"</div>");
	}
}

//obsolete
function checkTrialOptions(id){
	Spinner.toggle();
	$.ajax({ 
		url: "/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/" + id,
	    type: "GET",
	    data: "",
	    cache: false,
	    success: function(data) {
	    	if (data.nav == '0') {
	    		$('#manageTrialConfirmation').modal('show');
	    	}
	    	else if (data.nav == '1') {
	    		var fieldMapHref = $('#fieldmap-url').attr("href");	
	    		location.href = fieldMapHref + "/" + id;
	    	}
	    	
            Spinner.toggle();
        }
	});
}

//obsolete
function createNurseryFieldmap(id) {
	Spinner.toggle();
	$.ajax({ 
		url: "/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/" + id,
	    type: "GET",
	    data: "",
	    cache: false,
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
	location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/nursery/" 
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
		    cache: false,
		    data: "",
		    success: function(data) {
		    	if (data.fieldMapInfo != null && data.fieldMapInfo != "") {
		    		if (parseInt(data.size) > 1) {
		    			//show popup to select fieldmap to display
			    		clearStudyTree();
			    		isViewFieldmap = true;
			    		createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap, tableName);
			    		$("#selectTrialInstanceModal").modal("toggle");
		    		} else {
		    			//redirect to step 3
		    			var fieldMapInfo = $.parseJSON(data.fieldMapInfo);
		    			var datasetId = data.datasetId; //fieldMapInfo[0].datasets[0].datasetId;
		    			var geolocationId = data.geolocationId; //fieldMapInfo[0].datasets[0].trialInstances[0].geolocationId;	    			
		    			location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/" + datasetId + "/" + geolocationId;
		    		}
		    	}
	            Spinner.toggle();
	        }
		});
	} else {
		//redirect to step 3 for nursery
		var datasetId = $("#fieldmapDatasetId").val();
		var geolocationId = $("#fieldmapGeolocationId").val();
		location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/nursery/" + datasetId + "/" + geolocationId;
	}
}

function selectTrialInstanceCreate(tableName) {
	$.ajax({ 
		url: "/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance",
	    type: "GET",
	    async: false,
	    cache: false,
	    data: "",
	    success: function(data) {
	    	if (data.fieldMapInfo != null && data.fieldMapInfo != "") {
	    		//show popup to select instances to create field map
	    		clearStudyTree();
	    		isViewFieldmap = false;
	    		createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap, tableName);
	    		$("#selectTrialInstanceModal").modal("toggle");
	    	}
        } 
	});
}

function createStudyTree(fieldMapInfoList, hasFieldMap, tableName) { 
	createHeader(hasFieldMap);
	$.each(fieldMapInfoList, function (index, fieldMapInfo) {
		createRow(getPrefixName("study", fieldMapInfo.fieldbookId), "", fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, hasFieldMap);
		$.each(fieldMapInfo.datasets, function (index, value) {
			if (tableName == "trial-table") {
				//create trial study tree up to instance level
				createRow(getPrefixName("dataset", value.datasetId), getPrefixName("study", fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, hasFieldMap);
				$.each(value.trialInstances, function (index, childValue) {
					if ((hasFieldMap && childValue.hasFieldMap) || !hasFieldMap) {
						createRow(getPrefixName("trialInstance", childValue.geolocationId), getPrefixName("dataset", value.datasetId), childValue, childValue.geolocationId, hasFieldMap);
					}
				});
			} else {
				//if dataset has an instance, show up to the dataset level
				if (value.trialInstances.length > 0) {
					$.each(value.trialInstances, function (index, childValue) {
						createRowForNursery(getPrefixName("trialInstance", childValue.geolocationId), 
								getPrefixName("study", fieldMapInfo.fieldbookId), childValue, childValue.geolocationId, 
								hasFieldMap, value.datasetName, value.datasetId);
					});
				}
			}
		});
	});
	
	//set bootstrap ui
	$('.tree').treegrid();
	
	$('.tr-expander').on('click', function(){
		triggerExpanderClick($(this));
	});
	$('.treegrid-expander').on('click', function(){
		triggerExpanderClick($(this).parent().parent());
		
	});
	 
	
	//set as highlightable
	if (hasFieldMap) {
		triggerFieldMapTableSelection('studyTree');
		
	}
	styleDynamicTree('studyTree');
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
	
	if (!hasFieldMap) {
		if (trial) {
			newRow = newRow + "<th style='width:45%'>" + trialName+ "</th>" +
				"<th style='width:10%'>" + entryLabel + "</th>" +
				"<th style='width:10%'>" + repLabel + "</th>" +
				"<th style='width:20%'>" + plotLabel + "</th>";
		} else {
			newRow = newRow + "<th style='width:65%'>" + nurseryName + "</th>" +
			"<th style='width:20%'>" + entryPlotLabel + "</th>";
		}
		//remove has field map column for now GCp-7295
		//newRow = newRow + "<th style='width:15%'>" + fieldmapLabel + "</th>";
	} else {
		if (trial) {
			newRow = newRow + "<th style='width:40%'></th>" +
				"<th style='width:20%'>" + entryLabel + "</th>" +
				"<th style='width:20%'>" + repLabel + "</th>" +
				"<th style='width:20%'>" + plotLabel + "</th>";
		} else {
			newRow = newRow + "<th style='width:60%'></th>" +
			"<th style='width:40%'>" + entryPlotLabel + "</th>";
		}
	}
	newRow = newRow + "</tr></thead>";
	$("#studyTree").append(newRow+"<tbody></tbody>");
}

function createRowForNursery(id, parentClass, value, realId, withFieldMap, datasetName, datasetId) {
	var genClassName = "treegrid-";
	var genParentClassName = "";
	var newRow = "";
	var newCell = "";	
	if (parentClass != "") {
		genParentClassName = "treegrid-parent-" + parentClass;
	}
	
	//for create new fieldmap
	newRow = "<tr class='data-row trialInstance "+ genClassName + id + " " + genParentClassName + "'>";
	var checkBox = "<input class='checkInstance' type='checkbox' id='" + datasetId + "|" + realId + "' /> &nbsp;&nbsp;";
	newCell = "<td>" + checkBox + "&nbsp;" + datasetName + "</td><td>" + value.entryCount + "</td>";
	var hasFieldMap = value.hasFieldMap ? "Yes" : "No";
	//remve the has field map column for now GCP-7295
	//newCell = newCell + "<td class='hasFieldMap'>" + hasFieldMap + "</td>";
	$("#studyTree").append(newRow+newCell+"</tr>");
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
		//newRow = "<tr id='" + realId + "' class='"+ genClassName + id + " " + genParentClassName + "' onClick='triggerExpanderClick($(this))'>";
		newRow = "<tr id='" + realId + "' class='tr-expander "+ genClassName + id + " " + genParentClassName + "'>";
		
		if (trial) {
			newCell = newCell + "<td>" + value + "</td><td></td><td></td><td></td>";
		} else {
			newCell = newCell + "<td>" + value + "</td><td></td>";
		}
		if (!withFieldMap) {
			//remve the has field map column for now GCP-7295
			//newCell = newCell + "<td></td>";
			;
		}
	} else {
		//trial instance level
		if (withFieldMap) {
			//for view fieldmap
			newRow = "<tr id='" + realId + "' class='data-row trialInstance "+ genClassName + id + " " + genParentClassName + "'>";
			newCell = "<td>" + value.trialInstanceNo + "</td><td>" + value.entryCount + "</td>"; 
			if (trial) {
				newCell = newCell + "<td>" + value.repCount + "</td><td>" + value.plotCount + "</td>";
			}
		} else {
			//for create new fieldmap
			newRow = "<tr class='data-row trialInstance "+ genClassName + id + " " + genParentClassName + "'>";
			var checkBox = "<input class='checkInstance' type='checkbox' id='" + realId + "' /> &nbsp;&nbsp;";
			newCell = "<td>" + checkBox + "&nbsp;" + value.trialInstanceNo + "</td><td>" + value.entryCount + "</td>";
			if (trial) {
				newCell = newCell + "<td>" + value.repCount + "</td><td>" + value.plotCount + "</td>";
			}
			var hasFieldMap = value.hasFieldMap ? "Yes" : "No";
			//remve the has field map column for now GCP-7295
			//newCell = newCell + "<td class='hasFieldMap'>" + hasFieldMap + "</td>";
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
	
	var count = 0;
	var idVal = null;
	for(var index in selectedTableIds) {
		//console.log( index + " : " + selectedTableIds[index]);
		var tempVal = selectedTableIds[index];
		if(tempVal != null){
			idVal = tempVal;
			count++;
		}			
	}
	
	if(count != 1){
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+createLabelErrorMsg+"</div>");
		return;
	}
	
	if(idVal != null){
		var labelPrintingHref = $('#label-printing-url').attr("href");
		var id = idVal;
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
	var count = 0;
	var idVal = null;
	for(var index in selectedTableIds) {
		//console.log( index + " : " + selectedTableIds[index]);
		var tempVal = selectedTableIds[index];
		if(tempVal != null){
			idVal = tempVal;
			count++;
		}			
	}
	
	if(idVal != null){
		if (count > 1) {
			$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+fieldMapOneStudyErrorMsg+"</div>");
		} else {
			$("#page-message").html("");
			//showFieldMapPopUp(tableName, $('#'+tableName+' .field-map-highlight').attr('id'));
			showFieldMapPopUp(tableName, idVal);
		}
	} else {
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+fieldMapStudyRequired+"</div>");
	}
}

//show popup to select instances for field map creation
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
    		selectTrialInstanceCreate(tableName);
        },
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus , errorThrown);
	    }, 
	    complete: function(){ 
		   Spinner.toggle();
	    } 
	});
}

//show popup to select field map to display
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

function viewFieldMap() {
	if (isViewFieldmap) {
		showGeneratedFieldMap();
	} else {
		showCreateFieldMap();
	}
}

//redirect to step 3
function showGeneratedFieldMap() {
	if ($('#studyTree .field-map-highlight').attr('id')) {
		if ($('#studyTree .field-map-highlight').size() == 1) {
			$("#selectTrialInstanceModal").modal("toggle");
			var id = $('#studyTree .field-map-highlight').attr('id');
			var datasetId = $('#studyTree .field-map-highlight').treegrid('getParentNode').attr("id");
			location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/" + datasetId + "/" + id;
		} else {
			showMessage(multipleSelectError);
		}
	} else {
		showMessage(noSelectedTrialInstance);
	}
}

function showCreateFieldMap() {
	if ($('#studyTree .checkInstance:checked').attr('id')) {
		var selectedWithFieldMap = false;
		fieldmapIds = [];
		$('#studyTree .checkInstance:checked').each(function(){
			var id = this.id;
			var datasetId;
			var studyId;
			if (id.indexOf("|") > -1) {
				datasetId = id.split("|")[0];
				id = id.split("|")[1];
				studyId = $(this).parent().parent().treegrid('getParentNode').attr("id");
			} else {
				datasetId = $(this).parent().parent().treegrid('getParentNode').attr("id");
				studyId = $(this).parent().parent().treegrid('getParentNode').treegrid('getParentNode').attr("id");
			}
			var hasFieldMap;
			//get value hasfieldmap column
			/* commented for now the checking for has fieldmap GCp-7295
			if (trial) {
				hasFieldMap = $(this).parent().next().next().next().next().html();
			} else {
				hasFieldMap = $(this).parent().next().next().html();
			}
			*/
			//build id list of selected trials instances
			fieldmapIds.push(studyId+"|"+datasetId+"|"+id);
			/* commented for now the checking for has fieldmap GCp-7295
			if (hasFieldMap == "Yes") {
				selectedWithFieldMap = true;
			}*/
		});
		//this is to disable the 2nd popup
		if (false && selectedWithFieldMap) {
			$("#selectTrialInstanceModal button,input").attr("disabled", true);
			$("#confirmSubModal").modal("toggle");
		} else {
			//redirect to step 1
			redirectToFirstPage();
		}
	} else {
		//no trial instance is selected
		showMessage(noSelectedTrialInstance);
	}
}

function redirectToFirstPage() {
	location.href = $('#fieldmap-url').attr("href") + "/" + encodeURIComponent(fieldmapIds.join(","));
}

function setSelectedTrialsAsDraggable(){
	$("#selectedTrials").tableDnD();
	
	$("#selectedTrials").tableDnD({
        onDragClass: "myDragClass",
        onDrop: function(table, row) {
        	setSelectTrialOrderValues();
        }
    });
	
	setSelectTrialOrderValues();
	styleDynamicTree('selectedTrials');
}

function setSelectTrialOrderValues() {
	var i = 0;
	$("#selectedTrials .orderNo").each(function (){
		$(this).text(i+1);
		$(this).parent().parent().attr("id", i+1);
		i++;
	});
	styleDynamicTree('selectedTrials');
}

function styleDynamicTree(treeName){
	var count = 0;
	if($('#'+treeName) != null){
		$('#'+treeName+' tr').each(function(){
			count++;
			var className = "";
			if(count % 2 == 1){
				className = 'odd';
			}else{
				className = 'even';
			}
			$(this).find('td').removeClass('odd');
			$(this).find('td').removeClass('even');
			$(this).find('td').addClass(className);
			
			$(this).find('th').removeClass('odd');
			$(this).find('th').removeClass('even');
			$(this).find('th').addClass('table-header');
			
			
		});
		
	}
	
}

function openStudy(tableName){	
	
	var count = 0;
	for(var index in selectedTableIds) {
		//console.log( index + " : " + selectedTableIds[index]);
		var tempVal = selectedTableIds[index];
		if(tempVal != null){
			idVal = tempVal;
			count++;
		}			
	}
	
	if(count != 1){
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+openStudyError+"</div>");
		return;
	}
	
	Spinner.toggle();
	var openStudyHref = $('#open-study-url').attr("href");
	
	if (tableName == "nursery-table") {
		if(idVal != null){
			location.href = openStudyHref + "/" + idVal;
			Spinner.toggle();
		}
	} else {
		$.ajax(
	    { url: openStudyHref,
	       type: "GET",
	       data: "",
	       cache: false,
	       success: function() {    	            
	         Spinner.toggle();  
	       }
	     });
	}
}

function advanceNursery(tableName){
	var count = 0;
	for(var index in selectedTableIds) {
		//console.log( index + " : " + selectedTableIds[index]);
		var tempVal = selectedTableIds[index];
		if(tempVal != null){
			idVal = tempVal;
			count++;
		}			
	}
	
	if(count != 1){
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+advanceStudyError+"</div>");
		return;
	}
	
	Spinner.toggle();
	var advanceStudyHref = $('#advance-study-url').attr("href");
	
	if (tableName == "nursery-table") {
		if(idVal != null){
			location.href = advanceStudyHref + "/" + idVal;
			Spinner.toggle();
		}
	}
}
function showErrorMessage(messageDivId, message) {
	//console.log(message);
	$("#" + messageDivId).html(
			"<div class='alert alert-danger'>"+ message +"</div>"
	);
}

function showSuccessfulMessage(messageDivId, message) {
	$("#" + messageDivId).html(
			"<div class='alert alert-success'>"+ message +"</div>"
	);
}

function hideErrorMessage(){
	$('#page-message .alert-danger').fadeOut(1000);
}


function initializeHarvestLocationSelect2(locationSuggestions, locationSuggestions_obj) {

	$.each(locationSuggestions, function( index, value ) {
		locationSuggestions_obj.push({ 'id' : value.locid,
			  'text' : value.lname,
			  'abbr' : value.labbr
		});  
		
	});		
	
	//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('harvestLocationIdAll')).select2({
		minimumInputLength: 2,
        query: function (query) {
          var data = {results: locationSuggestions_obj}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
            query.callback(data);
            
        }

    }).on("change", function (){
    	$('#'+getJquerySafeId("harvestLocationId")).val($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data").id);
    	$('#'+getJquerySafeId("harvestLocationName")).val($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data").text);
    	$('#'+getJquerySafeId("harvestLocationAbbreviation")).val($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data").abbr);
    });
	
}

function initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj) {

	$.each(locationSuggestionsFav, function( index, value ) {
		locationSuggestionsFav_obj.push({ 'id' : value.locid,
			  'text' : value.lname,
			  'abbr' : value.labbr
		});  
  		
	});


//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
$('#'+getJquerySafeId('harvestLocationIdFavorite')).select2({
    query: function (query) {
      var data = {results: locationSuggestionsFav_obj}, i, j, s;
      // return the array that matches
      data.results = $.grep(data.results,function(item,index) {
        return ($.fn.select2.defaults.matcher(query.term,item.text));
      
      });
        query.callback(data);
        
    }

}).on("change", function (){
	$('#'+getJquerySafeId("harvestLocationId")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").id);
	$('#'+getJquerySafeId("harvestLocationName")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").text);
	$('#'+getJquerySafeId("harvestLocationAbbreviation")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").abbr);
});

}

function initializeMethodSelect2(methodSuggestions, methodSuggestions_obj) {

	$.each(methodSuggestions, function( index, value ) {
		methodSuggestions_obj.push({ 'id' : value.mid,
			  'text' : value.mname
		});  
		
	});		
	
	//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('methodIdAll')).select2({
		//minimumInputLength: 2,
        query: function (query) {
          var data = {results: methodSuggestions_obj}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
            query.callback(data);
            
        }

    }).on("change", function (){
    	$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdAll")).select2("data").id);
    });
	
}

function initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFav_obj) {

	$.each(methodSuggestionsFav, function( index, value ) {
		methodSuggestionsFav_obj.push({ 'id' : value.mid,
			  'text' : value.mname,
		});  
  		
	});


//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
$('#'+getJquerySafeId('methodIdFavorite')).select2({
    query: function (query) {
      var data = {results: methodSuggestionsFav_obj}, i, j, s;
      // return the array that matches
      data.results = $.grep(data.results,function(item,index) {
        return ($.fn.select2.defaults.matcher(query.term,item.text));
      
      });
        query.callback(data);
        
    }

}).on("change", function (){
	$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdFavorite")).select2("data").id);
});

}

