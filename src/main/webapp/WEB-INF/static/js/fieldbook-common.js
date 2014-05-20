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
    if (typeof convertToSelect2 === 'undefined' || convertToSelect2 == true) {
    	    // variable is undefined
    	
    $('select').each(function(){
			$(this).select2();
		});
    }
});


function doAjaxMainSubmit(pageMessageDivId, successMessage, overrideAction){
	Spinner.toggle();
	var form = $("form");
	var action = form.attr('action');
	var serializedData = form.serialize();
	
	if(overrideAction != null && overrideAction != '')
		action = overrideAction;
	
	$.ajax({
		url: action,
		type: "POST", 	
		data: serializedData,
		success: function (html) {
			//we just paste the whole html
			$('.container .row').first().html(html);
			if(pageMessageDivId != null && pageMessageDivId != '')
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
           //async: false,
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
             }else if(sectionDiv == 'inventory-germplasm-list'){
            	 //we highlight the previously clicked
            	 for(var index in selectedGids) {
         			//console.log( index + " : " + selectedTableIds[index]);
         			var idVal = selectedGids[index];
         			if(idVal != null){
         				//we need to highlight
         				$('tr.primaryRow[data-gid='+idVal+']').addClass('field-map-highlight');
         			}			
         		 }
             }
             
             if (sectionDiv == 'imported-germplasm-list') {
            	 makeDraggable(makeDraggableBool);
            	//we'll do the highlight
             	if(typeof itemsIndexAdded === 'undefined');
             	else{
             		
             		if(itemsIndexAdded != null && itemsIndexAdded.length > 0){
             			for(var indexItems = 0 ; indexItems < itemsIndexAdded.length ; indexItems++){
             				if(itemsIndexAdded[indexItems] != null){
             					var rowIndex = itemsIndexAdded[indexItems].index;
             					if($('.primaryRow[data-index="'+rowIndex+'"]').length != 0){
             						$('.primaryRow[data-index="'+rowIndex+'"]').css('opacity', '0.5');	
             					}
             				}
             			}
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
	var $form;
	if (formName.indexOf("#") > -1) {
		$form = $(formName);
	} 
	else {
		$form = $("#"+formName);
	}
	
	var completeSectionDivName;
	if (sectionDiv.indexOf("#") > -1) {
		completeSectionDivName = sectionDiv;
	}
	else {
		completeSectionDivName = "#" + sectionDiv;
	}
	
	var serializedData = $form.serialize();
	
	Spinner.toggle();
 	$.ajax(
         { url: paginationUrl+pageNum+"/"+previewPageNum+'?r=' + (Math.random() * 999),
           type: "POST",
           data: serializedData,
           cache: false,
           timeout: 70000,
           success: function(html) {
        	   
             $(completeSectionDivName).empty().append(html);
             
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
             
             if (sectionDiv == 'check-germplasm-list') {
            	 makeCheckDraggable(makeCheckDraggableBool);
             }
             
             Spinner.toggle();  
           }
         }
       );
}

function triggerFieldMapTableSelection(tableName){
	$('#'+tableName+' tr.data-row').on('click', function() {	
		//$('#'+tableName).find("*").removeClass('field-map-highlight');
		if (tableName == "studyFieldMapTree") {
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
	if($('#'+tableName+' .field-map-highlight').attr('id') != null || tableName == 'nursery-table'){
		var ids = [];
		//$('#'+tableName+' .field-map-highlight').each(function(){ ids.push(this.id); });
		//get selected studies
		/*
		for(var index in selectedTableIds) {			
			var idVal = selectedTableIds[index];
			if(idVal != null){
				ids.push(idVal);
			}			
		}
		//daniel
		*/
		if($("#createNurseryMainForm #studyId").length  == 1)
			ids.push($("#createNurseryMainForm #studyId").val());
		else
			ids.push(getCurrentStudyIdInTab());
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
		triggerFieldMapTableSelection('studyFieldMapTree');
		
	}
	styleDynamicTree('studyFieldMapTree');
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
		newRow = newRow + "<th style='width:15%'>" + fieldmapLabel + "</th>";
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
	$("#studyFieldMapTree").append(newRow+"<tbody></tbody>");
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
	var hasFieldMap = value.hasFieldMap ? "Yes" : "No";
	var disabledString = value.hasFieldMap ? "disabled" : "";
	
	newRow = "<tr class='data-row trialInstance "+ genClassName + id + " " + genParentClassName + "'>";
	var checkBox = "<input "+disabledString+" class='checkInstance' type='checkbox' id='" + datasetId + "|" + realId + "' /> &nbsp;&nbsp;";
	newCell = "<td>" + checkBox + "&nbsp;" + datasetName + "</td><td>" + value.entryCount + "</td>";
	
	newCell = newCell + "<td class='hasFieldMap'>" + hasFieldMap + "</td>";
	$("#studyFieldMapTree").append(newRow+newCell+"</tr>");
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
			newCell = newCell + "<td></td>";
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
			
			var hasFieldMap = value.hasFieldMap ? "Yes" : "No";
			var disabledString = value.hasFieldMap ? "disabled" : "";
			
			newRow = "<tr class='data-row trialInstance "+ genClassName + id + " " + genParentClassName + "'>";
			var checkBox = "<input "+disabledString+" class='checkInstance' type='checkbox' id='" + realId + "' /> &nbsp;&nbsp;";
			newCell = "<td>" + checkBox + "&nbsp;" + value.trialInstanceNo + "</td><td>" + value.entryCount + "</td>";
			if (trial) {
				newCell = newCell + "<td>" + value.repCount + "</td><td>" + value.plotCount + "</td>";
			}
			
			newCell = newCell + "<td class='hasFieldMap'>" + hasFieldMap + "</td>";
		}
	}
	$("#studyFieldMapTree").append(newRow+newCell+"</tr>");
}

function clearStudyTree() {
	$("#studyFieldMapTree").empty();
}

function showMessage(message) {
	$("#page-message").html(
		    "<div class='alert alert-danger'>"+ message +"</div>"
	);
}

function createLabelPrinting(tableName){	
	
	var count = 0;
	var idVal = null;
	/*
	for(var index in selectedTableIds) {
		//console.log( index + " : " + selectedTableIds[index]);
		var tempVal = selectedTableIds[index];
		if(tempVal != null){
			idVal = tempVal;
			count++;
		}			
	}
	*/
	if($("#createNurseryMainForm #studyId").length  == 1)
		idVal = ($("#createNurseryMainForm #studyId").val());
	else
		idVal = getCurrentStudyIdInTab();
	count++; 
	
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
	if ($('#studyFieldMapTree .field-map-highlight').attr('id')) {
		if ($('#studyFieldMapTree .field-map-highlight').size() == 1) {
			$("#selectTrialInstanceModal").modal("toggle");
			var id = $('#studyFieldMapTree .field-map-highlight').attr('id');
			var datasetId = $('#studyFieldMapTree .field-map-highlight').treegrid('getParentNode').attr("id");
			location.href = "/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/" + datasetId + "/" + id;
		} else {
			showMessage(multipleSelectError);
		}
	} else {
		showMessage(noSelectedTrialInstance);
	}
}

function showCreateFieldMap() {
	if ($('#studyFieldMapTree .checkInstance:checked').attr('id')) {
		var selectedWithFieldMap = false;
		fieldmapIds = [];
		$('#studyFieldMapTree .checkInstance:checked').each(function(){
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
			if (trial) {
				hasFieldMap = $(this).parent().next().next().next().next().html();
			} else {
				hasFieldMap = $(this).parent().next().next().html();
			}
			
			//build id list of selected trials instances
			fieldmapIds.push(studyId+"|"+datasetId+"|"+id);
			
			if (hasFieldMap == "Yes") {
				selectedWithFieldMap = true;
			}
		});
		//this is to disable the 2nd popup
		if (selectedWithFieldMap) {
			//$("#selectTrialInstanceModal button,input").attr("disabled", true);
			//$("#confirmSubModal").modal("toggle");
			showMessage(hasFieldmapError);
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
function openStudyOldFb(){
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
function openStudy(tableName){	
	
	var count = 0;
	/*
	for(var index in selectedTableIds) {
		//console.log( index + " : " + selectedTableIds[index]);
		var tempVal = selectedTableIds[index];
		if(tempVal != null){
			idVal = tempVal;
			count++;
		}			
	}
	*/
	idVal = getCurrentStudyIdInTab();
	count++;
	if(count != 1){
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+openStudyError+"</div>");
		return;
	}
	
	Spinner.toggle();
	var openStudyHref = $('#open-study-url').attr("href");
	
//	if (tableName == "nursery-table") {
		if(idVal != null){
			location.href = openStudyHref + "/" + idVal;
			Spinner.toggle();
		}
//	} else {
//		$.ajax(
//	    { url: openStudyHref,
//	       type: "GET",
//	       data: "",
//	       cache: false,
//	       success: function() {    	            
//	         Spinner.toggle();  
//	       }
//	     });
//	}
}

function advanceNursery(tableName){
	var count = 0;
	/*
	for(var index in selectedTableIds) {
		//console.log( index + " : " + selectedTableIds[index]);
		var tempVal = selectedTableIds[index];
		if(tempVal != null){
			idVal = tempVal;
			count++;
		}			
	}
	*/
	idVal = $("#createNurseryMainForm #studyId").val();
	count++;
	if(count != 1){
		$('#page-create-field-map-message').html("<div class='alert alert-danger'>"+advanceStudyError+"</div>");
		return;
	}

	Spinner.toggle();
	var advanceStudyHref = $('#advance-study-url').attr("href");
	
	if (tableName == "nursery-table") {
		if(idVal != null){

			$.ajax({ 
				url: advanceStudyHref + "/" + encodeURIComponent(idVal),
			    type: "GET",
			    success: function(html) {
			    	$("#advance-nursery-modal-div").html(html);
					$("#advanceNurseryModal").modal("show");
					$('#advanceNurseryModal select').select2();
		        },
				error: function(jqXHR, textStatus, errorThrown){
					console.log("The following error occured: " + textStatus , errorThrown);
			    }, 
			    complete: function(){ 
				   Spinner.toggle();
			    } 
			});
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
    	
    	//	$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdAll")).select2("data").id);
    	if($('#'+getJquerySafeId("advanceBreedingMethodId")).length != 0)
    		$('#'+getJquerySafeId("advanceBreedingMethodId")).val($('#'+getJquerySafeId("methodIdAll")).select2("data").id);
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
	//$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdFavorite")).select2("data").id);
	if($('#'+getJquerySafeId("advanceBreedingMethodId")).length != 0)
		$('#'+getJquerySafeId("advanceBreedingMethodId")).val($('#'+getJquerySafeId("methodIdFavorite")).select2("data").id);
});

}

function exportTrial(type){
	$('#page-modal-choose-instance-message-r').html('');
	$('#page-modal-choose-instance-message').html('');
	$('.instanceNumber:first').click();	
	var numberOfInstances = $('#numberOfInstances').val();
	$('.spinner-input').spinedit({
	    minimum: 1,
	    maximum: parseInt(numberOfInstances),
	    value: 1
	});
	$('#exportTrialType').val(type);
	initTrialModalSelection();
	if(type == 2){
		$("#chooseInstance").detach().appendTo('#importRChooseInstance');
		$('#importRModal').modal('show');
	}
	else{
		$("#chooseInstance").detach().appendTo('#exportChooseInstance');
		$('#trialModalSelection').modal('show');
	}
		
	/*
	if(type == 2){
		$('#importRModal').modal('show');
	}else{		
		doExportContinue(type);
	}
	*/
}

function initTrialModalSelection() {
	$("#xportInstanceType").val(1);
	$("#exportTrialInstanceNumber").val(1);
	$('#exportTrialInstanceNumber').spinedit('setValue', 1);
	$("#exportTrialInstanceStart").val(1);
	$('#exportTrialInstanceStart').spinedit('setValue', 1);
	$("#exportTrialInstanceEnd").val(1);
	$('#exportTrialInstanceEnd').spinedit('setValue', 1);
	$("#selectedRTrait").prop("selectedIndex", 0);
}

function doExportTrial(){
	//console.log();
	var exportTrialType = $('#exportTrialType').val();
	doExportContinue(exportTrialType, false);
}

function exportNursery(){
	/*
	if(type == 2){
		$('#importRModal').modal('show');
	}else{		
		doExportContinue(type, true);
	}
	*/
	var type = $('#exportType').val();
	if(type==0){
		showErrorMessage('page-export-message-modal', 'Please choose export type');
		return false;
	}
	
	if(type == 2){
		exportNurseryToR(type);
	}else
		doExportContinue(type, true);
}

function exportNurseryToR(type){
	//console.log($('#selectedRTrait').val());
	var isNursery = true;
	if($("#study-type").val() == 'Trial')
		isNursery = false;
	
	var additionalParams = '';
	if(!isNursery){
		additionalParams = validateTrialInstance();
		if(additionalParams == 'false')
			return false;
	}
	
	doExportContinue(type + "/" + $('#selectedRTrait').val(), isNursery);
	//$('#importRModal').modal('hide');
}

function validateTrialInstance(){
	var exportInstanceType = $('input:radio[name=exportInstanceType]:checked').val();
	var additionalParams = '';
	if(exportInstanceType == 1){
		additionalParams = '0/0';
	}else if(exportInstanceType == 2){
		additionalParams = $('#exportTrialInstanceNumber').val() + "/" + $('#exportTrialInstanceNumber').val();
	}else{
		var start =  $('#exportTrialInstanceStart').val();
		var end = $('#exportTrialInstanceEnd').val();
		additionalParams = start + "/" + end;
		var exportTrialType = $('#exportTrialType').val();
		if(parseInt(start) >= parseInt(end)){
			var errorDiv = "page-modal-choose-instance-message";
			if(exportTrialType == 2)
				errorDiv = "page-modal-choose-instance-message-r";
			showErrorMessage(errorDiv, 'To trial instance # should be greater than the From Trial Instance #');
			additionalParams = 'false';
			//return false;
		}
	}
	return additionalParams;
}

function doExportContinue(paramUrl, isNursery){

	
	var currentPage = $('#measurement-data-list-pagination .pagination .active a').html();
	
	var formname;
	if (isNursery) {
		formname = "#addVariableForm";	
	}
	else {
		formname = "#addVariableForm, #addVariableForm2";	
	}
	var $form = $(formname);	
	var serializedData = $form.serialize();

	
	var additionalParams = ''
	if(!isNursery){
		additionalParams = validateTrialInstance();
		if(additionalParams == 'false')
			return false;
		else{
			$('#trialModalSelection').modal('hide');
		}
	}
	var exportWayType = '/'+$('#exportWayType').val();
	var urlPage = paginationUrl+currentPage+"/"+currentPage+'?r=' + (Math.random() * 999);
	//alert(urlPage);
	Spinner.toggle();
 	$.ajax(
         { url: urlPage,
           type: "POST",
           data: serializedData,
           cache: false,
           timeout: 70000,
           async: false,
           success: function(html) {
        	   var formName = "#exportStudyForm";
        	   var action = submitExportUrl;
        	   var newAction = '';
        	   	if(isNursery)
        	   		newAction = action + "export/" + paramUrl;
        	   	else{ //meaning its trial
        	   		
        	   		newAction = action + "exportTrial/" + paramUrl + "/" + additionalParams;
        	   		
        	   
        	   	 
        	   	}
        		newAction += exportWayType;

        	   $(formName).attr('action', newAction);
        	   $(formName).submit();
        	   $(formName).attr('action', action);
        	   $('#exportStudyModal').modal('hide');
        	   Spinner.toggle();
           }
         }
       );
}

function importNursery(type){
		
	var action = "/Fieldbook/ImportManager/import/" + $("#study-type").val() + "/"+type;
	var formName = "#importStudyUploadForm";
	$(formName).attr('action', action);
	//console.log(action);   
}

function submitImportStudy() {
	if($('#importType').val() == 0){
		showErrorMessage('page-import-message-modal', 'Please choose import type');
		return false;
	}
	
	if($('#fileupload').val() == ''){
		showErrorMessage('page-import-message-modal', 'Please choose a file to import');
		return false;
	}
	Spinner.toggle();
	 $("#importStudyUploadForm").ajaxForm(importOptions).submit();  
}
function isFloat(value) { 
    return !isNaN(parseInt(value,10)) && (parseFloat(value,10) == parseInt(value,10)); 
}
function moveToTopScreen(){
	 $('html').scrollTop(0);
}
function openImportGermplasmList() {
	$('#listTreeModal').modal('hide');
	
	setTimeout(function(){
		$('#importFrame').attr('src', importLocationUrl);
		$('#importGermplasmModal').modal({ backdrop: 'static', keyboard: true });
		}, 500);

	
	//if(importIframeOpened == false){
	//	importIframeOpened = true;
		
	//}
}

function doTreeHighlight(treeName, nodeKey){
	$("#"+treeName).dynatree("getTree").activateKey(nodeKey);
	$('#'+treeName).find("*").removeClass('highlight');
	//then we highlight the nodeKey and its parents
	var elem = nodeKey.split("_");
	var count = 0;
	var key = "";
	for(count = 0 ; count < elem.length ; count++){
		if(key != '')
			key = key + "_";
		
		key = key + elem[count];
		$('.'+key).addClass('highlight');
		//console.log(key);
	}
}

function addCreateNurseryRequiredAsterisk(){
	var requiredText = "<span class='required'>*</span>";
	//console.log('add asterisk');
	
	for (var i = 0; i < requiredFields.length; i++) {
		var cvTermId = requiredFields[i];
	    if($('.cvTermIds[value="'+cvTermId+'"]').length != 0){
	    	
	    	$('.cvTermIds[value="'+cvTermId+'"]').parent().parent().find('.nursery-level-label').parent().append(requiredText);
	    }
	    //Do something
	}
	
}

function addCreateTrialRequiredAsterisk(){
	var requiredText = "<span class='required'>*</span>";
	//console.log('add asterisk');
	
	for (var i = 0; i < requiredFields.length; i++) {
		var cvTermId = requiredFields[i];
	    if($('.cvTermIds[value="'+cvTermId+'"]').length != 0){
	    	
	    	$('.cvTermIds[value="'+cvTermId+'"]').parent().parent().find('.trial-level-label').parent().append(requiredText);
	    }
	    //Do something
	}
	
}

function getDateRowIndex(divName, dateCvTermId) {
	
	var rowIndex = -1;
	$('.'+divName+' .cvTermIds').each(function(index){
		if($(this).val() ==  parseInt(dateCvTermId))
			rowIndex = index;
		})
		return rowIndex;	
}

function validateStartEndDate(divName){
	//8050 - start
	var startDateIndex = getDateRowIndex(divName, startDateId);
	var endDateIndex = getDateRowIndex(divName, endDateId);
	//console.log(startDateIndex);	  
	//console.log(endDateIndex);
	var startDate = $("#" + getJquerySafeId("studyLevelVariables"+startDateIndex+".value")).val();
	var endDate = $("#" + getJquerySafeId("studyLevelVariables"+endDateIndex+".value")).val();
	//console.log(startDate);	  
	//console.log(endDate);
	startDate = startDate == null ? '' : startDate;
	endDate = endDate == null ? '' : endDate;
	//console.log(startDate);	  
	//console.log(endDate);
	if(startDate == '' && endDate == '')
		return true;
	else if(startDate != '' && endDate == ''){
		return true;
	}else if(startDate == '' && endDate != ''){
		showErrorMessage("page-message", startDateRequiredError);
		return false;
	}else if(parseInt(startDate) > parseInt(endDate)){
		showErrorMessage("page-message", startDateRequiredEarlierError);
		return false;
	}
	return true;
	
}
function getIEVersion() {
    var myNav = navigator.userAgent.toLowerCase();
    return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
}

function callAdvanceNursery() {
	Spinner.toggle();
	var serializedData = $("#advanceNurseryModalForm").serialize();
	
 	$.ajax({ 
 		url: "/Fieldbook/NurseryManager/advance/nursery",
        type: "POST",
        data: serializedData,
        cache: false,
        success: function(html) {
        	$("#advanceNurseryModal").modal("hide");
        	$('#create-nursery-tab-headers li').removeClass('active');
       	 	$('#create-nursery-tabs .info').hide();
       	 
        	var uniqueId = $(html).find('.uniqueId').attr('id');
        	var close = '<button style="float: right" onclick="javascript: closeAdvanceListTab('+uniqueId+')" type="button" id="'+uniqueId+'" class="close">x</button>';
        	var aHtml = "<a href='javascript: showSelectedAdvanceTab("+uniqueId+")'>Advance List "+close+"</a>";
        	$("#create-nursery-tab-headers").append("<li class='active' id='advance-list"+uniqueId+"-li'>"+aHtml+"</li>");
        	$("#create-nursery-tabs").append("<div class='info' id='advance-list"+uniqueId+"'>" + html + "</div>");       	
        	showSelectedTab("advance-list"+uniqueId);
        	
        },
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus , errorThrown);
	    }, 
	    complete: function(){ 
	    	Spinner.toggle();
	    } 
	});
}
function showSelectedAdvanceTab(uniqueId){
	//$('#create-nursery-tab-headers li').removeClass('active');
	//$('#create-nursery-tabs .info').hide();
	showSelectedTab('advance-list'+uniqueId);
}
function closeAdvanceListTab(uniqueId){
	
	$('li#advance-list'+uniqueId+"-li").remove();
	$('.info#advance-list'+uniqueId).remove();
	setTimeout(function(){
	$('#create-nursery-tab-headers li').removeClass('active');
	$('#create-nursery-tabs .info').hide();
	$('#create-nursery-tab-headers li:eq(0)').addClass('active');
	$('#create-nursery-tabs .info:eq(0)').css('display', 'block');
	}, 100);
	
}

function displayAdvanceList(uniqueId, germplasmListId){
	Spinner.toggle();
	
 	$.ajax({  		
 		url: "/Fieldbook/SeedStoreManager/advance/displayGermplasmDetails/"+germplasmListId,
        type: "GET",       
        cache: false,
        success: function(html) {
       	 	$('#advance-list'+uniqueId).html(html);
        	Spinner.toggle();
        }
	});
}