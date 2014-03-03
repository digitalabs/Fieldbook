function checkMethod(){
	if($('input[type=radio][name=methodChoice]:checked').val() == 1){
			//$('#methodSelected').prop('disabled', false);
			$('#methodIdFavorite').select2('enable', true);
			$('#methodIdAll').select2('enable', true);
			$('#showFavoriteMethod').prop('disabled', false);
			
			//$('#methodSelected').val(oldMethodSelected);
			setCorrectMethodValues(true);
		}else{
			//$('#methodSelected').prop('disabled', 'disabled');
			$('#showFavoriteMethod').prop('disabled', 'disabled');
			$('#methodIdFavorite').select2('enable', false);
			$('#methodIdAll').select2('enable', false);
			
			oldMethodSelected = $('#'+getJquerySafeId("breedingMethodId")).val();
			 $('#methodSelected').val($('#defaultMethodId').val());
			 setCorrectMethodValues(false);
		} 	
}
function setCorrectMethodValues(isCheckMethod){
	 
	 if($('#showFavoriteMethod').is(':checked')){
		 //we check if the default is in the favorite method list or not
		 var isFound = false;
		 var dataVal = null;
		 var findId = $('#defaultMethodId').val();
		 if(isCheckMethod)
		 	findId = oldMethodSelected;
		 for(key in methodSuggestionsFav_obj){
			 if(methodSuggestionsFav_obj[key].id == findId){
				 isFound = true;
				 dataVal = methodSuggestionsFav_obj[key];
				 break;
			 }
		 }
		 if(isFound){
			 $("#methodIdFavorite").select2('data', dataVal).trigger('change');					  	
		 }else if(methodSuggestionsFav_obj.length > 0){
			 //we set the first
			 $("#methodIdFavorite").select2('data', methodSuggestionsFav_obj[0]).trigger('change');
		 }else{
			 $('#'+getJquerySafeId("breedingMethodId")).val(0);
		 }
		 
		
	 }else{
		 var isFound = false;
		 var dataVal = null;
		 var findId = $('#defaultMethodId').val();
		 if(isCheckMethod)
		 	findId = oldMethodSelected;
		 for(key in methodSuggestions_obj){
			 if(methodSuggestions_obj[key].id == findId){
				 isFound = true;
				 dataVal = methodSuggestions_obj[key];
				 break;
			 }
		 }
		 if(isFound){
			 $("#methodIdAll").select2('data', dataVal).trigger('change');					  	
		 }
	 }
}
function lineMethod(){
	if($('input[type=radio][name=lineChoice]:checked').val() == 1){
			$('#lineSelected').prop('disabled', false);
			$('#lineSelected').val(oldLineSelected);
		}else{
			$('#lineSelected').prop('disabled', 'disabled');
			oldLineSelected = $('#lineSelected').val();
			$('#lineSelected').val(1);
		} 
}

function validateAdvanceNursery(){
	//validate number of sample per plot
	var numberOfSamplePerPlot = $('#lineSelected').val();
	if(numberOfSamplePerPlot == ''){
		showErrorMessage('page-message',msgSamplePlotError);
		return false;
	}
	if(!isInt(numberOfSamplePerPlot)){
		showErrorMessage('page-message',msgSamplePlotError);
		return false;
	}
	if(Number(numberOfSamplePerPlot) < 1 || Number(numberOfSamplePerPlot) > 1000){
		showErrorMessage('page-message',msgSamplePlotError);
		return false;
	}
	if( $('#harvestDate').val() == ''){
		showErrorMessage('page-message',msgHarvestDateError);
		return false;
	}	
	
	
	//doAjaxMainSubmit('', '', '');
		
	
	//validate date
	return true;
}

function showCorrectLocationCombo() {
	var isChecked = $('#showFavoriteLocation').is(':checked');
	//if show favorite location is checked, hide all field locations, else, show only favorite locations
	if(isChecked){
		$('#s2id_harvestLocationIdFavorite').show();
		$('#s2id_harvestLocationIdAll').hide();
		if($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data") != null){
			$('#'+getJquerySafeId("harvestLocationId")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").id);
			$('#'+getJquerySafeId("harvestLocationName")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").text);
			$('#'+getJquerySafeId("harvestLocationAbbreviation")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").abbr);
			
		}else{
			$('#'+getJquerySafeId("harvestLocationId")).val(0);
			$('#'+getJquerySafeId("harvestLocationName")).val("");
			$('#'+getJquerySafeId("harvestLocationAbbreviation")).val("");
		}
	}else{
		$('#s2id_harvestLocationIdFavorite').hide();
		$('#s2id_harvestLocationIdAll').show();
		if($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data") != null){
			$('#'+getJquerySafeId("harvestLocationId")).val($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data").id);
			$('#'+getJquerySafeId("harvestLocationName")).val($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data").text);
			$('#'+getJquerySafeId("harvestLocationAbbreviation")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").abbr);
		}else{
			$('#'+getJquerySafeId("harvestLocationId")).val(0);
			$('#'+getJquerySafeId("harvestLocationName")).val("");
			$('#'+getJquerySafeId("harvestLocationAbbreviation")).val("");
		}
		
	}
}

function showCorrectMethodCombo() {
	var isChecked = $('#showFavoriteMethod').is(':checked');
	//if show favorite Method is checked, hide all field locations, else, show only favorite methods
	var methodSelect = false;
	if($('input[type=radio][name=methodChoice]:checked').val() == 1)
		methodSelect = true;
	
	if(isChecked){
		$('#s2id_methodIdFavorite').show();
		$('#s2id_methodIdAll').hide();
		setCorrectMethodValues(methodSelect);
		if($('#'+getJquerySafeId("methodIdFavorite")).select2("data") != null){
			$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdFavorite")).select2("data").id); 						 						
		}else{
			$('#'+getJquerySafeId("breedingMethodId")).val(0); 						
		}
	}else{
		$('#s2id_methodIdFavorite').hide();
		$('#s2id_methodIdAll').show();
		setCorrectMethodValues(methodSelect);
		if($('#'+getJquerySafeId("methodIdAll")).select2("data") != null){
			$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdAll")).select2("data").id);
		}else{
			$('#'+getJquerySafeId("breedingMethodId")).val(0);
		} 					
	}
}

function openManageLocations() {
	$('#manageLocationModal').modal({ backdrop: 'static', keyboard: true });
	$("#manageLocationModal").modal("show");
	if(locationIframeOpened == false){
		locationIframeOpened = true;
		$('#locationFrame').attr('src', programLocationUrl + $('#projectId').val());
	}
	
}

function openManageMethods() {
	$('#manageMethodModal').modal({ backdrop: 'static', keyboard: true });
	$("#manageMethodModal").modal("show");
	if(methodIframeOpened == false){
		methodIframeOpened = true;
		$('#methodFrame').attr('src', programMethodUrl + $('#projectId').val());
	}
}

function recreateMethodCombo() {
var selectedMethodAll = $("#methodIdAll").val();
var selectedMethodFavorite = $("#methodIdFavorite").val();

Spinner.toggle();
$.ajax(
     { url: "/Fieldbook/NurseryManager/advance/nursery/getBreedingMethods",
       type: "GET",
       cache: false,
       data: "",
       success: function(data) {
    	   if (data.success == "1") {
    		   if (selectedMethodAll) {
	    		   //recreate the select2 combos to get updated list of methods
	    		   recreateMethodComboAfterClose("methodIdAll", $.parseJSON(data.allMethods));
	    		   recreateMethodComboAfterClose("methodIdFavorite", $.parseJSON(data.favoriteMethods));
	    		   showCorrectMethodCombo();
	    		   //set previously selected value of method
	    		   if ($("#showFavoriteMethod").prop("checked")) {
	    			   setComboValues(methodSuggestionsFav_obj, selectedMethodFavorite, "methodIdFavorite");
	    		   } else {
	    			   setComboValues(methodSuggestions_obj, selectedMethodAll, "methodIdAll");
	    		   }
    		   } else {
    			   var selectedVal = null;
    			   //get index of breeding method row
    			   var index = getBreedingMethodRowIndex();
    			   
    			   if ($("#" + getJquerySafeId("nurseryLevelVariables" + index + ".value")).select2("data")) {
    				   selectedVal = $("#" + getJquerySafeId("nurseryLevelVariables" + index + ".value")).select2("data").id;
    			   }
    			   //recreate select2 of breeding method
    			   initializePossibleValuesCombo([], 
	 			 			"#" + getJquerySafeId("nurseryLevelVariables" + index + ".value"), false, selectedVal);
    			   initializePossibleValuesCombo($.parseJSON(data.allMethods), 
	 			 			"#" + getJquerySafeId("nurseryLevelVariables" + index + ".value"), false, selectedVal);
    		   }
    	   } else {
    		   showErrorMessage("page-message", data.errorMessage);
    	   }
       },
       error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
	   }, 
	   complete: function(){
		   Spinner.toggle();
	   } 
     }
 );
}

function getBreedingMethodRowIndex() {
	var rowIndex = 0;
	$.each($("#nurseryLevelSettings tr"), function (index, row) {
		var cvTermId = $($(row).children("td:nth-child(1)")
				.children("#" + getJquerySafeId("nurseryLevelVariables" + index + ".variable.cvTermId"))).val();
		if (parseInt(cvTermId) == parseInt(breedingMethodId)) {
			rowIndex = index;
		}
	});
	return rowIndex;
}

function recreateLocationCombo() {
	var selectedLocationAll = $("#harvestLocationIdAll").val();
	var selectedLocationFavorite = $("#harvestLocationIdFavorite").val();
	
	Spinner.toggle();
	$.ajax(
	{ url: "/Fieldbook/NurseryManager/advance/nursery/getLocations",
       type: "GET",
       cache: false,
       data: "",
       success: function(data) {
    	   if (data.success == "1") {
    		   if (selectedLocationAll) {
	    		   //recreate the select2 combos to get updated list of locations
	    		   recreateLocationComboAfterClose("harvestLocationIdAll", $.parseJSON(data.allLocations));
	    		   recreateLocationComboAfterClose("harvestLocationIdFavorite", $.parseJSON(data.favoriteLocations));
	    		   showCorrectLocationCombo();
	    		   //set previously selected value of location
	    		   if ($("#showFavoriteLocation").prop("checked")) {
	    			   setComboValues(locationSuggestionsFav_obj, selectedLocationFavorite, "harvestLocationIdFavorite");
	    		   } else {
	    			   setComboValues(locationSuggestions_obj, selectedLocationAll, "harvestLocationIdAll");
	    		   }
    		   } else {
    			   var selectedVal = null;
    			   if ($("#" + getJquerySafeId("nurseryLevelVariables0.value")).select2("data")) {
    				   selectedVal = $("#" + getJquerySafeId("nurseryLevelVariables0.value")).select2("data").id;
    			   } 
    			   initializePossibleValuesCombo([], 
	 			 			"#" + getJquerySafeId("nurseryLevelVariables0.value"), true, selectedVal);
    			   initializePossibleValuesCombo($.parseJSON(data.allLocations), 
	 			 			"#" + getJquerySafeId("nurseryLevelVariables0.value"), true, selectedVal);
    		   }
    	   } else {
    		   showErrorMessage("page-message", data.errorMessage);
    	   }
       },
       error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
	   }, 
	   complete: function(){  
		   Spinner.toggle();
	   } 
     }
 );
}

function setComboValues(suggestions_obj, id, name) {
	var dataVal = {id:'',text:'',description:''}; //default value
	if(id != ''){
		var count = 0;
		//find the matching value in the array given
    	for(count = 0 ; count < suggestions_obj.length ; count++){
    		if(suggestions_obj[count].id == id){
    			dataVal = suggestions_obj[count];			    			
    			break;
    		}			    			
    	}
	}
	//set the selected value of the combo
	$("#" + name).select2('data', dataVal);
}

function recreateLocationComboAfterClose(comboName, data) {	
	if (comboName == "harvestLocationIdAll") {
		//clear all locations dropdown
		locationSuggestions = [];
		locationSuggestions_obj = [];
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestions_obj);
		//reload the data retrieved
		locationSuggestions = data;
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestions_obj);
	} else {
		//clear the favorite locations dropdown
		locationSuggestionsFav = [];
		locationSuggestionsFav_obj = [];
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);
		//reload the data
		locationSuggestionsFav = data;
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);
	}

}

function recreateMethodComboAfterClose(comboName, data) {
	if (comboName == "methodIdAll") {
		//clear the all methods dropdown
		methodSuggestions = [];
		methodSuggestions_obj = [];
		initializeMethodSelect2(methodSuggestions, methodSuggestions_obj);
		//reload the data
		methodSuggestions = data;
		initializeMethodSelect2(methodSuggestions, methodSuggestions_obj);
	} else {
		//clear the favorite methods dropdown
		methodSuggestionsFav = [];
		methodSuggestionsFav_obj = [];
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFav_obj);
		//reload the data
		methodSuggestionsFav = data;
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFav_obj);
	}
}

function openAddVariablesSetting(variableType) {
	//change heading of popup based on clicked link
	$('#var-info').slideUp('fast');
	switch (parseInt(variableType)) {
		case 1:
			$("#heading-modal").text(addNurseryLevelSettings);
			break;
		case 2:
			$("#heading-modal").text(addPlotLevelSettings);
			break;
		case 3:
			$("#heading-modal").text(addBaselineTraits);
			break;
	default: 
		$("#heading-modal").text(addNurseryLevelSettings);
	}
	getStandardVariables(variableType);
	
}

function getStandardVariables(variableType) {
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/NurseryManager/manageNurserySettings/displayAddSetting/" + variableType,
		type: "GET",
		cache: false,
		success: function (data) {
			//clear and initialize standard variable combo
			initializeStandardVariableSearch([]);
			initializeStandardVariableSearch($.parseJSON(data));
			
			//clear selected variables table and attribute fields
			$("#newVariablesList > tbody").empty();
			$("#page-message-modal").html("");
			clearAttributeFields();
			$("#addVariables").attr("onclick", "javascript: submitSelectedVariables(" + variableType + ");");
			$("#addVariablesSettingModal").modal("show");
		},
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}

function initializeStandardVariableSearch(variables) {
	//set values
	var stdVariableSuggestions_obj = [];
	$.each(variables, function(index, value) {
		stdVariableSuggestions_obj.push({ 'id' : value.id,
			  'text' : value.name
		});  
	});
	
	stdVariableSuggestions_obj = sortByKey(stdVariableSuggestions_obj, "text"); 

	$("#stdVarSearch").select2({
		query: function (query) {	
      var data = {results: stdVariableSuggestions_obj}, i, j, s;
      // return the array that matches
      data.results = $.grep(data.results,function(item,index) {
        return ($.fn.select2.defaults.matcher(query.term,item.text));
      
      });
      if (data.results.length === 0){
    	  data.results.unshift({id:query.term,text:query.term});	        	 
      }
      
        query.callback(data);
    }
    }).unbind("change").on("change", function (){
    	//set attribute values
    	getStandardVariableDetailsModal($("#stdVarSearch").select2("data").id);
    });
	var dataVal = {'id': '', 'text': ''};
	$("#stdVarSearch").select2('data', dataVal).trigger('change');
}

function getStandardVariableDetailsModal(id) {
	if(id != ''){
		Spinner.toggle();
		$.ajax({
			url: "/Fieldbook/NurseryManager/manageNurserySettings/showVariableDetails/" + id,
			type: "GET",
			cache: false,
			success: function (data) {
				$('#var-info').slideDown("slow");
				populateAttributeFields($.parseJSON(data));
			},
			error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: " + textStatus, errorThrown); 
			},
			complete: function() {
				Spinner.toggle();
			}
		});
	}
	
}
		
function populateAttributeFields(data) {
	$("#selectedTraitClass").html(checkIfEmpty(data.traitClass));
	$("#selectedProperty").html(checkIfEmpty(data.property));
	$("#selectedMethod").html(checkIfEmpty(data.method));
	$("#selectedScale").html(checkIfEmpty(data.scale));
	$("#selectedDataType").html(checkIfEmpty(data.dataType));
	$("#selectedRole").html(checkIfEmpty(data.role));
	$("#selectedCropOntologyId").html(checkIfEmpty(data.cropOntologyId));
	$("#selectedStdVarId").val(data.cvTermId);
	$("#selectedName").val(data.name);
}

function checkIfEmpty(value) {
	if (value == "") {
		return "&nbsp";
	} else {
		return value;
	}
}

function clearAttributeFields() {
	$("#selectedTraitClass").html("&nbsp;");
	$("#selectedProperty").html("&nbsp;");
	$("#selectedMethod").html("&nbsp;");
	$("#selectedScale").html("&nbsp;");
	$("#selectedDataType").html("&nbsp;");
	$("#selectedRole").html("&nbsp;");
	$("#selectedCropOntologyId").html("&nbsp;");
	$("#selectedStdVarId").val("");
	$("#selectedName").val("");
}

function addVariableToList() { 
	var newRow;
	var rowCount = $("#newVariablesList tbody tr").length;
	var ctr;
	
	//get the last counter for the selected variables and add 1
	if (rowCount == 0) {
		ctr = 0; 
	} else {
		var lastVarId = $("#newVariablesList tbody tr:last-child td input[type='hidden']").attr("name");
		ctr = parseInt(lastVarId.substring(lastVarId.indexOf("[") + 1, lastVarId.indexOf("]"))) + 1;
	}

	var length = $("#newVariablesList tbody tr").length + 1;
	var className = length % 2 == 1 ? 'even' : 'odd';
	
	//if selected variable is not yet in the list and is not blank or new, add it
	if (notInList($("#selectedStdVarId").val()) && $("#selectedStdVarId").val() != "") {
		newRow = "<tr>";
		newRow = newRow + "<td class='"+className+"'><input type='hidden' class='addVariables' id='selectedVariables"+ ctr + ".cvTermId' " +  
			"name='selectedVariables["+ ctr + "].cvTermId' value='" + $("#selectedStdVarId").val() + "' />";
		newRow = newRow + "<input type='text' class='addVariables' id='selectedVariables"+ ctr + ".name' " +  
			"name='selectedVariables["+ ctr + "].name' value='" + $("#selectedName").val() + "' /></td>";
		newRow = newRow + "<td class='"+className+"'>" + $("#selectedProperty").text() + "</td>";
		newRow = newRow + "<td class='"+className+"'>" + $("#selectedScale").text() + "</td>";
		newRow = newRow + "<td class='"+className+"'>" + $("#selectedMethod").text() + "</td>";
		newRow = newRow + "<td class='"+className+"'>" + $("#selectedRole").text() + "</td>";
		newRow = newRow + "</tr>";
		
		$("#newVariablesList").append(newRow);
		$("#page-message-modal").html("");
	} else {
		$("#page-message-modal").html(
			    "<div class='alert alert-danger'>"+ varInListMessage +"</div>"
		);
	}
}
	
function notInList(id) {
	var isNotInList = true;
	$.each($("#newVariablesList tbody tr"), function() {
		if ($(this).find("input[type='hidden']").val() == id) {
			isNotInList = false;
		}
	});
	return isNotInList;
}

function submitSelectedVariables(variableType) {
	if ($("#newVariablesList tbody tr").length > 0) {
		
		var serializedData = $("input.addVariables").serialize();
		$("#page-message-modal").html("");
		Spinner.toggle();
		
		$.ajax({
			url: "/Fieldbook/NurseryManager/manageNurserySettings/addSettings/" + variableType,
			type: "POST",
			data: serializedData,
			success: function (data) {
				switch (variableType) {
					case 1:
						createNurseryLevelSettingVariables($.parseJSON(data));
						break;
					case 2:
						createPlotLevelSettingVariables($.parseJSON(data));
						break;
					case 3:
						createBaselineTraitVariables($.parseJSON(data));
						break;
					default:
						createNurseryLevelSettingVariables($.parseJSON(data));
				}
				
			},
			error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: " + textStatus, errorThrown); 
			},
			complete: function() {
				Spinner.toggle();
				$("#addVariablesSettingModal").modal("hide");
			}
		});
	} else {
		$("#page-message-modal").html(
			    "<div class='alert alert-danger'>"+ varInListMessage +"</div>"
		);
	}
}

function getLastRowIndex(name, hasTBody) {
	if (hasTBody) {
		return $("#" + name + " tbody tr").length - 1;
	} else {
		return $("#" + name + " tr").length - 1;
	}
}

function createNurseryLevelSettingVariables(data) {
	var ctr = getLastRowIndex("nurseryLevelSettings", false) + 1;
	$.each(data, function (index, settingDetail) {
		var newRow = "<tr class=''>";
		var isDelete = "";
		
		//include delete button if variable is deletable
		if (settingDetail.delete) {
			isDelete = "<span style='cursor: default; font-size: 16px;' class='glyphicon glyphicon-remove-circle' onclick='deleteVariable(1," + 
				settingDetail.variable.cvTermId + ",$(this))'></span>";
		}
		
		//create html elements dynamically
		newRow = newRow + "<td class='nurseryLevelVariableSetting'>" + isDelete + 
		"<input type='hidden' id='nurseryLevelVariables" + ctr + ".variable.cvTermId' name='nurseryLevelVariables[" + 
		ctr + "].variable.cvTermId' value='" + settingDetail.variable.cvTermId + "' />" + 
		"</td>";
		//newRow = newRow + "<td>" + settingDetail.variable.name + ':' + '<span class="required">*</span>' +  "</td>";
		newRow = newRow + "<td class='nurseryLevelVariableSetting'><label class='control-label'>" + settingDetail.variable.name + '</label>:' + '' +  "</td>";
		newRow = newRow + "<td>";
		/*
		newRow = newRow + "<input type='hidden' id='nurseryLevelVariables" + ctr + 
		".value' name='nurseryLevelVariables[" + ctr + "].value' class='form-control select2' />";
		*/
		var inputHtml = '';
				
		if(settingDetail.variable.widgetType == 'DROPDOWN'){
			inputHtml = createDropdownInput(ctr);
		}else if(settingDetail.variable.widgetType == 'DATE'){
			inputHtml = createDateInput(ctr);
		}else if(settingDetail.variable.widgetType == 'CTEXT'){
			inputHtml = createCharacterTextInput(ctr);
		}else if(settingDetail.variable.widgetType == 'NTEXT'){
			inputHtml = createNumericalTextInput(ctr);
		}else if(settingDetail.variable.widgetType == 'SLIDER'){
			inputHtml = createSliderInput(ctr, settingDetail.variable.minRange, settingDetail.variable.maxRange);
		}
		newRow = newRow + inputHtml;
		
		if (settingDetail.variable.cvTermId == breedingMethodId) {
			//show favorite method
			newRow = newRow + "<div class='possibleValuesDiv'><input type='checkbox' id='nurseryLevelVariables" + ctr + ".favorite1'" + 
			" name='nurseryLevelVariables[" + ctr + "].favorite'" +
			" onclick='javascript: toggleMethodDropdown(" + ctr + ");' />" +
			"<input type='hidden' name='_nurseryLevelVariables[" + ctr + "].favorite' value='on' /> " +
			"<span>&nbsp;&nbsp;" + showFavoriteMethodLabel + "</span></div>" + 
			"<div id='possibleValuesJson" + ctr + "' class='possibleValuesJson' style='display:none'>" + settingDetail.possibleValuesJson + 
			"</div><div id='possibleValuesFavoriteJson" + ctr + "' class='possibleValuesFavoriteJson' style='display:none'>" + 
			settingDetail.possibleValuesFavoriteJson + "</div>";
			
			newRow = newRow + "</td><td class='nurseryLevelVariableSetting'>";
			newRow = newRow + "&nbsp;&nbsp;<a href='javascript: openManageMethods();'>" + manageMethodLabel + "</a>";
			newRow = newRow + "</td></tr>";
			
		} else {
			newRow = newRow + "</td><td></td></tr>";
		}

		$("#nurseryLevelSettings").append(newRow);
		
		if(settingDetail.variable.widgetType == 'DROPDOWN'){
			//initialize select 2 combo
			initializePossibleValuesCombo(settingDetail.possibleValues, "#" + 
					getJquerySafeId("nurseryLevelVariables" + ctr + ".value"), false, null);
		}
		ctr++;
	});
	
	initializeDateAndSliderInputs();
}

function toggleMethodDropdown(rowIndex) {
	var possibleValues;  
	var showFavorite = $("#" + getJquerySafeId("nurseryLevelVariables" + rowIndex + ".favorite1")).is(":checked");
	var selectedVal;
	
	//get previously selected value
	if ($("#" + getJquerySafeId("nurseryLevelVariables" + rowIndex + ".value")).select2("data")) {
		selectedVal = $("#" + getJquerySafeId("nurseryLevelVariables" + rowIndex + ".value")).select2("data").id;
	}
	
	//reset select2 combo
	initializePossibleValuesCombo([], "#" + 
			getJquerySafeId("nurseryLevelVariables" + rowIndex + ".value"), false, null);
	
	//get possible values based on checkbox
	if (showFavorite) {
		possibleValues = $("#possibleValuesFavoriteJson" + rowIndex).text();
	} else {
		possibleValues = $("#possibleValuesJson" + rowIndex).text();
	}
	
	//recreate select2 combo
	initializePossibleValuesCombo($.parseJSON(possibleValues), "#" + 
			getJquerySafeId("nurseryLevelVariables" + rowIndex + ".value"), false, selectedVal);
}

function createPlotLevelSettingVariables(data) {
	$.each(data, function (index, settingDetail) {
		var length = $("#plotLevelSettings tbody tr").length + 1;
		var className = length % 2 == 1 ? 'even' : 'odd';
		var newRow = "<tr>";
		var isDelete = "";
		
		if (settingDetail.delete) {
			isDelete = "<span style='cursor: default; font-size: 16px;' class='glyphicon glyphicon-remove-circle' onclick='deleteVariable(2," + 
			settingDetail.variable.cvTermId + ",$(this))'></span>";
		}
		newRow = newRow + "<td style='text-align: center' class='"+className+"'>" + isDelete + 
		"<input type='hidden' id='plotLevelVariables" + (length-1) + ".variable.cvTermId' name='plotLevelVariables[" + 
		(length-1) + "].variable.cvTermId' value='" + settingDetail.variable.cvTermId + "' />" + 
		"</td>";
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.name + "</td>"; 
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.description + "</td></tr>";
		$("#plotLevelSettings").append(newRow);
	});
}

function createBaselineTraitVariables(data) {
	$.each(data, function (index, settingDetail) {
		var length = $("#baselineTraitSettings tbody tr").length + 1;
		var className = length % 2 == 1 ? 'even' : 'odd';
		var newRow = "<tr>";
		var isDelete = "";
		
		if (settingDetail.delete) {
			isDelete = "<span style='cursor: default; font-size: 16px;' class='glyphicon glyphicon-remove-circle' onclick='deleteVariable(3," + 
			settingDetail.variable.cvTermId + ",$(this))'></span>";
		}
		
		newRow = newRow + "<td style='text-align: center' class='"+className+"'>" + isDelete + 
		"<input type='hidden' id='baselineTraitVariables" + (length-1) + ".variable.cvTermId' name='baselineTraitVariables[" + 
		(length-1) + "].variable.cvTermId' value='" + settingDetail.variable.cvTermId + "' />" + 
		"</td>";
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.name + "</td>";		
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.description + "</td>"
		newRow = newRow + "<td class='"+className+"'>" + "<a href='javascript: void(0);' onclick='javascript:showBaselineTraitDetailsModal(" + 
		settingDetail.variable.cvTermId + ");'><span class='glyphicon glyphicon-eye-open'></span></a></td></tr>";
		$("#baselineTraitSettings").append(newRow);
	});
}

function sortByKey(array, key) {
    return array.sort(function(a, b) {
        var x = a[key].toLowerCase(); var y = b[key].toLowerCase();
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    });
}

function initializePossibleValuesCombo(possibleValues, name, isLocation, defaultValue) {
	var possibleValues_obj = [];
	var defaultJsonVal = null;

	$.each(possibleValues, function(index, value) {
		var jsonVal;
		if (value.id != undefined) {
			jsonVal = { 'id' : value.key,
					  'text' : value.name
				};
		} else if (value.locid != undefined){
			jsonVal = { 'id' : value.locid,
					  'text' : value.lname
				};
		} else {
			jsonVal = { 'id' : value.mid,
					  'text' : value.mname
				};
		}
		
		possibleValues_obj.push(jsonVal);  
		if(defaultValue != null && defaultValue != '' && 
				((defaultValue == value.key) || (defaultValue == value.locid) || (defaultValue == value.mid))){
			defaultJsonVal = jsonVal;
		}
		
	});
	
	possibleValues_obj = sortByKey(possibleValues_obj, "text");

	if (isLocation) {
		$(name).select2({
			minimumInputLength: 2,
			query: function (query) {	
		      var data = {results: possibleValues_obj}, i, j, s;
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index) {
		        return ($.fn.select2.defaults.matcher(query.term,item.text));
		      
		      });
		      if (data.results.length === 0){
		    	  data.results.unshift({id:query.term,text:query.term});	        	 
		      }
		      
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
		      if (data.results.length === 0){
		    	  data.results.unshift({id:query.term,text:query.term});	        	 
		      }
		      
		        query.callback(data);
		    }
	    });
	}
	
	if(defaultJsonVal != null){
		//console.log(defaultValue);
		//console.log(defaultJsonVal);
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}

function deleteVariable(variableType, variableId, deleteButton) {
	//remove row from UI
	deleteButton.parent().parent().remove();

	//remove row from session
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/NurseryManager/manageNurserySettings/deleteVariable/" + variableType + "/" + variableId,
		cache: false,
		type: "POST",
		success: function() {
			Spinner.toggle();
		}
	});
	
	//reinstantiate counters of ids and names
	sortVariableIdsAndNames(variableType);
}

function sortVariableIdsAndNames(variableType) {
	switch (variableType) {
	case 1:
		var reg = new RegExp("nurseryLevelVariables[0-9]+", "g");
		var reg2 = new RegExp("nurseryLevelVariables\[[0-9]+\]", "g");
		$.each($("#nurseryLevelSettings tr"), function (index, row) {
			//get the possible values of the variable
			var possibleValuesJson = $($(row).children("td:nth-child(3)").children(".possibleValuesJson")).text();
						
			//get currently selected value
			var selectedVal = null;
			var oldSelect2 = row.innerHTML.match(reg)[0];
		    if ($("#" + getJquerySafeId(oldSelect2 + ".value")).select2("data")) {
			   selectedVal = $("#" + getJquerySafeId(oldSelect2 + ".value")).select2("data").id;
		    }
		    
		    //change the ids and names of the objects and delete the existing select2 object
		    row.innerHTML = row.innerHTML.replace(reg, "nurseryLevelVariables" + index);
			row.innerHTML = row.innerHTML.replace(reg2, "nurseryLevelVariables[" + index + "]");
			$($(row).children("td:nth-child(3)")).html("<input type='hidden' id='nurseryLevelVariables" + index + 
				".value' name='nurseryLevelVariables[" + index + "].value' class='form-control select2' />");

			//recreate the select2 object
			if (index == 0) {
			    initializePossibleValuesCombo($.parseJSON(possibleValuesJson), 
				 			"#" + getJquerySafeId("nurseryLevelVariables" + index + ".value"), true, selectedVal);
			} else {
				initializePossibleValuesCombo($.parseJSON(possibleValuesJson), 
			 			"#" + getJquerySafeId("nurseryLevelVariables" + index + ".value"), false, selectedVal);
			}
		});
		break;
	case 2:
		var reg = new RegExp("plotLevelVariables[0-9]+", "g")
		var reg2 = new RegExp("plotLevelVariables\[[0-9]+\]", "g")
		$.each($("#plotLevelSettings tbody tr"), function (index, row) {
			row.innerHTML = row.innerHTML.replace(reg, "plotLevelVariables" + index);
			row.innerHTML = row.innerHTML.replace(reg2, "plotLevelVariables[" + index + "]");
		});
		break;
	default:
		var reg = new RegExp("baselineTraitVariables[0-9]+", "g")
		var reg2 = new RegExp("baselineTraitVariables\[[0-9]+\]", "g")
		$.each($("#baselineTraitSettings tbody tr"), function (index, row) {
			row.innerHTML = row.innerHTML.replace(reg, "baselineTraitVariables" + index);
			row.innerHTML = row.innerHTML.replace(reg2, "baselineTraitVariables[" + index + "]");
		});
	}
}

function hideDeleteConfirmation(){
	$('#delete-settings-confirmation').modal('hide');
}
function deleteNurserySettings(){
	var templateSettingsId = $('#selectedSettingId').val();
	if(templateSettingsId > 0){
		$('#delete-settings-confirmation').modal('hide');
		//doAjaxMainSubmit('page-message', deleteTemplateSettingSuccess, "/Fieldbook/NurseryManager/manageNurserySettings/delete/" + templateSettingsId);
		
		Spinner.toggle();
		
		$.ajax({
			url: "/Fieldbook/NurseryManager/manageNurserySettings/delete/" + templateSettingsId,
			type: "POST", 	
			cache: false,
			success: function (html) {
				//we just paste the whole html
				$('.container .row').first().html(html);
				
			    showSuccessfulMessage('page-message', deleteTemplateSettingSuccess);				
				Spinner.toggle();
			}
		});
			
	}else{
		alert('show error mesage');
	}
}
function clearSettings(){
	var templateSettingsId = $('#selectedSettingId').val();
	//window.location.hash = "/Fieldbook/NurseryManager/manageNurserySettings/clearSettings/"+templateSettingsId+"?t=";
	
	Spinner.toggle();	
		$.ajax({
			url: "/Fieldbook/NurseryManager/manageNurserySettings/clearSettings/"+templateSettingsId,
			type: "GET", 	
			cache: false,
			success: function (html) {
				//we just paste the whole html
				$('.container .row').first().html(html);				
				Spinner.toggle();
			}
		});
		
}
function loadNurserySettings(templateSettingsId) {
//alert($("#newVariablesList").html());
//var serializedData = $("#saveAdvanceNurseryForm").serialize();
	//window.location.hash = "/Fieldbook/NurseryManager/manageNurserySettings/view/"+templateSettingsId;
	
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/NurseryManager/manageNurserySettings/view/" + templateSettingsId,
		type: "GET", 	
		cache: false,				
		success: function (html) {
			//we just paste the whole html
			$('.container .row').first().html(html);
		},
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}
function addNewSettings(){
	//window.location.hash = "/Fieldbook/NurseryManager/manageNurserySettings/addNewSettings";
	
	Spinner.toggle();
	
	$.ajax({
		url: "/Fieldbook/NurseryManager/manageNurserySettings/addNewSettings",
		type: "GET",
		cache: false,
		success: function (html) {
			//we just paste the whole html
			$('.container .row').first().html(html);				
			Spinner.toggle();
		}
	});
	
}
function copySettings() {
	var templateSettingsId = $('#selectedSettingId').val();
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/NurseryManager/manageNurserySettings/copy/" + templateSettingsId,
		type: "GET", 	
		cache: false,				
		success: function (html) {
			//we just paste the whole html
			$('.container .row').first().html(html);			
			showSuccessfulMessage('page-message', copyTemplateSettingSuccess);	
			Spinner.toggle();
		}
	});
}
function hasDuplicateSettingName(){
	var selectedSettingsId = $('#selectedSettingId').val();
	var settingsName = $('#settingName').val() ;
	var hasDuplicate = false;
	$('#selectedSettingId option').each(function(){
	    if(selectedSettingsId != $(this).val() &&  $(this).html().trim() == settingsName)
	    	hasDuplicate = true;
	})
	return hasDuplicate;
}
function hasEmptyNurseryValue(){
	//would only check for the data numeric
	/*
	var hasEmpty = false;
	$.each($("#nurseryLevelSettings tbody tr"), function(index, row) {
		var input = $($(row).children("td:nth-child(3)").children("#" + getJquerySafeId("nurseryLevelVariables"+index+".value")));
		if(input.hasClass('select2') && input.select2("data") == null) {
			hasEmpty = true;
		}else if(input.hasClass('numeric-input')){
			console.log('numeric');
		}else if(input.hasClass('numeric-range-input')){
			console.log('numeric range');
		}else if(input.hasClass('character-input')){
			console.log('character');
		}else if(input.hasClass('date-input')){
			console.log('date');
		}
	})
	return hasEmpty;
	*/
	var hasError = false;
	var name = '';
	$('.numeric-input').each(function(){
		$(this).val($.trim($(this).val()));
		if(hasError == false && $(this).val() != '' && isNaN($(this).val())){
			hasError = true;
			name = $(this).parent().parent().find('.control-label').html();
			
		}
	});
	if(hasError){
		showErrorMessage('page-message', name + " " + nurseryNumericError);
	}
		
	return hasError;
	
}
function doSaveSettings(){
	$('#settingName').val($('#settingName').val().trim());
	if($('#settingName').val() == ''){
		showErrorMessage('page-message', templateSettingNameError);
	return false;
	}else if(hasDuplicateSettingName()){
		showErrorMessage('page-message', templateSettingNameErrorUnique);
	return false;
	} else if(hasEmptyNurseryValue()){
		//showErrorMessage('page-message', nurseryLevelValueEmpty);
		return false;
	} else{ 		
		doAjaxMainSubmit('page-message', saveTemplateSettingSuccess, null);
		/*
	Spinner.toggle();
	var $form = $("#saveNurserySettingsForm");
	serializedData = $form.serialize();
	
		$.ajax({
			url: "/Fieldbook/NurseryManager/manageNurserySettings/save/",
			type: "POST", 	
			data: serializedData,
			success: function (html) {
				//we just paste the whole html
				$('.container .row').first().html(html);
				
			    showSuccessfulMessage('page-message', saveTemplateSettingSuccess);				
				Spinner.toggle();
			}
		}); 
		*/
	}
				
}

function showBaselineTraitDetailsModal(id) {
	if(id != ''){
		Spinner.toggle();
		$.ajax({
			url: "/Fieldbook/NurseryManager/manageNurserySettings/showVariableDetails/" + id,
			type: "GET",
			cache: false,
			success: function (data) {
				populateBaselineTraits($.parseJSON(data));
				$("#baselineTraitDetails").modal("toggle");
			},
			error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: " + textStatus, errorThrown); 
			},
			complete: function() {
				Spinner.toggle();
			}
		});
	}
}

function populateBaselineTraits(standardVariable) {
	if (standardVariable != null) {
		$("#traitClass").text(checkIfNull(standardVariable.traitClass));
		$("#property").text(checkIfNull(standardVariable.property));
		$("#method").text(checkIfNull(standardVariable.method));
		$("#scale").text(checkIfNull(standardVariable.scale));
		$("#dataType").text(checkIfNull(standardVariable.dataType));
		$("#role").text(checkIfNull(standardVariable.role));
		$("#cropOntologyId").text(checkIfNull(standardVariable.cropOntologyId));
	} else {
		$("#traitClass").text("");
		$("#property").text("");
		$("#method").text("");
		$("#scale").text("");
		$("#dataType").text("");
		$("#role").text("");
		$("#cropOntologyId").text("");
	}
}

function checkIfNull(object) {
	if (object != null) {
		return object;
	} else {
		return "";
	}
}

function createSliderInput(ctr, minVal, maxVal){
	/*
	return "<input type='text' id='nurseryLevelVariables" + ctr + 
	".value' name='nurseryLevelVariables[" + ctr + "].value' class='form-control slider-input'" +
			" data-slider-min='"+minVal+"' data-slider-max='"+maxVal+"'" + 
				 " data-slider-step='1' data-slider-value='" +minVal+"'" +
					" data-slider-orientation='horizontal' data-slider-selection='after'" + 
						" data-slider-tooltip='always' />";
	*/
	/*
	 * <input th:if="${nurseryLevelVariable.variable.widgetType.type == 'SLIDER'}"  
										type="range" 
										th:field="*{nurseryLevelVariables[__${rowStat.index}__].value}"
										min="0" max="1" value="1" step=".05"
										class="form-control numeric-range-input"/>
	 */
	return "<input data-slider-orientation='horizontal' data-slider-selection='after' type='text' data-step='0.1' data-min='"+minVal+"' data-max='"+maxVal+"' id='nurseryLevelVariables" + ctr + 
	".value' name='nurseryLevelVariables[" + ctr + "].value' class='form-control numeric-range-input' />";
}
function createDropdownInput(ctr){
	 return "<input type='hidden' id='nurseryLevelVariables" + ctr + 
		".value' name='nurseryLevelVariables[" + ctr + "].value' class='form-control select2' />";
}
function createDateInput(ctr){	
	 return "<input type='text' id='nurseryLevelVariables" + ctr + 
		".value' name='nurseryLevelVariables[" + ctr + "].value' class='form-control date-input' />";
	 
}
function createNumericalTextInput(ctr){
	return "<input type='text' id='nurseryLevelVariables" + ctr + 
	".value' name='nurseryLevelVariables[" + ctr + "].value' class='form-control numeric-input' />";
}
function createCharacterTextInput(ctr){
	return "<input type='text' id='nurseryLevelVariables" + ctr + 
	".value' name='nurseryLevelVariables[" + ctr + "].value' class='form-control character-input' />";

}
function initializeDateAndSliderInputs(){
	if($('.date-input').length > 0){
		$('.date-input').each(function(){
			$(this).datepicker({'format': 'yyyymmdd'}).on('changeDate', function(ev) {
		
			$(this).datepicker('hide');
		})
		});
	}
	
	if($('.numeric-range-input').length > 0){
		
		$('.numeric-range-input').each(function(){
		console.log($(this).val());
		console.log(parseFloat($(this).data('min')));
		console.log(parseFloat($(this).val()));
		var currentVal  = $(this).val() == '' ? parseFloat($(this).data('min')) : parseFloat($(this).val());
		console.log(currentVal);
			$(this).slider({
				min: parseFloat($(this).data('min')),
				max: parseFloat($(this).data('max')),
				step: parseFloat($(this).data('step')),
				value: currentVal,
				formater: function(value) {
					return 'Value: ' + value;
				}
			});
		});
	}				
}

