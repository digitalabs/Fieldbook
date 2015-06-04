var TreePersist = {
		retrieveExpandedNodes : function(isTreeTable, containerSection){
			//Tree Table : TreePersist.retrieveExpandedNodes(true, '#germplasmTree')
			//Tree : TreePersist.retrieveExpandedNodes(false, '#germplasmFolderTree')			
			var expandedNodes = [];
			if(isTreeTable){
				$(containerSection).find('.expanded').each(function(){
					expandedNodes.push($(this).data('tt-id'));
				});
			}else{
				//dynatree
				var dynatree = $(containerSection).dynatree("getTree").toDict(true);  
				expandedNodes = TreePersist.getAllTreeExpandedNode(dynatree.children, expandedNodes);
			}			
			
			return expandedNodes;
		},
		getAllTreeExpandedNode : function(children, expandedNodes){
			if(typeof children !== "undefined") {			 
				for(var index = 0 ; index < children.length ; index++){
					if(children[index].expand == true){
						expandedNodes.push(children[index].key);
						TreePersist.getAllTreeExpandedNode(children[index].children, expandedNodes);
					}
				}
			}
			return expandedNodes;
		},
		saveGermplasmTreeState : function(isTreeTable, containerSection){
			TreePersist.saveTreeState(isTreeTable, containerSection, 'GERMPLASM_LIST');
		},
		saveStudyTreeState : function(isTreeTable, containerSection){
			TreePersist.saveTreeState(isTreeTable, containerSection, 'STUDY_LIST');
		},
		saveTreeState : function(isTreeTable, containerSection, listType){
			//listType: 'GERMPLASM_LIST', 'STUDY_LIST'
			var expandedNodesState  = TreePersist.retrieveExpandedNodes(isTreeTable, containerSection);			
			if(expandedNodesState.length == 0){
				return;
			}
			$.ajax({
				url: '/Fieldbook/ListTreeManager/save/state/'+listType,
				type: 'POST',
				data:  {
					expandedNodes: expandedNodesState 
			    },
				cache: false,
				async: false,
				success: function(data) {
					
				}
			});
		},
		preLoadStudyTreeState : function(isTreeTable, containerSection){
			TreePersist.preLoadTreeState(isTreeTable, containerSection, 'STUDY_LIST');
		},
		preLoadGermplasmTreeState : function(isTreeTable, containerSection){
			TreePersist.preLoadTreeState(isTreeTable, containerSection, 'GERMPLASM_LIST');
		},
		preLoadTreeState : function(isTreeTable, containerSection, listType){
			//listType: 'GERMPLASM_LIST', 'STUDY_LIST'
			var expandedNodes = [];
			
			$.ajax({
				url: '/Fieldbook/ListTreeManager/retrieve/state/'+listType,
				type: 'GET',
				data: '',
				cache: false,
				async: false,
				success: function(data) {
					expandedNodes = $.parseJSON(data);
				}
			});
			
			if(isTreeTable){
				//we simulate the opening of the folder
				for(var index = 0 ; index < expandedNodes.length; index++){
					var key = expandedNodes[index];
					if(index == 0){
						if(listType == 'GERMPLASM_LIST'){
							key = 'LISTS';
						}else if(listType == 'STUDY_LIST'){
							key = 'LOCAL';
						}
					}
					key = $.trim(key);
					expandGermplasmListInTreeTable(key);					
				}
			}else{
				var dynatree = $(containerSection).dynatree("getTree");				
				for(var index = 0 ; index < expandedNodes.length; index++){				
					var key = expandedNodes[index];
					if(index == 0){
						if(listType == 'GERMPLASM_LIST'){
							key = 'LISTS';
						}else if(listType == 'STUDY_LIST'){
							key = 'LOCAL';
						}
					}
					key = $.trim(key);
					var germplasmFocusNode = dynatree.getNodeByKey(key);
					if(germplasmFocusNode !== null){
						germplasmFocusNode.expand();
					}
				}
			}
		},
		
}