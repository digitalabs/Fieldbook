/*global angular, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('ExecuteCalculatedVariableModalCtrl', ['$scope', 'TrialManagerDataService','$http', function ($scope,
																																  TrialManagerDataService,$http) {


		$scope.settings = TrialManagerDataService.settings.environments;

		$scope.LOCATION_NAME_ID = 8190;
		$scope.TRIAL_INSTANCE_INDEX = 8170;
		$scope.data = TrialManagerDataService.currentData.environments;
		$scope.locationListView;
		$scope.variableListView;
		$scope.locationSelected = undefined;
		$scope.variableSelected = undefined;

		$scope.execute = function () {
			var calculateData = {
				variableId: $scope.variableSelected.cvTermId
				, geoLocationId: $scope.locationSelected.locationId
			};
			var nameVar = $scope.variableSelected.name;

			$http.post('/Fieldbook/DerivedVariableController/derived-variable/execute', JSON.stringify(calculateData))
				.success(function (data) {
					$('#executeCalculatedVariableModal').modal('hide');
					data.hasDataOverwrite = '1';
					if (data.hasDataOverwrite === '1') {
						// Nuevo Controller.
						$('#confirmOverrideCalculatedVariableModal').modal({ backdrop: 'static', keyboard: true });

						// Add hide listener to selectEnvironmentModal
						$('#confirmOverrideCalculatedVariableModal').one('hidden.bs.modal', function (e) {
							// When the selectEnvironmentModal is closed, remove the bs.modal data
							// so that the modal content is refreshed when it is opened again.
							$(e.target).removeData('bs.modal');
						});
						angular.element('#confirmOverrideCalculatedVariableModal').scope();
						//$scope.$apply();
					}else{
						showSuccessfulMessage('', 'Calculated values for ' + nameVar + ' were added successfully.');
					}



				}).error(function (data) {

				if (data.status == 401) {
					bmsAuth.handleReAuthentication();
				}
				if (data.status == 400) {
					showErrorMessage('', data.errorMessage);
				}
			});

		};

		$scope.init = function () {
			$scope.calculateVariableLocationForm.$setPristine();
			$scope.variableListView = convertTraitsVariablesToListView(TrialManagerDataService.settings.measurements.m_keys);
			$scope.locationListView = convertToEnvironmentListView($scope.data.environments, $scope.LOCATION_NAME_ID, $scope.TRIAL_INSTANCE_INDEX);
			//$scope.calculateVariableLocationForm.$setPristine();
			$scope.variableSelected = $scope.variableListView[0];

		};

		function convertTraitsVariablesToListView(traitIdList) {
			var variableListView = [];
			angular.forEach(traitIdList, function (id) {
				var variable = TrialManagerDataService.settings.measurements.m_vals[id].variable;
				//if (variable.formulaInputVariables) {
					variableListView.push({name: variable.name, cvTermId: variable.cvTermId});
				//}
			});
			return variableListView;
		};

		// Converts the environments data (($scope.data.environments) for UI usage.
		function convertToEnvironmentListView(environments, preferredLocationVariable, trialInstanceIndex) {

			var environmentListView = [];
			angular.forEach(environments, function(environment) {
				environmentListView.push({ name: getPreferredEnvironmentName(environment, preferredLocationVariable)
					,trialInstanceNumber: environment.managementDetailValues[trialInstanceIndex]
				,locationId:environment.locationId});

			});
			return environmentListView;

			function getPreferredEnvironmentName(environment, preferredLocationVariable) {

				var preferredLocation = '';
				if ($scope.settings.managementDetails.vals()[$scope.LOCATION_NAME_ID] !== undefined) {

					//create a map for location dropdown values
					var locationMap = {};

					angular.forEach($scope.settings.managementDetails.vals()[$scope.LOCATION_NAME_ID].allValues, function(locationVariable) {
						locationMap[locationVariable.id] = locationVariable;
					});

					var locationId = 0;
					if (environment.managementDetailValues[$scope.LOCATION_NAME_ID] !== undefined) {
						locationId = isNaN(environment.managementDetailValues[$scope.LOCATION_NAME_ID]) ?
							environment.managementDetailValues[$scope.LOCATION_NAME_ID].id :
							environment.managementDetailValues[$scope.LOCATION_NAME_ID];
					}

					if (locationId !== 0) {
						preferredLocation = locationMap[locationId].name;
					}

				}

				var preferredLocationVariableName = preferredLocationVariable === $scope.LOCATION_NAME_ID ? preferredLocation
					: environment.managementDetailValues[preferredLocationVariable];

				return preferredLocationVariableName;

			}
		};
	}]);

	manageTrialApp.controller('ConfirmOverrideCalculatedVariableModalCtrl', ['$scope', '$http', function ($scope, $http) {



		$scope.init = function () {


		};

		$scope.goBack = function () {
			$http.get('/Fieldbook/ImportManager/revert/data').success(function (data) {

			}).error(function (data) {

				if (data.status == 401) {
					bmsAuth.handleReAuthentication();
				}
				if (data.status == 400) {
					showErrorMessage('', data.errorMessage);
				}
			});
			$('#confirmOverrideCalculatedVariableModal').modal('hide');
			$('#executeCalculatedVariableModal').modal('show');

		};

		$scope.proceed = function () {
			$('#confirmOverrideCalculatedVariableModal').modal('hide');
			$scope = angular.element('#executeCalculatedVariableModal').scope();
			showSuccessfulMessage('', 'Calculated values for ' + $scope.variableSelected.name + ' were added successfully.');
		};

	}]);
})();
