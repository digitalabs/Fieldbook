(function () {
	'use strict';

	var subObservationModule = angular.module('subObservation', []);
	var hiddenColumns = [8201];

	subObservationModule.controller('SubObservationSetCtrl', ['$scope', 'TrialManagerDataService', '$stateParams', 'DTOptionsBuilder',
		'DTColumnBuilder', '$http', '$q', '$compile', 'environmentService', 'datasetService', '$timeout',
		function ($scope, TrialManagerDataService, $stateParams, DTOptionsBuilder, DTColumnBuilder, $http, $q, $compile, environmentService,
				  datasetService, $timeout
		) {
			$scope.traitVariables = new angular.OrderedHash();
			$scope.selectionVariables = new angular.OrderedHash();
			$scope.isHideDelete = false;
			$scope.addVariable = true;
			var subObservationSet = $scope.subObservationSet = $stateParams.subObservationSet;
			$scope.columnsObj = subObservationSet.columnsObj;
			$scope.rows = subObservationSet.rows;
			$scope.nested = {};
			$scope.nested.dtInstance = null;
			$scope.nested.reviewVariable = null;
			$scope.enableActions = false;
			$scope.isCategoricalDescriptionView = window.isCategoricalDescriptionView;
			$scope.columnDataByInputTermId = {};

			var subObservationTab = $scope.subObservationTab;
			var tableId = '#subobservation-table-' + subObservationTab.id + '-' + subObservationSet.id;
			var dtColumnsPromise = $q.defer();
			var dtColumnDefsPromise = $q.defer();

			$scope.dtColumns = dtColumnsPromise.promise;
			$scope.dtColumnDefs = dtColumnDefsPromise.promise;
			$scope.dtOptions = null;

			datasetService.getDataset(subObservationSet.id).then(function (dataset) {
				$scope.subObservationSet.dataset = dataset;
				if (!dataset.instances || !dataset.instances.length) {
					return;
				}
				$scope.environments = [{
					instanceNumber: null,
					locationName: 'All environments'
				}].concat(dataset.instances);

				$scope.traitVariables = $scope.getVariables('TRAIT');
				$scope.selectionVariables = $scope.getVariables('SELECTION_METHOD');
				$scope.selectedVariables = $scope.getSelectedVariables();

				$scope.isPendingView = subObservationSet.hasPendingData = subObservationTab.hasPendingData = dataset.hasPendingData;
				doPendingViewActions();

				loadTable();
			}); // getDataset

			$scope.getVariables = function (variableType) {
				var variables = {settings: []};
				angular.forEach($scope.subObservationSet.dataset.variables, function (datasetVariable) {
					var SettingDetail = $scope.transformSettingDetails(datasetVariable, variableType);
					variables.settings.push(SettingDetail);

				});
				return TrialManagerDataService.extractSettings(variables);
			};

			$scope.transformSettingDetails = function (datasetVariable, variableType) {
				var variable = $scope.transformVariable(datasetVariable);
				var SettingDetail = {
					variable: variable,
					hidden: datasetVariable.variableType != variableType,
					deletable: true
				};
				return SettingDetail;
			};

			$scope.transformVariable = function (datasetVariable) {
				var variable = {
					cvTermId: datasetVariable.termId,
					name: datasetVariable.alias,
					description: datasetVariable.description,
					property: datasetVariable.property,
					scale: datasetVariable.scale,
					//role:null,
					method: datasetVariable.method,
					dataType: datasetVariable.dataType,
					dataTypeId: datasetVariable.dataTypeId,
					minRange: null,
					maxRange: null,
					operation: null,
					formula: datasetVariable.formula
				};
				return variable;
			};

			$scope.getSelectedVariables = function () {
				var selected = {};
				angular.forEach($scope.subObservationSet.dataset.variables, function (variable) {
					selected[variable.termId] = variable.alias;
				});
				return selected;
			};

			$scope.selectVariableCallback = function (responseData) {
				// just override default callback (see VariableSelection.prototype._selectVariable)
			};

			$scope.onHideCallback = function () {
				adjustColumns($(tableId).DataTable());
			};

			$scope.onAddVariable = function (result, variableTypeId) {
				var variable = undefined;
				angular.forEach(result, function (val) {
					variable = val.variable;
					val.deletable = true;
					variable.description = variable.definition;
					variable.name = variable.alias ? variable.alias : variable.name;
					variable.termId = variable.id;
				});

				datasetService.addVariables($scope.subObservationSet.dataset.datasetId, {
					variableTypeId: variableTypeId,
					variableId: variable.id,
					studyAlias: variable.name
				}).then(function () {
					$scope.subObservationSet.dataset.variables.push(variable);
					loadTable();
				}, function (response) {
					if (response.errors && response.errors.length) {
						showErrorMessage('', response.errors[0].message);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});
			};

			$scope.onRemoveVariable = function (variableIds, settings) {
				var promise = $scope.validateRemoveVariable(variableIds);

				promise.then(function (doContinue) {
					if (doContinue) {
						datasetService.removeVariables($scope.subObservationSet.dataset.datasetId, variableIds).then(function () {
							angular.forEach(variableIds, function (cvtermId) {
								settings.remove(cvtermId);
								// TODO review
								$scope.subObservationSet.dataset.variables = $scope.subObservationSet.dataset.variables.filter(function (variable) {
									return variable.termId !== cvtermId;
								});
							});

							loadTable();
							$scope.selectedVariables = $scope.getSelectedVariables();
						}, function (response) {
							if (response.errors && response.errors.length) {
								showErrorMessage('', response.errors[0].message);
							} else {
								showErrorMessage('', ajaxGenericErrorMsg);
							}
						});
					}
				});
			};

			$scope.validateRemoveVariable = function (deleteVariables) {
				var deferred = $q.defer();
				if (deleteVariables.length != 0) {
					datasetService.observationCount($scope.subObservationSet.dataset.datasetId, deleteVariables).then(function (response) {
						var count = response.headers('X-Total-Count');
						if (count > 0) {
							var modalInstance = $scope.openConfirmModal(observationVariableDeleteConfirmationText , environmentConfirmLabel);
							modalInstance.result.then(deferred.resolve);
						} else {
							deferred.resolve(true);
						}
					});
				}
				return deferred.promise;
			};

			$scope.togglePendingView = function (isPendingView) {
				if ($scope.isPendingView === isPendingView) {
					return;
				}
				$scope.isPendingView = isPendingView;
				doPendingViewActions();
				loadTable();
			};

			$scope.acceptDraftData = function () {
				datasetService.acceptDraftData($scope.subObservationSet.dataset.datasetId);
			}

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
				$(tableId).DataTable().ajax.reload();
			};

			$scope.toggleShowCategoricalDescription = function () {
				switchCategoricalView().done(function () {
					$scope.$apply(function () {
						$scope.isCategoricalDescriptionView = window.isCategoricalDescriptionView;
						adjustColumns($(tableId).DataTable());
					});
				});
			};

			function doPendingViewActions() {
				$scope.toggleSection = $scope.isPendingView;

				if ($scope.isPendingView) {
					$scope.nested.selectedEnvironment = $scope.environments[0];
				} else {
					$scope.nested.selectedEnvironment = $scope.environments[1];
				}
			}

			function getDtOptions() {
				return addCommonOptions(DTOptionsBuilder.newOptions()
					.withOption('ajax', {
						url: datasetService.getObservationTableUrl(subObservationSet.id),
						type: 'GET',
						beforeSend: function (xhr) {
							xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
						},
						data: function (d) {
							var sortedColIndex = $(tableId).dataTable().fnSettings().aaSorting[0][0];
							var sortDirection = $(tableId).dataTable().fnSettings().aaSorting[0][1];
							var sortedColTermId = subObservationSet.columnsData[sortedColIndex].termId;
							var instanceId = $scope.nested.selectedEnvironment.instanceDbId;

							return {
								draw: d.draw,
								pageSize: d.length,
								pageNumber: d.length === 0 ? 1 : d.start / d.length + 1,
								sortBy: sortedColTermId,
								sortOrder: sortDirection,
								instanceId: instanceId,
								draftMode: $scope.isPendingView
							};
						}
					})
					.withDataProp('data')
					.withOption('serverSide', true)
					.withOption('initComplete', initCompleteCallback)
					.withOption('headerCallback', headerCallback)
					.withOption('drawCallback', drawCallback));
			}

			function addCommonOptions(options) {
				return options
					.withOption('processing', true)
					.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
					.withOption('scrollY', '500px')
					.withOption('scrollCollapse', true)
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
						text: '<i class="glyphicon glyphicon-th"></i>'
					}])
					.withColReorder()
					.withPaginationType('full_numbers');
			}

			/**
			 * FIXME we were having issues with clone() and $compile. Attaching and detaching instead for now
			 */
			function attachCategoricalDisplayBtn() {
				$timeout(function () {
					$('#subObsCategoricalDescriptionBtn').detach().insertBefore('#subObservationTableContainer .dt-buttons');
				});
			}

			function detachCategoricalDisplayBtn() {
				$('#subObsCategoricalDescriptionBtn').detach().appendTo('#subObsCategoricalDescriptionContainer');
			}

			function initCompleteCallback() {
				attachCategoricalDisplayBtn();
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

			function drawCallback() {
                addCellClickHandler();
			}

			function adjustColumns(table) {
				$timeout(function () {
					table.columns.adjust();
				});
			}

			function addCellClickHandler() {
				var $table = angular.element(tableId);

				addClickHandler();

				function addClickHandler() {
					$table.off('click').on('click', 'td.variates:not([disabled])', clickHandler);
				}

				function clickHandler() {
					var cell = this;

					var table = $table.DataTable();
					var dtRow = table.row(cell.parentNode);
					var rowData = dtRow.data();
					var dtCell = table.cell(cell);
					var cellData = dtCell.data();
					var index = table.colReorder.transpose(table.column(cell).index(), 'toOriginal');
					var columnData = $scope.columnsObj.columns[index].columnData;
					var termId = columnData.termId;

					if (!termId) return;

					/**
					 * Remove handler to not interfere with inline editor
					 * will be restored after fnUpdate
					 */
					$table.off('click');

					$scope.$apply(function () {

						var $inlineScope = $scope.$new(true);

						$inlineScope.observation = {
							value: $scope.isPendingView ? cellData.draftValue : cellData.value,
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

						$inlineScope.columnData = columnData;
						$inlineScope.isCategoricalDescriptionView = $scope.isCategoricalDescriptionView;

						$(cell).html('');
						var editor = $compile(
							' <observation-inline-editor ' +
							' is-categorical-description-view="isCategoricalDescriptionView" ' +
							' column-data="columnData" ' +
							' observation="observation"></observation-inline-editor> '
						)($inlineScope);

						$(cell).append(editor);

						function updateInline() {

							function doAjaxUpdate() {
								if (cellData.value === $inlineScope.observation.value) {
									return $q.resolve(cellData);
								}

								var value = cellData.value;
								var draftValue = cellData.draftValue;

								if ($scope.isPendingView) {
									draftValue = $inlineScope.observation.value;
								} else {
									value = $inlineScope.observation.value;
								}

								if (cellData.observationId) {
									if (!value && !$scope.isPendingView) {
										return datasetService.deleteObservation(subObservationSet.id, rowData.observationUnitId,
											cellData.observationId);
									}

									return confirmOutOfBoundData(value, columnData).then(function (doContinue) {
										if (!doContinue) {
											$inlineScope.observation.value = cellData.value;
											return {observationId: cellData.observationId};
										}
										return datasetService.updateObservation(subObservationSet.id, rowData.observationUnitId,
											cellData.observationId, {
												categoricalValueId: getCategoricalValueId(value, columnData),
												value: value,
												draftValue: draftValue
											});
									});
								}

								if (value) {
									return confirmOutOfBoundData(value, columnData).then(function (doContinue) {
										if (!doContinue) {
											$inlineScope.observation.value = cellData.value;
											return {observationId: cellData.observationId};
										}
										return datasetService.addObservation(subObservationSet.id, rowData.observationUnitId, {
											observationUnitId: rowData.observationUnitId,
											categoricalValueId: getCategoricalValueId(value, columnData),
											variableId: termId,
											value: value
										});
									});
								}

								return $q.resolve(cellData);
							}

							var promise = doAjaxUpdate();

							promise.then(function (data) {
								var valueChanged = false;
								if (cellData.value !== $inlineScope.observation.value) {
                                    valueChanged = true;
								}

								if ($scope.isPendingView) {
									cellData.draftValue = $inlineScope.observation.value;
								} else {
									cellData.value = $inlineScope.observation.value;
								}

								cellData.observationId = data.observationId;
								cellData.status = data.status;

								$inlineScope.$destroy();
								editor.remove();

								/**
								 * We are updating the cell value and the target if the trait is input of a formula
								 * to avoid reloading the page. It has these advantages:
								 * - Make the inline edition more dynamic and fast
								 * - Don't reset the table scroll
								 *
								 * The alternative would be:
								 *
								 *     table.ajax.reload(function () {
								 *         // Restore handler
								 *         $table.off('click').on('click', 'td.variates', clickHandler);
								 *     }, false);
								 */
								dtCell.data(cellData);
								processCell(cell, cellData, rowData, columnData);

								if (valueChanged && $scope.columnDataByInputTermId[termId]) {
									var targetColumnData = $scope.columnDataByInputTermId[termId];
									var targetColIndex = table.colReorder.transpose(targetColumnData.index, 'toCurrent');
									var targetDtCell = table.cell(dtRow.node(), targetColIndex);
									var targetCellData = targetDtCell.data();
									targetCellData.status = 'OUT_OF_SYNC';
									processCell(targetDtCell.node(), targetCellData, rowData, targetColumnData);
								}

								// Restore handler
								addClickHandler();
							}, function (response) {
								if (response.errors) {
									showErrorMessage('', response.errors[0].message);
								} else {
									showErrorMessage('', ajaxGenericErrorMsg);
								}
							});

						}

						if (columnData.dataTypeCode === 'D') {
							$(cell).one('click', 'input', function () {
								var initialValue;
								try {
									initialValue = $.datepicker.formatDate("yy-mm-dd", $.datepicker.parseDate('yymmdd', $(this).val()));
								} catch (e) {
								}

								$(this).on('keydown', function (e) {
									if (e.keyCode === 13) {
										e.stopImmediatePropagation();
									}
								}).datepicker({
									format: 'yyyymmdd',
									todayHighlight: true,
									todayBtn: true,
									forceParse: false
								}).on('hide', function () {
									updateInline();
								}).datepicker("show").datepicker('update', initialValue)
							});
						}

						// FIXME show combobox for categorical traits
						$(cell).css('overflow', 'visible');

						$timeout(function () {
							/**
							 * Initiate interaction with the input so that clicks on other parts of the page
							 * will trigger blur immediately. Also necessary to initiate datepicker
							 * This also avoids temporary click handler on body
							 * FIXME is there a better way?
							 */
							$(cell).find('a.ui-select-match, input').click().focus();
						}, 100);
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

				if ($scope.isPendingView) {
					deferred.resolve(true);
					return deferred.promise;
				}

				var invalid = validateDataOutOfScaleRange(cellDataValue, columnData);

				if (invalid) {
					var confirmModal = $scope.openConfirmModal(observationOutOfRange, keepLabel, discardLabel);
					confirmModal.result.then(deferred.resolve);
				} else {
					deferred.resolve(true);
				}

				return deferred.promise;
			}

			function loadTable() {
				detachCategoricalDisplayBtn();

				/**
				 * We need to reinitilize all this because
				 * if we use column.visible an change the columns with just
				 * 		$scope.dtColumns = columnsObj.columns;
				 * datatables is breaking with error:
				 * Cannot read property 'clientWidth' of null
				 */
				var dtColumnsPromise = $q.defer();
				var dtColumnDefsPromise = $q.defer();
				$scope.dtColumns = dtColumnsPromise.promise;
				$scope.dtColumnDefs = dtColumnDefsPromise.promise;
				$scope.dtOptions = null;

				loadColumns().then(function (columnsObj) {
					$scope.dtOptions = getDtOptions();
					dtColumnsPromise.resolve(columnsObj.columns);
					dtColumnDefsPromise.resolve(columnsObj.columnsDef);

					attachCategoricalDisplayBtn();
				});
			}

			function loadColumns() {
				return datasetService.getColumns(subObservationSet.id).then(function (columnsData) {
					subObservationSet.columnsData = columnsData;
					var columnsObj = $scope.columnsObj = subObservationSet.columnsObj = mapColumns(columnsData);

					// if not needed when implementing review -> remove
					subObservationSet.columnMap = {};
					angular.forEach(columnsData, function (columnData) {
						subObservationSet.columnMap[columnData.termId] = columnData;
					});

					return columnsObj;
				});
			}

			function mapColumns(columnsData) {
				var columns = [],
					columnsDef = [];

				angular.forEach(columnsData, function (columnData, index) {
					if (columnData.possibleValues) {
						columnData.possibleValuesByValue = {};
						angular.forEach(columnData.possibleValues, function (possibleValue) {
							// so we can use "Please Choose"=empty value
							possibleValue.displayValue = possibleValue.name;
							// convenience map to avoid looping later
							columnData.possibleValuesByValue[possibleValue.name] = possibleValue;
						});
						// waiting for https://github.com/angular-ui/ui-select/issues/152
						columnData.possibleValues.unshift({name: '', displayValue: 'Please Choose', displayDescription: 'Please Choose'});
					}

					// store formula info to update out-of-sync status after edit
					if (columnData.formula && columnData.formula.inputs) {
						columnData.formula.inputs.forEach(function (input) {
							$scope.columnDataByInputTermId[input.id] = columnData;
						});
					}
					columnData.index = index;

					columns.push({
						title: columnData.alias,
						data: function (row) {
							return row.variables[columnData.name];
						},
						visible: hiddenColumns.indexOf(columnData.termId) < 0,
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
					} else if (!columnData.factor) { // variates
						columnsDef.push({
							targets: columns.length - 1,
							createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
								processCell(td, cellData, rowData, columnData);
							},
							render: function (data, type, full, meta) {

								if (!data) {
									return '';
								}

								function renderByDataType(value, columnData) {
									if (columnData.dataTypeId === 1130) {
										return renderCategoricalValue(value, columnData);
									} else if (columnData.dataTypeId === 1110) {
										return getDisplayValueForNumericalValue(value);
									} else {
										return EscapeHTML.escape(value);
									}
								}

								var value = renderByDataType(data.value, columnData);
								if ($scope.isPendingView && data.draftValue !== null && data.draftValue !== undefined) {
									var existingValue = value;
									value = renderByDataType(data.draftValue, columnData);
									if (existingValue || existingValue === 0) {
										value += " (" + existingValue + ")";
									}
								}

								return value;
							}
						});
					} else {
						columnsDef.push({
							targets: columns.length - 1,
							render: function (data, type, full, meta) {

								if (!data) {
									return '';
								}

								if (columnData.dataTypeId === 1130) {
									return renderCategoricalValue(data && data.value, columnData);
								}

								return data && EscapeHTML.escape(data.value);
							}
						});
					}

					// Manually add SAMPLES count column after PLOT_NO field
					if (columnData.termId === 8200) {
						columns.push({
							title: 'SAMPLES',
							visible: true,
							defaultContent: '',
							className: 'factors',
							columnData: { formula: undefined}
						});

						columnsDef.push({
							targets: columns.length - 1,
							render: function (data, type, full, meta) {
								if (full.samplesCount !== '-') {
									return '<a class="gid-link" href="javascript: void(0)" ' +
										'onclick="openSampleSummary(\'' +
										full.variables['OBS_UNIT_ID'].value + '\', null)">' + EscapeHTML.escape(full.samplesCount) + '</a>';
								} else {
									return full.samplesCount;
								}
							}
						});
					}
				});

				return {
					columns: columns,
					columnsDef: columnsDef
				};
			}

			function renderCategoricalValue(value, columnData) {
				var categoricalValue = EscapeHTML.escape(value);

				if (columnData.possibleValues
					&& columnData.possibleValuesByValue
					&& columnData.possibleValuesByValue[categoricalValue]
					&& columnData.possibleValuesByValue[categoricalValue].displayDescription
					&& categoricalValue !== 'missing') {

					var displayDescription = columnData.possibleValuesByValue[categoricalValue].displayDescription;
					if (displayDescription) {
						var categoricalNameDom = '<span class="fbk-measurement-categorical-name"'
							+ ($scope.isCategoricalDescriptionView ? ' style="display: none; "' : '')
							+ ' >'
							+ categoricalValue + '</span>';
						var categoricalDescDom = '<span class="fbk-measurement-categorical-desc"'
							+ (!$scope.isCategoricalDescriptionView ? ' style="display: none; "' : '')
							+ ' >'
							+ displayDescription + '</span>';

						categoricalValue = categoricalNameDom + categoricalDescDom;
					}
				}
				return categoricalValue;
			}

			function validateNumericRange(minVal, maxVal, value, invalid) {
				if (parseFloat(value) < parseFloat(minVal) || parseFloat(value) > parseFloat(maxVal)) {

					invalid = true;
				}
				return invalid;
			}

			function validateCategoricalValues(columnData, cellDataValue, invalid) {
				if (columnData.possibleValues
					&& columnData.possibleValues.find(function (possibleValue) {
						return possibleValue.name === cellDataValue;
					}) === undefined
					&& cellDataValue !== 'missing') {

					invalid = true;
				}
				return invalid;
			}

			function validateDataOutOfScaleRange(cellDataValue, columnData) {
				var invalid = false;

				var value = cellDataValue;
				var minVal = columnData.scaleMinRange;
				var maxVal = columnData.scaleMaxRange;

				invalid = validateNumericRange(minVal, maxVal, value, invalid);
				invalid = validateCategoricalValues(columnData, cellDataValue, invalid);
				return invalid;
			}

			function validateDataOutOfRange(cellDataValue, columnData) {
				var invalid = false;

				var value = cellDataValue;
				var minVal = (columnData.variableMinRange || columnData.variableMinRange === 0) || columnData.scaleMinRange;
				var maxVal = (columnData.variableMaxRange || columnData.variableMaxRange === 0) || columnData.scaleMaxRange;

				invalid = validateNumericRange(minVal, maxVal, value, invalid);
				invalid = validateCategoricalValues(columnData, cellDataValue, invalid);
				return invalid;
			}

			function processCell(td, cellData, rowData, columnData) {
				$(td).removeClass('accepted-value');
				$(td).removeClass('invalid-value');
				$(td).removeClass('manually-edited-value');

				if ($scope.isPendingView) {
					if (cellData.draftValue === null || cellData.draftValue === undefined) {
						$(td).text('');
						$(td).attr('disabled', true);
						return;
					}
					var invalid = validateDataOutOfRange(cellData.draftValue, columnData);

					if (invalid) {
						$(td).addClass('invalid-value');
					}

					return;
				}

				if (cellData.value || cellData.value === 0) {
					var invalid = validateDataOutOfRange(cellData.value, columnData);

					if (invalid) {
						$(td).addClass('accepted-value');
					}
				}
				if (cellData.status) {
					var status = cellData.status;
					if (!cellData.observationId) {
						return;
					}
					$(td).removeAttr('title');
					var toolTip = 'GID: ' + rowData.variables.GID.value + ' Designation: ' + rowData.variables.DESIGNATION.value;
					if (status === 'MANUALLY_EDITED') {
						$(td).attr('title', toolTip + ' manually-edited-value');
						$(td).addClass('manually-edited-value');
					} else if (status === 'OUT_OF_SYNC') {
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
					columnData: '=',
					isCategoricalDescriptionView: '='
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
