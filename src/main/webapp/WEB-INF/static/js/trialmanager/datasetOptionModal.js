(function () {
	'use strict';

	var datasetOptionModal = angular.module('datasetOptionModal', ['ui.bootstrap', 'ngSanitize', 'ui.select', 'datasets-api']);

	datasetOptionModal.directive('datasetOptionModal', ['datasetService', function (datasetService) {
		return {
			restrict: 'E',
			require: '?ngModel',
			scope: {
				title: '=',
				message: '=',
				selected: '=',
				onContinue: '&'
			},

			templateUrl: '/Fieldbook/static/angular-templates/datasetOptionModal.html',
			controller: function ($scope) {

				var ctrl = this;

				$scope.datasets = [];

				$scope.cancel = function () {
					$scope.$parent.$dismiss();
				};

				$scope.continue = function () {
					$scope.onContinue();
					$scope.$parent.$close();
				};

				$scope.getDatasetType = datasetService.getDatasetType;

				ctrl.init = function () {
					datasetService.getPlotAndSubobservationDatasets().then(function (datasets) {
						$scope.datasets = datasets;
					});
				}

				ctrl.init();

			}
		};
	}]);
})();