/*global angular, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	var derivedVariableModule = angular.module('derived-variable', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	derivedVariableModule.factory('derivedVariableService', ['$http', '$q', 'studyContext', 'datasetService',
		'VARIABLE_TYPES', 'DATASET_TYPES_SUBOBSERVATION_IDS', 'DATASET_TYPES',
		function ($http, $q, studyContext, datasetService, VARIABLE_TYPES, DATASET_TYPES_SUBOBSERVATION_IDS, DATASET_TYPES) {

			var derivedVariableService = {};

			var FIELDBOOK_BASE_URL = '/Fieldbook/DerivedVariableController/';
			var BMSAPI_BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/studies/';

			derivedVariableService.isStudyHasCalculatedVariables = true;

			var successHandler = function (response) {

				if (response.data && response.data.inputMissingData) {
					showAlertMessage('', response.data.inputMissingData, 15000);
				}
				return response;

			};
			var failureHandler = function (response) {
				if (response.data.errorMessage) {
					showErrorMessage('', response.data.errorMessage);
				} else if (response.data.errors) {
					showErrorMessage('', response.data.errors[0].message);
				} else {
					showErrorMessage('', ajaxGenericErrorMsg);
				}
			};

			derivedVariableService.hasMeasurementData = function (variableIds) {
				return $http.post(FIELDBOOK_BASE_URL + 'derived-variable/dependencyVariableHasMeasurementData/',
					variableIds, {cache: false});
			};

			derivedVariableService.calculateVariableForObservation = function (calculateData) {
				var request = $http.post(FIELDBOOK_BASE_URL + 'derived-variable/execute', calculateData);
				return request.then(successHandler, failureHandler);
			};

			derivedVariableService.calculateVariableForSubObservation = function (datasetId, calculateData) {
				var request = $http.post(BMSAPI_BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/derived-variable/calculate', calculateData);
				return request.then(successHandler, failureHandler);
			};

			derivedVariableService.getDependencies = function (datasetId) {
				return $http.get(BMSAPI_BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/derived-variable/dependencies');
			};

			derivedVariableService.isAnyDatasetContainsCalculatedTraits = function (datasetIds) {
				var request = $http.get(BMSAPI_BASE_URL + studyContext.studyId + '/datasets/derived-variable/hasCalculatedTraits', {
					params: {
						datasetIds: datasetIds.join(",")
					}
				});
				return request.then(successHandler, failureHandler);
			};

			/**
			 * Displays the Execute Calculated Variable action menu if any of the datasets (plot and sub-observations) in the current study has calculated traits.
			 * If none of the datasets have calculated variables, Execute Calculated Variable action menu is hidden.
			 *
			 * This method is triggered when:
			 * 1. A study is opened and initialized.
			 * 2. Measurement table's trait section is changed (added/removed) and is saved.
			 * 3. SubObservation trait is changed (added/removed)
			 */
			derivedVariableService.displayExecuteCalculateVariableMenu = function () {

				datasetService.getDatasets(DATASET_TYPES_SUBOBSERVATION_IDS.concat(DATASET_TYPES.PLOT_OBSERVATIONS)).then(function (datasets) {
					var datasetIds = [];
					datasets.forEach(function (dataset) {
						datasetIds.push(dataset.datasetId)
					});
					derivedVariableService.isAnyDatasetContainsCalculatedTraits(datasetIds).then(function (response) {
						var hasCalculatedVariable = response.data;
						// isStudyHasCalculatedVariables variable is bound to HTML element via ngShow directive.
						derivedVariableService.isStudyHasCalculatedVariables = hasCalculatedVariable;
					});
				});

			}

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

		derivedVariableModalService.confirmOverrideCalculatedVariableModal = function (datasetId, selectedVariable, calculateRequestData) {
			$uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/derivedVariable/confirmOverrideCalculatedVariableModal.html',
				controller: "confirmOverrideCalculatedVariableModalCtrl",
				size: 'md',
				resolve: {
					datasetId: function () {
						return datasetId;
					},
					selectedVariable: function () {
						return selectedVariable;
					},
					calculateRequestData: calculateRequestData
				},
				controllerAs: 'ctrl'
			});
		};

		return derivedVariableModalService;

	}]);

	derivedVariableModule.controller('executeCalculatedVariableDatasetOptionCtrl', ['$rootScope', '$scope', '$uibModal', '$uibModalInstance',
		'studyContext', 'derivedVariableModalService', 'derivedVariableService',
		function ($rootScope, $scope, $uibModal, $uibModalInstance, studyContext, derivedVariableModalService, derivedVariableService) {

			$scope.modalTitle = 'Execute Calculations';
			$scope.message = 'Please choose the dataset where you would like to execute the calculation from:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};

			$scope.next = function () {

				// Do not continue with execute calculation process if no calculated traits is available for the selected dataset.
				derivedVariableService.isAnyDatasetContainsCalculatedTraits([$scope.selected.datasetId]).then(function (response) {
					var hasCalculatedVariable = response.data;
					if (hasCalculatedVariable) {
						if ($scope.selected.datasetId === $scope.measurementDatasetId) {
							$rootScope.navigateToTab('editMeasurements');
						} else {
							$rootScope.navigateToSubObsTab($scope.selected.datasetId);
						}
						derivedVariableModalService.openExecuteCalculatedVariableModal($scope.selected.datasetId);
						$uibModalInstance.close();
					} else {
						showErrorMessage('', 'There is no calculated variable for the dataset selected. Please add a calculated variable in the dataset and try again.');
					}
				});
			};

		}]);

	derivedVariableModule.controller('executeCalculatedVariableModalCtrl',
		['$rootScope', '$scope', '$http', '$uibModalInstance', 'datasetService', 'derivedVariableModalService', 'derivedVariableService', 'datasetId', 'studyContext',
			function ($rootScope, $scope, $http, $uibModalInstance, datasetService, derivedVariableModalService, derivedVariableService, datasetId, studyContext) {

				$scope.instances = [];
				$scope.selectedInstances = {};
				$scope.isEmptySelection = false;
				$scope.selected = {variable: undefined};

				$scope.init = function () {
					datasetService.getDataset(datasetId).then(function (dataset) {
						$scope.variableListView = buildVariableListView(dataset.variables);
						$scope.instances = dataset.instances;
					});
				};

				$scope.cancel = function () {
					$uibModalInstance.close();
				};

				$scope.reloadObservation = function () {
					$('.import-study-data').data('data-import', '1');
					$('body').addClass('import-preview-measurements');

					var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
					new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));
					$('.fbk-discard-imported-data').removeClass('fbk-hide');

					showSuccessfulMessage('', 'Calculated values for ' + $scope.selected.variable.name + ' were added successfully.');
				};

				$scope.reloadSubObservation = function () {
					$rootScope.navigateToSubObsTab(datasetId);
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

					var calculateRequestData = {
						variableId: $scope.selected.variable.cvTermId
						, geoLocationIds: geoLocationIds
					};

					// If selected dataset is PLOT DATA
					if (datasetId === studyContext.measurementDatasetId) {
						derivedVariableService.calculateVariableForObservation(calculateRequestData)
							.then(function (response) {
								if (response.data && response.data.hasDataOverwrite) {
									derivedVariableModalService.confirmOverrideCalculatedVariableModal(datasetId, $scope.selected.variable);
								} else {
									$scope.reloadObservation();
								}
								$uibModalInstance.close();
							});
					} else {
						derivedVariableService.calculateVariableForSubObservation(datasetId, calculateRequestData)
							.then(function (response) {
								if (response) {
									if (response.data && response.data.hasDataOverwrite) {
										derivedVariableModalService.confirmOverrideCalculatedVariableModal(datasetId, $scope.selected.variable, calculateRequestData);
									} else {
										$scope.reloadSubObservation();
									}
								}
								$uibModalInstance.close();
							});
					}


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

	derivedVariableModule.controller('confirmOverrideCalculatedVariableModalCtrl', ['$rootScope', '$scope', '$http', '$uibModalInstance',
		'derivedVariableModalService', 'derivedVariableService', 'selectedVariable', 'datasetId', 'studyContext', 'calculateRequestData',
		function ($rootScope, $scope, $http, $uibModalInstance, derivedVariableModalService, derivedVariableService, selectedVariable,
				  datasetId, studyContext, calculateRequestData) {

			$scope.goBack = function () {
				$http.get('/Fieldbook/ImportManager/revert/data')
					.then(function (response) {

						if (datasetId === studyContext.measurementDatasetId) {
							// Only revert plot measurements data if the selected dataset is PLOT DATA
							$scope.revertData();
						}
						$uibModalInstance.close();
						derivedVariableModalService.openExecuteCalculatedVariableModal(datasetId);
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

				// If selected dataset is PLOT DATA
				if (datasetId === studyContext.measurementDatasetId) {

					$('.import-study-data').data('data-import', '1');
					$('body').addClass('import-preview-measurements');

					var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
					new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));
					$('.fbk-discard-imported-data').removeClass('fbk-hide');

					showSuccessfulMessage('', 'Calculated values for ' + selectedVariable.name + ' were added successfully.');

				} else {
					// Explicitly tell the web service to save the calculated value immediately even if there's measurement data to overwrite.
					calculateRequestData.overwriteExistingData = true;
					derivedVariableService.calculateVariableForSubObservation(datasetId, calculateRequestData)
						.then(function (response) {
							if (response) {
								$rootScope.navigateToSubObsTab(datasetId);
								showSuccessfulMessage('', 'Calculated values for ' + selectedVariable.name + ' were added successfully.');
								$uibModalInstance.close();
							}
						});
				}

				$uibModalInstance.close();

			};

		}]);
})();
