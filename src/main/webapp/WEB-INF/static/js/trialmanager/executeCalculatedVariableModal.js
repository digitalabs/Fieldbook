/*global angular, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('ExecuteCalculatedVariableModalCtrl',
		['$scope', 'TrialManagerDataService', '$http', function ($scope, TrialManagerDataService, $http) {

		$scope.settings = TrialManagerDataService.settings.environments;
		$scope.LOCATION_NAME_ID = 8190;
		$scope.TRIAL_INSTANCE_INDEX = 8170;

		$scope.init = function () {
			$scope.calculateVariableLocationForm.$setPristine();

			$scope.locationSelected = undefined;
			$scope.variableSelected = undefined;
			$scope.data = TrialManagerDataService.currentData.environments;

			$scope.variableListView = convertTraitsVariablesToListView(TrialManagerDataService.settings.measurements.m_keys);
			$scope.locationListView = convertToEnvironmentListView($scope.data.environments, $scope.LOCATION_NAME_ID, $scope.TRIAL_INSTANCE_INDEX);
		//	$scope.variableSelected = $scope.variableListView[0]; //TODO select the first Variable

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
			var calculateData = {
				variableId: $scope.variableSelected.cvTermId
				, geoLocationId: $scope.locationSelected.locationId
			};

			$http.post('/Fieldbook/DerivedVariableController/derived-variable/execute', JSON.stringify(calculateData))
				.then(function (data) {
					$('#executeCalculatedVariableModal').modal('hide');
					if (data.hasDataOverwrite === '1') {
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
					if (!response || !response.status || response.status === 500) {
						showErrorMessage('', ajaxGenericErrorMsg);
					} else if (response.status == 401) {
						bmsAuth.handleReAuthentication();
					} else if (response.status == 400 && response.data.errorMessage) {
						showErrorMessage('', response.data.errorMessage);
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

		// TODO extract method shared with other controllers
		// Converts the environments data (($scope.environments) for UI usage.
		function convertToEnvironmentListView(environments, preferredLocationVariable, trialInstanceIndex) {

			var environmentListView = [];
			angular.forEach(environments, function(environment) {
				environmentListView.push({ name: getPreferredEnvironmentName(environment, preferredLocationVariable)
					,trialInstanceNumber: environment.managementDetailValues[trialInstanceIndex]
				,locationId:environment.locationId});
			});
			return environmentListView;

			function getPreferredEnvironmentName(environment, preferredLocationVariable) {
				var preferredLocation = '';
				if ($scope.settings.managementDetails.vals()[$scope.LOCATION_NAME_ID] !== undefined) {
					//create a map for location dropdown values
					var locationMap = {};

					angular.forEach($scope.settings.managementDetails.vals()[$scope.LOCATION_NAME_ID].allValues, function(locationVariable) {
						locationMap[locationVariable.id] = locationVariable;
					});

					var locationId = 0;
					if (environment.managementDetailValues[$scope.LOCATION_NAME_ID] !== undefined) {
						locationId = isNaN(environment.managementDetailValues[$scope.LOCATION_NAME_ID]) ?
							environment.managementDetailValues[$scope.LOCATION_NAME_ID].id :
							environment.managementDetailValues[$scope.LOCATION_NAME_ID];
					}

					if (locationId !== 0) {
						preferredLocation = locationMap[locationId].name;
					}

				}

				var preferredLocationVariableName = preferredLocationVariable === $scope.LOCATION_NAME_ID ? preferredLocation
					: environment.managementDetailValues[preferredLocationVariable];
				return preferredLocationVariableName;
			}
		};
	}]);

	manageTrialApp.controller('ConfirmOverrideCalculatedVariableModalCtrl', ['$scope', '$http', function ($scope, $http) {

		$scope.goBack = function () {
			$http.get('/Fieldbook/ImportManager/revert/data')
				.then(function (data) {
					$scope.revertData();
					$('#confirmOverrideCalculatedVariableModal').modal('hide');
					$('#executeCalculatedVariableModal').modal('show');
				}, function (response) {
					if (!response || !response.status || response.status === 500) {
						showErrorMessage('', ajaxGenericErrorMsg);
					} else if (response.status == 401) {
						bmsAuth.handleReAuthentication();
					} else if (response.status == 400 && response.data.errorMessage) {
						showErrorMessage('', response.data.errorMessage);
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
