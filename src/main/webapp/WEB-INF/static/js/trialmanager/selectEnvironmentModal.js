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
		$scope.trialInstances = [];
		$scope.environmentListView = [];
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

					selectedLocationForTrial = {
						id: environment.managementDetailValues[$scope.LOCATION_NAME_ID]
						, name: locationMap[environment.managementDetailValues[$scope.LOCATION_NAME_ID]].name
					};
				}
			});
		});

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

			if ($scope.trialInstances.length === 0) {
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

		$scope.init = function () {
			$scope.selectAll = true;
			environmentService.getEnvironments().then(function (environmentDetails) {
				$scope.environmentListView = [];
				$scope.trialInstances = [];
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
				if (!$.fn.dataTable.isDataTable('#selectEnvironmentModal .fbk-datatable-environments')) {
					$timeout(function () {
						angular.element('#selectEnvironmentModal .fbk-datatable-environments').DataTable({
							dom: "<'row'<'col-sm-6'l><'col-sm-6'f>>" +
								"<'row'<'col-sm-12'tr>>" +
								"<'row'<'col-sm-5'i><'col-sm-7'>>" +
								"<'row'<'col-sm-12'p>>"
						}).columns.adjust().draw();
					}, 1);
				}
			});
		};
	}]);
})();
