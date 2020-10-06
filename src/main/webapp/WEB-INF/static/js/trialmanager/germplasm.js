/*global angular, openListTree, displaySelectedGermplasmDetails*/

(function () {
	'use strict';

	var manageTrialAppModule = angular.module('manageTrialApp');

	manageTrialAppModule.controller('GermplasmCtrl',
		['$scope', '$q', '$compile', 'TrialManagerDataService', 'DTOptionsBuilder', 'studyStateService', 'studyGermplasmService', 'germplasmStudySourceService',
			function ($scope, $q, $compile, TrialManagerDataService, DTOptionsBuilder, studyStateService, studyGermplasmService, germplasmStudySourceService) {

				$scope.settings = TrialManagerDataService.settings.germplasm;
				$scope.isOpenStudy = TrialManagerDataService.isOpenStudy;
				$scope.trialMeasurement = {hasMeasurement: studyStateService.hasGeneratedDesign()};
				$scope.selectedItems = [];

				var tableLoadedResolve;
				$scope.tableLoadedPromise = new Promise(function (resolve) {
					tableLoadedResolve = resolve;
				});
				var tableRenderedResolve;
				$scope.tableRenderedPromise = new Promise(function (resolve) {
					tableRenderedResolve = resolve;
				});
				$scope.nested = {};
				$scope.nested.dtInstance = null;

				$scope.columnsData = {};
				$scope.columnsObj = {};
				if ($scope.isOpenStudy()) {
					var tableId = '#germplasm-table';
					var dtColumnsPromise = $q.defer();
					var dtColumnDefsPromise = $q.defer();
					$scope.dtColumns = dtColumnsPromise.promise;
					$scope.dtColumnDefs = dtColumnDefsPromise.promise;
					$scope.dtOptions = null;

					$scope.totalItems = 0;
					$scope.selectedItems = [];
					$scope.isAllPagesSelected = false;
					$scope.columnFilter = {
						selectAll: function () {
							this.columnData.possibleValues.forEach(function (value) {
								value.isSelectedInFilters = this.columnData.isSelectAll;
							}.bind(this));
						},
						selectOption: function (selected) {
							if (!selected) {
								this.columnData.isSelectAll = false;
							}
						},
						search: function (item) {
							var query = $scope.columnFilter.columnData.query;
							if (!query) {
								return true;
							}
							if (item.name.indexOf(query) !== -1 || item.displayDescription.indexOf(query) !== -1) {
								return true;
							}
							return false;
						}
					};

					loadTable();
				}

				function table() {
					return $scope.nested.dtInstance.DataTable;
				}

				function getDtOptions() {
					return addCommonOptions(DTOptionsBuilder.newOptions()
						.withOption('ajax',
							function (d, callback) {
								$.ajax({
									type: 'POST',
									url: studyGermplasmService.getStudyEntries() + getPageQueryParameters(d),
									data: JSON.stringify({
										draw: d.draw
									}),
									success: function (res) {
										callback(res);
									},
									contentType: 'application/json',
									beforeSend: function (xhr) {
										xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
									},
								});
							})
						.withDataProp('data')
						.withOption('serverSide', true)
						.withOption('initComplete', initCompleteCallback)
						.withOption('headerCallback', headerCallback)
						.withOption('drawCallback', drawCallback));
				}


				function getPageQueryParameters(data) {
					var order = data.order && data.order[0];
					var pageQuery = '?size=' + data.length
						+ '&page=' + ((data.length === 0) ? 0 : data.start / data.length);
					return pageQuery;
				}

				function addCommonOptions(options) {
					return options
						.withOption('processing', true)
						.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
						.withOption('scrollY', '500px')
						.withOption('scrollCollapse', true)
						.withOption('scrollX', '100%')
						.withOption('order', [[2, 'desc']]) //gid
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
						.withDOM('<"pull-left fbk-left-padding"r>' + //
							'<"pull-right"B>' + //
							'<"clearfix">' + //
							'<"row add-top-padding-small"<"col-sm-12"t>>' + //
							'<"row"<"col-sm-12 paginate-float-center"<"pull-left"i><"pull-right"l>p>>')
						.withButtons([{
							extend: 'colvis',
							className: 'fbk-buttons-no-border fbk-colvis-button',
							text: '<i class="glyphicon glyphicon-th"></i>',
							columns: ':gt(0)'
						}])
						.withPaginationType('full_numbers');
				}

				function initCompleteCallback() {
					table().columns().every(function () {
						$(this.header())
							.prepend($compile('<span class="glyphicon glyphicon-bookmark" style="margin-right: 10px; color:#1b95b2;"' +
								' ng-if="isVariableBatchActionSelected(' + this.index() + ')"> </span>')($scope))
							.append($compile('<span ng-if="!isCheckBoxColumn(' + this.index() + ')" class="glyphicon glyphicon-filter" ' +
								' style="cursor:pointer; padding-left: 5px;"' +
								' popover-placement="bottom"' +
								' ng-class="getFilteringByClass(' + this.index() + ')"' +
								' popover-append-to-body="true"' +
								' popover-trigger="\'outsideClick\'"' +
								// does not work with outsideClick
								// ' popover-is-open="columnFilter.isOpen"' +
								' ng-if="isVariableFilter(' + this.index() + ')"' +
								' ng-click="openColumnFilter(' + this.index() + ')"' +
								' uib-popover-template="\'columnFilterPopoverTemplate.html\'"></span>')($scope))
							.prepend($compile('<span ng-if="isCheckBoxColumn(' + this.index() + ')">'
								+ '<input type="checkbox" title="select current page" ng-checked="isPageSelected()"  ng-click="onSelectPage()">'
								+ '</span>')($scope));
					});
					adjustColumns();
					tableRenderedResolve();
				}

				function headerCallback(thead, data, start, end, display) {
					table().columns().every(function () {
						var column = $scope.columnsObj.columns[this.index()];
						if (column.columnData.formula) {
							$(this.header()).addClass('derived-trait-column-header');
						}
					});
				}

				function drawCallback() {
					adjustColumns();
				}

				function adjustColumns() {
					if ($scope.hasInstances) {
						$timeout(function () {
							table().columns.adjust();
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

				function loadTable() {
					/**
					 * We need to reinitilize all this because
					 * if we use column.visible an change the columns with just
					 *        $scope.dtColumns = columnsObj.columns;
					 * datatables is breaking with error:
					 * Cannot read property 'clientWidth' of null
					 */
					var dtColumnsPromise = $q.defer();
					var dtColumnDefsPromise = $q.defer();
					$scope.dtColumns = dtColumnsPromise.promise;
					$scope.dtColumnDefs = dtColumnDefsPromise.promise;
					$scope.dtOptions = null;

					return loadColumns().then(function (columnsObj) {
						$scope.dtOptions = getDtOptions();

						dtColumnsPromise.resolve(columnsObj.columns);
						dtColumnDefsPromise.resolve(columnsObj.columnsDef);

						tableLoadedResolve();
					});
				}

				function loadColumns() {
					return studyGermplasmService.getEntryTableColumns().then(function (columnsData) {
						$scope.columnsData = addCheckBoxColumn(columnsData);
						var columnsObj = $scope.columnsObj = mapColumns($scope.columnsData);
						return columnsObj;
					});
				}

				function addCheckBoxColumn(columnsData) {
					// copy array to avoid modifying the parameter (unit test might reuse the same object)
					var columns = columnsData.slice();
					columns.unshift({
						alias: "",
						factor: true,
						name: "CHECK",
						termId: -3,
					});
					return columns;
				}

				function mapColumns(columnsData) {
					$scope.columnDataByInputTermId = {};

					var columns = [],
						columnsDef = [];

					angular.forEach(columnsData, function (columnData, index) {
						if (columnData.possibleValues) {
							columnData.possibleValuesByName = {};
							columnData.possibleValuesById = {};
							angular.forEach(columnData.possibleValues, function (possibleValue) {
								// so we can use "Please Choose"=empty value
								possibleValue.displayValue = possibleValue.name;
								// convenience map to avoid looping later
								columnData.possibleValuesByName[possibleValue.name] = possibleValue;
								columnData.possibleValuesById[possibleValue.id] = possibleValue;
							});
							// waiting for https://github.com/angular-ui/ui-select/issues/152
							columnData.possibleValues.unshift({name: '', displayValue: 'Please Choose', displayDescription: 'Please Choose'});
						}
						columnData.index = index;

						columns.push({
							title: columnData.alias,
							name: columnData.alias,
							data: function (row) {
								if(columnData.termId === -3) {
									return '';
								} else {
									return row.properties[columnData.termId];
								}
							},
							visible: true,
							defaultContent: '',
							columnData: columnData
						});

						// CheckBox Column
						if (columnData.index === 0) {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
									$(td).append($compile('<span><input type="checkbox" ng-checked="isSelected(' + rowData.observationUnitId + ')" ng-click="toggleSelect(' + rowData.observationUnitId + ')"></span>')($scope));
								}
							});
						} else if (columnData.termId === 8240 || columnData.termId === 8250) {
							// GID or DESIGNATION
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									return '<a class="gid-link" href="javascript: void(0)" ' +
										'onclick="openGermplasmDetailsPopopWithGidAndDesig(\'' +
										full.gid + '\',\'' + full.designation + '\')">' + EscapeHTML.escape(data.value) + '</a>';
								}
							});
						} else if (columnData.termId === 8255) {
							// ENTRY_TYPE
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									return renderCategoricalValue(data.value, columnData);
								}
							});
						} else {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {

									if (!data) {
										return '';
									}

									if (columnData.dataTypeId === 1130) {
										return renderCategoricalValue(data.value, columnData);
									}

									return EscapeHTML.escape(data.value);
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
					var possibleValue = null;

					if (columnData.possibleValues) {
						/* FIXME fix data model
						 *  Some variables don't store the cvterm.name (like traits in phenotype)
						 *  but the cvterm.cvterm_id (like treatment factors in nd_experimentprop).
						 *  This workaround will work most of the time with exception of out-of-bound categorical values that coincides
						 *  with the cvterm_id, though it's unlikely because the ids are not small numbers and it's not possible now to insert
						 *  outliers for categorical variables.
						 */
						possibleValue = columnData.possibleValuesByName[value] || columnData.possibleValuesById[value];
					}

					if (possibleValue
						&& possibleValue.displayDescription) {

						value = '<span class="fbk-measurement-categorical-desc">'
							+ EscapeHTML.escape(possibleValue.description) + '</span>';
					}
					return value;
				}

				$scope.showImportListBrowser = !($scope.isOpenStudy() && TrialManagerDataService.applicationData.germplasmListSelected);

				$scope.showStudyEntriesTable = $scope.isOpenStudy() && TrialManagerDataService.applicationData.germplasmListSelected;

				$scope.labels = {};
				$scope.labels.germplasmFactors = {
					label: 'Temp label here',
					placeholderLabel: 'Temp placeholder here'
				};

				$scope.updateOccurred = false;

				$scope.$on('deleteOccurred', function () {
					$scope.updateOccurred = true;
				});

				$scope.$on('variableAdded', function () {
					$scope.updateOccurred = true;
				});

				$scope.handleSaveEvent = function () {
					$scope.updateOccurred = false;
					TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.getTotalListNo();
				};

				// function called whenever the user has successfully selected a germplasm list
				$scope.germplasmListSelected = function () {
					// validation requiring user to re-generate experimental design after selecting new germplasm list is removed as per new maintain germplasm list functionality
					$scope.updateOccurred = false;
				};

				$scope.germplasmListCleared = function () {
					TrialManagerDataService.applicationData.germplasmListCleared = true;
					TrialManagerDataService.applicationData.germplasmListSelected = false;
				};

				$scope.hasUnsavedGermplasmChanges = function () {
					return TrialManagerDataService.applicationData.germplasmChangesUnsaved;
				};

				$(document).on('germplasmListUpdated', function () {
					TrialManagerDataService.applicationData.germplasmListSelected = true;
					$scope.germplasmChangesOccurred();
					if (TrialManagerDataService.isOpenStudy()) {
						studyStateService.updateOccurred();
					}

				});

				$scope.germplasmChangesOccurred = function() {
					$scope.$apply(function () {
						TrialManagerDataService.applicationData.germplasmChangesUnsaved = true;
					});
				}

				$scope.openGermplasmTree = function () {
					openListTree(1, $scope.germplasmListSelected);
				};

				$scope.updateModifyList = function () {
					$scope.showImportListBrowser = true;
					showGermplasmDetailsSection();
				};

				$scope.showUpdateImportList = function () {
					return $scope.isOpenStudy() && TrialManagerDataService.applicationData.germplasmListSelected && !studyStateService.hasGeneratedDesign() && !$scope.showImportListBrowser;
				};

				$scope.showImportList = function () {
					return $scope.showImportListBrowser;
				};

				$scope.showStudyTable = function() {
					return $scope.showStudyEntriesTable;
				}

				TrialManagerDataService.registerSaveListener('germplasmUpdate', $scope.handleSaveEvent);

				$scope.hasGeneratedDesign = function () {
					return studyStateService.hasGeneratedDesign();
				};

				$scope.disableAddButton = function () {
					return studyStateService.hasGeneratedDesign();
				};
				$scope.displayUpdateButton = function () {
					return $scope.updateOccurred && $scope.listAvailable();
				};

				$scope.listAvailable = function () {
					var entryHtml = $('#numberOfEntries').html();
					return (entryHtml !== '' && parseInt(entryHtml, 10) > 0);
				};

				$scope.getTotalListNo = function () {
					return (parseInt($('#totalGermplasms').val())) ? parseInt($('#totalGermplasms').val()) : 0;
				};

				$scope.updateDataTable = function () {
					$.ajax({
						url: '/Fieldbook/ListManager/GermplasmList/refreshListDetails',
						type: 'GET',
						cache: false,
						data: ''
					}).success(function (html) {
						$('#liExportList').removeClass('fbk-dropdown-select-fade');
						$('#imported-germplasm-list').html(html);
						window.ImportGermplasm.initialize(dataGermplasmList);
						$('#entries-details').css('display', 'block');
						$('#numberOfEntries').html($('#totalGermplasms').val());
						$('#imported-germplasm-list-reset-button').css('opacity', '1');
						$scope.updateOccurred = false;
						TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.getTotalListNo();
						$scope.germplasmChangesOccurred();

						if (!$scope.$$phase) {
							$scope.$apply();
						}

					});

				};

				$scope.validateGermplasmForReplacement = function() {
					germplasmStudySourceService.searchGermplasmStudySources({}, 0, 1).then((germplasmStudySourceTable) => {

						// Check if study has advance or cross list
						if (germplasmStudySourceTable.data.length > 0) {
						showAlertMessage('', $.germplasmMessages.studyHasCrossesOrSelections);
					} else {
						// Validate entry for replacement
						studyGermplasmService.resetSelectedEntries();
						$.each($("input[name='entryId']:checked"), function(){
							studyGermplasmService.toggleSelect($(this).val());
						});
						var selectedEntries = studyGermplasmService.getSelectedEntries();
						if (selectedEntries.length === 0) {
							showAlertMessage('', $.germplasmMessages.selectEntryForReplacement);
						} else if (selectedEntries.length !== 1) {
							showAlertMessage('', $.germplasmMessages.selectOnlyOneEntryForReplacement);
						} else {
							$scope.replaceGermplasm();
						}
					}
				});
				};

				$scope.replaceGermplasm = function() {
					if (studyStateService.hasGeneratedDesign()) {
						var modalConfirmReplacement = $scope.openConfirmModal($.germplasmMessages.replaceGermplasmWarning, 'Yes','No');
						modalConfirmReplacement.result.then(function (shouldContinue) {
							if (shouldContinue) {
								studyGermplasmService.openReplaceGermplasmModal();
							}
						});
					} else {
						studyGermplasmService.openReplaceGermplasmModal();
					}

				};

			}]);

	manageTrialAppModule.controller('replaceGermplasmCtrl', ['$scope', '$uibModalInstance', 'studyContext', 'studyGermplasmService',
		function ($scope, $uibModalInstance, studyContext, studyGermplasmService) {
			var ctrl = this;

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			// Wrap 'showAlertMessage' global function to a controller function so that we can mock it in unit test.
			ctrl.showAlertMessage = function (title, message) {
				showAlertMessage(title, message);
			};


			$scope.performGermplasmReplacement = function () {
				var newGid = $('#replaceGermplasmGID').val();
				var regex = new RegExp('^[0-9]+$');
				if (!regex.test(newGid)) {
					ctrl.showAlertMessage('', 'Please enter valid GID.');
				} else {
					var selectedEntries = studyGermplasmService.getSelectedEntries();
					// if there are multiple entries selected, get only the first entry for replacement
					studyGermplasmService.replaceStudyGermplasm(selectedEntries[0], newGid).then(function (response) {
						showSuccessfulMessage('', $.germplasmMessages.replaceGermplasmSuccessful);
						window.location = '/Fieldbook/TrialManager/openTrial/' + studyContext.studyId;
					}, function(errResponse) {
						showErrorMessage($.fieldbookMessages.errorServerError,  errResponse.errors[0].message);
					});
				}

			};
		}
	]);


})();

// README IMPORTANT: Code unmanaged by angular should go here

/* This will be called when germplasm details page is loaded */
(function() {
	'use strict';

	document.onLoadGermplasmDetails = function() {

		displayGermplasmListTreeTable('germplasmTree');

		changeBrowseGermplasmButtonBehavior(false);

		$('#listTreeModal').off('hide.bs.modal');
		$('#listTreeModal').on('hide.bs.modal', function() {
			TreePersist.saveGermplasmTreeState(true, '#germplasmTree');
			displayGermplasmListTreeTable('germplasmTree');
			changeBrowseGermplasmButtonBehavior(false);
			$(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').hide();
			$(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').hide();
		});

		$('#manageCheckTypesModal').on('hidden.bs.modal', function() {
			reloadCheckTypeDropDown(false, 'checklist-select');
		});

		initializeCheckTypeSelect2(document.checkTypes, [], false, 0, 'comboCheckCode');
		$('#updateCheckTypes').hide();
		$('#deleteCheckTypes').hide();

	};

})();
