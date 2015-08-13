/*global expandGermplasmListInTreeTable*/
var GERMPLASM_LIST_TYPE = 'GERMPLASM_LIST';
var STUDY_LIST_TYPE = 'STUDY_LIST';

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
		TreePersist.saveTreeState(isTreeTable, containerSection,
				GERMPLASM_LIST_TYPE);
	},
	saveStudyTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.saveTreeState(isTreeTable, containerSection, STUDY_LIST_TYPE);
	},
	saveTreeState : function(isTreeTable, containerSection, listType) {
		'use strict';
		var expandedNodesState = TreePersist.retrieveExpandedNodes(isTreeTable,
				containerSection);
		
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
			success : function() {

			}
		});
	},
	preLoadStudyTreeState : function(containerSection) {
		'use strict';
		TreePersist.preLoadTreeState(containerSection,
				STUDY_LIST_TYPE);
	},
	preLoadGermplasmTreeState : function(isTreeTable, containerSection) {
		'use strict';

		if (isTreeTable){
			TreePersist.preLoadTreeTableState(GERMPLASM_LIST_TYPE);
		} else {
			TreePersist.preLoadTreeState(containerSection, STUDY_LIST_TYPE);
		}

	},

	retrievePreviousTreeState : function(listType) {
		'use strict';
		var deferred = $.Deferred();
		$.ajax({
			url : '/Fieldbook/ListTreeManager/retrieve/state/' + listType,
			type : 'GET',
			data : '',
			cache : false,
			async : false,
			success : function(data) {
				var expandedNodes = $.parseJSON(data);
				if(expandedNodes.length == 1 && expandedNodes[0] === ''){
					deferred.reject(expandedNodes);
				} else {
					deferred.resolve(expandedNodes);
				}
			}
		});

		return deferred.promise();
	},

	preLoadTreeTableState : function(listType) {
		'use strict';
		TreePersist.retrievePreviousTreeState(listType).done(function(expandedNodes) {
			TreePersist.traverseNodes(expandedNodes, listType, expandGermplasmListInTreeTable);
		});
	},

	preLoadTreeState : function(containerSection, listType) {
		'use strict';
		
		TreePersist.retrievePreviousTreeState(listType).done(function(expandedNodes){
			var dynatree = $(containerSection).dynatree('getTree');
			TreePersist.traverseNodes(expandedNodes, listType, function(key) {
				var germplasmFocusNode = dynatree.getNodeByKey(key);
				if (germplasmFocusNode !== null) {
					germplasmFocusNode.expand();
				}
			});
		});
	},

	traverseNodes : function(expandedNodes, listType, keyProcessor){
		var key, index;
		for (index = 0; index < expandedNodes.length; index++) {
			key = expandedNodes[index];
			if (index === 0) {
				if (listType === GERMPLASM_LIST_TYPE) {
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