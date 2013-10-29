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
	
	if(elem.length == 3){
		//call ajax
		standardVariableKey = elem[elem.length-1];
		//alert("Do the ajax call now with standard variable id " + standardVariableKey);
		processTab(standardVariableKey);
	}else{
		clearAndAppendOntologyDetailsTab('');
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
function processTab(variableId) {
	viewTabs(variableId);
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

function clearAndAppendOntologyDetailsTab(html){
	if(html != '')
		$("#ontology-detail-tabs").empty().append(html);
	else
		$("#ontology-detail-tabs").empty();
}

function viewTabs(variableId) {
	Spinner.toggle();
	$.ajax({
		url: "details/" + variableId,
		type: "get",
		//dataType: "json",
		success: function(html) {
			clearAndAppendOntologyDetailsTab(html);			
		},
		error: function(jqXHR, textStatus, errorThrown){ 
			console.log("The following error occured: " + textStatus, errorThrown); 
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}

function doSave(combo) {
	if (validateCombo(combo)) {
		var $form = $("#addVariableForm");
		serializedData = $form.serialize();
		Spinner.toggle();
		
		$.ajax({
			url: "addVariable/" + combo,
			type: "post",
			dataType: "json",
			data: serializedData,
		    success: function(data){
			    if (data.status == "1") {
			    	recreateCombo(combo, data);	
			    	showSuccessMessage(data.successMessage);
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

function setCorrespondingTraitClass(propertyId){
	//console.log(propertyId);
	Spinner.toggle();
	$.ajax({
		url: "retrieve/trait/property/" + propertyId,
		type: "GET",
		dataType: "json",
		data: "",
	    success: function(data){
		    if (data.status == "1") {
		    	var dataVal = {id:'',text:'',description:''}; //default value
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
}

function getOntologySuffix(id){
	return (id > -1 ? "(Shared)" : "") + " "; 
}

function initializeVariable(variableSuggestions, variableSuggestions_obj, description, name) {
	if (description == "description") {
		$.each(variableSuggestions, function( index, value ) {
			variableSuggestions_obj.push({ 'id' : value.id,
				  'text' : getOntologySuffix(value.id) + value.name,
				  'description' : value.description
			});  
	  		
		});
	} else {
		$.each(variableSuggestions, function( index, value ) {
			variableSuggestions_obj.push({ 'id' : value.id,
				  'text' : getOntologySuffix(value.id) + value.name,
				  'description' : value.definition
			});  
	  		
		});
	}
	
	if (name == "Variable Name") {
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
	
	    });
	} else {
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
	    	$("#" + lowerCaseFirstLetter(name) + "Description").val($("#combo"+name).select2("data").description);
	    	if(name == 'Property'){
	    		setCorrespondingTraitClass($("#comboProperty").select2("data").id);
	    	}
	    	
	    });
	}
}

function lowerCaseFirstLetter(string)
{
    return string.charAt(0).toLowerCase() + string.slice(1);
}

$(function () {
	initializeVariable(variableNameSuggestions, variableNameSuggestions_obj, "description", "VariableName");
	initializeVariable(traitClassesSuggestions, traitClassesSuggestions_obj, "description", "TraitClass");
	initializeVariable(propertySuggestions, propertySuggestions_obj, "definition", "Property");
	initializeVariable(methodSuggestions, methodSuggestions_obj, "definition", "Method");
	initializeVariable(scaleSuggestions, scaleSuggestions_obj, "definition", "Scale");
});		

function clearFields() {
	$("div.modal .form-control").val("");
	$("div.modal .select2").select2("val", "");
	$("#page-message-modal").html("");
}

function recreateCombo(combo, data) {
	var suggestions_obj = [];
	var description = null;
	
	//add the new data in the collection
	if (combo == "TraitClass") {
		traitClassesSuggestions_obj.push({ 'id' : data.id,
			  'text' : data.name,
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(traitClassesSuggestions_obj, "text");
		description = $("#traitClassDescription");
	} else if (combo == "Property") {
		propertySuggestions_obj.push({ 'id' : data.id,
			  'text' : data.name,
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(propertySuggestions_obj, "text");
	} else if (combo == "Method") {
		methodSuggestions_obj.push({ 'id' : data.id,
			  'text' : data.name,
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(methodSuggestions_obj, "text");
	} else {
		scaleSuggestions_obj.push({ 'id' : data.id,
			  'text' : data.name,
			  'description' : data.definition
		});
		suggestions_obj = sortByKey(scaleSuggestions_obj, "text");
	}
	
	//set description field to empty
	if (description == null) {
		description = $("#"+combo.toLowerCase()+"Description"); 
	}
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
}

function itemExists(combo) {
	return $("#combo"+combo).select2("data").id != $("#combo"+combo).select2("data").text && $("#combo"+combo).select2("data").description != undefined;
}
function showSuccessMessage(message) {
	$("#page-message-modal").html(
		    "<div class='alert alert-success'>"+ message +"</div>"
	);
	setTimeout("hideSuccessMessage()", 3000);
}
function hideSuccessMessage(){
	$('#page-message-modal .alert-success').fadeOut(1000);
}

function showMessage(message) {
	$("#page-message-modal").html(
		    "<div class='alert alert-danger'>"+ message +"</div>"
	);
}

function requiredFieldsEmpty() {
	return $("#variableName").val() == "" || $("#dataType").val() == "" || $("#role").val() == "" || 
	$("#comboTraitClass").val() == "" || $("#comboProperty").val() == "" || 
	$("#comboMethod").val() == "" || $("#comboScale").val() == "";
}

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