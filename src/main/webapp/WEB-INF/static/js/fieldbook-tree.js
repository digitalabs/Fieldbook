/** Germplasm Tree API **/

function displayGermplasmListTree(treeName) {
	displayTree(treeName, "loadInitGermplasmTree", "expandGermplasmTree");
}

/** Study Tree API **/

function displayStudyTree(treeName) {
	displayTree(treeName, "loadInitTree", "expandTree");
}

function addFolderToStudyTree(treeName) {
	//TODO: ajax call to insert the node to the database
	addNodeToRoot(treeName);
}

function addFolderInStudyTree(treeName) {
	//TODO: implement ajax call
	var node = $("#" + treeName).dynatree("getTree").getActiveNode();
	if (node != null) {
		node.addChild({
			title: "New Sub Node",
			isFolder: true,
			key: "888"
		});
	}
}

function addLeafNodeInStudyTree(treeName) {
	//TODO: implement ajax call
	var node = $("#" + treeName).dynatree("getTree").getActiveNode();
	if (node != null) {
		node.addChild({
			title: "New Leaf Node",
			isFolder: false,
			key: "777"
		});
	}
}


/** Utility functions **/

function displayTree(treeName, initMethodName, expandMethodName) {
	$("#" + treeName).dynatree({
		title: treeName,
		checkbox: false,
		noLink: false,
		autoFocus: false,
		imagePath: "../img/",
		activeVisible: true,
		initAjax: {url: initMethodName,
			dataType: "json"
		},
		onLazyRead: function(node) {
			node.appendAjax({
				url: expandMethodName + "/" + node.data.key,
				dataType: "json",
				success: function(node) {
					//do nothing
				},
				error: function(node, XMLHttpRequest, textStatus, errorThrown) {
					console.log("The following error occured: " + textStatus, errorThrown); 
				},
				cache: false
			});
		},
		classNames: {
			container: "fbtree-container",
			expander: "fbtree-expander",
			nodeIcon: "fbtree-icon",
			combinedIconPrefix: "fbtree-ico-",
			focused: "fbtree-focused",
			active: "fbtree-active"
		}
	});
}

function addNodeToRoot(treeName) {
	var rootNode = $("#" + treeName).dynatree("getRoot");
	rootNode.addChild({
		title: "New Node",
		isFolder: true,
		key: "888"
	});
}

function displayOntologyTree(treeName, treeData){
	var json = $.parseJSON(treeData);
	/*
	json =  [
	         {title: "item1 with key and tooltip", tooltip: "Look, a tool tip!" },
	         {title: "item2: selected on init", select: true },
	         {title: "Folder", isFolder: true, key: "id3",
	           children: [
	             {title: "Sub-item 3.1",
	               children: [
	                 {title: "Sub-item 3.1.1", key: "id3.1.1" },
	                 {title: "Sub-item 3.1.2", key: "id3.1.2" }
	               ]
	             },
	             {title: "Sub-item 3.2",
	               children: [
	                 {title: "Sub-item 3.2.1", key: "id3.2.1" },
	                 {title: "Sub-item 3.2.2", key: "id3.2.2" }
	               ]
	             }
	           ]
	         },
	         {title: "Document with some children (expanded on init)", key: "id4", expand: true,
	           children: [
	             {title: "Sub-item 4.1 (active on init)", activate: true,
	               children: [
	                 {title: "Sub-item 4.1.1", key: "id4.1.1" },
	                 {title: "Sub-item 4.1.2", key: "id4.1.2" }
	               ]
	             },
	             {title: "Sub-item 4.2 (selected on init)", select: true,
	               children: [
	                 {title: "Sub-item 4.2.1", key: "id4.2.1" },
	                 {title: "Sub-item 4.2.2", key: "id4.2.2" }
	               ]
	             },
	             {title: "Sub-item 4.3 (hideCheckbox)", hideCheckbox: true },
	             {title: "Sub-item 4.4 (unselectable)", unselectable: true }
	           ]
	         }
	       ];
*/
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
		alert("Do the ajax call now with standard variable id " + standardVariableKey);
		
	}
}

function seachOntologyTreeNodeWithName(treeName, name) {
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
