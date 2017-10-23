/*globals displaySampleListTree, changeBrowseSampleButtonBehavior, displayAdvanceList,saveGermplasmReviewError */
/*globals $,showErrorMessage, showInvalidInputMessage, getDisplayedTreeName,listShouldNotBeEmptyError,getJquerySafeId,
 validateAllDates, saveListSuccessfullyMessage */
/*globals listParentFolderRequired, listNameRequired */
/*globals listDateRequired, listTypeRequired, moveToTopScreen */
/*globals TreePersist, showSuccessfulMessage, console */
/*exported save, openSaveSampleListModal*/

var SaveSampleList = {};
(function() {
	'use strict';
	SaveSampleList.initializeSampleListTree = function() {
		displaySampleListTree('sampleFolderTree', true, 1);
		changeBrowseSampleButtonBehavior(false);
		$('#saveSampleListTreeModal').off('hide.bs.modal');
		$('#saveSampleListTreeModal').on('hide.bs.modal', function() {
			TreePersist.saveSampleTreeState(false, '#sampleFolderTree');
		});
		$('#saveSampleListTreeModal').on('hidden.bs.modal', function() {
			$('#sampleFolderTree').dynatree('getTree').reload();
			changeBrowseSampleButtonBehavior(false);
		});
	};

	SaveSampleList.openSaveSampleListModal = function(details) {
		SaveSampleList.details = details;
		$('#selectSelectionVariableToSampleListModal').modal('hide');

		var sampleListTreeNode = $('#sampleFolderTree').dynatree('getTree');
		setTimeout(function() {
			$('#saveSampleListTreeModal').on('shown.bs.modal', function () {
				$(getDisplayedModalSelector() + ' #renameSampleFolderDiv').slideUp('fast');
				$('#listName').val('');
				$('#listDate').datepicker({ dateFormat: 'yyyy-mm-dd'}).datepicker("setDate", new Date());
				$('#listDescription').val('');
				$('#listNotes').val('');
				$('#listOwner').text(SaveSampleList.details.createdBy);
				TreePersist.preLoadSampleTreeState(false, '#sampleFolderTree', true);
			});
			$('#saveSampleListTreeModal').modal({ backdrop: 'static', keyboard: true });
		}, 300);
		//we preselect the program lists
		if (sampleListTreeNode !== null && sampleListTreeNode.getNodeByKey('LISTS') !== null) {
			sampleListTreeNode.getNodeByKey('LISTS').activate();
		}
	};

	SaveSampleList.save = function() {
		var chosenNodeFolder = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode();

		if (chosenNodeFolder === null) {
			showErrorMessage('page-save-list-message-modal', listParentFolderRequired);
			return false;
		}
		if ($('#listName').val() === '') {
			showInvalidInputMessage(listNameRequired);
			return false;
		}
		SaveSampleList.details.listName = $('#listName').val();

		if ($('#listDate').val() === '') {
			showInvalidInputMessage(listDateRequired);
			return false;
		}

		var invalidDateMsg = validateAllDates();
		if (invalidDateMsg !== '') {
			showInvalidInputMessage(invalidDateMsg);
			return false;
		}
		SaveSampleList.details.createdDate = $('#listDate').val();
		SaveSampleList.details.description = $('#listDescription').val();
		SaveSampleList.details.notes = $('#listNotes').val();

		var parentId = chosenNodeFolder.data.key;

		if (parentId === 'LISTS') {
			parentId = 0;
		}

		SaveSampleList.details.parentId = parentId;

		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

		$.ajax({
			url: '/bmsapi/sampleLists/' + cropName + '/sampleList',
			type: 'POST',
			data: JSON.stringify(SaveSampleList.details),
			contentType: "application/json",
			beforeSend: function (xhr) {
				xhr.setRequestHeader('X-Auth-Token', xAuthToken);
			},
			error: function (data) {
				if (data.status == 401) {
					bmsAuth.handleReAuthentication();
				} else if (data.status == 500) {
					showErrorMessage('page-save-list-message-modal', data.responseJSON.errors[0].message);
				} else if (data.status == 409) {
					showErrorMessage('page-save-list-message-modal', data.responseJSON.ERROR);

				}
			},
			success: function (response) {
				$('#saveSampleListTreeModal').modal('hide');
				showSuccessfulMessage('', saveListSuccessfullyMessage);
				displaySampleList(
					response.id, SaveSampleList.details.listName
					, false);
			}
		});
	};
})();

