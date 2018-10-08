/*global angular, showAlertMessage, showErrorMessage, selectEnvironmentContinueAdvancing*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SelectEnvironmentModalCtrl', ['$scope', 'TrialManagerDataService', 'environmentService', function ($scope,
																																  TrialManagerDataService, environmentService) {

		$scope.settings = TrialManagerDataService.settings.environments;
		if (Object.keys($scope.settings).length === 0) {
			$scope.settings = {};
			$scope.settings.managementDetails = [];
			$scope.settings.trialConditionDetails = [];
		}

		$scope.TRIAL_LOCATION_NAME_INDEX = 8180;
		$scope.TRIAL_LOCATION_ABBR_INDEX = 8189;
		$scope.LOCATION_NAME_ID = 8190;
		$scope.environmentListView = [];
		$scope.applicationData = TrialManagerDataService.applicationData;
		$scope.data = TrialManagerDataService.currentData.environments;

		$scope.$on('refreshEnvironmentService', function() {
			environmentService.updateEnvironmentData();
		});

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

					selectedLocationForTrial = {
						id: environment.managementDetailValues[$scope.LOCATION_NAME_ID]
						, name: locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].name
					};
				}
			});
		});

		$scope.trialInstances = [];

		$scope.noOfReplications = TrialManagerDataService.currentData.experimentalDesign.replicationsCount;

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

			if (!$scope.trialInstances) {
				showErrorMessage('', selectOneLocationErrorMessageForAdvancing);
			} else {
				if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX)) {
					selectedLocationDetails
						.push($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX).variable.name);
					locationAbbr = true;
				} else {
					selectedLocationDetails
						.push($scope.settings.managementDetails.val($scope.LOCATION_NAME_ID).variable.name);
				}

				angular.forEach($scope.trialInstances, function(trialInstanceNumber) {
					if (trialInstanceNumber) {
						selectedTrialInstances.push(trialInstanceNumber);
						angular.forEach($scope.environmentListView, function(environment) {
							if (environment.trialInstanceNumber === trialInstanceNumber) {
								if(locationAbbr){
									selectedLocationDetails.push(environment.customAbbrName);
								}else{
									selectedLocationDetails.push(environment.name);
								}
							}
						});
					}
				});

				selectEnvironmentContinueAdvancing(selectedTrialInstances, $scope.noOfReplications, selectedLocationDetails,
					$scope.applicationData.advanceType);
			}

		};

		$scope.doSelectAll = function () {
			$scope.trialInstances = [];
			var i = 1;
			angular.forEach($scope.environmentListView, function (environment) {
				if ($scope.selectAll) {
					environment.selected = i;
					i++;
					$scope.trialInstances.push(environment.trialInstanceNumber);
				} else {
					environment.selected = undefined;
				}
			});
		};

		$scope.init = function () {
			$scope.selectAll = true;
			$scope.environmentListView = environmentService.getEnvironmentDetails();
			$scope.doSelectAll();
		};
		$scope.init();

	}]);
})();
