(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.factory('CalculateCOPService', ['$http', 'serviceUtilities', 'studyContext',
		function ($http, serviceUtilities, studyContext) {
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var service = {};

			service.exportStudy = function (studyRequest) {
				const url = 'http://ecs-services-496103597.us-east-1.elb.amazonaws.com/api/study-export/'
				return $http({
					method: 'POST',
					url: url,
					data: JSON.stringify(studyRequest),
					headers: {'Content-Type': 'application/x-www-form-urlencoded'},
					overrideAuthToken: true
				}).then(successHandler, failureHandler);
			};

			service.permissions = function (apiRequest) {
				const url = 'http://ecs-services-496103597.us-east-1.elb.amazonaws.com/api/brapi-permissions/'
				return $http({
					method: 'POST',
					url: url,
					data: JSON.stringify(apiRequest),
					headers: {'Content-Type': 'application/x-www-form-urlencoded'},
					overrideAuthToken: true
				}).then(successHandler, failureHandler);
			};

			service.grantProcessPermissions = function () {
				return 'brapi-permissions'
			};

			return service;

		}

	]);
})();
