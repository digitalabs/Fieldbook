function doSearchTree(){
	var result = searchOntologyTreeNodeWithName(treeDivId, $('#srch-term').val());
	//var res = $("#"+treeDivId+" a.dynatree-title:contains('"++"')");
	$("#page-message").html("");
	if(result == null){   		    	       	    
    	$("#page-message").html("<div class='alert alert-danger'>"+ seasrchErrorMessage +"</div>");
	}else{
		console.log(result);
		//$("#"+treeDivId).dynatree("getTree").activateKey(result.data.key);
		//doOntologyTreeHighlight(treeDivId, result.data.key);
	}
} 


function doOntologyTreeHighlight(treeName, nodeKey){
	//console.log(nodeKey);
	$("#"+treeName).dynatree("getTree").activateKey(nodeKey);
	$('#'+treeName).find("*").removeClass('highlight');
	//then we highlight the nodeKey and its parents
	var elem = nodeKey.split("_");
	var count = 0;
	var key = "";
	var standardVariableKey = "";
	for(count = 0 ; count < elem.length ; count++){
		if(key != '')
			key = key + "_";
		
		key = key + elem[count];
		//console.log("Highlight: " + key);
		$('.'+key).addClass('highlight');
	}
	
	var node = $("#"+treeName).dynatree("getTree").getNodeByKey(nodeKey);
	
	//console.log(nodeKey)
	
	if(node.data.lastChildren == true){
		
	//if(elem.length == 3){
		//call ajax
		standardVariableKey = elem[elem.length-1];
		//console.log('im here' + standardVariableKey);
		//alert("Do the ajax call now with standard variable id " + standardVariableKey);
		processTab(node.data.title, standardVariableKey);
	}else{
		clearAndAppendOntologyDetailsTab('', '');
	}
}

function searchOntologyTreeNodeWithName(treeName, name) {
    if (name == null) {
        return null;
    }

    
    var searchFrom = $('#'+treeName).dynatree("getRoot");
    

    var match = null;

    searchFrom.visit(function (node) {
    	if(node.data.includeInSearch == true){
	        if (node.data.title.toUpperCase().indexOf(name.toUpperCase()) != -1) {
	        	if(match == null){
	        		match = new Array();
	        	}
	            match[match.length] = node;
	            //return false; // Break if found
	        }
	    }
    });
    return match;
    
    	
};

function displayOntologyTree(treeName, treeData, searchTreeData, searchDivId){
	//for triggering the start of search type ahead
	
	var searchTypeAhead = $('#'+searchDivId).typeahead({
   	  name: 'OntologyBrowserSearchTree', 
   	  local:  $.parseJSON(searchTreeData),
   	  limit : 20,   	
   	  template: '<p><strong>{{value}}</strong> ({{type}}) <br /> {{parentTitle}}</p>',
   	  engine: Hogan,   	
   	});
   	
   	searchTypeAhead.on('typeahead:selected',function(evt,data){   	    
   	    doOntologyTreeHighlight(treeDivId, data.key);
   	    return false;
   	    });
   	searchTypeAhead.on('typeahead:autocomplete',function(evt,data){   	    
   	    doOntologyTreeHighlight(treeDivId, data.key);
   	    return false;
   	    });
   	
	
	var json = $.parseJSON(treeData);
	
	$("#" + treeName).dynatree({
	      checkbox: false,
	      // Override class name for checkbox icon:
	      classNames: {
				container: "fbtree-container",
				expander: "fbtree-expander",
				nodeIcon: "fbtree-icon",
				combinedIconPrefix: "fbtree-ico-",
				focused: "fbtree-focused",
				active: "fbtree-active"
			},
	      selectMode: 1,
	      children: json,
	      onActivate: function(node) {
	        //alert("onActivate" + node.data.title);
	     // Display list of selected nodes
	        var selNodes = node.tree.getSelectedNodes();
	        // convert to title/key array
	        var selKeys = $.map(selNodes, function(node){
	             return "[" + node.data.key + "]: '" + node.data.title + "'";
	        });
	        //alert(selKeys.join(", "));
	        //alert(selNodes);
	        /*
	        if(node.data.isLastChildren == true){
	        	alert("Trigger Ajax 1");
	        	//$('.'+node.data.key).addClass("highlight");
	        	
	        }
	        console.log(node.data.key);
	        */
	        //$('.fbtree-focused').addClass("highlight");
	        doOntologyTreeHighlight(treeName, node.data.key);
	      },
	      onSelect: function(select, node) {
	        // Display list of selected nodes
	    	  /*
	        var s = node.tree.getSelectedNodes().join(", ");
	        //alert("onSelect" + s);
	        
	        if(node.data.lastChildren == true){
	        	alert("Trigger Ajax 2");
	        	//$('.'+node.data.key).addClass("highlight");
	        	
	        	
	        }
	        */
	        //$('.fbtree-focused').addClass("highlight");
	        doOntologyTreeHighlight(treeName, node.data.key);
	      },
	      onDblClick: function(node, event) {
	        node.toggleSelect();
	      },
	      onKeydown: function(node, event) {
	        if( event.which == 32 ) {
	          node.toggleSelect();
	          return false;
	        }
	      },
	    });

}

//Tab functions
function processTab(variableName, variableId) {
	viewTabs(variableName, variableId);
}

function showSelectedTab(selectedTabName) {
	$("#ontology-tab-headers").show();
	var tabs = $("#ontology-tabs").children();
	for (var i = 0; i < tabs.length; i++) {
		if (tabs[i].id == selectedTabName) {
			$("#" + tabs[i].id + "-li").addClass("active");
			$("#" + tabs[i].id).show();
		} else {
			$("#" + tabs[i].id + "-li").removeClass("active");
			$("#" + tabs[i].id).hide();
		}
	}
}

function clearAndAppendOntologyDetailsTab(variableName, html){
	if(html != ''){
		$("#ontology-detail-tabs").empty().append(html);		
		var varDetails = variableDetailHeader + " " + variableName;
		$("#variable-details").html(varDetails);
	}		
	else{
		$("#ontology-detail-tabs").empty();
		$("#variable-details").html('');
		
	}
		
}

function viewTabs(variableName, variableId) {
	Spinner.toggle();
	$.ajax({
		url: ontologyUrl + "details/" + variableId,
		type: "get",
		//dataType: "json",
		success: function(html) {
			clearAndAppendOntologyDetailsTab(variableName, html);			
		},
		error: function(jqXHR, textStatus, errorThrown){ 
			console.log("The following error occured: " + textStatus, errorThrown); 
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}

//save function for adding ontology
function doSave(combo) {
	if (validateCombo(combo)) {
		//get form data
		var $form = $("#addVariableForm");
		serializedData = $form.serialize();
		
		Spinner.toggle();
		
		$.ajax({
			url: ontologyUrl + "addVariable/" + combo,
			type: "post",
			dataType: "json",
			data: serializedData,
		    success: function(data){
			    if (data.status == "1") {
			    	//add the newly inserted ontology in its corresponding dropdown
			    	recreateCombo(combo, data);	
			    	showSuccessMessage(data.successMessage);
			    	
			    	if(data.addedNewTrait == "1"){
			    		//we need to recreate the combo for the traitClass			    		
			    		var newData = {id:data.traitId, name:data.traitName, definition:data.traitDefinition};
			    		recreateCombo('TraitClass', newData);
			    		
			    	}
			    	//$("#comboTraitClass").select2('data', dataVal).trigger('change');
		       	} else {
		       		showMessage(data.errorMessage);
		       	}
		   }, 
		   error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: " + textStatus, errorThrown);
		   }, 
		   complete: function(){ 
			   Spinner.toggle();
		   } 
		});
		
		$("#page-message-modal").html("");
	}
}

function isInt(value) { 
    return !isNaN(parseInt(value,10)) && (parseFloat(value,10) == parseInt(value,10)); 
}

function setCorrespondingTraitClass(propertyId){
	//console.log(propertyId);
	var dataVal = {id:'',text:'',description:''}; //default value
	if(isInt(propertyId)){
		Spinner.toggle();
		$.ajax({
			url: ontologyUrl+"retrieve/trait/property/" + propertyId,
			type: "GET",
			dataType: "json",
			data: "",
		    success: function(data){
			    if (data.status == "1") {
			    	
			    	if(data.traitId != ''){
			    		var count = 0;
				    	for(count = 0 ; count < traitClassesSuggestions_obj.length ; count++){
				    		if(traitClassesSuggestions_obj[count].id == data.traitId){
				    			//console.log(traitClassesSuggestions_obj[count]);
				    			//$("#comboTraitClass").val(traitClassesSuggestions_obj[count]);
				    			dataVal = traitClassesSuggestions_obj[count];			    			
				    			break;
				    		}			    			
				    	}
			    	}			    	
			    	$("#comboTraitClass").select2('data', dataVal).trigger('change');
			    	
		       	}
			    Spinner.toggle();
		   }
		   
		});
	}else{
		//$("#comboTraitClass").select2('data', dataVal).trigger('change');
	}
	
	
}

function getOntologySuffix(id){
	return (id > -1 ? " (Shared)" : ""); 
}

//function to create the select2 combos
function initializeVariable(variableSuggestions, variableSuggestions_obj, description, name, allowTypedValues) {
	
	if (name.indexOf('TraitClass') > -1 && variableSuggestions_obj.length == 1 || variableSuggestions_obj.length == 0) {
		//initialize the arrays that would contain json data for the combos
		if (description == "description") {
			$.each(variableSuggestions, function( index, value ) {
				variableSuggestions_obj.push({ 'id' : value.id,
					  'text' : value.name + getOntologySuffix(value.id),
					  'description' : value.description
				});  
		  		
			});
		} else if (name == "Property"){		
			$.each(variableSuggestions, function( index, value ) {
				variableSuggestions_obj.push({ 'id' : value.id,
					  'text' : value.name + getOntologySuffix(value.id),
					  'description' : value.definition,
					  'traitId' : value.isAId
				});  
		  		
			});
		} else {
			$.each(variableSuggestions, function( index, value ) {
				variableSuggestions_obj.push({ 'id' : value.id,
					  'text' : value.name + getOntologySuffix(value.id),
					  'description' : value.definition
				});  
		  		
			});
		}
	}
	//create the select2 combo
	//if combo to create is the variable name, add an onchange event to fill up all the fields of the selected variable
	if (name == "VariableName") {
		$("#combo" + name).select2({
	        query: function (query) {	
	          var data = {results: sortByKey(variableSuggestions_obj, "text")}, i, j, s;
	          // return the array that matches
	          data.results = $.grep(data.results,function(item,index) {
	            return ($.fn.select2.defaults.matcher(query.term,item.text));
	          
	          });
	          if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
	          
	            query.callback(data);
	        }
	
	    }).on("change", function(){
	    	getStandardVariableDetails($("#combo" + name).select2("data").id);
	    });
	} else {
		//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
		$("#combo" + name).select2({
	        query: function (query) {
	          var data = {results: sortByKey(variableSuggestions_obj, "text")}, i, j, s;
	          // return the array that matches
	          data.results = $.grep(data.results,function(item,index) {
	            return ($.fn.select2.defaults.matcher(query.term,item.text));
	          
	          });
	          if(allowTypedValues == true){
	        	  if (data.results.length === 0) 
	        		  data.results.unshift({id:query.term,text:query.term});  
	          }
	            
	          
	            query.callback(data);
	            
	        }
	
	    }).on("change", function(){
	    	$("#" + lowerCaseFirstLetter(name) + "Description").val($("#combo"+name).select2("data").description);
	    	if(name == 'TraitClass'){
	    		filterPropertyCombo(treeDivId, "comboTraitClass", "traitClassDescription", $("#comboTraitClass").select2("data").id, true);
	    	}
	    	if (name.match("^Manage")) {
	    		if ($("#combo"+name).select2("data").description) { //edit mode
			    	$("#" + lowerCaseFirstLetter(name) + "Id").val($("#combo"+name).select2("data").id);
			    	$("#" + lowerCaseFirstLetter(name) + "Name").val($("#combo"+name).select2("data").text.replace(" (Shared)", ""));
		    		$("#btnAdd" + name).hide();
		    		$("#btnUpdate" + name).show();
		    		$("#btnDelete" + name).show();
		    		$("#" + lowerCaseFirstLetter(name) + "NameText").html($("#combo"+name).select2("data").text.replace(" (Shared)", ""));
		    		
		    		//add the loading of the linked variables here
		    		if (allowTypedValues) {
		    			retrieveLinkedVariables(name, $("#combo"+name).select2("data").id);
		    		}
	    		} else { //add mode
		    		clearForm(lowerCaseFirstLetter(name) + "Form");
			    	$("#" + lowerCaseFirstLetter(name) + "Id").val('');
			    	$("#" + lowerCaseFirstLetter(name) + "Name").val($("#combo"+name).select2("data").id);
		    		$("#btnAdd" + name).show();
		    		$("#btnUpdate" + name).hide();
		    		$("#btnDelete" + name).hide();
		    		$("#" + lowerCaseFirstLetter(name) + "NameText").html($("#combo"+name).select2("data").id);
		    		$("#manageLinkedVariableList").html("");
	    		}
	    	}	    	
	    });
	}
}

function retrieveLinkedVariables(ontologyType, ontologyId){
	console.log(ontologyType + " = " + ontologyId);
	Spinner.toggle();
	$.ajax({
		url: ontologyUrl + "retrieve/linked/variable/" + ontologyType + "/"+ontologyId,
		type: "get",
		success: function(html) {
			$("#manageLinkedVariableList").empty().append(html);			
			Spinner.toggle();
		}
	});
	
}


function lowerCaseFirstLetter(string)
{
    return string.charAt(0).toLowerCase() + string.slice(1);
}

function loadOntologyCombos(){
	//create combos
	initializeVariable(variableNameSuggestions, variableNameSuggestions_obj, "description", "VariableName", true);
	
	traitClassesSuggestions_obj.push({ 'id' : 0,
		  'text' : "-- All --",
		  'description' : "All",
	});  
	
	initializeVariable(traitClassesSuggestions, traitClassesSuggestions_obj, "description", "TraitClass", false);
	initializeVariable(propertySuggestions, propertySuggestions_obj, "definition", "Property", false);
	initializeVariable(methodSuggestions, methodSuggestions_obj, "definition", "Method", false);
	initializeVariable(scaleSuggestions, scaleSuggestions_obj, "definition", "Scale", false);
}		

function clearFields() {
	$(".form-control").val("");
	$(".select2").select2("val", "");
	$("#page-message-modal").html("");
}

function recreateCombo(combo, data) {
	var suggestions_obj = [];
	var description = null;
	
	//add the new data in the collection
	if (combo == "TraitClass") {
		traitClassesSuggestions_obj.push({ 'id' : data.id,
			  'text' : data.name + getOntologySuffix(data.id),
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(traitClassesSuggestions_obj, "text");
	} else if (combo == "Property") {
		propertySuggestions_obj.push({ 'id' : data.id,
			  'text' : data.name + getOntologySuffix(data.id),
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(propertySuggestions_obj, "text");
	} else if (combo == "Method") {
		methodSuggestions_obj.push({ 'id' : data.id,
			  'text' : data.name + getOntologySuffix(data.id),
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(methodSuggestions_obj, "text");
	} else {
		scaleSuggestions_obj.push({ 'id' : data.id,
			  'text' : data.name + getOntologySuffix(data.id),
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(scaleSuggestions_obj, "text");
	}
	
	//set description field to empty
	description = $("#"+lowerCaseFirstLetter(combo)+"Description"); 
	description.val("");
	
	//recreate the dropdown
	$("#combo" + combo).select2({
			query: function (query) {
	              var data = {results: suggestions_obj}, i, j, s;
	              // return the array that matches
	              data.results = $.grep(data.results,function(item,index) {
	                return ($.fn.select2.defaults.matcher(query.term,item.text));
	              
	              });
	              
	              if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
	              
	                query.callback(data);
	                
	            }		
	});
	var newData = { 'id' : data.id,
			  'text' : data.name + getOntologySuffix(data.id),
			  'description' : data.definition
		}
	description.val(data.definition);
	$("#combo"+combo).select2('data', newData);//no need to trigger change.trigger('change');
}

//check if the selected item is an existing record 
function itemExists(combo) {
	return $("#combo"+combo).select2("data").id != $("#combo"+combo).select2("data").text && $("#combo"+combo).select2("data").description != undefined;
}

function showSuccessMessage(message) {
	$("#page-message").html(
		    "<div class='alert alert-success'>"+ message +"</div>"
	);
	setTimeout("hideSuccessMessage()", 3000);
}

function hideSuccessMessage(){
	$('#page-message .alert-success').fadeOut(1000);
}

function showMessage(message) {
	$("#page-message").html(
		    "<div class='alert alert-danger'>"+ message +"</div>"
	);
}

//check if any of the required fields is empty
function requiredFieldsEmpty() {
	return $("#variableName").val() == "" || $("#dataType").val() == "" || $("#role").val() == "" || 
	$("#comboTraitClass").val() == "" || $("#comboProperty").val() == "" || 
	$("#comboMethod").val() == "" || $("#comboScale").val() == "";
}

//check if the values selected in the combo is a new entry
function comboValuesInvalid() {	
	return ($("#comboTraitClass").select2("data").id == $("#comboTraitClass").select2("data").text && 
    		$("#comboTraitClass").select2("data").description == undefined) || 
    	   ($("#comboProperty").select2("data").id == $("#comboProperty").select2("data").text && 
			$("#comboProperty").select2("data").description == undefined) || 
		   ($("#comboMethod").select2("data").id == $("#comboMethod").select2("data").text && 
			$("#comboMethod").select2("data").description == undefined) || 
		   ($("#comboScale").select2("data").id == $("#comboScale").select2("data").text && 
			$("#comboScale").select2("data").description == undefined);
}

function sortByKey(array, key) {
    return array.sort(function(a, b) {
        var x = a[key].toLowerCase(); var y = b[key].toLowerCase();
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    });
}

function doTraitClassTreeHighlight(treeName, comboName, descriptionName, nodeKey){
	$("#"+treeName).dynatree("getTree").activateKey(nodeKey);
	$('#'+treeName).find("*").removeClass('highlight');
	//then we highlight the nodeKey and its parents
	var elem = nodeKey.split("_");
	var count = 0;
	var key = "";
	var traitClassId = ""
	for(count = 0 ; count < elem.length ; count++){
		if(key != '')
			key = key + "_";
		
		key = key + elem[count];
		$('.'+key).addClass('highlight');
	}
	
	var node = $("#"+treeName).dynatree("getTree").getNodeByKey(nodeKey);
	
	traitClassId = elem[elem.length-1];
	
	filterPropertyCombo(treeName, comboName, descriptionName, traitClassId, false);
	
}

function getNodeKeyFromTraitClass(traitClassId, treeName){

	var rootNode = $("#"+treeName).dynatree("getRoot");

	//console.log(rootNode.data.key);

	var children=rootNode.getChildren() ;
	var i = 0;
	var nodeKey = '';
	for(i=0;i<children.length;i++){
		//console.log("child key:"+children[i].data.key);
		nodeKey = getTreeChildren(children[i], traitClassId);
		if(nodeKey != ''){
			break;
		}
	}
	return nodeKey;
}
function getTreeChildren(child, traitClassId){
	//console.log("parent child key:"+child.data.key);
	var nodeKey = "";
	if( child.data.key.indexOf(traitClassId) != -1){
		console.log("FOUND");
		return child.data.key;
	}
	
	var children=child.getChildren();
	if(children != null){
		//console.log("Children Length:"+children.length);
		var i = 0;
		for(i=0;i<children.length;i++){
			//console.log("child key:"+children[i].data.key);	
			if(children[i].data.key.indexOf(traitClassId) != -1){
				//console.log("FOUND");
				return children[i].data.key;
			}
			if(children[i].getChildren() != null)
				nodeKey = getTreeChildren(children[i], traitClassId);
			
			if(nodeKey != ""){
				break;
			}
		}
	}
	return nodeKey;
}

function filterPropertyCombo(treeName, comboName, descriptionName, traitClassId, isFromDropDown){
	//console.log("Load property of trait class id: "+traitClassId);
	if(isFromDropDown){
		$('#'+treeName).find("*").removeClass('highlight');
		//if(traitClassId != 0){
			var nodeKey = getNodeKeyFromTraitClass(traitClassId, treeName);
			//console.log("Activate: "+ nodeKey);
			//console.log(json);
			//we need to highlight the tree
			$("#"+treeName).dynatree("getTree").activateKey(nodeKey);
			
			//then we highlight the nodeKey and its parents		
			
			if(nodeKey != ''){
				var elem = nodeKey.split("_");
				var count = 0;
				var key = "";
				for(count = 0 ; count < elem.length ; count++){
					if(key != '')
						key = key + "_";
					
					key = key + elem[count];
					$('.'+key).addClass('highlight');
				}
			}
		//}
		
		
		
	}else {
		var counter = 0;
		for(counter = 0 ; counter < traitClassesSuggestions_obj.length ; counter++){
			if(traitClassId == traitClassesSuggestions_obj[counter].id){
				var dataVal = traitClassesSuggestions_obj[counter];
				//console.log(dataVal);
				$("#" + comboName).select2('data', dataVal);
				$("#" + descriptionName).val(dataVal.description);
				break;
			}
		}	
	}
	
	//we filter the property combo
	var suggestions_obj = [];
	if(traitClassId == 0){
		suggestions_obj = sortByKey(propertySuggestions_obj, "text");
	}		
	else{
		//we filter by specific
		var count = 0;
		for(count = 0 ; count < propertySuggestions_obj.length ; count++){
			if(traitClassId == propertySuggestions_obj[count].traitId){
				suggestions_obj[suggestions_obj.length] = propertySuggestions_obj[count];
			}
		}
	}
	
	$("#comboProperty").select2({
		query: function (query) {
              var data = {results: suggestions_obj}, i, j, s;
              // return the array that matches
              data.results = $.grep(data.results,function(item,index) {
                return ($.fn.select2.defaults.matcher(query.term,item.text));
              
              });
              
              //if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
              
                query.callback(data);
                
            }
	
});
	$("#propertyDescription").val("");
}
function loadTraitClassTree(treeName, comboName, descriptionName, treeData, dropDownId){
	//for triggering the start of search type ahead
			
	var json = $.parseJSON(treeData);
	
	
	$("#" + treeName).dynatree({
	      checkbox: false,
	      // Override class name for checkbox icon:
	      classNames: {
				container: "fbtree-container",
				expander: "fbtree-expander",
				nodeIcon: "fbtree-icon",
				combinedIconPrefix: "fbtree-ico-",
				focused: "fbtree-focused",
				active: "fbtree-active"
			},
	      selectMode: 1,
	      children: json,
	      onActivate: function(node) {
	        //alert("onActivate" + node.data.title);
	     // Display list of selected nodes
	        var selNodes = node.tree.getSelectedNodes();
	        // convert to title/key array
	        var selKeys = $.map(selNodes, function(node){
	             return "[" + node.data.key + "]: '" + node.data.title + "'";
	        });
	       
	        doTraitClassTreeHighlight(treeName, comboName, descriptionName, node.data.key);
	      },
	      onSelect: function(select, node) {
	        // Display list of selected nodes		    	
	        doTraitClassTreeHighlight(treeName, comboName, descriptionName, node.data.key);
	      },
	      onDblClick: function(node, event) {
	        node.toggleSelect();
	      },
	      onKeydown: function(node, event) {
	        if( event.which == 32 ) {
	          node.toggleSelect();
	          return false;
	        }
	      },
	    });

}

//function to retrieve the standard variable details of the selected variable
function getStandardVariableDetails(variableId) {
	if(isInt(variableId)){
		Spinner.toggle();
		$.ajax({
			url: ontologyUrl + "retrieve/variable/" + variableId,
			type: "GET",
			dataType: "json",
			data: "",
		    success: function(data){
			    if (data.status == "1") {
			    	$("#variableId").val(variableId);
			    	$("#newVariableName").val(data.name);
			    	$("#variableDescription").val(data.description);
			    	$("#dataType").val(data.dataType);
			    	$("#role").val(data.role).attr("disabled","disabled");
			    	$("#cropOntologyId").val(data.cropOntologyId);
			    	setComboValues(traitClassesSuggestions_obj, data.traitClass, "TraitClass");
			    	setComboValues(propertySuggestions_obj, data.property, "Property");
			    	setComboValues(methodSuggestions_obj, data.method, "Method");
			    	setComboValues(scaleSuggestions_obj, data.scale, "Scale");
		       	}
			    Spinner.toggle();
		   }
		   
		});
		setVisibleButtons(false, true, true);
		setDeleteOperation(2);
	} else {
		//save the variable name in a hidden field for saving new standard variables
		$("#variableId").val("");
		$("#newVariableName").val($("#comboVariableName").select2("data").text);
		setVisibleButtons(true, false, false);
		$("#role").removeAttr("disabled");
		setDeleteOperation(0);
	}
	$("#page-message").html("");
}

function setComboValues(suggestions_obj, id, name) {
	var dataVal = {id:'',text:'',description:''}; //default value
	if(id != ''){
		var count = 0;
		//find the matching ontology value in the array given
    	for(count = 0 ; count < suggestions_obj.length ; count++){
    		if(suggestions_obj[count].id == id){
    			dataVal = suggestions_obj[count];			    			
    			break;
    		}			    			
    	}
	}
	//set the selected value of the ontology combo
	$("#combo" + name).select2('data', dataVal).trigger('change');
}

function setVisibleButtons(addButton, updateButton, deleteButton){
	setVisibility(addButton, "#addVariable");
	setVisibility(updateButton, "#updateVariable");
	setVisibility(deleteButton, "#deleteVariable");
}

function setVisibility(isVisible, buttonId) {
	if (isVisible) {
		$(buttonId).show();
	} else {
		$(buttonId).hide();
	}
}

function setDeleteOperation(val) {
	$("#isDelete").val(val);
}

function loadOntologyModal(ontologyName){
		Spinner.toggle();
		$.ajax(
		         { url: ontologyUrl + ontologyName,
		           type: "GET",
		           data: "",
		           success: function(html) {
		        	   
		             $("#manageOntologyModal"+" .modal-content").empty().append(html);
		               
		             $('#manageOntologyModal').modal('show');
		             $.fn.modal.Constructor.prototype.enforceFocus = function () {};
		             Spinner.toggle();
		           }
		         }
		       );
	}

function showErrorMessageInModal(messageDivId, message) {
	$("#" + messageDivId).html(
			"<div class='alert alert-danger'>"+ message +"</div>"
	);
}

function validateTraitClass() {
	return ($("#comboManageTraitClass").val() && $("#manageParentTraitClassId").val() && $("#manageParentTraitClassId").val() != "0");
}

function validateProperty() {
	return ($("#comboManageProperty").val() && $("#managePropTraitClassId").val());
}

function validateScale() {
	return ($("#comboManageScale").val());
}

function validateMethod() {
	return ($("#comboManageMethod").val());
}

function findIndexOfOntology(suggestions_obj, data) {
	for (var i = 0; i < suggestions_obj.length; i++) {
	    if (suggestions_obj[i].id == data.id) {
	        return i;
	    }
	}
	return -1;
}

function recreate(combo, suggestions_obj) {
	$("#combo" + combo).select2({
		query: function (query) {
              var data = {results: suggestions_obj}, i, j, s;
              // return the array that matches
              data.results = $.grep(data.results,function(item,index) {
                return ($.fn.select2.defaults.matcher(query.term,item.text));
              
              });
              if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
              
                query.callback(data);
            }		
	});
}

function recreateComboAfterDelete(combo, data) {
	var description = null;
	var index = 0;

	//add the new data in the collection
	if (combo == "VariableName") {
		index = findIndexOfDeletedVariable(variableNameSuggestions_obj, data);
		variableNameSuggestions_obj.splice(index, 1);
		recreate(combo, variableNameSuggestions_obj);
	} else if (combo == "ManageTraitClass") {		
		index = findIndexOfOntology(traitClassesSuggestions_obj, data);
		traitClassesSuggestions_obj.splice(index, 1);
		recreate(combo, traitClassesSuggestions_obj);
	} else if (combo == "ManageProperty") {
		index = findIndexOfOntology(propertySuggestions_obj, data);
		propertySuggestions_obj.splice(index, 1);
		recreate(combo, propertySuggestions_obj);
	} else if (combo == "ManageMethod") {
		index = findIndexOfOntology(methodSuggestions_obj, data);
		methodSuggestions_obj.splice(index, 1);
		recreate(combo, methodSuggestions_obj);
	} else {
		index = findIndexOfOntology(scaleSuggestions_obj, data);
		scaleSuggestions_obj.splice(index, 1);
		recreate(combo, scaleSuggestions_obj);
	}
	
	//set description field to empty
	if (description == null) {
		description = $("#"+lowerCaseFirstLetter(combo)+"Description"); 
	}
	description.val("");
}

function recreateComboAfterUpdate(combo, data) {
	var suggestions_obj = [];
	var description = null;
	
	if (combo.indexOf("TraitClass") > -1) {
		suggestions_obj = traitClassesSuggestions_obj;
	}
	else if (combo.indexOf("Property") > -1) {
		suggestions_obj = propertySuggestions_obj;
	}
	else if (combo.indexOf("Method") > -1) {
		suggestions_obj = methodSuggestions_obj;
	}
	else {
		suggestions_obj = scaleSuggestions_obj;
	}
	
	var index = findIndexOfOntology(suggestions_obj, data);
	if (index > -1) { //update
		suggestions_obj[index].description = data.definition;
	}
	else { //add
		suggestions_obj.push({ 'id' : data.id,
			  'text' : data.name + getOntologySuffix(data.id),
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(suggestions_obj, "text");
	}
	
	//set description field to empty
	description = $("#"+lowerCaseFirstLetter(combo)+"Description"); 
	description.val("");
	
	//recreate the dropdown
	$("#combo" + combo).select2({
			query: function (query) {
	              var data = {results: suggestions_obj}, i, j, s;
	              // return the array that matches
	              data.results = $.grep(data.results,function(item,index) {
	                return ($.fn.select2.defaults.matcher(query.term,item.text));
	              
	              });
	              
	              if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
	              
	                query.callback(data);
	                
	            }		
	});
	var newData = { 'id' : data.id,
			  'text' : data.name + getOntologySuffix(data.id),
			  'description' : data.definition
		}
	description.val(data.definition);
	$("#combo"+combo).select2('data', newData);//no need to trigger change.trigger('change');
}

function findIndexOfDeletedVariable(suggestions_obj, id) {
	for (var i = 0; i < suggestions_obj.length; i++) {
	    if (suggestions_obj[i].id == id) {
	        return i;
	    }
	}
}

function recreateVariableNameCombo(combo, id, name) {
	var suggestions_obj = [];
	
	//add the new data in the collection
	variableNameSuggestions_obj.push({ 'id' : id,
		  'text' : name + getOntologySuffix(id),
		  'description' : name
	});
	suggestions_obj = sortByKey(variableNameSuggestions_obj, "text");
	
	//recreate the dropdown
	$("#combo" + combo).select2({
			query: function (query) {
	              var data = {results: suggestions_obj}, i, j, s;
	              // return the array that matches
	              data.results = $.grep(data.results,function(item,index) {
	                return ($.fn.select2.defaults.matcher(query.term,item.text));
	              
	              });
	              
	              if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
	              
	                query.callback(data);
	                
	            }		
	});
	
	var newData = { 'id' : id,
			  'text' : name + getOntologySuffix(id),
			  'description' : name
		};
	$("#combo"+combo).select2('data', newData).trigger('change');
}

function preSelectAfterUpdate(combo, id, name) {
	var newData = { 'id' : id,
			  'text' : name,
			  'description' : name
		};
	$("#combo" + combo).select2('data', newData).trigger('change');	
}

//function for deleting ontology
function deleteOntology(combo) {
	//if (validateComboForDelete(combo)) {
		//get the form data
		var formData = {id: $("#" + lowerCaseFirstLetter(combo) + "Id").val(), name: $("#" + lowerCaseFirstLetter(combo) + "Name").val()};
		
		Spinner.toggle();
		
		$.ajax({
			url: ontologyUrl + "deleteOntology/" + lowerCaseFirstLetter(combo.replace("Manage", "")),
			type: "post",
			dataType: "json",
			data: formData,
		    success: function(data){
			    if (data.status == "1") {
		    		$("#btnClose" + combo).trigger("click");
		    		recreateComboAfterDelete(combo, formData);
		    		showSuccessMessage(data.successMessage);
		    	} else {
		    		showErrorMessageInModal("page-message-" + lowerCaseFirstLetter(combo) + "-modal", data.errorMessage);
		       	}
		   }, 
		   error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: " + textStatus, errorThrown);
		   }, 
		   complete: function(){ 
			   Spinner.toggle();
		   } 
		});

	//}
}

function clearForm(formName) {
	//$("#" + formName).reset();
	$("#" + formName).find("input").each(function() {
		this.value = "";
	});
}


