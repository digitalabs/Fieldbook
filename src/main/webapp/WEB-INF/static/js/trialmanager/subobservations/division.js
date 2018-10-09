(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationDivisionCtrl', ['$scope', 'TrialManagerDataService', '$stateParams', 'DTOptionsBuilder',
		'DTColumnBuilder', '$http', "$q", "$compile",
		function ($scope, TrialManagerDataService, $stateParams, DTOptionsBuilder, DTColumnBuilder, $http, $q, $compile) {

			var division = $scope.division = $stateParams.division;
			$scope.preview = Boolean(division.preview);
			$scope.columnsObj = division.columnsObj;
			$scope.rows = division.rows;
			$scope.nested = {};
			$scope.nested.dtInstance = null;

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
						var sortedColIndex = $(tableIdentifier).dataTable().fnSettings().aaSorting[0][0];
						var sortDirection = $(tableIdentifier).dataTable().fnSettings().aaSorting[0][1];
						var sortedColTermId = division.displayColumns[sortedColIndex].termId;

						return {
							draw: d.draw,
							pageSize: d.length,
							pageNumber: d.length === 0 ? 1 : d.start / d.length + 1,
							sortBy : sortedColTermId,
							sortOrder : sortDirection
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

			$scope.resetPreview = function () {
				$scope.rows = division.rows = null;
				$scope.nested.dtInstance.changeData(getPreview());
			};

			$scope.savePreview = function () {
				$http.post('sub-observation-set/preview/save/', division.rows);
			};

			function renderPreview() {
				$scope.dtOptionsPreview = DTOptionsBuilder
					.fromFnPromise(getPreview())
					.withOption('rowCallback', previewRowCallback)
					// FIXME buttons
				 // .withDOM('<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info"B>>tp')
					.withDOM('<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info">>tp')
					.withPaginationType('full_numbers')
				;

			}

			function previewRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				var experimentId = aData.experimentId;

				$('td.variates', nRow).off().on('click', function () {
					var termId = $(this).data('term-id'),
						phenotypeId = $(this).data('phenotype-id'),
						that = this;

					// FIXME BMS-4453
					if (!termId || !phenotypeId) return;

					/**
					 * Remove handler to not interfere with inline editor
					 * fnUpdate will trigger rowCallback and restore it
					 */
					$(this).off('click');

					$scope.$apply(function () {
						var data = division.rowMap[experimentId][termId];

						/*
						FIXME change json response for an object with named properties
							Current structure is an array
								AleuCol_E_1to5: ["7", "7", true, "", null]
							where the first item is the value
						 */

						var $inlineScope = $scope.$new(true)

						$inlineScope.observation = {
							value: data[0],
							change: function () {
								updateInline();
							},
							// FIXME altenative to blur bug https://github.com/angular-ui/ui-select/issues/499
							onOpenClose: function(isOpen) {
								if (!isOpen) updateInline();
							}
						};

						var column = $inlineScope.column = division.columnMap[termId];

						$(that).html('');
						var editor = $compile(
							" <observation-inline-editor " +
							" column='column' " +
							" observation='observation'></observation-inline-editor> "
						)($inlineScope);

						$(that).append(editor);

						function updateInline() {
							data[0] = $inlineScope.observation.value;

							$inlineScope.$destroy();
							editor.remove();

							$('#preview-subobservation-table-' + subObservation.id + '-' + division.id)
								.dataTable()
								.fnUpdate(division.rows[iDisplayIndexFull], iDisplayIndexFull, null, true);
						}

						// FIXME
						$(that).css('overflow', 'visible')

					});
				})
			}

			function getPreview() {
				if (division.rows) {
					return $q.resolve(division.rows);
				}
				return $http
					.post('/Fieldbook/ImportManager/import/preview')
					.then(function (resp) {

						// Create map for easy access on review
						var rowMap = $scope.rowMap = division.rowMap = {};
						angular.forEach(resp.data, function (row) {
							rowMap[row.experimentId] = {};
							angular.forEach(division.columnsObj.columns, function (column) {
								rowMap[row.experimentId][column.termId] = row[column.title];
							})
						});

						$scope.rows = division.rows = resp.data;
						return $q.resolve(resp.data);
					})
			}

			function reload() {
				var studyId = $('#studyId').val();
				var environmentId = getCurrentEnvironmentNumber();

				$http({
					method: 'POST',
					url: '/Fieldbook/TrialManager/openTrial/columns',
					headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
					data: 'variableList=' + TrialManagerDataService
						.settings
						.measurements.m_keys.concat(TrialManagerDataService.settings.selectionVariables.m_keys).join()
				}).then(function (response) {
					division.displayColumns = response.data;
					var columnsObj = $scope.columnsObj = division.columnsObj = getColumns(response.data, false);

					division.columnMap = {};
					angular.forEach(response.data, function (column) {
						division.columnMap[column.termId] = column;
					});

					dtColumnsPromise.resolve(columnsObj.columns);
					dtColumnDefsPromise.resolve(columnsObj.columnsDef);
				});
			};

		}])
		.directive('observationInlineEditor', function () {
			return {
				restrict: 'E',
				templateUrl: '/Fieldbook/static/angular-templates/observationInlineEditor.html',
				scope: {
					observation: '=',
					column: '=' // TODO upgrade angular to > 1.5 to use one-way binding
				}
			}
		})
	;
})();
