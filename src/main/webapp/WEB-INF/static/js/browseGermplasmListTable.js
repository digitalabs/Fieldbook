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
	angular.element(document.getElementById('germplasm-table')).scope().resetStudyEntries();

}

function showChangeEntryTypeModal(entryId, currentValue, studyEntryPropertyId) {
	'use strict';
	if ((!isOpenStudy()) || (isOpenStudy() && !hasGeneratedDesign())) {
		angular.element(document.getElementById('germplasm-table')).scope().showPopOverCheck(entryId, currentValue, studyEntryPropertyId);
	}
}
