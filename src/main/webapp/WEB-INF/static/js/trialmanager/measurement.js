/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function() {
	'use strict';

	angular.module('manageTrialApp').controller('MeasurementsCtrl',
		['$scope', 'TrialManagerDataService', '$uibModal', '$q', 'debounce', '$http',
			function($scope, TrialManagerDataService, $uibModal, $q, debounce, $http) {
				$scope.settings = TrialManagerDataService.settings.measurements;

				$scope.isHideDelete = false;
				$scope.updateOccurred = false;
				$scope.addVariable = true;

				/* Watchers */
				$scope.$watch(function() {
					return TrialManagerDataService.settings.measurements;
				}, function(newValue) {
					if ($scope.settings !== newValue) {
						angular.copy(newValue, $scope.settings);
					}
				});

				$scope.$watch(function() {
					return TrialManagerDataService.isGeneratedOwnDesign;
				}, function(newValue) {
					if (newValue === true) {
						// update the measurement tab
						reloadMeasurementPage();

						TrialManagerDataService.isGeneratedOwnDesign = false;
						$scope.updateOccurred = true;
					}

				}

				);

				/* Scope functions */
				$scope.beforeDelete = function(variableType, variableIds) {
					var deferred = $q.defer();

					$http.post('/Fieldbook/manageSettings/hasMeasurementData/' + variableType, variableIds, {cache: false})
						.success(function(data, status, headers, config) {
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

					reloadOnDebounce();

					TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;
				});

				$scope.$on('onDeleteEnvironment',function(event, deletedEnvironmentIndex) {
					reloadMeasurementPage(deletedEnvironmentIndex);
					$scope.updateOccurred = true;
				});

				$scope.$on('variableAdded', function() {
					$scope.updateOccurred = true;

					reloadOnDebounce();

					TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;
				});

				/* Controller Utility functions */
				var DELAY = 1500; // 1.5 secs
				var reloadOnDebounce = debounce(reloadMeasurementPage, DELAY, false);

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
						TrialManagerDataService.reloadMeasurementAjax(dataParam).success(function(data) {
							$measurementContainer.html(data);
							$body.data('needToSave', '1');
							$body.data('columnReordered', columnsOrder.length != 0 ? '1' : '0');
						});
					}
				};
			}]);
		})();
