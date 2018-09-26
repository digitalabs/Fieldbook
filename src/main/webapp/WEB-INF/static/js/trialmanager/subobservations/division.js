(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationDivisionCtrl', ['$scope', 'TrialManagerDataService', '$stateParams'/*, 'subObservation'*/,
		function ($scope, TrialManagerDataService, $stateParams/*, subObservation*/) {

			$scope.division = $stateParams.division;
			// $scope.subObservation = subObservation;

			$scope.addDataTable = function () {

				// for testing
				var tableIdentifier = '#subobservation-table-' + $scope.subObservation.id + '-' + $scope.division.id;
				new BMS.Fieldbook.MeasurementsDataTable(tableIdentifier);

				/*
				TODO
				new DataTable({
					tableIdentifier: 'subobservation-table-' + subObservation.id + '-' + division.id
				})
				 */
			}
		}]);

	// TODO
	function DataTable(options) {
		var tableIdentifier = options.tableIdentifier;

		// recreate a table if exists
		if ($(tableIdentifier).html() && !!$(tableIdentifier).html().trim()) {
			$(tableIdentifier).dataTable().fnDestroy();
			$(tableIdentifier).empty();
		}


	}
})();
