(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.factory('CalculateCOPService', ['$http', 'serviceUtilities', 'studyContext',
		function ($http, serviceUtilities, studyContext) {
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/' + studyContext.studyId;
			var service = {};

			service.exportStudy = function (studyRequest) {
				const url = BASE_URL + `/cop/export`;
				return $http.post(url, studyRequest)
					.then(successHandler, failureHandler);
			};

			service.permissions = function (apiRequest) {
				const url = BASE_URL + `/cop/export/permissions`;
				return $http.post(url, apiRequest)
					.then(successHandler, failureHandler);
			};

			return service;

		}

	]);
})();
