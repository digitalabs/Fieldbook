/*global angular, showAlertMessage, showErrorMessage*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');
	manageTrialApp.controller('SubObservationUnitDatasetSelectorCtrl', ['$scope', function ($scope) {

		$scope.option = {
			name: ''
		};

		$scope.selectEnvironmentContinue = function () {
			console.log($scope.option.name);
			subObservationUnitDatasetbuild($scope.option.name);

		};

		$scope.init = function () {
			$scope.option = {
				name: ''
			};
		};
		$scope.init();
	}]);

	manageTrialApp.controller('SubObservationUnitDatasetBuildCtrl', ['$scope', 'environmentService', '$timeout', function ($scope, environmentService, $timeout) {

		$scope.trialInstances = [];
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

		$scope.backToSubObservationUnitDatasetSelector = function () {
			$('#SubObservationUnitDatasetSelectorModal').modal('show');
			$('#SubObservationUnitDatasetBuildModal').modal('hide');

		};

		$scope.saveDataset = function(){

		};

		$scope.init = function (option) {
			$scope.trialInstances = [];
			$scope.environmentListView = [];
			$scope.header = option;

			$scope.selectAll = true;
			$scope.environmentListView = environmentService.getEnvironmentDetails();
			$scope.doSelectAll();
		};
		$scope.init();
	}]);
})();


