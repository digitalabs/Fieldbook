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
			$scope.nested.dtInstance = null;
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
							var sortedColTermId = subObservationSet.columnsData[sortedColIndex].termId;

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
					.withOption('serverSide', true)
					.withOption('headerCallback', headerCallback)
					.withOption('initComplete', initComplete));
			}

			function addCommonOptions(options) {
				return options
					.withOption('processing', true)
					.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
					.withOption('scrollY', '500px')
					.withOption('scrollX', '100%')
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
					// TODO 1) extract common logic rowCallback 2) use datatable api to store data 3) use DataTable().rows().data() to save
					// .withOption('rowCallback', previewRowCallback)
				);
			}

			function headerCallback(thead, data, start, end, display) {
				var table = $scope.nested.dtInstance.DataTable;
				table.columns().every(function() {
					var column = $scope.columnsObj.columns[this.index()];
					if (column.columnData.formula) {
						$(this.header()).addClass('derived-trait-column-header');
					}
				});
			}

			function initComplete() {
				var $table = angular.element(tableId);
				$table.off().on('click', 'td.variates', clickHandler);

				function clickHandler() {
					var cell = this;

					var table = $table.DataTable();
					var rowIndex = cell.parentNode.rowIndex - 1;
					var rowData = table.row(rowIndex).data();
					var dtCell = table.cell({row: rowIndex, column: cell.cellIndex});
					var cellData = dtCell.data();
					var column = $scope.columnsObj.columns[cell.cellIndex];
					var termId = column.columnData.termId;

					if (!termId) return;

					/**
					 * Remove handler to not interfere with inline editor
					 * will be restored after fnUpdate
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

						$inlineScope.column = column.columnData;

						$(cell).html('');
						var editor = $compile(
							' <observation-inline-editor ' +
							' column="column" ' +
							' observation="observation"></observation-inline-editor> '
						)($inlineScope);

						$(cell).append(editor);

						function updateInline() {
							var promise;

							if (cellData.value === $inlineScope.observation.value) {
								promise = $q.resolve(cellData);
							} else {
								var value = $inlineScope.observation.value;

								if (cellData.observationId) {
									if (value) {
										promise = confirmOutOfBoundData(value, column.columnData).then(function(doContinue) {
											if (!doContinue) {
												$inlineScope.observation.value = cellData.value;
												return {observationId: cellData.observationId};
											}
											return datasetService.updateObservation(subObservationSet.id, rowData.observationUnitId,
												cellData.observationId, {
													categoricalValueId: getCategoricalValueId(value, column.columnData),
													value: value
												});
										});
									} else {
										promise = datasetService.deleteObservation(subObservationSet.id, rowData.observationUnitId,
											cellData.observationId);
									}
								} else {
									if (value) {
										promise = confirmOutOfBoundData(value, column.columnData).then(function(doContinue) {
											if (!doContinue) {
												$inlineScope.observation.value = cellData.value;
												return {observationId: cellData.observationId};
											}
											return datasetService.addObservation(subObservationSet.id, rowData.observationUnitId, {
												observationUnitId: rowData.observationUnitId,
												categoricalValueId: getCategoricalValueId(value, column.columnData),
												variableId: termId,
												value: value
											});
										});
									} else {
										promise = $q.resolve(cellData);
									}
								}
							}

							promise.then(function (data) {
								cellData.value = $inlineScope.observation.value;
								cellData.observationId = data.observationId;

								$inlineScope.$destroy();
								editor.remove();

								dtCell.data(cellData);

								/**
								 * Restore cell click handler
								 */
								$table.on('click', 'td.variates', clickHandler);

								applyCellColor(cell, cellData, rowData, column.columnData);
							}, function (response) {
								if (response.errors) {
									showErrorMessage('', response.errors[0].message);
								} else {
									showErrorMessage('', ajaxGenericErrorMsg);
								}
							});

						}

						if (column.columnData.dataTypeCode === 'D') {
							$timeout(function () {
								angular.element('input', cell).on('keydown', function (e) {
									if (e.keyCode === 13) {
										e.stopImmediatePropagation();
									}
								}).datepicker({
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

			function getCategoricalValueId(cellDataValue, columnData) {
				if (columnData.possibleValues
					&& cellDataValue !== 'missing') {

					var categoricalValue = columnData.possibleValues.find(function (possibleValue) {
						return possibleValue.name === cellDataValue;
					});
					if (categoricalValue !== undefined) {
						return categoricalValue.id;
					}

				}
				return null;
			}

			function confirmOutOfBoundData(cellDataValue, columnData) {
				var deferred = $q.defer();

				var invalid = validateOutOfBoundData(cellDataValue, columnData);

				if (invalid) {
					var confirmModal = $scope.openConfirmModal(observationOutOfRange, keepLabel, discardLabel);
					confirmModal.result.then(deferred.resolve);
				} else {
					deferred.resolve(true);
				}

				return deferred.promise;
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
				return datasetService.getColumns(subObservationSet.id).then(function (columnsData) {
					subObservationSet.columnsData = columnsData;
					var columnsObj = $scope.columnsObj = subObservationSet.columnsObj = mapColumns(columnsData);

					subObservationSet.columnMap = {};
					angular.forEach(columnsData, function (columnData) {
						subObservationSet.columnMap[columnData.termId] = columnData;
					});

					return columnsObj;
				});
			}

			// TODO merge with measurements-table-trial.js#getColumns
			function mapColumns(columnsData) {
				var columns = [],
					columnsDef = [];

				// TODO complete column definitions (highlighting, links, etc)

				angular.forEach(columnsData, function (columnData) {
					columns.push({
						title: columnData.name,
						data: function (row) {
							return row.variables[columnData.name];
						},
						defaultContent: '',
						className: columnData.factor === true ? 'factors' : 'variates',
						columnData: columnData
					});
					// GID or DESIGNATION
					if (columnData.termId === 8240 || columnData.termId === 8250) {
						columnsDef.push({
							targets: columns.length - 1,
							render: function (data, type, full, meta) {
								return '<a class="gid-link" href="javascript: void(0)" ' +
									'onclick="openGermplasmDetailsPopopWithGidAndDesig(\'' +
									full.gid + '\',\'' + full.designation + '\')">' + EscapeHTML.escape(data.value) + '</a>';
							}
						});
					} else if (!columnData.factor) {
						columnsDef.push({
							targets: columns.length - 1,
							createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
								applyCellColor(td, cellData, rowData, columnData);
							},
							render: function (data, type, full, meta) {
								return data && EscapeHTML.escape(data.value);
							}
						});
					} else {
						columnsDef.push({
							targets: columns.length - 1,
							render: function (data, type, full, meta) {
								return data && EscapeHTML.escape(data.value);
							}
						});
					}
				});

				return {
					columns: columns,
					columnsDef: columnsDef
				};
			}

			function validateOutOfBoundData(cellDataValue, columnData) {
				var invalid = false;

				var value = cellDataValue;
				var minVal = columnData.minRange;
				var maxVal = columnData.maxRange;

				// Numeric
				if (minVal && maxVal
					&& (parseFloat(value) < parseFloat(minVal) || parseFloat(value) > parseFloat(maxVal))) {

					invalid = true;
				}
				// Categorical
				if (columnData.possibleValues
					&& columnData.possibleValues.find(function (possibleValue) {
						return possibleValue.name === cellDataValue;
					}) === undefined
					&& cellDataValue !== 'missing') {

					invalid = true;
				}
				return invalid;
			}

			function applyCellColor(td, cellData, rowData, columnData) {
				$(td).removeClass('accepted-value');
				$(td).removeClass('invalid-value');
				$(td).removeClass('manually-edited-value');

				if (cellData.value) {
					var invalid = validateOutOfBoundData(cellData.value, columnData);

					if (invalid) {
						$(td).addClass($scope.preview ? 'invalid-value' : 'accepted-value');
					}
				}
				if (cellData.status) {
					var status = cellData.status;
					if (!cellData.observationId) {
						return;
					}
					$(td).removeAttr('title');
					var toolTip = 'GID: ' + rowData.variables.GID.value + ' Designation: ' + rowData.variables.DESIGNATION.value;
					if (status == 'MANUALLY_EDITED') {
						$(td).attr('title', toolTip + ' manually-edited-value');
						$(td).addClass('manually-edited-value');
					} else if (status == 'OUT_OF_SYNC') {
						$(td).attr('title', toolTip + ' out-of-sync-value');
						$(td).addClass('out-of-sync-value');
					}
				}
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
				},
				controller: function($scope) {
					$scope.doBlur = function ($event) {
						if ($event.keyCode === 13) {
							$event.target.blur();
						}
					}
				}
			};
		})
	;
})();
