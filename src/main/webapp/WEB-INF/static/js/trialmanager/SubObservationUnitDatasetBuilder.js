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

		$scope.backToSubObservationUnitDatasetSelector = function () {
			$('#SubObservationUnitDatasetSelectorModal').modal('show');
			$('#SubObservationUnitDatasetBuildModal').modal('hide');

		};

		$scope.saveDataset = function(){

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

		$scope.init = function (option) {
			$scope.selectAll = true;
			$scope.header = option;
			environmentService.getEnvironments().then(function(environmentDetails){
				try {
					$scope.environmentListView = [];

					angular.forEach(environmentDetails, function (environment) {
						$scope.environmentListView.push({
							name: environment.locationName + ' - (' + environment.locationAbbreviation + ')',
							abbrName: environment.locationAbbreviation,
							customAbbrName: environment.customLocationAbbreviation,
							trialInstanceNumber: environment.instanceNumber,
							instanceDbId: environment.instanceDbId
						});
					});

				} catch (e) {
					//TODO
				}

			}, function() {
				//TODO
			}).finally (function() {
				if($scope.selectAll){
					$scope.doSelectAll();
				}
			});

		};
		$scope.init();
	}]);
	manageTrialApp.filter('capitalize', function() {
		return function(input) {
			return (!!input) ? input.charAt(0).toUpperCase() + input.substr(1).toLowerCase() : '';
		}
	});
})();


