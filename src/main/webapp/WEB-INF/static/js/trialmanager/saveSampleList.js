/*globals displaySampleListTree, changeBrowseSampleButtonBehavior, displayAdvanceList,saveGermplasmReviewError */
/*globals $,showErrorMessage, showInvalidInputMessage, getDisplayedTreeName,ImportCrosses,listShouldNotBeEmptyError,getJquerySafeId,
 validateAllDates, saveListSuccessfullyMessage */
/*globals listParentFolderRequired, listNameRequired */
/*globals listDateRequired, listTypeRequired, moveToTopScreen */
/*globals TreePersist, showSuccessfulMessage, console, germplasmEntrySelectError */
/*exported saveGermplasmList, openSaveSampleListModal*/

// TODO saveGermplasmList = cambiar. germplasmEntrySelectError ??
var SaveSampleList = {};
var sampleList = {};
var programUUID = undefined;
(function() {
	'use strict';
	SaveSampleList.initializeSampleListTree = function() {
		displaySampleListTree('sampleFolderTree', true, 1);// TODO Carga el Arbol
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
	SaveSampleList.clearForm = function(){

		//document.getElementById('listOwner').innerText = sampleList.createdBy;
		$('#listOwner').val(sampleList.createdBy);
		$('#listDate').val("");
		$('#listDescription').val("");
		$('#listNotes').val("");
	};

	SaveSampleList.openSaveSampleListModal = function(object) {
		console.log(object);
		sampleList = object;
		$('#selectSelectionVariableToSampleListModal').modal('hide');

		/*		if (parseInt($('#reviewAdvanceNurseryModal .total-review-items').html(), 10) < 1) {
					showErrorMessage('', saveGermplasmReviewError);
					return false;
				}
				$('#reviewAdvanceNurseryModal').modal('hide');*/

/*
		var listIdentifier = $(object).attr('id'),
*/
		var sampleListTreeNode = $('#sampleFolderTree').dynatree('getTree'),
			additionalLazyLoadUrl = '/1';
/*		$.ajax(
			{
				url: '/Fieldbook/SampleListTreeManager/saveList/' + listIdentifier,
				type: 'GET',
				cache: false,
				success: function(html) {
					// WTF $('#saveSampleListTreeModal').data('is-save-crosses', '0');
					$('#saveSampleRightSection').html(html);
					setTimeout(function() {
						$('#saveSampleListTreeModal').modal({ backdrop: 'static', keyboard: true });
						TreePersist.preLoadSampleTreeState(false, '#sampleFolderTree', true);
					}, 300);
					//we preselect the program lists
					if (sampleListTreeNode !== null && sampleListTreeNode.getNodeByKey('LISTS') !== null) {
						sampleListTreeNode.getNodeByKey('LISTS').activate();
					}
				}
			}
		);*/
		setTimeout(function() {
			$('#saveSampleListTreeModal').modal({ backdrop: 'static', keyboard: true });
			//TreePersist.preLoadSampleTreeState(false, '#sampleFolderTree', true);
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
		sampleList.listName = $('#listName').val();

		if ($('#listDate').val() === '') {
			showInvalidInputMessage(listDateRequired);
			return false;
		}

		var invalidDateMsg = validateAllDates();
		if (invalidDateMsg !== '') {
			showInvalidInputMessage(invalidDateMsg);
			return false;
		}
		sampleList.createdDate = $('#listDate').val();
		sampleList.description = $('#listDescription').val();
		sampleList.notes = $('#listNotes').val();

		var parentId = chosenNodeFolder.data.key;
		sampleList.parentId = parentId;

		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

		$.ajax({
			url: '/bmsapi/sampleLists/' + cropName + '/sampleList',
			type: 'POST',
			data: JSON.stringify(sampleList),
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

