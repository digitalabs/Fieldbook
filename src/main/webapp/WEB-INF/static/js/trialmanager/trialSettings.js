/*global angular*/

(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('TrialSettingsCtrl', ['$scope', 'TrialManagerDataService', '_', '$filter','VARIABLE_TYPES', function($scope, TrialManagerDataService, _, $filter,VARIABLE_TYPES) {

		$scope.data = TrialManagerDataService.currentData.trialSettings;
		$scope.addVariable = true;

        $scope.managementDetails =TrialManagerDataService.settings.trialSettings;

        $scope.managementDetailOptions = {
			selectAll: false
		};

        $scope.selectionVariables =TrialManagerDataService.settings.selectionVariables;

        $scope.selectionVariablesOptions = {
            selectAll: false
        };

		$scope.doSelectAll = function(variables, options) {

			var filteredVariables = $filter('removeHiddenAndDeletablesVariableFilter')(variables.keys(), variables.vals());

			_.each(filteredVariables, function(cvTermID) {
                variables.val(cvTermID).isChecked = options.selectAll;
			});

		};

		$scope.removeSettings = function(variableType, variables, options) {
			TrialManagerDataService.removeSettings(variableType, variables).then(function(data) {
				_(data).each(function(ids) {
					delete $scope.data.userInput[ids];
				});

				options.selectAll = false;
			});

		};

		$scope.managementDetailsSize = function() {
			return $scope.managementDetails.length();
		};

	}]);

})();
