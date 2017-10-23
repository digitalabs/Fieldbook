/*global expandGermplasmListInTreeTable*/
var GERMPLASM_LIST_TYPE = 'GERMPLASM_LIST';
var STUDY_LIST_TYPE = 'STUDY_LIST';
var SAMPLE_LIST_TYPE = 'SAMPLE_LIST';

var TreePersist = {
	retrieveExpandedNodes : function(isTreeTable, containerSection) {
		'use strict';
		var expandedNodes = [];
		if (isTreeTable) {
			$(containerSection).find('.expanded').each(function() {
				expandedNodes.push($(this).data('tt-id'));
			});
		} else {
			var dynatree = $(containerSection).dynatree('getTree').toDict(true);
			expandedNodes = TreePersist.getAllTreeExpandedNode(
					dynatree.children, expandedNodes);
		}

		return expandedNodes;
	},
	getAllTreeExpandedNode : function(children, expandedNodes) {
		'use strict';
		if (typeof children !== 'undefined') {
			for (var index = 0; index < children.length; index++) {
				if (children[index].expand === true) {
					expandedNodes.push(children[index].key);
					TreePersist.getAllTreeExpandedNode(
							children[index].children, expandedNodes);
				}
			}
		}
		return expandedNodes;
	},
	saveGermplasmTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.saveTreeState(isTreeTable, containerSection, GERMPLASM_LIST_TYPE);
	},
	saveSampleTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.saveTreeState(isTreeTable, containerSection, SAMPLE_LIST_TYPE);
	},
	saveStudyTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.saveTreeState(isTreeTable, containerSection, STUDY_LIST_TYPE);
	},
	saveTreeState : function(isTreeTable, containerSection, listType) {
		'use strict';
		//console.log("saveTreeState --> isTreeTable:" + isTreeTable + " ,containerSection:" + containerSection + " ,listType:" + listType);

		var expandedNodesState = TreePersist.retrieveExpandedNodes(isTreeTable,
				containerSection);
		//console.log("saveTreeState -->  expandedNodesState: " + expandedNodesState + " ,listType:" + listType);
		if (expandedNodesState.length === 0) {
			expandedNodesState = ['None'];
		}
		$.ajax({
			url : '/Fieldbook/ListTreeManager/save/state/' + listType,
			type : 'POST',
			data : {
				expandedNodes : expandedNodesState
			},
			cache : false,
			async : false,
			success : function(data) {
				//console.log("saveTreeState: " + data);
			}
		});
	},
	preLoadStudyTreeState : function(containerSection) {
		'use strict';
		TreePersist.preLoadTreeState(containerSection,
				STUDY_LIST_TYPE);
	},
	preLoadGermplasmTreeState: function(isTreeTable, containerSection, isSaveList) {
		'use strict';
		//console.log("isTreeTable:" + isTreeTable + " ,containerSection:" + containerSection + " ,isSaveList:" + isSaveList);
		if (isTreeTable){
			TreePersist.preLoadTreeTableState(GERMPLASM_LIST_TYPE, isSaveList);
		} else {
			TreePersist.preLoadTreeState(containerSection, GERMPLASM_LIST_TYPE, isSaveList);
		}

	},

	preLoadSampleTreeState: function(isTreeTable, containerSection, isSaveList) {
		'use strict';
		//console.log("isTreeTable:" + isTreeTable + " ,containerSection:" + containerSection + " ,isSaveList:" + isSaveList);
		if (isTreeTable){
			TreePersist.preLoadTreeTableSampleListState(SAMPLE_LIST_TYPE, isSaveList);
		} else {
			TreePersist.preLoadTreeSampleListState(containerSection, SAMPLE_LIST_TYPE, isSaveList);
		}

	},
	retrievePreviousTreeState: function(listType, isSaveList) {
		'use strict';
		var deferred = $.Deferred();
		//console.log("retrievePreviousTreeState--> listType:" + listType + " ,isSaveList:" + isSaveList);

		if (isSaveList === undefined) {
			isSaveList = false;
		}

		$.ajax({
			url: '/Fieldbook/ListTreeManager/retrieve/state/' + listType + '/' + isSaveList,
			type : 'GET',
			data : '',
			cache : false,
			async : false,
			success : function(data) {
				var expandedNodes = $.parseJSON(data);
				if((expandedNodes.length === 1 && expandedNodes[0] === '') || expandedNodes.length === 0){
					deferred.reject(expandedNodes);
				} else {
					deferred.resolve(expandedNodes);
				}
			}
		});

		return deferred.promise();
	},

	retrievePreviousTreeSampleListState: function(listType, isSaveList) {
		'use strict';
		var deferred = $.Deferred();

		if (isSaveList === undefined) {
			isSaveList = false;
		}
		//console.log("retrievePreviousTreeSampleListState--> listType:" + listType + " ,isSaveList:" + isSaveList);

		$.ajax({
			url: '/Fieldbook/SampleListTreeManager/retrieve/state/' + listType + '/' + isSaveList,
			type : 'GET',
			data : '',
			cache : false,
			async : false,
			success : function(data) {
				var expandedNodes = $.parseJSON(data);
				//console.log("retrievePreviousTreeSampleListState--> data:" + data);
				if((expandedNodes.length === 1 && expandedNodes[0] === '') || expandedNodes.length === 0){
					deferred.reject(expandedNodes);
				} else {
					deferred.resolve(expandedNodes);
				}
			}
		});

		return deferred.promise();
	},

	retrievePreviousTreeSampleListState: function(listType, isSaveList) {
		'use strict';
		var deferred = $.Deferred();

		if (isSaveList === undefined) {
			isSaveList = false;
		}
		//console.log("retrievePreviousTreeSampleListState: ");

		$.ajax({
			url: '/Fieldbook/SampleListTreeManager/retrieve/state/' + listType + '/' + isSaveList,
			type : 'GET',
			data : '',
			cache : false,
			async : false,
			success : function(data) {
				var expandedNodes = $.parseJSON(data);
				//console.log(data);
				if((expandedNodes.length === 1 && expandedNodes[0] === '') || expandedNodes.length === 0){
					deferred.reject(expandedNodes);
				} else {
					deferred.resolve(expandedNodes);
				}
			}
		});

		return deferred.promise();
	},

	preLoadTreeTableState: function(listType, isSaveList) {
		'use strict';
		TreePersist.retrievePreviousTreeState(listType, isSaveList).done(function(expandedNodes) {
			TreePersist.traverseNodes(expandedNodes, listType, expandGermplasmListInTreeTable);
		}).fail(function () {
			// If there's no previous tree state, the top level 'Lists' node should be expanded by default.
			$('#germplasmTreeTable').treetable('expandNode', 'LISTS');
		});
	},

	preLoadTreeTableSampleListState: function(listType, isSaveList) {
		'use strict';
		TreePersist.retrievePreviousTreeSampleListState(listType, isSaveList).done(function(expandedNodes) {
			TreePersist.traverseNodes(expandedNodes, listType, expandGermplasmListInTreeTable);
		}).fail(function () {
			// If there's no previous tree state, the top level 'Lists' node should be expanded by default.
			$('#germplasmTreeTable').treetable('expandNode', 'LISTS');
		});
	},

	preLoadTreeState: function(containerSection, listType, isSaveList) {
		'use strict';

		TreePersist.retrievePreviousTreeState(listType, isSaveList).done(function(expandedNodes) {
			var dynatree = $(containerSection).dynatree('getTree');
			var shouldActivateNode = false;

			if (isSaveList) {
				// tree state retrieval used when saving lists provides an additional marker key at the front to indicate status
				shouldActivateNode = expandedNodes[0] === 'SAVED';

				// remove the marker key to continue normal tree state processing
				expandedNodes = expandedNodes.slice(1, expandedNodes.length);
			}

			TreePersist.traverseNodes(expandedNodes, listType, function(key) {
				var germplasmFocusNode = dynatree.getNodeByKey(key);
				if (germplasmFocusNode !== null) {
					germplasmFocusNode.expand();
				}
			});

			setTimeout(function() {
				$(containerSection).dynatree('getRoot').visit(function(node) {
					node.select(false);
					node.deactivate();
				});

				if (shouldActivateNode) {
					dynatree.getNodeByKey(expandedNodes[expandedNodes.length - 1]).activate();
				}
			}, 50);

		});
	},

	preLoadTreeSampleListState: function(containerSection, listType, isSaveList) {
		'use strict';

		TreePersist.retrievePreviousTreeSampleListState(listType, isSaveList).done(function(expandedNodes) {
			var dynatree = $(containerSection).dynatree('getTree');
			var shouldActivateNode = false;

			if (isSaveList) {
				// tree state retrieval used when saving lists provides an additional marker key at the front to indicate status
				shouldActivateNode = expandedNodes[0] === 'SAVED';

				// remove the marker key to continue normal tree state processing
				expandedNodes = expandedNodes.slice(1, expandedNodes.length);
			}

			TreePersist.traverseNodes(expandedNodes, listType, function(key) {
				var germplasmFocusNode = dynatree.getNodeByKey(key);
				if (germplasmFocusNode !== null) {
					germplasmFocusNode.expand();
				}
			});

			setTimeout(function() {
				$(containerSection).dynatree('getRoot').visit(function(node) {
					node.select(false);
					node.deactivate();
				});

				if (shouldActivateNode) {
					dynatree.getNodeByKey(expandedNodes[expandedNodes.length - 1]).activate();
				}
			}, 50);

		});
	},

	traverseNodes : function(expandedNodes, listType, keyProcessor){
		var key, index;
		for (index = 0; index < expandedNodes.length; index++) {
			key = expandedNodes[index];
			if (index === 0) {
				if (listType === GERMPLASM_LIST_TYPE || listType === SAMPLE_LIST_TYPE) {
					key = 'LISTS';
				} else if (listType === STUDY_LIST_TYPE) {
					key = 'LOCAL';
				}
			}
			key = $.trim(key);
			keyProcessor(key);
		}
	}
};
