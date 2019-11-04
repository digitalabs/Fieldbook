/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('environmentService', ['$rootScope', 'TrialManagerDataService', '$http', 'serviceUtilities', 'studyContext', function ($rootScope, TrialManagerDataService, $http, serviceUtilities, studyContext) {

		var BASE_CROP_URL = '/bmsapi/crops/' + studyContext.cropName;
		var BASE_STUDY_URL = BASE_CROP_URL + '/studies/';

		var environmentService = {};
		var successHandler = serviceUtilities.restSuccessHandler,
			failureHandler = serviceUtilities.restFailureHandler;

		environmentService.createStudyInstance = function (instanceNumber) {
			var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/instances/' + instanceNumber);
			return request.then(successHandler, failureHandler);
		}

		environmentService.getStudyInstances = function () {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/instances/');
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
