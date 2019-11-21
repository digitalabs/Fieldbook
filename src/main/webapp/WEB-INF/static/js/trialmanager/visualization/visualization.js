(function () {
	'use strict';

	var visualizationModule = angular.module('visualization', ['r-package']);

	visualizationModule.factory('visualizationModalService', ['$uibModal', function ($uibModal) {

		var visualizationModalService = {};

		visualizationModalService.openModal = function () {
			return $uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/visualization/visualizationModal.html',
				controller: 'visualizationModalController',
				size: 'md'
			});
		};

		return visualizationModalService;

	}]);

	visualizationModule.controller('visualizationModalController', ['$scope', '$q', '$uibModalInstance', 'rPackageService',
		function ($scope, $q, $uibModalInstance, rPackageService) {

			$scope.rCalls = [];
			$scope.selectedRCall = {selected: null};

			$scope.init = function () {

				var qplotPackageId = 3;
				rPackageService.getRCallsObjects(qplotPackageId).success(function (data) {
					$scope.rCalls = data;
					$scope.selectedRCall.selected = data[0];
				});

			};

			$scope.cancel = function () {
				$uibModalInstance.close();
			};

			$scope.generate = function () {
				$uibModalInstance.close();
			};

			$scope.init();

		}]);

})();