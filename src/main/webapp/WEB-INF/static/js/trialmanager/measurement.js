/* global angular */
(function () {
	'use strict';

	angular.module('manageTrialApp').controller('MeasurementsCtrl',
		['$rootScope', '$scope', 'TrialManagerDataService', '$uibModal', '$q', 'debounce', '$http', 'DTOptionsBuilder', 'DTColumnBuilder',
			'DTColumnDefBuilder', '$filter', 'derivedVariableService', 'datasetService', 'studyContext',
			function ($rootScope, $scope, TrialManagerDataService, $uibModal, $q, debounce, $http, DTOptionsBuilder, DTColumnBuilder,
					  DTColumnDefBuilder, $filter, derivedVariableService, datasetService, studyContext) {
				var DELAY = 1500; // 1.5 secs

				$scope.settings = TrialManagerDataService.settings.measurements;

				$scope.isHideDelete = false;
				$scope.updateOccurred = false;
				$scope.addVariable = true;
				$scope.isNewStudy = function () {
					return !studyContext.studyId;
				};

				$scope.initEnvironmentList = function () {
					if (!$scope.isNewStudy()) {
						$http.get('/Fieldbook/trial/measurements/instanceMetadata/' + studyContext.studyId).success(function (data) {
							$scope.environmentsList = data;
							$scope.selectedEnvironment = data[0];
							TrialManagerDataService.selectedEnviromentOnMeasurementTab = $scope.selectedEnvironment;
						});
					} else {
						$scope.environmentsList = [{}];
						$scope.selectedEnvironment = $scope.environmentsList[0];
					}
				};

				$scope.changeEnvironmentForMeasurementDataTable = function ($item, $model) {
					$('#measurement-table').DataTable().ajax.url('/Fieldbook/trial/measurements/plotMeasurements/' + studyContext.studyId + '/' +
						$item.instanceDbId).load();
					$scope.selectedEnvironment = $item;
					TrialManagerDataService.selectedEnviromentOnMeasurementTab = $scope.selectedEnvironment;
				};

				$scope.getListOfAdditionalColumns = function () {
					if (!$scope.settings.keys()) {
						return [];
					}
					return $filter('removeHiddenVariableFilter')($scope.settings.keys(), $scope.settings.vals());
				};

				$scope.previewMeasurements = function () {
					new BMS.Fieldbook.PreviewMeasurementsDataTable('#preview-measurement-table',
						encodeURIComponent(JSON.stringify($scope.getListOfAdditionalColumns())));
				};

				$scope.reloadMeasurements = function () {
					new BMS.Fieldbook.MeasurementsDataTable('#measurement-table',
						encodeURIComponent(JSON.stringify($scope.getListOfAdditionalColumns())));
				};

				if ($('body').hasClass('preview-measurements-only')) {
					$scope.previewMeasurements();
				}

				/* Watchers */
				$scope.$watch(function () {
					return TrialManagerDataService.settings.measurements;
				}, function (newValue) {
					if ($scope.settings !== newValue) {
						angular.copy(newValue, $scope.settings);
					}
				});

				$scope.$watch(function () {
						return TrialManagerDataService.applicationData.isGeneratedOwnDesign;
					}, function (newValue) {
						if (newValue === true) {
							$scope.updateOccurred = true;
							TrialManagerDataService.clearUnappliedChangesFlag();
							TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
							TrialManagerDataService.applicationData.isGeneratedOwnDesign = false;

							reloadMeasurementPage(0, $scope.getListOfAdditionalColumns());

						}

					}
				);

				/* Scope functions */
				$scope.beforeDelete = function (variableType, variableIds) {

					// Only check for measurement data if the study is already created.
					if (!$scope.isNewStudy()) {

						var deferred = $q.defer();

						derivedVariableService.hasMeasurementData(variableIds).then(function (response) {
							var dependencyVariableHasMeasurementData = response.data;

							// Check first if any of removed dependency variables has measurement data.
							if (dependencyVariableHasMeasurementData) {
								var modalInstance = $rootScope.openConfirmModal(removeVariableDependencyConfirmationText,
									environmentConfirmLabel);
								modalInstance.result.then(deferred.resolve);
							} else {
								// else, check if any of the selected variables for deletion has measurement data.
								datasetService.observationCount(studyContext.studyId, studyContext.measurementDatasetId, variableIds).then(function (response) {
									var count = response.headers('X-Total-Count');
									if (count > 0) {
										var modalInstance = $rootScope.openConfirmModal(measurementModalConfirmationText,
											environmentConfirmLabel);
										modalInstance.result.then(deferred.resolve);
									} else {
										deferred.resolve(true);
									}
								});
							}
						});

						return deferred.promise;

					} else {
						return $q.resolve(true);
					}
				};

				/* Event Handlers */
				$scope.$on('deleteOccurred', function () {
					$scope.updateOccurred = true;
					TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;

					reloadMeasurementPage(0, $scope.getListOfAdditionalColumns());

					$('body').addClass('measurements-traits-changed');
				});

				$scope.$on('previewMeasurements', function () {
					$('body').addClass('preview-measurements-only');
					$scope.previewMeasurements();
				});

				$scope.$on('onDeleteEnvironment', function (event, result) {
					// result object contains deletedEnvironmentIndex and result.deferred object that need to be resolved after the reload
					$scope.updateOccurred = true;
					TrialManagerDataService.clearUnappliedChangesFlag();
					TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;

					reloadMeasurementPage(result.deletedEnvironmentIndex, $scope.getListOfAdditionalColumns());
					$scope.environmentsList.pop($scope.environmentsList[result.deletedEnvironmentIndex]);
					$scope.selectedEnvironment = $scope.environmentsList[0];
				});

				$scope.$on('refreshEnvironmentListInMeasurementTable', function () {
					$scope.initEnvironmentList();
				});

				$scope.$on('variableAdded', function () {
					$scope.updateOccurred = true;
					TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;

					reloadMeasurementPage(0, $scope.getListOfAdditionalColumns());

					$('body').addClass('measurements-traits-changed');
				});

				/* Controller Utility functions */
				function reloadMeasurementPage(deletedEnvironmentIndex, columnsOrder) {
					deletedEnvironmentIndex = typeof deletedEnvironmentIndex === 'undefined' ? 0 : deletedEnvironmentIndex;

					var $body = $('body');
					var $measurementTable = $('#measurement-table');
					var $measurementContainer = $('#measurementsDiv');

					if ($measurementTable.length !== 0) {
						var addedData = '&columnOrders=' + encodeURIComponent(JSON.stringify(columnsOrder));
						var dataParam = 'variableList=' + TrialManagerDataService.settings.measurements.m_keys.concat(TrialManagerDataService.settings.selectionVariables.m_keys).join() +
							'&deletedEnvironment=' + deletedEnvironmentIndex + addedData;

						return $http.post('/Fieldbook/TrialManager/openTrial/load/dynamic/change/measurement', dataParam,
							{headers: {'Content-Type': 'application/x-www-form-urlencoded'}}).success(function (data) {
							//$measurementContainer.html(data);
							// TODO Change that global to the dirty study flag
							$body.data('needToSave', '1');
							//TODO Remove that global
							$body.data('columnReordered', columnsOrder.length !== 0 ? '1' : '0');
							if ($('body').hasClass('preview-measurements-only')) {
								$scope.previewMeasurements();
							} else {
								$scope.reloadMeasurements();
							}
						});
					}
				}

				$scope.initEnvironmentList();
			}]);
})();
