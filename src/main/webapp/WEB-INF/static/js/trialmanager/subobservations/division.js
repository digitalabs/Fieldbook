(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationDivisionCtrl', ['$scope', 'TrialManagerDataService', '$stateParams', 'DTOptionsBuilder',
		'DTColumnBuilder', '$http', "$q",
		function ($scope, TrialManagerDataService, $stateParams, DTOptionsBuilder, DTColumnBuilder, $http, $q) {

			$scope.division = $stateParams.division;

			var subObservation = $scope.subObservation;
			var division = $scope.division;
			var dataTable = $scope.division.dataTable;
			var tableIdentifier = '#subobservation-table-' + subObservation.id + '-' + division.id;
			var studyId = $('#studyId').val();
			var environmentId = getCurrentEnvironmentNumber();
			var dtColumnsPromise = $q.defer();
			var dtColumnDefsPromise = $q.defer();

			$scope.dtColumns = dtColumnsPromise.promise;
			$scope.dtColumnDefs = dtColumnDefsPromise.promise;

			$scope.dtOptions = DTOptionsBuilder.newOptions()
				.withOption('ajax', {
					url: '/Fieldbook/trial/measurements/plotMeasurements/' + studyId + '/' + environmentId,
					type: 'GET',
					data: function(d) {
						// TODO
						// var sortedColIndex = $(tableIdentifier).dataTable().fnSettings().aaSorting[0][0];
						// var sortDirection = $(tableIdentifier).dataTable().fnSettings().aaSorting[0][1];
						// var sortedColTermId = displayColumns[sortedColIndex].termId;

						return {
							draw: d.draw,
							pageSize: d.length,
							pageNumber: d.length === 0 ? 1 : d.start / d.length + 1,
							// sortBy : sortedColTermId,
							// sortOrder : sortDirection
							sortBy : 8230,
							sortOrder : "asc"
						};
					}
				})
				// FIXME buttons
			 // .withDOM('<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info"B>>tp')
			    .withDOM('<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info">>tp')
				.withDataProp('data')
				.withOption('processing', true)
				.withOption('serverSide', true)
				.withPaginationType('full_numbers')
			;

			if (dataTable) {
				reload()
			}

			$scope.addDataTable = function () {
				division.dataTable = {};
				reload();
			}

			function reload() {
				var studyId = $('#studyId').val();
				var environmentId = getCurrentEnvironmentNumber();

				$http.post('/Fieldbook/TrialManager/openTrial/columns', {
					data: 'variableList=' + TrialManagerDataService
						.settings
						.measurements.m_keys.concat(TrialManagerDataService.settings.selectionVariables.m_keys).join()
				}).then(function (displayColumns) {
					var columnsObj = getColumns(displayColumns.data, false);

					dtColumnsPromise.resolve(columnsObj.columns);
					dtColumnDefsPromise.resolve(columnsObj.columnsDef);
				});
			};

		}]);
})();
