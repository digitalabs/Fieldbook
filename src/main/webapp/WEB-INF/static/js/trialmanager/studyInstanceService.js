/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('studyInstanceService', ['$rootScope', 'TrialManagerDataService', '$http', 'serviceUtilities', 'studyContext', function ($rootScope, TrialManagerDataService, $http, serviceUtilities, studyContext) {

		var BASE_STUDY_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';

		var studyInstanceService = {};
		var successHandler = serviceUtilities.restSuccessHandler,
			failureHandler = serviceUtilities.restFailureHandler;

		studyInstanceService.createStudyInstances = function (numberOfInstancesToGenerate) {
			var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/instances/generation/?numberOfInstancesToGenerate=' + numberOfInstancesToGenerate);
			return request.then(successHandler, failureHandler);
		}

		studyInstanceService.getStudyInstances = function () {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/instances');
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.getStudyInstance = function (instanceId) {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/instances/' + instanceId);
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.deleteStudyInstances = function (instanceIds) {
			var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/instances/?instanceIds=' + instanceIds.join(','));
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.addInstanceObservation = function (observationData) {
			var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/instances/' + observationData.instanceId + '/observations', observationData);
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.updateInstanceObservation = function (observationData) {
			var request = $http.patch(BASE_STUDY_URL + studyContext.studyId + '/instances/' + observationData.instanceId + '/observations/' + observationData.observationDataId, observationData);
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.addInstanceDescriptor = function (descriptorData) {
			var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/instances/' + descriptorData.instanceId + '/descriptors', descriptorData);
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.updateInstanceDescriptor = function (descriptorData) {
			var request = $http.patch(BASE_STUDY_URL + studyContext.studyId + '/instances/' + descriptorData.instanceId + '/descriptors/' + descriptorData.descriptorDataId, descriptorData);
			return request.then(successHandler, failureHandler);
		};

		studyInstanceService.instanceInfo = TrialManagerDataService.currentData.instanceInfo;

		studyInstanceService.changeEnvironments = function () {
			this.broadcastEnvironments();
		};

		studyInstanceService.broadcastEnvironments = function () {
			$rootScope.$broadcast('changeEnvironments');
		};

		return studyInstanceService;

	}]);
})();
