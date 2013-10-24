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
		console.log("Highlight: " + key);
		$('.'+key).addClass('highlight');
	}
	
	if(elem.length == 3){
		//call ajax
		standardVariableKey = elem[elem.length-1];
		//alert("Do the ajax call now with standard variable id " + standardVariableKey);
		processTab(standardVariableKey);
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
	clearAllTabs();
	showSelectedTab("ontology-details-tab");
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

function viewTabs(variableId) {
	Spinner.toggle();
	$.ajax({
		url: "details/" + variableId,
		type: "get",
		dataType: "json",
		success: function(data) {
			if (data && !$.isEmptyObject(data) && data.status && data.status == "success") {
				populateDetailsTab(data.variable);
				populateValidValuesTab(data.variable);
				populateUsageTab(data);
			} else {
				clearAllTabs();
			}
		},
		error: function(jqXHR, textStatus, errorThrown){ 
			console.log("The following error occured: " + textStatus, errorThrown); 
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}

function populateDetailsTab(variable) {
	if (variable.isA) {
		$("#traitClass").text(variable.isA.name);
	} else {
		$("#traitClass").text("");
	}
	$("#property").text(variable.property.name);
	$("#method").text(variable.method.name);
	$("#scale").text(variable.scale.name);
	$("#dataType").text(variable.dataType.name);
	$("#role").text(variable.phenotypicType);
	if (variable.cropOntologyId) {
		$("#cropOntologyId > p").text(variable.cropOntologyId);
		$("#cropOntologyId").attr("href", "http://www.cropontology.org/terms/" + variable.cropOntologyId + "/");
	} else {
		$("#cropOntologyId > p").text("");
		$("#cropOntologyId").attr("href", "");
	}
}

function populateValidValuesTab(variable) {
	
}

function populateUsageTab(data) {
	$("#projectCount").text(data.projectCount);
	$("#observationCount").text(data.observationCount);
}

function clearAllTabs() {
	var elements = $("#ontology-details-tab").find("p.form-control-static");
	for (var i = 0; i < elements.length; i++) {
		elements.text("");
	}
	
	elements = $("#usageTabForm").find("p.form-control-static");
	for (var i = 0; i < elements.length; i++) {
		elements.text("");
	}
}