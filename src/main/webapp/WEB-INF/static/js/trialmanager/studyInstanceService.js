/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('studyInstanceService', ['$rootScope', 'TrialManagerDataService', '$http', 'serviceUtilities', 'studyContext', function ($rootScope, TrialManagerDataService, $http, serviceUtilities, studyContext) {

		var BASE_CROP_URL = '/bmsapi/crops/' + studyContext.cropName;
		var BASE_STUDY_URL = BASE_CROP_URL + '/studies/';

		var studyInstanceService = {};
		var successHandler = serviceUtilities.restSuccessHandler,
			failureHandler = serviceUtilities.restFailureHandler;

		studyInstanceService.createStudyInstance = function (instanceNumber) {
			var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/instances/' + instanceNumber);
			return request.then(successHandler, failureHandler);
		}

		studyInstanceService.environments = TrialManagerDataService.currentData.environments;

		studyInstanceService.changeEnvironments = function () {
			this.broadcastEnvironments();
		};

		studyInstanceService.broadcastEnvironments = function () {
			$rootScope.$broadcast('changeEnvironments');
		};

		return studyInstanceService;

	}]);
})();
