/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationTabCtrl', ['$scope', 'TrialManagerDataService', '$stateParams',
		function ($scope, TrialManagerDataService, $stateParams) {

			$scope.subObservationTab = $stateParams.subObservationTab;
			$scope.title = $scope.subObservationTab.name;

		}]);

})();
