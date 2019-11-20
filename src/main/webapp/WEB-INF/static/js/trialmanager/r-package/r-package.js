(function () {
	'use strict';

	var rPackageModule = angular.module('r-package', []);

	rPackageModule.factory('rPackageService', ['$http', function ($http) {

		var rPackageService = {};

		rPackageService.getRCallsObjects = function (packageId) {
			return $http({
				method: 'GET',
				url: '/bmsapi/r-packages/' + +packageId + '/r-calls',
				headers: {'x-auth-token': JSON.parse(localStorage["bms.xAuthToken"]).token}
			});
		};

		return rPackageService;

	}]);


})();