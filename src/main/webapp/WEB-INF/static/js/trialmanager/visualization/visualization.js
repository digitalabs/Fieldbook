(function () {
	'use strict';

	var visualizationModule = angular.module('visualization', ['r-package', 'datasets-api']);

	visualizationModule.factory('visualizationModalService', ['$uibModal', function ($uibModal) {

		var visualizationModalService = {};

		visualizationModalService.openModal = function (datasetId, observationUnitsSearch) {
			return $uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/visualization/visualizationModal.html',
				controller: 'visualizationModalController',
				size: 'md',
				resolve: {
					datasetId: function () {
						return datasetId;
					},
					observationUnitsSearch: function () {
						return observationUnitsSearch;
					}
				}
			});
		};

		visualizationModalService.showImageModal = function (imageUrl) {
			return $uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/visualization/showImageModal.html',
				controller: 'showImageModalController',
				size: 'lg',
				resolve: {
					imageUrl: function () {
						return imageUrl;
					}
				}
			});
		};

		return visualizationModalService;

	}]);

	visualizationModule.controller('visualizationModalController', ['$scope', '$q', '$uibModalInstance', 'rPackageService', 'datasetService',
		'datasetId', 'observationUnitsSearch', 'visualizationModalService',
		function ($scope, $q, $uibModalInstance, rPackageService, datasetService, datasetId, observationUnitsSearch, visualizationModalService) {

			$scope.rCalls = [];
			$scope.selection = {selectedRCall: null};
			$scope.variates = [];
			$scope.factors = [];
			$scope.regressionMethods = [{
				method: '\"auto\"',
				description: 'auto'
			}, {
				method: '\"lm\"',
				description: 'linear model'
			}, {
				method: '\"glm\"',
				description: 'generalized linear model'
			}, {
				method: '\"gam\"',
				description: 'generalized additive model'
			}, {
				method: '\"loess\"',
				description: 'loess regression'
			}]

			$scope.init = function () {
				var qplotPackageId = 3;
				rPackageService.getRCallsObjects(qplotPackageId).success(function (data) {
					$scope.rCalls = data;
					$scope.selection.selectedRCall = data[0];
				});
				datasetService.getColumns(datasetId, observationUnitsSearch.draftMode).then(function (columnsData) {
					// Only show numeric and categorical trait variables
					$scope.variates = columnsData.filter(column => column.variableType === 'TRAIT' && (column.dataTypeCode === 'C' || column.dataTypeCode === 'N'));
					$scope.factors = columnsData.filter(column => column.variableType !== 'TRAIT');
				});
			};

			$scope.cancel = function () {
				$uibModalInstance.close();
			};

			$scope.generate = function () {
				var rCall = angular.copy($scope.selection.selectedRCall);
				observationUnitsSearch.filter.filterColumns = getFilterColumns(rCall);
				if (rCall.description === 'Boxplot') {
					rCall.parameters.x = 'factor(' + rCall.parameters.x + ')';
				}
				datasetService.getObservationForVisualization(datasetId, JSON.stringify(observationUnitsSearch)).then(function (data) {
					rCall.parameters.data = JSON.stringify(data);
					return rPackageService.executeRCall(rCall.endpoint, rCall.parameters);
				}).then(function (response) {
					visualizationModalService.showImageModal(response.headers().location + 'graphics/1/svg');
				}).catch(function (errorResponse) {
					showErrorMessage('', 'There\'s an error in generating graphic plot. Please check the variables have data and have correct data type. ' + errorResponse.data);
				});
			};

			function getFilterColumns(rCall) {
				var filterColumns = [];
				filterColumns.push(rCall.parameters.x);
				if (rCall.description !== 'Histogram') {
					filterColumns.push(rCall.parameters.y);
				}
				return filterColumns;
			}

			$scope.init();

		}]);


	visualizationModule.controller('showImageModalController', ['$scope', '$uibModalInstance', 'imageUrl', function ($scope, $uibModalInstance, imageUrl) {

		$scope.imageUrl = imageUrl;

		$scope.cancel = function () {
			$uibModalInstance.close();
		};

	}]);

})();