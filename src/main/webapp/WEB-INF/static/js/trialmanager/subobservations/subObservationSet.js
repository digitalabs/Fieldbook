(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationSetCtrl', ['$scope', 'TrialManagerDataService', '$stateParams', 'DTOptionsBuilder',
		'DTColumnBuilder', '$http', '$q', '$compile',
		function ($scope, TrialManagerDataService, $stateParams, DTOptionsBuilder, DTColumnBuilder, $http, $q, $compile) {

			var subObservationSet = $scope.subObservationSet = $stateParams.subObservationSet;
			$scope.preview = Boolean(subObservationSet.preview);
			$scope.columnsObj = subObservationSet.columnsObj;
			$scope.rows = subObservationSet.rows;
			$scope.nested = {};
			$scope.nested.dtPreviewInstance = null;
			$scope.nested.reviewVariable = null;

			var subObservationTab = $scope.subObservationTab;
			var dataTable = $scope.subObservationSet.dataTable;
			var tableId = '#subobservation-table-' + subObservationTab.id + '-' + subObservationSet.id;
			var previewTableId = '#preview-subobservation-table-' + subObservationTab.id + '-' + subObservationSet.id;
			var studyId = $('#studyId').val();
			var environmentId = getCurrentEnvironmentNumber();
			var dtColumnsPromise = $q.defer();
			var dtColumnDefsPromise = $q.defer();

			$scope.dtColumns = dtColumnsPromise.promise;
			$scope.dtColumnDefs = dtColumnDefsPromise.promise;

			$scope.dtOptions = addCommonOptions(DTOptionsBuilder.newOptions()
				.withOption('ajax', {
					url: '/Fieldbook/trial/measurements/plotMeasurements/' + studyId + '/' + environmentId,
					type: 'GET',
					data: function(d) {
						var sortedColIndex = $(tableId).dataTable().fnSettings().aaSorting[0][0];
						var sortDirection = $(tableId).dataTable().fnSettings().aaSorting[0][1];
						var sortedColTermId = subObservationSet.displayColumns[sortedColIndex].termId;

						return {
							draw: d.draw,
							pageSize: d.length,
							pageNumber: d.length === 0 ? 1 : d.start / d.length + 1,
							sortBy : sortedColTermId,
							sortOrder : sortDirection
						};
					}
				})
				.withDataProp('data')
				.withOption('serverSide', true));

			if (dataTable) {
				loadDataTable();
			}

			if ($scope.preview) {
				loadPreview();
			}

			$scope.addDataTable = function () {
				subObservationSet.dataTable = {};
				loadDataTable();
			};

			$scope.togglePreviewMode = function () {
				$scope.preview = subObservationSet.preview = !$scope.preview;
				if (!$scope.preview) {
					return;
				}
				loadPreview();
			};

			$scope.resetPreview = function () {
				$scope.rows = subObservationSet.rows = null;
				$scope.nested.dtPreviewInstance.changeData(getPreview());
			};

			$scope.savePreview = function () {
				// TODO implement call
				$http.post('sub-observation-set/preview/save/', subObservationSet.rows);
			};

			$scope.filterVariable = function () {

				angular.forEach($scope.columnsObj.columns, function (col) {
					if (col.className === 'variates') {
						col.visible = false;
					}
					if (col.title === $scope.nested.reviewVariable.title) {
						col.visible = true;
					}
				});
				// FIXME is there a faster way?
				$scope.dtColumns = $scope.columnsObj.columns;
				// FIXME Loading gif doesn't show immediately - is there a better way?
				$('.dataTables_processing', $(previewTableId).closest('.dataTables_wrapper')).show();
			};

			$scope.subDivide = function () {
				var id = $scope.subObservationTab.subObservationSets.length + 1;
				var name = 'Sub-observation set ' + $scope.subObservationTab.id + ' - subObservationSet ' + id;
				$scope.subObservationTab.subObservationSets.push({
					id: id,
					name: name
				});
			};

			function addCommonOptions(options) {
				return options
					.withOption('processing', true)
					.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
					.withOption('scrollY', '500px')
					.withOption('language', {
						processing: '<span class="throbber throbber-2x"></span>',
						lengthMenu: 'Records per page: _MENU_',
						paginate: {
							next: '>',
							previous: '<',
							first: '<<',
							last: '>>'
						}
					})
					.withDOM('<"row"' + //
						'<"col-sm-11"<"pull-left"l><"pull-left"i><"pull-left fbk-left-padding"r>>' + //
						'<"col-sm-1"<"pull-right"B>>' + //
						'>' + //
						'<"row"<"col-sm-12"t>>' + //
						'<"row"<"col-sm-12"p>>')
					.withButtons([{
						extend: 'colvis',
						className: 'fbk-buttons-no-border fbk-colvis-button',
						text: '<i class="glyphicon glyphicon-th"></i>',
						columns: ':gt(0):not(.ng-hide)'
					}])
					.withPaginationType('full_numbers');
			}

			function loadPreview() {
				$scope.dtOptionsPreview = addCommonOptions(DTOptionsBuilder
					.fromFnPromise(getPreview())
					.withOption('rowCallback', previewRowCallback));
			}

			function previewRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				var experimentId = aData.experimentId;

				// FIXME attach to table instead? prevent multiple cells click?
				$('td.variates', nRow).off().on('click', cellClickHandler);

				/**
				 * Keep a closure with experimentId, iDisplayIndexFull, etc
				 */
				function cellClickHandler() {
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
						var data = subObservationSet.rowMap[experimentId][termId];

						/*
						FIXME change json response for an object with named properties
							Current structure is an array
								AleuCol_E_1to5: ["7", "7", true, "", null]
							where the first item is the value
						 */

						var $inlineScope = $scope.$new(true);

						$inlineScope.observation = {
							value: data[0],
							change: function () {
								updateInline();
							},
							// FIXME altenative to blur bug https://github.com/angular-ui/ui-select/issues/499
							onOpenClose: function(isOpen) {
								if (!isOpen) updateInline();
							},
							newInlineValue: function (newValue) {
								return {name: newValue};
							}
						};

						var column = $inlineScope.column = subObservationSet.columnMap[termId];

						$(that).html('');
						var editor = $compile(
							' <observation-inline-editor ' +
							' column="column" ' +
							' observation="observation"></observation-inline-editor> '
						)($inlineScope);

						$(that).append(editor);

						function updateInline() {
							data[0] = $inlineScope.observation.value;

							setTimeout(function () {
								$inlineScope.$destroy();
								editor.remove();

								$('#preview-subobservation-table-' + subObservationTab.id + '-' + subObservationSet.id)
									.dataTable()
									.fnUpdate(subObservationSet.rows[iDisplayIndexFull], iDisplayIndexFull, null, false);

								/**
								 * Restore cell click handler
								 */
								$(that).off().on('click', cellClickHandler);
							});

						}

						if (column.dataTypeCode === 'D') {
							setTimeout(function () {
								$('input', that).datepicker({
									'format': 'yyyymmdd'
								}).on('hide', function () {
									updateInline();
								});
							});
						}

						// FIXME show combobox for categorical traits
						$(that).css('overflow', 'visible');

					});
				}
			}

			function getPreview() {
				if (subObservationSet.rows) {
					return $q.resolve(subObservationSet.rows);
				}
				return $http
					.post('/Fieldbook/ImportManager/import/preview')
					.then(function (resp) {

						// Create map for easy access on review
						var rowMap = $scope.rowMap = subObservationSet.rowMap = {};
						angular.forEach(resp.data, function (row) {
							rowMap[row.experimentId] = {};
							angular.forEach(subObservationSet.columnsObj.columns, function (column) {
								rowMap[row.experimentId][column.termId] = row[column.title];
							});
						});

						$scope.rows = subObservationSet.rows = resp.data;
						return $q.resolve(resp.data);
					});
			}

			function loadDataTable() {
				$http({
					method: 'POST',
					url: '/Fieldbook/TrialManager/createTrial/columns',
					headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
					data: 'variableList=' + TrialManagerDataService
						.settings
						.measurements.m_keys.concat(TrialManagerDataService.settings.selectionVariables.m_keys).join()
				}).then(function (response) {
					subObservationSet.displayColumns = response.data;
					var columnsObj = $scope.columnsObj = subObservationSet.columnsObj = getColumns(response.data, false);

					subObservationSet.columnMap = {};
					angular.forEach(response.data, function (column) {
						subObservationSet.columnMap[column.termId] = column;
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
					// TODO upgrade angular to > 1.5 to use one-way binding
					column: '='
				}
			};
		})
	;
})();
