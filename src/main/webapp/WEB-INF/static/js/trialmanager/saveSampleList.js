/*globals displaySampleListTree, changeBrowseSampleButtonBehavior, displayAdvanceList,saveGermplasmReviewError */
/*globals $,showErrorMessage, showInvalidInputMessage, getDisplayedTreeName,listShouldNotBeEmptyError,getJquerySafeId,
 validateAllDates, saveListSuccessfullyMessage */
/*globals listParentFolderRequired, listNameRequired */
/*globals listDateRequired, listTypeRequired, moveToTopScreen */
/*globals TreePersist, showSuccessfulMessage, console, */
/*exported save, openSaveSampleListModal*/

var SaveSampleList = {};
var SampleList = {};
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

	SaveSampleList.openSaveSampleListModal = function(object) {
		SampleList = object;
		$('#selectSelectionVariableToSampleListModal').modal('hide');

		var sampleListTreeNode = $('#sampleFolderTree').dynatree('getTree');
		setTimeout(function() {

			$('#saveSampleListTreeModal').on('shown.bs.modal', function () {
				TreePersist.preLoadSampleTreeState(false, '#sampleFolderTree', true);
				$('#listName').val('');
				$('#listDate').val('');
				$('#listDescription').val('');
				$('#listNotes').val('');
				$('#listOwner').text(SampleList.createdBy);
			});
			$('#saveSampleListTreeModal').modal({ backdrop: 'static', keyboard: true });
/*			TreePersist.preLoadSampleTreeState(false, '#sampleFolderTree', true);
			$('#listName').val('');
			$('#listDate').val('');
			$('#listDescription').val('');
			$('#listNotes').val('');
			$('#listOwner').text(SampleList.createdBy);*/
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
		SampleList.listName = $('#listName').val();

		if ($('#listDate').val() === '') {
			showInvalidInputMessage(listDateRequired);
			return false;
		}

		var invalidDateMsg = validateAllDates();
		if (invalidDateMsg !== '') {
			showInvalidInputMessage(invalidDateMsg);
			return false;
		}
		SampleList.createdDate = $('#listDate').val();
		SampleList.description = $('#listDescription').val();
		SampleList.notes = $('#listNotes').val();

		var parentId = chosenNodeFolder.data.key;
		SampleList.parentId = parentId;

		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

		$.ajax({
			url: '/bmsapi/sampleLists/' + cropName + '/sampleList',
			type: 'POST',
			data: JSON.stringify(SampleList),
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
			success: function () {
				$('#saveSampleListTreeModal').modal('hide');
				showSuccessfulMessage('', saveListSuccessfullyMessage);
			}
		});
	};
})();

