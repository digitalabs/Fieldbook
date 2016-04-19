/*global angular*/

(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('environmentService', ['$rootScope', 'TrialManagerDataService', function($rootScope, TrialManagerDataService) {

		var environmentService = {};

		environmentService.environments = TrialManagerDataService.currentData.environments;

		environmentService.changeEnvironments = function(data) {
			//this.environments.environments = data.key;
			this.broadcastEnvironments();
		};

		environmentService.broadcastEnvironments = function() {
			$rootScope.$broadcast('changeEnvironments');
		};

		return environmentService;

	}]);

})();
