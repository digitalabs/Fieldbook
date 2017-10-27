/*global angular, showAlertMessage, showErrorMessage, trialSelectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('selectEnvironmentToSampleListModalCtrl', ['$scope', 'TrialManagerDataService', 'environmentService', function ($scope,
																																			  TrialManagerDataService, environmentService) {

		$scope.settings = TrialManagerDataService.settings.environments;

		if (Object.keys($scope.settings).length === 0) {
			$scope.settings = {};
			$scope.settings.managementDetails = [];
			$scope.settings.trialConditionDetails = [];
		}

		$scope.selectedTrialInstancesBySampleList = [];

		$scope.trialSettings = TrialManagerDataService.settings.trialSettings;

		$scope.TRIAL_LOCATION_NAME_ID = 8180;
		$scope.TRIAL_INSTANCE_ID = 8170;
		$scope.PREFERRED_LOCATION_VARIABLE = 8170;
		$scope.LOCATION_NAME_ID = 8190;

		$scope.instances = angular.copy(environmentService.environments);

		$scope.continueCreatingSampleList = function () {
			if ($scope.selectedTrialInstancesBySampleList.length === 0) {
				showErrorMessage('', selectOneLocationErrorMessageForSampleList);
			} else {
				trialSelectedEnvironmentContinueCreatingSample($scope.selectedTrialInstancesBySampleList);
			}
		};

		$scope.doSelectAll = function () {
			$scope.selectedTrialInstancesBySampleList = [];
			var i = 1;
			angular.forEach($scope.instances.environments, function (environment) {
				if ($scope.selectAll) {
					environment.Selected = i;
					i = i + 1;
					$scope.selectedTrialInstancesBySampleList.push(environment.managementDetailValues[$scope.TRIAL_INSTANCE_ID]);
				} else {
					environment.Selected = undefined;
				}
			});
		};

		$scope.doSelectInstance = function (index) {
			var environment = $scope.instances.environments[index];
			if (environment.Selected != undefined) {
				$scope.selectedTrialInstancesBySampleList.push(environment.managementDetailValues[$scope.TRIAL_INSTANCE_ID]);
			} else {
				$scope.selectAll = false;
				var idx = $scope.selectedTrialInstancesBySampleList.indexOf(String(index + 1));
				$scope.selectedTrialInstancesBySampleList.splice(idx, 1);
			}
		};


		$scope.init = function () {

			$scope.instances = angular.copy(environmentService.environments);
			$scope.locationFromTrialSettings = false;
			$scope.locationFromTrial = false;
			$scope.selectAll = true;

			if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_NAME_ID) != null) {
				// LOCATION_NAME from environments
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_ID;
				$scope.locationFromTrial = true;
			} else {
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_INSTANCE_ID;
			}
			$scope.doSelectAll();
		};

		$scope.init();
	}]);
})();
