(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationDivisionCtrl', ['$scope', 'TrialManagerDataService', '$stateParams', 'DTOptionsBuilder',
		'DTColumnBuilder', '$http', "$q",
		function ($scope, TrialManagerDataService, $stateParams, DTOptionsBuilder, DTColumnBuilder, $http, $q) {

			var division = $scope.division = $stateParams.division;
			$scope.preview = Boolean(division.preview);
			$scope.columnsObj = division.columnsObj;
			$scope.rows = division.rows;

			var subObservation = $scope.subObservation;
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

			var dtColumnDefsPreviewPromise = $q.defer();
			$scope.dtColumnDefsPreview = dtColumnDefsPreviewPromise.promise;

			if (dataTable) {
				reload()
			}

			if ($scope.preview) {
				renderPreview();
			}

			$scope.addDataTable = function () {
				division.dataTable = {};
				reload();
			}

			$scope.togglePreviewMode = function () {
				$scope.preview = division.preview = !$scope.preview;
				if (!$scope.preview) {
					return;
				}
				renderPreview();
			}

			function renderPreview() {
				getPreview()
					.then(function (rows) {
						$scope.rows = division.rows = rows;

						$scope.dtOptionsPreview = DTOptionsBuilder.newOptions()
							// FIXME buttons
						 // .withDOM('<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info"B>>tp')
							.withDOM('<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info">>tp')
							.withPaginationType('full_numbers')
						;

						// TODO
						// dtColumnDefsPreviewPromise.resolve(division.columnsObj.columnsDef);
					})
			}

			function getPreview() {
				if (division.rows) {
					return $q.resolve(division.rows);
				}
				return $http
					.get('/Fieldbook/trial/measurements/plotMeasurements/' + studyId + '/' + environmentId, {
						// TODO
						params: {
							pageSize: 1000,
							pageNumber: 0,
							sortBy : 8230,
							sortOrder : "asc"
						}
					}).then(function (resp) {
						// Wrap each element of the matrix in an object
						angular.forEach(resp.data.data, function (row) {
							angular.forEach(row, function (value, key) {
								row[key] = {
									edit: false, // edit mode for the cell
									data: row[key]
								}
							})
						})
						return $q.resolve(resp.data.data);
					})
			}

			function reload() {
				var studyId = $('#studyId').val();
				var environmentId = getCurrentEnvironmentNumber();

				$http.post('/Fieldbook/TrialManager/openTrial/columns', {
					data: 'variableList=' + TrialManagerDataService
						.settings
						.measurements.m_keys.concat(TrialManagerDataService.settings.selectionVariables.m_keys).join()
				}).then(function (displayColumns) {
					var columnsObj = $scope.columnsObj = division.columnsObj = getColumns(displayColumns.data, false);

					dtColumnsPromise.resolve(columnsObj.columns);
					dtColumnDefsPromise.resolve(columnsObj.columnsDef);
				});
			};

		}]);
})();
