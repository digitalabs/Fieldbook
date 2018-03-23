/*global showErrorMessage, measurementRowCount, showAlertMessage, ImportCrosses, ImportDesign*/
var selectedTableIds = new Array();
//TODO CUENYAD REMOVE JS.
function submitEditWorkbook() {
	'use strict';

	var $form = $('#createNurseryForm, #createNurseryMainForm'),
			serializedData = $form.serialize();
	var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
	serializedData += '&columnOrders=' + encodeURIComponent(JSON.stringify(columnsOrder));

	//TODO add error handler to the ajax request
	$.ajax({
		url: '/Fieldbook/NurseryManager/editNursery',
		type: 'POST',
		data: serializedData,
		cache: false,
		success: function(data) {
			if (data.status === '-1') {
				showErrorMessage('page-message', data.errorMessage);
			} else if ($('#chooseGermplasmAndChecks').data('replace') !== undefined &&
					parseInt($('#chooseGermplasmAndChecks').data('replace')) === 1 || measurementRowCount === 0) {
				submitGermplasmAndCheck();
			} else if (data.status === '1') {
				recreateSessionVariables();
				//update the import-desigm change button
				$('#measurementDataExisting').val(data.hasMeasurementData);
				ImportDesign.hideChangeButton();
			}
			$('.import-study-data').data('data-import', '0');
			$('.fbk-discard-imported-data').addClass('fbk-hide');
			$('.fbk-discard-imported-stocklist-data').addClass('fbk-hide');
			stockListImportNotSaved = false;
		}
	});
}

function deleteMeasurementRows() {
	'use strict';

	//TODO add error handler to the ajax request
	$.ajax({
		url: '/Fieldbook/NurseryManager/editNursery/deleteMeasurementRows',
		type: 'POST',
		data: '',
		cache: false,
		success: function(data) {
			if (data.status === '-1') {
				showErrorMessage('page-message', data.errorMessage);

			} else {
				submitEditWorkbook();
			}
		}
	});
}

function submitEditForm() {
	'use strict';

	if (!processInlineEditInput()) {
		return false;
	}

	if (hasOutOfBoundValues()) {
		//we check if there is invalid value in the measurements
		showErrorMessage('', 'There are some measurements that have invalid value, please correct them before proceeding');
		return false;
	}
	if (validateCreateNursery()) {
		if (!validateStartEndDate('nurseryLevelSettings')) {
			moveToTopScreen();
			return false;
		}
		if ($('#chooseGermplasmAndChecks').data('replace') !== undefined &&
				parseInt($('#chooseGermplasmAndChecks').data('replace')) === 1 && measurementRowCount > 0) {
			deleteMeasurementRows();
		} else {
			if ($('.import-study-data').data('data-import') === '1') {
				doSaveImportedData();
			} else {
				submitEditWorkbook();
			}

		}

		if (document.enableActions !== undefined) {
			document.enableActions();
		}

	} else {
		moveToTopScreen();
	}
}

$(document).ready(function() {
	'use strict';

	if (measurementRowCount > 0) {
		$('#import-crosses').css('display', 'block');
	}

	if (createdCrossesListId !== null && createdCrossesListId.length > 0) {
		ImportCrosses.isFileCrossesImport = false;
		ImportCrosses.openBreedingModal();
	}

	$('#basicDetails.toggle-icon').click(function() {
		var expandedImg = $(this).find('.section-expanded'),
				collapsedImg = $(this).find('.section-collapsed'),
				className = $(this).data('target');

		expandedImg.toggle();
		collapsedImg.toggle();
		$(className).slideToggle();
	});

/*	// handler for the manualNamingSettings display
	$('input:radio[name=manualNamingSettings]').on('change', function() {
		if ($('input:radio[name=manualNamingSettings]:checked').val() === 'true') {
			$('#manualNamingSettingsPanel').removeClass('fbk-hide');
		} else {
			$('#manualNamingSettingsPanel').addClass('fbk-hide');
		}
	});*/

	if ($('#folderId').val() === '1') {
		$('#folderNameLabel').html(programNurseriesText);
	}

	displayEditFactorsAndGermplasmSection();

	if ($('.nursery-name').length > 0) {
		$('.nursery-name').tooltip();
	}
	truncateStudyVariableNames('.fbk-variable', 30);
	$('.nav-tabs').tabdrop({position: 'left'});
	$('.nav-tabs').tabdrop('layout');
	$('li#nursery-settings-li a').tab('show');
	$('a[data-toggle="tab"]').each(function() {
		if ($(this).hasClass('crossesList') || $(this).hasClass('advanceList') ) {
			StockIDFunctions.generateStockListTabIfNecessary($(this).data('list-id')).done(function() {
				// re compute the tab drop whenever a new tab is generated
				$('.nav-tabs').tabdrop({position: 'left'});
				$('.nav-tabs').tabdrop('layout');
			});
		}
	});
	$('a[data-toggle="tab"]').on('shown.bs.tab', function() {
		if ($('.import-study-data').data('data-import') === '1') {
			if ($('.import-study-data').data('measurement-show-already') !== '1') {
				showAlertMessage('', importSaveDataWarningMessage);
				$('.import-study-data').data('measurement-show-already', '1');
				$('li#nursery-measurements-li a').tab('show');
			}else {
				$('.import-study-data').data('measurement-show-already', '0');
			}
		} else if ($(this).hasClass('germplasm')) {
			if (germplasmDataTable != null) {
				germplasmDataTable.getDataTable().fnAdjustColumnSizing();
			}
			if (selectedCheckListDataTable != null) {
				selectedCheckListDataTable.getDataTable().fnAdjustColumnSizing();
			}
		} else if ($(this).hasClass('measurements')) {
			if ($('#measurement-table').length !== 0 && $('#measurement-table').dataTable()) {
				$('#measurement-table').dataTable().fnAdjustColumnSizing();
			}
		} else if ($(this).hasClass('stockList') && $(this).data('has-loaded') !== '1') {
			$(this).data('has-loaded', '1');
			StockIDFunctions.displayStockList($(this).data('list-id'));

		} else if ($(this).hasClass('crossesList') && $(this).data('has-loaded') !== '1') {
			$(this).data('has-loaded', '1');
			ImportCrosses.displayCrossesList($(this).data('list-id'), $(this).data('list-id'), '', true);

		} else if ($(this).hasClass('advanceList') && $(this).data('has-loaded') !== '1') {
			$(this).data('has-loaded', '1');
			displayAdvanceList($(this).data('list-id'), $(this).data('list-id'), '', true);

		}
	});
	$('a[data-toggle="tab"]').on('show.bs.tab', function(e) {
		if ($('.import-inventory').data('data-import') === '1') {
			showAlertMessage('', importSaveDataWarningMessage);
			e.preventDefault();
		}
		if (stockListImportNotSaved) {
			showAlertMessage('', importSaveDataWarningMessage);
			e.preventDefault();
		}
	});

	if ($('#experimentTypeId').val() !== '') {
		$('#nursery-experimental-design-li').show();
	} else {
		$('#nursery-experimental-design-li').hide();
	}

});
