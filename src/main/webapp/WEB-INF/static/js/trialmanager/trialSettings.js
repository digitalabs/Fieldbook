/*global angular*/

(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('TrialSettingsCtrl', ['$scope', 'TrialManagerDataService', '_', '$filter', 'VARIABLE_TYPES', 'datasetService', 'studyContext', '$q', function ($scope, TrialManagerDataService, _, $filter, VARIABLE_TYPES, datasetService, studyContext, $q) {

		$scope.data = TrialManagerDataService.currentData.trialSettings;
		$scope.addVariable = true;

		$scope.managementDetails = TrialManagerDataService.settings.trialSettings;

        $scope.managementDetailOptions = {
			selectAll: false
		};

		$scope.selectionVariables = TrialManagerDataService.settings.selectionVariables;

        $scope.selectionVariablesOptions = {
            selectAll: false
        };

		$scope.doSelectAll = function(variables, options) {

			var filteredVariables = $filter('removeHiddenAndDeletablesVariableFilter')(variables.keys(), variables.vals());

			_.each(filteredVariables, function(cvTermID) {
                variables.val(cvTermID).isChecked = options.selectAll;
			});

		};

		$scope.removeSettings = function (variableType, variables, options) {
			var checkedVariableTermIds = $scope.retrieveCheckedVariableTermIds(variables);

			var promise = $scope.validateRemoveVariable(checkedVariableTermIds, observationVariableDeleteConfirmationText);

			promise.then(function (doContinue) {
				if (doContinue) {
					TrialManagerDataService.removeSettings(variableType, variables).then(function (data) {
						_(data).each(function (ids) {
							delete $scope.data.userInput[ids];
						});

						options.selectAll = false;
					});
				}
			});
		};

		$scope.retrieveCheckedVariableTermIds = function(_settings) {
			var checkedCvtermIds = _.pairs(_settings.vals())
				.filter(function(val) {
					return _.last(val).isChecked;
				})
				.map(function(val) {
					return parseInt(_.first(val));
				});
			return checkedCvtermIds;
		};

		$scope.validateRemoveVariable = function (deleteVariables, message) {
			var deferred = $q.defer();
			if (deleteVariables.length != 0) {
				datasetService.observationCount(studyContext.measurementDatasetId, deleteVariables).then(function (response) {
					var count = response.headers('X-Total-Count');
					if (count > 0) {
						var modalInstance = $scope.openConfirmModal(message,
							environmentConfirmLabel);
						modalInstance.result.then(deferred.resolve);
					} else {
						deferred.resolve(true);
					}
				});
			}
			return deferred.promise;
		};

		$scope.onAddVariable = function (result, options) {
			options.selectAll = false;
		};

		$scope.managementDetailsSize = function() {
			return $scope.managementDetails.length();
		};

	}]);

})();
