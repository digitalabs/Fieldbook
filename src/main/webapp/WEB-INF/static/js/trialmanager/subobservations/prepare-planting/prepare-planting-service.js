(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.factory('PreparePlantingService', ['$http', 'serviceUtilities', 'studyContext',
		function ($http, serviceUtilities, studyContext) {
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/' + studyContext.studyId;

			var service = {};

			service.getPreparePlantingData = function (searchComposite, datasetId) {
				return $http({
					method: 'POST',
					url: `${BASE_URL}/datasets/${datasetId}/planting/preparation/search`,
					data: searchComposite
				}).then(successHandler, failureHandler);
			};

			return service;
		}
	]);
})();
