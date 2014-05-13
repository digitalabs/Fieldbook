
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
		imagePath: "/Fieldbook/static/img/",
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

