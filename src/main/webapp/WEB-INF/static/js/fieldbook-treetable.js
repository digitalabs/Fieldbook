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
	if(obj.attr('has-children')==='1') {
		obj.attr('data-tt-branch', true);
	} else {
		obj.attr('data-tt-branch', false);
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
		if($(this).attr('has-children')==='1') {
			$(this).attr('data-tt-branch', true);
		} else {
			$(this).attr('data-tt-branch', false);
		}
	});

	updateTools();
	
	updateDraggableTableRows();
	
	updateDroppableTableRows();
	
	$('#germplasmTreeTable').treetable('unloadBranch', $('#germplasmTreeTable').treetable('node','LOCAL'));
	
	updateTableRowsBgColor();
	
	$('#germplasmTreeTable .file').each(function() {
		updateDoubleClickEvent($(this));
	});
}
function updateTools() {
	$('#germplasmTreeTable tbody').on('mousedown', 'tr', function() {
		$('tr.selected').removeClass('selected');
		$(this).addClass('selected');
		if($(this).attr('data-tt-id')==='CENTRAL' ||
				($(this).attr('data-tt-id')!=='LOCAL' && $(this).attr('data-tt-id')>'0')) {
			changeBrowseGermplasmButtonBehavior(false);
			hideAddGermplasmFolderDiv();
			hideRenameGermplasmFolderDiv();
		} else if($(this).attr('data-tt-id')==='LOCAL') {
			changeBrowseGermplasmButtonBehavior(true);
			$('.edit-germplasm-folder').addClass('disable-image');
			$('.delete-germplasm-folder').addClass('disable-image');
			hideRenameGermplasmFolderDiv();
		} else if($(this).attr('has-children')==='1') {
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
		var hasChildren = $(this).parents('tr').attr('has-children');
		if(rowId !== 'CENTRAL' && rowId !== 'LOCAL' && rowId < '0') {
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
	$('#germplasmTreeTable .folder').parents('tr').filter(function() {
		  return $(this).attr('data-tt-id') === 'LOCAL' || $(this).attr('data-tt-id') < '0';
	}).each(function() {
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
		 	var hasChildren = sourceNodeObj.attr('has-children');
		 	var sourceNodeName = sourceNodeObj.find('.name').first().text();
		 	if(hasChildren === '1') {
		 		showErrorMessage(getMessageErrorDiv(), cannotMove + ' ' + sourceNodeName + ' ' + hasChildrenString);
		 		return false;
		 	}
		 	targetNode = $('#germplasmTreeTable').treetable('node', $(this).data('ttId'));
		 	moveGermplasmListInTreeTable(sourceNode, targetNode);
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
function moveGermplasmListInTreeTable(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.id,
		targetId = targetNode.id;

	if (targetId === 'LOCAL') {
		targetId = 1;
	}

	$.ajax({
		url: '/Fieldbook/ListTreeManager/moveGermplasmFolder',
		type: 'POST',
		data: 'sourceId=' + sourceId + '&targetId=' + targetId,
		cache: false,
		success: function(data) {
			$('#germplasmTreeTable').treetable('move', sourceNode.id, targetNode.id);
			updateTableRowsBgColor();
			var targetNodeObj = $('tr[data-tt-id='+targetNode.id+']').attr('has-children', '1');
			updateExpandEventIfHasChildren(targetNodeObj);
		}
	});
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
        if (parentFolderId === 'LOCAL') {
            parentFolderId = 1;
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

    if (id > 1 || id === 'CENTRAL' || id === 'LOCAL') {
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
        if (parentFolderId === 'LOCAL') {
            parentFolderId = 1;
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