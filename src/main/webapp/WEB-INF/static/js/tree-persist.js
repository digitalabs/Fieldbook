/*global expandGermplasmListInTreeTable*/

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
				'GERMPLASM_LIST');
	},
	saveStudyTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.saveTreeState(isTreeTable, containerSection, 'STUDY_LIST');
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
	preLoadStudyTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.preLoadTreeState(isTreeTable, containerSection,
				'STUDY_LIST');
	},
	preLoadGermplasmTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.preLoadTreeState(isTreeTable, containerSection,
				'GERMPLASM_LIST');
	},
	preLoadTreeState : function(isTreeTable, containerSection, listType) {
		'use strict';
		
		var expandedNodes = [];
		var index, key;
		$.ajax({
			url : '/Fieldbook/ListTreeManager/retrieve/state/' + listType,
			type : 'GET',
			data : '',
			cache : false,
			async : false,
			success : function(data) {
				expandedNodes = $.parseJSON(data);
			}
		});
		if(expandedNodes.length == 1 && expandedNodes[0] === ''){
			return;
		}
		if (isTreeTable) {
			// we simulate the opening of the folder
			for (index = 0; index < expandedNodes.length; index++) {
				key = expandedNodes[index];
				if (index === 0) {
					if (listType === 'GERMPLASM_LIST') {
						key = 'LISTS';
					} else if (listType === 'STUDY_LIST') {
						key = 'LOCAL';
					}
				}
				key = $.trim(key);
				expandGermplasmListInTreeTable(key);
			}
		} else {
			var dynatree = $(containerSection).dynatree('getTree');
			for (index = 0; index < expandedNodes.length; index++) {
				key = expandedNodes[index];
				if (index === 0) {
					if (listType === 'GERMPLASM_LIST') {
						key = 'LISTS';
					} else if (listType === 'STUDY_LIST') {
						key = 'LOCAL';
					}
				}
				key = $.trim(key);
				var germplasmFocusNode = dynatree.getNodeByKey(key);
				if (germplasmFocusNode !== null) {
					germplasmFocusNode.expand();
				}
			}
		}
	},
};