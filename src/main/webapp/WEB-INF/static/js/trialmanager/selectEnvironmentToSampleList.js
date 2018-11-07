/*global angular, showAlertMessage, showErrorMessage, selectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('selectEnvironmentToSampleListModalCtrl', ['$scope', 'environmentService', '$timeout', function ($scope,
																															   environmentService, $timeout) {

		$scope.trialInstances = [];
		$scope.environmentListView = [];

		$scope.continueCreatingSampleList = function () {
			if ($scope.trialInstances.length === 0) {
				showErrorMessage('', selectOneLocationErrorMessageForSampleList);
			} else {
				selectedEnvironmentContinueCreatingSample($scope.trialInstances);
			}
		};

		$scope.init = function () {
			$scope.selectAll = true;
			environmentService.getEnvironments().then(function (environmentDetails) {
				$scope.trialInstances = [];
				$scope.environmentListView = [];

				angular.forEach(environmentDetails, function (environment) {
					$scope.environmentListView.push({
						name: environment.locationName + ' - (' + environment.locationAbbreviation + ')',
						abbrName: environment.locationAbbreviation,
						customAbbrName: environment.customLocationAbbreviation,
						trialInstanceNumber: environment.instanceNumber,
						instanceDbId: environment.instanceDbId,
						selected: $scope.selectAll
					});
					$scope.trialInstances.push(environment.instanceNumber)
				});

				//This can be used to check if a table is a DataTable or not already.
				if (!$.fn.dataTable.isDataTable('#selectEnvironmentToSampleListModal .fbk-datatable-environments')) {
					$timeout(function () {
						angular.element('#selectEnvironmentToSampleListModal .fbk-datatable-environments').DataTable({
							dom: "<'row'<'col-sm-6'l><'col-sm-6'f>>" +
								"<'row'<'col-sm-12'tr>>" +
								"<'row'<'col-sm-5'i><'col-sm-7'>>" +
								"<'row'<'col-sm-12'p>>"
						}).columns.adjust().draw();
					}, 1);
				}
			});
		};
	}]);
})();
