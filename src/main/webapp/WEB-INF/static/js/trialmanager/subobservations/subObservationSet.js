(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SubObservationSetCtrl', ['$scope', 'TrialManagerDataService', '$stateParams', 'DTOptionsBuilder',
		'DTColumnBuilder', '$http', '$q', '$compile', 'environmentService', 'datasetService', '$timeout',
		function ($scope, TrialManagerDataService, $stateParams, DTOptionsBuilder, DTColumnBuilder, $http, $q, $compile, environmentService,
				  datasetService, $timeout
		) {

			var subObservationSet = $scope.subObservationSet = $stateParams.subObservationSet;
			$scope.preview = Boolean(subObservationSet.preview);
			$scope.columnsObj = subObservationSet.columnsObj;
			$scope.rows = subObservationSet.rows;
			$scope.nested = {};
			$scope.nested.dtPreviewInstance = null;
			$scope.nested.reviewVariable = null;
			$scope.enableActions = false;

			var subObservationTab = $scope.subObservationTab;
			var tableId = '#subobservation-table-' + subObservationTab.id + '-' + subObservationSet.id;
			var previewTableId = '#preview-subobservation-table-' + subObservationTab.id + '-' + subObservationSet.id;
			var dtColumnsPromise = $q.defer();
			var dtColumnDefsPromise = $q.defer();

			$scope.dtColumns = dtColumnsPromise.promise;
			$scope.dtColumnDefs = dtColumnDefsPromise.promise;
			$scope.dtOptions = null;

			datasetService.getDataset(subObservationSet.id).then(function (dataset) {
				if (!dataset.instances || !dataset.instances.length) {
					return;
				}
				$scope.environments = dataset.instances;
				$scope.nested.selectedEnvironment = dataset.instances[0];

				$scope.dtOptions = getDtOptions();

				loadColumns().then(function (columnsObj) {
					dtColumnsPromise.resolve(columnsObj.columns);
					dtColumnDefsPromise.resolve(columnsObj.columnsDef);
				});
			});

			if ($scope.preview) {
				loadPreview();
			}

			// Review prototype - remove when done
			$scope.togglePreviewMode = function () {
				$scope.preview = subObservationSet.preview = !$scope.preview;
				if (!$scope.preview) {
					return;
				}
				loadPreview();
			};

			// Review prototype - remove when done
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
				// TODO
				// var id = $scope.subObservationTab.subObservationSets.length + 1;
				// var name = 'Sub-observation set ' + $scope.subObservationTab.id + ' - subObservationSet ' + id;
				// $scope.subObservationTab.subObservationSets.push({
				// 	id: id,
				// 	name: name
				// });
			};

			$scope.changeEnvironment = function () {
				$scope.dtOptions = getDtOptions();
			};

			function getDtOptions() {
				return addCommonOptions(DTOptionsBuilder.newOptions()
					.withOption('ajax', {
						url: datasetService.getObservationTableUrl(subObservationSet.id, $scope.nested.selectedEnvironment.instanceDbId),
						type: 'GET',
						beforeSend: function (xhr) {
							xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
						},
						data: function (d) {
							var sortedColIndex = $(tableId).dataTable().fnSettings().aaSorting[0][0];
							var sortDirection = $(tableId).dataTable().fnSettings().aaSorting[0][1];
							var sortedColTermId = subObservationSet.displayColumns[sortedColIndex].termId;

							return {
								draw: d.draw,
								pageSize: d.length,
								pageNumber: d.length === 0 ? 1 : d.start / d.length + 1,
								sortBy: sortedColTermId,
								sortOrder: sortDirection
							};
						}
					})
					.withDataProp('data')
					.withOption('serverSide', true))
					.withOption('initComplete', initComplete);
			}

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
					.withDOM('"<"pull-left fbk-left-padding"l>' + //
						'<"pull-left"i>' + //
						'<"pull-left fbk-left-padding"r>' + //
						'<"pull-right"B>' + //
						'<"clearfix">' + //
						'<"row add-top-padding-small"<"col-sm-12"t>>' + //
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

			function initComplete() {
				var $table = angular.element(tableId);
				$table.off().on('click', 'td.variates', clickHandler);

				function clickHandler() {
					var cell = this;

					var table = $table.DataTable();
					var rowIndex = cell.parentNode.rowIndex - 1;
					var row = table.row(rowIndex).data();
					var cellData = table.cell({row: rowIndex, column: cell.cellIndex}).data();
					var column = subObservationSet.columnsObj.columns[cell.cellIndex];
					var termId = column.termId;

					if (!termId) return;

					/**
					 * Remove handler to not interfere with inline editor
					 * fnUpdate will trigger rowCallback and restore it
					 */
					$table.off('click');

					$scope.$apply(function () {

						var $inlineScope = $scope.$new(true);

						$inlineScope.observation = {
							value: cellData.value,
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

						$(cell).html('');
						var editor = $compile(
							' <observation-inline-editor ' +
							' column="column" ' +
							' observation="observation"></observation-inline-editor> '
						)($inlineScope);

						$(cell).append(editor);

						function updateInline() {
							cellData.value = $inlineScope.observation.value;

							var addOrUpdate;
							if (cellData.observationId) {
								addOrUpdate = datasetService.updateObservation(subObservationSet.id, row.observationUnitId,
									cellData.observationId, {
										categoricalValueId: null,
										value: cellData.value
									})
							} else {
								addOrUpdate = datasetService.addObservation(subObservationSet.id, row.observationUnitId, {
									observationUnitId: row.observationUnitId,
									categoricalValueId: null,
									variableId: termId,
									value: cellData.value
								})
							}
							addOrUpdate.then(function (data) {
								cellData.observationId = data.observationId;

								$inlineScope.$destroy();
								editor.remove();

								// TODO
								$(tableId).dataTable().fnUpdate(row, rowIndex, null, false);

								/**
								 * Restore cell click handler
								 */
								$table.on('click', 'td', clickHandler);
							}, function (response) {
								if (response.errors) {
									showErrorMessage('', response.errors[0].message);
								} else {
									showErrorMessage('', ajaxGenericErrorMsg);
								}
							});

						}

						if (column.dataTypeCode === 'D') {
							setTimeout(function () {
								$('input', cell).datepicker({
									'format': 'yyyymmdd'
								}).on('hide', function () {
									updateInline();
								});
							});
						}

						// FIXME show combobox for categorical traits
						$(cell).css('overflow', 'visible');

					});
				}
			}

			// TODO 1) extract common logic rowCallback 2) use datatable api to store data 3) use DataTable().rows().data() to save
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

			// FIXME 1) adapt to subobs 2) See previewRowCallback
			function getPreview() {
				// TODO check memory consumption
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

			function reloadTable() {
				loadColumns().then(function (columnsObj) {
					$scope.dtColumns = columnsObj.columns;
					$scope.dtColumnDefs = columnsObj.columnsDef;
				});
			}

			function loadColumns() {
				return datasetService.getColumns(subObservationSet.id).then(function (data) {
					subObservationSet.displayColumns = data;
					var columnsObj = $scope.columnsObj = subObservationSet.columnsObj = mapColumns(data);

					subObservationSet.columnMap = {};
					angular.forEach(data, function (column) {
						subObservationSet.columnMap[column.termId] = column;
					});

					return columnsObj;
				});
			}

			// TODO merge with measurements-table-trial.js#getColumns
			function mapColumns(data) {
				var columns = [],
					columnsDef = [];

				// TODO complete column definitions (highlighting, links, etc)

				angular.forEach(data, function (displayColumn) {
					columns.push({
						title: displayColumn.name,
						termId: displayColumn.termId,
						factor: displayColumn.factor,
						className: displayColumn.factor === true ? 'factors' : 'variates',
						data: function (row) {
							if (row.variables[displayColumn.name]) {
								return row.variables[displayColumn.name];
							}
							return "";
						}
					});
					// GID or DESIGNATION
					if (displayColumn.termId === 8240 || displayColumn.termId === 8250) {
						columnsDef.push({
							targets: columns.length - 1,
							render: function(data, type, full, meta) {
								return '<a class="gid-link" href="javascript: void(0)" ' +
									'onclick="openGermplasmDetailsPopopWithGidAndDesig(\'' +
									full.gid + '\',\'' + full.designation + '\')">' + EscapeHTML.escape(data.value) + '</a>';
							}
						});
					} else {
						columnsDef.push({
							targets: columns.length - 1,
							render: function(data, type, full, meta) {
								return data && data.value || '';
							}
						});
					}
				});

				return {
					columns: columns,
					columnsDef: columnsDef
				};
			}

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
