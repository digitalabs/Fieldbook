/*global angular, showAlertMessage, showErrorMessage, showSuccessfulMessage*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');
	manageTrialApp.controller('SubObservationUnitDatasetBuildCtrl', ['$scope', 'environmentService', '$http', 'formUtilities', 'MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS', 'MAXIMUM_NUMBER_FOR_EACH_PARENT_UNIT', 'configService', 'variableService', function ($scope, environmentService, $http, formUtilities, MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS, MAXIMUM_NUMBER_FOR_EACH_PARENT_UNIT, configService, variableService) {

		// TODO see Workbench/src/main/web/src/apps/ontology/app-services/bmsAuth.js
		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;
		var config = { headers: { 'X-Auth-Token': xAuthToken } };

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
			if ($scope.dtForm.$valid) {
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

				$http.get(/bmsapi/+configService.getCropName()+'/studies/'+configService.getStudyId()+'/datasets/generation', config).success(function (data) {
					showSuccessfulMessage('', subObservationDatasetBuiltSuccessMessage);
					angular.element('#SubObservationUnitDatasetBuildModal').modal('hide');
					$scope.submitted = false;
				}).error(function (data) {
					if (data.status == 401) {
						bmsAuth.handleReAuthentication();
					}
					showErrorMessage('', data.message);
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
			});

			variableService.getVariablesByFilter(null, null, 4040, 6040, null, null, null, 1812, null).then(function (variablesFiltered) {
				$scope.variables = variablesFiltered;
				angular.forEach($scope.variables, function (variable) {
					if ($scope.datasetType.defaultVariableId === parseInt(variable.id)) {
						$scope.selectedVariable = variable;
					}
				});
			});
		};

		$scope.formGroupClass = formUtilities.formGroupClassGenerator($scope, 'dtForm');

		$scope.validation = function () {
			if ($scope.subObservationTabs.length === MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS) {
				return true;
			}
		};

		$scope.init = function () {

			if ($scope.validation()) {
				showErrorMessage('', 'A study cannot have more than ' + MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS + ' Sub-Observation Tabs');

			} else {

				angular.element('#SubObservationUnitDatasetBuildModal').modal('hide');
				angular.element('#SubObservationUnitDatasetSelectorModal').modal({backdrop: 'static', keyboard: true});

				// Add hide listener to selectEnvironmentModal
				angular.element('#SubObservationUnitDatasetSelectorModal').one('hidden.bs.modal', function (e) {
					// When the selectEnvironmentModal is closed, remove the bs.modal data
					// so that the modal content is refreshed when it is opened again.
					angular.element(e.target).removeData('bs.modal');
				});

				$scope.datasetType = undefined;
				$scope.datasetTypes = $scope.getDatasetTypes();
			}
		};

		$scope.getDatasetTypes = function () {
			return [({
				id: 10094,
				label: 'Plants',
				name: 'plants',
				defaultVariableId: 8206,
				alias: 'plants'
			}), ({
				id: 10095,
				label: 'Quadrats',
				name: 'quadrats',
				defaultVariableId: 8207,
				alias: 'quadrats'
			}), ({
				id: 10096,
				label: 'Time Series',
				name: 'time series',
				defaultVariableId: 8205,
				alias: 'time points'
			}), ({
				id: 10097,
				label: 'Custom',
				name: 'sub-observation units',
				defaultVariableId: undefined,
				alias: 'sub-observation units'
			})];
		};

		$scope.dataSetTypeSelected = function (datasetType) {
			$scope.datasetType = datasetType;
		};

	}]);
})();


