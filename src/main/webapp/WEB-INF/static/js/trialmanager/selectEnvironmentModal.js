/*global angular, showAlertMessage, showErrorMessage, trialSelectEnvironmentContinueAdvancing*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SelectEnvironmentModalCtrl', ['$scope', 'TrialManagerDataService', 'environmentService', function($scope,
																																 TrialManagerDataService, environmentService) {

		$scope.settings = TrialManagerDataService.settings.environments;
		if (Object.keys($scope.settings).length === 0) {
			$scope.settings = {};
			$scope.settings.managementDetails = [];
			$scope.settings.trialConditionDetails = [];
		}
		$scope.userInput = TrialManagerDataService.currentData.trialSettings.userInput;
		$scope.trialSettings = TrialManagerDataService.settings.trialSettings;

		$scope.TRIAL_LOCATION_NAME_INDEX = 8180;
		$scope.TRIAL_LOCATION_ABBR_INDEX = 8189;
		$scope.TRIAL_INSTANCE_INDEX = 8170;
		$scope.PREFERRED_LOCATION_VARIABLE = 8170;
		$scope.LOCATION_NAME_ID = 8190;

		$scope.data = TrialManagerDataService.currentData.environments;

		$scope.$on('changeEnvironments', function() {
			$scope.data = environmentService.environments;

			//create a map for location dropdown values
			var locationMap = {};
			angular.forEach($scope.settings.managementDetails.vals()[$scope.LOCATION_NAME_ID].allValues, function(locationVariable) {
				locationMap[locationVariable.id] = locationVariable;
			});

			angular.forEach($scope.data.environments, function(environment) {
				if(locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]]) {
					//Set the value of the location id per environment
					environment.managementDetailValues[$scope.LOCATION_NAME_ID] = locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].id;
					selectedLocationForTrial = {id: environment.managementDetailValues[$scope.LOCATION_NAME_ID], name: locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].name};
				}
			});
		});

		$scope.noOfReplications = TrialManagerDataService.currentData.experimentalDesign.replicationsCount;

		//NOTE: Continue action for navigate from Locations to Advance Study Modal
		$scope.trialSelectEnvironmentContinue = function () {

			// Do not go ahead for Advancing unless trial has experimental design & number of replications variables
			if (TrialManagerDataService.currentData.experimentalDesign.designType === null) {
				showAlertMessage('', $.fieldbookMessages.advanceListUnableToGenerateWarningMessage);
				return;
			}

			var selectedLocationDetails = [];
			var selectedTrialInstancesByAdvanceList = [];

			if ($scope.locationFromTrialSettings) {
				selectedLocationDetails
					.push($scope.trialSettings.val($scope.PREFERRED_LOCATION_VARIABLE).variable.name);
			} else {
				selectedLocationDetails
					.push($scope.settings.managementDetails.val($scope.PREFERRED_LOCATION_VARIABLE).variable.name);
			}

			angular.forEach($scope.data.environments, function (env) {

				if ($scope.locationFromTrialSettings) {
					selectedLocationDetails.push($scope.userInput[$scope.PREFERRED_LOCATION_VARIABLE]);
				} else if (env.Selected) {
					selectedLocationDetails.push(env.managementDetailValues[$scope.PREFERRED_LOCATION_VARIABLE]);
					selectedTrialInstancesByAdvanceList.push(env.managementDetailValues[$scope.TRIAL_INSTANCE_INDEX]);
				}
			});

			var isTrialInstanceNumberUsed = false;
			if ($scope.PREFERRED_LOCATION_VARIABLE === 8170) {
				isTrialInstanceNumberUsed = true;
			}

			if (selectedTrialInstancesByAdvanceList.length === 0) {
				showErrorMessage('', selectOneLocationErrorMessage);
			} else {
				trialSelectEnvironmentContinueAdvancing(selectedTrialInstancesByAdvanceList, $scope.noOfReplications, selectedLocationDetails,
					isTrialInstanceNumberUsed);
			}
		};

		$scope.doSelectAll = function() {
			angular.forEach($scope.data.environments, function (environment) {
				environment.Selected = $scope.selectAll;
			});
		};

		$scope.doSelectInstance = function (index) {
			var environment = $scope.data.environments[index];
			if (!environment.Selected) {
				$scope.selectAll = false;
			}
		};

		$scope.init = function() {
			$scope.locationFromTrialSettings = false;
			$scope.selectAll = true;
			$scope.doSelectAll();

			if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX) != null) {
				// LOCATION_ABBR from environments
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_ABBR_INDEX;
			} else if ($scope.trialSettings.val($scope.TRIAL_LOCATION_ABBR_INDEX) != null) {
				// LOCATION_ABBR from trial settings
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_ABBR_INDEX;
				$scope.locationFromTrialSettings = true;
			} else if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_NAME_INDEX) != null) {
				// LOCATION_NAME from environments
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_INDEX;
			} else if ($scope.trialSettings.val($scope.TRIAL_LOCATION_NAME_INDEX) != null) {
				// LOCATION_NAME from trial settings
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_INDEX;
				$scope.locationFromTrialSettings = true;
			} else {
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_INSTANCE_INDEX;
			}
		};
		$scope.init();

	}]);

})();
