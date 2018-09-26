/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationCtrl', ['$scope', 'TrialManagerDataService', '$stateParams',
		function ($scope, TrialManagerDataService, $stateParams) {

		// On route transition
		$scope.title = $stateParams.subObservation.name;

		// TODO lazy load content

	}]);

})();
