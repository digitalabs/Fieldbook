/*global angular, showAlertMessage, showErrorMessage, selectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('selectEnvironmentToSampleListModalCtrl', ['$scope', 'environmentService', function ($scope, environmentService) {

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

			if ($scope.isEmptySelection) {
				showErrorMessage('', $.fieldbookMessages.errorNotSelectedInstance);
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
