$(function() {

	var newHash = '';

	$(window).bind('hashchange', function() {

		newHash = window.location.hash.substring(1);

		if (newHash) {
			Spinner.toggle();
			$.ajax({
				url: newHash,
				type: 'GET',
				success: function(html) {
					// We just paste the whole html
					$('.container .row').first().html(html);
					Spinner.toggle();
				}
			});
		}
	});

	$(window).trigger('hashchange');

	if (typeof convertToSelect2 === 'undefined' || convertToSelect2) {
		// Variable is undefined
		$('select').each(function() {
			$(this).select2({minimumResultsForSearch: 20});
		});
	}
});

function doAjaxMainSubmit(pageMessageDivId, successMessage, overrideAction) {

	Spinner.toggle();

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
			Spinner.toggle();
		}
	});
}

function showPage(paginationUrl, pageNum, sectionDiv) {
	'use strict';

	Spinner.toggle();

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

			$('#' + sectionDiv).empty().append(html);

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
			Spinner.toggle();
		}
	});
}

function showMultiTabPage(paginationUrl, pageNum, sectionDiv, sectionContainerId, paginationListIdentifier) {
	'use strict';
	Spinner.toggle();

	$.ajax(
		 { url: paginationUrl+pageNum+'?listIdentifier='+paginationListIdentifier,
		   type: 'GET',
		   data: '',
		   cache: false,
		   success: function(html) {
			   var paginationDiv = '#'+sectionContainerId + ' #' + sectionDiv;
			   $(paginationDiv + ':eq(0)').html('');
			   $(paginationDiv + ':eq(0)').html(html);

			   Spinner.toggle();
		   }
		 }
	   );
}

function showPostPage(paginationUrl,previewPageNum, pageNum, sectionDiv, formName) {
	var $form;
	if (formName.indexOf('#') > -1) {
		$form = $(formName);
	}
	else {
		$form = $('#'+formName);
	}

	var completeSectionDivName;
	if (sectionDiv.indexOf('#') > -1) {
		completeSectionDivName = sectionDiv;
	}
	else {
		completeSectionDivName = '#' + sectionDiv;
	}

	var serializedData = $form.serialize();

	Spinner.toggle();
	$.ajax(
		 { url: paginationUrl+pageNum+'/'+previewPageNum+'?r=' + (Math.random() * 999),
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
						$('tr.data-row#'+idVal).addClass('field-map-highlight');
					}
				 }
			 }

			 if (sectionDiv == 'check-germplasm-list') {
				 makeCheckDraggable(makeCheckDraggableBool);
			 }

			 Spinner.toggle();
		   }
		 }
	   );
}

function triggerFieldMapTableSelection(tableName) {
	$('#'+tableName+' tr.data-row').on('click', function() {
		if (tableName == 'studyFieldMapTree') {
			$(this).toggleClass('trialInstance');
			$(this).toggleClass('field-map-highlight');

		} else {
			$(this).toggleClass('field-map-highlight');
			var id = $(this).attr('id') + '';
			if ($(this).hasClass('field-map-highlight')) {
				selectedTableIds[id] = id;
			} else {
				selectedTableIds[id] = null;
			}
		}
	});
}

function createFieldMap(tableName) {
	if ($('#'+tableName+' .field-map-highlight').attr('id') != null || tableName == 'nursery-table') {
		var ids = [];
		// Get selected studies
		if ($('#createNurseryMainForm #studyId').length  === 1) {
			ids.push($('#createNurseryMainForm #studyId').val());
		}
		else if ($('#trial-table').length === 1) {
			for (var index in selectedTableIds) {
				var idVal = selectedTableIds[index];
				if (idVal != null) {
					ids.push(idVal);
				}
			}
		}
		else
			ids.push(getCurrentStudyIdInTab());
		var idList = ids.join(',');
		$('#page-message').html('');

		// Show pop up to select instances/dataset for field map creation
		showFieldMapPopUpCreate(tableName, idList);
	} else {
		$('#page-create-field-map-message').html('<div class="alert alert-danger">'+fieldMapStudyRequired+'</div>');
	}
}

// FIXME obsolete
function checkTrialOptions(id) {
	Spinner.toggle();
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/' + id,
		type: 'GET',
		data: '',
		cache: false,
		success: function(data) {
			if (data.nav == '0') {
				$('#manageTrialConfirmation').modal('show');
			}
			else if (data.nav == '1') {
				var fieldMapHref = $('#fieldmap-url').attr('href');
				location.href = fieldMapHref + '/' + id;
			}

			Spinner.toggle();
		}
	});
}

// FIXME obsolete
function createNurseryFieldmap(id) {
	Spinner.toggle();
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
			}
			else if (data.nav == '1') {
				var fieldMapHref = $('#fieldmap-url').attr('href');
				location.href = fieldMapHref + '/' + id;
			}
			Spinner.toggle();
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
	location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/nursery/'
		+ $('#fieldmapDatasetId').val() + '/' + $('#fieldmapGeolocationId').val();
}

function getJquerySafeId(fieldId) {
	return replaceall(fieldId, '.', '\\.');
}

function replaceall(str, replace, with_this) {
	var str_hasil ='';
	var temp;

	for (var i = 0; i < str.length; i++) { // not need to be equal. it causes the last change: undefined..
		if (str[i] == replace) {
			temp = with_this;
		} else {
			temp = str[i];
		}
		str_hasil += temp;
	}
	return str_hasil;
}

function isInt(value) {
	if ((undefined === value) || (null === value) || (value === '')) {
		return false;
	}
	return value % 1 === 0;
}

function selectTrialInstance(tableName) {
	if (tableName == 'trial-table') {
		Spinner.toggle();
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
				Spinner.toggle();
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
	createHeader(hasFieldMap);
	$.each(fieldMapInfoList, function(index, fieldMapInfo) {
		createRow(getPrefixName('study', fieldMapInfo.fieldbookId), '', fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, hasFieldMap);
		$.each(fieldMapInfo.datasets, function(index, value) {
			if (tableName == 'trial-table') {
				// Create trial study tree up to instance level
				createRow(getPrefixName('dataset', value.datasetId), getPrefixName('study', fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, hasFieldMap);
				$.each(value.trialInstances, function(index, childValue) {
					if ((hasFieldMap && childValue.hasFieldMap) || !hasFieldMap) {
						createRow(getPrefixName('trialInstance', childValue.geolocationId), getPrefixName('dataset', value.datasetId), childValue, childValue.geolocationId, hasFieldMap);
					}
				});
			} else {
				// If dataset has an instance, show up to the dataset level
				if (value.trialInstances.length > 0) {
					$.each(value.trialInstances, function(index, childValue) {
						createRowForNursery(getPrefixName('trialInstance', childValue.geolocationId),
								getPrefixName('study', fieldMapInfo.fieldbookId), childValue, childValue.geolocationId,
								hasFieldMap, value.datasetName, value.datasetId);
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
		return cat + 'n' + (parseInt(id)*-1);
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
			newRow = newRow + '<th style="width:45%">' + trialName+ '</th>' +
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
			newRow = newRow + '<th style="width:60%""></th>' +
			'<th style="width:40%">' + entryPlotLabel + '</th>';
		}
	}
	newRow = newRow + '</tr></thead>';
	$('#studyFieldMapTree').append(newRow+'<tbody></tbody>');
}

function createRowForNursery(id, parentClass, value, realId, withFieldMap, datasetName, datasetId) {
	var genClassName = 'treegrid-';
	var genParentClassName = '';
	var newRow = '';
	var newCell = '';
	if (parentClass !== '') {
		genParentClassName = 'treegrid-parent-' + parentClass;
	}

	// For create new fieldmap
	var hasFieldMap = value.hasFieldMap ? 'Yes' : 'No';
	var disabledString = value.hasFieldMap ? 'disabled' : '';

	newRow = '<tr class="data-row trialInstance '+ genClassName + id + ' ' + genParentClassName + '">';
	var checkBox = '<input '+disabledString+' class="checkInstance" type="checkbox" id="' + datasetId + '|' + realId + '" /> &nbsp;&nbsp;';
	newCell = '<td>' + checkBox + '&nbsp;' + datasetName + '</td><td>' + value.entryCount + '</td>';

	newCell = newCell + '<td class="hasFieldMap">' + hasFieldMap + '</td>';
	$('#studyFieldMapTree').append(newRow+newCell+'</tr>');
}

function createRow(id, parentClass, value, realId, withFieldMap) {
	var genClassName = 'treegrid-';
	var genParentClassName = '';
	var newRow = '';
	var newCell = '';
	if (parentClass !== '') {
		genParentClassName = 'treegrid-parent-' + parentClass;
	}

	if (id.indexOf('study') > -1 || id.indexOf('dataset') > -1) {
		// Study and dataset level
		newRow = '<tr id="' + realId + '" class="tr-expander '+ genClassName + id + ' ' + genParentClassName + '">';

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
			newRow = '<tr id="' + realId + '" class="data-row trialInstance '+ genClassName + id + ' ' + genParentClassName + '">';
			newCell = '<td>' + value.trialInstanceNo + '</td><td>' + value.entryCount + '</td>';
			if (trial) {
				newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
			}
		} else {
			// For create new fieldmap
			var hasFieldMap = value.hasFieldMap ? 'Yes' : 'No';
			var disabledString = value.hasFieldMap ? 'disabled' : '';

			newRow = '<tr class="data-row trialInstance '+ genClassName + id + ' ' + genParentClassName + '">';
			var checkBox = '<input '+disabledString+' class="checkInstance" type="checkbox" id="' + realId + '" /> &nbsp;&nbsp;';
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
	$('#page-message').html('<div class="alert alert-danger">' + message + '</div>');
}

function createLabelPrinting(tableName) {

	var count = 0;
	var idVal = null;

	if ($('#createNurseryMainForm #studyId').length === 1) {
		idVal = ($('#createNurseryMainForm #studyId').val());
		count++;
	} else if ($('#trial-table').length === 1) {
		for (var index in selectedTableIds) {
			var tempVal = selectedTableIds[index];
			if (tempVal != null) {
				idVal = tempVal;
				count++;
			}
		}
	} else {
		idVal = getCurrentStudyIdInTab();
		count++;
	}

	if(count !== 1){
		$('#page-create-field-map-message').html('<div class="alert alert-danger">' + createLabelErrorMsg + '</div>');
		return;
	}

	if (idVal !== null) {
		var labelPrintingHref = $('#label-printing-url').attr('href');
		var id = idVal;
		Spinner.toggle();
		location.href = labelPrintingHref + '/' + id;
		Spinner.toggle();

	} else {
		var type = 'Trial';
		if (tableName === 'nursery-table')
			type='Nursery';
		$('#page-create-field-map-message').html('<div class="alert alert-danger">' + createLabelErrorMsg + '</div>');
	}
}

function showFieldMap(tableName) {
	var count = 0;
	var idVal = null;
	for (var index in selectedTableIds) {
		var tempVal = selectedTableIds[index];
		if (tempVal != null) {
			idVal = tempVal;
			count++;
		}
	}

	if (idVal != null) {
		if (count > 1) {
			$('#page-create-field-map-message').html('<div class="alert alert-danger">' + fieldMapOneStudyErrorMsg + '</div>');
		} else {
			$('#page-message').html('');
			showFieldMapPopUp(tableName, idVal);
		}
	} else {
		$('#page-create-field-map-message').html('<div class="alert alert-danger">' + fieldMapStudyRequired + '</div>');
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
	Spinner.toggle();
	$.ajax({
		url: link + encodeURIComponent(ids),
		type: 'GET',
		data: '',
		success: function(data) {
			selectTrialInstanceCreate(tableName);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus , errorThrown);
		},
		complete: function() {
		   Spinner.toggle();
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
	Spinner.toggle();
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
				$('#page-create-field-map-message').html('<div class="alert alert-danger">' + noFieldMapExists + '</div>');
			}
			Spinner.toggle();
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
	if ($('#studyFieldMapTree .checkInstance:checked').attr('id')) {
		var selectedWithFieldMap = false;
		fieldmapIds = [];
		$('#studyFieldMapTree .checkInstance:checked').each(function() {
			var id = this.id;
			var datasetId;
			var studyId;
			if (id.indexOf('|') > -1) {
				datasetId = id.split('|')[0];
				id = id.split('|')[1];
				studyId = $(this).parent().parent().treegrid('getParentNode').attr('id');
			} else {
				datasetId = $(this).parent().parent().treegrid('getParentNode').attr('id');
				studyId = $(this).parent().parent().treegrid('getParentNode').treegrid('getParentNode').attr('id');
			}
			var hasFieldMap;
			// Get value hasfieldmap column
			if (trial) {
				hasFieldMap = $(this).parent().next().next().next().next().html();
			} else {
				hasFieldMap = $(this).parent().next().next().html();
			}

			// Build id list of selected trials instances
			fieldmapIds.push(studyId+'|'+datasetId+'|'+id);

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
		$(this).text(i+1);
		$(this).parent().parent().attr('id', i+1);
		i++;
	});
	styleDynamicTree('selectedTrials');
}

function styleDynamicTree(treeName) {
	var count = 0;
	if ($('#'+treeName) != null) {
		$('#'+treeName+' tr').each(function() {
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
	var count = 0;
	for (var index in selectedTableIds) {
		var tempVal = selectedTableIds[index];
		if (tempVal != null) {
			idVal = tempVal;
			count++;
		}
	}

	if (count != 1) {
		$('#page-create-field-map-message').html('<div class="alert alert-danger">' + openStudyError + '</div>');
		return;
	}

	Spinner.toggle();
	var openStudyHref = $('#open-study-url').attr('href');
	$.ajax({
		url: openStudyHref,
		type: 'GET',
		data: '',
		cache: false,
		success: function() {
			Spinner.toggle();
		}
	});
}

function openStudy(tableName) {

	var count = 0;

	idVal = getCurrentStudyIdInTab();
	count++;
	if(count != 1){
		$('#page-create-field-map-message').html('<div class="alert alert-danger">' + openStudyError + '</div>');
		return;
	}

	Spinner.toggle();
	var openStudyHref = $('#open-study-url').attr('href');

	if (idVal != null) {
		location.href = openStudyHref + '/' + idVal;
		Spinner.toggle();
	}
}

function advanceNursery(tableName) {
	var count = 0;

	idVal = $('#createNurseryMainForm #studyId').val();
	count++;
	if(count != 1){
		$('#page-create-field-map-message').html('<div class="alert alert-danger">' + advanceStudyError + '</div>');
		return;
	}

	Spinner.toggle();
	var advanceStudyHref = $('#advance-study-url').attr('href');

	if (tableName == 'nursery-table') {
		if (idVal != null) {

			$.ajax({
				url: advanceStudyHref + '/' + encodeURIComponent(idVal),
				type: 'GET',
				success: function(html) {
					$('#advance-nursery-modal-div').html(html);
					$('#advanceNurseryModal').modal('show');
					$('#advanceNurseryModal select').select2({minimumResultsForSearch: 20});
				},
				error: function(jqXHR, textStatus, errorThrown) {
					console.log('The following error occured: ' + textStatus , errorThrown);
				},
				complete: function() {
				   Spinner.toggle();
				}
			});
		}
	}
}

function showErrorMessage(messageDivId, message) {
	$('#' + messageDivId).html('<div class="alert alert-danger">' + message + '</div>');
}

function showSuccessfulMessage(messageDivId, message) {
	$('#' + messageDivId).html('<div class="alert alert-success">' + message + '</div>');
}

function hideErrorMessage() {
	$('#page-message .alert-danger').fadeOut(1000);
}

function initializeHarvestLocationSelect2(locationSuggestions, locationSuggestions_obj) {

	$.each(locationSuggestions, function(index, value) {
		locationSuggestions_obj.push({
			id: value.locid,
			text: value.lname,
			abbr: value.labbr
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('harvestLocationIdAll')).select2({
		minimumInputLength: 2,
		query: function(query) {
		  var data = {results: locationSuggestions_obj}, i, j, s;
		  // Return the array that matches
		  data.results = $.grep(data.results,function(item,index) {
			return ($.fn.select2.defaults.matcher(query.term,item.text));
		  });
			query.callback(data);
		}

	}).on('change', function() {
		$('#'+getJquerySafeId('harvestLocationId')).val($('#'+getJquerySafeId('harvestLocationIdAll')).select2('data').id);
		$('#'+getJquerySafeId('harvestLocationName')).val($('#'+getJquerySafeId('harvestLocationIdAll')).select2('data').text);
		$('#'+getJquerySafeId('harvestLocationAbbreviation')).val($('#'+getJquerySafeId('harvestLocationIdAll')).select2('data').abbr);
		if ($('#harvestloc-tooltip')) {
			$('#harvestloc-tooltip').attr('title', $('#'+getJquerySafeId('harvestLocationIdAll')).select2('data').abbr);
		}
	});
}

function initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj) {

	$.each(locationSuggestionsFav, function( index, value ) {
		locationSuggestionsFav_obj.push({
			id: value.locid,
			text: value.lname,
			abbr: value.labbr
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('harvestLocationIdFavorite')).select2({
		query: function(query) {
		  var data = {results: locationSuggestionsFav_obj}, i, j, s;
		  // Return the array that matches
		  data.results = $.grep(data.results,function(item,index) {
			return ($.fn.select2.defaults.matcher(query.term,item.text));
		  });
			query.callback(data);
		}

	}).on('change', function() {
		$('#'+getJquerySafeId('harvestLocationId')).val($('#'+getJquerySafeId('harvestLocationIdFavorite')).select2('data').id);
		$('#'+getJquerySafeId('harvestLocationName')).val($('#'+getJquerySafeId('harvestLocationIdFavorite')).select2('data').text);
		$('#'+getJquerySafeId('harvestLocationAbbreviation')).val($('#'+getJquerySafeId('harvestLocationIdFavorite')).select2('data').abbr);
		if ($('#harvestloc-tooltip')) {
			$('#harvestloc-tooltip').attr('title', $('#'+getJquerySafeId('harvestLocationIdFavorite')).select2('data').abbr);
		}
	});
}

function initializeMethodSelect2(methodSuggestions, methodSuggestions_obj) {

	$.each(methodSuggestions, function(index, value) {
		methodSuggestions_obj.push({
			id: value.mid,
			text: value.mname,
			tooltip: value.mdesc
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('methodIdAll')).select2({
		query: function(query) {
			var data = {results: methodSuggestions_obj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results,function(item,index) {
				return ($.fn.select2.defaults.matcher(query.term,item.text));
			});
			query.callback(data);
		}

	}).on('change', function() {
		if ($('#' + getJquerySafeId('advanceBreedingMethodId')).length !== 0) {
			$('#' + getJquerySafeId('advanceBreedingMethodId')).val($('#' + getJquerySafeId('methodIdAll')).select2('data').id);
			if ($('#method-tooltip')) {
				$('#method-tooltip').attr('title', $('#'+getJquerySafeId('methodIdAll')).select2('data').tooltip);
			}
			$('#'+getJquerySafeId('advanceBreedingMethodId')).trigger('change');
		}
	});
}

function initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFav_obj) {

	$.each(methodSuggestionsFav, function( index, value ) {
		methodSuggestionsFav_obj.push({ id: value.mid,
			  text: value.mname,
			  tooltip: value.mdesc
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#'+getJquerySafeId('methodIdFavorite')).select2({
		query: function(query) {
		  var data = {results: methodSuggestionsFav_obj}, i, j, s;
		  // Return the array that matches
		  data.results = $.grep(data.results,function(item,index) {
			return ($.fn.select2.defaults.matcher(query.term,item.text));
		  });
			query.callback(data);
		}

	}).on('change', function() {
		if ($('#'+getJquerySafeId('advanceBreedingMethodId')).length !== 0) {
			$('#'+getJquerySafeId('advanceBreedingMethodId')).val($('#'+getJquerySafeId('methodIdFavorite')).select2('data').id);
			if ($('#method-tooltip')) {
				$('#method-tooltip').attr('title', $('#'+getJquerySafeId('methodIdFavorite')).select2('data').tooltip);
			}
			$('#'+getJquerySafeId('advanceBreedingMethodId')).trigger('change');
		}
	});
}

function exportTrial(type) {
	$('#page-modal-choose-instance-message-r').html('');
	$('#page-modal-choose-instance-message').html('');
	$('.instanceNumber:first').click();
	var numberOfInstances = $('#numberOfInstances').val();
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
	}
	else {
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

function doExportTrial() {
	var exportTrialType = $('#exportTrialType').val();
	doExportContinue(exportTrialType, false);
}

function exportNursery() {
	var type = $('#exportType').val();
	if (type === '0') {
		showErrorMessage('page-export-message-modal', 'Please choose export type');
		return false;
	}

	if (type === '2') {
		exportNurseryToR(type);
	} else {
		doExportContinue(type, true);
	}
}

function exportNurseryToR(type) {
	var isNursery = true;
	if ($('#study-type').val() == 'Trial')
		isNursery = false;

	var additionalParams = '';
	if (!isNursery) {
		additionalParams = validateTrialInstance();
		if (additionalParams == 'false')
			return false;
	}
	doExportContinue(type + '/' + $('#selectedRTrait').val(), isNursery);
}

function validateTrialInstance() {
	var exportInstanceType = $('input:radio[name=exportInstanceType]:checked').val();
	var additionalParams = '';
	if (exportInstanceType == 1) {
		additionalParams = '0/0';
	} else if (exportInstanceType == 2) {
		additionalParams = $('#exportTrialInstanceNumber').val() + '/' + $('#exportTrialInstanceNumber').val();
	} else {
		var start =  $('#exportTrialInstanceStart').val();
		var end = $('#exportTrialInstanceEnd').val();
		additionalParams = start + '/' + end;
		var exportTrialType = $('#exportTrialType').val();
		if (parseInt(start) >= parseInt(end)) {
			var errorDiv = 'page-modal-choose-instance-message';
			if (exportTrialType == 2)
				errorDiv = 'page-modal-choose-instance-message-r';
			showErrorMessage(errorDiv, 'To trial instance # should be greater than the From Trial Instance #');
			additionalParams = 'false';
		}
	}
	return additionalParams;
}

function doExportContinue(paramUrl, isNursery) {
	var currentPage = $('#measurement-data-list-pagination .pagination .active a').html();

	var formname;
	if (isNursery) {
		formname = '#addVariableForm';
	}
	else {
		formname = '#addVariableForm, #addVariableForm2';
	}
	var $form = $(formname);
	var serializedData = $form.serialize();

	var additionalParams = '';
	if (!isNursery) {
		additionalParams = validateTrialInstance();
		if (additionalParams == 'false')
			return false;
		else {
			$('#trialModalSelection').modal('hide');
		}
	}
	var exportWayType = '/'+$('#exportWayType').val();
	if ($('#browser-nurseries').length !== 0) {
		studyId = getCurrentStudyIdInTab();
		doFinalExport(paramUrl, additionalParams,exportWayType, isNursery);
	} else {
		doFinalExport(paramUrl, additionalParams,exportWayType, isNursery);
	}
}

function doFinalExport(paramUrl, additionalParams, exportWayType, isNursery) {
	   var formName = '#exportStudyForm';
	   var action = submitExportUrl;
	   var newAction = '';
		if (isNursery)
			newAction = action + 'export/' + paramUrl;
		else {
			// Meaning its trial
			newAction = action + 'exportTrial/' + paramUrl + '/' + additionalParams;
		}
		newAction += exportWayType;

		var studyId = '0';
		if ($('#browser-nurseries').length !== 0) {
			// Meaning we are on the landing page
			studyId = getCurrentStudyIdInTab();
		}
		$('#exportStudyForm #studyExportId').val(studyId);
	   $(formName).attr('action', newAction);
	   Spinner.toggle();
	   $(formName).ajaxForm(exportOptions).submit();
	   $('#exportStudyForm #studyExportId').val('0');
}

function importNursery(type) {

	var action = '/Fieldbook/ImportManager/import/' + $('#study-type').val() + '/'+type;
	var formName = '#importStudyUploadForm';
	$(formName).attr('action', action);
}

function submitImportStudy() {
	if ($('#importType').val() == 0) {
		showErrorMessage('page-import-message-modal', 'Please choose import type');
		return false;
	}

	if ($('#fileupload').val() == '') {
		showErrorMessage('page-import-message-modal', 'Please choose a file to import');
		return false;
	}
	Spinner.toggle();
	$('#importStudyUploadForm').ajaxForm(importOptions).submit();
}

function isFloat(value) {
	return !isNaN(parseInt(value,10)) && (parseFloat(value,10) == parseInt(value,10));
}

function moveToTopScreen() {
	 $('html').scrollTop(0);
}

function openImportGermplasmList() {
	$('#listTreeModal').modal('hide');

	setTimeout(function() {
		$('#importFrame').attr('src', importLocationUrl);
		$('#importGermplasmModal').modal({ backdrop: 'static', keyboard: true });
		}, 500);
}

function doTreeHighlight(treeName, nodeKey) {
	$('#'+treeName).dynatree('getTree').activateKey(nodeKey);
	$('#'+treeName).find('*').removeClass('highlight');
	// Then we highlight the nodeKey and its parents
	var elem = nodeKey.split('_');
	var count = 0;
	var key = '';
	for (count = 0 ; count < elem.length ; count++) {
		if (key != '') {
			key = key + '_';
		}
		key = key + elem[count];
		$('.'+key).addClass('highlight');
	}
}

function addCreateNurseryRequiredAsterisk() {
	var requiredText = '<span class="required">*</span>';

	for (var i = 0; i < requiredFields.length; i++) {
		var cvTermId = requiredFields[i];
		if ($('.cvTermIds[value=""+cvTermId+""]').length !== 0) {
			$('.cvTermIds[value=""+cvTermId+""]').parent().parent().find('.nursery-level-label').parent().append(requiredText);
		}
	}
}

function addCreateTrialRequiredAsterisk() {
	var requiredText = '<span class="required">*</span>';

	for (var i = 0; i < requiredFields.length; i++) {
		var cvTermId = requiredFields[i];
		if ($('.cvTermIds[value=""+cvTermId+""]').length !== 0) {
			$('.cvTermIds[value=""+cvTermId+""]').parent().parent().find('.trial-level-label').parent().append(requiredText);
		}
	}
}

function getDateRowIndex(divName, dateCvTermId) {

	var rowIndex = -1;
	$('.'+divName+' .cvTermIds').each(function(index) {
		if ($(this).val() ==  parseInt(dateCvTermId))
			rowIndex = index;
		})
		return rowIndex;
}

function validateStartEndDate(divName) {
	//8050 - start
	var startDateIndex = getDateRowIndex(divName, startDateId);
	var endDateIndex = getDateRowIndex(divName, endDateId);
	var startDate = $('#' + getJquerySafeId('studyLevelVariables'+startDateIndex+'.value')).val();
	var endDate = $('#' + getJquerySafeId('studyLevelVariables'+endDateIndex+'.value')).val();
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
	var ids = '';
	var isMixed = false;
	var isBulk = false;

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

	var valid = true;
	if (ids !== '')	{
		Spinner.toggle();
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/countPlots/' + ids,
			type: 'GET',
			cache: false,
			async: false,
			success: function(data) {
				if (isMixed) {
					if (data == 0) {
						showErrorMessage('page-message', msgEmptyListError);
						valid = false;
					}
				} else if (isBulk) {
					var choice = !$('#plot-variates-section').is(':visible');
					if (choice == false && data == '0') {
						showErrorMessage('page-message', msgEmptyListError);
						valid = false;
					}
				} else {
					var choice = !$('#line-variates-section').is(':visible');
					var lineSameForAll = $('input[type=checkbox][name=lineChoice]:checked').val() == 1;
					if (lineSameForAll == false && choice == false && data == '0') {
						showErrorMessage('page-message', msgEmptyListError);
						valid = false;
					}
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			},
			complete: function() {
				Spinner.toggle();
			}
		});
	}
	if (valid && isMixed) {
		return validateBreedingMethod();
	}
	return valid;
}

function callAdvanceNursery() {
	var lines = $('#lineSelected').val();
	if (!lines.match(/^\s*(\+|-)?\d+\s*$/)) {
		showErrorMessage('page-message', linesNotWholeNumberError);
		return false;
	} else if (validatePlantsSelected()) {
		doAdvanceNursery();
	}
}

function doAdvanceNursery() {
	Spinner.toggle();
	$('input[type=checkbox][name=methodChoice]').prop('disabled', false);
	var serializedData = $('#advanceNurseryModalForm').serialize();

	$.ajax({
		url: '/Fieldbook/NurseryManager/advance/nursery',
		type: 'POST',
		data: serializedData,
		cache: false,
		success: function(html) {
			var listSize = $(html).find('.advance-list-size').text();
			if (listSize === '0') {
				showErrorMessage('page-message', listShouldNotBeEmptyError);
			} else {
				$('#advanceNurseryModal').modal('hide');
				$('#create-nursery-tab-headers li').removeClass('active');
				$('#create-nursery-tabs .info').hide();

				var uniqueId = $(html).find('.uniqueId').attr('id');
				var close = '<button style="float: right" onclick="javascript: closeAdvanceListTab(' + uniqueId +
					')" type="button" id="' + uniqueId + '" class="close">x</button>';
				var aHtml = '<a id="advanceHref'+uniqueId+'" href="javascript: showSelectedAdvanceTab('+uniqueId+')">Advance List'+close+'</a>';
				$('#create-nursery-tab-headers').append('<li class="active" id="advance-list'+uniqueId+'-li">'+aHtml+'</li>');
				$('#create-nursery-tabs').append('<div class="info" id="advance-list'+uniqueId+'">' + html + '</div>');
				showSelectedTab('advance-list'+uniqueId);
			}

		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus , errorThrown);
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}
function showSelectedAdvanceTab(uniqueId) {
	showSelectedTab('advance-list'+uniqueId);
}
function closeAdvanceListTab(uniqueId) {

	$('li#advance-list'+uniqueId+'-li').remove();
	$('.info#advance-list'+uniqueId).remove();
	setTimeout(function() {
	$('#create-nursery-tab-headers li').removeClass('active');
	$('#create-nursery-tabs .info').hide();
	$('#create-nursery-tab-headers li:eq(0)').addClass('active');
	$('#create-nursery-tabs .info:eq(0)').css('display', 'block');
	}, 100);

}

function displayAdvanceList(uniqueId, germplasmListId, listName) {
	$('#advanceHref'+uniqueId).append(': ['+listName+']');
	Spinner.toggle();
	$.ajax({
		url: '/Fieldbook/SeedStoreManager/advance/displayGermplasmDetails/'+germplasmListId,
		type: 'GET',
		cache: false,
		success: function(html) {
			$('#advance-list'+uniqueId).html(html);
			Spinner.toggle();
		}
	});
}

function validateBreedingMethod() {
	var id = $('#methodVariateId').val();

	var valid = true;
	if ($('input[type=checkbox][name=methodChoice]:checked').val() !== '1' && id) {
		Spinner.toggle();
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/countPlots/' + id,
			type: 'GET',
			cache: false,
			async: false,
			success: function(data) {
				if (data == 0) {
					showErrorMessage('page-message', noMethodValueError);
					valid = false;
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			},
			complete: function() {
				Spinner.toggle();
			}
		});
	}
	return valid;
}

function showBaselineTraitDetailsModal(id) {
	'use strict';
	if (id !== '') {
		Spinner.toggle();
		$.ajax({
			url: '/Fieldbook/NurseryManager/createNursery/showVariableDetails/' + id,
			type: 'GET',
			cache: false,
			success: function(data) {
				populateVariableDetails($.parseJSON(data));
				$('#variableDetailsModal').modal('toggle');
				Spinner.toggle();
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

function openManageLocations() {
	$('#manageLocationModal').modal({ backdrop: 'static', keyboard: true });
	if (locationIframeOpened == false) {
		locationIframeOpened = true;
		$('#locationFrame').attr('src', programLocationUrl + $('#projectId').val());
	}

}

function openManageMethods() {
	$('#manageMethodModal').modal({ backdrop: 'static', keyboard: true });
	if (methodIframeOpened == false) {
		methodIframeOpened = true;
		$('#methodFrame').attr('src', programMethodUrl + $('#projectId').val());
	}
}

function recreateMethodCombo() {
	var selectedMethodAll = $('#methodIdAll').val();
	var selectedMethodFavorite = $('#methodIdFavorite').val();

	Spinner.toggle();
	$.ajax(
		 { url: '/Fieldbook/NurseryManager/advance/nursery/getBreedingMethods',
		   type: 'GET',
		   cache: false,
		   data: '',
		   async: false,
		   success: function(data) {
			   if (data.success == '1') {
				   if (selectedMethodAll != null) {
					   //recreate the select2 combos to get updated list of methods
					   recreateMethodComboAfterClose('methodIdAll', $.parseJSON(data.allMethods));
					   recreateMethodComboAfterClose('methodIdFavorite', $.parseJSON(data.favoriteMethods));
					   showCorrectMethodCombo();
					   //set previously selected value of method
					   if ($('#showFavoriteMethod').prop('checked')) {
						   setComboValues(methodSuggestionsFav_obj, selectedMethodFavorite, 'methodIdFavorite');
					   } else {
						   setComboValues(methodSuggestions_obj, selectedMethodAll, 'methodIdAll');
					   }
				   } else {
					   var selectedVal = null;
					   //get index of breeding method row
					   var index = getBreedingMethodRowIndex();

					   if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data')) {
						   selectedVal = $('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data').id;
					   }
					   //recreate select2 of breeding method
					   initializePossibleValuesCombo([],
								'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);

					   //update values of combo
					   if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.favorite1')).is(':checked')) {
						   initializePossibleValuesCombo($.parseJSON(data.favoriteMethods),
									'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
					   } else {
						   initializePossibleValuesCombo($.parseJSON(data.allMethods),
									'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
					   }

					   replacePossibleJsonValues(data.favoriteMethods, data.allMethods, index);
				   }
			   } else {
				   showErrorMessage('page-message', data.errorMessage);
			   }
		   },
		   error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
		   },
		   complete: function() {
			   Spinner.toggle();
		   }
		 }
	 );
}


function recreateLocationComboAfterClose(comboName, data) {
	if (comboName == 'harvestLocationIdAll') {
		//clear all locations dropdown
		locationSuggestions = [];
		locationSuggestions_obj = [];
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestions_obj);
		//reload the data retrieved
		locationSuggestions = data;
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestions_obj);
	} else if (comboName == 'inventoryMethodIdAll') {
		//clear all locations dropdown
		initializePossibleValuesComboInventory(data, '#inventoryMethodIdAll', true, null);
	} else if (comboName == 'inventoryMethodIdFavorite') {
		initializePossibleValuesComboInventory(data, '#inventoryMethodIdFavorite', false, null);
	} else {
		//clear the favorite locations dropdown
		locationSuggestionsFav = [];
		locationSuggestionsFav_obj = [];
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);
		//reload the data
		locationSuggestionsFav = data;
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);
	}

}

function recreateMethodComboAfterClose(comboName, data) {
	if (comboName == 'methodIdAll') {
		//clear the all methods dropdown
		methodSuggestions = [];
		methodSuggestions_obj = [];
		initializeMethodSelect2(methodSuggestions, methodSuggestions_obj);
		//reload the data
		methodSuggestions = data;
		initializeMethodSelect2(methodSuggestions, methodSuggestions_obj);
	} else {
		//clear the favorite methods dropdown
		methodSuggestionsFav = [];
		methodSuggestionsFav_obj = [];
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFav_obj);
		//reload the data
		methodSuggestionsFav = data;
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFav_obj);
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
	var folderName = $.trim($('#addFolderName').val());
	if (folderName === '') {
		showErrorMessage('page-add-study-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else {
		//alert('Ajax Submit')
		var parentFolderId = $('#studyTree').dynatree('getTree').getActiveNode().data.key;
		if (parentFolderId === 'LOCAL')
			parentFolderId = 1;

		Spinner.toggle();
		$.ajax({
			url: '/Fieldbook/StudyTreeManager/addStudyFolder',
			type: 'POST',
			data: 'parentFolderId='+parentFolderId+'&folderName='+folderName,
			cache: false,
			success: function(data) {
				if (data.isSuccess == 1) {
					var node = $('#studyTree').dynatree('getTree').getActiveNode();
					doStudyLazyLoad(node);
					node.focus();
					node.expand();
					$('#addStudyFolder').modal('hide');

					} else {
						showErrorMessage('page-add-study-folder-message-modal', data.message);
					}
					Spinner.toggle();
				}
			});
	}
	return false;
}

function renameFolder(object) {
	'use strict';
	if (!$(object).hasClass('disable-image')) {

		$('#page-rename-study-folder-message-modal').html('');
		$('#renameStudyFolder').modal('show');
		var currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title
		$('#renameStudyFolder #heading-modal').html(renameFolderHeader + ' ' + currentFolderName);
		$('#newFolderName').val(currentFolderName);
	}
}

function submitRenameFolder() {
	'use strict';
	var folderName = $.trim($('#newFolderName').val());
	if ($.trim(folderName)  === $('#studyTree').dynatree('getTree').getActiveNode().data.title) {
		$('#renameStudyFolder').modal('hide');
		return false;
	}
	if (folderName === '') {
		showErrorMessage('page-rename-study-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else {
		//alert('Ajax Submit')
		var parentFolderId = $('#studyTree').dynatree('getTree').getActiveNode().data.key;
		if (parentFolderId === 'LOCAL')
			parentFolderId = 1;

		Spinner.toggle();
		$.ajax({
			url: '/Fieldbook/StudyTreeManager/renameStudyFolder',
			type: 'POST',
			data: 'folderId='+parentFolderId+'&newFolderName='+folderName,
			cache: false,
			success: function(data) {
				if (data.isSuccess === '1') {
					$('#renameStudyFolder').modal('hide');
					var node = $('#studyTree').dynatree('getTree').getActiveNode();
					node.data.title = folderName
					$(node.span).find('a').html(folderName);
					node.focus();
						//lazy load the node
						//doStudyLazyLoad($('#studyTree').dynatree('getTree').getActiveNode());
					} else {
						showErrorMessage('page-rename-study-folder-message-modal', data.message);
					}
					Spinner.toggle();
				}
			});
	}
}

function deleteFolder(object) {
	'use strict';
	if (!$(object).hasClass('disable-image')) {
		$('#deleteStudyFolder').modal('show');
		var currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title
		$('#delete-confirmation').html(deleteConfirmation + ' ' + currentFolderName + '?');
		$('#page-delete-study-folder-message-modal').html('');
	}
}

function submitDeleteFolder() {
	'use strict';
	var folderId = $('#studyTree').dynatree('getTree').getActiveNode().data.key;

	Spinner.toggle();
	$.ajax({
		url: '/Fieldbook/StudyTreeManager/deleteStudyFolder',
		type: 'POST',
		data: 'folderId='+folderId,
		cache: false,
		success: function(data) {
			if (data.isSuccess === '1') {
				$('#deleteStudyFolder').modal('hide');
				var node = $('#studyTree').dynatree('getTree').getActiveNode();
				if (node != null)
					node.remove();
				changeBrowseNurseryButtonBehavior(false);
			} else {
				showErrorMessage('page-delete-study-folder-message-modal', deleteFolderHasTrial);
			}
			Spinner.toggle();
		}
	});
}

function moveStudy(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key;
	var targetId = targetNode.data.key;
	var isStudy = sourceNode.data.isFolder === true ? 0 : 1;

	if (targetId === 'CENTRAL' || targetId > 0) {
		var title = $('#studyTree').dynatree('getTree').getNodeByKey('CENTRAL').data.title;
		showErrorMessage('page-study-tree-message-modal', 'Can not move to ' + title);
		return false;
	}

	if (targetId === 'LOCAL')
		targetId = 1;

	Spinner.toggle();
	$.ajax({
		url: '/Fieldbook/StudyTreeManager/moveStudyFolder',
		type: 'POST',
		data: 'sourceId='+sourceId+'&targetId='+targetId + '&isStudy='+isStudy,
		cache: false,
		success: function(data) {
			sourceNode.remove();
			var node = targetNode;
			doStudyLazyLoad(node);
			node.focus();
			Spinner.toggle();
		}
	});
}
function createGermplasmFolder() {
	'use strict';
	var folderName = $.trim($('#addGermplasmFolderName').val());
	if (folderName === '') {
		showErrorMessage('page-add-germplasm-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else {
		//alert('Ajax Submit')
		var parentFolderId = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.key;
		if (parentFolderId === 'LOCAL') {
			parentFolderId = 1;
		}

		Spinner.toggle();
		$.ajax({
			url: '/Fieldbook/ListTreeManager/addGermplasmFolder',
			type: 'POST',
			data: 'parentFolderId='+parentFolderId+'&folderName='+folderName,
			cache: false,
			success: function(data) {
				if (data.isSuccess === '1') {
					//lazy load the node
					var node = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode();
					doGermplasmLazyLoad(node);
					node.focus();
					node.expand();
					$('#addGermplasmFolder').modal('hide');
				} else {
					showErrorMessage('page-add-germplasm-folder-message-modal', data.message);
				}
				Spinner.toggle();
			}
		});
	}
	return false;
}

function renameGermplasmFolder(object) {
	'use strict';
	if (!$(object).hasClass('disable-image')) {

		$('#page-rename-germplasm-folder-message-modal').html('');
		$('#renameGermplasmFolder').modal('show');
		var currentFolderName = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title
		$('#renameGermplasmFolder #heading-modal').html(renameFolderHeader + ' ' + currentFolderName);
		$('#newGermplasmFolderName').val(currentFolderName);
	}
}

function submitRenameGermplasmFolder() {
	'use strict';
	var folderName = $.trim($('#newGermplasmFolderName').val());
	if ($.trim(folderName)  === $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title) {
		$('#renameGermplasmFolder').modal('hide');
		return false;
	}

	if (folderName === '') {
		showErrorMessage('page-rename-germplasm-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else {
		//alert('Ajax Submit')
		var parentFolderId = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.key;
		if (parentFolderId === 'LOCAL')
			parentFolderId = 1;

		Spinner.toggle();
		$.ajax({
			url: '/Fieldbook/ListTreeManager/renameGermplasmFolder',
			type: 'POST',
			data: 'folderId='+parentFolderId+'&newFolderName='+folderName,
			cache: false,
			success: function(data) {
				if (data.isSuccess === '1') {
					$('#renameGermplasmFolder').modal('hide');
					var node = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode();
					node.data.title = folderName;
					$(node.span).find('a').html(folderName);
					node.focus();
				} else {
					showErrorMessage('page-rename-germplasm-folder-message-modal', data.message);
				}
				Spinner.toggle();
			}
		});
	}
}

function deleteGermplasmFolder(object) {
	'use strict';
	if (!$(object).hasClass('disable-image')) {
		$('#deleteGermplasmFolder').modal('show');
		var currentFolderName = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title
		$('#delete-folder-confirmation').html(deleteConfirmation + ' ' + currentFolderName + '?');

		$('#page-delete-germplasm-folder-message-modal').html('');
	}
}

function submitDeleteGermplasmFolder() {
	'use strict';
	var folderId = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.key;

	Spinner.toggle();
	$.ajax({
		url: '/Fieldbook/ListTreeManager/deleteGermplasmFolder',
		type: 'POST',
		data: 'folderId='+folderId,
		cache: false,
		success: function(data) {
			if (data.isSuccess === '1') {
				$('#deleteGermplasmFolder').modal('hide');
				var node = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode();
				node.remove();
			} else {
				showErrorMessage('page-delete-germplasm-folder-message-modal', data.message);
			}
			Spinner.toggle();
		}
	});
}

function moveGermplasm(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key;
	var targetId = targetNode.data.key;

	if (targetId === 'CENTRAL' || targetId > 0) {
		var title = $('#'+getDisplayedTreeName()).dynatree('getTree').getNodeByKey('CENTRAL').data.title;
		showErrorMessage('page-import-message-modal', 'Can not move to ' + title);
		return false;
	}

	if (targetId === 'LOCAL')
		targetId = 1;

	Spinner.toggle();
	$.ajax({
		url: '/Fieldbook/ListTreeManager/moveGermplasmFolder',
		type: 'POST',
		data: 'sourceId='+sourceId+'&targetId='+targetId,
		cache: false,
		success: function(data) {
			sourceNode.remove();
			var node = targetNode;
			doGermplasmLazyLoad(node);
			node.focus();
			Spinner.toggle();
		}
	});
}
function closeModal(modalId) {
	'use strict';
	$('#'+modalId).modal('hide');
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
			$('#openGermplasmFrame').attr('src', germplasmDetailsUrl+gid);
			$('#openGermplasmModal .modal-title').html(headerMsg1 + ' ' + desig + ' (' + headerMsg2 + ' '+ gid +')');
			$('#openGermplasmModal').modal({ backdrop: 'static', keyboard: true });
		}
	});
}
function initializeMeasurementsDatatable(tableIdentifier, ajaxUrl) {
	'use strict';
	var columns = [];
	var columnsDef = [];
	$(tableIdentifier + ' thead tr th').each(function() {
		//console.log('here' + ($(this).data('term-id') == '8240'));
		columns.push({'data':$(this).html()});
		if ($(this).data('term-id') == '8240') {
			//for GID
			columnsDef.push({
				targets: columns.length - 1,
				data: $(this).html(),
				width: '100px',
				render: function (data, type, full, meta) {
				  return '<a class="gid-link" href="javascript: void(0)" onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;'+full.GID+'&quot;,&quot;'+full.DESIGNATION+'&quot;)">'+data+'</a>';
				 }
			});
		} else if ($(this).data('term-id') == '8250') {
			//for designation
			columnsDef.push({
				targets: columns.length - 1,
				data: $(this).html(),
				render: function (data, type, full, meta) {
					return '<a class="desig-link" href="javascript: void(0)" onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;'+full.GID+'&quot;,&quot;'+full.DESIGNATION+'&quot;)">'+data+'</a>';
				 }
			});
		} else if ($(this).data('term-id') == 'Action') {
			//for designation
			columnsDef.push({
				targets: columns.length - 1,
				data: $(this).html(),
				render: function (data, type, full, meta) {
					return '<a href="javascript: editExperiment(&quot;'+tableIdentifier+'&quot;,'+data+','+meta.row+')" class="fbk-edit-experiment"></a>';
				 }
			});
		}

	});
	var table =  $(tableIdentifier).DataTable({
		ajax: ajaxUrl,
		columns: columns,
		scrollY: '500px',
		scrollX: '100%',
		scrollCollapse: true,
		columnDefs: columnsDef,
		lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
		bAutoWidth: true,
		iDisplayLength: 100,
		fnRowCallback: function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			var toolTip = 'GID: ' + aData.GID + ' Designation: ' + aData.DESIGNATION;
			// assuming ID is in last column

			$(nRow).attr('id', aData.experimentId);
			$(nRow).attr('title', toolTip);
			return nRow;
		},
		fnInitComplete: function(oSettings, json) {
			$(tableIdentifier+ '_wrapper .dataTables_length select').select2({'minimumResultsForSearch' : 10});
			//there is a bug in datatable for now
			setTimeout(function() {$(tableIdentifier).dataTable().fnAdjustColumnSizing();}, 1000);
		},
		language: {
			search: '<span class="fbk-search-data-table">Search:</span>'
		},
		dom: 'R<<"row"<"col-md-6"l<"fbk-data-table-info"i>><"col-md-4"f><"col-md-2 fbk-colvis-btn">>r<t><"row col-md-12 fbk-data-table-paginate"p>>',
		// For column visibility
		colVis: {
			exclude: [ 0 ],
			restore: 'Restore',
			showAll: 'Show all'
		},
		// Problem with reordering plugin and fixed column for column re-ordering
		colReorder: {
			fixedColumns: 3
		}
	});
	$(tableIdentifier).dataTable().bind('sort', function() {
		$(tableIdentifier).dataTable().fnAdjustColumnSizing();
	});

	new $.fn.dataTable.FixedColumns( table, {iLeftColumns: 3});

	$('.measurement-show-hide-drop-down').appendTo('.fbk-colvis-btn');
	$('.measurement-dropdown-menu a').click(function(e) {
		e.stopPropagation();
		if ($(this).parent().hasClass('fbk-dropdown-select-fade')) {
			$(this).parent().removeClass('fbk-dropdown-select-fade');
			$(this).parent().addClass('fbk-dropdown-select-highlight');

		} else {
			$(this).parent().addClass('fbk-dropdown-select-fade');
			$(this).parent().removeClass('fbk-dropdown-select-highlight');
		}

	 // Get the column API object
		var column = table.column( $(this).attr('data-index') );
		console.log($(this).attr('data-index'));
		// Toggle the visibility
		column.visible( ! column.visible() );


	});

}
function editExperiment(tableIdentifier, expId, rowIndex) {
	//we show the ajax page here
	'use strict';
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
			url: '/Fieldbook/ListTreeManager/germplasm/list/header/details/'+node.data.key,
			type: 'GET',
			cache: false,
			success: function(data) {
				var listDetails = $('.list-details').clone();

				$(listDetails).find('#list-name').html(data.name);
				$(listDetails).find('#list-description').html(data.description);
				$(listDetails).find('#list-status').html(data.status);
				$(listDetails).find('#list-date').html(data.date);
				$(listDetails).find('#list-owner').html(data.owner);
				$(listDetails).find('#list-type').html(data.type);
				var notes = data.notes == null ? '-' : data.notes;
				$(listDetails).find('#list-notes').html(notes);

				$(nodeSpan).find('a.dynatree-title').popover({'html':true,
					'title': 'List Details',
				'content': $(listDetails).html(),
				'trigger': 'hover',
				'placement' : 'right'
				});
			}
		});
}