(function () {
	'use strict';

	var importStudyModule = angular.module('import-study', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	importStudyModule.factory('importStudyModalService', ['$uibModal',
		function ($uibModal) {

			var importStudyModalService = {};

			importStudyModalService.openDatasetOptionModal = function () {
				$uibModal.open({
					template: '<dataset-option-modal title="title" message="message"' +
						'selected="selected" on-continue="showImportOptions()"></dataset-option-modal>',
					controller: 'importDatasetOptionCtrl',
					size: 'md'
				});
			};

			importStudyModalService.openImportStudyModal = function (datasetId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/importStudy/ImportStudyModal.html',
					controller: "importStudyCtrl",
					size: 'md',
					resolve: {
						datasetId: function () {
							return datasetId;
						}
					},
					controllerAs: 'ctrl'
				});
			};

			importStudyModalService.redirectToOldImportModal = function () {
				// Call the global function to show the old import study modal
				setTimeout(function () {
					showImportOptions();
				});
			};

			importStudyModalService.showAlertMessage = function (title, message) {
				// Call the global function to show alert message
				showAlertMessage(title, message);
			};

			importStudyModalService.showWarningMessage = function (header, title, warnings, question, confirmButtonLabel, cancelButtonLabel) {
				var modalInstance = $uibModal.open({
					animation: true,
					templateUrl: '/Fieldbook/static/angular-templates/warningModal.html',
					controller: function ($scope, $uibModalInstance) {
						$scope.header = header;
						$scope.title = title;
						$scope.warnings = warnings;
						$scope.question = question;
						$scope.confirmButtonLabel = confirmButtonLabel;
						$scope.cancelButtonLabel = cancelButtonLabel;

						$scope.confirm = function () {
							$uibModalInstance.close(true);
						};

						$scope.cancel = function () {
							$uibModalInstance.close(false);
						};
					}
				});
				return modalInstance;
			};

			return importStudyModalService;

		}]);

	importStudyModule.controller('importDatasetOptionCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'importStudyModalService',
		function ($scope, $uibModal, $uibModalInstance, studyContext, importStudyModalService) {

			$scope.title = 'Import measurements';
			$scope.message = 'Please choose the dataset you would like to import:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};

			$scope.showImportOptions = function () {

				if ($scope.measurementDatasetId === $scope.selected.datasetId) {
					importStudyModalService.redirectToOldImportModal();
				} else {
					importStudyModalService.openImportStudyModal($scope.selected.datasetId);
				}

			};

		}]);

	importStudyModule.controller('importStudyCtrl', ['datasetId', '$scope', '$rootScope', '$uibModalInstance', 'datasetService', 'importStudyModalService',
		'TrialManagerDataService',
		function (datasetId, $scope, $rootScope, $uibModalInstance, datasetService, importStudyModalService) {

			$scope.title = 'Import measurements';
			$scope.file = '';
			$scope.importedData = '';
			var ctrl = this;

			ctrl.importFormats = [{itemId: '1', name: 'CSV', extension: '.csv'}];
			ctrl.format = {selected: ctrl.importFormats[0].extension};

			$scope.backToDatasetOptionModal = function () {
				$uibModalInstance.close();
				importStudyModalService.openDatasetOptionModal();
			};

			$scope.clearSelectedFile = function () {
				$scope.file = '';
				$scope.importedData = '';
			};

			$scope.submitImport = function () {
				$scope.importMeasurements(true);
			};

			$scope.importMeasurements = function (processWarnings) {
				datasetService.importObservations(datasetId, $scope.importedData, processWarnings).then(function () {
					displaySaveSuccessMessage('page-message', 'Your data was successfully imported and saved.');
					$scope.reloadObservations();
					$scope.close();
				}, function (response) {
					if (response.status == 401) {
						bmsAuth.handleReAuthentication();
					} else if (response.status == 400) {
						showErrorMessage('', response.data.errors[0].message);
					} else if (response.status == 412) {
						ctrl.showConfirmModal(response.data.errors);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});
			};

			$scope.close = function () {
				$uibModalInstance.close();
			};

			$scope.reloadObservations = function () {
				var scope = angular.element(document.getElementById("mainApp")).scope();
				scope.navigateToSubObsTab(datasetId);
			};

			ctrl.showConfirmModal = function (warnings) {
				$uibModalInstance.close();
				var warningMessages = [];
				for (var i = 0; i < warnings.length; i++) {
					warningMessages.push(warnings[i].message);
				}

				var modalWarningMessage = importStudyModalService.showWarningMessage('Confirmation', 'Some observations were found in the imported file:', warningMessages, 'Would you like to proceed with the import ?', 'Proceed', 'Back');
				modalWarningMessage.result.then(function (shouldContinue) {
					if (shouldContinue) {
						$scope.importMeasurements(false);
					} else {
						importStudyModalService.openImportStudyModal(datasetId);
					}
				});
			};

			ctrl.init = function () {
				$scope.file = '';
				$scope.importedData = '';
				//$scope.reloadObservations();
			};

			ctrl.init();

		}])
		.directive('importSheetJs', function () {
			return {
				restrict: 'AE',
				scope: {
					importedFile: '=',
					importedData: '='
				},
				link: function (scope, elem, attrs) {
					elem.on('change', function (changeEvent) {
						var reader = new FileReader();

						reader.onload = function (e) {
							/* read workbook */
							var bstr = e.target.result;
							var wb = XLSX.read(bstr, {type: 'binary', sheetStubs: true});

							/* grab first sheet */
							var wsname = wb.SheetNames[0];
							var ws = wb.Sheets[wsname];

							/* grab first row and generate column headers */
							var aoa = XLSX.utils.sheet_to_json(ws, {header: 1, raw: false});
							var cols = [];
							for (var i = 0; i < aoa[0].length; ++i) cols[i] = {field: aoa[0][i]};

							/* replace empty spaces by double quote */
							for (var r = 1; r < aoa.length; ++r) {
								if (cols.length == aoa[r].length) {
									for (i = 0; i < aoa[r].length; ++i) {
										if (aoa[r][i] == null || aoa[r][i] == undefined) {
											aoa[r][[i]] = "";
										}

									}
								}
							}

							/* update scope */
							scope.$apply(function () {
								var length = 30;
								scope.importedData = aoa;
								scope.importedFile = changeEvent.target.files[0];
								scope.importedFile.abbrName = scope.importedFile.name;

								if (scope.importedFile.name.length > length) {
									scope.importedFile.abbrName = scope.importedFile.abbrName.substring(0, length) + '...';
								}
							});
						};
						reader.readAsBinaryString(changeEvent.target.files[0]);
					});
				}
			};
		});
})();
