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

			var OBSERVATION_UNIT_ID = 8201;
			var PLOT_TYPES = {
				SCATTERPLOT: 'Scatterplot',
				BOXPLOT: 'Boxplot',
				HISTOGRAM: 'Histogram'
			};

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
					$scope.variates = columnsData.filter(column => column.variableType === 'TRAIT');
					$scope.factors = columnsData.filter(column => column.variableType !== 'TRAIT' && column.termId !== OBSERVATION_UNIT_ID);
				});
			};

			$scope.cancel = function () {
				$uibModalInstance.close();
			};

			$scope.generate = function () {

				if (hasRequiredFields($scope.selection.selectedRCall)) {
					var rCall = prepareParameters(angular.copy($scope.selection.selectedRCall));
					observationUnitsSearch.filter.filterColumns = getFilterColumns($scope.selection.selectedRCall);
					datasetService.getObservationForVisualization(datasetId, JSON.stringify(observationUnitsSearch)).then(function (data) {
						rCall.parameters.data = JSON.stringify(data);
						return rPackageService.executeRCall(rCall.endpoint, rCall.parameters);
					}).then(function (response) {
						visualizationModalService.showImageModal(response.headers().location + 'graphics/1/svg');
					}).catch(function (errorResponse) {
						showErrorMessage('', $.fieldbookMessages.errorPlotGraphGeneration);
					});
				} else {
					showErrorMessage('', $.fieldbookMessages.errorPlotGraphRequiredFields);
				}

			};

			function hasRequiredFields(rCall) {
				if (rCall.description === PLOT_TYPES.SCATTERPLOT) {
					return rCall.parameters.x && rCall.parameters.y && rCall.parameters.method;
				} else if (rCall.description === PLOT_TYPES.HISTOGRAM) {
					return rCall.parameters.x;
				} else if (rCall.description === PLOT_TYPES.BOXPLOT) {
					return rCall.parameters.x && rCall.parameters.y;
				}
			}

			function getFilterColumns(rCall) {

				// Only the variables selected from the UI should be included in the data that will be sent to OpenCPU.
				var filterColumns = [];
				filterColumns.push(rCall.parameters.x);
				if (rCall.description !== 'Histogram') {
					filterColumns.push(rCall.parameters.y);
				}
				return filterColumns;
			}

			function prepareParameters(rCall) {

				// Make sure that field names are wrapped in backticks (`) to escape the spaces in them if there's any.
				if (rCall.description === PLOT_TYPES.BOXPLOT) {
					// If plot graph is a Boxplot, we must wrap the field name with R 'factor' function
					// so that R will know that the field is a factor
					rCall.parameters.x = 'factor(`' + rCall.parameters.x + '`)';
				} else {
					rCall.parameters.x = '`' + rCall.parameters.x + '`';
				}

				if (rCall.description !== PLOT_TYPES.HISTOGRAM) {
					rCall.parameters.y = '`' + rCall.parameters.y + '`';
				}

				return rCall;
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