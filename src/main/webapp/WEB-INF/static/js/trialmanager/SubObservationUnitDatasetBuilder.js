/*global angular, showAlertMessage, showErrorMessage, showSuccessfulMessage*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');
	manageTrialApp.controller('SubObservationUnitDatasetBuildCtrl', ['$scope', 'environmentService', '$http', 'formUtilities', 'MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS', 'MAXIMUM_NUMBER_FOR_EACH_PARENT_UNIT', function ($scope, environmentService, $http, formUtilities, MAXIMUM_NUMBER_OF_SUB_OBSERVATION_SETS, MAXIMUM_NUMBER_FOR_EACH_PARENT_UNIT) {

		// TODO see Workbench/src/main/web/src/apps/ontology/app-services/bmsAuth.js
		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;
		var config = { headers: { 'X-Auth-Token': xAuthToken } };

		$scope.trialInstances = [];
		$scope.maximunNumForEachParentUnit = MAXIMUM_NUMBER_FOR_EACH_PARENT_UNIT;

		$scope.backToSubObservationUnitDatasetSelector = function () {
			$scope.submitted = false;
			$('#SubObservationUnitDatasetSelectorModal').modal('show');
			$('#SubObservationUnitDatasetBuildModal').modal('hide');

		};


		$scope.change = function () {
			$scope.submitted = false;
		};

		$scope.saveDataset = function(){
			if ($scope.dtForm.$valid) {
				showSuccessfulMessage('', 'The Sub-Observation Set was created successfully. You can review the details and begin working with these observation units in the new tab that has been added to the study.');
				$('#SubObservationUnitDatasetBuildModal').modal('hide');
				$scope.submitted = false;
			}
		};

		$http.get('/bmsapi/ontology/maize/filtervariables?programId='+currentProgramId+'&variableTypeIds=1808', config).success(function (data) {
			$scope.variables = data;
		}).error(function (data) {
			if (data.status == 401) {
				bmsAuth.handleReAuthentication();
			}
			showErrorMessage('', data.errors[0].message);
			$scope.variables = [];
		});

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

		$scope.continue = function () {
			$('#SubObservationUnitDatasetSelectorModal').modal('hide');
			$('#SubObservationUnitDatasetBuildModal').modal({backdrop: 'static', keyboard: true});

			// Add hide listener to selectEnvironmentModal
			$('#SubObservationUnitDatasetBuildModal').one('hidden.bs.modal', function (e) {
				// When the selectEnvironmentModal is closed, remove the bs.modal data
				// so that the modal content is refreshed when it is opened again.
				$(e.target).removeData('bs.modal');
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
			$scope.name = '';
			$scope.parentUnitNumber = '';
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

			angular.forEach($scope.variables,function (variable) {
				if ($scope.datasetType.defaultVariable === parseInt(variable.id)) {
					$scope.selectedVariable = variable;
				}
			})
		};

		$scope.formGroupClass = formUtilities.formGroupClassGenerator($scope, 'dtForm');

		$scope.init = function () {
			$scope.datasetType = undefined;
			$scope.datasetTypes = [({
				label:'Plants',
				name: 'plants',
				defaultVariable: 20428,
				alias: 'plants'
			}), ({
				label:'Quadrats',
				name: 'quadrats',
				defaultVariable: 62556,
				alias: 'quadrats'
			}), ({
				label:'Time Series',
				name: 'time series',
				defaultVariable: 20350,
				alias: 'time points'
			}), ({
				label:'Custom',
				name: 'sub-observation unit',
				defaultVariable: 20325,
				alias: 'sub-observation unit'
			})];
		};

		$scope.dataSetTypeSelected= function (datasetType) {
			$scope.datasetType = datasetType;
		};

	}]);

	manageTrialApp.filter('capitalize', function() {
		return function (input) {
			if (input !== undefined) {
				if (input.indexOf(' ') !== -1) {
					var inputPieces, i;

					input = input.toLowerCase();
					inputPieces = input.split(' ');

					for (i = 0; i < inputPieces.length; i++) {
						inputPieces[i] = capitalizeString(inputPieces[i]);
					}
					return inputPieces.toString().replace(/,/g, ' ');
				}
				else {
					input = input.toLowerCase();
					return capitalizeString(input);
				}
			} else {
				return input;
			}

			function capitalizeString(inputString) {
				if (inputString.indexOf('-') !== -1) {
					var inputPieces, i;

					inputString = inputString.toLowerCase();
					inputPieces = inputString.split('-');

					for (i = 0; i < inputPieces.length; i++) {
						inputPieces[i] = capitalizeString(inputPieces[i]);
					}
					return inputPieces.toString().replace(/,/g, '-');
				}
				return inputString.substring(0, 1).toUpperCase() + inputString.substring(1);
			}

		};
	});
})();


