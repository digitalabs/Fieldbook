$(function() {
    'use strict';

    // attach spinner operations to ajax events
    $(document).ajaxStart(function() {
        SpinnerManager.addActive();
    }).ajaxStop(function() {
        SpinnerManager.resolveActive();
    }).ajaxError(function(){
        showErrorMessage('', ajaxGenericErrorMsg);
        SpinnerManager.resolveActive();
    });

	if (typeof convertToSelect2 === 'undefined' || convertToSelect2) {
		// Variable is undefined
		$('select').each(function() {
			$(this).select2({minimumResultsForSearch: 20});
		});
	}

    function measureScrollBar() {
        // david walsh
        var inner = document.createElement('p');
        inner.style.width = "100%";
        inner.style.height = "200px";

        var outer = document.createElement('div');
        outer.style.position = "absolute";
        outer.style.top = "0px";
        outer.style.left = "0px";
        outer.style.visibility = "hidden";
        outer.style.width = "200px";
        outer.style.height = "150px";
        outer.style.overflow = "hidden";
        outer.appendChild (inner);

        document.body.appendChild (outer);
        var w1 = inner.offsetWidth;
        outer.style.overflow = 'scroll';
        var w2 = inner.offsetWidth;
        if (w1 == w2) w2 = outer.clientWidth;

        document.body.removeChild (outer);

        return (w1 - w2);
    }

    $(document.body)
        .on('show.bs.modal', function () {
            if (this.clientHeight < window.innerHeight) {return;}

            var scrollbarWidth = measureScrollBar();
            if (scrollbarWidth) {
                $(document.body).css('padding-right', scrollbarWidth);
            }
        })
        .on('hidden.bs.modal', function () {
            $(document.body).css('padding-right', 0);
        });

    

});

function isStudyNameUnique(studyName, studyId) {
    'use strict';
    if (!studyId) {
        studyId = 0;
    }

    var isUnique = true;
    $.ajax({
        url: '/Fieldbook/StudyTreeManager/isNameUnique',
        type: 'POST',
        data: 'studyId=' + studyId + '&name=' + studyName,
        cache: false,
        async: false,
        success: function (data) {
            if (data.isSuccess == 1) {
                isUnique = true;
            } else {
                isUnique = false;
            }
        }
    });
    return isUnique;
}

function validateStartEndDateBasic(startDate, endDate) {

    'use strict';

    startDate = startDate == null ? '' : startDate.replace(/-/g, '');
    endDate = endDate == null ? '' : endDate.replace(/-/g, '');

    if (startDate === '' && endDate === '') {
        return true;
    } else if (startDate !== '' && endDate === '') {
        return true;
    } else if (startDate === '' && endDate !== '') {
        return startDateRequiredError;
    } else if (parseInt(startDate) > parseInt(endDate)) {
        return startDateRequiredEarlierError;
    }

    return true;

}

function doAjaxMainSubmit(pageMessageDivId, successMessage, overrideAction) {
    'use strict';

	var form = $('form'),
		action = form.attr('action'),
		serializedData = form.serialize();

	if (overrideAction) {
		action = overrideAction;
	}

	$.ajax({
		url: action,
		type: 'POST',
		data: serializedData,
		success: function(html) {
			// Paste the whole html
			$('.container .row').first().html(html);
			if (pageMessageDivId) {
				showSuccessfulMessage(pageMessageDivId, successMessage);
			}
		}
	});
}

function showPage(paginationUrl, pageNum, sectionDiv) {
	'use strict';


	$.ajax({
		url: paginationUrl + pageNum,
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {

			var tableId,
				gid,
				idVal,
				indexItems,
				rowIndex;

			$('#' + sectionDiv).html(html);

			if (sectionDiv === 'trial-details-list' || sectionDiv === 'nursery-details-list') {
				// We highlight the previously clicked
				for (tableId in selectedTableIds) {
					idVal = selectedTableIds[tableId];
					if (idVal != null) {
						// We need to highlight
						$('tr.data-row#' + idVal).addClass('field-map-highlight');
					}
				}
			} else if (sectionDiv === 'inventory-germplasm-list') {
				// We highlight the previously clicked
				for (gid in selectedGids) {
					idVal = selectedGids[gid];
					if (idVal !== null) {
						// We need to highlight
						$('tr.primaryRow[data-gid=' + idVal + ']').addClass('field-map-highlight');
					}
				}
			}

			if (sectionDiv === 'imported-germplasm-list') {
				makeDraggable(makeDraggableBool);

				// Highlight
				if (itemsIndexAdded && itemsIndexAdded.length > 0) {
					for (indexItems = 0; indexItems < itemsIndexAdded.length ; indexItems++) {
						if (itemsIndexAdded[indexItems] != null) {
							rowIndex = itemsIndexAdded[indexItems].index;
							if ($('.primaryRow[data-index=""+rowIndex+""]').length !== 0) {
								$('.primaryRow[data-index=""+rowIndex+""]').css('opacity', '0.5');
							}
						}
					}
				}
			}
		}
	});
}

function showMultiTabPage(paginationUrl, pageNum, sectionDiv, sectionContainerId, paginationListIdentifier) {
	'use strict';

	$.ajax({
		url: paginationUrl + pageNum + '?listIdentifier=' + paginationListIdentifier,
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {
			var paginationDiv = '#' + sectionContainerId + ' #' + sectionDiv;
			$(paginationDiv + ':eq(0)').html('');
			$(paginationDiv + ':eq(0)').html(html);
		}
	});
}

function showPostPage(paginationUrl, previewPageNum, pageNum, sectionDiv, formName) {
    'use strict';
	var $form,
		completeSectionDivName,
		serializedData;

	if (formName.indexOf('#') > -1) {
		$form = $(formName);
	} else {
		$form = $('#' + formName);
	}

	if (sectionDiv.indexOf('#') > -1) {
		completeSectionDivName = sectionDiv;
	} else {
		completeSectionDivName = '#' + sectionDiv;
	}

	serializedData = $form.serialize();

	$.ajax({
		url: paginationUrl + pageNum + '/' + previewPageNum + '?r=' + (Math.random() * 999),
		type: 'POST',
		data: serializedData,
		cache: false,
		timeout: 70000,
		success: function(html) {
			$(completeSectionDivName).empty().append(html);

			if (sectionDiv == 'trial-details-list' || sectionDiv == 'nursery-details-list') {
				// We highlight the previously clicked
				for (var index in selectedTableIds) {
					var idVal = selectedTableIds[index];
					if (idVal != null) {
						// We need to highlight
						$('tr.data-row#' + idVal).addClass('field-map-highlight');
					}
				}
			}

			if (sectionDiv == 'check-germplasm-list') {
				makeCheckDraggable(makeCheckDraggableBool);
			}

		}
	});
}

function triggerFieldMapTableSelection(tableName) {

	var id;

	$('#' + tableName + ' tr.data-row').on('click', function() {
		if (tableName == 'studyFieldMapTree') {
			$(this).toggleClass('trialInstance');
			$(this).toggleClass('field-map-highlight');

		} else {
			$(this).toggleClass('field-map-highlight');
			id = $(this).attr('id') + '';
			if ($(this).hasClass('field-map-highlight')) {
				selectedTableIds[id] = id;
			} else {
				selectedTableIds[id] = null;
			}
		}
	});
}

function createFieldMap(tableName) {

	var ids = [],
		index,
		idVal,
		idList;
	
	if($('.import-study-data').data('data-import') === '1'){
		showErrorMessage('', needSaveImportDataError);
		return;
	}

	if ($('#' + tableName + ' .field-map-highlight').attr('id') != null || tableName == 'nursery-table' || tableName == 'trial-table') {
		// Get selected studies
		if ($('#createNurseryMainForm #studyId').length  === 1) {
			ids.push($('#createNurseryMainForm #studyId').val());
		} else {
			ids.push(getCurrentStudyIdInTab());
		}
		idList = ids.join(',');
		$('#page-message').html('');

		// Show pop up to select instances/dataset for field map creation
		showFieldMapPopUpCreate(tableName, idList);
	} else {
		showErrorMessage('', fieldMapStudyRequired);
	}
}

// FIXME obsolete
function checkTrialOptions(id) {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/' + id,
		type: 'GET',
		data: '',
		cache: false,
		success: function(data) {
			if (data.nav == '0') {
				$('#manageTrialConfirmation').modal('show');
			} else if (data.nav == '1') {
				var fieldMapHref = $('#fieldmap-url').attr('href');
				location.href = fieldMapHref + '/' + id;
			}
		}
	});
}

// FIXME obsolete
function createNurseryFieldmap(id) {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/' + id,
		type: 'GET',
		data: '',
		cache: false,
		success: function(data) {
			if (data.nav == '0') {
				$('#manageTrialConfirmation').modal('show');
				$('#fieldmapDatasetId').val(data.datasetId);
				$('#fieldmapGeolocationId').val(data.geolocationId);
			} else if (data.nav == '1') {
				var fieldMapHref = $('#fieldmap-url').attr('href');
				location.href = fieldMapHref + '/' + id;
			}
		}
	});
}

function proceedToCreateFieldMap() {
	$('#manageTrialConfirmation').modal('hide');
	var fieldMapHref = $('#fieldmap-url').attr('href');
	location.href = fieldMapHref + '/' + $('#fieldmapStudyId').val();
}

function proceedToGenerateFieldMap() {
	$('#manageTrialConfirmation').modal('hide');
	location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/nursery/' +
		$('#fieldmapDatasetId').val() + '/' + $('#fieldmapGeolocationId').val();
}

function getJquerySafeId(fieldId) {
	return replaceall(fieldId, '.', '\\.');
}

function replaceall(str, replace, withThis) {
	var strHasil = '',
		temp;

	for (var i = 0; i < str.length; i++) { // not need to be equal. it causes the last change: undefined..
		if (str[i] == replace) {
			temp = withThis;
		} else {
			temp = str[i];
		}
		strHasil += temp;
	}
	return strHasil;
}

function isInt(value) {
	if ((undefined === value) || (null === value) || (value === '')) {
		return false;
	}
	return value % 1 === 0;
}
function isFloatNumber(val) {
	if(!val || (typeof val != "string" || val.constructor != String)) {
		return(false);
	}
	var isNumber = !isNaN(new Number(val));
	if(isNumber) {
		if(val.indexOf('.') != -1) {
			return(true);
		} else {
			return isInt(val);
		}
	} else {
		return(false);
	}
}

function selectTrialInstance(tableName) {
	if (tableName == 'trial-table') {
		$.ajax({
			url: '/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance',
			type: 'GET',
			cache: false,
			data: '',
			success: function(data) {
				if (data.fieldMapInfo != null && data.fieldMapInfo != '') {
					if (parseInt(data.size) > 1) {
						// Show popup to select fieldmap to display
						clearStudyTree();
						isViewFieldmap = true;
						createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap, tableName);
						$('#selectTrialInstanceModal').modal('toggle');
					} else {
						// Redirect to step 3
						var fieldMapInfo = $.parseJSON(data.fieldMapInfo);
						var datasetId = data.datasetId;
						var geolocationId = data.geolocationId;
						location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/' + datasetId + '/' + geolocationId;
					}
				}
			}
		});
	} else {
		// Redirect to step 3 for nursery
		var datasetId = $('#fieldmapDatasetId').val();
		var geolocationId = $('#fieldmapGeolocationId').val();
		location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/nursery/' + datasetId + '/' + geolocationId;
	}
}

function selectTrialInstanceCreate(tableName) {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance',
		type: 'GET',
		async: false,
		cache: false,
		data: '',
		success: function(data) {
			if (data.fieldMapInfo != null && data.fieldMapInfo != '') {
				// Show popup to select instances to create field map
				clearStudyTree();
				isViewFieldmap = false;
				createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap, tableName);
				$('#selectTrialInstanceModal').modal('toggle');
			}
		}
	});
}

function createStudyTree(fieldMapInfoList, hasFieldMap, tableName) {
	var hasOneInstance = false;
	createHeader(hasFieldMap);
	$.each(fieldMapInfoList, function(index, fieldMapInfo) {
		createRow(getPrefixName('study', fieldMapInfo.fieldbookId), '', fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, hasFieldMap, hasOneInstance);
		$.each(fieldMapInfo.datasets, function(index, value) {
			hasOneInstance = fieldMapInfoList.length === 1 && fieldMapInfoList[0].datasets.length === 1 && fieldMapInfoList[0].datasets[0].trialInstances.length === 1;
			if (tableName == 'trial-table') {
				// Create trial study tree up to instance level
				createRow(getPrefixName('dataset', value.datasetId), getPrefixName('study', fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, hasFieldMap, hasOneInstance);
				$.each(value.trialInstances, function(index, childValue) {
					if ((hasFieldMap && childValue.hasFieldMap) || !hasFieldMap) {
						createRow(getPrefixName('trialInstance', childValue.geolocationId), getPrefixName('dataset', value.datasetId), childValue, childValue.geolocationId, hasFieldMap, hasOneInstance);
					}
				});
			} else {
				// If dataset has an instance, show up to the dataset level
				if (value.trialInstances.length > 0) {
					$.each(value.trialInstances, function(index, childValue) {
						createRowForNursery(getPrefixName('trialInstance', childValue.geolocationId),
								getPrefixName('study', fieldMapInfo.fieldbookId), childValue, childValue.geolocationId,
								hasFieldMap, value.datasetName, value.datasetId, hasOneInstance);
					});
				}
			}
		});
	});

	// Set bootstrap ui
	$('.tree').treegrid();

	$('.tr-expander').on('click', function() {
		triggerExpanderClick($(this));
	});
	$('.treegrid-expander').on('click', function() {
		triggerExpanderClick($(this).parent().parent());

	});

	// Set as highlightable
	if (hasFieldMap) {
		triggerFieldMapTableSelection('studyFieldMapTree');

	}
	styleDynamicTree('studyFieldMapTree');
}

function getPrefixName(cat, id) {
	if (parseInt(id) > 0) {
		return cat + id;
	} else {
		return cat + 'n' + (parseInt(id) * -1);
	}
}

function triggerExpanderClick(row) {
	if (row.treegrid('isExpanded')) {
		row.treegrid('collapse');
	} else {
		row.treegrid('expand');
	}
}

function createHeader(hasFieldMap) {
	var newRow = '<thead><tr>';

	if (!hasFieldMap) {
		if (trial) {
			newRow = newRow + '<th style="width:45%">' + trialName + '</th>' +
				'<th style="width:10%">' + entryLabel + '</th>' +
				'<th style="width:10%">' + repLabel + '</th>' +
				'<th style="width:20%">' + plotLabel + '</th>';
		} else {
			newRow = newRow + '<th style="width:65%">' + nurseryName + '</th>' +
			'<th style="width:20%">' + entryPlotLabel + '</th>';
		}
		newRow = newRow + '<th style="width:15%">' + fieldmapLabel + '</th>';
	} else {
		if (trial) {
			newRow = newRow + '<th style="width:40%"></th>' +
				'<th style="width:20%">' + entryLabel + '</th>' +
				'<th style="width:20%">' + repLabel + '</th>' +
				'<th style="width:20%">' + plotLabel + '</th>';
		} else {
			newRow = newRow + '<th style="width:60%"></th>' +
			'<th style="width:40%">' + entryPlotLabel + '</th>';
		}
	}
	newRow = newRow + '</tr></thead>';
	$('#studyFieldMapTree').append(newRow + '<tbody></tbody>');
}

function createRowForNursery(id, parentClass, value, realId, withFieldMap, datasetName, datasetId, hasOneInstance) {
	var genClassName = 'treegrid-',
		genParentClassName = '',
		newRow = '',
		newCell = '',
		hasFieldMap,
		disabledString,
		checkBox;

	if (parentClass !== '') {
		genParentClassName = 'treegrid-parent-' + parentClass;
	}

	// For create new fieldmap
	hasFieldMap = value.hasFieldMap ? 'Yes' : 'No';
	disabledString = value.hasFieldMap ? 'disabled' : '';
	var checked = hasOneInstance ? 'checked' : '';

	newRow = '<tr class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
	checkBox = '<input ' + disabledString + ' class="checkInstance" type="checkbox" id="' + datasetId + '|' + realId + '" ' + checked + ' /> &nbsp;&nbsp;';
	newCell = '<td>' + checkBox + '&nbsp;' + datasetName + '</td><td>' + value.entryCount + '</td>';
	newCell = newCell + '<td class="hasFieldMap">' + hasFieldMap + '</td>';
	$('#studyFieldMapTree').append(newRow + newCell + '</tr>');
}

function createRow(id, parentClass, value, realId, withFieldMap, hasOneInstance) {
	var genClassName = 'treegrid-',
		genParentClassName = '',
		newRow = '',
		newCell = '',
		hasFieldMap,
		disabledString,
		checkBox;

	if (parentClass !== '') {
		genParentClassName = 'treegrid-parent-' + parentClass;
	}

	if (id.indexOf('study') > -1 || id.indexOf('dataset') > -1) {
		// Study and dataset level
		newRow = '<tr id="' + realId + '" class="tr-expander ' + genClassName + id + ' ' + genParentClassName + '">';

		if (trial) {
			newCell = newCell + '<td>' + value + '</td><td></td><td></td><td></td>';
		} else {
			newCell = newCell + '<td>' + value + '</td><td></td>';
		}
		if (!withFieldMap) {
			newCell = newCell + '<td></td>';
		}
	} else {
		// Trial instance level
		if (withFieldMap) {
			// For view fieldmap
			newRow = '<tr id="' + realId + '" class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
			newCell = '<td>' + value.trialInstanceNo + '</td><td>' + value.entryCount + '</td>';
			if (trial) {
				newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
			}
		} else {
			// For create new fieldmap
			hasFieldMap = value.hasFieldMap ? 'Yes' : 'No';
			disabledString = value.hasFieldMap ? 'disabled' : '';
			var checked = hasOneInstance ? 'checked' : '';

			newRow = '<tr class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
			checkBox = '<input ' + disabledString + ' class="checkInstance" type="checkbox" id="' + realId + '" ' + checked + ' /> &nbsp;&nbsp;';
			newCell = '<td>' + checkBox + '&nbsp;' + value.trialInstanceNo + '</td><td>' + value.entryCount + '</td>';
			if (trial) {
				newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
			}
			newCell = newCell + '<td class="hasFieldMap">' + hasFieldMap + '</td>';
		}
	}
	$('#studyFieldMapTree').append(newRow + newCell + '</tr>');
}

function clearStudyTree() {
	$('#studyFieldMapTree').empty();
}

function showMessage(message) {
	createErrorNotification(errorMsgHeader,message);
}

function createLabelPrinting(tableName) {

	var count = 0,
		idVal = null,
		index,
		tempVal,
		labelPrintingHref,
		id,
		type;
	
	if($('.import-study-data').data('data-import') === '1'){
		showErrorMessage('', needSaveImportDataError);
		return;
	}

	if ($('#createNurseryMainForm #studyId').length === 1) {
		idVal = ($('#createNurseryMainForm #studyId').val());
		count++;
	} else {
		idVal = getCurrentStudyIdInTab();
		count++;
	}

	if (count !== 1) {
		showMessage(createLabelErrorMsg);
		return;
	}

	if (idVal !== null) {
		labelPrintingHref = $('#label-printing-url').attr('href');
		id = idVal;
		location.href = labelPrintingHref + '/' + id;

	} else {
		type = 'Trial';
		if (tableName === 'nursery-table') {
			type = 'Nursery';
		}
		showMessage(createLabelErrorMsg);
	}
}

function showFieldMap(tableName) {
	var count = 0;
	var idVal = null;
	if (selectedTableIds.length > 0) {
		for (var index in selectedTableIds) {
			var tempVal = selectedTableIds[index];
			
			if (tempVal != null) {
				idVal = tempVal;
				count++;
			}
		}
	} else {
		//edit nursery
		if ($('#studyId')) {
			idVal = $('#studyId').val();
		}
	}

	if (idVal != null) {
		if (count > 1) {
			showMessage(fieldMapOneStudyErrorMsg);
		} else {
			$('#page-message').html('');
			showFieldMapPopUp(tableName, idVal);
		}
	} else {
		showMessage(fieldMapStudyRequired);
	}
}

// Show popup to select instances for field map creation
function showFieldMapPopUpCreate(tableName, ids) {
	var link = '';
	if (tableName === 'trial-table') {
		link = '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/';
		trial = true;
	} else {
		link = '/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/';
		trial = false;
	}
	$.ajax({
		url: link + encodeURIComponent(ids),
		type: 'GET',
		data: '',
		success: function(data) {
			selectTrialInstanceCreate(tableName);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		},
		complete: function() {
		}
	});
}

// Show popup to select field map to display
function showFieldMapPopUp(tableName, id) {
	var link = '';
	if (tableName == 'trial-table') {
		link = '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/';
	} else {
		link = '/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/';
	}
	$.ajax({
		url: link + id,
		type: 'GET',
		data: '',
		success: function(data) {
			if (data.nav == '0') {
				if (tableName == 'nursery-table') {
					$('#fieldmapDatasetId').val(data.datasetId);
					$('#fieldmapGeolocationId').val(data.geolocationId);
				}
				selectTrialInstance(tableName);
			} else if (data.nav == '1') {
				showMessage(noFieldMapExists);
			}
		}
	});
}

function viewFieldMap() {
	if (isViewFieldmap) {
		showGeneratedFieldMap();
	} else {
		showCreateFieldMap();
	}
}

// Redirect to step 3
function showGeneratedFieldMap() {
	if ($('#studyFieldMapTree .field-map-highlight').attr('id')) {
		if ($('#studyFieldMapTree .field-map-highlight').size() == 1) {
			$('#selectTrialInstanceModal').modal('toggle');
			var id = $('#studyFieldMapTree .field-map-highlight').attr('id');
			var datasetId = $('#studyFieldMapTree .field-map-highlight').treegrid('getParentNode').attr('id');
			location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/' + datasetId + '/' + id;
		} else {
			showMessage(multipleSelectError);
		}
	} else {
		showMessage(noSelectedTrialInstance);
	}
}

function showCreateFieldMap() {

	var selectedWithFieldMap,
		id,
		dataset,
		studyId,
		hasFieldMap;

	if ($('#studyFieldMapTree .checkInstance:checked').attr('id')) {
		selectedWithFieldMap = false;
		fieldmapIds = [];
		$('#studyFieldMapTree .checkInstance:checked').each(function() {
			id = this.id;
			if (id.indexOf('|') > -1) {
				datasetId = id.split('|')[0];
				id = id.split('|')[1];
				studyId = $(this).parent().parent().treegrid('getParentNode').attr('id');
			} else {
				datasetId = $(this).parent().parent().treegrid('getParentNode').attr('id');
				studyId = $(this).parent().parent().treegrid('getParentNode').treegrid('getParentNode').attr('id');
			}
			// Get value hasfieldmap column
			if (trial) {
				hasFieldMap = $(this).parent().next().next().next().next().html();
			} else {
				hasFieldMap = $(this).parent().next().next().html();
			}

			// Build id list of selected trials instances
			fieldmapIds.push(studyId + '|' + datasetId + '|' + id);

			if (hasFieldMap == 'Yes') {
				selectedWithFieldMap = true;
			}
		});
		// This is to disable the 2nd popup
		if (selectedWithFieldMap) {
			showMessage(hasFieldmapError);
		} else {
			// Redirect to step 1
			redirectToFirstPage();
		}
	} else {
		// No trial instance is selected
		showMessage(noSelectedTrialInstance);
	}
}

function redirectToFirstPage() {
	location.href = $('#fieldmap-url').attr('href') + '/' + encodeURIComponent(fieldmapIds.join(','));
}

function setSelectedTrialsAsDraggable() {
	$('#selectedTrials').tableDnD();

	$('#selectedTrials').tableDnD({
		onDragClass: 'myDragClass',
		onDrop: function(table, row) {
			setSelectTrialOrderValues();
		}
	});

	setSelectTrialOrderValues();
	styleDynamicTree('selectedTrials');
}

function setSelectTrialOrderValues() {
	var i = 0;
	$('#selectedTrials .orderNo').each(function() {
		$(this).text(i + 1);
		$(this).parent().parent().attr('id', i + 1);
		i++;
	});
	styleDynamicTree('selectedTrials');
}

function styleDynamicTree(treeName) {
	var count = 0;

	if ($('#' + treeName) != null) {
		$('#' + treeName + ' tr').each(function() {
			count++;
			var className = '';
			if (count % 2 == 1) {
				className = 'odd';
			} else {
				className = 'even';
			}
			$(this).find('td').removeClass('odd');
			$(this).find('td').removeClass('even');
			$(this).find('td').addClass(className);

			$(this).find('th').removeClass('odd');
			$(this).find('th').removeClass('even');
			$(this).find('th').addClass('table-header');
		});
	}
}

function openStudyOldFb() {
	'use strict';
	//for opening old fb
	var openStudyHref = '/Fieldbook/TrialManager/createTrial/open';
	$.ajax({
		url: openStudyHref,
		type: 'GET',
		data: '',
		cache: false,
		success: function() {
		}
	});
}

function openStudy(tableName) {
	'use strict';
	var count = 0,
		idVal = getCurrentStudyIdInTab();
		count++;

	if (count !== 1) {
		showMessage(openStudyError);
		return;
	}

	var openStudyHref = $('#open-study-url').attr('href');

	if (idVal != null) {
		location.href = openStudyHref + '/' + idVal;
	}
}

function openTreeStudy(id){
	'use strict';
	if(isNursery()){
		location.href = '/Fieldbook/NurseryManager/editNursery/' + id;
	}else{
		location.href = '/Fieldbook/TrialManager/openTrial/' + id;
	}
}

function openDeleteConfirmation() {
	'use strict';
	
	$('#deleteStudyModal').modal({ backdrop: 'static', keyboard: true });
	var idVal = getCurrentStudyIdInTab();
	if (!idVal) {
		idVal = $('#studyId').val();
	}
	var name = $('#study' + idVal + ' .review-study-name').text();
	if (!name) {
		name = $('.nursery-name-display').text();
	}
	$('#delete-study-confirmation').html(deleteNurseryConfirmation + ' ' + name + '?');
}

function deleteNursery() {
	'use strict';
	
	if ($('.review-nursery-page-identifier').length) {
		deleteNurseryInReview();
	}
	else if ($('.edit-nursery-page-identifier').length) {
		deleteNurseryInEdit();
	}else if ($('.review-trial-page-identifier').length) {
		deleteNurseryInReview();
	}
	else if ($('.edit-trial-page-identifier').length) {
		deleteNurseryInEdit();
	}
}

function deleteNurseryInReview() {
	'use strict';

	var idVal = getCurrentStudyIdInTab();
	doDeleteNursery(idVal, function(data) {
		$('#deleteStudyModal').modal('hide');
		
		setTimeout(function() {
			//simulate close tab
			$('#' + idVal).trigger('click');
			//remove it from the tree
			if ($('#studyTree').dynatree('getTree').getNodeByKey(idVal)) {
				$('#studyTree').dynatree('getTree').getNodeByKey(idVal).remove();
			}
			showSuccessfulMessage('',isNursery() ? deleteNurserySuccessful : deleteTrialSuccessful);
		}, 500);
	});
}

function deleteNurseryInEdit() {
	'use strict';
	
	var idVal = $('#studyId').val();
	doDeleteNursery(idVal, function(data) {
		$('#deleteStudyModal').modal('hide');
		showSuccessfulMessage('',isNursery() ? deleteNurserySuccessful : deleteTrialSuccessful);
		setTimeout(function() {
			//go back to review nursery page
			location.href = $('#delete-success-return-url').attr('href');
		}, 500);
	});
}

function advanceNursery(tableName) {
	'use strict';
	
	var count = 0, 
		idVal = $('#createNurseryMainForm #studyId').val();
	
	if($('.import-study-data').data('data-import') === '1'){
		showErrorMessage('', needSaveImportDataError);
		return;
	}
	
	count++;
	if (count !== 1) {
		showMessage(advanceStudyError);
		return;
	}


	var advanceStudyHref = $('#advance-study-url').attr('href');

	if (tableName == 'nursery-table') {
		if (idVal != null) {

			$.ajax({
				url: advanceStudyHref + '/' + encodeURIComponent(idVal),
				type: 'GET',
				aysnc: false,
				success: function(html) {
					$('#advance-nursery-modal-div').html(html);
					$('#advanceNurseryModal').modal({ backdrop: 'static', keyboard: true });
					
					$('#advanceNurseryModal select').each(function(){
						$(this).select2({minimumResultsForSearch: $(this).find('option').length == 0 ? -1 : 20});
					});
					
				}				
			});
		}
	}
}
function showInvalidInputMessage(message){
	'use strict';	
	createErrorNotification(invalidInputMsgHeader,message);
}
function showErrorMessage(messageDivId, message) {
    // TODO : change showErrorMessage and calling functions to remove unnecessary div parameter
	'use strict';
	createErrorNotification(errorMsgHeader,message);
}

function showSuccessfulMessage(messageDivId, message) {
    // TODO : change showSuccessfulMessage and calling functions to remove unnecessary div parameter
	'use strict';
	createSuccessNotification(successMsgHeader,message);
}

function showAlertMessage(messageDivId, message) {
	'use strict';
	createWarningNotification(warningMsgHeader,message);
}

function hideErrorMessage() {
	$('#page-message .alert-danger').fadeOut(1000);
}

function initializeHarvestLocationSelect2(locationSuggestions, locationSuggestionsObj) {

	$.each(locationSuggestions, function(index, value) {
		locationSuggestionsObj.push({
			id: value.locid,
			text: value.lname,
			abbr: value.labbr
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('harvestLocationIdAll')).select2({
		query: function(query) {
			var data = {results: locationSuggestionsObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}
	}).on('change', function() {
		$('#' + getJquerySafeId('harvestLocationId')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').id);
		$('#' + getJquerySafeId('harvestLocationName')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').text);
		$('#' + getJquerySafeId('harvestLocationAbbreviation')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').abbr);
		if ($('#harvestloc-tooltip')) {
			$('#harvestloc-tooltip').attr('title', locationTooltipMessage + $('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').abbr);
			$('.help-tooltip-nursery-advance').tooltip('destroy');
			$('.help-tooltip-nursery-advance').tooltip();
		}
	});
}

function initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFavObj) {

	$.each(locationSuggestionsFav, function(index, value) {
		locationSuggestionsFavObj.push({
			id: value.locid,
			text: value.lname,
			abbr: value.labbr
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('harvestLocationIdFavorite')).select2({
		minimumResultsForSearch: locationSuggestionsFavObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: locationSuggestionsFavObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}
	}).on('change', function() {
		$('#' + getJquerySafeId('harvestLocationId')).val($('#' + getJquerySafeId('harvestLocationIdFavorite')).select2('data').id);
		$('#' + getJquerySafeId('harvestLocationName')).val($('#' + getJquerySafeId('harvestLocationIdFavorite')).select2('data').text);
		$('#' + getJquerySafeId('harvestLocationAbbreviation')).val($('#' + getJquerySafeId('harvestLocationIdFavorite')).select2('data').abbr);
		if ($('#harvestloc-tooltip')) {
			$('#harvestloc-tooltip').attr('title', locationTooltipMessage + $('#' + getJquerySafeId('harvestLocationIdFavorite')).select2('data').abbr);
			$('.help-tooltip-nursery-advance').tooltip('destroy');
			$('.help-tooltip-nursery-advance').tooltip();
		}
	});
}

function initializeMethodSelect2(methodSuggestions, methodSuggestionsObj) {

	$.each(methodSuggestions, function(index, value) {
		methodSuggestionsObj.push({
			id: value.mid,
			text: value.mname + ' - ' + value.mcode,
			tooltip: value.mdesc
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('methodIdAll')).select2({
		minimumResultsForSearch: methodSuggestionsObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: methodSuggestionsObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}

	}).on('change', function() {
		if ($('#' + getJquerySafeId('advanceBreedingMethodId')).length !== 0) {
			$('#' + getJquerySafeId('advanceBreedingMethodId')).val($('#' + getJquerySafeId('methodIdAll')).select2('data').id);
			if ($('#method-tooltip')) {
				$('#method-tooltip').attr('title', $('#' + getJquerySafeId('methodIdAll')).select2('data').tooltip);
				$('.help-tooltip-nursery-advance').tooltip('destroy');
				$('.help-tooltip-nursery-advance').tooltip();
			}
			$('#' + getJquerySafeId('advanceBreedingMethodId')).trigger('change');
		}
	});
}

function initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFavObj) {

	$.each(methodSuggestionsFav, function(index, value) {
		methodSuggestionsFavObj.push({
			id: value.mid,
			text: value.mname + ' - ' + value.mcode,
			tooltip: value.mdesc
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('methodIdFavorite')).select2({
		minimumResultsForSearch: methodSuggestionsFavObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: methodSuggestionsFavObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}
	}).on('change', function() {
		if ($('#' + getJquerySafeId('advanceBreedingMethodId')).length !== 0) {
			$('#' + getJquerySafeId('advanceBreedingMethodId')).val($('#' + getJquerySafeId('methodIdFavorite')).select2('data').id);
			if ($('#method-tooltip')) {
				$('#method-tooltip').attr('title', $('#' + getJquerySafeId('methodIdFavorite')).select2('data').tooltip);
				$('.help-tooltip-nursery-advance').tooltip('destroy');
				$('.help-tooltip-nursery-advance').tooltip();
			}
			$('#' + getJquerySafeId('advanceBreedingMethodId')).trigger('change');
		}
	});
}

function exportTrial(type) {

	var numberOfInstances;

	$('#page-modal-choose-instance-message-r').html('');
	$('#page-modal-choose-instance-message').html('');
	$('.instanceNumber:first').click();
	numberOfInstances = $('#numberOfInstances').val();
	$('.spinner-input').spinedit({
		minimum: 1,
		maximum: parseInt(numberOfInstances),
		value: 1
	});
	$('#exportTrialType').val(type);
	initTrialModalSelection();
	if (type == 2) {
		$('#chooseInstance').detach().appendTo('#importRChooseInstance');
		$('#importRModal').modal('show');
	} else {
		$('#chooseInstance').detach().appendTo('#exportChooseInstance');
		$('#trialModalSelection').modal('show');
	}
}

function initTrialModalSelection() {
	$('#xportInstanceType').val(1);
	$('#exportTrialInstanceNumber').val(1);
	$('#exportTrialInstanceNumber').spinedit('setValue', 1);
	$('#exportTrialInstanceStart').val(1);
	$('#exportTrialInstanceStart').spinedit('setValue', 1);
	$('#exportTrialInstanceEnd').val(1);
	$('#exportTrialInstanceEnd').spinedit('setValue', 1);
	$('#selectedRTrait').prop('selectedIndex', 0);
}

function exportStudy() {
	'use strict';
	var type = $('#exportType').val();
	if (type === '0') {
		showMessage('Please choose export type');
		return false;
	}
		
	if (type === '2') {
		exportStudyToR(type);
	} else {
		doExportContinue(type, isNursery());
	}
}

function exportStudyToR(type) {
	'use strict';
	doExportContinue(type + '/' + $('#selectedRTrait').val(), isNursery());
}
function getExportCheckedInstances(){
	'use strict';
	var checkedInstances = [];	
	$('.trial-instance-export').each(function(){
			if($(this).is(':checked')){
		  		checkedInstances.push({'instance': $(this).data('instance-number'), 'hasFieldmap' :  $(this).data('has-fieldmap')});
			}
		});
	return checkedInstances;
}
function validateTrialInstance() {
	'use strict';
	var checkedInstances = getExportCheckedInstances(),
		counter = 0,
		additionalParam = '';
	if(checkedInstances !== null && checkedInstances.length !== 0){
		
		for(counter = 0 ; counter < checkedInstances.length ; counter++){
			if(additionalParam !== ''){
				additionalParam += '|';
			}
			additionalParam += checkedInstances[counter].instance;
		}
	}
	return additionalParam;
}

function doExportContinue(paramUrl, isNursery) {
	var currentPage = $('#measurement-data-list-pagination .pagination .active a').html(),
		additionalParams = '',
		formname,
		$form,
		serializedData,
		exportWayType;

	if (isNursery) {
		formname = '#addVariableForm';
	} else {
		formname = '#addVariableForm, #addVariableForm2';
	}
	$form = $(formname);
	serializedData = $form.serialize();
	if (!isNursery) {
		additionalParams = validateTrialInstance();
		if (additionalParams == 'false') {
			return false;
		}
	}
	exportWayType = '/' + $('#exportWayType').val();

	doFinalExport(paramUrl, additionalParams, exportWayType, isNursery);
}

function doFinalExport(paramUrl, additionalParams, exportWayType, isNursery) {
	var formName = '#exportStudyForm',
		action = submitExportUrl,
		newAction = '',
		studyId = '0';

	if (isNursery) {
		newAction = action + 'export/' + paramUrl;
	} else {
		// Meaning its trial
		newAction = action + 'exportTrial/' + paramUrl + '/' + additionalParams;
	}
	newAction += exportWayType;

	if ($('#browser-nurseries').length !== 0) {
		// Meaning we are on the landing page
		studyId = getCurrentStudyIdInTab();
	}

	$('#exportStudyForm #studyExportId').val(studyId);
	$(formName).attr('action', newAction);


	$(formName).ajaxForm(exportOptions).submit();
	$('#exportStudyForm #studyExportId').val('0');
}

function importNursery(type) {

	var action = '/Fieldbook/ImportManager/import/' + $('#study-type').val() + '/' + type,
		formName = '#importStudyUploadForm';

	$(formName).attr('action', action);
}

function submitImportStudy() {
	'use strict';
	if ($('#importType').val() === '0') {
		showErrorMessage('page-import-study-message-modal', 'Please choose import type');
		return false;
	}

	if ($('#fileupload').val() === '') {
		showErrorMessage('page-import-study-message-modal', 'Please choose a file to import');
		return false;
	}
	
	if($('.import-study-data').data('data-import') === '1'){
		setTimeout(function(){$('#importOverwriteConfirmation').modal({ backdrop: 'static', keyboard: true });}, 300);
	}else{
		continueStudyImport(false);
	}
}
function continueStudyImport(doDataRevert){
	'use strict';
	if(doDataRevert){
		revertData(false);
		$('#importOverwriteConfirmation').modal('hide');
	}
	
	$('#importStudyUploadForm').ajaxForm(importOptions).submit();
}

function showImportOptions(){
	'use strict';
	$('#fileupload').val('');
	$('#importStudyModal').modal({ backdrop: 'static', keyboard: true });
}
function goBackToImport(){
	'use strict';
	//revertData(false);
	$('#importStudyConfirmationModal').modal('hide');
	$('#importStudyDesigConfirmationModal').modal('hide');
	$('#importOverwriteConfirmation').modal('hide');
	setTimeout(function(){$('#importStudyModal').modal({ backdrop: 'static', keyboard: true });}, 300);
	
}

function isFloat(value) {
	'use strict';
	return !isNaN(parseInt(value, 10)) && (parseFloat(value, 10) == parseInt(value, 10));
}

function moveToTopScreen() {
	
}

function openImportGermplasmList(type) {
	'use strict';
	$('.germplasmAndCheckSection').data('import-from', type);
	if($('#importLocationUrl').length != 0){
		importLocationUrl = $('#importLocationUrl').val();
	}
	
	setTimeout(function() {
		$('#importFrame').attr('src', importLocationUrl);
		$('#importGermplasmModal').modal({ backdrop: 'static', keyboard: true });
	}, 500);
}

function doTreeHighlight(treeName, nodeKey) {

	var count = 0,
		key = '',
		elem;

	$('#' + treeName).dynatree('getTree').activateKey(nodeKey);
	$('#' + treeName).find('*').removeClass('highlight');

	// Then we highlight the nodeKey and its parents
	elem = nodeKey.split('_');
	for (count = 0 ; count < elem.length ; count++) {
		if (key != '') {
			key = key + '_';
		}
		key = key + elem[count];
		$('.' + key).addClass('highlight');
	}
}

function addCreateNurseryRequiredAsterisk() {
	var requiredText = '<span class="required">*</span>',
		i,
		cvTermId;

	for (i = 0; i < requiredFields.length; i++) {
		cvTermId = requiredFields[i];
		if ($('.cvTermIds[value=""+cvTermId+""]').length !== 0) {
			$('.cvTermIds[value=""+cvTermId+""]').parent().parent().find('.nursery-level-label').parent().append(requiredText);
		}
	}
}

function addCreateTrialRequiredAsterisk() {
	var requiredText = '<span class="required">*</span>',
		i,
		cvTermId;

	for (i = 0; i < requiredFields.length; i++) {
		cvTermId = requiredFields[i];
		if ($('.cvTermIds[value=""+cvTermId+""]').length !== 0) {
			$('.cvTermIds[value=""+cvTermId+""]').parent().parent().find('.trial-level-label').parent().append(requiredText);
		}
	}
}

function getDateRowIndex(divName, dateCvTermId) {

	var rowIndex = -1;

	$('.' + divName + ' .cvTermIds').each(function(index) {
		if ($(this).val() == parseInt(dateCvTermId)) {
			rowIndex = index;
		}
	});
	return rowIndex;
}

function validateStartEndDate(divName) {
	//8050 - start
	var startDateIndex = getDateRowIndex(divName, startDateId),
		endDateIndex = getDateRowIndex(divName, endDateId),
		startDate = $('#' + getJquerySafeId('studyLevelVariables' + startDateIndex + '.value')).val(),
		endDate = $('#' + getJquerySafeId('studyLevelVariables' + endDateIndex + '.value')).val();

	startDate = startDate == null ? '' : startDate;
	endDate = endDate == null ? '' : endDate;

	if (startDate === '' && endDate === '') {
		return true;
	} else if (startDate !== '' && endDate === '') {
		return true;
	} else if (startDate === '' && endDate !== '') {
		showErrorMessage('page-message', startDateRequiredError);
		return false;
	} else if (parseInt(startDate) > parseInt(endDate)) {
		showErrorMessage('page-message', startDateRequiredEarlierError);
		return false;
	}
	return true;
}

function getIEVersion() {
	var myNav = navigator.userAgent.toLowerCase();
	return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
}

function validatePlantsSelected() {
	var ids = '',
		isMixed = false,
		isBulk = false,
		valid;

	if ($('.bulk-section').is(':visible')) {
		if ($('input[type=checkbox][name=allPlotsChoice]:checked').val() !== '1') {
			ids = ids + $('#plotVariateId').val();
		}
		isBulk = true;
	}
	if ($('.lines-section').is(':visible')) {
		if ($('input[type=checkbox][name=lineChoice]:checked').val() !== '1') {
			if (ids !== '') {
				ids = ids + ',';
			}
			ids = ids + $('#lineVariateId').val();
		}
		if (isBulk) {
			isMixed = true;
		}
	}

	valid = true;
	if ($('input[type=checkbox][name=methodChoice]:checked').val() === '1'
		&& $('#namingConvention').val() !== '1'
		&& $('#advanceBreedingMethodId').val() === '') {
		showErrorMessage('page-advance-modal-message', msgMethodError);
		valid = false;
	}
	if(valid){
		valid = validateBreedingMethod();
	}
	if (valid && ids !== '')	{
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/countPlots/' + ids,
			type: 'GET',
			cache: false,
			async: false,
			success: function(data) {
				var choice,
					lineSameForAll;

				if (isMixed) {
					if (data == 0) {
						var param = $('lineVariateId').select2('data').text + ' and/or ' + $('#plotVariateId').select2('data').text;
						var newMessage = msgEmptyListError.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);
						valid = false;
					}
				} else if (isBulk) {
					choice = !$('#plot-variates-section').is(':visible');
					if (choice == false && data == '0') {
						var param = $('#plotVariateId').select2('data').text;
						var newMessage = msgEmptyListError.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);
						valid = false;
					}
				} else {
					choice = !$('#line-variates-section').is(':visible');
					lineSameForAll = $('input[type=checkbox][name=lineChoice]:checked').val() == 1;
					if (lineSameForAll == false && choice == false && data == '0') {
						var param = $('#lineVariateId').select2('data').text;
						var newMessage = msgEmptyListError.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);
						valid = false;
					}
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			},
			complete: function() {
			}
		});
	}
	if (valid && isMixed) {
		return valid;
	}
	return valid;
}

function callAdvanceNursery() {
	var lines = $('#lineSelected').val();

	if (!lines.match(/^\s*(\+|-)?\d+\s*$/)) {
		showErrorMessage('page-advance-modal-message', linesNotWholeNumberError);
		return false;
	} else if (validatePlantsSelected()) {
		doAdvanceNursery();
	}
}

function doAdvanceNursery() {

	var serializedData;

	$('input[type=checkbox][name=methodChoice]').prop('disabled', false);
	serializedData = $('#advanceNurseryModalForm').serialize();

	$.ajax({
		url: '/Fieldbook/NurseryManager/advance/nursery',
		type: 'POST',
		data: serializedData,
		cache: false,
		success: function(html) {
			var errorMessage = $(html).find('#errorInAdvance').val(),
			    listSize = $(html).find('.advance-list-size').text(),
				uniqueId,
				close,
				aHtml;

			if (errorMessage) {
				showErrorMessage('page-advance-modal-message', errorMessage);
			}
			else if (listSize === '0') {
				showErrorMessage('page-advance-modal-message', listShouldNotBeEmptyError);
			} else {
				$('#advanceNurseryModal').modal('hide');
				$('#create-nursery-tab-headers li').removeClass('active');
				$('#create-nursery-tabs .info').hide();

				uniqueId = $(html).find('.uniqueId').attr('id');
				close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="'+uniqueId+'" onclick="javascript: closeAdvanceListTab(' + uniqueId +')"></i>';
				aHtml = '<a id="advanceHref' + uniqueId + '" href="javascript: showSelectedAdvanceTab(' + uniqueId + ')">Advance List' + close + '</a>';
				$('#create-nursery-tab-headers').append('<li class="active" id="advance-list' + uniqueId + '-li">' + aHtml + '</li>');
				$('#create-nursery-tabs').append('<div class="info" id="advance-list' + uniqueId + '">' + html + '</div>');
				showSelectedTab('advance-list' + uniqueId);
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		}
	});
}

function showSelectedAdvanceTab(uniqueId) {
	showSelectedTab('advance-list' + uniqueId);
}

function closeAdvanceListTab(uniqueId) {

	$('li#advance-list' + uniqueId + '-li').remove();
	$('.info#advance-list' + uniqueId).remove();

	setTimeout(function() {
		$('#create-nursery-tab-headers li').removeClass('active');
		$('#create-nursery-tabs .info').hide();
		$('#create-nursery-tab-headers li:eq(0)').addClass('active');
		$('#create-nursery-tabs .info:eq(0)').css('display', 'block');
	}, 100);
}

function displayAdvanceList(uniqueId, germplasmListId, listName) {
	$('#advanceHref' + uniqueId + ' .fbk-close-tab').before(': [' + listName + ']');
	$.ajax({
		url: '/Fieldbook/SeedStoreManager/advance/displayGermplasmDetails/' + germplasmListId,
		type: 'GET',
		cache: false,
		success: function(html) {
			$('#advance-list' + uniqueId).html(html);
		}
	});
}

function validateBreedingMethod() {
	var id = $('#methodVariateId').val(),
		valid = true;

	if ($('input[type=checkbox][name=methodChoice]:checked').val() !== '1' && id) {
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/countPlots/' + id,
			type: 'GET',
			cache: false,
			async: false,
			success: function(data) {
				if (data == 0) {
					var newMessage = noMethodValueError.replace(new RegExp(/\{0\}/g), $('#methodVariateId').select2('data').text);
					showErrorMessage('page-advance-modal-message', newMessage);
					valid = false;
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			},
			complete: function() {
			}
		});
	}
	return valid;
}

function showBaselineTraitDetailsModal(id) {
	'use strict';

	if (id !== '') {
		$.ajax({
			url: '/Fieldbook/NurseryManager/createNursery/showVariableDetails/' + id,
			type: 'GET',
			cache: false,
			success: function(data) {
				populateVariableDetails($.parseJSON(data));
				$('#variableDetailsModal').modal('toggle');
			}
		});
	}
}

function populateVariableDetails(standardVariable) {
	if (standardVariable != null) {
		$('#traitClass').html(checkIfNull(standardVariable.traitClass));
		$('#property').html(checkIfNull(standardVariable.property));
		$('#method').html(checkIfNull(standardVariable.method));
		$('#scale').html(checkIfNull(standardVariable.scale));
		$('#dataType').html(checkIfNull(standardVariable.dataType));
		$('#role').html(checkIfNull(standardVariable.role));
		$('#cropOntologyId').html(checkIfNull(standardVariable.cropOntologyId));
		$('#variableDetailsModal .modal-title').html(variableDetailsHeader + ' ' + checkIfNull(standardVariable.name));
	} else {
		$('#traitClass').html('');
		$('#property').html('');
		$('#method').html('');
		$('#scale').html('');
		$('#dataType').html('');
		$('#role').html('');
		$('#cropOntologyId').html('');
		$('#variableDetailsModal .modal-title').html();
	}
}

function checkIfNull(object) {
	if (object != null) {
		return object;
	} else {
		return "";
	}
}

function recreateMethodCombo() {
	var selectedMethodAll = $('#methodIdAll').val(),
		selectedMethodFavorite = $('#methodIdFavorite').val();
	var createGermplasm = false;
	var createGermplasmOpened = false;
	
	if ($('#importStudyDesigConfirmationModal').length !== 0){
		createGermplasm = true;
		if ($('#importStudyDesigConfirmationModal').hasClass('in')) {
			createGermplasmOpened = true;
		}
	}

	$.ajax({
		url: '/Fieldbook/NurseryManager/advance/nursery/getBreedingMethods',
		type: 'GET',
		cache: false,
		data: '',
		async: false,
		success: function(data) {
			//allNonGenerativeMethods
			//favoriteNonGenerativeMethods
			if (data.success == '1') {
				if (createGermplasmOpened) {
					refreshImportMethodCombo(data);
					refreshMethodComboInSettings(data);
				} else if (selectedMethodAll != null) {
					
					//recreate the select2 combos to get updated list of methods
					recreateMethodComboAfterClose('methodIdAll', $.parseJSON(data.allNonGenerativeMethods));
					recreateMethodComboAfterClose('methodIdFavorite', $.parseJSON(data.favoriteNonGenerativeMethods));
					showCorrectMethodCombo();
					//set previously selected value of method
					if ($('#showFavoriteMethod').prop('checked')) {
						setComboValues(methodSuggestionsFavObj, selectedMethodFavorite, 'methodIdFavorite');
					} else {
						setComboValues(methodSuggestionsObj, selectedMethodAll, 'methodIdAll');
					}

					if ($("#advanceNurseryModal").length > 0 ) {
						refreshMethodComboInSettings(data);
					}
				} else {
					if ($('.hasCreateGermplasm').length === 0 || ($('.hasCreateGermplasm').length > 0 && $('.hasCreateGermplasm').val() === '0')) {
						refreshMethodComboInSettings(data);
					}
					if (createGermplasm) {
						refreshImportMethodCombo(data);
					}
				}
			} else {
				showErrorMessage('page-message', data.errorMessage);
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		},
		complete: function() {
		}
	});
}

function refreshImportMethodCombo(data) {
	var selectedValue = null;
	if ($('#importMethodId').select2('data')) {
		selectedValue = $('#importMethodId').select2('data').id;
	}
	if ($('#importFavoriteMethod').is(':checked')) {
		
		initializePossibleValuesCombo($.parseJSON(data.favoriteMethods),
				'#importMethodId', false, selectedValue);
	} else {
		initializePossibleValuesCombo($.parseJSON(data.allMethods),
				'#importMethodId', false, selectedValue);
	}
	replacePossibleJsonValues(data.favoriteMethods, data.allMethods, 'Method');
}

function refreshImportLocationCombo(data) {
	var selectedValue = null;
	if ($('#importLocationId').select2('data')) {
		selectedValue = $('#importLocationId').select2('data').id;
	}
	if ($('#importFavoriteLocation').is(':checked')) {
		initializePossibleValuesCombo($.parseJSON(data.favoriteLocations),
				'#importLocationId', true, selectedValue);
	} else {
		initializePossibleValuesCombo($.parseJSON(data.allBreedingLocations),
				'#importLocationId', true, selectedValue);
	}
	replacePossibleJsonValues(data.favoriteLocations, data.allBreedingLocations, 'Location');
}

function recreateLocationCombo() {
	var selectedLocationAll = $('#harvestLocationIdAll').val();
	var selectedLocationFavorite = $('#harvestLocationIdFavorite').val();
	
	var inventoryPopup = false;
	var advancePopup = false;
	var fieldmapScreen = false;
	var createGermplasm = false;
    var hasCreateGermplasm = false;
	var createGermplasmOpened = false;
	
	if ($('#addLotsModal').length !== 0 && $('#addLotsModal').hasClass('in')){
		inventoryPopup = true;
	}
	else if ($('#advanceNurseryModal').length !== 0 && $('#advanceNurseryModal').hasClass('in')) {
		advancePopup = true;
	} else if ($('#enterFieldDetailsForm').length !== 0) {
		fieldmapScreen = true;
	}
	
	if ($('#importStudyDesigConfirmationModal').length !== 0){
		createGermplasm = true;
		if ($('#importStudyDesigConfirmationModal').hasClass('in')) {
			createGermplasmOpened = true;
		}
	}

    if ($('.hasCreateGermplasm').length === 0 || ($('.hasCreateGermplasm').length > 0 && $('.hasCreateGermplasm').val() === '0')) {
        hasCreateGermplasm = true;
    }

    if (inventoryPopup || advancePopup || fieldmapScreen || createGermplasm || hasCreateGermplasm || createGermplasmOpened) {
        $.ajax({
            url: '/Fieldbook/NurseryManager/advance/nursery/getLocations',
            type: 'GET',
            cache: false,
            data: '',
            async: false,
            success: function (data) {
                if (data.success == '1') {
                    if (createGermplasmOpened) {
                        refreshImportLocationCombo(data);
                        refreshLocationComboInSettings(data);
                    } else if (inventoryPopup) {
                        recreateLocationComboAfterClose('inventoryLocationIdAll', $.parseJSON(data.allSeedStorageLocations));
                        recreateLocationComboAfterClose('inventoryLocationIdFavorite', $.parseJSON(data.favoriteLocations));
                        showCorrectLocationInventoryCombo();
                        // set previously selected value of location
                        if ($('#showFavoriteLocationInventory').prop('checked')) {
                            setComboValues(locationSuggestionsFav_obj, $('#inventoryLocationIdFavorite').val(), 'inventoryLocationIdFavorite');
                        } else {
                            setComboValues(locationSuggestions_obj, $('#inventoryLocationIdAll').val(), 'inventoryLocationIdAll');
                        }
                        refreshLocationComboInSettings(data);
                    } else if (advancePopup === true
                        || selectedLocationAll != null) {
                        // recreate the select2 combos to get updated list
                        // of locations
                        recreateLocationComboAfterClose('harvestLocationIdAll', $.parseJSON(data.allBreedingLocations));
                        recreateLocationComboAfterClose('harvestLocationIdFavorite', $.parseJSON(data.favoriteLocations));
                        showCorrectLocationCombo();
                        // set previously selected value of location
                        if ($('#showFavoriteLocation').prop('checked')) {
                            setComboValues(locationSuggestionsFav_obj, selectedLocationFavorite, 'harvestLocationIdFavorite');
                        } else {
                            setComboValues(locationSuggestions_obj, selectedLocationAll, 'harvestLocationIdAll');
                        }
                        refreshLocationComboInSettings(data);
                        
                    } else if (fieldmapScreen === true) {
                        //recreate the select2 combos to get updated list of locations
                        recreateLocationComboAfterClose('fieldLocationIdAll', $.parseJSON(data.allBreedingLocations));
                        recreateLocationComboAfterClose('fieldLocationIdFavorite', $.parseJSON(data.favoriteLocations));
                        showCorrectLocationCombo();
                        //set previously selected value of location
                        if ($('#showFavoriteLocation').prop('checked')) {
                            setComboValues(locationSuggestionsFav_obj, $('#fieldLocationIdFavorite').val(), 'fieldLocationIdFavorite');
                        } else {
                            setComboValues(locationSuggestions_obj, $('#fieldLocationIdAll').val(), 'fieldLocationIdAll');
                        }
                    } else {
                        if (hasCreateGermplasm) {
                            refreshLocationComboInSettings(data);
                        }
                        if (createGermplasm) {
                            refreshImportLocationCombo(data);
                        }
                    }
                } else {
                    showErrorMessage('page-message', data.errorMessage);
                }

            }
        });
    }
}


function refreshMethodComboInSettings(data) {
	//get index of breeding method row
	var index = getBreedingMethodRowIndex(), selectedVal = null;
	if (index > -1) {
		var pleaseChoose = '{"mid":0,"mname":"Please Choose","mdesc":"Please Choose"}';
		if($.parseJSON( data.favoriteNonGenerativeMethods).length == 0){
			data.favoriteNonGenerativeMethods = '['+pleaseChoose+']';
		}else{
			data.favoriteNonGenerativeMethods = '['+pleaseChoose+',' + data.favoriteNonGenerativeMethods.substring(1);
		}
		
		if($.parseJSON( data.allNonGenerativeMethods).length == 0){
			data.allNonGenerativeMethods = '['+pleaseChoose+']';
		}else{
			data.allNonGenerativeMethods = '['+pleaseChoose+', ' + data.allNonGenerativeMethods.substring(1);
		}
		
		
		if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data')) {
			selectedVal = $('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data').id;
		}
		
		//recreate select2 of breeding method
		initializePossibleValuesCombo([],
				'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
		
		//update values of combo
		if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.favorite1')).is(':checked')) {
			initializePossibleValuesCombo($.parseJSON(data.favoriteNonGenerativeMethods),
					'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
		} else {
			initializePossibleValuesCombo($.parseJSON(data.allNonGenerativeMethods),
					'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
		}
	
		replacePossibleJsonValues(data.favoriteNonGenerativeMethods, data.allNonGenerativeMethods, index);
	}
}

function refreshLocationComboInSettings(data) {
	var selectedVal = null;
	var index = getLocationRowIndex();
	if (index > -1) {
		if ($('#'+ getJquerySafeId('studyLevelVariables'+ index + '.value')).select2('data')) {
			selectedVal = $('#'+ getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data').id;
		}
		initializePossibleValuesCombo([], '#' + getJquerySafeId('studyLevelVariables' + index + '.value'), true, selectedVal);
	
		// update values in combo
		if ($("#"+ getJquerySafeId('studyLevelVariables' + index + '.favorite1')).is(':checked')) {
			initializePossibleValuesCombo($.parseJSON(data.favoriteLocations), "#" + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
		} else {
			initializePossibleValuesCombo($.parseJSON(data.allBreedingLocations), '#' + getJquerySafeId('studyLevelVariables' + index + '.value'), true, selectedVal);
		}
	
		replacePossibleJsonValues(data.favoriteLocations, data.allBreedingLocations, index);
	}
}

function recreateLocationComboAfterClose(comboName, data) {
	if (comboName == 'harvestLocationIdAll') {
		//clear all locations dropdown
		locationSuggestions = [];
		locationSuggestionsObj = [];
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestionsObj);
		//reload the data retrieved
		locationSuggestions = data;
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestionsObj);
	} else if (comboName == 'inventoryLocationIdAll') {
		//clear all locations dropdown
		initializePossibleValuesComboInventory(data, '#inventoryLocationIdAll', true, null);
	} else if (comboName == 'inventoryLocationIdFavorite') {
		initializePossibleValuesComboInventory(data, '#inventoryLocationIdFavorite', false, null);
	} else {
		//clear the favorite locations dropdown
		locationSuggestionsFav = [];
		locationSuggestionsFavObj = [];
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFavObj);
		//reload the data
		locationSuggestionsFav = data;
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFavObj);
	}

}

function recreateMethodComboAfterClose(comboName, data) {
	if (comboName == 'methodIdAll') {
		//clear the all methods dropdown
		methodSuggestions = [];
		methodSuggestionsObj = [];
		initializeMethodSelect2(methodSuggestions, methodSuggestionsObj);
		//reload the data
		methodSuggestions = data;
		initializeMethodSelect2(methodSuggestions, methodSuggestionsObj);
	} else {
		//clear the favorite methods dropdown
		methodSuggestionsFav = [];
		methodSuggestionsFavObj = [];
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFavObj);
		//reload the data
		methodSuggestionsFav = data;
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFavObj);
	}
}

function changeBuildOption() {
	'use strict';

	if ($('#studyBuildOption').val() === '1') {
		$('#choosePreviousStudy').hide();
		clearSettings();
	} else if ($('#studyBuildOption').val() === '2') {
		$('#choosePreviousStudy').show();
	}
}

function createFolder() {
	'use strict';

	var folderName = $.trim($('#addFolderName').val()),
		parentFolderId;

	if (folderName === '') {
		showErrorMessage('page-add-study-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else if ( ! isValidInput(folderName)) {
        showErrorMessage('page-add-study-folder-message-modal', invalidFolderNameCharacterMessage);
        return false;
    }else {
    	var activeStudyNode = $('#studyTree').dynatree('getTree').getActiveNode();
    	
    	if(activeStudyNode == null || activeStudyNode.data.isFolder === false || activeStudyNode.data.key > 1 || activeStudyNode.data.key === 'CENTRAL'){
    		showErrorMessage('', studyProgramFolderRequired);
    		return false;
    	}
    	
		parentFolderId = activeStudyNode.data.key;
		if (parentFolderId === 'LOCAL') {
			parentFolderId = 1;
		}

		$.ajax({
			url: '/Fieldbook/StudyTreeManager/addStudyFolder',
			type: 'POST',
			data: 'parentFolderId=' + parentFolderId + '&folderName=' + folderName,
			cache: false,
			success: function(data) {
				var node;

				if (data.isSuccess == 1) {
					node = $('#studyTree').dynatree('getTree').getActiveNode();
					doStudyLazyLoad(node);
					node.focus();
					node.expand();
					$('#addFolderDiv').slideUp();
					showSuccessfulMessage('',addFolderSuccessful);
				} else {
					showErrorMessage('page-add-study-folder-message-modal', data.message);
				}
			}
		});
	}
	return false;
}

function deleteFolder(object) {
	'use strict';

	var currentFolderName,
		isFolder = $('#studyTree').dynatree('getTree').getActiveNode().data.isFolder,
		deleteConfirmationText;

	if (!$(object).hasClass('disable-image')) {
		if (isFolder) {
			$('#delete-heading-modal').text(deleteFolderTitle);
			deleteConfirmationText = deleteConfirmation;
		}
		else {
			$('#delete-heading-modal').text(deleteNurseryTitle);
			deleteConfirmationText = deleteNurseryConfirmation;
		}
		$('#deleteStudyFolder').modal('show');
        hideAddFolderDiv();
        hideRenameFolderDiv();
		currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title;
		$('#delete-confirmation').html(deleteConfirmationText + ' ' + currentFolderName + '?');
		$('#page-delete-study-folder-message-modal').html('');
	}
}

function submitDeleteFolder() {
	'use strict';
	
	var folderId = $('#studyTree').dynatree('getTree').getActiveNode().data.key;
	var isFolder = $('#studyTree').dynatree('getTree').getActiveNode().data.isFolder;
	
	if (isFolder) {
		$.ajax({
			url: '/Fieldbook/StudyTreeManager/deleteStudyFolder',
			type: 'POST',
			data: 'folderId=' + folderId,
			cache: false,
			success: function(data) {
				var node;
				if (data.isSuccess === '1') {
					$('#deleteStudyFolder').modal('hide');
					node = $('#studyTree').dynatree('getTree').getActiveNode();
					if (node != null) {
						node.remove();
					}
					changeBrowseNurseryButtonBehavior(false);
					showSuccessfulMessage('',deleteFolderSuccessful);
				} else {
					showErrorMessage('page-delete-study-folder-message-modal', data.message);
				}
			}
		});
	}
	else {
		doDeleteNursery(folderId, function(data) {
			var node;
			if (data.isSuccess === '1') {
				$('#deleteStudyFolder').modal('hide');
				node = $('#studyTree').dynatree('getTree').getActiveNode();
				if (node != null) {
					node.remove();
				}
				changeBrowseNurseryButtonBehavior(false);
				showSuccessfulMessage('',isNursery() ? deleteNurserySuccessful : deleteTrialSuccessful);
			}
		});
	}
}

function moveStudy(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key,
		targetId = targetNode.data.key,
		isStudy = sourceNode.data.isFolder === true ? 0 : 1,
		title;

	if (targetId === 'CENTRAL' || targetId > 0) {
		title = $('#studyTree').dynatree('getTree').getNodeByKey('CENTRAL').data.title;
		showErrorMessage('page-study-tree-message-modal', 'Can not move to ' + title);
		return false;
	}

	if (targetId === 'LOCAL') {
		targetId = 1;
	}

	$.ajax({
		url: '/Fieldbook/StudyTreeManager/moveStudyFolder',
		type: 'POST',
		data: 'sourceId=' + sourceId + '&targetId=' + targetId + '&isStudy=' + isStudy,
		cache: false,
		success: function(data) {
			var node = targetNode;
			sourceNode.remove();
			doStudyLazyLoad(node);
			node.focus();
		}
	});
}

function deleteGermplasmFolder(object) {
	'use strict';

	var currentFolderName;

	if (!$(object).hasClass('disable-image')) {
		$('#deleteGermplasmFolder').modal('show');
        $('#addGermplasmFolderDiv').slideUp('fast');
        $('#renameGermplasmFolderDiv').slideUp('fast');
		currentFolderName = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title
		$('#delete-folder-confirmation').html(deleteConfirmation + ' ' + currentFolderName + '?');

		$('#page-delete-germplasm-folder-message-modal').html('');
	}
}

function submitDeleteGermplasmFolder() {
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
				$('#deleteGermplasmFolder').modal('hide');
				node = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode();
				node.remove();
                showSuccessfulMessage('',deleteFolderSuccessful);
			} else {
				showErrorMessage('page-delete-germplasm-folder-message-modal', data.message);
			}
		}
	});
}

function moveGermplasm(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key,
		targetId = targetNode.data.key,
		title;

	if (targetId === 'CENTRAL' || targetId > 0) {
		title = $('#' + getDisplayedTreeName()).dynatree('getTree').getNodeByKey('CENTRAL').data.title;
		showErrorMessage('page-import-message-modal', 'Can not move to ' + title);
		return false;
	}

	if (targetId === 'LOCAL') {
		targetId = 1;
	}

	$.ajax({
		url: '/Fieldbook/ListTreeManager/moveGermplasmFolder',
		type: 'POST',
		data: 'sourceId=' + sourceId + '&targetId=' + targetId,
		cache: false,
		success: function(data) {
			var node = targetNode;
			sourceNode.remove();
			doGermplasmLazyLoad(node);
			node.focus();
		}
	});
}

function closeModal(modalId) {
	'use strict';
	$('#' + modalId).modal('hide');
}

function openGermplasmDetailsPopopWithGidAndDesig(gid, desig) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/ListTreeManager/germplasm/detail/url',
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {
			var germplasmDetailsUrl = html;
			$('#openGermplasmFrame').attr('src', germplasmDetailsUrl + gid);
			$('#openGermplasmModal .modal-title').html(headerMsg1 + ' ' + desig + ' (' + headerMsg2 + ' ' + gid + ')');
			$('#openGermplasmModal').modal({ backdrop: 'static', keyboard: true });
		}
	});
}

function editExperiment(tableIdentifier, expId, rowIndex) {
	'use strict';

	// We show the ajax page here
	$.ajax({
		url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/' + rowIndex,
		type: 'GET',
		cache: false,
		success: function(dataResp) {
			$('.edit-experiment-section').html(dataResp);
			$('.updateExperimentModal').modal({ backdrop: 'static', keyboard: true });
		}
	});
}

function showListTreeToolTip(node, nodeSpan) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/ListTreeManager/germplasm/list/header/details/' + node.data.key,
		type: 'GET',
		cache: false,
		success: function(data) {
			var listDetails = $('.list-details').clone(),
				notes;

			$(listDetails).find('#list-name').html(data.name);
			$(listDetails).find('#list-description').html(data.description);
			$(listDetails).find('#list-status').html(data.status);
			$(listDetails).find('#list-date').html(data.date);
			$(listDetails).find('#list-owner').html(data.owner);
			$(listDetails).find('#list-type').html(data.type);
			$(listDetails).find('#list-total-entries').html(data.totalEntries);
			notes = data.notes == null ? '-' : data.notes;
			$(listDetails).find('#list-notes').html(notes);

			$(nodeSpan).find('a.dynatree-title').popover({
				html: true,
				title: 'List Details',
				content: $(listDetails).html(),
				trigger: 'manual',
				placement: 'right',
				container: '.modal-popover'
			}).hover(function(){
				$('.popover').hide();
				$(this).popover('show');
			}, function(){
				$(this).popover('hide');
			});
			$('.popover').hide();
			$(nodeSpan).find('a.dynatree-title').popover('show');
		}
	});
}
function truncateStudyVariableNames(domSelector, charLimit){
	'use strict';
	$(domSelector).each(function(){
		var htmlString = $(this).html();		
		if($(this).data('truncate-limit') !== undefined) {
			charLimit = parseInt($(this).data('truncate-limit'), 10);
		}
		
		if(htmlString.length > charLimit){
			if(!$(this).hasClass('variable-tooltip')){
				$(this).addClass('variable-tooltip');
				$(this).attr('title',htmlString);
				
				if($(this).data('truncate-placement') !== undefined) {
					$(this).data('placement',$(this).data('truncate-placement'));
				}
				

				htmlString = htmlString.substring(0,charLimit) + '...';

			}
			$(this).html(htmlString);
		}

	});
	$('.variable-tooltip').each(function(){
		$(this).data('toggle', 'tooltip');
		if($(this).data('placement') === undefined) {
			$(this).data('placement', 'right');
		}
		$(this).data('container', 'body');
		$(this).tooltip();
	});
}
function checkTraitsAndSelectionVariateTable(containerDiv, isLandingPage){
	'use strict';
	if($(containerDiv + ' .selection-variate-table tbody tr').length > 0){
		$(containerDiv + ' .selection-variate-table').removeClass('fbk-hide');
	} else {
		$(containerDiv + ' .selection-variate-table').addClass('fbk-hide');
		if(isLandingPage) {
			$(containerDiv + ' .selection-variate-table').parent().prev().addClass('fbk-hide');
		}
	}
	if($(containerDiv + ' .traits-table tbody tr').length > 0){
		$(containerDiv + ' .traits-table').removeClass('fbk-hide');
	} else {
		$(containerDiv + ' .traits-table ').addClass('fbk-hide');
		if(isLandingPage) {
			$(containerDiv + ' .traits-table').parent().prev().addClass('fbk-hide');
		}
	}
}

function isValidInput(input) {
	'use strict';
    var invalidInput = /[<>&=%;?]/.test(input);
    return !invalidInput;
}


function doDeleteNursery(id, callback) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/NurseryManager/deleteNursery/' + id,
		type: 'POST',
		cache: false,
		success: function(data) {
			callback(data);
		}
	});
}
function changeBrowseNurseryButtonBehavior(isEnable) {
	'use strict';
	if (isEnable){
		$('.browse-nursery-action').removeClass('disable-image');
	}
	else{
		$('.browse-nursery-action').addClass('disable-image');
	}
}
function changeBrowseGermplasmButtonBehavior(isEnable) {
	'use strict';
	if (isEnable){
		$('.browse-germplasm-action').removeClass('disable-image');
	}
	else{
		$('.browse-germplasm-action').addClass('disable-image');
	}
}
function showManageCheckTypePopup() {
	'use strict';
	$('#page-check-message-modal').html('');
	$('.check-germplasm-list-items .popover').remove();
	resetButtonsAndFields();
	$('#manageCheckTypesModal').modal({
		backdrop : 'static',
		keyboard : false
	});
}
function addUpdateCheckType(operation) {
	'use strict';
	if (validateCheckFields()) {
		var $form = $("#manageCheckValue,#comboCheckCode");
		var serializedData = $form.serialize();
		$
				.ajax({
					url : "/Fieldbook/NurseryManager/importGermplasmList/addUpdateCheckType/"
							+ operation,
					type : "POST",
					data : serializedData,
					cache : false,
					success : function(data) {
						if (data.success == "1") {
							// reload dropdown
							reloadCheckTypeList(data.checkTypes, operation);
							showCheckTypeMessage(data.successMessage);
						} else {
							showCheckTypeErrorMessage(data.error);
						}
					}
				});
	}
}

function validateCheckFields() {
	'use strict';
	if (checkTypesObj.length === 0 && checkTypes != null) {
		$.each(checkTypes, function(index, item) {
			checkTypesObj.push({
				'id' : item.id,
				'text' : item.name,
				'description' : item.description
			});
		});
	}

	if (!$('#comboCheckCode').select2('data')) {
		showInvalidInputMessage(codeRequiredError);
		return false;
	} else if ($("#manageCheckValue").val() === "") {
		showInvalidInputMessage(valueRequiredError);
		return false;
	} else if (!isValueUnique()) {
		showInvalidInputMessage(valueNotUniqueError);
		return false;
	}

	return true;
}

function isValueUnique() {
	'use strict';
	var isUnique = true;
	$.each(checkTypesObj, function(index, item) {
		if (item.description == $('#manageCheckValue').val()
				&& item.id != $('#comboCheckCode').select2("data").id) {
			isUnique = false;
			return false;
		}
	});
	return isUnique;
}
function resetButtonsAndFields() {
	'use strict';
	$('#manageCheckValue').val('');
	$('#comboCheckCode').select2('val', '');
	$('#updateCheckTypes').hide();
	$('#deleteCheckTypes').hide();
	$('#addCheckTypes').show();
}


function showCheckTypeErrorMessage(message) {
	'use strict';
	showErrorMessage('', message);
}

function showCheckTypeMessage(message) {
	'use strict';
	showSuccessfulMessage('', message);
}

function deleteCheckType() {
	'use strict';
	var isFound = false;
	if ($('manageCheckCode').select2('data')) {
		// we need to check here if it neing used in current
		if($('.check-germplasm-list-items tbody tr').length != 0 && selectedCheckListDataTable !== null && selectedCheckListDataTable.getDataTable() !== null){
			var currentId = $('#comboCheckCode').select2('data').id;
			selectedCheckListDataTable.getDataTable().$('.check-hidden').each(function(){
				if($(this).val() == currentId){
					isFound = true;
				}
			});
		} 
        if(isFound){
        	showCheckTypeErrorMessage(checkTypeCurrentlyUseError);
        	return false;
        }

		
		var $form = $("#manageCheckValue,#comboCheckCode");
		var serializedData = $form.serialize();
		$
				.ajax({
					url : "/Fieldbook/NurseryManager/importGermplasmList/deleteCheckType",
					type : "POST",
					data : serializedData,
					cache : false,
					success : function(data) {
						if (data.success == "1") {
							reloadCheckTypeList(data.checkTypes, 3);
							showCheckTypeMessage(data.successMessage);
							resetButtonsAndFields();
						} else {
							showCheckTypeErrorMessage(data.error);
						}
					}
				});
	} else {
		showCheckTypeErrorMessage(noCheckSelected);
	}
}

function reloadCheckTypeList(data, operation) {
	'use strict';
	var selectedValue = 0;

	checkTypesObj = [];

	if (data != null) {
		$.each($.parseJSON(data), function(index, value) {
			checkTypesObj.push({
				'id' : value.id,
				'text' : value.name,
				'description' : value.description
			});
		});
	}

	if (operation == 2) {
		// update
		selectedValue = getIdOfValue($("#manageCheckValue").val());
	}

	$("#manageCheckValue").val("");
	initializeCheckTypeSelect2(null, [], false, 0, "comboCheckCode");
	initializeCheckTypeSelect2(null, checkTypesObj, false, selectedValue,
			"comboCheckCode");
}

function getIdOfValue(value) {
	'use strict';
	var id = 0;
	$.each(checkTypesObj, function(index, item) {
		if (item.description == value) {
			id = item.id;
			return false;
		}
	});
	return id;
}

function reloadCheckTypeDropDown(addOnChange, select2ClassName) {
	'use strict';
	var currentCheckId = $('#checkId').val();
	$.ajax({
		url : '/Fieldbook/NurseryManager/importGermplasmList/getAllCheckTypes',
		type : 'GET',
		cache : false,
		data : '',
		success : function(data) {
			initializeCheckTypeSelect2($.parseJSON(data.allCheckTypes), [],
					addOnChange, currentCheckId, select2ClassName);
		}
	});
}

function initializeCheckTypeSelect2(suggestions, suggestions_obj, addOnChange,
		currentFieldId, comboName) {
	var defaultData = null;

	if (suggestions_obj.length === 0) {
		$
				.ajax({
					url : "/Fieldbook/NurseryManager/importGermplasmList/getAllCheckTypes",
					type : "GET",
					cache : false,
					data : "",
					async : false,
					success : function(data) {
						checkTypes = $.parseJSON(data.allCheckTypes);
						suggestions = checkTypes;
						// alert('here');
					}
				});
	}

	if (suggestions != null) {
		$.each(suggestions, function(index, value) {
			if (comboName === 'comboCheckCode') {
				dataObj = {
					'id' : value.id,
					'text' : value.name,
					'description' : value.description,
					'originalText' : value.name
				};
			} else {
				dataObj = {
					'id' : value.id,
					'text' : value.description,
					'description' : value.description,
					'originalText' : value.name
				};
			}
			suggestions_obj.push(dataObj);
		});
	} else {
		$.each(suggestions_obj, function(index, value) {
			if (currentFieldId != '' && currentFieldId == value.id) {
				defaultData = value;
			}
		});
	}
	// if combo to create is one of the ontology combos, add an onchange event
	// to populate the description based on the selected value
	if (comboName === 'comboCheckCode') {
		$('#' + comboName)
				.select2(
						{
							query : function(query) {
								var data = {
									results : sortByKey(suggestions_obj, 'text')
								};
								// return the array that matches
								data.results = $.grep(data.results, function(
										item, index) {
									return ($.fn.select2.defaults.matcher(
											query.term, item.text));
								});
								if (data.results.length === 0) {
									data.results.unshift({
										id : query.term,
										text : query.term
									});
								}
								query.callback(data);
							},
							dropdownCssClass : 's2-nosearch-icon'
						})
				.on(
						'change',
						function() {
							if ($('#comboCheckCode').select2('data')) {
								if ($('#comboCheckCode').select2('data').id == $('#comboCheckCode').select2('data').text) {
									$('#manageCheckValue').val('');
									$('#updateCheckTypes').hide();
									$('#deleteCheckTypes').hide();
									$('#addCheckTypes').show();
								} else {
									$('#manageCheckValue').val($('#comboCheckCode').select2('data').description);
									$('#updateCheckTypes').show();
									$('#deleteCheckTypes').show();
									$('#addCheckTypes').hide();
								}
							}
						});
	} else {
		
		if($('.check-table-popover tbody tr').length != 0){
				reloadCheckListTable();
				/*
				var checkDataTable = isNursery() ? selectedCheckListDataTable.getDataTable() : germplasmDataTable.getDataTable();
				
				checkDataTable.$('.check-hidden').each(function(){
				
			
				var currentCode = $(this).data('code');
				
				for (i = 0; i < suggestions_obj.length; i++) {
					var val = suggestions_obj[i].text;
					var id = suggestions_obj[i].id;
					selected = '';
					if (currentCode == suggestions_obj[i].originalText){
						selected = 'selected';
						var $href = $(this).siblings().parent().find('.check-href');
						$href.html(val);
						$href.data('code', suggestions_obj[i].originalText);
						$(this).data('code', suggestions_obj[i].originalText);
						$(this).val(id);
						break;
					}
				}
				
			});	
			*/					    	
			//we need to get the real index of the check						
		}
	}
}
function reloadCheckListTable(){
	'use strict';
	if(isNursery()){
		$.ajax({
			url: '/Fieldbook/ListManager/GermplasmList/reload/check/list/N',
			type: 'GET',
			data: '',
			async: false,
			success: function(data) {
				$('#check-germplasm-list').html(data);
			}
		});
	}else{
		 $.ajax({
             url: '/Fieldbook/ListManager/GermplasmList/refreshListDetails',
             type: 'GET',
             cache: false,
             data: ''
         }).success(function (html) {
             $('#imported-germplasm-list').html(html);
             $('#entries-details').css('display', 'block');
             $('#numberOfEntries').html($('#totalGermplasms').val());
         });
	}
}
function openStudyTree(type) {
	'use strict';
	$('#page-study-tree-message-modal').html('');
	  $('#addFolderDiv').hide();
	  $('#renameFolderDiv').hide();
	if( $('#create-nursery #studyTree').length !== 0){
			$('#studyTree').dynatree('destroy');
			displayStudyListTree('studyTree', 'N', type);
			changeBrowseNurseryButtonBehavior(false);
	}else if( $('#create-trial #studyTree').length !== 0){
		$('#studyTree').dynatree('destroy');
		displayStudyListTree('studyTree', 'T', type);
		changeBrowseNurseryButtonBehavior(false);
	}

	$('#studyTreeModal').modal({
		backdrop : 'static',
		keyboard : true
	});
	choosingType = type;
	if(isNursery()){
		$('.fbk-study-tree-title.nursery').removeClass('fbk-hide');
	}else{
		$('.fbk-study-tree-title.trial').removeClass('fbk-hide');
	}
}

function isNursery(){
	'use strict';
	if($('#check-germplasm-list').length != 0 || $('.nursery-header').length != 0){
		return true;
	}else{
		return false;
	}		
}


function addStudyTreeHighlight(node) {
	$(node.span).addClass('fbtree-focused');
}

function initializeStudyTabs() {
	$('#study-tab-headers li').on('click', function() {
		$('#study-tab-headers li').removeClass('active');
		$(this).addClass('active');
		$('#study-tabs .info').hide();
		$('.info#' + $(this).attr('id')).show();
	});
	$('#study-tab-headers .fbk-close-tab').on('click', function() {
		var studyId = $(this).attr('id');
		var showFirst = false;
		if ($(this).parent().parent().hasClass('active')) {
			// console.log('get the first item');
			showFirst = true;
		}
		$('li#study' + studyId).remove();
		$('.info#study' + studyId).remove();
		if (showFirst && $('#study-tab-headers li').length > 0) {
			var studyIdString = $('#study-tab-headers li:eq(0)').attr('id');
			$('li#' + studyIdString).addClass('active');
			$('.info#' + studyIdString).show();
		}
		determineIfShowCloseAllStudyTabs();
	});
	determineIfShowCloseAllStudyTabs();
}
function addDetailsTab(studyId, title) {
	// if the study is already existing, we show that tab
	'use strict';
	$('#study-tab-headers li').removeClass('active');
	$('#study-tabs .info').hide();
	if ($('li#study' + studyId).length !== 0) {
		$('li#study' + studyId).addClass('active');
		$('.info#study' + studyId).show();
	} else {
		var studyType = isNursery() ? 'N' : 'T';
		$.ajax({
			url : '/Fieldbook/StudyManager/reviewStudyDetails/show/' + studyType + '/' + studyId,
			type : 'GET',
			cache : false,
			success : function(data) {
				var close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="'+studyId+'"></i>';
				$('#study-tab-headers').append(
						"<li id='study" + studyId + "' class='active'><a><span class='review-study-name'>"
								+ title + "</span>"+ close + "</a></li>");
				$('#study-tabs').append(
						'<div class="info" id="study' + studyId + '">' + data
								+ '</div>');
				$('.info#study' + studyId).show();
				initializeStudyTabs();
				$('.info#study' + studyId + ' select').each(function() {
					$(this).select2({minimumResultsForSearch: 20});
				});
				truncateStudyVariableNames('#study'+studyId+' .review-study-name', 20);
			}
		});
	}
	determineIfShowCloseAllStudyTabs();
	// if not we get the info
}

function determineIfShowCloseAllStudyTabs() {
	'use strict';
	if ($('#study-tab-headers li').length > 0) {
		$('.review-nursery-details').removeClass('fbk-hide');
	} else {
		$('.review-nursery-details').addClass('fbk-hide');
	}
}

function closeAllStudyTabs() {
	'use strict';
	$('#study-tab-headers').html('');
	$('#study-tabs').html('');
	determineIfShowCloseAllStudyTabs();
}

function loadDatasetDropdown(optionTag) {
	'use strict';
	if ($('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option').length > 1)
		return;
	$.ajax({
		url : "/Fieldbook/StudyManager/reviewStudyDetails/datasets/"
				+ getCurrentStudyIdInTab(),
		type : "GET",
		cache : false,
		success : function(data) {
			var i = 0;
			for (i = 0; i < data.length; i++) {
				optionTag.append(new Option(data[i].name, data[i].id));
			}
			$('#study' + getCurrentStudyIdInTab() + " #dataset-selection").val('');
		},
		error : function(jqXHR, textStatus, errorThrown) {
			console.log("The following error occured: " + textStatus,
					errorThrown);
		},
		complete : function() {
		}
	});
}

function getCurrentStudyIdInTab() {
	'use strict';
	return $('#study-tab-headers li.active .fbk-close-tab').attr('id');
}

function loadDatasetMeasurementRowsViewOnly(datasetId, datasetName) {
	'use strict';
	var currentStudyId = getCurrentStudyIdInTab();
	if (datasetId == 'Please Choose'
			|| $("#" + getJquerySafeId('dset-tab-') + datasetId).length !== 0)
		return;
	$.ajax({
		url : '/Fieldbook/NurseryManager/addOrRemoveTraits/viewNurseryAjax/' + datasetId,
		type : 'GET',
		cache : false,
		success : function(html) {
			var close = '<i class="glyphicon glyphicon-remove fbk-close-dataset-tab" id="'+datasetId+'"></i>';
			$('#study' + currentStudyId + ' #measurement-tab-headers').append(
					'<li class="active" id="dataset-li' + datasetId + '"><a><span class="review-dataset-name">'
							+ datasetName + '</span>'+close+'</a> ' + '</li>');
			$('#study' + currentStudyId + " #measurement-tabs").append(
					'<div class="review-info" id="dset-tab-' + datasetId + '">' + html + '</div>');
			$('#study' + currentStudyId + ' .measurement-section').show();
			truncateStudyVariableNames('#dataset-li'+datasetId+' .review-dataset-name', 40);
			initializeReviewDatasetTabs(datasetId);
		}
	});
}
function showSelectedTab(selectedTabName) {
	'use strict';
	if($('.import-study-data').data('data-import') === '1'){
		showAlertMessage('', importSaveDataWarningMessage);
		return;
	}

	$("#create-nursery-tab-headers").show();
	var tabs = $("#create-nursery-tabs").children();
	for (var i = 0; i < tabs.length; i++) {
		if (tabs[i].id == selectedTabName) {
			$("#" + tabs[i].id + "-li").addClass("active");
			$("#" + tabs[i].id).show();
		} else {
			$("#" + tabs[i].id + "-li").removeClass("active");
			$("#" + tabs[i].id).hide();
		}
	}

	if(selectedTabName === 'nursery-measurements' || selectedTabName === 'trial-measurements') {
		var dataTable = $('#measurement-table').dataTable();
		if(dataTable.length !== 0)
			dataTable.fnAdjustColumnSizing();
	}

}

function showStudyInfo() {
	$("#folderBrowserModal").modal("show");
}

function initializeReviewDatasetTabs(datasetId) {
	'use strict';
	$('#dataset-li' + datasetId).on('click', function() {
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option:selected').prop('selected', false);
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option').each(function(index) {
			if ($(this).val() === datasetId) {
				$(this).prop('selected', true);
			}
		});
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection').change();
	});

	$('#dataset-li' + datasetId +' .fbk-close-dataset-tab').on('click', function() {
		var datasetId = $(this).attr('id'),
			showFirst = false;
		if ($(this).parent().parent().hasClass('active')) {
			showFirst = true;
		}
		$('li#dataset-li' + datasetId).remove();
		$('#measurement-tabs #dset-tab-' + datasetId).remove();
		if (showFirst && $('#measurement-tab-headers li').length > 0) {
			var datasetIdString = $('#measurement-tab-headers li:eq(0) .fbk-close-dataset-tab').attr('id');
			$('li#dataset-li' + datasetIdString).addClass('active');
			$('#measurement-tabs #dset-tab-' + datasetIdString).show();
		}
	});
}