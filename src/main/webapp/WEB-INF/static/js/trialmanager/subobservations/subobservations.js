/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationCtrl', ['$scope', 'TrialManagerDataService', '$stateParams',
		function ($scope, TrialManagerDataService, $stateParams) {

			$scope.subObservation = $stateParams.subObservation;
			$scope.title = $scope.subObservation.name;

		}]);

})();
