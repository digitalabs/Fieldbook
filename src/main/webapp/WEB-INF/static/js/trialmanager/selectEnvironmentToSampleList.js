/*global angular, showAlertMessage, showErrorMessage, selectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('selectEnvironmentToSampleListModalCtrl', ['$scope', 'environmentService', '$timeout', function ($scope,
																															   environmentService, $timeout) {

		$scope.instances = [];
		$scope.selectedInstances = {};
		$scope.isEmptySelection = false;

		$scope.continueCreatingSampleList = function () {

			var instanceNumbers = [];
			Object.keys($scope.selectedInstances).forEach(function(instanceNumber) {
				var isSelected = $scope.selectedInstances[instanceNumber];
				if (isSelected) {
					instanceNumbers.push(instanceNumber);
				}
			});

			if (instanceNumbers.length === 0) {
				showErrorMessage('', selectOneLocationErrorMessageForSampleList);
			} else {
				selectedEnvironmentContinueCreatingSample(instanceNumbers);
			}
		};

		$scope.init = function () {

			environmentService.getEnvironments().then(function (environmentDetails) {
				$scope.instances = environmentDetails;
			});
		};
	}]);
})();
