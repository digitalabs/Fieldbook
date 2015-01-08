/*globals displayGermplasmListTree, changeBrowseGermplasmButtonBehavior, additionalLazyLoadUrl, displayAdvanceList*/
/*globals showErrorMessage, showInvalidInputMessage, getDisplayedTreeName*/
/*globals listParentFolderRequired, listNameRequired, listDescriptionRequired, listDateRequired, listTypeRequired, listNameDuplicateError */
/*exported saveGermplasmList, openSaveListModal*/

var SaveAdvanceList = {};
 
(function() {
	'use strict';
	SaveAdvanceList.initializeGermplasmListTree = function() {
		'use strict';
		displayGermplasmListTree('germplasmFolderTree', true, 1);
		changeBrowseGermplasmButtonBehavior(false);
		$('#saveListTreeModal').on('hidden.bs.modal', function() {
			$('#germplasmFolderTree').dynatree('getTree').reload();
			changeBrowseGermplasmButtonBehavior(false);
		});
	};
	SaveAdvanceList.openSaveListModal = function(object) {
		'use strict';
		var listIdentifier = $(object).attr('id'),
		germplasmTreeNode = $('#germplasmFolderTree').dynatree('getTree');
		additionalLazyLoadUrl = '/1';
		$.ajax(
			{ 
				url: '/Fieldbook/ListTreeManager/saveList/'+listIdentifier,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('#saveGermplasmRightSection').html(html);
					$('#saveListTreeModal').modal({
						show: true,
						keyboard: true,
						backdrop: 'static'
					});
					//we preselect the program lists
					if(germplasmTreeNode !== null && germplasmTreeNode.getNodeByKey('LISTS') !== null){
						germplasmTreeNode.getNodeByKey('LISTS').activate();
						germplasmTreeNode.getNodeByKey('LISTS').expand();
					}
				}
			}
		);
	};
	SaveAdvanceList.saveGermplasmList = function() {
		'use strict';
		var chosenNodeFolder = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode();
		var errorMessageDiv = 'page-save-list-message-modal';
		if(chosenNodeFolder === null){
			showErrorMessage(errorMessageDiv, listParentFolderRequired);
			return false;
		}
		if($('#listName').val() === ''){
			showInvalidInputMessage(listNameRequired);
			return false;
		}
		if($('#listDescription').val() === ''){
			showInvalidInputMessage(listDescriptionRequired);
			return false;
		}
		if($('#listType').val() === ''){
			showInvalidInputMessage(listTypeRequired);
			return false;
		}
		if($('#listDate').val() === ''){
			showInvalidInputMessage(listDateRequired);
			return false;
		}
		var invalidDateMsg = validateAllDates();
	    if(invalidDateMsg !== '') {
	    	showInvalidInputMessage(invalidDateMsg);
	    	return false;
	    }
	    
		var parentId = chosenNodeFolder.data.key;
		$('#saveListForm #parentId').val(parentId);
		var dataForm = $('#saveListForm').serialize();
		$.ajax({
			url: '/Fieldbook/ListTreeManager/saveList/',
			type: 'POST',
			data: dataForm,
			cache: false,
			success: function(data) {
				if(data.isSuccess === 1){
					$('#saveListTreeModal').modal('hide');
					displayAdvanceList(data.uniqueId, data.germplasmListId, data.listName, false, data.advancedGermplasmListId);
				}else{
					showErrorMessage('page-save-list-message-modal', data.message);
				}
			}
		});
	};
})(); 

