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

$(function () {
	
	
  	$.each(traitClassesSuggestions, function( index, value ) {
  		traitClassesSuggestions_obj.push({ 'id' : value.id,
			  'text' : value.name,
			  'description' : value.description
		});  
  		
	});
  	
  	$.each(propertySuggestions, function( index, value ) {
  		propertySuggestions_obj.push({ 'id' : value.id,
			  'text' : value.name,
			  'description' : value.definition
		});  
	});
  	
  	$.each(methodSuggestions, function( index, value ) {
  		methodSuggestions_obj.push({ 'id' : value.id,
			  'text' : value.name,
			  'description' : value.definition
		});  
	});
  	
  	$.each(scaleSuggestions, function( index, value ) {
  		scaleSuggestions_obj.push({ 'id' : value.id,
			  'text' : value.name,
			  'description' : value.definition
		});  
	});
  	
  	$("#comboTraitClass").select2({
        query: function (query) {
          var data = {results: sortByKey(traitClassesSuggestions_obj, "text")}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
          if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
          
            query.callback(data);
        }

    }).on("change", function(){
    	$("#traitClassDescription").val($("#comboTraitClass").select2("data").description);
    });
  	
  	$("#comboProperty").select2({
        query: function (query) {
          var data = {results: sortByKey(propertySuggestions_obj, "text")}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
          if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
          
            query.callback(data);
        }

    }).on("change", function(){
    	$("#propertyDescription").val($("#comboProperty").select2("data").description);
    });
  	
  	$("#comboMethod").select2({
        query: function (query) {
          var data = {results: sortByKey(methodSuggestions_obj, "text")}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
          if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
          
            query.callback(data);
        }

    }).on("change", function(){
    	$("#methodDescription").val($("#comboMethod").select2("data").description);
    });
  	
  	$("#comboScale").select2({
        query: function (query) {
          var data = {results: sortByKey(scaleSuggestions_obj, "text")}, i, j, s;
          // return the array that matches
          data.results = $.grep(data.results,function(item,index) {
            return ($.fn.select2.defaults.matcher(query.term,item.text));
          
          });
          if (data.results.length === 0) data.results.unshift({id:query.term,text:query.term});
          
            query.callback(data);
        }

    }).on("change", function(){
    	$("#scaleDescription").val($("#comboScale").select2("data").description);
    });
  	
  //$("#comboTraitClass").data("ui-combobox").value($("#hidTraitClass").val());
  //$("#comboProperty").data("ui-combobox").value($("#hidProperty").val());
  //$("#comboMethod").data("ui-combobox").value($("#hidMethod").val());
  //$("#comboScale").data("ui-combobox").value($("#hidScale").val());
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