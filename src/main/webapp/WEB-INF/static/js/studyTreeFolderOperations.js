/* global Spinner:false */
/* global showErrorMessage */
/* global deleteConfirmation */
/* global deleteNurseryConfirmation */
/* global deleteFolderTitle */
/* global deleteNurseryTitle */
function addFolder(object) {
	'use strict';
	if (!$(object).hasClass('disable-image')) {
		hideRenameFolderDiv();
		hideStudyTypeDiv();
		$('#addFolderName', '#studyTreeModal').val('');
		$('#addFolderDiv', '#studyTreeModal').slideDown('fast');
	}
}

function hideAddFolderDiv() {
	'use strict';
	$('#addFolderDiv', '#studyTreeModal').slideUp('fast');
}

function hideRenameFolderDiv() {
	'use strict';
	$('#renameFolderDiv', '#studyTreeModal').slideUp('fast');
}

function renameStudy(object) {
	if (!$(object).hasClass('disable-image')) {
		hideAddFolderDiv();
		hideStudyTypeDiv();
		hideRenameFolderDiv();
		$('#renameStudyDiv', '#studyTreeModal').slideDown('fast');
		currentStudy = $('#studyTree').dynatree('getTree').getActiveNode().data.title;
		$('#newStudyName', '#studyTreeModal').val(currentStudy);
	}
}

function renameFolder(object) {
	'use strict';

	const isFolder = $('#studyTree').dynatree('getTree').getActiveNode().data.isFolder;
	if(!isFolder) {
		renameStudy(object);
	}
	else{
		var currentFolderName;

		if (!$(object).hasClass('disable-image')) {
			hideAddFolderDiv();
			hideStudyTypeDiv();
			hideRenameStudyDiv();
			$('#renameFolderDiv', '#studyTreeModal').slideDown('fast');
			currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title;
			$('#newFolderName', '#studyTreeModal').val(currentFolderName);
		}
	}
}

function submitRenameFolder() {
	'use strict';

	var folderName = $.trim($('#newFolderName', '#studyTreeModal').val()),
			parentFolderId;

	var activeStudyNode = $('#studyTree').dynatree('getTree').getActiveNode();

	if (activeStudyNode === null || activeStudyNode.data.isFolder === false || activeStudyNode.data.key === 'LOCAL') {
		showErrorMessage('', studyProgramFolderRequired);
		return false;
	}

	if ($.trim(folderName) === activeStudyNode.data.title) {
		$('#renameFolderDiv', '#studyTreeModal').slideUp('fast');
		return false;
	}
	if (folderName === '') {
		showErrorMessage('page-rename-study-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else if (!isValidInput(folderName)) {
		showErrorMessage('page-rename-study-folder-message-modal', invalidFolderNameCharacterMessage);
		return false;
	} else {
		parentFolderId = activeStudyNode.data.key;

		if (parentFolderId === 'LOCAL') {
			parentFolderId = 1;
		}

		$.ajax({
			url: '/Fieldbook/StudyTreeManager/renameStudyFolder',
			type: 'POST',
			data: 'folderId=' + parentFolderId + '&newFolderName=' + folderName,
			cache: false,
			success: function(data) {
				var node;
				if (data.isSuccess === '1') {
					hideRenameFolderSection();
					node = $('#studyTree').dynatree('getTree').getActiveNode();
					node.data.title = folderName;
					$(node.span).find('a').html(folderName);
					node.focus();
					showSuccessfulMessage('', renameItemSuccessful);
				} else {
					showErrorMessage('page-rename-study-folder-message-modal', data.message);
				}
			}
		});
	}
}
