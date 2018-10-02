/*global angular, showAlertMessage, showErrorMessage, selectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('selectEnvironmentToSampleListModalCtrl', ['$scope', 'environmentService', function ($scope,
																												   environmentService) {

		$scope.trialInstances = [];
		$scope.environmentListView = [];

		$scope.continueCreatingSampleList = function () {
			if (!$scope.trialInstances) {
				showErrorMessage('', selectOneLocationErrorMessageForSampleList);
			} else {
				selectedEnvironmentContinueCreatingSample($scope.trialInstances);
			}
		};

		$scope.doSelectAll = function () {
			$scope.trialInstances = [];
			var i = 1;
			angular.forEach($scope.environmentListView, function (environment) {
				if ($scope.selectAll) {
					environment.Selected = i;
					i++;
					$scope.trialInstances.push(environment.trialInstanceNumber);
				} else {
					environment.Selected = undefined;
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
