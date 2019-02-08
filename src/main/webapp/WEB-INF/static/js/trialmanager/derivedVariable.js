/*global angular, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	var derivedVariableModule = angular.module('derived-variable', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	derivedVariableModule.factory('derivedVariableService', ['$http', '$q', function ($http, $q) {

		var derivedVariableService = {};

		derivedVariableService.getDependencies = function () {
			return $http.get('/Fieldbook/DerivedVariableController/derived-variable/dependencies');
		};
		derivedVariableService.hasMeasurementData = function (variableIds) {
			return $http.post('/Fieldbook/DerivedVariableController/derived-variable/dependencyVariableHasMeasurementData/',
				variableIds, {cache: false});
		};

		return derivedVariableService;

	}]);


	derivedVariableModule.factory('derivedVariableModalService', ['$uibModal', function ($uibModal) {

		var derivedVariableModalService = {};

		derivedVariableModalService.openDatasetOptionModal = function () {
			$uibModal.open({
				template: '<dataset-option-modal modal-title="modalTitle" message="message"' +
					'selected="selected" on-continue="next()"></dataset-option-modal>',
				controller: 'executeCalculatedVariableDatasetOptionCtrl',
				size: 'md'
			});
		};

		derivedVariableModalService.openExecuteCalculatedVariableModal = function (datasetId) {
			$uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/derivedVariable/executeCalculatedVariableModal.html',
				controller: "executeCalculatedVariableModalCtrl",
				size: 'md',
				resolve: {
					datasetId: function () {
						return datasetId;
					}
				},
				controllerAs: 'ctrl'
			});
		};

		derivedVariableModalService.confirmOverrideCalculatedVariableModal = function (datasetId, selectedVariable) {
			$uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/derivedVariable/confirmOverrideCalculatedVariableModal.html',
				controller: "confirmOverrideCalculatedVariableModalCtrl",
				size: 'md',
				resolve: {
					datasetId: function () {
						return datasetId;
					},
					selectedVariable: function() {
						return selectedVariable;
					}
				},
				controllerAs: 'ctrl'
			});
		};

		return derivedVariableModalService;

	}]);

	derivedVariableModule.controller('executeCalculatedVariableDatasetOptionCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'derivedVariableModalService',
		function ($scope, $uibModal, $uibModalInstance, studyContext, derivedVariableModalService) {

			$scope.modalTitle = 'Execute Calculations';
			$scope.message = 'Please choose the dataset where you would like to execute the calculation from:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};

			$scope.next = function () {
				derivedVariableModalService.openExecuteCalculatedVariableModal($scope.selected.datasetId);
				$uibModalInstance.close();
			};

		}]);

	derivedVariableModule.controller('executeCalculatedVariableModalCtrl',
		['$scope', '$http', '$uibModalInstance', 'datasetService', 'derivedVariableModalService', 'datasetId',
			function ($scope, $http, $uibModalInstance, datasetService, derivedVariableModalService, datasetId) {

				$scope.instances = [];
				$scope.selectedInstances = {};
				$scope.isEmptySelection = false;
				$scope.selected = { variable: undefined };

				$scope.init = function () {

					datasetService.getDataset(datasetId).then(function (dataset) {
						$scope.variableListView = buildVariableListView(dataset.variables);
						$scope.instances = dataset.instances;

					});

				};

				$scope.cancel = function () {
					$uibModalInstance.close();
				};

				$scope.proceedExecution = function () {

					$('.import-study-data').data('data-import', '1');
					$('body').addClass('import-preview-measurements');

					var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
					new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));
					$('.fbk-discard-imported-data').removeClass('fbk-hide');

					showSuccessfulMessage('', 'Calculated values for ' + $scope.selected.variable.name + ' were added successfully.');
				};

				$scope.execute = function () {
					var geoLocationIds = [];

					Object.keys($scope.selectedInstances).forEach(function (instanceDbId) {
						var isSelected = $scope.selectedInstances[instanceDbId];
						if (isSelected) {
							geoLocationIds.push(instanceDbId);
						}
					});

					var calculateData = {
						variableId: $scope.selected.variable.cvTermId
						, geoLocationIds: geoLocationIds
					};

					$http.post('/Fieldbook/DerivedVariableController/derived-variable/execute', JSON.stringify(calculateData))
						.then(function (response) {
							$uibModalInstance.close();
							if (response.data && response.data.inputMissingData) {
								showAlertMessage('', response.data.inputMissingData, 15000);
							}
							if (response.data && response.data.hasDataOverwrite) {
								derivedVariableModalService.confirmOverrideCalculatedVariableModal(datasetId, $scope.selected.variable);
							} else {
								$scope.proceedExecution();
							}
						}, function (response) {
							if (response.data.errorMessage) {
								showErrorMessage('', response.data.errorMessage);
							} else {
								showErrorMessage('', ajaxGenericErrorMsg);
							}
						});

				};

				function buildVariableListView(variables) {
					var variableListView = [];
					angular.forEach(variables, function (variable) {
						if (variable.formula) {
							variableListView.push({name: variable.name, cvTermId: variable.termId});//termId
						}
					});
					return variableListView;
				};


				$scope.init();

			}]);

	derivedVariableModule.controller('confirmOverrideCalculatedVariableModalCtrl', ['$scope', '$http', '$uibModalInstance', 'derivedVariableModalService', 'selectedVariable',
		function ($scope, $http, $uibModalInstance, derivedVariableModalService, selectedVariable) {

			$scope.goBack = function () {
				$http.get('/Fieldbook/ImportManager/revert/data')
					.then(function (response) {
						$scope.revertData();
						$uibModalInstance.close();
						derivedVariableModalService.openExecuteCalculatedVariableModal(1);
					}, function (response) {
						if (response.data.errorMessage) {
							showErrorMessage('', response.data.errorMessage);
						} else {
							showErrorMessage('', ajaxGenericErrorMsg);
						}
					});

			};

			$scope.revertData = function () {
				$('body').removeClass('import-preview-measurements');
				showSuccessfulMessage('', 'Discarded data successfully');

				if ($('#measurement-table').length !== 0 && $('#measurement-table').dataTable()) {
					$('#measurement-table').dataTable().fnAdjustColumnSizing();
				}
				$('#review-out-of-bounds-data-list').hide();
				$('.fbk-discard-imported-data').addClass('fbk-hide');
				$('.import-study-data').data('data-import', '0');
			};

			$scope.proceed = function () {

				$('.import-study-data').data('data-import', '1');
				$('body').addClass('import-preview-measurements');

				var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
				new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));
				$('.fbk-discard-imported-data').removeClass('fbk-hide');

				showSuccessfulMessage('', 'Calculated values for ' + selectedVariable.name + ' were added successfully.');

				$uibModalInstance.close();

			};

		}]);
})();
