/*global angular, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('ExecuteCalculatedVariableModalCtrl',
		['$scope', 'TrialManagerDataService', '$http', 'environmentService', function ($scope, TrialManagerDataService, $http, environmentService) {

			$scope.instances = [];
			$scope.selectedInstances = {};
			$scope.isEmptySelection = false;

		$scope.init = function () {
			$scope.calculateVariableLocationForm.$setPristine();
			$scope.environmentSelected = undefined;
			$scope.variableSelected = undefined;

			$scope.variableListView = convertTraitsVariablesToListView(TrialManagerDataService.settings.measurements.m_keys);
			environmentService.getEnvironments().then(function (environmentDetails) {
				$scope.instances = environmentDetails;
			});

		};

		$scope.proceedExecution = function () {
			$('.import-study-data').data('data-import', '1');
			$('body').addClass('import-preview-measurements');
			var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
			new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));
			$('.fbk-discard-imported-data').removeClass('fbk-hide');
			showSuccessfulMessage('', 'Calculated values for ' + $scope.variableSelected.name + ' were added successfully.');
		};

		$scope.execute = function () {
			var geoLocationIds = [];

			Object.keys($scope.selectedInstances).forEach(function (instanceId) {
				var isSelected = $scope.selectedInstances[instanceId];
				if (isSelected) {
					var instanceSelected = $scope.instances.find(function(instance) {
						return instance.instanceNumber === parseInt(instanceId);
					});
					geoLocationIds.push(instanceSelected.instanceDbId);
				}
			});

			var calculateData = {
				variableId: $scope.variableSelected.cvTermId
				, geoLocationId: geoLocationIds
			};

			$http.post('/Fieldbook/DerivedVariableController/derived-variable/execute', JSON.stringify(calculateData))
				.then(function (response) {
					$('#executeCalculatedVariableModal').modal('hide');
					if (response.data && response.data.inputMissingData) {
						showAlertMessage('', response.data.inputMissingData, 15000);
					}
					if (response.data && response.data.hasDataOverwrite) {
						$('#confirmOverrideCalculatedVariableModal').modal({backdrop: 'static', keyboard: true});

						// Add hide listener to confirmOverrideCalculatedVariableModal
						$('#confirmOverrideCalculatedVariableModal').one('hidden.bs.modal', function (e) {
							// When the confirmOverrideCalculatedVariableModal is closed, remove the bs.modal data
							// so that the modal content is refreshed when it is opened again.
							$(e.target).removeData('bs.modal');
						});
						angular.element('#confirmOverrideCalculatedVariableModal').scope();
					} else {
						$scope.proceedExecution();
					}
				}, function (response) {
					if (response.status == 401) {
						bmsAuth.handleReAuthentication();
					} else if (response.data.errorMessage) {
						showErrorMessage('', response.data.errorMessage);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});

		};

		function convertTraitsVariablesToListView(traitIdList) {
			var variableListView = [];
			angular.forEach(traitIdList, function (id) {
				var variable = TrialManagerDataService.settings.measurements.m_vals[id].variable;
				if (variable.formula) {
					variableListView.push({name: variable.name, cvTermId: variable.cvTermId});
				}
			});
			return variableListView;
		};

	}]);

	manageTrialApp.controller('ConfirmOverrideCalculatedVariableModalCtrl', ['$scope', '$http', function ($scope, $http) {

		$scope.goBack = function () {
			$http.get('/Fieldbook/ImportManager/revert/data')
				.then(function (response) {
					$scope.revertData();
					$('#confirmOverrideCalculatedVariableModal').modal('hide');
					$('#executeCalculatedVariableModal').modal('show');
				}, function (response) {
					if (response.status == 401) {
						bmsAuth.handleReAuthentication();
					} else if (response.data.errorMessage) {
						showErrorMessage('', response.data.errorMessage);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});

		};

		$scope.revertData = function () {
			$('body').removeClass('import-preview-measurements');
			showSuccessfulMessage('', 'Discarded data successfully');

			if ($('#measurement-table').length !== 0 && $('#measurement-table').dataTable()) {
				$('#measurement-table').dataTable().fnAdjustColumnSizing();
			}
			$('#review-out-of-bounds-data-list').hide();
			$('.fbk-discard-imported-data').addClass('fbk-hide');
			$('.import-study-data').data('data-import', '0');
		};

		$scope.proceed = function () {
			$('#confirmOverrideCalculatedVariableModal').modal('hide');
			angular.element('#executeCalculatedVariableModal').scope().proceedExecution();
		};

	}]);
})();
