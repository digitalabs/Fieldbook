/*global angular, showAlertMessage, showErrorMessage, selectEnvironmentContinueAdvancing*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SelectEnvironmentModalCtrl', ['$scope', 'TrialManagerDataService', 'environmentService', '$timeout', function ($scope,
																																			  TrialManagerDataService, environmentService, $timeout) {

		$scope.settings = TrialManagerDataService.settings.environments;
		if (Object.keys($scope.settings).length === 0) {
			$scope.settings = {};
			$scope.settings.managementDetails = [];
			$scope.settings.trialConditionDetails = [];
		}

		$scope.TRIAL_LOCATION_NAME_INDEX = 8180;
		$scope.TRIAL_LOCATION_ABBR_INDEX = 8189;
		$scope.LOCATION_NAME_ID = 8190;
		$scope.applicationData = TrialManagerDataService.applicationData;
		$scope.data = TrialManagerDataService.currentData.environments;

		$scope.$on('changeEnvironments', function () {
			$scope.data = environmentService.environments;

			//create a map for location dropdown values
			var locationMap = {};
			angular.forEach($scope.settings.managementDetails.vals()[$scope.LOCATION_NAME_ID].allValues, function (locationVariable) {
				locationMap[locationVariable.id] = locationVariable;
			});

			angular.forEach($scope.data.environments, function (environment) {
				if (locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]]) {

					// Ensure that the location id and location name details of the $scope.data.environments
					// are updated with values from Location json object
					environment.managementDetailValues[$scope.LOCATION_NAME_ID]
						= locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].id;
					environment.managementDetailValues[$scope.TRIAL_LOCATION_NAME_INDEX]
						= locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].name;
				}
			});
		});

		$scope.noOfReplications = TrialManagerDataService.currentData.experimentalDesign.replicationsCount;

		$scope.instances = [];
		$scope.selectedInstances = {};
		$scope.isEmptySelection = false;

		//NOTE: Continue action for navigate from Locations to Advance Study Modal
		$scope.selectEnvironmentContinue = function() {

			// Do not go ahead for Advancing unless study has experimental design & number of replications variables
			if (TrialManagerDataService.currentData.experimentalDesign.designType === null) {
				showAlertMessage('', $.fieldbookMessages.advanceListUnableToGenerateWarningMessage);
				return;
			}

			var selectedTrialInstances = [];
			var selectedLocationDetails = [];
			var locationAbbr = false;

			if ($scope.isEmptySelection) {
				showErrorMessage('', $.fieldbookMessages.errorNotSelectedInstance);
			} else {
				if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX)) {
					selectedLocationDetails
						.push($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX).variable.name);
					locationAbbr = true;
				} else {
					selectedLocationDetails
						.push($scope.settings.managementDetails.val($scope.LOCATION_NAME_ID).variable.name);
				}

				angular.forEach($scope.instances, function (environment) {
					var isSelected = $scope.selectedInstances[environment.instanceNumber];
					if (isSelected) {
						selectedTrialInstances.push(environment.instanceNumber);
						if (locationAbbr) {
							selectedLocationDetails.push(environment.customLocationAbbreviation);
						} else {
							selectedLocationDetails.push(environment.locationName);
						}
					}
				});

				selectEnvironmentContinueAdvancing(selectedTrialInstances, $scope.noOfReplications, selectedLocationDetails,
					$scope.applicationData.advanceType);
			}

		};

		$scope.init = function () {
			environmentService.getEnvironments().then(function (environmentDetails) {
				$scope.instances = environmentDetails;
			});
		};
	}]);
})();
