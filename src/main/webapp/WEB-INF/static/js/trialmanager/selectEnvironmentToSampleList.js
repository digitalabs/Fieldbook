/*global angular, showAlertMessage, showErrorMessage, selectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('selectEnvironmentToSampleListModalCtrl', ['$scope', 'TrialManagerDataService', function ($scope,
																														TrialManagerDataService) {

		$scope.settings = TrialManagerDataService.settings.environments;

		if (Object.keys($scope.settings).length === 0) {
			$scope.settings = {};
			$scope.settings.managementDetails = [];
			$scope.settings.trialConditionDetails = [];
		}

		$scope.selectedInstancesBySampleList = [];

		$scope.trialSettings = TrialManagerDataService.settings.trialSettings;

		$scope.TRIAL_INSTANCE_ID = 8170;
		$scope.LOCATION_NAME_ID = 8190;
		$scope.environmentListView = [];

		$scope.continueCreatingSampleList = function () {
			if ($scope.selectedInstancesBySampleList.length === 0) {
				showErrorMessage('', selectOneLocationErrorMessageForSampleList);
			} else {
				selectedEnvironmentContinueCreatingSample($scope.selectedInstancesBySampleList);
			}
		};

		$scope.doSelectAll = function () {
			$scope.selectedInstancesBySampleList = [];
			var i = 1;
			angular.forEach($scope.environmentListView, function (environment) {
				if ($scope.selectAll) {
					environment.Selected = i;
					i = i + 1;
					$scope.selectedInstancesBySampleList.push(environment.trialInstanceNumber);
				} else {
					environment.Selected = undefined;
				}
			});
		};

		$scope.doSelectInstance = function (index) {
			var environment = $scope.environmentListView[index];
			if (environment.Selected != undefined) {
				$scope.selectedInstancesBySampleList.push(environment.trialInstanceNumber);
			} else {
				$scope.selectAll = false;
				var idx = $scope.selectedInstancesBySampleList.indexOf(String(index + 1));
				$scope.selectedInstancesBySampleList.splice(idx, 1);
			}
		};


		$scope.init = function () {
			$scope.locationFromTrialSettings = false;
			$scope.locationFromTrial = false;
			$scope.selectAll = true;

			if ($scope.settings.managementDetails.val($scope.LOCATION_NAME_ID) != null) {
				// LOCATION_NAME from environments
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.LOCATION_NAME_ID;
				$scope.locationFromTrial = true;
			} else {
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_INSTANCE_ID;
			}
			$scope.environmentListView = TrialManagerDataService.getEnvironments($scope.PREFERRED_LOCATION_VARIABLE, $scope.settings.managementDetails);
			$scope.doSelectAll();
		};

		$scope.init();
	}]);
})();
