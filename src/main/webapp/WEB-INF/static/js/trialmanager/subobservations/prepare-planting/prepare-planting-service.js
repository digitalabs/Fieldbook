(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.factory('PreparePlantingService', ['$http', 'serviceUtilities',
		function ($http, serviceUtilities) {
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var service = {};

			service.getPreparePlantingData = function () {
				// return $http({
				// 	method: 'GET',
				// 	url: '/bmsapi/...'
				// }).then(successHandler, failureHandler);

				return Promise.resolve();
			};

			return service;
		}
	]);
})();
