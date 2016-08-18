/* global angular */
(function() {
	'use strict';

	angular.module('manageTrialApp').controller('MeasurementsCtrl',
		['$scope', 'TrialManagerDataService', '$uibModal', '$q', 'debounce', '$http',
			function($scope, TrialManagerDataService, $uibModal, $q, debounce, $http) {
				var DELAY = 1500; // 1.5 secs

				$scope.settings = TrialManagerDataService.settings.measurements;

				$scope.isHideDelete = false;
				$scope.updateOccurred = false;
				$scope.addVariable = true;
				$scope.numberOfEnvironments = TrialManagerDataService.currentData.environments.noOfEnvironments;

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
						.success(function(data) {
							if ('true' === data) {
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

						//we reload
						return TrialManagerDataService.reloadMeasurementAjax(dataParam).success(function(data) {
							$measurementContainer.html(data);
							$body.data('needToSave', '1');
							$body.data('columnReordered', columnsOrder.length !== 0 ? '1' : '0');
						});
					}
				}
			}]);
})();
