/* global angular */
(function() {
	'use strict';

	angular.module('manageTrialApp').controller('MeasurementsCtrl',
		['$scope', 'TrialManagerDataService', '$uibModal', '$q', 'debounce', '$http', 'DTOptionsBuilder', 'DTColumnBuilder',
		'DTColumnDefBuilder',
			function($scope, TrialManagerDataService, $uibModal, $q, debounce, $http, DTOptionsBuilder, DTColumnBuilder, DTColumnDefBuilder) {
				var DELAY = 1500; // 1.5 secs
				var studyId = $('#studyId').val();

				$scope.settings = TrialManagerDataService.settings.measurements;

				$scope.isHideDelete = false;
				$scope.updateOccurred = false;
				$scope.addVariable = true;
				// controls if the user have chosen to display preview
				$scope.isDisplayPreview = false;
				$scope.isNewStudy = function() {
					return ($('#studyId').val() === '');
				};

				$scope.initEnvironmentList = function() {
					if (!$scope.isNewStudy()) {
						$http.get('/Fieldbook/Common/addOrRemoveTraits/instanceMetadata/' + studyId).success(function(data) {
							$scope.environmentsList = data;
							$scope.selectedEnvironment = data[0];
						});
					} else {
						$scope.environmentsList = [{}];
						$scope.selectedEnvironment = $scope.environmentsList[0];
					}
				};

				$scope.changeEnvironmentForMeasurementDataTable = function($item, $model) {
					$('#measurement-table').DataTable().ajax.url('/Fieldbook/Common/addOrRemoveTraits/plotMeasurements/' + studyId + '/' +
						$item.instanceDbId).load();
				};

				$scope.previewMeasurements = function() {
					$scope.isDisplayPreview = true;
					// The jquery is out of sync with angular changes, set this hack timeout, it will be removed once the table is in
					// angular
					// the jQuery datatable for preview needs a list of measurement variables used as columns, it gets them from the DOM
					// the preview datatable could not start its construction until all variables in 'thead tr th' are loaded with
					// arrangeMeasurementVariables() function
					// FIXME this should be reimplemented properly
					$http.get('/Fieldbook/TrialManager/createTrial/measurements/variables').success(function(data) {
						debounce(function() {
							new BMS.Fieldbook.PreviewMeasurementsDataTable('#preview-measurement-table', data);
						}, DELAY, false)();
					});

				};

				/* Watchers */
				$scope.$watch(function() {
					return TrialManagerDataService.settings.measurements;
				}, function(newValue) {
					if ($scope.settings !== newValue) {
						angular.copy(newValue, $scope.settings);
					}
				});

				$scope.$watch(function() {
					return TrialManagerDataService.applicationData.isGeneratedOwnDesign;
				}, function(newValue) {
					if (newValue === true) {
						$scope.updateOccurred = true;
						TrialManagerDataService.clearUnappliedChangesFlag();
						TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
						TrialManagerDataService.applicationData.isGeneratedOwnDesign = false;

						debounce(reloadMeasurementPage, DELAY, false)();

					}

				}

				);

				/* Scope functions */
				$scope.beforeDelete = function(variableType, variableIds) {
					var deferred = $q.defer();

					$http.post('/Fieldbook/manageSettings/hasMeasurementData/' + variableType, variableIds, {cache: false})
						.success(function(hasMeasurementData) {
							if (hasMeasurementData) {
								var modalInstance = $uibModal.open({
									templateUrl: '/Fieldbook/static/angular-templates/confirmModal.html',
									controller: 'ConfirmModalController',
									resolve: {
										MODAL_TITLE: function() {
											return modalConfirmationTitle;
										},
										MODAL_TEXT: function() {
											return measurementModalConfirmationText;
										},
										CONFIRM_BUTTON_LABEL: function() {
											return environmentConfirmLabel;
										}
									}
								});

								modalInstance.result.then(deferred.resolve);

							} else {
								deferred.resolve(true);
							}
						});

					return deferred.promise;
				};

				/* Event Handlers */
				$scope.$on('deleteOccurred', function() {
					$scope.updateOccurred = true;
					TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;

					debounce(reloadMeasurementPage, DELAY, false)();

					$scope.isDisplayPreview = false;
					$('body').addClass('preview-measurements-only');
					$('body').addClass('measurements-traits-changed');
				});

				$scope.$on('onDeleteEnvironment', function(event, result) {
					// result object contains deletedEnvironmentIndex and result.deferred object that need to be resolved after the reload
					$scope.updateOccurred = true;
					TrialManagerDataService.clearUnappliedChangesFlag();
					TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;

					debounce(function() {
						reloadMeasurementPage(result.deletedEnvironmentIndex).then(function() {
							result.deferred.resolve();
						});
					}, DELAY, false)();
				});

				$scope.$on('variableAdded', function() {
					$scope.updateOccurred = true;
					TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;

					debounce(reloadMeasurementPage, DELAY, false)();

					$scope.isDisplayPreview = false;
                    $('body').addClass('preview-measurements-only');
                    $('body').addClass('measurements-traits-changed');
				});

				/* Controller Utility functions */
				function reloadMeasurementPage(deletedEnvironmentIndex) {
					deletedEnvironmentIndex = typeof deletedEnvironmentIndex === 'undefined' ? 0 : deletedEnvironmentIndex;

					var $body = $('body');
					var $measurementTable = $('#measurement-table');
					var $measurementContainer = $('#measurementsDiv');

					if ($measurementTable.length !== 0) {
						var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table', true);
						var addedData = '&columnOrders=' + encodeURIComponent(JSON.stringify(columnsOrder));
						var dataParam = 'traitsList=' + TrialManagerDataService.settings.measurements.m_keys +
							'&deletedEnvironment=' + deletedEnvironmentIndex + addedData;

						return $http.post('/Fieldbook/TrialManager/openTrial/load/dynamic/change/measurement', dataParam,
                            {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}).success(function(data) {
							//$measurementContainer.html(data);
							// TODO Change that global to the dirty study flag
							$body.data('needToSave', '1');
							//TODO Remove that global
							$body.data('columnReordered', columnsOrder.length !== 0 ? '1' : '0');
						});
					}
				}

				$scope.initEnvironmentList();
			}]);
})();
