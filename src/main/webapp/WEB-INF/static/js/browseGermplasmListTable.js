/*exported startIndex,germplasmStartingEntry,importLocationUrl,importIframeOpened,overwriteChecksList*/
/*exported confirmReplaceList,resetGermplasmList,removeCheckFromList,openListTree,viewGermplasmLitDetails */
/*exported additionalLazyLoadUrl, chooseList*/
/*globals angular*/
var addedGid = {},
	listId = 0,
	makeDraggableBool = true,

	startIndex = 0,
	lastDraggedPrimaryList = 0,
	lastDraggedChecksList = 0,

	itemsToAdd = [],
	checksFromPrimary = 0,

	itemsIndexAdded = [];

var makeCheckDraggableBool = true;

//to be use as reference to the data table object
var germplasmDataTable = null,
		selectedCheckListDataTable = null;

function resetGermplasmList() {
	'use strict';
	$('#imported-germplasm-list').html('<h3></h3>');
	$('#imported-germplasm-list-reset-button').css('opacity', '0');
	$('#entries-details').css('display', 'none');
	$('#numberOfEntries').text('');
	listId = 0;
	lastDraggedPrimaryList = 0;

	$.ajax({
		url: '/Fieldbook/StudyManager/importGermplasmList/resetStudyGermplasmDetails',
		type: 'GET',
		cache: false,
		async: false,
		success: function() {
		}
	});
	if (angular && angular.element("#manage-study-tabs [ui-view='germplasm']").length === 1) {
		var _scope = angular.element("#manage-study-tabs [ui-view='germplasm']").scope();

		setTimeout(function () {
			_scope.$apply(function () {
				_scope.germplasmListCleared();
				_scope.updateOccurred = false;
			});
		}, 1);

	}


}

function showPopoverCheck(index, sectionContainer, bodyContainer) {
	'use strict';
	//if replace has been clicked or if new study or if there are no measurement rows saved yet for study
	var isShowPopOver =
		(!isOpenStudy()) || (isOpenStudy() && !hasGeneratedDesign());
	if (isShowPopOver) {
		var currentCheckVal = $(sectionContainer + ' #selectedCheck' + index).val(),
			realIndex = index,
			popoverOptions = {},
			listDataTable = germplasmDataTable;
		//we need to get the real index of the check

		if (listDataTable != null) {
			listDataTable.getDataTable().$('.check-hidden').each(function(indexCount) {
				if ($(this).attr('id') === 'selectedCheck' + index) {
					realIndex = indexCount;
					return;
				}
			});
		}
		$.ajax({
			url: '/Fieldbook/TrialManager/GermplasmList/edit/check/' + index + '/' + realIndex,
			type: 'GET',
			data: 'currentVal=' + currentCheckVal,
			cache: false,
			success: function(data) {
				popoverOptions = {
					html: true,
					title: 'Edit Check',
					content: data,
					trigger: 'manual',
					placement: 'right',
					container: 'body'
				};
				$('body .popover').remove();
				$(sectionContainer + ' .edit-check' + index).popover('destroy');
				$(sectionContainer + ' .edit-check' + index).popover(popoverOptions);
				$(sectionContainer + ' .edit-check' + index).popover('show');
			}
		});
	}
}
