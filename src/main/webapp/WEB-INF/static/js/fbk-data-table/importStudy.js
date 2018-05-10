/*globals displaySaveSuccessMessage, createTableSettingVariables, importNursery, Spinner, showErrorMessage*/
/*globals recreateMethodCombo,showErrorMessage,showImportResponse,recreateLocationCombo, isCategoricalDisplay*/
/*exported showImportResponse, importOptions, confirmDesignation, goBackToImport, doImportActionChange*/
/*exported importStudyBookHeader, importStudyHeader*/

var changeConfirmationDetails = [],
	currentConfirmationIndex = 0,
	importOptions = {
		dataType: 'text',
		success: showImportResponse // post-submit callback
	};

$(document).ready(function() {
	'use strict';
	$('.import-window select').select2({width: 'copy', minimumResultsForSearch: 20});
	$('#importType').on('change', function() {
		if ($(this).val() !== '0') {
			importNursery($(this).val());
		}
	});

	$('#importStudyModal').on('hide.bs.modal', function() {
		$('div.fileupload').parent().parent().removeClass('has-error');
		$('#importStudyModal .fileupload-exists').click();
	});

	$('#importStudyDesigConfirmationModal').on('hide.bs.modal', function() {
		$('input[type=checkbox][name=yesToAllDesig]').prop('checked', false);
	});

	//set to 1 to initialize only the location and method dropdowns in the import modal
	$('.hasCreateGermplasm').val(1);
	recreateMethodCombo('importFavoriteMethod');
	recreateLocationCombo('importFavoriteLocation');
	$('.hasCreateGermplasm').val(0);

	$('#importDate').each(function() {
		$(this).datepicker({'format': 'yyyy-mm-dd'}).on('change', function(e) {
			var curDate = $(this).val();
			try {
				var r = $.datepicker.parseDate('yy-mm-dd', curDate);
				$(this).datepicker('setDate', r);
			} catch (e) {
				$(this).datepicker('setDate', new Date());
			}
		}).on('changeDate', function(ev) {
			$(this).datepicker('hide');

		}).on('show', function() {
			$('input[type=checkbox][name=yesToAllDesig]').data('is-checked', $('input[type=checkbox][name=yesToAllDesig]').prop('checked'));
		}).on('hide', function() {
			setTimeout(function() {$('input[type=checkbox][name=yesToAllDesig]').prop('checked', $('input[type=checkbox][name=yesToAllDesig]').data('is-checked'));}, 5);
		});
	});
	$('#importType').change();

	$('.import-manage-location').on('click', function() {
		$('#importStudyDesigConfirmationModal').modal('hide');
		$('#importStudyDesigConfirmationModal').data('open', '1');
		setTimeout(openManageLocations, 500);
	});

	$('.import-manage-method').on('click', function() {
		$('#importStudyDesigConfirmationModal').modal('hide');
		$('#importStudyDesigConfirmationModal').data('open', '1');
		setTimeout(openManageMethods, 500);
	});
	$('#importStudyDesigConfirmationModal').data('open', '0');

	$(document).off('location-update');
	$(document).on('location-update', recreateLocationCombo);
});

function doSaveImportedData() {
	'use strict';
	var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
	var serializedData = 'columnOrders=' + encodeURIComponent(JSON.stringify(columnsOrder));
	return $.ajax({
		url: '/Fieldbook/ImportManager/import/save',
		type: 'POST',
		data: serializedData,
		async: true,
		success: function (html) {
			$('.import-study-data').data('data-import', '0');
			$('.import-study-data').data('measurement-show-already', '0');
			$('.fbk-discard-imported-data').addClass('fbk-hide');

			$('body').trigger({
				type: 'REFRESH_AFTER_IMPORT_SAVE'
			});

			displaySaveSuccessMessage('page-message', saveImportSuccessMessage);
			$.ajax({
				url: '/Fieldbook/ImportManager/import/preview',
				type: 'POST',
				success: function (html) {
					onMeasurementsObservationLoad(typeof isCategoricalDisplay !== 'undefined' ? isCategoricalDisplay : false);
				}
			});

		}
	});
}
function doMeasurementsReload(hasDataOverwrite) {
	'use strict';
	$('.import-study-data').data('data-import', '1');

	$('body').addClass('import-preview-measurements');
	var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
	new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));

	$('.fbk-discard-imported-data').removeClass('fbk-hide');
	if (hasDataOverwrite === '1') {
		showAlertMessage('', importSuccessOverwriteDataWarningToSaveMessage, 5000);
	} else {
		showSuccessfulMessage('', importSuccessReminderToSaveMessage);
	}

	$('#importStudyModal').modal('hide');
	$('#importStudyConfirmationModal').modal('hide');
	$('#importOverwriteConfirmation').modal('hide');
	return false;
}
function confirmStudyImport(hasDataOverwrite) {
	'use strict';
	if (changeConfirmationDetails.length !== 0) {
		showDesigConfirmation(hasDataOverwrite);
	} else {
		doMeasurementsReload(hasDataOverwrite);
	}
	$('.import-study-data').data('data-import', '1');
}

function showImportResponse(responseText) {
//reload the screen
	'use strict';
  	var resp = $.parseJSON(responseText);
  	changeConfirmationDetails = [];
  	currentConfirmationIndex = 0;
  	if (resp.changeDetails != null && resp.changeDetails.length !== 0) {
   		for (var index = 0; index < resp.changeDetails.length; index++) {
      	changeConfirmationDetails.push(resp.changeDetails[index]);
    	}

	}
	if (resp.isSuccess === 1) {
    	if (resp.conditionConstantsImportErrorMessage !== null && resp.conditionConstantsImportErrorMessage !== '') {
      	showAlertMessage('', resp.conditionConstantsImportErrorMessage);
    	}

    	$('#importStudyModal').modal('hide');
    	if (resp.deletedTraits !== ' ' || resp.addedTraits !== ' ') {
			showWarningTraitsImport(resp);
    	} else if (resp.plotsNotFound !== ' ') {
    		ShowWarningPlotIdNotFound(resp);
        } else {
			showWarningImport(resp);
    	}
	} else {
		showErrorMessage('page-import-study-message-modal', resp.error);
		$('#importStudyModal').modal('hide');
	}
}

 function ShowWarningPlotIdNotFound(resp) {
 	'use strict';
 	setTimeout(function () {
 		$('#importStudyConfirmationModal').modal({
 			backdrop: 'static',
 			keyboard: true
 		});
 	}, 300);

 	var warningMessage = '', errorIndex = 0;
 	//warningMessage = resp.confirmMessageTrais;
 	//warningMessage += '<p> </p>';
 	warningMessage += '<ul>';
 	warningMessage += '<p>' + resp.plotsNotFound + '</p>';
 	warningMessage += '</ul>';
 	//warningMessage += '<br />';

	$('#importStudyConfirmationModal .import-confirmation').html(warningMessage);
	$('#studyConfirmationButton').off('click');
	$('#studyConfirmationButton').on('click', function () {
		showWarningImport(resp);
	})
 }

function showWarningTraitsImport(resp) {
	'use strict';
	setTimeout(function() {
	$('#importStudyConfirmationModal').modal({
	      backdrop: 'static',
	      keyboard: true
	    });
	  }, 300);
	var warningTraitsMessage = '', errorIndex = 0;
	warningTraitsMessage = resp.confirmMessageTrais;

	warningTraitsMessage += '<p> </p>';
	warningTraitsMessage += '<ul>';

	if (resp.deletedTraits !== ' ') {
		warningTraitsMessage += '<li>';
		warningTraitsMessage += '<b>' + 'Missing traits: ' + '</b>';
		warningTraitsMessage += resp.deletedTraits + ' in the study are not present in the file.';
		warningTraitsMessage += '</li>';
		warningTraitsMessage += '<p>' + '</p>';
	}

	if (resp.addedTraits !== ' ') {
		warningTraitsMessage += '<li>';
		warningTraitsMessage += '<b>' + 'Additional traits: ' + '</b>';
		warningTraitsMessage += 'the file contains the trait(s) ' + resp.addedTraits + ', that are not present in the study. ';
		warningTraitsMessage += 'This data will be discarded. if you wish to import additional traits, please add them to the study before ';
		warningTraitsMessage += 'importing the data file.';
		warningTraitsMessage += '</li>';
	}

	warningTraitsMessage += '</ul>';
	warningTraitsMessage += '<br />';

	$('#importStudyConfirmationModal .import-confirmation').html(warningTraitsMessage);
	$('#studyConfirmationButton').off('click');
	$('#studyConfirmationButton').on('click', function() {
	    showWarningImport(resp);
	})

}

function showWarningImport(resp) {
	'use strict';
	if (resp.hasDataOverwrite === '1') {
		  setTimeout(function() {
		    $('#importStudyConfirmationModal').modal({
		      backdrop: 'static',
		      keyboard: true
		    });
		  }, 300);
		var confirmMessage = '', errorIndex = 0;

		confirmMessage = resp.message;
		confirmMessage += '<p> </p>';
		confirmMessage += resp.confirmMessage;
		$('#importStudyConfirmationModal .import-confirmation').html(confirmMessage);
		$('#studyConfirmationButton').off('click');
		$('#studyConfirmationButton').on('click', function() {
		    confirmStudyImport('0');
		})
	} else {
		confirmStudyImport('0');
	}
}

function applyChangeDetails() {
	'use strict';
	var data = JSON.stringify(changeConfirmationDetails);
	$.ajax({
		url: '/Fieldbook/ImportManager/apply/change/details',
		type: 'POST',
		data: 'data=' + data,
		success: function() {
			$('#importStudyDesigConfirmationModal').modal('hide');
			doMeasurementsReload('0');
		}
	});
}

function showDesignationConfirmationMessage() {
	'use strict';
	$('#import-action-type option:selected').prop('selected', false);
	$('#import-action-type option[value=0]').prop('selected', true);
	$('#import-action-type').change();
	$('input[type=checkbox][name=yesToAllDesig]').prop('checked', false);
	$('#matching-gid').empty();
	$('#matching-gid').change();
	$('#importStudyDesigConfirmationModal .import-desig-confirmation').html(changeConfirmationDetails[currentConfirmationIndex].message);
	if (changeConfirmationDetails[currentConfirmationIndex].matchingGids && changeConfirmationDetails[currentConfirmationIndex].matchingGids.length > 0) {
		for (var i = 0; i < changeConfirmationDetails[currentConfirmationIndex].matchingGids.length; i++) {
			$('#matching-gid').append(
					new Option(changeConfirmationDetails[currentConfirmationIndex].matchingGids[i],
							changeConfirmationDetails[currentConfirmationIndex].matchingGids[i]));
		}
		$('#matching-gid').change();

		$('#import-action-type option[value=3]').prop('disabled', false);
		if ($('#import-action-type').val() === '3') {
			$('.choose-gids').show();
			$('.no-choose-gids').hide();
		} else {
			$('.choose-gids').hide();
			$('.no-choose-gisd').show();
		}
	} else {
		$('#import-action-type option[value=3]').prop('disabled', true);
		$('.choose-gids').hide();
		$('.no-choose-gids').show();
	}
	//we do the reset here
	resetDesigConfirmationFields();
}

function showDesigConfirmation(hasDataOverwrite) {
	'use strict';
	if (hasDataOverwrite === '1') {
		showAlertMessage('', importSuccessOverwriteDataWarningToSaveMessage, 5000);
	}

	$('#importStudyModal').modal('hide');
	$('#importStudyConfirmationModal').modal('hide');
	$('#importOverwriteConfirmation').modal('hide');
	setTimeout(function() {$('#importStudyDesigConfirmationModal').modal({ backdrop: 'static', keyboard: true });}, 300);
	resetDesigConfirmationFields();
	showDesignationConfirmationMessage();
}

function confirmDesignation() {
	'use strict';
	var status = $('#import-action-type').val(),
		applyDetails = false,
		importDate = $('#importDate').val(),
		nameType = $('#nameType').select2('data').id,
		importLocationId = $('#importLocationId').select2('data') === null ? null : $('#importLocationId').select2('data').id,
		importMethodId = $('#importMethodId').select2('data') === null ? null : $('#importMethodId').select2('data').id;

	if (validateGermplasmInput(importDate, importLocationId, importMethodId)) {
		if ($('input[type=checkbox][name=yesToAllDesig]:checked').val() === '1') {
			for (var index = currentConfirmationIndex ; index < changeConfirmationDetails.length ; index++) {
				changeConfirmationDetails[index].status = status;
				changeConfirmationDetails[index].importDate = importDate;
				changeConfirmationDetails[index].nameType = nameType;
				changeConfirmationDetails[index].importLocationId = importLocationId;
				changeConfirmationDetails[index].importMethodId = importMethodId;
			}
			applyDetails = true;
		} else {
			changeConfirmationDetails[currentConfirmationIndex].status = status;
			changeConfirmationDetails[currentConfirmationIndex].importDate = importDate;
			changeConfirmationDetails[currentConfirmationIndex].nameType = nameType;
			changeConfirmationDetails[currentConfirmationIndex].importLocationId = importLocationId;
			changeConfirmationDetails[currentConfirmationIndex].importMethodId = importMethodId;
			changeConfirmationDetails[currentConfirmationIndex].selectedGid = $('#matching-gid').val();
			currentConfirmationIndex++;
			if (currentConfirmationIndex === changeConfirmationDetails.length) {
				//meaning we've gone through all the confirmation
				applyDetails = true;
			} else {
				showDesignationConfirmationMessage();
			}

		}
		if (applyDetails) {
			applyChangeDetails();
		}
	}
}

function saveStockListImport(){
	showSuccessfulMessage('', 'Imported data successfully saved.');
	$('.fbk-save-nursery').removeClass('fbk-hide');
	$('.fbk-save-stocklist').addClass('fbk-hide');
	$('.fbk-discard-imported-stocklist-data').addClass('fbk-hide');
	stockListImportNotSaved = false;
}

function revertStockListData(){
	'use strict';
	var revertUrl = '/Fieldbook/stock/revertStockListData/data';
	$.ajax({
		url: revertUrl,
		type: 'POST',
		data: '',
		cache: false,
		success: function(resp) {
			//TODO Localise message
			showSuccessfulMessage('', 'Imported data successfully discarded.');
			$('.fbk-save-nursery').removeClass('fbk-hide');
			$('.fbk-save-stocklist').addClass('fbk-hide');
			$('.fbk-discard-imported-stocklist-data').addClass('fbk-hide');
			$('#discardImportStockListDataConfirmation').modal('hide');
			$('.import-study-data').data('data-import', '0');
			var response = JSON.parse(resp);
			StockIDFunctions.displayStockList(response.stockListId);
			stockListImportNotSaved = false;
		}
	});
}
function revertData(showMessage) {
	'use strict';
	var revertUrl = '/Fieldbook/ImportManager/revert/data';
	$.ajax({
		url: revertUrl,
		type: 'GET',
		data: '',
		cache: false,
		async: false,
		success: function(html) {
			$('body').removeClass('import-preview-measurements');
			if (showMessage === true) {
				showSuccessfulMessage('', 'Discarded imported data successfully');
			}
			if ($('#measurement-table').length !== 0 && $('#measurement-table').dataTable()) {
            	$('#measurement-table').dataTable().fnAdjustColumnSizing();
            }
            $('#review-out-of-bounds-data-list').hide();
			$('.fbk-discard-imported-data').addClass('fbk-hide');
			$('#discardImportDataConfirmation').modal('hide');
			$('.import-study-data').data('data-import', '0');
		}
	});
}

function doImportActionChange() {
	'use strict';
	if ($('#import-action-type').val() === '3') {
		$('.choose-gids').show();
		$('.no-choose-gids').hide();
		$('.create-germplasm').hide();
		$('input[type=checkbox][name=yesToAllDesig]').prop('checked', false);
	} else if ($('#import-action-type').val() === '2') {
		$('.create-germplasm').show();
		$('.no-choose-gids').show();
		$('.choose-gids').hide();
	} else if ($('#import-action-type').val() === '1') {
		$('.create-germplasm').show();
		$('.add-desig-to-gid').hide();
		$('.no-choose-gids').show();
		$('.choose-gids').hide();
	} else {
		$('.choose-gids').hide();
		$('.create-germplasm').hide();
		$('.no-choose-gids').show();
		$('#matching-gid').empty();
		$('#matching-gid').change();
	}
}
