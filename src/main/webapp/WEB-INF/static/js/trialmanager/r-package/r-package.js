(function () {
	'use strict';

	var rPackageModule = angular.module('r-package', []);

	rPackageModule.factory('rPackageService', ['$http', function ($http) {

		var rPackageService = {};

		rPackageService.getRCallsObjects = function (packageId) {
			return $http({
				method: 'GET',
				url: '/bmsapi/r-packages/' + +packageId + '/r-calls'
			});
		};

		rPackageService.executeRCall = function (url, parameters) {
			delete $http.defaults.headers["x-auth-token"];

			return $http({
				method: 'POST',
				url: url,
				data: $.param(parameters),
				headers: {'Content-Type': 'application/x-www-form-urlencoded'},
				override: true
			});
		};

		return rPackageService;

	}]);


})();