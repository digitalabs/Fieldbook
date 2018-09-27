(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationDivisionCtrl', ['$scope', 'TrialManagerDataService', '$stateParams',
		function ($scope, TrialManagerDataService, $stateParams) {

			$scope.division = $stateParams.division;

			var subObservation = $scope.subObservation,
				division = $scope.division,
				dataTable = $scope.division.dataTable
			;

			if (dataTable) {
				dataTable.reload();
			}

			$scope.addDataTable = function () {

				if (dataTable) {
					return;
				}

				// for testing
				var tableIdentifier = '#subobservation-table-' + subObservation.id + '-' + division.id;

				division.dataTable = new DataTable({
					tableIdentifier: tableIdentifier
				})
			}
		}]);

	// TODO
	function DataTable(options) {
		this.tableIdentifier = options.tableIdentifier;

		// recreate a table if exists
		if ($(this.tableIdentifier).html() && !!$(this.tableIdentifier).html().trim()) {
			$(this.tableIdentifier).dataTable().fnDestroy();
			$(this.tableIdentifier).empty();
		}

		this.table = new BMS.Fieldbook.MeasurementsDataTable(this.tableIdentifier);
	}

	DataTable.prototype.reload = function () {
		this.table = new BMS.Fieldbook.MeasurementsDataTable(this.tableIdentifier);
	};

})();
