/*global angular, showAlertMessage, showErrorMessage, selectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('selectEnvironmentToSampleListModalCtrl', ['$scope', 'environmentService', function ($scope,
																												   environmentService) {

		$scope.trialInstances = [];
		$scope.environmentListView = [];

		$scope.continueCreatingSampleList = function () {
			if ($scope.trialInstances.length === 0) {
				showErrorMessage('', selectOneLocationErrorMessageForSampleList);
			} else {
				selectedEnvironmentContinueCreatingSample($scope.trialInstances);
			}
		};

		$scope.init = function () {
			$scope.selectAll = true;
			environmentService.getEnvironments().then(function (environmentDetails) {
				$scope.trialInstances = [];
				$scope.environmentListView = [];

				angular.forEach(environmentDetails, function (environment) {
					$scope.environmentListView.push({
						name: environment.locationName + ' - (' + environment.locationAbbreviation + ')',
						abbrName: environment.locationAbbreviation,
						customAbbrName: environment.customLocationAbbreviation,
						trialInstanceNumber: environment.instanceNumber,
						instanceDbId: environment.instanceDbId,
						selected: $scope.selectAll
					});
					$scope.trialInstances.push(environment.instanceNumber)
				});
			});
		};
	}]);
})();
