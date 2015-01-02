function expandGermplasmListInTreeTable(id) {
	var table = $('#germplasmTreeTable');
	var node = table.treetable('node', id);
	expandGermplasmListNode(node);
}
function expandGermplasmListNode(node) {
	$.ajax({
	 	async: false,
	 	url: '/Fieldbook/ListTreeManager/expandGermplasmListFolder/'+node.id
	}).done(function(html) {
	 	var rows = $(html).filter('tr');
	 	rows.find('.folder').parents('tr').each(function() {
	 		updateExpandEventIfHasChildren($(this));
		});
	 	rows.find('.file').each(function() {
	 		updateDoubleClickEvent($(this));
		});
		$('#germplasmTreeTable').treetable('unloadBranch', node);
	 	$('#germplasmTreeTable').treetable('loadBranch', node, rows);
	 	if(!$('tr[data-tt-id='+node.id+']').hasClass('expanded')) {
	 		$('#germplasmTreeTable').treetable('expandNode', node.id);
		}
		updateTableRowsBgColor();
		updateTools();
		updateDroppableTableRows();
		updateDraggableTableRows();
	});
}
function updateExpandEventIfHasChildren(obj) {
	if(obj.attr('num-of-children')==='0') {
		obj.attr('data-tt-branch', false);
		obj.find('span.indenter').html('');
	} else {
		obj.attr('data-tt-branch', true);
	}
}
function updateDoubleClickEvent(obj) {
	obj.dblclick(function() {
		chooseList();
	});
}
function updateTableRowsBgColor() {
	$('.germplasm-list-row').each(function(index) {
    	$('td', $(this)).each(function() {
    		$(this).removeClass('even');
    		$(this).removeClass('odd');
    		if(index % 2 === 0) {
	    		$(this).addClass('even');	
			} else {
				$(this).addClass('odd');
			}
	    });
	});
}
function initializeGermplasmListTreeTable() {
	$( '.germplasm-list-row').each(function(index) {
		if($(this).attr('num-of-children')==='0') {
			$(this).attr('data-tt-branch', false);
		} else {
			$(this).attr('data-tt-branch', true);
		}
	});
	updateTools();
	updateDraggableTableRows();
	updateDroppableTableRows();
	$('#germplasmTreeTable').treetable('unloadBranch', $('#germplasmTreeTable').treetable('node','LISTS'));
	updateTableRowsBgColor();
	$('#germplasmTreeTable .file').each(function() {
		updateDoubleClickEvent($(this));
	});
}
function updateTools() {
	$('#germplasmTreeTable tbody').on('mousedown', 'tr', function() {
		$('tr.selected').removeClass('selected');
		$(this).addClass('selected');
		if($(this).attr('data-tt-id')==='LISTS') {
			changeBrowseGermplasmButtonBehavior(true);
			$('.edit-germplasm-folder').addClass('disable-image');
			$('.delete-germplasm-folder').addClass('disable-image');
			hideRenameGermplasmFolderDiv();
		} else if($(this).attr('num-of-children')!=='0') {
			changeBrowseGermplasmButtonBehavior(true);
			$('.delete-germplasm-folder').addClass('disable-image');
		} else {
			changeBrowseGermplasmButtonBehavior(true);
		}
	});
}
function updateDraggableTableRows() {
	$('#germplasmTreeTable .folder, #germplasmTreeTable .file').each(function() {
		var rowId = $(this).parents('tr').attr('data-tt-id');
		if(rowId !== 'LISTS') {
			germplasmTreeTableDraggableSetup.apply(this);
		}
	});
}
function germplasmTreeTableDraggableSetup() {
	$(this).draggable({
		helper: 'clone',
		opacity: .75,
		refreshPositions: true,
		revert: 'invalid',
		revertDuration: 300,
		scroll: true
	});
}
function updateDroppableTableRows() {
	$('#germplasmTreeTable .folder').parents('tr').each(function() {
		germplasmTreeTableDroppableSetup.apply(this);
	});
}
function germplasmTreeTableDroppableSetup() {
	$(this).droppable({
		accept: '.file, .folder',
		drop: function(e, ui) {
			var droppedEl, sourceNode, targetNode;
		 	droppedEl = ui.draggable.parents('tr');
		 	sourceNode = $('#germplasmTreeTable').treetable('node', droppedEl.data('ttId'));
		 	var sourceNodeObj = $('tr[data-tt-id='+sourceNode.id+']');
		 	var sourceParentId = sourceNodeObj.attr('data-tt-parent-id');
		 	var numOfChildren = sourceNodeObj.attr('num-of-children');
		 	var sourceNodeName = sourceNodeObj.find('.name').first().text();
		 	if(numOfChildren !== '0') {
		 		showErrorMessage(getMessageErrorDiv(), cannotMove + ' ' + sourceNodeName + ' ' + hasChildrenString);
		 		return false;
		 	}
		 	targetNode = $('#germplasmTreeTable').treetable('node', $(this).data('ttId'));
		 	moveGermplasmListInTreeTable(sourceNode, targetNode,sourceParentId);
		},
		hoverClass: 'accept',
		over: function(e, ui) {
			var droppedEl = ui.draggable.parents('tr');
		 	if(this != droppedEl[0] && !$(this).is('.expanded')) {
		 		$('#germplasmTreeTable').treetable('expandNode', $(this).data('ttId'));
			}
		}
	});
}
function moveGermplasmListInTreeTable(sourceNode, targetNode, sourceParentId) {
	'use strict';
	var sourceId = sourceNode.id,
		targetId = targetNode.id;

	if (targetId === 'LISTS') {
		targetId = 0;
	}

	$.ajax({
		url: '/Fieldbook/ListTreeManager/moveGermplasmFolder',
		type: 'POST',
		data: 'sourceId=' + sourceId + '&targetId=' + targetId,
		cache: false,
		success: function(data) {
			$('#germplasmTreeTable').treetable('move', sourceNode.id, targetNode.id);
			updateTableRowsBgColor();
			var sourceNodeObj = $('tr[data-tt-id='+sourceNode.id+']');
			updateParentId(sourceNodeObj,targetNode.id);
			var targetNodeObj = $('tr[data-tt-id='+targetNode.id+']');
			incrementNumberOfChildren(targetNodeObj);
			updateExpandEventIfHasChildren(targetNodeObj);
			var sourceParentObj = $('tr[data-tt-id='+sourceParentId+']');
			decrementNumberOfChildren(sourceParentObj);
			updateExpandEventIfHasChildren(sourceParentObj);
		}
	});
}
function updateParentId(obj,parentId) {
	obj.attr('data-tt-parent-id', parentId); 
}
function incrementNumberOfChildren(obj) {
	var numOfChildren = parseInt(obj.attr('num-of-children')) + 1;
	obj.attr('num-of-children', ''+numOfChildren);
}

function decrementNumberOfChildren(obj) {
	var numOfChildren = parseInt(obj.attr('num-of-children')) - 1;
	obj.attr('num-of-children', ''+numOfChildren);
}
function submitDeleteGermplasmFolderInTreeTable() {
	'use strict';

	var folderId = getSelectedGermplasmListId();

	$.ajax({
		url: '/Fieldbook/ListTreeManager/deleteGermplasmFolder',
		type: 'POST',
		data: 'folderId=' + folderId,
		cache: false,
		success: function(data) {
			var node;
			if (data.isSuccess === '1') {
				$('#deleteGermplasmFolder').modal('hide');
				node = getSelectedGermplasmList();
				node.remove();
				updateTableRowsBgColor();
				changeBrowseGermplasmButtonBehavior(false);
                showSuccessfulMessage('',deleteFolderSuccessful);
			} else {
				showErrorMessage('page-delete-germplasm-folder-message-modal', data.message);
			}
		}
	});
}
function createGermplasmFolderInTreeTable() {
    'use strict';

    if (!getSelectedGermplasmList()) {
        showErrorMessage('page-rename-germplasm-folder-message-modal', invalidNodeGermplasmTreeMessage);
        return false;
    }
    
    var folderName = $.trim($(getDisplayedModalSelector() + ' #addGermplasmFolderName').val()),
            parentFolderId;

    if (folderName === '') {
        showErrorMessage('page-add-germplasm-folder-message-modal', folderNameRequiredMessage);
        return false;
    } else if (!isValidInput(folderName)) {
        showErrorMessage('page-add-germplasm-folder-message-modal', invalidFolderNameCharacterMessage);
        return false;
    } else {
        parentFolderId = getSelectedGermplasmListId();
        if (parentFolderId === 'LISTS') {
            parentFolderId = 0;
        }

        $.ajax({
            url: '/Fieldbook/ListTreeManager/addGermplasmFolder',
            type: 'POST',
            data: 'parentFolderId=' + parentFolderId + '&folderName=' + folderName,
            cache: false,
            success: function (data) {
                if (data.isSuccess === '1') {
                	expandGermplasmListInTreeTable(getParentOfNewlyAddedGermplasmList());
                	var id = data.id;
                	selectNode(id);
                	setAsDroppableAndDraggable(id);
                    hideAddGermplasmFolderDiv();
                    changeBrowseGermplasmButtonBehavior(true);
                    showSuccessfulMessage('', addFolderSuccessful);
                } else {
                    showErrorMessage('page-add-germplasm-folder-message-modal', data.message);
                }
            }
        });
    }
    return false;
}
function setAsDroppableAndDraggable(id) {
	$('tr[data-tt-id='+id+']').each(function() {
		germplasmTreeTableDroppableSetup.apply(this);
	});
	$('tr[data-tt-id='+id+'] .folder').each(function() {
		germplasmTreeTableDraggableSetup.apply(this);
	});
}
function getParentOfNewlyAddedGermplasmList() {
	var parentId = getSelectedGermplasmListId();
	if(!isSelectedGermplasmListAFolder()) {
		parentId = getSelectedGermplasmListParentId();
	}
	return parentId;
}
function selectNode(id) {
	$('tr.selected').removeClass('selected');
	$('tr[data-tt-id='+id+']').addClass('selected');
}
function addGermplasmFolderInTreeTable(object) {
    'use strict';
    if (!$(object).hasClass('disable-image')) {
        hideRenameGermplasmFolderDiv();
        $(getDisplayedModalSelector() + ' #addGermplasmFolderName').val('');
        $(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').slideDown('fast');
    }
}

function renameGermplasmFolerInTreeTable(object) {
    'use strict';

    var currentFolderName;

    if (!$(object).hasClass('disable-image')) {
    	hideAddGermplasmFolderDiv();

        currentFolderName = getSelectedGermplasmListName();
        $(getDisplayedModalSelector() + ' #newGermplasmFolderName').val(currentFolderName);

        $(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').slideDown('fast');
    }
}

function submitRenameGermplasmFolderInTreeTable() {
    'use strict';

    var folderName = $.trim($(getDisplayedModalSelector() + ' #newGermplasmFolderName').val()),
            parentFolderId;

    if (!getSelectedGermplasmList()) {
        showErrorMessage('page-rename-germplasm-folder-message-modal',invalidNodeGermplasmTreeMessage);
        return false;
    }
    
    var id = getSelectedGermplasmListId();

    if (id === 'LISTS') {
        showErrorMessage('page-rename-germplasm-folder-message-modal', renameGermplasmInvalidFolderMessage);
        return false;
    }

    var name = getSelectedGermplasmListName();
    if ($.trim(folderName) === name) {
    	hideRenameGermplasmFolderDiv();
        return false;
    }

    if (folderName === '') {
        showErrorMessage('page-rename-germplasm-folder-message-modal', folderNameRequiredMessage);
        return false;
    } else if (!isValidInput(folderName)) {
        showErrorMessage('page-rename-germplasm-folder-message-modal', invalidFolderNameCharacterMessage);
        return false;
    } else {
        parentFolderId = id;
        if (parentFolderId === 'LISTS') {
            parentFolderId = 0;
        }

        $.ajax({
            url: '/Fieldbook/ListTreeManager/renameGermplasmFolder',
            type: 'POST',
            data: 'folderId=' + parentFolderId + '&newFolderName=' + folderName,
            cache: false,
            success: function (data) {
                var node;
                if (data.isSuccess === '1') {
                    hideRenameGermplasmFolderDiv();
                    setSelectedGermplasmListName(folderName);
                    showSuccessfulMessage('', renameFolderSuccessful);
                } else {
                    showErrorMessage('page-rename-germplasm-folder-message-modal', data.message);
                }
            }
        });
    }
}

function deleteGermplasmFolderInTreeTable(object) {
	'use strict';

	var currentFolderName;

	if (!$(object).hasClass('disable-image')) {
		$('#deleteGermplasmFolder').modal('show');
        $('#addGermplasmFolderDiv').slideUp('fast');
        $('#renameGermplasmFolderDiv').slideUp('fast');
		currentFolderName = getSelectedGermplasmListName();
		$('#delete-folder-confirmation').html(deleteConfirmation + ' ' + currentFolderName + '?');

		$('#page-delete-germplasm-folder-message-modal').html('');
	}
}

function getSelectedGermplasmList() {
	return $('tr.selected');
}

function getSelectedGermplasmListId() {
	return $('tr.selected').attr('data-tt-id');
}

function getSelectedGermplasmListParentId() {
	return $('tr.selected').attr('data-tt-parent-id');
}

function getSelectedGermplasmListName() {
	return $('tr.selected .name').text();
}

function setSelectedGermplasmListName(name) {
	return $('tr.selected .name').text(name);
}

function isSelectedGermplasmListAFolder() {
	return $('tr.selected').attr('is-folder')==='1';
}