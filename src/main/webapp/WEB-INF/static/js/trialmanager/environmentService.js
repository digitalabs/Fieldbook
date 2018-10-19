/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('environmentService', ['$rootScope', 'TrialManagerDataService', '$http', 'serviceUtilities', 'configService', function ($rootScope, TrialManagerDataService, $http, serviceUtilities, configService) {

		var environmentService = {};
		var successHandler = serviceUtilities.restSuccessHandler,
			failureHandler = serviceUtilities.restFailureHandler;

		environmentService.getEnvironments = function () {
			var xAuthToken = JSON.parse(localStorage['bms.xAuthToken']).token;
			var config = {
				headers: {
					'X-Auth-Token': xAuthToken
				}
			};
			var request = $http.get('/bmsapi/study/' + configService.getCropName() + '/' + configService.getStudyId() + '/instances', config);
			return request.then(successHandler, failureHandler);
		};

		environmentService.environments = TrialManagerDataService.currentData.environments;

		environmentService.changeEnvironments = function () {
			this.broadcastEnvironments();
		};

		environmentService.broadcastEnvironments = function () {
			$rootScope.$broadcast('changeEnvironments');
		};

		return environmentService;

	}]);
})();
