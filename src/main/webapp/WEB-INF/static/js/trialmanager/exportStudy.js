(function () {
	'use strict';


	var exportStudyModule = angular.module('export-study', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal']);

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

		exportStudyModalService.openExportStudyModal = function () {
			$uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/exportStudy/exportStudyModal.html',
				controller: "exportStudyCtrl",
				size: 'md'
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

	exportStudyModule.controller('exportStudyCtrl', ['$scope', '$uibModalInstance', function ($scope, $uibModalInstance) {

		$scope.cancel = function () {
			$uibModalInstance.dismiss();
		}

		$scope.export = function () {
			$uibModalInstance.close();
		}

	}]);


})();