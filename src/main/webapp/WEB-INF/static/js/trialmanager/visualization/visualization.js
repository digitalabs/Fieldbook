(function () {
	'use strict';

	var visualizationModule = angular.module('visualization', ['r-package']);

	visualizationModule.factory('visualizationModalService', ['$uibModal', function ($uibModal) {

		var visualizationModalService = {};

		visualizationModalService.openModal = function (columnsData) {
			return $uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/visualization/visualizationModal.html',
				controller: 'visualizationModalController',
				size: 'md',
				resolve: {
					columnsData: function () {
						return columnsData
					}
				}
			});
		};

		return visualizationModalService;

	}]);

	visualizationModule.controller('visualizationModalController', ['$scope', '$q', '$uibModalInstance', 'rPackageService', 'columnsData',
		function ($scope, $q, $uibModalInstance, rPackageService, columnsData) {

			$scope.rCalls = [];
			$scope.selection = {selectedRCall: null};
			$scope.variates = columnsData.filter(column => column.variableType === 'TRAIT');
			$scope.factors = columnsData.filter(column => column.variableType !== 'TRAIT');
			$scope.regressionMethods = [{
				method: 'auto',
				description: 'auto'
			}, {
				method: 'lm',
				description: 'linear model'
			}, {
				method: 'glm',
				description: 'generalized linear model'
			}, {
				method: 'gam',
				description: 'generalized additive model'
			}, {
				method: 'loess',
				description: 'loess regression'
			}]

			$scope.init = function () {

				var qplotPackageId = 3;
				rPackageService.getRCallsObjects(qplotPackageId).success(function (data) {
					$scope.rCalls = data;
					$scope.selection.selectedRCall = data[0];
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