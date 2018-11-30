/*globals displaySaveSuccessMessage, createTableSettingVariables, importFormatType, Spinner, showErrorMessage*/
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
			importFormatType($(this).val());
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
			if (html.containsOutOfSyncValues == true) {
				showAlertMessage('', outOfSyncWarningMessage);
			}
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
    		ShowWarningObsUnitIdNotFound(resp);
        } else {
			showWarningImport(resp);
    	}
	} else {
		showErrorMessage('page-import-study-message-modal', resp.error);
		$('#importStudyModal').modal('hide');
	}
}

 function ShowWarningObsUnitIdNotFound(resp) {
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


(function () {
	'use strict';

	var importStudyModule = angular.module('import-study', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	importStudyModule.factory('importStudyModalService', ['$uibModal',
		function ($uibModal) {

			var importStudyModalService = {};

			importStudyModalService.openDatasetOptionModal = function () {
				$uibModal.open({
					template: '<dataset-option-modal title="title" message="message"' +
						'selected="selected" on-continue="showImportOptions()"></dataset-option-modal>',
					controller: 'importDatasetOptionCtrl',
					size: 'md'
				});
			};

			importStudyModalService.openImportStudyModal = function (datasetId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/importStudy/ImportStudyModal.html',
					controller: "importStudyCtrl",
					size: 'md',
					resolve: {
						datasetId: function () {
							return datasetId;
						}
					},
					controllerAs: 'ctrl'
				});
			};

			importStudyModalService.redirectToOldImportModal = function () {
				// Call the global function to show the old import study modal
				setTimeout(function () {
					showImportOptions();
				});
			};

			importStudyModalService.showAlertMessage = function (title, message) {
				// Call the global function to show alert message
				showAlertMessage(title, message);
			};

			importStudyModalService.showWarningMessage = function (header, title, warnings, question, confirmButtonLabel, cancelButtonLabel) {
				var modalInstance = $uibModal.open({
					animation: true,
					templateUrl: '/Fieldbook/static/angular-templates/warningModal.html',
					controller: function ($scope, $uibModalInstance) {
						$scope.header = header;
						$scope.title = title;
						$scope.warnings = warnings;
						$scope.question = question;
						$scope.confirmButtonLabel = confirmButtonLabel;
						$scope.cancelButtonLabel = cancelButtonLabel;

						$scope.confirm = function () {
							$uibModalInstance.close(true);
						};

						$scope.cancel = function () {
							$uibModalInstance.close(false);
						};
					}
				});
				return modalInstance;
			};

			return importStudyModalService;

		}]);

	importStudyModule.controller('importDatasetOptionCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'importStudyModalService',
		function ($scope, $uibModal, $uibModalInstance, studyContext, importStudyModalService) {

			$scope.title = 'Import measurements';
			$scope.message = 'Please choose the dataset you would like to import:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};

			$scope.showImportOptions = function () {

				if ($scope.measurementDatasetId === $scope.selected.datasetId) {
					importStudyModalService.redirectToOldImportModal();
				} else {
					importStudyModalService.openImportStudyModal($scope.selected.datasetId);
				}

			};

		}]);

	importStudyModule.controller('importStudyCtrl', ['datasetId', '$scope', '$rootScope', '$uibModalInstance', 'datasetService', 'importStudyModalService',
		'TrialManagerDataService',
		function (datasetId, $scope, $rootScope, $uibModalInstance, datasetService, importStudyModalService) {

			$scope.title = 'Import measurements';
			$scope.fileName = undefined;
			$scope.importData = undefined;
			var ctrl = this;

			ctrl.selectedImportFormatId = '1';

			$scope.importFormats = [{itemId: '1', name: 'CSV'}];


			$scope.backToDatasetOptionModal = function () {
				$uibModalInstance.close();
				importStudyModalService.openDatasetOptionModal();
			};

			$scope.clearSelectedFile = function () {
				$scope.file = undefined;
				$scope.fileName = undefined;
			};

			$scope.submitImport = function () {
				$scope.previewImportMeasurements();
			};

			$scope.previewImportMeasurements = function () {
				datasetService.importObservations(datasetId, $scope.importData, true).then(function (response) {
					$scope.importMeasurements();
				}, function (response) {
					if (response.status == 401) {
						bmsAuth.handleReAuthentication();
					} else if (response.status == 400) {
						showErrorMessage('', response.data.errors[0].message);
					} else if (response.status == 412) {
						ctrl.showConfirmModal(response.data.errors);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});
			};

			$scope.importMeasurements = function () {
				datasetService.importObservations(datasetId, $scope.importData, false).then(function (response) {
				}, function (response) {
					if (response.status == 401) {
						bmsAuth.handleReAuthentication();
					} else if (response.status == 400) {
						showErrorMessage('', response.data.errors[0].message);
					} else if (response.status == 412) {
						ctrl.showConfirmModal(response.data.errors);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});
			};

			$scope.close = function () {
				$uibModalInstance.close();
			};

			ctrl.showConfirmModal = function (warnings) {
				var warningMessages="";
				for (var i = 0; i < warnings.length; i++) {
					warningMessages += warnings[i].message;
				}

				var modalInstance = $rootScope.openConfirmModal(warningMessages, 'Proceed');
				modalInstance.result.then(function (shouldContinue) {
					if (shouldContinue) {
						$scope.importMeasurements();
					}
				});
			};


			ctrl.init = function () {
				$scope.file = undefined;

			};

			ctrl.init();

		}])
			.directive('importSheetJs', function () {
				return {
					//scope: true,
					link: function ($scope, $elm) {
						$elm.on('change', function (changeEvent) {
							$scope.$parent.fileName = changeEvent.target.files[0].name;
							$scope.$parent.file = changeEvent.target.files[0];
							$scope.importData = undefined;
							var reader = new FileReader();

							reader.onload = function(e) {
								/* read workbook */
								var bstr = e.target.result;
								var wb = XLSX.read(bstr, {type:'binary'});

								/* grab first sheet */
								var wsname = wb.SheetNames[0];
								var ws = wb.Sheets[wsname];

								/* grab first row and generate column headers */
								var aoa = XLSX.utils.sheet_to_json(ws, {header:1, raw:false});
								var cols = [];
								for(var i = 0; i < aoa[0].length; ++i) cols[i] = { field: aoa[0][i] };

								/* generate rest of the data */
								var data = [];
								for(var r = 1; r < aoa.length; ++r) {
									if (cols.length == aoa[r].length) {
										for (i = 0; i < aoa[r].length; ++i) {
											if (aoa[r][i] == null || aoa[r][i] == undefined) {
												aoa[r][[i]] = "";
											}

										}
									} else if (cols.length > aoa[r].length) {
										for (i = 0; i < aoa[r].length; ++i) {
											if (aoa[r][i] == null || aoa[r][i] == undefined) {
												aoa[r][[i]] = "";
											}
										}

										var count = cols.length - (cols.length - aoa[r].length);
										for (i = count; i < cols.length; i++) {
											aoa[r][[i]] = "";
										}
									}
								}

								/*for(var r = 1; r < aoa.length; ++r) {
									data[r-1] = {};
									if (cols.length == aoa[r].length) {
										for (i = 0; i < aoa[r].length; ++i) {
											if (aoa[r][i] == null || aoa[r][i] == undefined) {
												//data[r - 1][aoa[0][i]] = "";
												data[r - 1][[i]] = "";
											}
											else {
												//data[r - 1][aoa[0][i]] = aoa[r][i]
												data[r - 1][[i]] = aoa[r][i]
											}
										}
									} else if (cols.length > aoa[r].length) {
										for (i = 0; i < aoa[r].length; ++i) {
											if (aoa[r][i] == null || aoa[r][i] == undefined) {
												//data[r - 1][aoa[0][i]] = "";
												data[r - 1][[i]] = "";
											} else {
												//data[r - 1][aoa[0][i]] = aoa[r][i]
												data[r - 1][[i]] = aoa[r][i]
											}
										}

										var count = cols.length - (cols.length - aoa[r].length);
										for (i = count; i < cols.length; i++) {
											//data[r - 1][aoa[0][i]] = "";
											data[r - 1][[i]] = "";
										}
									}
								}*/

								/* update scope */
								//$scope.$apply(function () {
								$scope.$parent.importData = aoa;
								//});
							};
							$scope.importData = reader.readAsBinaryString(changeEvent.target.files[0]);
						});
					}
				};
			})
/*		.directive('importSheetJs', function () {
			return {
				scope: {
					filename: '=',
					importData: '='
				},
				link: function ($scope, $elm) {
					$elm.on('change', function (changeEvent) {
						$scope.filename = changeEvent.target.file[0].name;
						var reader = new FileReader();

						reader.onload = function (e) {
							/!* read workbook *!/
							var bstr = e.target.result;
							var workbook = XLSX.read(bstr, {type: 'binary'});

							/!* DO SOMETHING WITH workbook HERE *!/
						};

						reader.readAsBinaryString(changeEvent.target.file[0]);
					});
				}
			};
		})*/
/*		.directive('fileInput', ['$parse', function($parse) {
		return {
			restrict: 'A',
			link: function(scope, elm, attrs) {
				elm.bind('change', function() {
					$parse(attrs.fileInput).assign(scope, elm[0].file);
				});
			}
		}

})();
