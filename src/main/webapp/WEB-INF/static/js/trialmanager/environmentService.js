/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('environmentService', ['$rootScope', 'TrialManagerDataService', '$http', 'serviceUtilities', function ($rootScope, TrialManagerDataService, $http, serviceUtilities) {

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
			var request = $http.get('/bmsapi/study/' + cropName + '/' + TrialManagerDataService.currentData.basicDetails.studyID + '/instances', config);
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

	manageTrialApp.factory('serviceUtilities', ['$q', function ($q) {
		return {
			restSuccessHandler: function (response) {
				return response.data;
			},

			restFailureHandler: function (response) {
				return $q.reject({
					status: response.status,
					errors: response.data && response.data.errors
				});
			}
		};
	}]);
})();
