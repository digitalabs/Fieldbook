/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function() {
	'use strict';

	angular.module('manageTrialApp').controller('MeasurementsCtrl',
		['$scope', 'TrialManagerDataService', '$modal', '$q', 'debounce', '$http',
			function($scope, TrialManagerDataService, $modal, $q, debounce, $http) {

				$scope.settings = TrialManagerDataService.settings.measurements;

				$scope.$watch(function() {
					return TrialManagerDataService.settings.measurements;
				}, function(newValue) {
					if ($scope.settings !== newValue) {
						angular.copy(newValue, $scope.settings);
					}
				});

				$scope.deletedEnvironment = 0;

				$scope.$watch(function() {
					return TrialManagerDataService.isGeneratedOwnDesign;
				}, function(newValue) {
					if (newValue === true) {
						// update the measurement tab
						$scope.reloadMeasurementPage();
						TrialManagerDataService.clearUnappliedChangesFlag();
						TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;

						TrialManagerDataService.isGeneratedOwnDesign = false;
						$scope.updateOccurred = true;
					}

				}

				);

				$scope.$watch(function() {
					return TrialManagerDataService.deletedEnvironment;
				}, function(newValue) {

					// this scenario only covered the update of measurement table
					// when the user delete an environment for a existing trial with measurement data

					if (newValue !== 0 && TrialManagerDataService.trialMeasurement.hasMeasurement) {
						$scope.deletedEnvironment = TrialManagerDataService.deletedEnvironment;

						// update the measurement tab
						$scope.reloadMeasurementPage();
						TrialManagerDataService.clearUnappliedChangesFlag();
						TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;

						TrialManagerDataService.deletedEnvironment = 0;
						$scope.updateOccurred = true;
					}

				});

				$scope.beforeDelete = function(variableType, variableIds) {
					var deferred = $q.defer();

					$http.post('/Fieldbook/manageSettings/hasMeasurementData/' + variableType, variableIds, {cache: false})
						.success(function(data, status, headers, config) {
							if ('true' === data) {
								var modalInstance = $modal.open({
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

				$scope.isHideDelete = false;

				$scope.updateOccurred = false;

				$scope.addVariable = true;

				$scope.$on('deleteOccurred', function() {
					$scope.updateOccurred = true;

					$scope.reloadOnDebounce();

					TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;
				});

				$scope.$on('variableAdded', function() {
					$scope.updateOccurred = true;

					$scope.reloadOnDebounce();

					TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;
				});

				$scope.reloadMeasurementPage = function() {

					if ($('#measurement-table').length !== 0) {
						//we reload
						var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table', true);
						var addedData = '&columnOrders=' + encodeURIComponent(JSON.stringify(columnsOrder));

						$.ajax({
							url: '/Fieldbook/TrialManager/openTrial/load/dynamic/change/measurement',
							type: 'POST',
							data: 'traitsList=' + TrialManagerDataService.settings.measurements.m_keys + "&deletedEnvironment=" + $scope.deletedEnvironment + addedData,
							cache: false,
							success: function(html) {
								$('#measurementsDiv').html(html);
								$('body').data('needToSave', '1');
								$('body').data('columnReordered', columnsOrder.length != 0 ? '1' : '0');
							}
						});
					}
				};

				var DELAY = 1500; // 1.5 secs
				$scope.reloadOnDebounce = debounce($scope.reloadMeasurementPage, DELAY, false);

			}]);
		})();
