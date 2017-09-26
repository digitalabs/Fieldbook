/*global angular, showAlertMessage, showErrorMessage, trialSelectEnvironmentContinueAdvancing*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SelectEnvironmentModalCtrl', ['$scope', 'TrialManagerDataService', 'environmentService', '$http', function($scope,
	TrialManagerDataService, environmentService, $http) {

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
		$scope.environmentListView = [];


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

					// Ensure that the location id and location name details of the $scope.data.environments
					// are updated with values from Location json object
					environment.managementDetailValues[$scope.LOCATION_NAME_ID]
						= locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].id;
					environment.managementDetailValues[$scope.TRIAL_LOCATION_NAME_INDEX]
						= locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].name;

					selectedLocationForTrial = {id: environment.managementDetailValues[$scope.LOCATION_NAME_ID]
						, name: locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].name};
				}
			});
		});

		$scope.trialInstances = [];

		$scope.noOfReplications = TrialManagerDataService.currentData.experimentalDesign.replicationsCount;

		//NOTE: Continue action for navigate from Locations to Advance Study Modal
		$scope.trialSelectEnvironmentContinue = function() {

			// Do not go ahead for Advancing unless trial has experimental design & number of replications variables
			if (TrialManagerDataService.currentData.experimentalDesign.designType === null) {
				showAlertMessage('', $.fieldbookMessages.advanceListUnableToGenerateWarningMessage);
				return;
			}

			var isTrialInstanceSelected = false;
			var selectedTrialInstances = [];
			var selectedLocationDetails = [];
			angular.forEach($scope.trialInstances, function(id) {
				if (id && !isTrialInstanceSelected) {
					isTrialInstanceSelected = true;
				}
			});

			if (!isTrialInstanceSelected) {
				showErrorMessage('', selectOneLocationErrorMessage);
			} else {
				if ($scope.locationFromTrialSettings) {
					selectedLocationDetails
						.push($scope.trialSettings.val($scope.PREFERRED_LOCATION_VARIABLE).variable.name);
				} else {
					selectedLocationDetails
						.push($scope.settings.managementDetails.val($scope.PREFERRED_LOCATION_VARIABLE).variable.name);
				}

				angular.forEach($scope.trialInstances, function(trialInstanceNumber, idx) {
					if (trialInstanceNumber) {
						selectedTrialInstances.push(trialInstanceNumber);

						if ($scope.locationFromTrialSettings) {
							selectedLocationDetails.push($scope.userInput[$scope.PREFERRED_LOCATION_VARIABLE]);
						} else {
							angular.forEach($scope.data.environments, function(environment, position) {
								if (position === idx) {
									selectedLocationDetails.push(environment.managementDetailValues[$scope.PREFERRED_LOCATION_VARIABLE]);
								}
							});
						}

					}
				});

				var isTrialInstanceNumberUsed = false;
				if ($scope.PREFERRED_LOCATION_VARIABLE === 8170) {
					isTrialInstanceNumberUsed = true;
				}
				trialSelectEnvironmentContinueAdvancing(selectedTrialInstances, $scope.noOfReplications, selectedLocationDetails,
					isTrialInstanceNumberUsed);
			}

		};

		$scope.doSelectAll = function() {
			$scope.trialInstances = [];
			$scope.trialInstancesName = [];
			if ($scope.selectAll) {
				$scope.selectAll = true;
			} else {
				$scope.selectAll = false;
				$scope.trialInstances = [];
			}
			angular.forEach($scope.environmentListView, function(environment) {
				environment.selected = $scope.selectAll;
				if ($scope.selectAll) {
					$scope.trialInstances.push(environment.trialInstanceNumber);
				}
			});

		};

		$scope.init = function() {

			debugger;
			$scope.locationFromTrialSettings = false;

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

			$scope.environmentListView = convertToEnvironmentListView($scope.data.environments,
				$scope.PREFERRED_LOCATION_VARIABLE, $scope.TRIAL_INSTANCE_INDEX);
		};

		$scope.init();

		// Converts the environments data (($scope.data.environments) for UI usage.
		function convertToEnvironmentListView(environments, preferredLocationVariable, trialInstanceIndex) {

			var environmentListView = [];
			angular.forEach($scope.data.environments, function(environment, key) {
				environmentListView.push({ name: environment.managementDetailValues[preferredLocationVariable]
					, variableId: preferredLocationVariable, trialInstanceNumber: environment.managementDetailValues[trialInstanceIndex]});
			});

			return environmentListView;

		};


	}]);

})();
