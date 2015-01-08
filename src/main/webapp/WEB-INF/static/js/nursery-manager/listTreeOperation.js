/*globals showErrorMessage, getDisplayedTreeName, getDisplayedModalSelector*/

var ListTreeOperation = {};
 
(function() {
	'use strict';
	ListTreeOperation.createGermplasmFolder = function() {
        'use strict';
        var node = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode();
        if (!node) {
            showErrorMessage('page-add-message-modal', invalidNodeGermplasmTreeMessage);
            return false;
        }
        if (!node.data.isFolder) {
            showErrorMessage('page-add-message-modal', addGermplasmInvalidFolderMessage);
            return false;
        }
        var folderName = $.trim($(getDisplayedModalSelector() + ' #addFolderName').val()),
                parentFolderId;
        if (folderName === '') {
            showErrorMessage('page-add-message-modal', folderNameRequiredMessage);
            return false;
        } else if (!isValidInput(folderName)) {
            showErrorMessage('page-add-message-modal', invalidFolderNameCharacterMessage);
            return false;
        } else {
            parentFolderId = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.key;
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
                        // Lazy load the node
                        var node = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode();
                        doGermplasmLazyLoad(node);
                        node.focus();
                        node.expand();
                        $(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').slideUp('fast');
                        showSuccessfulMessage('', addFolderSuccessful);
                    } else {
                        showErrorMessage('page-add-message-modal', data.message);
                    }
                }
            });
        }
        return false;
    };
    ListTreeOperation.addGermplasmFolder = function(object) {
        'use strict';
        if (!$(object).hasClass('disable-image')) {
        	$(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').slideUp('fast');
            $(getDisplayedModalSelector() + ' #addFolderName').val('');
            $(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').slideDown('fast');
        }
    };

    ListTreeOperation.renameGermplasmFolder = function(object) {
        'use strict';
        var currentFolderName;
        if (!$(object).hasClass('disable-image')) {
            currentFolderName = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title;
            $(getDisplayedModalSelector() + ' #renameFolderName').val(currentFolderName);
            $(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').slideDown('fast');
        }
    };
    
    ListTreeOperation.hideAddGermplasmFolderDiv = function() {
    	'use strict';
    	$(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').slideUp('fast');
    };
    
    ListTreeOperation.hideRenameGermplasmFolderDiv = function() {
    	'use strict';
    	$(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').slideUp('fast');
    };
    
    ListTreeOperation.hideDeleteGermplasmFolderDiv = function() {
    	'use strict';
    	$(getDisplayedModalSelector() + ' #deleteGermplasmFolderDiv').slideUp('fast');
    };

    ListTreeOperation.submitRenameGermplasmFolder = function() {
        'use strict';
        var folderName = $.trim($(getDisplayedModalSelector() + ' #renameFolderName').val()),
                parentFolderId;
        var node = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode();
        if (! node) {
            showErrorMessage('page-rename-message-modal',invalidNodeGermplasmTreeMessage);
            return false;
        }
        if (!node.data.isFolder || node.data.key === 'LISTS') {
            showErrorMessage('page-rename-message-modal', renameGermplasmInvalidFolderMessage);
            return false;
        }
        if ($.trim(folderName) === $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title) {
        	$(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').slideUp('fast');
            return false;
        }
        if (folderName === '') {
            showErrorMessage('page-rename-message-modal', folderNameRequiredMessage);
            return false;
        } else if (!isValidInput(folderName)) {
            showErrorMessage('page-rename-message-modal', invalidFolderNameCharacterMessage);
            return false;
        } else {
            parentFolderId = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.key;
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
                    	$(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').slideUp('fast');
                        node = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode();
                        node.data.title = folderName;
                        $(node.span).find('a').html(folderName);
                        node.focus();
                        showSuccessfulMessage('', renameItemSuccessful);
                    } else {
                        showErrorMessage('page-rename-message-modal', data.message);
                    }
                }
            });
        }
    };
    
    ListTreeOperation.deleteGermplasmFolder = function(object) {
    	'use strict';
    	var currentFolderName;
    	if (!$(object).hasClass('disable-image')) {
    		$('#deleteFolderModal').modal('show');
    		hideAddGermplasmFolderDiv();
    		hideRenameGermplasmFolderDiv();
    		currentFolderName = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title;
    		$('#delete-folder-confirm').html(deleteConfirmation + ' ' + currentFolderName + '?');
    		$('#page-delete-germplasm-folder-message-modal').html('');
    	}
    };

    
    ListTreeOperation.submitDeleteGermplasmFolder = function() {
    	'use strict';
    	var folderId = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.key;
    	$.ajax({
    		url: '/Fieldbook/ListTreeManager/deleteGermplasmFolder',
    		type: 'POST',
    		data: 'folderId=' + folderId,
    		cache: false,
    		success: function(data) {
    			var node;
    			if (data.isSuccess === '1') {
    				$('#deleteFolderModal').modal('hide');
    				node = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode();
    				node.remove();
                    showSuccessfulMessage('',deleteItemSuccessful);
    			} else {
    				showErrorMessage('page-delete-message-modal', data.message);
    			}
    		}
    	});
    };
})();