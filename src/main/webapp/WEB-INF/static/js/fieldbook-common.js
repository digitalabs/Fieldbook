var isAdvanceListGeneratedForTrial = false;

$(function() {
	'use strict';

	// attach spinner operations to ajax events
	jQuery.ajaxSetup({
		beforeSend: function() {
			SpinnerManager.addActive();
		},
		complete: function() {
			SpinnerManager.resolveActive();
		},
		success: function() {},
		error: function(jqXHR, textStatus, errorThrown) {
			if (jqXHR.status === 500) {
				showErrorMessage('', ajaxGenericErrorMsg);
			} else {
				showErrorMessage('INVALID INPUT', jqXHR.responseText);
			}
		}
	});

	// COMMENTED BROKE ANGULAR COMBO
	/*	if (typeof convertToSelect2 === 'undefined' || convertToSelect2) {
			// Variable is undefined
			$('select').each(function() {
				$(this).select2({minimumResultsForSearch: 20});
			});
		}*/

	function measureScrollBar() {
		// david walsh
		var inner = document.createElement('p');
		inner.style.width = '100%';
		inner.style.height = '200px';

		var outer = document.createElement('div');
		outer.style.position = 'absolute';
		outer.style.top = '0px';
		outer.style.left = '0px';
		outer.style.visibility = 'hidden';
		outer.style.width = '200px';
		outer.style.height = '150px';
		outer.style.overflow = 'hidden';
		outer.appendChild (inner);

		document.body.appendChild (outer);
		var w1 = inner.offsetWidth;
		outer.style.overflow = 'scroll';
		var w2 = inner.offsetWidth;
		if (w1 == w2) {
			w2 = outer.clientWidth;
		}

		document.body.removeChild (outer);

		return (w1 - w2);
	}


	$(document.body)
		.on('show.bs.modal', function() {
			if (this.clientHeight < window.innerHeight) {return;}

			var scrollbarWidth = measureScrollBar();
			if (scrollbarWidth) {
				$(document.body).css('padding-right', scrollbarWidth);
			}
		})
		.on('shown.bs.modal', function () {
			/**
			 * XXX Multiple modals are not supported in bootstrap.
			 * https://bootstrapdocs.com/v3.3.6/docs/javascript/#callout-stacked-modals
			 * If we are opening two modals at the same time or chaining one after another,
			 * the closing modal will remove
			 * the modal-open class from the body, making the scrollbar disappear
			 */
			$(this).addClass('modal-open');
		})
		.on('hidden.bs.modal', function() {
			$(document.body).css('padding-right', 0);

			// is there any other modal open?
			if ($('.modal.in').length > 0) {
				/**
				 * Bootstrap will remove modal-open on hide:
				 * https://github.com/twbs/bootstrap/blob/81df608a40bf0629a1dc08e584849bb1e43e0b7a/dist/js/bootstrap.js#L1081
				 * causing issues with the scroll of other modals
				 * This is to avoid that
				 */
				$(document.body).addClass('modal-open');
			}
		});

	$('.fbk-help')
		.click(function() {
			var helpModule = $(this).data().helpLink;
			$.get('/ibpworkbench/controller/help/getUrl/' + helpModule).success(function(helpUrl) {
				if (!helpUrl || !helpUrl.length) {
					$.when(
						$.get('/ibpworkbench/controller/help/headerText'),
						$.get('/ibpworkbench/VAADIN/themes/gcp-default/layouts/help_not_installed.html')
					).done(function(headerText,helpHtml) {
						bootbox.dialog({
							title: headerText[0],
							message: helpHtml[0],
							className: 'help-box',
							onEscape: true
						});
					});
				} else {
					window.open(helpUrl);
				}
			});
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
		success: function(data) {
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

			if (sectionDiv === 'trial-details-list') {
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
				makeGermplasmListDraggable(makeDraggableBool);

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

			if (sectionDiv === 'trial-details-list') {
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

function createFieldMap() {

	if ($('.import-study-data').data('data-import') === '1') {
		showErrorMessage('', needSaveImportDataError);
		return;
	}
	var id = $('#studyId').val(),
		name = $('#studyName').val();

	openStudyFieldmapTree(id, name);
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

function $safeId(fieldId) {
	return $(getJquerySafeId(fieldId));
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
	if (!val || (typeof val != 'string' || val.constructor != String)) {
		return(false);
	}
	var isNumber = !isNaN(Number(val));
	if (isNumber) {
		if (val.indexOf('.') != -1) {
			return(true);
		} else {
			return isInt(val);
		}
	} else {
		return(false);
	}
}

function selectTrialInstance() {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance',
		type: 'GET',
		cache: false,
		data: '',
		success: function (data) {
			if (data.fieldMapInfo != null && data.fieldMapInfo != '') {
				if (parseInt(data.size) > 1) {
					// Show popup to select fieldmap to display
					clearStudyTree();
					isViewFieldmap = true;
					createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap);
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
}

function selectTrialInstanceCreate() {
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
				createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap);
				$('#selectTrialInstanceModal').modal('toggle');
			}
		}
	});
}

function createStudyTree(fieldMapInfoList, hasFieldMap) {
	var hasOneInstance = false;
	createHeader(hasFieldMap);
	$.each(fieldMapInfoList, function(index, fieldMapInfo) {
		createRow(getPrefixName('study', fieldMapInfo.fieldbookId), '', fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, hasFieldMap, hasOneInstance);
		$.each(fieldMapInfo.datasets, function(index, value) {
			hasOneInstance = fieldMapInfoList.length === 1 && fieldMapInfoList[0].datasets.length === 1 && fieldMapInfoList[0].datasets[0].trialInstances.length === 1;
			// Create study tree up to instance level
			createRow(getPrefixName('dataset', value.datasetId), getPrefixName('study', fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, hasFieldMap, hasOneInstance);
			$.each(value.trialInstances, function (index, childValue) {
				if ((hasFieldMap && childValue.hasFieldMap) || !hasFieldMap) {
					createRow(getPrefixName('trialInstance', childValue.geolocationId), getPrefixName('dataset', value.datasetId), childValue, childValue.geolocationId, hasFieldMap, hasOneInstance);
				}
			});
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
		newRow = newRow + '<th style="width:35%">' + trialName + '</th>' +
			'<th style="width:15%">' + entryLabel + '</th>' +
			'<th style="width:10%">' + repLabel + '</th>' +
			'<th style="width:20%">' + plotLabel + '</th>';
		newRow = newRow + '<th style="width:15%">' + fieldmapLabel + '</th>';
	} else {
		newRow = newRow + '<th style="width:40%"></th>' +
			'<th style="width:20%">' + entryLabel + '</th>' +
			'<th style="width:20%">' + repLabel + '</th>' +
			'<th style="width:20%">' + plotLabel + '</th>';

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
	newCell = '<td>' + checkBox + '&nbsp;' + datasetName + '</td><td>' + value.plotCount + '</td>';
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
		newCell = newCell + '<td>' + value + '</td><td></td><td></td><td></td>';

		if (!withFieldMap) {
			newCell = newCell + '<td></td>';
		}
	} else {
		// Trial instance level
		if (withFieldMap) {
			// For view fieldmap
			newRow = '<tr id="' + realId + '" class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
			newCell = '<td>' + value.trialInstanceNo + '</td><td>' + value.entryCount + '</td>';
			newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
		} else {
			// For create new fieldmap
			hasFieldMap = value.hasFieldMap ? 'Yes' : 'No';
			disabledString = value.hasFieldMap ? 'disabled' : '';
			var checked = hasOneInstance ? 'checked' : '';

			newRow = '<tr class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
			checkBox = '<input ' + disabledString + ' class="checkInstance" type="checkbox" id="' + realId + '" ' + checked + ' /> &nbsp;&nbsp;';
			newCell = '<td>' + checkBox + '&nbsp;' + value.trialInstanceNo + '</td><td>' + value.entryCount + '</td>';
			newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
			newCell = newCell + '<td class="hasFieldMap">' + hasFieldMap + '</td>';
		}
	}
	$('#studyFieldMapTree').append(newRow + newCell + '</tr>');
}

function clearStudyTree() {
	$('#studyFieldMapTree').empty();
}

function showMessage(message) {
	createErrorNotification(errorMsgHeader, message);
}

function createLabelPrinting(tableName) {

	var count = 0,
		idVal = null,
		index,
		tempVal,
		labelPrintingHref,
		id,
		type;

	if ($('.import-study-data').data('data-import') === '1') {
		showErrorMessage('', needSaveImportDataError);
		return;
	}

	if ($('#createNurseryMainForm #studyId').length === 1) {
		idVal = ($('#createNurseryMainForm #studyId').val());
		count++;
	}else if ($('#createTrialMainForm #studyId').length === 1) {
		idVal = ($('#createTrialMainForm #studyId').val());
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
		showMessage(createLabelErrorMsg);
	}
}

function showFieldMap(tableName) {
	'use strict';
	var count = 0,
		idVal = null;

	//edit study
	if ($('.review-landing-page').length !== 0) {
		//meaning we are in the landing page
		idVal = getCurrentStudyIdInTab();
	} else if ($('#studyId')) {
		idVal = $('#studyId').val();
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
function showFieldMapPopUpCreate(ids) {

	var link = '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/';
	$.ajax({
		url: link + encodeURIComponent(ids),
		type: 'GET',
		data: '',
		success: function(data) {
			selectTrialInstanceCreate();
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		}
	});
}

// Show popup to select field map to display
function showFieldMapPopUp(tableName, id) {
	link = '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/';
	$.ajax({
		url: link + id,
		type: 'GET',
		data: '',
		success: function(data) {
			if (data.nav == '0') {
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
			hasFieldMap = $(this).parent().next().next().next().next().html();
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
		// No study instance is selected
		showMessage(noSelectedTrialInstance);
	}
}

function redirectToFirstPage() {
	var studyId = $('#studyId').val()
	location.href = $('#fieldmap-url').attr('href') + '/' + studyId + '/' + encodeURIComponent(fieldmapIds.join(','));
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

function openTreeStudy(id) {
	'use strict';
	location.href = '/Fieldbook/TrialManager/openTrial/' + id;

}

function openDeleteConfirmation() {
	'use strict';
	var deleteConfirmationText;

	$('#delete-study-heading-modal').text(deleteStudyTitle);
	deleteConfirmationText = deleteStudyConfirmation;

	$('#deleteStudyModal').modal({backdrop: 'static', keyboard: true});
	var name = $('#studyName').val();
	$('#delete-study-confirmation').html(deleteConfirmationText + ' ' + name + '?');
}

function deleteStudy() {
	'use strict';
	if ($('.review-trial-page-identifier').length) {
		deleteStudyInReview();
	} else if ($('.edit-trial-page-identifier').length) {
		deleteStudyInEdit();
	}
}

function deleteStudyInReview() {
	'use strict';

	var idVal = getCurrentStudyIdInTab();
	doDeleteStudy(idVal, function(data) {
		$('#deleteStudyModal').modal('hide');
		if (data.isSuccess === '1') {
			setTimeout(function() {
				//simulate close tab
				$('#' + idVal).trigger('click');
				//remove it from the tree
				if ($('#studyTree').dynatree('getTree').getNodeByKey(idVal)) {
					$('#studyTree').dynatree('getTree').getNodeByKey(idVal).remove();
				}
				showSuccessfulMessage('', deleteStudySuccessful);
			}, 500);
		} else {
			showErrorMessage('', data.message);
		}
	});
}

function deleteStudyInEdit() {
	'use strict';
	var idVal = $('#studyId').val();
	doDeleteStudy(idVal, function(data) {
		$('#deleteStudyModal').modal('hide');
		if (data.isSuccess === '1') {
			showSuccessfulMessage('', deleteStudySuccessful);
			setTimeout(function() {
				//go back to review nursery page
				location.href = $('#delete-success-return-url').attr('href');
			}, 500);
		} else {
			showErrorMessage('', data.message);
		}
	});
}

/* ADVANCING SPECIFIC FUNCTIONS */

function startAdvance(advanceType) {
	var $scope = angular.element('#selectEnvironmentModal').scope();
	$scope.applicationData.advanceType = advanceType;
	if (advanceType == 'sample') {
		advanceSample();
	} else {
		initSelectEnvironment();
	}
}

function initSelectEnvironment() {
	'use strict';
	$('#advanceNurseryModal').modal('hide');

	// we need to redraw the columns of the table
	if ($('.fbk-datatable-environments').length !== 0 && $('.fbk-datatable-environments').DataTable() !== null) {
		$('.fbk-datatable-environments').DataTable().columns.adjust().draw();
	}

	$('#selectEnvironmentModal').modal({ backdrop: 'static', keyboard: true });

	// Add hide listener to selectEnvironmentModal
	$('#selectEnvironmentModal').one('hidden.bs.modal', function (e) {
		// When the selectEnvironmentModal is closed, remove the bs.modal data
		// so that the modal content is refreshed when it is opened again.
		$(e.target).removeData('bs.modal');
	});

	var $scope = angular.element('#selectEnvironmentModal').scope();
	$scope.init();
	$scope.$apply();
}

function advanceSample() {
	'use strict';
	var idVal = $('#studyId').val();

	// Validate if there is something to advance
	var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

	$.ajax({
		url: '/bmsapi/study/' + cropName + '/' + idVal + '/sampled',
		type: 'GET',
		async: false,
		beforeSend: function (xhr) {
			xhr.setRequestHeader('X-Auth-Token', xAuthToken);
		}
	}).done(function (data) {
		if (data == false) {
			showErrorMessage('', advanceSamplesError);
		} else {
			initSelectEnvironment();
		}
	}).fail(function (data) {
		if (data.status == 401) {
			bmsAuth.handleReAuthentication();
		} else {
			showErrorMessage('page-rename-message-modal', data.responseJSON.errors[0].message);
		}
	});

}

function backAdvanceStudy() {
	'use strict';
	$('#advanceNurseryModal').modal('hide');

	// we need to redraw the columns of the table
	if ($('.fbk-datatable-environments').length !== 0 && $('.fbk-datatable-environments').DataTable() !== null) {
		$('.fbk-datatable-environments').DataTable().columns.adjust().draw();
	}
	$('#selectEnvironmentModal').modal('show');
}

function createSample() {
	'use strict';
	if ($('.import-study-data').data('data-import') === '1') {
		showErrorMessage('', needSaveImportDataError);
		return;
	}

	$('#selectSelectionVariableToSampleListModal').modal('hide');
	$('.fbk-datatable-environments').DataTable().columns.adjust().draw();
	$('#selectEnvironmentToSampleListModal').modal({ backdrop: 'static', keyboard: true });

	var scope = angular.element('#selectEnvironmentToSampleListModal').scope();
	scope.init();
	scope.$apply();
}

function selectEnvironmentContinueAdvancing(trialInstances, noOfReplications, selectedLocations, isTrialInstanceNumberUsed, advanceType) {
	'use strict';
	var idVal = $('#studyId').val();
	$('#selectEnvironmentModal').modal('hide');
	var locationDetailHtml = generateLocationDetailTable(selectedLocations, isTrialInstanceNumberUsed);
	advanceStudy(idVal, trialInstances, noOfReplications, locationDetailHtml, advanceType);
}


function selectedEnvironmentContinueCreatingSample(trialInstances) {
	'use strict';
	var idVal = $('#studyId').val();
	$('#selectEnvironmentToSampleListModal').modal('hide');

	var scope = angular.element('#selectSelectionVariableToSampleListModal').scope();
	scope.init(idVal, trialInstances);
	$('#selectSelectionVariableToSampleListModal').modal('show');
}

function openSampleSummary(plotId, plotNumber) {
	'use strict';
	BMS.Fieldbook.SamplesSummaryDataTable('#samples-summary-table', plotId, plotNumber);
	$('#samplesSummaryModal').modal({ backdrop: 'static', keyboard: true });
	$('#samples-summary-table').wrap('<div style="overflow-x: auto" />');
}

function generateLocationDetailTable(selectedLocations, isTrialInstanceNumberUsed) {
	//TODO Why do we generate an html code here in js?
	//FIXME The caption is not localised
	var result = "<table class='table table-curved table-condensed'>";
	if (isTrialInstanceNumberUsed) {
		result += "<caption>Update Location Name or Location Abbr in Environment Details.</caption>";
	}
	result += "<thead><tr><th>" + selectedLocations[0] + "</th></tr></thead>";

	for (var i = 1; i < selectedLocations.length; i++) {
		result += "<tbody><tr>";
		result += "<td>" + selectedLocations[i] + "</td>";
		result += "</tr></tbody>";
	}
	result += "</table>";
	return result;
}

/* END ADVANCING TRIAL SPECIFIC FUNCTIONS */

/* ADVANCING NURSERY SPECIFIC FUNCTIONS */

function advanceNursery() {
	var idVal = $('#createNurseryMainForm #studyId').val();
	advanceStudy(idVal);
}

/* END ADVANCING NURSERY SPECIFIC FUNCTIONS */


/*
 * Section for Advancing Study (Common for Trial and Nursery)
 * @param studyId Nursery or Trial study Id
 * @param locationIds Location will be passed for Advance Trial only
 */
function advanceStudy(studyId, trialInstances, noOfReplications, locationDetailHtml, advanceType) {
	'use strict';
	var count = 0,
		idVal = studyId;

	if ($('.import-study-data').data('data-import') === '1') {
		showErrorMessage('', needSaveImportDataError);
		return;
	}

	count++;
	if (count !== 1) {
		showMessage(advanceStudyError);
		return;
	}

	//TODO do we advance the study using the same ajax function as advancing the nursery from the nursery manager.
	//TODO Should that be common then with the common path?
	var advanceStudyHref = '/Fieldbook/StudyManager/advance/study';
	advanceStudyHref = advanceStudyHref + '/' + encodeURIComponent(idVal);
	advanceStudyHref = advanceStudyHref + '?selectedInstances=' + encodeURIComponent(trialInstances.join(","));

	if (noOfReplications) {
		advanceStudyHref = advanceStudyHref + '&noOfReplications=' + encodeURIComponent(noOfReplications);
	}

	if (advanceType) {
		advanceStudyHref = advanceStudyHref + '&advanceType=' + encodeURIComponent(advanceType);
	}


	if (idVal != null) {
		//TODO the failure of the ajax request should be processed and error shown
		$.ajax({
			url: advanceStudyHref,
			type: 'GET',
			aysnc: false,
			success: function(html) {
				$('#advance-nursery-modal-div').html(html);
				$('#advanceNurseryModal')
					.modal({ backdrop: 'static', keyboard: true });

				$('#advanceNurseryModal select').not('.fbk-harvest-year').each(function () {
					$(this).select2({minimumResultsForSearch: $(this).find('option').length == 0 ? -1 : 20});
				});
				$('#advanceNurseryModal select.fbk-harvest-year').each(function () {
					$(this).select2({minimumResultsForSearch: -1});
				});

				$('#location-details-section').append(locationDetailHtml);

			}
		});
	}
}
function showInvalidInputMessage(message) {
	'use strict';
	createErrorNotification(invalidInputMsgHeader, message);
}
function showErrorMessage(messageDivId, message) {
	'use strict';
	createErrorNotification(errorMsgHeader, message);
}

function showSuccessfulMessage(messageDivId, message) {
	'use strict';
	createSuccessNotification(successMsgHeader, message);
}

function showAlertMessage(messageDivId, message, duration) {
	'use strict';
	createWarningNotification(warningMsgHeader, message, duration);
}

function hideErrorMessage() {
	$('#page-message .alert-danger').fadeOut(1000);
}

function initializeHarvestLocationSelect2(locationSuggestions, locationSuggestionsObj) {

	$.each(locationSuggestions, function(index, value) {
		var locNameDisplay = value.lname;
		if (value.labbr != null && value.labbr != '') {
			locNameDisplay  += ' - (' + value.labbr + ')';
		}
		locationSuggestionsObj.push({
			id: value.locid,
			text: locNameDisplay,
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

function initializeHarvestLocationBreedingFavoritesSelect2(locationSuggestionsBreedingFavorites, locationSuggestionsBreedingFavoritesObj) {

	$.each(locationSuggestionsBreedingFavorites, function(index, value) {
		locationSuggestionsBreedingFavoritesObj.push({
			id: value.locid,
			text: value.lname,
			abbr: value.labbr
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('harvestLocationIdBreedingFavorites')).select2({
		minimumResultsForSearch: locationSuggestionsBreedingFavoritesObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: locationSuggestionsBreedingFavoritesObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}
	}).on('change', function() {
		$('#' + getJquerySafeId('harvestLocationId')).val($('#' + getJquerySafeId('harvestLocationIdBreedingFavorites')).select2('data').id);
		$('#' + getJquerySafeId('harvestLocationName')).val($('#' + getJquerySafeId('harvestLocationIdBreedingFavorites')).select2('data').text);
		$('#' + getJquerySafeId('harvestLocationAbbreviation')).val($('#' + getJquerySafeId('harvestLocationIdBreedingFavorites')).select2('data').abbr);
		if ($('#harvestloc-tooltip')) {
			$('#harvestloc-tooltip').attr('title', locationTooltipMessage + $('#' + getJquerySafeId('harvestLocationIdBreedingFavorites')).select2('data').abbr);
			$('.help-tooltip-nursery-advance').tooltip('destroy');
			$('.help-tooltip-nursery-advance').tooltip();
		}
	});
}

function initializeHarvestLocationBreedingSelect2(locationSuggestionsBreeding, locationSuggestionsBreedingObj) {

	$.each(locationSuggestionsBreeding, function(index, value) {
		locationSuggestionsBreedingObj.push({
			id: value.locid,
			text: value.lname,
			abbr: value.labbr
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('harvestLocationIdBreeding')).select2({
		minimumResultsForSearch: locationSuggestionsBreedingObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: locationSuggestionsBreedingObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}
	}).on('change', function() {
		$('#' + getJquerySafeId('harvestLocationId')).val($('#' + getJquerySafeId('harvestLocationIdBreeding')).select2('data').id);
		$('#' + getJquerySafeId('harvestLocationName')).val($('#' + getJquerySafeId('harvestLocationIdBreeding')).select2('data').text);
		$('#' + getJquerySafeId('harvestLocationAbbreviation')).val($('#' + getJquerySafeId('harvestLocationIdBreeding')).select2('data').abbr);
		if ($('#harvestloc-tooltip')) {
			$('#harvestloc-tooltip').attr('title', locationTooltipMessage + $('#' + getJquerySafeId('harvestLocationIdBreeding')).select2('data').abbr);
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

function initializeMethodSelect2(methodSuggestions, methodSuggestionsObj, methodId) {

	$.each(methodSuggestions, function(index, value) {
		methodSuggestionsObj.push({
			id: value.mid,
			text: value.mname + ' - ' + value.mcode,
			tooltip: value.mdesc
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId(methodId)).select2({
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
			$('#' + getJquerySafeId('advanceBreedingMethodId')).val($('#' + getJquerySafeId(methodId)).select2('data').id);
			if ($('#method-tooltip')) {
				$('#method-tooltip').attr('title', $('#' + getJquerySafeId(methodId)).select2('data').tooltip);
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

function exportGermplasmList() {
	'use strict';
	var submitExportUrl = '/Fieldbook/ExportManager/exportGermplasmList/',
		formName = '#exportGermplasmListForm',
		type = $('#exportGermplasmListFormat').val();
	var exportOptions = {
		dataType: 'text',
		success: showGermplasmExportResponse // post-submit callback
	};
	if (type === '0') {
		showMessage('Please choose export format');
		return false;
	}
	submitExportUrl = submitExportUrl + type;
	$(formName).attr('action', submitExportUrl);
	$(formName).ajaxForm(exportOptions).submit();
}

function exportStudy() {
	'use strict';
	var type = $('#exportType').val();
	if (type === '0') {
		showMessage('Please choose export type');
		return false;
	}

	doExportContinue(type);
}

function getExportCheckedAdvancedList() {
	'use strict';
	var advancedLists = [];
	$('.export-advance-germplasm-lists-checkbox').each(function() {
		if ($(this).is(':checked')) {
			advancedLists.push($(this).data('advance-list-id'));
		}
	});
	return advancedLists;
}
function getExportCheckedInstances() {
	'use strict';
	var checkedInstances = [];
	$('.trial-instance-export').each(function() {
		if ($(this).is(':checked')) {
			checkedInstances.push({'instance': $(this).data('instance-number'), 'hasFieldmap':  $(this).data('has-fieldmap')});
		}
	});
	return checkedInstances;
}
function validateTrialInstance() {
	'use strict';
	var checkedInstances = getExportCheckedInstances(),
		counter = 0,
		additionalParam = '';
	if (checkedInstances !== null && checkedInstances.length !== 0) {

		for (counter = 0 ; counter < checkedInstances.length ; counter++) {
			if (additionalParam !== '') {
				additionalParam += '|';
			}
			additionalParam += checkedInstances[counter].instance;
		}
	}
	return additionalParam;
}
function exportAdvanceStudyList(advancedListIdParams) {
	'use strict';
	$('#exportAdvanceStudyForm #exportAdvanceListGermplasmIds').val(advancedListIdParams);
	$('#exportAdvanceStudyForm #exportAdvanceListGermplasmType').val($('#exportAdvancedType').val());
	$('#exportAdvanceStudyForm').ajaxForm(exportAdvanceOptions).submit();
}

function doExportContinue(paramUrl) {
	var currentPage = $('#measurement-data-list-pagination .pagination .active a').html(),
		additionalParams = '',
		formname,
		$form,
		serializedData,
		exportWayType;

	formname = '#addVariableForm, #addVariableForm2';
	$form = $(formname);
	serializedData = $form.serialize();

	additionalParams = validateTrialInstance();
	if (additionalParams == 'false') {
		return false;
	}

	exportWayType = '/' + $('#exportWayType').val();
	doFinalExport(paramUrl, additionalParams, exportWayType);
}

function doFinalExport(paramUrl, additionalParams, exportWayType) {
	var action = submitExportUrl,
		newAction = '',
		studyId = '0',
		visibleColumns = '';

	newAction = action + 'exportStudy/' + paramUrl + '/' + additionalParams;
	newAction += exportWayType;
	studyId = $('#studyId').val();

	if ($('#browser-studies').length === 0) {
		// the study is opened
		var tableContainsPlotId = BMS.Fieldbook.MeasurementsTable.containsHeader('measurement-table', '8201');
		visibleColumns = getMeasurementTableVisibleColumns(tableContainsPlotId);
		var exportType = $('#exportType').val();
		// excel or csv
		if ((exportType == 6 || exportType == 2) && visibleColumns.length !== 0) {
			showWarningMessageForRequiredColumns(visibleColumns);
		}
	}

	var columnOrders = '';
	if ($('.review-nursery-details').length == 0) {
		var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table', true);
		columnOrders = (JSON.stringify(columnsOrder));
	}
	$.ajax(newAction, {
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
		data: JSON.stringify({
			'visibleColumns': visibleColumns,
			'columnOrders': columnOrders,
			'studyExportId': studyId
		}),
		type: 'POST',
		dataType: 'text',
		success: function (data) {
			showExportResponse(data);
		}
	});
}

function hasRequiredColumnsHiddenInMeasurementDataTable(visibleColumns) {
	'use strict';
	var requiredColumns = [plotNoTermId, entryNoTermId, desigTermId];
	var i = 0;
	var noOfRequiredColumns = 0;
	for (i = 0; i < requiredColumns.length; i++) {
		if (visibleColumns.indexOf(requiredColumns[i]) >= 0) {
			noOfRequiredColumns++;
		}
	}
	return !(noOfRequiredColumns == requiredColumns.length);
}

function showWarningMessageForRequiredColumns(visibleColumns) {
	'use strict';
	var warningMessage = 'The export file will leave out contain columns that you have marked ' +
		'as hidden in the table view, with the exception of key columns that are ' +
		'necessary to identify your data when you import it back into the system.';
	if (hasRequiredColumnsHiddenInMeasurementDataTable(visibleColumns)) {
		showAlertMessage('', warningMessage);
	}
}

function getMeasurementTableVisibleColumns(addPlotId) {
	'use strict';
	var visibleColumns = '';
	if ($('[ui-view="editMeasurements"]').text().length === 0) {
		return visibleColumns;
	}
	var headers = $('#measurement-table_wrapper .dataTables_scrollHead [data-term-id]');
	var headerCount = headers.size();
	var i = 0;
	var plotIdFound = false;
	for (i = 0; i < headerCount; i++) {
		var headerId = $('#measurement-table_wrapper .dataTables_scrollHead [data-term-id]:eq(' + i + ')').attr('data-term-id');
		if ($.isNumeric(headerId)) {
			if (headerId == '8201'){
				plotIdFound = true;
			}
			if (visibleColumns.length == 0) {
				visibleColumns = headerId;
			} else {
				visibleColumns = visibleColumns + ',' + headerId;
			}
		}
	}
	if (addPlotId && !plotIdFound) {
		visibleColumns = visibleColumns + ',' + '8201';
	}
	return visibleColumns;
}

function importFormatType(type) {
	'use strict';
	var action = '/Fieldbook/ImportManager/import/' + type,
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

	if ($('.import-study-data').data('data-import') === '1') {
		setTimeout(function() {$('#importOverwriteConfirmation').modal({ backdrop: 'static', keyboard: true });}, 300);
	} else {
		continueStudyImport(false);
	}
}
function continueStudyImport(doDataRevert) {
	'use strict';
	if (doDataRevert) {
		revertData(false);
		$('#importOverwriteConfirmation').modal('hide');
	}

	$('#importStudyUploadForm').ajaxForm(importOptions).submit();
}

function showImportOptions() {
	'use strict';
	$('#fileupload').val('');
	$('#importStudyModal').modal({ backdrop: 'static', keyboard: true });
	// Navigate to edit measurements tab when clicking on import measurements
	// similar to the nursery
	var scope = angular.element(document.getElementById("mainApp")).scope();
	scope.$apply(function () {
		scope.navigateToTab('editMeasurements');
	});

	if ($('.import-study-data').data('data-import') === '1') {
		showAlertMessage('', importDataWarningNotification);
	}
}
function goBackToImport() {
	'use strict';
	revertData(false);
	$('#importStudyConfirmationModal').modal('hide');
	$('#importStudyDesigConfirmationModal').modal('hide');
	$('#importOverwriteConfirmation').modal('hide');
	setTimeout(function() {$('#importStudyModal').modal({ backdrop: 'static', keyboard: true });}, 300);

}

function isFloat(value) {
	'use strict';
	return !isNaN(parseInt(value, 10)) && (parseFloat(value, 10) == parseInt(value, 10));
}

function moveToTopScreen() {

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
	var startDate = $('#startDate').val(),
		endDate = $('#enDate').val();

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
			url: '/Fieldbook/StudyManager/advance/study/countPlots/' + ids,
			type: 'GET',
			cache: false,
			async: false,
			success: function(data) {
				var choice,
					lineSameForAll;

				if (isMixed) {
					if (data == 0) {
						var param = $('#lineVariateId').select2('data').text + ' and/or ' + $('#plotVariateId').select2('data').text;
						var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);

						valid = false;
					}
				} else if (isBulk) {
					choice = !$('#plot-variates-section').is(':visible');
					if (choice == false && data == '0') {
						var param = $('#plotVariateId').select2('data').text;
						var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);

						valid = false;
					}
				} else {
					choice = !$('#line-variates-section').is(':visible');
					lineSameForAll = $('input[type=checkbox][name=lineChoice]:checked').val() == 1;
					if (lineSameForAll == false && choice == false && data == '0') {
						var param = $('#lineVariateId').select2('data').text;
						var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);
						valid = false;
					}
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			}
		});
	}
	if (valid && isMixed) {
		return valid;
	}
	return valid;
}

function callAdvanceStudy() {

	var lines = $('#lineSelected').val();
	var methdodId = $('#advanceBreedingMethodId').val();
	var advanceType = angular.element('#mainApp').injector().get('TrialManagerDataService').applicationData.advanceType;

	var repsSectionIsDisplayed = $('#reps-section').length;
	if(repsSectionIsDisplayed) {
		var selectedReps = [];
		$('#replications input:checked').each(function() {
			selectedReps.push($(this).val());
		});

		if(selectedReps.length == 0){
			showErrorMessage('page-advance-modal-message', noReplicationSelectedError);
			return false;
		}
	}

	if (methdodId === '0' || (methdodId === '' && advanceType == 'sample')) {
		showErrorMessage('page-advance-modal-message', msgMethodError);
		return false;
	} else if (lines && !lines.match(/^\s*(\+|-)?\d+\s*$/)) {
		showErrorMessage('page-advance-modal-message', linesNotWholeNumberError);
		return false;
	} else if (validatePlantsSelected()) {
		SaveAdvanceList.doAdvanceStudy();
	}
}

function closeAdvanceListTab(uniqueId) {
	'use strict';
	$('li#advance-list' + uniqueId + '-li').remove();
	if ($('#list' + uniqueId).length === 1) {
		$('#list' + uniqueId).remove();
	}

	setTimeout(function() {
		$('#create-nursery-tab-headers li:eq(0) a').tab('show');
		$('.nav-tabs').tabdrop('layout');
	}, 100);
}

function displayAdvanceList(germplasmListId, listName, isDefault, advancedGermplasmListId, isPageLoading) {
	'use script';
	var id = advancedGermplasmListId ? advancedGermplasmListId : germplasmListId;
	var url = '/Fieldbook/germplasm/list/advance/' + id;
	if (!isDefault) {
		$('#advanceHref' + id + ' .fbk-close-tab').before(': [' + listName + ']');
		url += '?isSnapshot=0';
	} else {
		url += '?isSnapshot=1';
	}
	$.ajax({
		url: url,
		type: 'GET',
		cache: false,
		success: function (html) {
			var element = angular.element(document.getElementById("mainApp")).scope();
			// To apply scope safely
			element.safeApply(function () {
				element.addAdvanceTabData(id, html, listName, isPageLoading);
			});
			// Display Stock List if it is generated
			StockIDFunctions.generateStockListTabIfNecessary(id, isPageLoading);
		}
	});
}

function displayCrossesList(germplasmListId, listName, crossesType, isDefault, crossesListId, isPageLoading) {
	'use script';
	var id = crossesListId ? crossesListId : germplasmListId;
	var url = '/Fieldbook/germplasm/list/crosses/' + id;
	if (!isDefault) {
		$('#advanceHref' + id + ' .fbk-close-tab').before(': [' + listName + ']');
		url += '?isSnapshot=0';
	} else {
		url += '?isSnapshot=1';
	}
	$.ajax({
		url: url,
		type: 'GET',
		cache: false,
		success: function (html) {
			var element = angular.element(document.getElementById("mainApp")).scope();
			// To apply scope safely
			element.safeApply(function () {
				element.addCrossesTabData(id, html, listName, crossesType, isPageLoading);
			});
			// Display Stock List if it is generated
			StockIDFunctions.generateStockListTabIfNecessary(id, isPageLoading);
		}
	});
}

function displaySampleList(id, listName, isPageLoading) {
	'use script';

	var url = '/Fieldbook/sample/list/sampleList/' + id;

	$.ajax({
		url: url,
		type: 'GET',
		cache: false,
		success: function (html) {
			var element = angular.element(document.getElementById("mainApp")).scope();
			// To apply scope safely
			element.safeApply(function () {
				element.addSampleTabData(id, html, listName, isPageLoading);
			});
		}
	});
}

function validateBreedingMethod() {
	var id = $('#methodVariateId').val(),
		valid = true;

	if ($('input[type=checkbox][name=methodChoice]:checked').val() !== '1' && id) {
		$.ajax({
			url: '/Fieldbook/StudyManager/advance/study/countPlots/' + id,
			type: 'GET',
			cache: false,
			async: false,
			success: function(data) {
				if (data == 0) {
					var newMessage = noMethodValueErrorTrial.replace(new RegExp(/\{0\}/g), $('#methodVariateId').select2('data').text);
					showErrorMessage('page-advance-modal-message', newMessage);

					valid = false;
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			}
		});
	}
	return valid;
}

function showBaselineTraitDetailsModal(id) {
	'use strict';

	if (id !== '') {
		$.ajax({
			url: '/Fieldbook/manageSettings/settings/details/' + id,
			type: 'GET',
			cache: false,
			success: function(html) {
				$('.variable-details-section').empty().append(html);
				if ($('#selectedStdVarId').length != 0) {
					$('#selectedStdVarId').val(id);
				}
				$('#variableDetailsModal').modal('toggle');
				if ($('#variableDetailsModal')) {
					var variableName = $('#ontology-tabs').data('selectedvariablename');
					$('#variableDetailsModal .modal-title').html(variableDetailsHeader + ' ' + variableName);
				}
			}
		});
	}
}

function showBaselineTraitDetailsModal(id, variableTypeId) {
	'use strict';

	if (id !== '') {
		$.ajax({
			url: '/Fieldbook/manageSettings/settings/details/' + variableTypeId + '/' + id,
			type: 'GET',
			cache: false,
			success: function (html) {
				$('.variable-details-section').empty().append(html);
				if ($('#selectedStdVarId').length != 0) {
					$('#selectedStdVarId').val(id);
				}
				$('#variableDetailsModal').modal('toggle');
				if ($('#variableDetailsModal')) {
					var variableName = $('#ontology-tabs').data('selectedvariablename');
					$('#variableDetailsModal .modal-title').html(variableDetailsHeader + ' ' + variableName);
				}
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
		return '';
	}
}

function getAdvanceBreedingMethodURL() {
	var url = '/Fieldbook/breedingMethod/getBreedingMethods';

	var advanceType = angular.element('#mainApp').injector().get('TrialManagerDataService').applicationData.advanceType;
	if (advanceType == 'sample') {
		return '/Fieldbook/breedingMethod/getNoBulkingBreedingMethods';
	}
	return url;
}

function recreateMethodCombo(possibleFavorite, url) {
	var selectedMethodAll = $('#methodIdAll').val(),
		selectedMethodFavorite = $('#methodIdFavorite').val();
	var createGermplasm = false;
	var createGermplasmOpened = false;

	if ($('#importStudyDesigConfirmationModal').length !== 0) {
		createGermplasm = true;
		if ($('#importStudyDesigConfirmationModal').hasClass('in') || $('#importStudyDesigConfirmationModal').data('open') === '1') {
			createGermplasmOpened = true;
		}
	}

	$.ajax({
		url: url || '/Fieldbook/breedingMethod/getBreedingMethods',
		type: 'GET',
		cache: false,
		data: '',
		async: false,
		success: function(data) {
			if (data.success == '1') {
				if (createGermplasmOpened) {
					refreshImportMethodCombo(data);
					refreshMethodComboInSettings(data);
					refreshGermplasMethodCombo(data);
				} else if (selectedMethodAll != null) {

					//recreate the select2 combos to get updated list of methods
					recreateMethodComboAfterClose('methodIdAll', data.allMethods);
					recreateMethodComboAfterClose('methodIdFavorite', data.favoriteMethods);

					recreateMethodComboAfterClose('methodIdDerivativeAndMaintenance', data.allNonGenerativeMethods);
					recreateMethodComboAfterClose('methodIdDerivativeAndMaintenanceFavorite', data.favoriteNonGenerativeMethods);

					showCorrectMethodCombo();
					//set previously selected value of method
					if ($('#showFavoriteMethod').prop('checked')) {
						setComboValues(methodSuggestionsFavObj, selectedMethodFavorite, 'methodIdFavorite');
					} else {
						setComboValues(methodSuggestionsObj, selectedMethodAll, 'methodIdAll');
					}

					if ($('#advanceNurseryModal').length > 0) {
						refreshMethodComboInSettings(data);
					}
				} else {
					if ($('.hasCreateGermplasm').length === 0 || ($('.hasCreateGermplasm').length > 0 && $('.hasCreateGermplasm').val() === '0')) {
						refreshMethodComboInSettings(data);
					}
					if (createGermplasm) {
						refreshImportMethodCombo(data);
						refreshGermplasMethodCombo(data);
					}
				}
				if(possibleFavorite){
					ValidateValueCheckBoxFavorite(possibleFavorite,data);
					refreshGermplasMethodCombo(data);
				}
			} else {
				showErrorMessage('page-message', data.errorMessage);
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		}
	});
}

function refreshImportMethodCombo(data) {
	var selectedValue = null;
	if ($('#importMethodId').select2('data')) {
		selectedValue = $('#importMethodId').select2('data').id;
	}
	if ($('#importFavoriteMethod').is(':checked')) {
		if ($('#showAllMethodOnlyRadio').is(':checked')) {
			initializePossibleValuesCombo(data.favoriteMethods, '#importMethodId', false, selectedValue);
		} else {
			initializePossibleValuesCombo(data.favoriteGenerativeMethods, '#importMethodId', false, selectedValue);
		}
	} else if ($('#showAllMethodOnlyRadio').is(':checked')) {
		initializePossibleValuesCombo(data.allMethods, '#importMethodId', false, selectedValue);
	} else {
		initializePossibleValuesCombo(data.allGenerativeMethods, '#importMethodId', false, selectedValue);
	}
}

function refreshGermplasMethodCombo(data) {
	var selectedValue = null;
	if ($('#importMethodId').select2('data')) {
		selectedValue = $('#importMethodId').select2('data').id;
	}
	if ($('#importFavoriteMethod').is(':checked')) {
		if ($('#showDerivateAndMaintenanceMethodImportStudyOnlyRadio').is(':checked')) {
			initializePossibleValuesCombo(data.favoriteNonGenerativeMethods, '#importMethodId', true, selectedValue);
		} else {
			initializePossibleValuesCombo(data.favoriteMethods, '#importMethodId', true, selectedValue);
		}

	} else {
		if ($('#showDerivateAndMaintenanceMethodImportStudyOnlyRadio').is(':checked')) {
			initializePossibleValuesCombo(data.allNonGenerativeMethods, '#importMethodId', true, selectedValue);
		} else {
			initializePossibleValuesCombo(data.allMethods, '#importMethodId', true, selectedValue);
		}

	}
	replacePossibleJsonValuesImportGermPlasm(data.allNonGenerativeMethods, data.favoriteNonGenerativeMethods, data.allMethods, data.favoriteMethods,
		'Method');
}

function refreshImportLocationCombo(data) {
	var selectedValue = null;
	if ($('#importLocationId').select2('data')) {
		selectedValue = $('#importLocationId').select2('data').id;
	}
	if ($('#importFavoriteLocation').is(':checked')) {
		initializePossibleValuesCombo(data.favoriteLocations,
			'#importLocationId', true, selectedValue);
	} else {
		initializePossibleValuesCombo(data.allBreedingLocations,
			'#importLocationId', true, selectedValue);
	}
	replacePossibleJsonValues(data.allBreedingLocations, data.allBreedingFavoritesLocations, data.allLocations, data.favoriteLocations,
		'Location');
}

function refreshGermplasLocationCombo(data) {
	var selectedValue = null;
	if ($('#importLocationId').select2('data')) {
		selectedValue = $('#importLocationId').select2('data').id;
	}
	if ($('#importFavoriteLocation').is(':checked')) {
		if ($('#showBreedingLocationImportStudyOnlyRadio').is(':checked')) {
			initializePossibleValuesCombo(data.allBreedingFavoritesLocations, '#importLocationId', true, selectedValue);
		} else {
			initializePossibleValuesCombo(data.favoriteLocations, '#importLocationId', true, selectedValue);
		}

	} else {
		if ($('#showBreedingLocationImportStudyOnlyRadio').is(':checked')) {
			initializePossibleValuesCombo(data.allBreedingLocations, '#importLocationId', true, selectedValue);
		} else {
			initializePossibleValuesCombo(data.allLocations, '#importLocationId', true, selectedValue);
		}

	}
	replacePossibleJsonValuesImportGermPlasm(data.allBreedingLocations, data.allBreedingFavoritesLocations, data.allLocations, data.favoriteLocations,
		'Location');
}

function generateGenericLocationSuggestions(genericLocationJson) {
	var genericLocationSuggestion = [];
	$.each(genericLocationJson, function(index, value) {
		var locNameDisplay = value.lname;
		if (value.labbr != null && value.labbr != '') {
			locNameDisplay  += ' - (' + value.labbr + ')';
		}
		genericLocationSuggestion.push({
			'id': value.locid,
			'text': locNameDisplay
		});
	});
	return genericLocationSuggestion;
}
function recreateLocationCombo(possibleFavorite) {
	var selectedLocationAll = $('#harvestLocationIdAll').val();
	var selectedLocationBreeding = $('#harvestLocationIdBreeding').val();
	var selectedLocationBreedingFavorites = $('#harvestLocationIdBreedingFavorites').val();
	var selectedLocationFavorite = $('#harvestLocationIdFavorite').val();

	var inventoryPopup = false;
	var advancePopup = false;
	var fieldmapScreen = false;
	var createGermplasm = false;
	var hasCreateGermplasm = false;
	var createGermplasmOpened = false;

	if ($('#addLotsModal').length !== 0 && ($('#addLotsModal').data('open') === '1' ||  $('#addLotsModal').hasClass('in'))) {
		inventoryPopup = true;
	} else if ($('#advanceNurseryModal').length !== 0 && ($('#advanceNurseryModal').data('open') === '1' ||  $('#advanceNurseryModal').hasClass('in'))) {
		advancePopup = true;
	} else if ($('#enterFieldDetailsForm').length !== 0) {
		fieldmapScreen = true;
	}

	if ($('#importStudyDesigConfirmationModal').length !== 0) {
		createGermplasm = true;
		if ($('#importStudyDesigConfirmationModal').hasClass('in') || $('#importStudyDesigConfirmationModal').data('open') === '1') {
			createGermplasmOpened = true;
		}
	}

	if ($('.hasCreateGermplasm').length === 0 || ($('.hasCreateGermplasm').length > 0 && $('.hasCreateGermplasm').val() === '0')) {
		hasCreateGermplasm = true;
	}

	if (inventoryPopup || advancePopup || fieldmapScreen || createGermplasm || hasCreateGermplasm || createGermplasmOpened) {
		$.ajax({
			url: '/Fieldbook/locations/getLocations',
			type: 'GET',
			cache: false,
			data: '',
			async: false,
			success: function(data) {
				if (data.success == '1') {
					if (createGermplasmOpened) {
						refreshImportLocationCombo(data);
						refreshLocationComboInSettings(data);
						refreshGermplasLocationCombo(data);
					} else if (inventoryPopup) {
						recreateLocationComboAfterClose('inventoryLocationIdAll', data.allLocations); // All locations
						recreateLocationComboAfterClose('inventoryLocationIdFavorite', data.favoriteLocations); // Favorites
						recreateLocationComboAfterClose('inventoryLocationIdSeedStorage', data.allSeedStorageLocations);// All seed Storage
						recreateLocationComboAfterClose('inventoryLocationIdFavoriteSeedStorage', data.allSeedStorageFavoritesLocations); // All Favorites
						// seed
						// Storage
						showCorrectLocationInventoryCombo();
						// set previously selected value of location
						if ($('#showFavoriteLocationInventory').prop('checked')) {
							if ($('#showSeedStorageLocationInventory').prop('checked')) {
								setComboValues(generateGenericLocationSuggestions(data.allSeedStorageFavoritesLocations), $(
									'#inventoryLocationIdFavoriteSeedStorage').val(), 'inventoryLocationIdFavoriteSeedStorage');
							} else {
								setComboValues(generateGenericLocationSuggestions(data.favoriteLocations),
									$('#inventoryLocationIdFavorite').val(), 'inventoryLocationIdFavorite');
							}
						} else {
							if ($('#showSeedStorageLocationInventory').prop('checked')) {
								setComboValues(generateGenericLocationSuggestions(data.allSeedStorageLocations), $(
									'#inventoryLocationIdSeedStorage').val(), 'inventoryLocationIdSeedStorage');
							} else {
								setComboValues(generateGenericLocationSuggestions(data.allLocations), $('#inventoryLocationIdAll').val(),
									'inventoryLocationIdAll');

							}
						}
						refreshLocationComboInSettings(data);
					} else if (advancePopup === true
						|| selectedLocationAll != null) {
						// recreate the select2 combos to get updated list
						// of locations

						if (data.allBreedingFavoritesLocations && data.allBreedingFavoritesLocations.length > 0) {
							$('#showFavoriteLocation').prop('checked', true);
						} else {
							$('#showFavoriteLocation').prop('checked', false);
						}

						recreateLocationComboAfterClose('harvestLocationIdAll', data.allLocations);
						recreateLocationComboAfterClose('harvestLocationIdBreeding', data.allBreedingLocations);
						recreateLocationComboAfterClose('harvestLocationIdBreedingFavorites', data.allBreedingFavoritesLocations);
						recreateLocationComboAfterClose('harvestLocationIdFavorite', data.favoriteLocations);
						showCorrectLocationCombo();
						// set previously selected value of location
						if ($('#showFavoriteLocation').prop('checked') && $('#showBreedingLocationOnlyRadio').prop('checked')) {
							setComboValues(selectedLocationBreedingFavorites_obj, selectedLocationBreedingFavorites, 'harvestLocationIdBreedingFavorites');
						} else if ($('#showFavoriteLocation').prop('checked')) {
							setComboValues(locationSuggestionsFav_obj, selectedLocationFavorite, 'harvestLocationIdFavorite');
						} else if ($('#showAllLocationOnlyRadio').prop('checked')) {
							setComboValues(locationSuggestions_obj, selectedLocationAll, 'harvestLocationIdAll');
						} else {
							setComboValues(locationSuggestionsBreeding_obj, selectedLocationBreeding, 'harvestLocationIdBreeding');
						}
						refreshLocationComboInSettings(data);

					} else if (fieldmapScreen === true) {
						recreateFieldLocationComboAfterClose('fieldLocationIdAll', data.allLocations);
						recreateFieldLocationComboAfterClose('fieldLocationIdFavorite', data.favoriteLocations);
						recreateFieldLocationComboAfterClose('fieldLocationIdBreeding', data.allBreedingLocations);
						recreateFieldLocationComboAfterClose('fieldLocationIdBreedingFavorites', data.allBreedingFavoritesLocations);
						showCorrectFieldLocationCombo();
						//set previously selected value of location
						if ($('#showFavoriteLocation').prop('checked') && $('#showBreedingLocationOnlyRadio').prop('checked')) {
							setComboValues(locationSuggestionsBreedingFav_obj, $('#fieldLocationIdBreedingFavorites').val(), 'fieldLocationIdBreedingFavorites');
						} else if ($('#showFavoriteLocation').prop('checked')) {
							setComboValues(locationSuggestionsFav_obj, $('#fieldLocationIdFavorite').val(), 'fieldLocationIdFavorite');
						} else if ($('#showAllLocationRadio').prop('checked')) {
							setComboValues(locationSuggestions_obj, $('#fieldLocationIdAll').val(), 'fieldLocationIdAll');
						} else {
							setComboValues(locationSuggestionsBreeding_obj, $('#fieldLocationIdBreeding').val(), 'fieldLocationIdBreeding');
						}
					} else {
						if (hasCreateGermplasm) {
							refreshLocationComboInSettings(data);
						}
						if (createGermplasm) {
							refreshImportLocationCombo(data);
						}

					}
					if(possibleFavorite){
						ValidateValueCheckBoxFavorite(possibleFavorite,data);
						refreshGermplasLocationCombo(data);
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
		var pleaseChoose = {"mid":0, "mname":"Please Choose", "mdesc":"Please Choose"};
		data.favoriteNonGenerativeMethods.unshift(pleaseChoose);
		data.allNonGenerativeMethods.unshift(pleaseChoose);

		if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data')) {
			selectedVal = $('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data').id;
		}

		//recreate select2 of breeding method
		initializePossibleValuesCombo([],
			'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);

		var allSelected = $('.filter_selectors_all_' + index).prop("checked");

		//update values of combo
		if ($('[name="' + getJquerySafeId('studyLevelVariables[' + index + '].favorite') + '"]').is(':checked')) {
			if (allSelected) {
				initializePossibleValuesCombo(data.favoriteMethods, '#' +
					getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
			} else {
				initializePossibleValuesCombo(data.favoriteNonGenerativeMethods, '#' +
					getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
			}
		} else {
			if (allSelected) {
				initializePossibleValuesCombo(data.allMethods, '#' +
					getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
			} else {
				initializePossibleValuesCombo(data.allNonGenerativeMethods, '#' +
					getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
			}
		}

		replacePossibleJsonValues(data.allNonGenerativeMethods, data.favoriteNonGenerativeMethods, data.allMethods, data.favoriteMethods,
			index);
	}
}

function refreshLocationComboInSettings(data) {
	var selectedVal = null;
	var index = getLocationRowIndex();
	if (index > -1) {
		var id = '#' + getJquerySafeId('studyLevelVariables' + index + '.value');
		if ($(id).select2('data')) {
			selectedVal = $(id).select2('data').id;
		}
		initializePossibleValuesCombo([], id, true, selectedVal);

		// update values in combo
		if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.favorite1')).is(':checked')) {
			if ($("#allLocations").is(':checked')) {
				initializePossibleValuesCombo(data.favoriteLocations, id, false, selectedVal);
			} else {
				initializePossibleValuesCombo(data.allBreedingFavoritesLocations, id, false, selectedVal);
			}
		} else if ($("#allLocations").is(':checked')) {
			initializePossibleValuesCombo(data.allLocations, id, true, selectedVal);
		} else {
			initializePossibleValuesCombo(data.allBreedingLocations, id, true, selectedVal);
		}

		replacePossibleJsonValues(data.allBreedingLocations, data.allBreedingFavoritesLocations, data.allLocations, data.favoriteLocations,
			index);
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
	} else if (comboName == 'harvestLocationIdBreeding') {
		//clear Breeding locations dropdown
		locationSuggestionsBreeding = [];
		locationSuggestionsBreedingObj = [];
		initializeHarvestLocationBreedingSelect2(locationSuggestionsBreeding, locationSuggestionsBreedingObj);
		//reload the data retrieved
		locationSuggestionsBreeding = data;
		initializeHarvestLocationBreedingSelect2(locationSuggestionsBreeding, locationSuggestionsBreedingObj);
	} else if (comboName == 'harvestLocationIdBreedingFavorites') {
		//clear BreedingFavorites locations dropdown
		locationSuggestionsBreedingFavorites = [];
		locationSuggestionsBreedingFavoritesObj = [];
		initializeHarvestLocationBreedingFavoritesSelect2(locationSuggestionsBreedingFavorites, locationSuggestionsBreedingFavoritesObj);
		//reload the data retrieved
		locationSuggestionsBreedingFavorites = data;
		initializeHarvestLocationBreedingFavoritesSelect2(locationSuggestionsBreedingFavorites, locationSuggestionsBreedingFavoritesObj);
	} else if (comboName == 'inventoryLocationIdAll') {
		//clear all locations dropdown
		initializePossibleValuesComboInventory(data, '#inventoryLocationIdAll', true, null);
	} else if (comboName == 'inventoryLocationIdFavorite') {
		initializePossibleValuesComboInventory(data, '#inventoryLocationIdFavorite', false, null);
	} else if(comboName == 'inventoryLocationIdSeedStorage' ){
		initializePossibleValuesComboInventory(data, '#inventoryLocationIdSeedStorage', false, null);
	}else if(comboName == 'inventoryLocationIdFavoriteSeedStorage' ){
		initializePossibleValuesComboInventory(data, '#inventoryLocationIdFavoriteSeedStorage', false, null);
	} else {
		//clear the favorite locations dropdown
		locationSuggestionsFav = [];
		locationSuggestionsFavObj = [];
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFavObj);
		//reload the data
		locationSuggestionsFav = data;
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFavObj);
		//	locSug = [];
	}
}


function recreateFieldLocationComboAfterClose(comboName, data) {
	if (comboName == 'fieldLocationIdAll') {
		locationSuggestions = [];
		locationSuggestions_obj = [];
		initializeFieldLocationsSelect2(locationSuggestions, locationSuggestions_obj,comboName);
		//reload the data retrieved
		locationSuggestions = data;
		initializeFieldLocationsSelect2(locationSuggestions, locationSuggestions_obj,comboName);
	} else if (comboName == 'fieldLocationIdBreeding') {
		locationSuggestionsBreeding = [];
		locationSuggestionsBreeding_obj = [];
		initializeFieldLocationsSelect2(locationSuggestionsBreeding, locationSuggestionsBreeding_obj, comboName);
		//reload the data retrieved
		locationSuggestionsBreeding = data;
		initializeFieldLocationsSelect2(locationSuggestionsBreeding, locationSuggestionsBreeding_obj, comboName);
	} else if (comboName == 'fieldLocationIdBreedingFavorites') {
		locationSuggestionsBreedingFav = [];
		locationSuggestionsBreedingFav_obj = [];
		initializeFieldLocationsSelect2(locationSuggestionsBreedingFav, locationSuggestionsBreedingFav_obj,comboName);
		//reload the data retrieved
		locationSuggestionsBreedingFav = data;
		initializeFieldLocationsSelect2(locationSuggestionsBreedingFav, locationSuggestionsBreedingFav_obj,comboName);
	} else if (comboName=="fieldLocationIdFavorite") {
		locationSuggestionsFav = [];
		locationSuggestionsFav_obj = [];
		initializeFieldLocationsSelect2(locationSuggestionsFav, locationSuggestionsFav_obj,comboName);
		//reload the data retrieved
		locationSuggestionsFav = data;
		initializeFieldLocationsSelect2(locationSuggestionsFav, locationSuggestionsFav_obj,comboName);
	}
}

function recreateMethodComboAfterClose(comboName, data) {
	if (comboName == 'methodIdAll') {
		//clear the all methods dropdown
		methodSuggestions = [];
		methodSuggestionsObj = [];
		initializeMethodSelect2(methodSuggestions, methodSuggestionsObj, comboName);
		//reload the data
		methodSuggestions = data;
		initializeMethodSelect2(methodSuggestions, methodSuggestionsObj, comboName);
	} else if (comboName == 'methodIdDerivativeAndMaintenance') {
		//clear the all methods dropdown
		methodSuggestionsDerivativeAndMaintenance = [];
		methodSuggestionsDerivativeAndMaintenanceObj = [];
		initializeMethodSelect2(methodSuggestionsDerivativeAndMaintenance, methodSuggestionsDerivativeAndMaintenanceObj, comboName);
		//reload the data
		methodSuggestionsDerivativeAndMaintenance = data;
		initializeMethodSelect2(methodSuggestionsDerivativeAndMaintenance, methodSuggestionsDerivativeAndMaintenanceObj, comboName);
	} else if (comboName == 'methodIdDerivativeAndMaintenanceFavorite') {
		//clear the all methods dropdown
		methodSuggestionsDerivativeAndMaintenanceFavorite = [];
		methodSuggestionsDerivativeAndMaintenanceFavoriteObj = [];
		initializeMethodSelect2(methodSuggestionsDerivativeAndMaintenanceFavorite, methodSuggestionsDerivativeAndMaintenanceFavoriteObj, comboName);
		//reload the data
		methodSuggestionsDerivativeAndMaintenanceFavorite = data;
		initializeMethodSelect2(methodSuggestionsDerivativeAndMaintenanceFavorite, methodSuggestionsDerivativeAndMaintenanceFavoriteObj, comboName);
	} else {
		//clear the favorite methods dropdown
		methodSuggestionsFav = [];
		methodSuggestionsFavObj = [];
		initializeMethodSelect2(methodSuggestionsFav, methodSuggestionsFavObj, 'methodIdFavorite');
		//reload the data
		methodSuggestionsFav = data;
		initializeMethodSelect2(methodSuggestionsFav, methodSuggestionsFavObj, 'methodIdFavorite');
	}
}

function changeBuildOption() {
	'use strict';
	if ($('#studyBuildOption').is(':checked')) {
		$('#choosePreviousStudy, #resetTabsData')
			.removeClass('fbk-hide')
			.addClass('fbk-show-inline');
	} else {
		$('#choosePreviousStudy, #resetTabsData')
			.addClass('fbk-hide')
			.removeClass('fbk-show-inline');
	}
}

function createFolder() {
	'use strict';

	var folderName = $.trim($('#addFolderName', '#studyTreeModal').val()),
		parentFolderId;

	if (folderName === '') {
		showErrorMessage('page-add-study-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else if (! isValidInput(folderName)) {
		showErrorMessage('page-add-study-folder-message-modal', invalidFolderNameCharacterMessage);
		return false;
	} else {
		var activeStudyNode = $('#studyTree').dynatree('getTree').getActiveNode();

		if (activeStudyNode == null || activeStudyNode.data.isFolder === false) {
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
					doStudyLazyLoad(node, data.newFolderId);
					node.focus();
					node.expand();
					$('#addFolderDiv', '#studyTreeModal').slideUp();
					showSuccessfulMessage('', addFolderSuccessful);
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
	if (!$(object).hasClass('disable-image')) {
		var currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title,
			isFolder = $('#studyTree').dynatree('getTree').getActiveNode().data.isFolder,
			deleteConfirmationText,
			folderId = $('#studyTree').dynatree('getTree').getActiveNode().data.key,
			folderName = JSON.stringify({'folderName': currentFolderName});

		if (isFolder) {
			$.ajax({
				url: '/Fieldbook/StudyTreeManager/isFolderEmpty/' + folderId,
				headers: {
					'Accept': 'application/json',
					'Content-Type': 'application/json'
				},
				type: 'POST',
				data: folderName,
				cache: false,
				success: function (data) {
					var node;
					if (data.isSuccess === '1') {
						$('#delete-heading-modal').text(deleteFolderTitle);
						deleteConfirmationText = deleteConfirmation;
						showDeleteStudyFolderDiv(deleteConfirmationText);
					} else {
						hideAddFolderDiv();
						hideRenameFolderDiv();
						$('#cant-delete-heading-modal').text(deleteFolderTitle);
						$('#cant-delete-message').html(data.message);
						$('#cantDeleteFolder').modal('show');
					}
				}
			});
		} else {

			$('#delete-heading-modal').text(deleteStudyTitle);
			deleteConfirmationText = deleteStudyConfirmation;
			showDeleteStudyFolderDiv(deleteConfirmationText);
		}
	}
}

function showDeleteStudyFolderDiv(deleteConfirmationText) {
	hideAddFolderDiv();
	hideRenameFolderDiv();
	var currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title;
	$('#delete-confirmation').html(deleteConfirmationText + ' ' + currentFolderName + '?');
	$('#deleteStudyFolder').modal('show');
	$('#page-delete-study-folder-message-modal').html('');
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
					showSuccessfulMessage('', deleteFolderSuccessful);
				} else {
					showErrorMessage('page-delete-study-folder-message-modal', data.message);
				}
			}
		});
	} else {
		doDeleteStudy(folderId, function(data) {
			var node;
			$('#deleteStudyFolder').modal('hide');
			if (data.isSuccess === '1') {
				node = $('#studyTree').dynatree('getTree').getActiveNode();
				if (node != null) {
					node.remove();
				}
				changeBrowseNurseryButtonBehavior(false);
				showSuccessfulMessage('', deleteStudySuccessful);
			} else {
				showErrorMessage('', data.message);
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

	if (targetId === 'LOCAL') {
		targetId = 1;
	}

	$.ajax({
		url: '/Fieldbook/StudyTreeManager/moveStudyFolder',
		type: 'POST',
		data: 'sourceId=' + sourceId + '&targetId=' + targetId + '&isStudy=' + isStudy,
		cache: false,
		success: function(data) {
			if (data.isSuccess === '1') {
				var node = targetNode;
				sourceNode.remove();
				doStudyLazyLoad(node);
				node.focus();
			}
			else {
				showErrorMessage('page-rename-message-modal', data.message);
			}
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
		currentFolderName = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title;
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
				showSuccessfulMessage('', deleteItemSuccessful);
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

function moveSamplesListFolder(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key,
		targetId = targetNode.data.key;
	var isCropList = false;

	if (targetId === 'CROPLISTS') {
		isCropList = true;
	}

	if (targetId === 'LISTS' || targetId === 'CROPLISTS') {
		targetId = 0;
	}

	var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

	return $.ajax({
		url: '/bmsapi/sampleLists/' + cropName + '/sampleListFolder/' + sourceId + '/move?newParentId=' + targetId
		+ '&isCropList=' + isCropList + '&programUUID=' + currentProgramId,
		type: 'PUT',
		beforeSend: function (xhr) {
			xhr.setRequestHeader('X-Auth-Token', xAuthToken);
		},
		error: function (data) {
			if (data.status == 401) {
				bmsAuth.handleReAuthentication();
			} else {
				showErrorMessage('page-rename-message-modal', data.responseJSON.errors[0].message);
			}
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
			showGermplasmDetailsPopUp(gid, desig, germplasmDetailsUrl);
		}
	});
}

function showGermplasmDetailsPopUp(gid, desig, germplasmDetailsUrl) {
	'use strict';
	var url = '/Fieldbook/ListTreeManager/getPreferredName/' + gid;
	$.ajax({
		url: url,
		type: 'GET',
		data: '',
		cache: false,
		success: function(preferredName) {
			desig =  preferredName;
			$('#openGermplasmFrame').attr('src', germplasmDetailsUrl + gid);
			$('#openGermplasmModal .modal-title').html(headerMsg1 + ' ' + desig + ' (' + headerMsg2 + ' ' + gid + ')');
			$('#openGermplasmModal').modal({ backdrop: 'static', keyboard: true });
		}
	});
}

function isAllowedEditMeasurementDataCellForTrials(needToSaveFirst) {
	'use strict';
	var trialManagerDataService = angular.element('#mainApp').injector().get('TrialManagerDataService');
	if (needToSaveFirst) {
		showAlertMessage('', $.fieldbookMessages.measurementsTraitsChangeWarning);
		return false;
	}
	trialManagerDataService.warnAboutUnappliedChanges();
	return !trialManagerDataService.applicationData.unappliedChangesAvailable;
}

function isAllowedEditMeasurementDataCell() {
	'use strict';
	// used to watch for changes on Traits
	var needToSaveFirst = $('body').data('needToSave') === '1' ? true : false;

	isAllowedEditMeasurementDataCellForTrials(needToSaveFirst);

	if (needToSaveFirst) {
		showAlertMessage('', $.fieldbookMessages.measurementWarningNeedGenExpDesign);
	}
	return !needToSaveFirst;
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
			}).hover(function() {
				$('.popover').hide();
				$(this).popover('show');
			}, function() {
				$(this).popover('hide');
			});
			$('.popover').hide();
			$(nodeSpan).find('a.dynatree-title').popover('show');
		}
	});
}

function truncateStudyVariableNames(domSelector, charLimit) {
	'use strict';
	$(domSelector).each(function() {
		var htmlString = $(this).html();
		if ($(this).data('truncate-limit') !== undefined) {
			charLimit = parseInt($(this).data('truncate-limit'), 10);
		}

		if (htmlString.length > charLimit) {
			if (!$(this).hasClass('variable-tooltip')) {
				$(this).addClass('variable-tooltip');
				$(this).attr('title', htmlString);

				if ($(this).data('truncate-placement') !== undefined) {
					$(this).data('placement', $(this).data('truncate-placement'));
				}

				htmlString = htmlString.substring(0, charLimit) + '...';

			}
			$(this).html(htmlString);
		}

	});
	$('.variable-tooltip').each(function() {
		$(this).data('toggle', 'tooltip');
		if ($(this).data('placement') === undefined) {
			$(this).data('placement', 'right');
		}
		$(this).data('container', 'body');
		$(this).tooltip();
	});
}

function checkTraitsAndSelectionVariateTable(containerDiv, isLandingPage) {
	'use strict';
	if ($(containerDiv + ' .selection-variate-table tbody tr').length > 0) {
		$(containerDiv + ' .selection-variate-table').removeClass('fbk-hide');
	} else {
		$(containerDiv + ' .selection-variate-table').addClass('fbk-hide');
		if (isLandingPage) {
			$(containerDiv + ' .selection-variate-table').parent().prev().addClass('fbk-hide');
		}
	}
	if ($(containerDiv + ' .traits-table tbody tr').length > 0) {
		$(containerDiv + ' .traits-table').removeClass('fbk-hide');
	} else {
		$(containerDiv + ' .traits-table ').addClass('fbk-hide');
		if (isLandingPage) {
			$(containerDiv + ' .traits-table').parent().prev().addClass('fbk-hide');
		}
	}
}

function isValidInput(input) {
	'use strict';
	var invalidInput = /[<>&=%;?]/.test(input);
	return !invalidInput;
}

function doDeleteStudy(id, callback) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/StudyManager/deleteStudy/' + id,
		type: 'POST',
		cache: false,
		success: function(data) {
			callback(data);
		}
	});
}
function changeBrowseNurseryButtonBehavior(isEnable) {
	'use strict';
	if (isEnable) {
		$('.browse-study-action').removeClass('disable-image');
	} else {
		$('.browse-study-action').addClass('disable-image');
	}
}
function changeBrowseGermplasmButtonBehavior(isEnable) {
	'use strict';
	if (isEnable) {
		$('.browse-germplasm-action').removeClass('disable-image');
	} else {
		$('.browse-germplasm-action').addClass('disable-image');
	}
}

function changeBrowseSampleButtonBehavior(isEnable) {
	'use strict';
	if (isEnable) {
		$('.browse-sample-action').removeClass('disable-image');
	} else {
		$('.browse-sample-action').addClass('disable-image');
	}
}
function showManageCheckTypePopup() {
	'use strict';
	$('#page-check-message-modal').html('');
	$('.check-germplasm-list-items .popover').remove();
	resetButtonsAndFields();
	$('#manageCheckTypesModal').modal({
		backdrop: 'static',
		keyboard: false
	});
}
function showExportGermplasmListPopup() {
	'use strict';
	$('.check-germplasm-list-items .popover').remove();
	$('#exportGermplasmListModal').modal({
		backdrop: 'static',
		keyboard: false
	});
	var visibleColumnTermIds = [];
	$('#imported-germplasm-list th[aria-label!=\'\']').each(
		function() {
			var termId = $(this).attr('data-col-name').split('-')[0];
			if ($.inArray(termId, visibleColumnTermIds) === -1) {
				visibleColumnTermIds.push(termId);
			}
		}
	);
	if ($('#imported-germplasm-list').size() !== 0) {
		if ($.inArray(gidTermId + '', visibleColumnTermIds) === -1
			|| $.inArray(entryNoTermId + '', visibleColumnTermIds) === -1
			|| $.inArray(desigTermId + '', visibleColumnTermIds) === -1) {
			showAlertMessage('', requiredGermplasmColumnsMessage);
		}
	}
}
function addUpdateCheckType(operation) {
	'use strict';
	if (validateCheckFields()) {
		var $form = $('#manageCheckValue,#comboCheckCode');
		var serializedData = $form.serialize();
		$.ajax({
			url: '/Fieldbook/StudyManager/importGermplasmList/addUpdateCheckType/'
			+ operation,
			type: 'POST',
			data: serializedData,
			cache: false,
			success: function(data) {
				if (data.success == '1') {
					// reload dropdown
					reloadCheckTypeList(data.checkTypes, operation);
					showCheckTypeMessage(data.successMessage);
					$('#comboCheckCode').select2('data', [{id:'', text:'', description:'' }]);
					$('#comboCheckCode').select2('val', '');
					$('#manageCheckValue').val('');
					$('#manageCheckTypesModal').modal('hide');
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
				'id': item.id,
				'text': item.name,
				'description': item.description
			});
		});
	}

	if (!$('#comboCheckCode').select2('data')) {
		showInvalidInputMessage(codeRequiredError);
		return false;
	} else if ($('#manageCheckValue').val() === '') {
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
			&& item.id != $('#comboCheckCode').select2('data').id) {
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
		if ($('.check-germplasm-list-items tbody tr').length != 0 && selectedCheckListDataTable !== null && selectedCheckListDataTable.getDataTable() !== null) {
			var currentId = $('#comboCheckCode').select2('data').id;
			selectedCheckListDataTable.getDataTable().$('.check-hidden').each(function() {
				if ($(this).val() == currentId) {
					isFound = true;
				}
			});
		}
		if (isFound) {
			showCheckTypeErrorMessage(checkTypeCurrentlyUseError);
			return false;
		}

		var $form = $('#manageCheckValue,#comboCheckCode');
		var serializedData = $form.serialize();
		$
			.ajax({
				url: '/Fieldbook/StudyManager/importGermplasmList/deleteCheckType',
				type: 'POST',
				data: serializedData,
				cache: false,
				success: function(data) {
					if (data.success == '1') {
						reloadCheckTypeList(data.checkTypes, 3);
						showCheckTypeMessage(data.successMessage);
						resetButtonsAndFields();
						$('#comboCheckCode').select2('data', [{id:'', text:'', description:'' }]);
						$('#comboCheckCode').select2('val', '');
						$('#manageCheckValue').val('');
						$('#manageCheckTypesModal').modal('hide');
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
				'id': value.id,
				'text': value.name,
				'description': value.description
			});
		});
	}

	if (operation == 2) {
		// update
		selectedValue = getIdOfValue($('#manageCheckValue').val());
	}

	$('#manageCheckValue').val('');
	initializeCheckTypeSelect2(null, [], false, 0, 'comboCheckCode');
	initializeCheckTypeSelect2(null, checkTypesObj, false, selectedValue,
		'comboCheckCode');
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
		url: '/Fieldbook/StudyManager/importGermplasmList/getAllCheckTypes',
		type: 'GET',
		cache: false,
		data: '',
		success: function(data) {
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
				url: '/Fieldbook/StudyManager/importGermplasmList/getAllCheckTypes',
				type: 'GET',
				cache: false,
				data: '',
				async: false,
				success: function(data) {
					checkTypes = $.parseJSON(data.allCheckTypes);
					suggestions = checkTypes;
				}
			});
	}

	if (suggestions != null) {
		$.each(suggestions, function(index, value) {
			if (comboName === 'comboCheckCode') {
				dataObj = {
					'id': value.id,
					'text': value.name,
					'description': value.description,
					'originalText': value.name
				};
			} else {
				dataObj = {
					'id': value.id,
					'text': value.description,
					'description': value.description,
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
					query: function(query) {
						var data = {
							results: sortByKey(suggestions_obj, 'text')
						};
						// return the array that matches
						data.results = $.grep(data.results, function(
							item, index) {
							if (item.text.toUpperCase().indexOf(query.term.toUpperCase()) === 0) {
								return true;
							}
							return false;
						});
						if (data.results.length === 0 || data.results[0].text.toUpperCase() != query.term.toUpperCase()) {
							data.results.unshift({
								id: query.term,
								text: query.term
							});
						}
						query.callback(data);
					},
					dropdownCssClass: 's2-nosearch-icon'
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

		if ($('.check-table-popover tbody tr').length != 0) {
			reloadCheckListTable();
			//we need to get the real index of the check
		}
	}
}

function hideClearChecksButton() {
	if ($('.check-germplasm-list-items tbody tr').length === 0
		|| ($('#studyId') != null && $('#chooseGermplasmAndChecks').data('replace') !== undefined
			&& parseInt($('#chooseGermplasmAndChecks').data('replace')) === 0)) {
		$('#check-germplasm-list-reset-button').hide();
	}
}
function reloadCheckListTable() {
	'use strict';
	refreshListDetails();
}

function refreshListDetails() {
	if(typeof dataGermplasmList !== 'undefined' && dataGermplasmList !== null) {
		$.ajax({
			url: '/Fieldbook/ListManager/GermplasmList/refreshListDetails',
			type: 'GET',
			cache: false,
			data: ''
		}).success(function(html) {
			$('#imported-germplasm-list').html(html);
			window.ImportGermplasm.initialize(dataGermplasmList);
			$('#entries-details').css('display', 'block');
			$('#numberOfEntries').html($('#totalGermplasms').val());
			$('#txtStartingEntryNo').prop('readOnly', false);
		});
	}
}

function openStudyTree(type, selectStudyFunction, isPreSelect) {
	'use strict';
	if (isPreSelect) {
		$('body').data('doAutoSave', '1');
	} else {
		$('body').data('doAutoSave', '0');
	}
	$('#page-study-tree-message-modal').html('');
	$('#addFolderDiv').hide();
	$('#renameFolderDiv').hide();
	if ($('#create-nursery #studyTree').length !== 0) {
		$('#studyTree').dynatree('destroy');
		displayStudyListTree('studyTree', type, selectStudyFunction, isPreSelect);
		changeBrowseNurseryButtonBehavior(false);
	} else if ($('#create-trial #studyTree').length !== 0) {
		$('#studyTree').dynatree('destroy');
		displayStudyListTree('studyTree', type, selectStudyFunction, isPreSelect);
		changeBrowseNurseryButtonBehavior(false);
	}

	$('#studyTreeModal').modal({
		backdrop: 'static',
		keyboard: true
	});
	$('#studyTreeModal').off('hide.bs.modal');
	$('#studyTreeModal').on('hide.bs.modal', function() {
		TreePersist.saveStudyTreeState(false, '#studyTree');
	});
	choosingType = type;

	$('.fbk-study-tree-title.trial').removeClass('fbk-hide');
	TreePersist.preLoadStudyTreeState('#studyTree');
}

function makeGermplasmListDraggable(isDraggable) {
	'use strict';
	// isDraggable is always false, analyze to refactor or remove this
	isDraggable = isDraggable
		&& (($('#chooseGermplasmAndChecks').data('replace') && parseInt($('#chooseGermplasmAndChecks').data('replace')) === 1
			|| ($('#studyId').length === 0 && false))
			|| $('#studyId').length > 0 && false && measurementRowCount === 0);
	if (isDraggable) {
		$('.germplasm-list-items tbody  tr').draggable({

			helper: function(/*event, ui*/) {
				var width = $(this)[0].offsetWidth,
					selected = $('.germplasm-list-items tr.germplasmSelectedRow'),
					container;

				if (selected.length === 0) {
					selected = $(this).addClass('germplasmSelectedRow');
				}

				container = $('<table style="width:' + width + 'px; background-color:green;" />').attr('id', 'draggingContainer');
				container.append(selected.clone().removeClass('germplasmSelectedRow'));

				return container;
			},

			revert: 'invalid',

			start: function(/*event, ui*/) {
				var selected = $('.germplasm-list-items tr.germplasmSelectedRow');
			},

			stop: function(/*event, ui*/) {
				var selected = $('.germplasm-list-items tr.germplasmSelectedRow');
				$(selected).css('opacity', '1');
			},

			zIndex: 9999,

			appendTo: '#chooseGermplasmAndChecks'
		});

		$('.germplasm-list-items tbody tr').off('click').on('click', function() {
			$(this).toggleClass('germplasmSelectedRow');
		});

	} else {
		if ($('.germplasm-list-items .ui-draggable').length !== 0) {
			$('.germplasm-list-items tbody  tr').draggable('destroy');
		}
		$('.germplasm-list-items tbody tr').off('click');
	}

	SaveAdvanceList.setSelectedEntries();
	// Change background of selected rows
	$('.germplasm-list-items tr.germplasmSelectedRow').removeClass('germplasmSelectedRow');
}

function isOpenStudy() {
	'use strict';
	var trialStatus = $('body').data('trialStatus');
	return (trialStatus && trialStatus === 'OPEN');
}

function addStudyTreeHighlight(node) {
	$(node.span).addClass('fbtree-focused');
}

function initializeStudyTabs() {
	'use strict';
	$('.nav-tabs').tabdrop({position: 'left'});
	$('.nav-tabs').tabdrop('layout');
	$('#study-tab-headers .fbk-close-tab').on('click', function() {
		var studyId = $(this).attr('id');
		$('li#li-study' + studyId).remove();
		$('.info#study' + studyId).remove();
		if ($('#study-tab-headers li').length > 1) {
			var studyIdString = $('#study-tab-headers li:eq(0)').attr('id');
			$('li#' + studyIdString + ' a').tab('show');
		}
		determineIfShowCloseAllStudyTabs();
		$('.nav-tabs').tabdrop('layout');
	});
	determineIfShowCloseAllStudyTabs();
}

function addDetailsTab(studyId, title) {//TODO MUESTRA ESTO EN EL SUMMARY PARA TRIAL
	// if the study is already existing, we show that tab
	'use strict';
	if ($('li#li-study' + studyId).length !== 0) {
		$('li#li-study' + studyId + ' a').tab('show');
	} else {
		$.ajax({
			url: '/Fieldbook/StudyManager/reviewStudyDetails/show/' + studyId,
			type: 'GET',
			cache: false,
			success: function (data) {
				var close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="' + studyId + '"></i>';
				$('#study-tab-headers').append(
					'<li id="li-study' + studyId + '"><a href="#study' + studyId + '" role="tab" data-toggle="tab"><span class="review-study-name">'
					+ title + '</span>' + close + '</a></li>');
				$('#study-tabs').append(
					'<div class="info tab-pane" id="study' + studyId + '">' + data + '</div>');
				if ($('#review-study-error-' + studyId).val() !== '') {
					createErrorNotification(errorMsgHeader, $('#review-study-error-' + studyId).val());
					$('#study-tab-headers li#li-study' + studyId).remove();
					$('#study-tabs div#study' + studyId).remove();
				} else {
					initializeStudyTabs();
					$('li#li-study' + studyId + ' a').tab('show');
					$('.info#study' + studyId + ' select').each(function () {
						$(this).select2({minimumResultsForSearch: 20});
					});
					truncateStudyVariableNames('#study' + studyId + ' .review-study-name', 20);
					reviewLandingSetup();
				}
			}
		});
	}
	determineIfShowCloseAllStudyTabs();
	// if not we get the info
}

function determineIfShowCloseAllStudyTabs() {
	'use strict';
	if ($('#study-tab-headers li').length > 1) {
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
	if ($('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option').length > 1) {
		return;
	}
	$.ajax({
		url: '/Fieldbook/StudyManager/reviewStudyDetails/datasets/'
		+ getCurrentStudyIdInTab(),
		type: 'GET',
		cache: false,
		success: function(data) {
			var i = 0;
			for (i = 0; i < data.length; i++) {
				optionTag.append(new Option(data[i].name, data[i].id));
			}
			$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection').val('');
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus,
				errorThrown);
		}
	});
}

function getCurrentStudyIdInTab() {
	'use strict';
	if ($('#study-tab-headers .tabdrop').hasClass('active')) {
		//means the active is in the tab drop
		return $('#study-tab-headers .tabdrop li.active .fbk-close-tab').attr('id');
	} else {
		return $('#study-tab-headers li.active .fbk-close-tab').attr('id');
	}
}

function loadDatasetMeasurementRowsViewOnly(datasetId, datasetName) {
	'use strict';
	var currentStudyId = getCurrentStudyIdInTab();
	if (datasetId == 'Please Choose' || $('#' + getJquerySafeId('dset-tab-') + datasetId).length !== 0) {
		return;
	}
	$.ajax({
		url: '/Fieldbook/trial/measurements/viewStudyAjax/' + datasetId,
		type: 'GET',
		cache: false,
		success: function(html) {
			var close = '<i class="glyphicon glyphicon-remove fbk-close-dataset-tab" id="' + datasetId + '"></i>';
			$('#study' + currentStudyId + ' #measurement-tab-headers').append(
				'<li class="active" id="dataset-li' + datasetId + '"><a><span class="review-dataset-name">'
				+ datasetName + '</span>' + close + '</a> ' + '</li>');
			$('#study' + currentStudyId + ' #measurement-tabs').append(
				'<div class="review-info" id="dset-tab-' + datasetId + '">' + html + '</div>');
			$('#study' + currentStudyId + ' .measurement-section').show();
			truncateStudyVariableNames('#dataset-li' + datasetId + ' .review-dataset-name', 40);
			initializeReviewDatasetTabs(datasetId);
		}
	});
}

function showSelectedTab(selectedTabName) {
	'use strict';
	$('#ontology-tab-headers').show();
	var tabs = $('#ontology-tabs').children();
	for (var i = 0; i < tabs.length; i++) {
		if (tabs[i].id === selectedTabName) {
			$('#' + tabs[i].id + '-li').addClass('active');
			$('#' + tabs[i].id).show();
		} else {
			$('#' + tabs[i].id + '-li').removeClass('active');
			$('#' + tabs[i].id).hide();
		}
	}
}

function showSelectedTabNursery(selectedTabName) {//TODO REMOVE THIS OLD CODE
	'use strict';
	if ($('.import-study-data').data('data-import') === '1') {
		showAlertMessage('', importSaveDataWarningMessage);
		return;
	}

	if (stockListImportNotSaved) {
		showAlertMessage('', importSaveDataWarningMessage);
		e.preventDefault();
	}

	$('#create-nursery-tab-headers').show();
	var tabs = $('#create-nursery-tabs').children();
	for (var i = 0; i < tabs.length; i++) {
		if (tabs[i].id == selectedTabName) {
			$('#' + tabs[i].id + '-li').addClass('active');
			$('#' + tabs[i].id).show();
		} else {
			$('#' + tabs[i].id + '-li').removeClass('active');
			$('#' + tabs[i].id).hide();
		}
	}

	if (selectedTabName === 'trial-measurements') {
		var dataTable = $('#measurement-table').dataTable();
		if (dataTable.length !== 0) {
			dataTable.fnAdjustColumnSizing();
		}
	}

}

function showStudyInfo() {
	$('#folderBrowserModal').modal('show');
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

	$('#dataset-li' + datasetId + ' .fbk-close-dataset-tab').on('click', function() {
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

function displayEditFactorsAndGermplasmSection() {
	'use strict';
	if ($('#measurementDataExisting').length !== 0) {
		displayCorrespondingGermplasmSections();

		//enable/disable adding of factors if nursery has measurement data
		if ($('#measurementDataExisting').val() === 'true') {
			$('.chs-add-variable-factor').hide();
			$.each($('#plotLevelSettings tbody tr'), function(index, row) {
				$(row).find('.delete-icon').hide();
			});
		} else {
			$('.chs-add-variable-factor').show();
			$.each($('#plotLevelSettings tbody tr'), function(index, row) {
				$(row).find('.delete-icon').show();
			});
		}
	} else {
		displayCorrespondingGermplasmSections();
		if ($('#measurementDataExisting').val() === 'true') {
			$('.chs-add-variable-factor').hide();
		} else {
			$('.chs-add-variable-factor').show();
		}
	}
}

// Function to enable/disable & show/hide controls as per Clear list button's visibility
function toggleControlsForGermplasmListManagement(value) {
	'use strict';
	if (value) {
		$('#imported-germplasm-list-reset-button').show();
		$('#txtStartingEntryNo').prop('title', '');
		$('#txtStartingPlotNo').prop('title', '');
	} else {
		$('#imported-germplasm-list-reset-button').hide();
		$('#txtStartingEntryNo').prop('title', 'Click Modify List button to edit entry number');
	}

	$('#txtStartingEntryNo').prop('readOnly', !value);
	$('#txtStartingPlotNo').prop('readOnly', !value);
}

function showGermplasmDetailsSection() {
	'use strict';

	// If Advance List for Trial is already generated then user can not Clear / Modify List.
	if (isAdvanceListGeneratedForTrial) {
		showAlertMessage('', advanceListAlreadyGeneratedForTrialWarningMessage, 10000);
		return;
	}

	$('.observation-exists-notif').hide();
	$('.overwrite-germplasm-list').hide();
	$('.browse-import-link').show();
	if ($('.germplasm-list-items tbody tr').length > 0) {
		toggleControlsForGermplasmListManagement(true);
	}
	//flag to determine if existing measurements should be deleted
	$('#chooseGermplasmAndChecks').data('replace', '1');
}

function displayCorrespondingGermplasmSections() {
	'use strict';
	var hasData = $('#measurementDataExisting').val() === 'true' ? true : false;
	displayStudyGermplasmSection(hasData, measurementRowCount);
}

function hasMeasurementData() {
	'use strict';
	return angular.element('#mainApp').injector().get('TrialManagerDataService').trialMeasurement.hasMeasurement;
}

function displayStudyGermplasmSection(hasData, observationCount) {
	'use strict';
	if (hasData) {
		$('.overwrite-germplasm-list').hide();
		$('.observation-exists-notif').show();
		$('.browse-import-link').hide();
	} else if (observationCount > 0) {
		$('.observation-exists-notif').hide();
		$('.overwrite-germplasm-list').show();
		$('#imported-germplasm-list').show();
		$('.browse-import-link').hide();
	} else if (countGermplasms() > 0) {
		$('.observation-exists-notif').hide();
		$('.overwrite-germplasm-list').show();
		$('.browse-import-link').hide();
	} else {
		$('.observation-exists-notif').hide();
		$('.overwrite-germplasm-list').hide();
	}
}

function countGermplasms() {
	var totalGermplasms = parseInt($('#totalGermplasms').val());
	return totalGermplasms ? totalGermplasms : 0;
}

//TODO review checks functions
function disableCheckVariables(isDisabled) {
	$('#' + getJquerySafeId('checkVariables2.value')).select2('destroy');
	$('#' + getJquerySafeId('checkVariables2.value')).prop('disabled', isDisabled);
	$('#' + getJquerySafeId('checkVariables2.value')).select2();
	$('#specifyCheckSection').find('input,select').prop('disabled', isDisabled);
}

function displaySelectedCheckGermplasmDetails() {
	$.ajax({
		url: '/Fieldbook/StudyManager/importGermplasmList/displaySelectedCheckGermplasmDetails',
		type: 'GET',
		cache: false,
		async: false,
		success: function(html) {
			$('#check-germplasm-list').html(html);
			setSpinnerMaxValue();
			itemsIndexAdded = [];
			$('#check-details').removeClass('fbk-hide');
			//hide clear button, set list id used fror checks if from list, and set checksFromPrimary value based on checks
			$('#check-germplasm-list-reset-button').hide();
			lastDraggedChecksList = $('#lastDraggedChecksList').val();
			if (lastDraggedChecksList.toString() === '' || lastDraggedChecksList.toString() === '0') {
				checksFromPrimary = $('.check-germplasm-list-items tbody tr').length;
			} else {
				checksFromPrimary = 0;
			}
		}
	});
}

function displaySelectedGermplasmDetails() {
	var url = '/Fieldbook/StudyManager/importGermplasmList/displaySelectedGermplasmDetails';

	$.ajax({
		url: url,
		type: 'GET',
		data: '',
		cache: false,
		async: false,
		success: function(html) {
			$('#imported-germplasm-list').css('display', 'block');
			$('#imported-germplasm-list').html(html);
			window.ImportGermplasm.initialize(dataGermplasmList);
			if (parseInt($('#totalGermplasms').val()) !== 0) {
				$('#entries-details').css('display', 'block');
			}
			$('#numberOfEntries').html($('#totalGermplasms').val());
			$('#imported-germplasm-list-reset-button').css('opacity', '1');
			$(document).trigger('germplasmListUpdated');
			listId = $('#lastDraggedPrimaryList').val();
			if (listId === '') {
				$('.view-header').hide();
				// Hide Numbering section if germplasm list is not available
				$('#specify-numbering-section').hide();
			} else {
				$('.view-header').show();
			}
			toggleControlsForGermplasmListManagement(false);
		}
	});
}

function checkBeforeAdvanceExport() {
	'use strict';
	var checkedAdvancedLists = getExportCheckedAdvancedList(),
		counter = 0,
		additionalAdvanceExportParams = '';

	if (checkedAdvancedLists !== null && checkedAdvancedLists.length === 0) {
		showErrorMessage('', 'Please select at least 1 advance list');
		return false;
	}

	if (checkedAdvancedLists !== null && checkedAdvancedLists.length !== 0) {

		for (counter = 0 ; counter < checkedAdvancedLists.length ; counter++) {
			if (additionalAdvanceExportParams !== '') {
				additionalAdvanceExportParams += '|';
			}
			additionalAdvanceExportParams += checkedAdvancedLists[counter];
		}
	}
	exportAdvanceStudyList(additionalAdvanceExportParams);
}

function showExportAdvanceOptions() {
	'use strict';
	var studyId = $('#studyId').val();
	$.ajax({
		url: '/Fieldbook/ExportManager/retrieve/advanced/lists/' + studyId,
		type: 'GET',
		cache: false,
		success: function(data) {
			$('.export-advance-germplasm-list .advances-list').html(data);
			$('.export-advance-germplasm-list').removeClass('fbk-hide');
		}
	});
	$('#exportAdvancedType').select2('destroy');
	$('#exportAdvancedType').val('1');
	$('#exportAdvanceListModal select').select2({width: 'copy', minimumResultsForSearch: 20});
	$('#exportAdvanceListModal').modal({ backdrop: 'static', keyboard: true });

}
function showExportAdvanceResponse(responseText, statusText, xhr, $form) {
	'use strict';
	var resp = $.parseJSON(responseText);
	$('#exportAdvanceStudyDownloadForm #outputFilename').val(resp.outputFilename);
	$('#exportAdvanceStudyDownloadForm #filename').val(resp.filename);
	$('#exportAdvanceStudyDownloadForm #contentType').val(resp.contentType);
	$('#exportAdvanceStudyDownloadForm').submit();
	$('#exportAdvanceListModal').modal('hide');
}
function processInlineEditInput() {
	'use strict';

	var tableIdentifier = $('body').hasClass('import-preview-measurements') ? '#import-preview-measurement-table' :
		'#measurement-table';

	if ($('.inline-input').length !== 0) {
		var experimentId = $('.data-hidden-value-experimentId').val();
		var indexElem = $('.data-hidden-value-index').val();
		var phenotypeId = $('.data-hidden-value-phenotypeId').val();
		var termId = $('.data-hidden-value-term-id').val();
		var indexDataVal = '';
		var isNew = '0';
		if ($('.data-value').hasClass('variates-select')) {
			if ($('.data-value').select2('data')) {
				indexDataVal = $('.data-value').select2('data').id;
				isNew  = $('.data-value').select2('data').status;
			} else {
				indexDataVal = '';
			}
		} else if ($('.data-value').hasClass('numeric-value')) {
			var minVal = ($('.data-value').data('min-range'));
			var maxVal = ($('.data-value').data('max-range'));
			var cellText = $('.data-value').val();
			if ($.trim(cellText.toLowerCase()) == 'missing') {
				if (minVal && maxVal) {
					isNew = '1';
				} else {
					isNew = '0';
				}
				$('.data-value').val('missing');
			} else if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellText) ||
				parseFloat(cellText) > parseFloat(maxVal))) {
				isNew = '1';
			}
			indexDataVal =  $('.data-value').val();
		} else {
			indexDataVal =  $('.data-value').val();
		}

		var currentInlineEdit = {
			experimentId: experimentId,
			index: indexElem,
			phenotypeId: phenotypeId,
			termId: termId,
			value: indexDataVal,
			isNew: isNew
		};
		$(tableIdentifier).data('json-inline-edit-val', JSON.stringify(currentInlineEdit));
		if (isNew === '1') {
			$('#inlineEditConfirmationModal').modal({
				backdrop: 'static',
				keyboard: true
			});
			$(tableIdentifier).data('show-inline-edit', '0');
			return false;
		} else {
			saveInlineEdit(0, 0);
		}
	}
	return true;
}
function saveInlineEdit(isDiscard, invalidButKeep) {
	'use strict';

	var isImportPreviewMeasurementsView = $('body').hasClass('import-preview-measurements');
	var tableIdentifier = isImportPreviewMeasurementsView ? '#import-preview-measurement-table' : '#measurement-table';

	$.ajax({
		url: '/Fieldbook/trial/measurements/' +
		(isImportPreviewMeasurementsView ? 'updateByIndex' : 'update') +
		'/experiment/cell/data?isDiscard=' + isDiscard + '&invalidButKeep=' + invalidButKeep,
		type: 'POST',
		async: false,
		data:   $(tableIdentifier).data('json-inline-edit-val'),
		contentType: 'application/json',
		success: function(data) {
			var jsonData = $.parseJSON($(tableIdentifier).data('json-inline-edit-val'));
			if (isDiscard === 0 && jsonData.isNew === '1' && jsonData.value !== 'missing') {
				$('.inline-input').parent('td').addClass('accepted-value').removeClass('invalid-value');
				$('.inline-input').parent('td').data('is-accepted', '1');
			} else if (jsonData.isNew === '0') {
				$('.inline-input').parent('td').removeClass('accepted-value').removeClass('invalid-value');
				$('.inline-input').parent('td').data('is-accepted', '0');
			}
			if (data.success === '1') {
				$('.inline-input').parent('td').data('is-inline-edit', '0');

				var oTable = $(tableIdentifier).dataTable();
				oTable.fnUpdate(data.data, data.index, null, false); // Row
				oTable.fnAdjustColumnSizing();
				$('body').off('click');

				if (!isImportPreviewMeasurementsView) {
					var trialManagerDataService = angular.element('#mainApp').injector().get('TrialManagerDataService');
					trialManagerDataService.trialMeasurement.hasMeasurement = true;
					// .. so that generate design is disabled because input is instantly saved.
				}
			} else {
				$(tableIdentifier).data('show-inline-edit', '0');
				showErrorMessage('page-update-experiment-message-modal', data.errorMessage);
			}
		},
		error: function() {
			//TODO Localise the message
			showErrorMessage('', 'Could not update the measurement');
		}
	});
}

function hasOutOfBoundValuesAsync() {
	'use strict';

	return $.ajax({
		url: '/Fieldbook/Common/ReviewDetailsOutOfBounds/hasOutOfBoundValues',
		type: 'GET',
		cache: false
	});
}

function hasOutOfBoundValues() {
	'use strict';
	var _hasOutOfBound = false;

	$.ajax({
		url: '/Fieldbook/Common/ReviewDetailsOutOfBounds/hasOutOfBoundValues',
		type: 'GET',
		cache: false,
		async: false,
		success: function (hasOutOfBound) {
			_hasOutOfBound = hasOutOfBound;
		}
	});

	return _hasOutOfBound;
}

function hasMeasurementsInvalidValue() {
	'use strict';

	var isImportPreviewMeasurementsView = $('body').hasClass('import-preview-measurements');
	var tableIdentifier = isImportPreviewMeasurementsView ? '#import-preview-measurement-table' : '#measurement-table';

	if ($(tableIdentifier).find('.invalid-value').length === 0) {
		return false;
	}
	return true;
}

function reviewOutOfBoundsData() {
	'use strict';

	hasOutOfBoundValuesAsync().then(function (hasOutOfBound) {
		// Display the Review Out of Bound Data dialog if there are invalid values in the measurements table.
		if (hasOutOfBound) {
			$('#reviewOutOfBoundsDataModal').modal({ backdrop: 'static', keyboard: true });
		} else {
			showAlertMessage('', 'There are no more out of bounds data to review.', 5000);
		}
	}, function () {
		// TODO error message
	});
}

function displayDetailsOutOfBoundsData() {
	'use strict';

	removeDetailsOutOfBoundDataInSessionStorage();

	if ($('#reviewDetailsOutOfBoundsDataModalBody').length !== 0) {
		$.ajax({
			url: '/Fieldbook/Common/ReviewDetailsOutOfBounds/showDetails',
			type: 'GET',
			success: function(html) {
				$('#reviewOutOfBoundsDataModal').modal('hide');
				$('#reviewDetailsOutOfBoundsDataModalBody').html(html);
				$('#reviewDetailsOutOfBoundsDataModal').modal({
					backdrop: 'static',
					keyboard: true
				});
			}
		});
	}
}

function removeDetailsOutOfBoundDataInSessionStorage() {
	'use strict';
	if (sessionStorage) {
		for (var i in sessionStorage)
		{
			if (i.indexOf('reviewDetailsFormData') === 0) {
				sessionStorage.removeItem(i);
			}
		}
	}

}

function proceedToReviewOutOfBoundsDataAction() {
	var action = $('#review-out-of-bounds-data-action').select2('data').id;
	if (action === '0') {
		showErrorMessage('page-review-out-of-bounds-data-message-modal', reviewOutOfBoundsDataActionRequiredError);
	} else if (action === '1') {
		displayDetailsOutOfBoundsData();
	} else if (action === '2') {
		markAllCellAsAccepted();
	} else if (action === '3') {
		markAllCellAsMissing();
	}
}

function exportDesignTemplate() {
	$.ajax({
		url: '/Fieldbook/DesignTemplate/export',
		type: 'GET',
		cache: false,
		success: function(result) {
			if (result.isSuccess === 1) {
				$.fileDownload('/Fieldbook/crosses/download/file', {
					httpMethod: 'POST',
					data: result
				});
			} else {
				showErrorMessage('page-review-out-of-bounds-data-message-modal', result.errorMessage);
			}
		}
	});
}

function setSpinnerMaxValue() {
	'use strict';
	if ($('#' + getJquerySafeId('checkVariables0.value')).val() === null || $('#' + getJquerySafeId('checkVariables0.value')).val() === '') {
		$('#' + getJquerySafeId('checkVariables0.value')).val(1);
	}
}
function switchCategoricalView(showCategoricalDescriptionView) {
	'use strict';

	if (typeof showCategoricalDescriptionView === 'undefined') {
		showCategoricalDescriptionView = null;
	}

	$('.fbk-measurement-categorical-name').toggle();
	$('.fbk-measurement-categorical-desc').toggle();

	return $.get('/Fieldbook/trial/measurements/setCategoricalDisplayType', {showCategoricalDescriptionView: showCategoricalDescriptionView})
		.done(function(result) {
			window.isCategoricalDescriptionView = result;

			$('.fbk-toggle-categorical-display').text(result ? window.measurementObservationMessages.hideCategoricalDescription :
				window.measurementObservationMessages.showCategoricalDescription);

		});
}

function onMeasurementsInlineEditConfirmationEvent() {
	'use strict';
	return function(e) {
		if (parseInt($(this).data('inline-edit'), 10) === 1) {
			//keep the changes
			saveInlineEdit(0, 1);
		} else if (parseInt($(this).data('inline-edit'), 10) === 0) {
			//discard the changes
			saveInlineEdit(1, 0);
		}
		$('#inlineEditConfirmationModal').modal('hide');
	};
}

function ValidateValueCheckBoxFavorite(checkFavorite,data){

	if(checkFavorite === 'showFavoriteLocationInventory'){
		if(data.allSeedStorageFavoritesLocations.length !== 0){
			$('#' + checkFavorite).prop('checked', true);
		}
	}

	if(checkFavorite === 'importFavoriteMethod'){
		if(data.favoriteNonGenerativeMethods.length !== 0){
			$('#' + checkFavorite).prop('checked', true);
		}
	}

	if(checkFavorite === 'importFavoriteLocation'){
		if(data.allBreedingFavoritesLocations.length !== 0){
			$('#' + checkFavorite).prop('checked', true);
		}
	}
}

/**
 * The following contructor contains utility functions for escaping html content from a string
 * Logic is extracted from lodash 4.11.1 source: https://github.com/lodash/lodash/blob/master/dist/lodash.core.js
 * @constructor
 */
function EscapeUtilityConstructor() {
	/** Used to match HTML entities and HTML characters. */
	this.unescapedHtmlRegEx = /[&<>"'`]/g;
	this.hasUnescapedHtmlRegEx = RegExp(this.unescapedHtmlRegEx.source);
}

/**
 * Converts `value` to a string. An empty string is returned for `null`
 * and `undefined` values. The sign of `-0` is preserved.
 *
 * @param {*} value The value to process.
 * @returns {string} Returns the string.
 * @example
 *
 * toString(null);
 * // => ''
 *
 * toString(-0);
 * // => '-0'
 *
 * toString([1, 2, 3]);
 * // => '1,2,3'
 */

EscapeUtilityConstructor.prototype.toString = function(value) {
	if (typeof value == 'string') {
		return value;
	}
	return value == null ? '' : (value + '');
};

/**
 * Used to convert characters to HTML entities.
 *
 * @private
 * @param {string} chr The matched character to escape.
 * @returns {string} Returns the escaped character.
 */
EscapeUtilityConstructor.prototype.escape = function(string)
{
	var htmlEscapes = {
		'&': '&amp;',
		'<': '&lt;',
		'>': '&gt;',
		'"': '&quot;',
		"'": '&#39;',
		'`': '&#96;'
	};

	string = this.toString(string);

	return (string && this.hasUnescapedHtmlRegEx.test(string))
		? string.replace(this.unescapedHtmlRegEx, function(chr) {
			return htmlEscapes[chr];
		}) : string;
};

/* make a global instance of EscapeUtility usable to all Fieldbook modules */
var EscapeHTML = new EscapeUtilityConstructor();
