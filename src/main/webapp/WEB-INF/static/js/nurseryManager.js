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
			clearAttributeFields();
			
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
	stdVariableSuggestions = variables;
	$.each(stdVariableSuggestions, function(index, value) {
		stdVariableSuggestions_obj.push({ 'id' : value.id,
			  'text' : value.name
		});  
	});

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
    }).on("change", function (){
    	//set attribute values
    	getStandardVariableDetails($("#stdVarSearch").select2("data").id);
    });
}

function getStandardVariableDetails(id) {
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/NurseryManager/manageNurserySettings/showVariableDetails/" + id,
		type: "GET",
		cache: false,
		success: function (data) {
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
		
function populateAttributeFields(data) {
	$("#selectedTraitClass").text(data.traitClass);
	$("#selectedProperty").text(data.property);
	$("#selectedMethod").text(data.method);
	$("#selectedScale").text(data.scale);
	$("#selectedDataType").text(data.dataType);
	$("#selectedRole").text(data.role);
	$("#selectedCropOntologyId").text(data.cropOntologyId);
	$("#selectedStdVarId").val(data.cvTermId);
	$("#selectedName").val(data.name);
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
		ctr = 1; 
	} else {
		var lastVarId = $("#newVariablesList tbody tr:last-child td input[type='hidden']").attr("id");
		ctr = parseInt(lastVarId.substring(lastVarId.indexOf("[") + 1, lastVarId.indexOf("]"))) + 1;
	}

	//if selected variable is not yet in the list and is not blank or new, add it
	if (notInList($("#selectedStdVarId").val()) && $("#selectedStdVarId").val() != "") {
		newRow = "<tr>";
		newRow = newRow + "<td><input type='hidden' id='selectedVariables["+ ctr + "].cvTermId' " + 
			"name='selectedVariables["+ ctr + "].cvTermId' value='" + $("#selectedStdVarId").val() + "' />";
		newRow = newRow + "<input type='text' id='selectedVariables["+ ctr + "].name' " + 
			"name='selectedVariables["+ ctr + "].name' value='" + $("#selectedName").val() + "' /></td>";
		newRow = newRow + "<td>" + $("#selectedProperty").text() + "</td>";
		newRow = newRow + "<td>" + $("#selectedScale").text() + "</td>";
		newRow = newRow + "<td>" + $("#selectedMethod").text() + "</td>";
		newRow = newRow + "<td>" + $("#selectedRole").text() + "</td>";
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