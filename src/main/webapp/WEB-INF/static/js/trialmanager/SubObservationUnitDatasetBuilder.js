/*global angular, showAlertMessage, showErrorMessage, showSuccessfulMessage*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');
	manageTrialApp.controller('SubObservationUnitDatasetBuildCtrl', ['$scope', 'environmentService', '$http', 'formUtilities',
		'MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS', 'MAXIMUM_NUMBER_FOR_EACH_PARENT_UNIT', 'variableService', 'studyContext',
		'DATASET_TYPES', 'datasetService', '$timeout', function ($scope, environmentService, $http, formUtilities, MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS,
																 MAXIMUM_NUMBER_FOR_EACH_PARENT_UNIT, variableService, studyContext, DATASET_TYPES, datasetService, $timeout) {

		$scope.trialInstances = [];
		$scope.maximunNumForEachParentUnit = MAXIMUM_NUMBER_FOR_EACH_PARENT_UNIT;

		$scope.backToSubObservationUnitDatasetSelector = function () {
			$scope.submitted = false;
			angular.element('#SubObservationUnitDatasetSelectorModal').modal('show');
			angular.element('#SubObservationUnitDatasetBuildModal').modal('hide');

		};

		$scope.change = function () {
			$scope.submitted = false;
		};

		$scope.saveDataset = function(){
			if ($scope.dtForm.$valid && $scope.trialInstances != 0) {
				var instanceIds = [];

				angular.forEach($scope.trialInstances, function (trialInstanceNumber) {
					angular.forEach($scope.environmentListView, function (environment) {
						if (environment.trialInstanceNumber === trialInstanceNumber) {
							instanceIds.push(environment.instanceDbId);

						}
					});
				});

				var newDataset = {
					"datasetTypeId": $scope.datasetType.id,
					"datasetName": $scope.datasetName,
					"instanceIds": instanceIds,
					"sequenceVariableId": parseInt($scope.selectedVariable.id),
					"numberOfSubObservationUnits": $scope.numberOfSubObservationUnits
				};

				datasetService.generation(newDataset).then(function (response) {
					showSuccessfulMessage('', subObservationDatasetBuiltSuccessMessage);
					angular.element('#SubObservationUnitDatasetBuildModal').modal('hide');
					$scope.submitted = false;
					$scope.addSubObservationTabData(response.datasetId, response.name,response.datasetTypeId,response.parentDatasetId);
				}, function (response) {
					if (response.status == 401) {
						bmsAuth.handleReAuthentication();
					} else if (response.data.errors) {
						showErrorMessage('', response.data.errors[0].message);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});
			}
		};

		$scope.continue = function () {
			angular.element('#SubObservationUnitDatasetSelectorModal').modal('hide');
			angular.element('#SubObservationUnitDatasetBuildModal').modal({backdrop: 'static', keyboard: true});

			// Add hide listener to selectEnvironmentModal
			angular.element('#SubObservationUnitDatasetBuildModal').one('hidden.bs.modal', function (e) {
				// When the selectEnvironmentModal is closed, remove the bs.modal data
				// so that the modal content is refreshed when it is opened again.
				angular.element(e.target).removeData('bs.modal');
			});
				$scope.initDatasetBuild();
		};

		$scope.initDatasetBuild = function () {
			if($scope.dtForm){
				$scope.dtForm.$submitted = false;
				$scope.dtForm.$setUntouched(true);
				$scope.dtForm.selectVariableDatasetBuilder.$setPristine();
			}

			$scope.header = $scope.datasetType.name;
			$scope.datasetName = '';
			$scope.numberOfSubObservationUnits = '';
			$scope.selectedVariable = undefined;
			$scope.trialInstances = [];
			$scope.selectAll = true;

			environmentService.getEnvironments().then(function (environmentDetails) {
				$scope.environmentListView = [];

				angular.forEach(environmentDetails, function (environment) {
					$scope.environmentListView.push({
						name: environment.locationName + ' - (' + environment.locationAbbreviation + ')',
						abbrName: environment.locationAbbreviation,
						customAbbrName: environment.customLocationAbbreviation,
						trialInstanceNumber: environment.instanceNumber,
						instanceDbId: environment.instanceDbId,
						selected: $scope.selectAll
					});
					$scope.trialInstances.push(environment.instanceNumber);
				});

				//This can be used to check if a table is a DataTable or not already.
				if (!$.fn.dataTable.isDataTable('#SubObservationUnitDatasetBuildModal .fbk-datatable-environments')) {
					$timeout(function () {
						angular.element('#SubObservationUnitDatasetBuildModal .fbk-datatable-environments').DataTable({
							dom: "<'row'<'col-sm-6'l><'col-sm-6'f>>" +
								"<'row'<'col-sm-12'tr>>" +
								"<'row'<'col-sm-5'i><'col-sm-7'>>" +
								"<'row'<'col-sm-12'p>>"
						}).columns.adjust().draw();
					}, 1);
				}
			});

			variableService.getVariablesByFilter({
				methodIds: 4040,
				scaleIds: 6040,
				variableTypeIds: 1812
			}).then(function (variablesFiltered) {
				$scope.variables = variablesFiltered;
				angular.forEach($scope.variables, function (variable) {
					if ($scope.datasetType.defaultVariableId === parseInt(variable.id)) {
						$scope.selectedVariable = variable;
					}
					$timeout(function () {
						angular.element('#variableDatasetBuilder').select2();
					}, 1);
				});
			});
		};

		$scope.formGroupClass = formUtilities.formGroupClassGenerator($scope, 'dtForm');

		$scope.validation = function () {
			if ($scope.subObservationTabs.length >= MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS) {
				showErrorMessage('', 'A study cannot have more than ' + MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS + ' Sub-Observation Tabs');
				return false;
			}
			return true;
		};

		$scope.init = function () {
			if ($scope.validation()) {
				angular.element('#SubObservationUnitDatasetBuildModal').modal('hide');
				angular.element('#SubObservationUnitDatasetSelectorModal').modal({backdrop: 'static', keyboard: true});
				// Add hide listener to SubObservationUnitDatasetSelectorModal
				angular.element('#SubObservationUnitDatasetSelectorModal').one('hidden.bs.modal', function (e) {
					// When the SubObservationUnitDatasetSelectorModal is closed, remove the bs.modal data
					// so that the modal content is refreshed when it is opened again.
					angular.element(e.target).removeData('bs.modal');
				});

				$scope.datasetType = undefined;
				$scope.datasetTypes = $scope.getDatasetTypes();
			}
		};

		$scope.getDatasetTypes = function () {
			return [{
				id: DATASET_TYPES.PLANT_SUBOBSERVATIONS,
				label: 'Plants',
				name: 'plants',
				defaultVariableId: 8206,
				alias: 'plants'
			}, {
				id: DATASET_TYPES.QUADRAT_SUBOBSERVATIONS,
				label: 'Quadrats',
				name: 'quadrats',
				defaultVariableId: 8207,
				alias: 'quadrats'
			}, {
				id: DATASET_TYPES.TIME_SERIES_SUBOBSERVATIONS,
				label: 'Time Series',
				name: 'time series',
				defaultVariableId: 8205,
				alias: 'time points'
			}, {
				id: DATASET_TYPES.CUSTOM_SUBOBSERVATIONS,
				label: 'Custom',
				name: 'sub-observation units',
				defaultVariableId: undefined,
				alias: 'sub-observation units'
			}];
		};

		$scope.dataSetTypeSelected = function (datasetType) {
			$scope.datasetType = datasetType;
		};

	}]);
})();


