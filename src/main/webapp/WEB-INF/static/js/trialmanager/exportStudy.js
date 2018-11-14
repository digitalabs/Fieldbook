(function () {
	'use strict';


	var exportStudyModule = angular.module('export-study', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	exportStudyModule.factory('exportStudyModalService', ['$uibModal', function ($uibModal) {

		var exportStudyModalService = {};

		exportStudyModalService.openDatasetOptionModal = function () {
			$uibModal.open({
				template: '<dataset-option-modal title="title" message="message"' +
				'selected="selected" on-continue="showExportOptions()"></dataset-option-modal>',
				controller: 'exportDatasetOptionCtrl',
				size: 'md'
			});
		};

		exportStudyModalService.openExportStudyModal = function (datasetId) {
			$uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/exportStudy/exportStudyModal.html',
				controller: "exportStudyCtrl",
				size: 'md',
				resolve: {
					datasetId: function () {
						return datasetId;
					}
				},
				controllerAs: 'ctrl'
			});
		};

		exportStudyModalService.redirectToOldExportModal = function () {
			// Call the global function to show the old export study modal
			showExportOptions();
		};

		return exportStudyModalService;

	}]);

	exportStudyModule.controller('exportDatasetOptionCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'exportStudyModalService',
		'datasetService', function ($scope, $uibModal, $uibModalInstance, studyContext, exportStudyModalService, datasetService) {

			$scope.title = 'Export study book';
			$scope.message = 'Please choose the dataset you would like to export:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};

			$scope.showExportOptions = function () {

				if ($scope.measurementDatasetId === $scope.selected.datasetId) {
					// If the selected dataset is a PLOT OBSERVATION, then use the old
					// export study modal (non-Angular)
					exportStudyModalService.redirectToOldExportModal();
				} else {
					exportStudyModalService.openExportStudyModal();
				}

			};

		}]);

	exportStudyModule.controller('exportStudyCtrl', ['datasetId', '$scope', '$uibModalInstance', 'datasetService',
		function (datasetId, $scope, $uibModalInstance, datasetService) {

			var ctrl = this;

			ctrl.selectedExportFormatId = '1';
			ctrl.selectedCollectionOrderId = '1';

			$scope.exportFormats = [{ itemId: '1' , name: 'CSV'}];
			$scope.collectionOrders = [
				{ itemId: '1' , name: 'Plot Order'},
				{ itemId: '2' , name: 'Serpentine - Along Rows'},
				{ itemId: '3' , name: 'Serpentine - Along columns'}
			];

			$scope.instances = [];
			$scope.selectedInstances = {};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.export = function () {
				console.log(ctrl.selectedCollectionOrderId);
				$uibModalInstance.close();
			};

			ctrl.init = function () {
				datasetService.getDatasetInstances(datasetId).then(function (instances) {
					$scope.instances = instances;
				});
			};

			ctrl.init();

		}]);

})();