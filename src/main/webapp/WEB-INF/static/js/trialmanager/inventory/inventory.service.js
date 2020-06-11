(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.factory('InventoryService', ['$http', 'serviceUtilities', 'studyContext',
		function ($http, serviceUtilities, studyContext) {
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName;

			var service = {};

			service.queryUnits = function () {
				return $http.get(`${BASE_URL}/inventory-units`)
					.then(successHandler, failureHandler);
			}

			return service;
		}
	]);
})();
