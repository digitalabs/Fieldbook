/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationDivisionCtrl', ['$scope', 'TrialManagerDataService', '$stateParams',
		function ($scope, TrialManagerDataService, $stateParams) {

			$scope.division = $stateParams.division;

			// TODO lazy load content
		}]);

})();
