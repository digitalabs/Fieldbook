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

		exportStudyModalService.showAlertMessage = function (title, message) {
			// Call the global function to show alert message
			showAlertMessage(title, message);
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
					exportStudyModalService.openExportStudyModal($scope.selected.datasetId);
				}

			};

		}]);

	exportStudyModule.controller('exportStudyCtrl', ['datasetId', '$scope', '$uibModalInstance', 'datasetService', 'exportStudyModalService',
		'TrialManagerDataService', 'fileDownloadHelper', function (datasetId, $scope, $uibModalInstance, datasetService, exportStudyModalService, TrialManagerDataService, fileDownloadHelper) {

			var ctrl = this;

			ctrl.selectedExportFormatId = '1';
			ctrl.selectedCollectionOrderId = '1';

			$scope.exportFormats = [{itemId: '1', name: 'CSV'}];
			$scope.collectionOrders = [
				{itemId: '1', name: 'Plot Order'},
				{itemId: '2', name: 'Serpentine - Along Rows'},
				{itemId: '3', name: 'Serpentine - Along columns'}
			];

			$scope.instances = [];
			$scope.selectedInstances = {};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.export = function () {

				var instanceIds = [];

				Object.keys($scope.selectedInstances).forEach(function (instanceId) {
					var isSelected = $scope.selectedInstances[instanceId];
					if (isSelected) {
						instanceIds.push(instanceId);
					}
				});

				datasetService.exportDataset(datasetId, instanceIds, ctrl.selectedCollectionOrderId).then(function (response) {
					var fileName = fileDownloadHelper.getFileNameFromResponseContentDisposition(response);
					fileDownloadHelper.save(response.data, fileName);
					$uibModalInstance.close();
				});
			};

			ctrl.init = function () {
				datasetService.getDatasetInstances(datasetId).then(function (instances) {

					$scope.instances = instances;

					var noOfEnvironments = parseInt(TrialManagerDataService.currentData.environments.noOfEnvironments);

					if (noOfEnvironments !== $scope.instances.length) {
						exportStudyModalService.showAlertMessage('', 'Some instances do not have sub observation units associated and can not be' +
							' selected to export.');
					}
				});
			};

			ctrl.init();

		}]);

})();