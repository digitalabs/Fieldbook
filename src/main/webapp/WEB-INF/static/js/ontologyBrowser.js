function doSearchTree(){
	var result = searchOntologyTreeNodeWithName(treeDivId, $('#srch-term').val());
	//var res = $("#"+treeDivId+" a.dynatree-title:contains('"++"')");
	$("#page-message").html("");
	if(result == null){   		    	       	    
    	$("#page-message").html("<div class='alert alert-danger'>"+ message +"</div>");
	}else{
		$("#"+treeDivId).dynatree("getTree").activateKey(result.data.key);
		doOntologyTreeHighlight(treeDivId, result.data.key);
	}
} 


function doOntologyTreeHighlight(treeName, nodeKey){
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
        if (node.data.title.toUpperCase() == name.toUpperCase()) {
            match = node;
            return false; // Break if found
        }
    });
    return match;
    
    	
};

function displayOntologyTree(treeName, treeData){
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
	showSelectedTab("ontology-details-tab");
	viewDetails(variableId);
}

function showSelectedTab(selectedTabName) {
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

function viewDetails(variableId) {
	$.ajax({
		url: "details/" + variableId,
		type: "get",
		dataType: "json",
		success: function(data) {
			$("#traitClass").text(data.isA.name);
			$("#property").text(data.property.name);
			$("#method").text(data.method.name);
			$("#scale").text(data.scale.name);
			$("#dataType").text(data.dataType.name);
			$("#role").text(data.phenotypicType);
			$("#cropOntologyId > p").text(data.cropOntologyId);
			$("#cropOntologyId").attr("href", "http://www.cropontology.org/terms/" + data.cropOntologyId) + "/";
		},
		error: function(jqXHR, textStatus, errorThrown){ 
				console.log("The following error occured: " + textStatus, errorThrown); 
		}
	});
}