(function () {
	'use strict';

	var visualizationModule = angular.module('visualization', ['r-package']);

	visualizationModule.factory('visualizationModalService', ['$uibModal', function ($uibModal) {

		var visualizationModalService = {};

		visualizationModalService.openModal = function () {
			
		};

		return visualizationModalService;

	}]);

	visualizationModule.controller('visualizationModalController', ['$scope', '$q', '$uibModalInstance', 'rPackageService',
		function ($scope, $q, $uibModalInstance, rPackageService) {

		}]);

})();