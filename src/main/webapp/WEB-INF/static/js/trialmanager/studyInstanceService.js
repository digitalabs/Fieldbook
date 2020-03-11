/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('studyInstanceService', ['$rootScope', 'TrialManagerDataService', '$http', 'serviceUtilities', 'studyContext', function ($rootScope, TrialManagerDataService, $http, serviceUtilities, studyContext) {

		var BASE_STUDY_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';

		var studyInstanceService = {};
		var successHandler = serviceUtilities.restSuccessHandler,
			failureHandler = serviceUtilities.restFailureHandler;

		studyInstanceService.createStudyInstances = function (numberOfEnvironmentsToGenerate) {
			var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/environments/generation/?numberOfEnvironmentsToGenerate=' + numberOfEnvironmentsToGenerate);
			return request.then(successHandler, failureHandler);
		}

		studyInstanceService.getStudyInstances = function () {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/environments');
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.getStudyInstance = function (instanceId) {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/environments/' + instanceId);
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.deleteStudyInstances = function (instanceIds) {
			var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/environments/?environmentIds=' + instanceIds.join(','));
			return request.then(successHandler, failureHandler);
		};

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
