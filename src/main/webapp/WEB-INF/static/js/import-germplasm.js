/* global angular, germplasmDataTable, BMS, isNursery, setSpinnerMaxValue, noNurseryGermplasmList, noTrialGermplasmList */
/* global entryNoShouldBeInRange, plotNoShouldBeInRange, showAlertMessage, showInvalidInputMessage */
(function() {
	'use strict';

	if (typeof window.ImportGermplasm === 'undefined') {
		window.ImportGermplasm = {
			initialize: function(dataGermplasmList) {
				var $noGermplasmListIndicator = $('.noGermplasmListIndicator'),
					$txtStartingEntryNo = $('#txtStartingEntryNo'),
					$txtStartingPlotNo = $('#txtStartingPlotNo');

				var gpListItemsClass = '.germplasm-list-items',
					gpListDataTblClass = '.germplasm-list-data-table';
				/*if (isNursery()) {
					germplasmDataTable = new BMS.Fieldbook.GermplasmListDataTable(gpListItemsClass, gpListDataTblClass, dataGermplasmList);
					setSpinnerMaxValue();
					if ($noGermplasmListIndicator.length !== 0) {
						$noGermplasmListIndicator.html(noNurseryGermplasmList);
					}
				} else {*/
					germplasmDataTable = new BMS.Fieldbook.TrialGermplasmListDataTable(gpListItemsClass, gpListDataTblClass, dataGermplasmList);
					if ($noGermplasmListIndicator.length !== 0 && $('body').data('service.trialMeasurement.count') !== 0) {
						$noGermplasmListIndicator.html(noTrialGermplasmList);//TODO MUESTRA MENSAJE CUENYAD
					}
				//}

				// disable enter submit event on $('#txtStartingEntryNo') input box
				$txtStartingEntryNo.keypress(function(event) {
					if (event.keyCode === 13) {
						$txtStartingEntryNo.change();

						event.preventDefault();
					}
				});

				$txtStartingEntryNo.change(function() {
					window.ImportGermplasm.validateAndSetEntryNo();
				});

				if (!isNursery()) {
					window.ImportGermplasm.setStartingEntryNumberFirstTime();
				}else{

					$txtStartingPlotNo.keypress(function(event) {
						if (event.keyCode === 13) {
							$txtStartingPlotNo.change();

							event.preventDefault();
						}
					});

					$txtStartingPlotNo.change(function() {
						window.ImportGermplasm.validateAndSetPlotNo();
					});
				}

			},
			validateAndSetEntryNo: function () {
				var $txtStartingEntryNo = $('#txtStartingEntryNo');

				var customMessage = '';
				var entryNo = $.trim($txtStartingEntryNo.val());

				if (!window.ImportGermplasm.validateEntryNo(entryNo)) {
					customMessage = entryNoShouldBeInRange;
				}

				if (customMessage !== '') {
					showInvalidInputMessage(customMessage);
					$('#txtStartingEntryNo').val('1');
				} else {
					var dataTableIdentifier = '.germplasm-list-items';
					var entryNoColIndex = window.ImportGermplasm.findEntryColIndex(dataTableIdentifier);
					var lowestEntryNo = window.ImportGermplasm.findLowestEntryNo(dataTableIdentifier, entryNoColIndex);
					var diff = entryNo - lowestEntryNo;
					window.ImportGermplasm.updateEntryNo(dataTableIdentifier, entryNoColIndex, diff);
					if (isNursery()) {
						showAlertMessage('', 'These changes have not yet been applied to the Measurements table. ' +
							'To update the Measurements table, please save the Nursery', 10000);
					} else {
						window.ImportGermplasm.setUnappliedChangesAvailable();
						showAlertMessage('', 'These changes have not yet been applied to the Measurements table. ' +
							'To update the Measurements table, please review your settings and regenerate ' +
							'the Experimental Design', 10000);
					}
				}

			},
			findEntryColIndex: function(dataTableId) {
				var entryNoColNum = 0;
				var dataTable = $(dataTableId).dataTable();
				dataTable.api().columns().every(function(index) {
					var colHeader = dataTable.api().column(index).header();
					var colName = colHeader.attributes['data-col-name'].nodeValue;
					if (colName === '8230-key' || colName === 'entry') {
						entryNoColNum = colHeader.attributes['data-column-index'].nodeValue;
					}
				});
				return entryNoColNum;
			},
			findLowestEntryNo: function(dataTableId, entryNoColIndex) {
				var lowestEntryNo = 0;
				var dataTable = $(dataTableId).dataTable();
				var numberOfRows = dataTable.api().rows().data().length;
				var index;
				for (index = 0; index < numberOfRows; index++) {
					var cell = dataTable.api().cell(index, entryNoColIndex);
					var currentEntryNo = cell.data();
					if (lowestEntryNo === 0 || currentEntryNo < lowestEntryNo) {
						lowestEntryNo = parseInt(currentEntryNo);
					}
				}
				return lowestEntryNo;
			},
			updateEntryNo: function(dataTableId, entryNoColIndex, numToAddToEntryNo) {
				var dataTable = $(dataTableId).dataTable();
				var numberOfRows = dataTable.api().rows().data().length;
				var index;
				for (index = 0; index < numberOfRows; index++) {
					var cell = dataTable.api().cell(index, entryNoColIndex);
					var currentEntryNo = cell.data();
					var newEntryNo = parseInt(currentEntryNo) + numToAddToEntryNo;
					cell.data(newEntryNo);
				}
				dataTable.fnDraw();

				var startingEntryNo = $.trim($('#txtStartingEntryNo').val());

				// Update the entry numbers of the selected Germplasm List on the server side based on the specified
				// starting entry number. This is to make sure the entry numbers are the same in the UI and server.
				if (!isNursery()) {
					var trialManager = angular.element('#mainApp').injector().get('TrialManagerDataService');
					trialManager.updateStartingEntryNoCount(startingEntryNo);
				} else {
					if($.isNumeric(startingEntryNo)) {
						var data = new FormData().append('startingEntryNo', startingEntryNo);
						$.ajax({
							type : 'POST',
							url : '/Fieldbook/NurseryManager/GermplasmList/startingEntryNo',
							contentType: 'application/json; charset=utf-8',
							data: startingEntryNo
						});
					}
				}
			},
			validatePlotNo: function(inputNo) {
				var validNo = '^(?=.*[1-9].*)[0-9]{1,8}$';

				return !!inputNo.match(validNo);
			},
			validateEntryNo: function(inputNo) {
				var validNo = '^(?=.*[1-9].*)[0-9]{1,5}$';

				return !!inputNo.match(validNo);
			},
			validateAndSetPlotNo: function () {
				var customMessage = '';
				var plotNo = $.trim($('#txtStartingPlotNo').val());

				if (!window.ImportGermplasm.validatePlotNo(plotNo)) {
					customMessage = plotNoShouldBeInRange;
				}

				if (customMessage !== '') {
					showInvalidInputMessage(customMessage);
				} else {
					if (isNursery()) {
						showAlertMessage('', 'These changes have not yet been applied to the Measurements table. ' +
							'To update the Measurements table, please save the Nursery', 10000);
					} else {
						showAlertMessage('', 'These changes have not yet been applied to the Measurements table. ' +
							'To update the Measurements table, please save the Trial', 10000);
					}
				}

			},
			setUnappliedChangesAvailable: function() {
				var trialManager = angular.element('#mainApp').injector().get('TrialManagerDataService');
				trialManager.setUnappliedChangesAvailable();
				trialManager.updateStartingEntryNoCount($.trim($('#txtStartingEntryNo').val()));
			},
			setStartingEntryNumberFirstTime: function() {
				// Setting Starting Entry Number for first time while generating design
				var trialManager = angular.element('#mainApp').injector().get('TrialManagerDataService');
				trialManager.updateStartingEntryNoCount($.trim($('#txtStartingEntryNo').val()));
			}
		};
	}
})();
