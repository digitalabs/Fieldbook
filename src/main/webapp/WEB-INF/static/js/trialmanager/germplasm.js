/*global angular, openListTree, displaySelectedGermplasmDetails*/

(function () {
	'use strict';

	var manageTrialAppModule = angular.module('manageTrialApp');

	manageTrialAppModule.controller('GermplasmCtrl',
		['$scope', '$rootScope', '$q', '$compile', 'TrialManagerDataService', 'DTOptionsBuilder', 'studyStateService', 'studyEntryService', 'germplasmStudySourceService',
			'datasetService', '$timeout', '$uibModal',
			function ($scope, $rootScope, $q, $compile, TrialManagerDataService, DTOptionsBuilder, studyStateService, studyEntryService, germplasmStudySourceService,
					  datasetService, $timeout, $uibModal) {

				$scope.settings = TrialManagerDataService.settings.germplasm;
				$scope.trialMeasurement = {hasMeasurement: studyStateService.hasGeneratedDesign()};
				$scope.isHideDelete = studyStateService.hasGeneratedDesign();
				$scope.addVariable = !studyStateService.hasGeneratedDesign();
				$scope.selectedItems = [];
				$scope.numberOfEntries = 0;

				var tableLoadedResolve;
				$scope.tableLoadedPromise = new Promise(function (resolve) {
					tableLoadedResolve = resolve;
				});
				var tableRenderedResolve;
				$scope.tableRenderedPromise = new Promise(function (resolve) {
					tableRenderedResolve = resolve;
				});

				var dtColumnsPromise = $q.defer();
				var dtColumnDefsPromise = $q.defer();
				$scope.dtColumns = dtColumnsPromise.promise;
				$scope.dtColumnDefs = dtColumnDefsPromise.promise;
				$scope.dtOptions = null;

				$scope.totalItems = 0;
				$scope.isAllPagesSelected = false;

				loadTable();

				$rootScope.$on("reloadStudyEntryTableData", function(){
					$scope.reloadStudyEntryTableData();
				});

				function table() {
					return $scope.nested.dtInstance.DataTable;
				}

				function getDtOptions() {
					return addCommonOptions(DTOptionsBuilder.newOptions()
						.withOption('ajax',
							function (d, callback) {
								$.ajax({
									type: 'POST',
									url: studyEntryService.getStudyEntries() + getPageQueryParameters(d),
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
						.withOption('destroy', true)
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
					$timeout(function () {
						table().columns.adjust();
					});
				}

				function loadTable() {
					/**
					 * We need to reinitilize all this because
					 * if we use column.visible an change the columns with just
					 *        $scope.dtColumns = columnsObj.columns;
					 * datatables is breaking with error:
					 * Cannot read property 'clientWidth' of null
					 */

					$scope.nested = {};
					$scope.nested.dtInstance = null;

					$scope.columnsData = {};
					$scope.columnsObj = {};
					var dtColumnsPromise = $q.defer();
					var dtColumnDefsPromise = $q.defer();
					$scope.dtColumns = dtColumnsPromise.promise;
					$scope.dtColumnDefs = dtColumnDefsPromise.promise;
					$scope.dtOptions = null;

					setNumberOfEntries();

					return loadColumns().then(function (columnsObj) {
						$scope.dtOptions = getDtOptions();

						dtColumnsPromise.resolve(columnsObj.columns);
						dtColumnDefsPromise.resolve(columnsObj.columnsDef);

						$scope.selectedItems = [];
						tableLoadedResolve();
					});
				}

				function loadColumns() {
					return studyEntryService.getEntryTableColumns().then(function (columnsData) {
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
									$(td).append($compile('<span><input type="checkbox" ng-click="toggleSelect(' + rowData.entryId + ')"></span>')($scope));
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
									return '<a class="check-href edit-check' + full.entryId + '" href="javascript: void(0)" ' +
										'onclick="showChangeEntryTypeModal(\'' + full.entryId + '\',\'' + data.value + '\',\'' + data.studyEntryPropertyId + '\')">' + EscapeHTML.escape(getCategoricalValue(data.value, columnData)) + '</a>';
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

				function getCategoricalValue(value, columnData) {
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

					if (possibleValue && possibleValue.displayDescription) {
						return possibleValue.description;
					}
					return value;
				}

				$scope.showImportListBrowser = !TrialManagerDataService.applicationData.germplasmListSelected;

				$scope.showStudyEntriesTable = TrialManagerDataService.applicationData.germplasmListSelected;

				$scope.showClearList = TrialManagerDataService.applicationData.germplasmListSelected && !studyStateService.hasGeneratedDesign();

				$scope.showUpdateImportListButton = TrialManagerDataService.applicationData.germplasmListSelected && !studyStateService.hasGeneratedDesign() && !$scope.showImportListBrowser;

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
					TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.numberOfEntries;
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

				$(document).on('germplasmListUpdated', function () {
					TrialManagerDataService.applicationData.germplasmListSelected = true;
				});

				$scope.openGermplasmTree = function () {
					openListTree(1, $scope.germplasmListSelected);
				};

				$scope.updateModifyList = function () {
					$scope.showImportListBrowser = true;
					$scope.showUpdateImportListButton = false;
					showGermplasmDetailsSection();
				};

				$scope.showUpdateImportList = function () {
					return $scope.showUpdateImportListButton;
				};

				$scope.showClearListButton = function() {
					return $scope.showClearList;
				}

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

				$scope.validateGermplasmForReplacement = function() {
					germplasmStudySourceService.searchGermplasmStudySources({}, 0, 1).then((germplasmStudySourceTable) => {

						// Check if study has advance or cross list
						if (germplasmStudySourceTable.data.length > 0) {
						showAlertMessage('', $.germplasmMessages.studyHasCrossesOrSelections);
					} else {
						if ($scope.selectedItems.length === 0) {
							showAlertMessage('', $.germplasmMessages.selectEntryForReplacement);
						} else if ($scope.selectedItems.length !== 1) {
							showAlertMessage('', $.germplasmMessages.selectOnlyOneEntryForReplacement);
						} else {
							$scope.replaceGermplasm($scope.selectedItems[0]);
						}
					}
				});
				};

				$scope.toggleSelect = function (data) {
					var idx = $scope.selectedItems.indexOf(data);
					if (idx > -1) {
						$scope.selectedItems.splice(idx, 1)
					} else {
						$scope.selectedItems.push(data);
					}
				};

				$scope.openReplaceGermplasmModal = function(entryId) {
					$uibModal.open({
						templateUrl: '/Fieldbook/static/angular-templates/germplasm/replaceGermplasm.html',
						controller: "replaceGermplasmCtrl",
						size: 'md',
						resolve: {
							entryId: function () {
								return entryId;
							},
						},
					});
				};


				$scope.replaceGermplasm = function(entryId) {
					if (studyStateService.hasGeneratedDesign()) {
						var modalConfirmReplacement = $scope.openConfirmModal($.germplasmMessages.replaceGermplasmWarning, 'Yes','No');
						modalConfirmReplacement.result.then(function (shouldContinue) {
							if (shouldContinue) {
								$scope.openReplaceGermplasmModal(entryId);
							}
						});
					} else {
						$scope.openReplaceGermplasmModal(entryId);
					}

				};

				$scope.saveStudyEntries = function (listId) {

					studyEntryService.deleteEntries().then(function () {
						studyEntryService.saveStudyEntries(listId).then(function(res){
							TrialManagerDataService.applicationData.germplasmListSelected = true;
							setNumberOfEntries();
							$scope.reloadStudyEntryTableData();
							$scope.showImportListBrowser = false;
							$scope.showUpdateImportListButton = true;
							$scope.showStudyEntriesTable = true;
							$scope.showClearList = true;
						});
					});
				};

				$scope.resetStudyEntries = function() {
					var modalConfirmCancellation = $scope.openConfirmModal($.fieldbookMessages.confirmResetStudyEntries, 'Confirm', 'Cancel');
					modalConfirmCancellation.result.then(function (shouldContinue) {
						if (shouldContinue) {
							studyEntryService.deleteEntries().then(function () {
								$scope.numberOfEntries = 0;
								$scope.showImportListBrowser = true;
								$scope.showUpdateImportListButton = false;
								$scope.showStudyEntriesTable = false;
								$scope.showClearList = false;
								TrialManagerDataService.applicationData.germplasmListSelected = false;
							});
						}
					});
				}

				$scope.onRemoveVariable = function (variableType, variableIds) {
					var deferred = $q.defer();
					datasetService.getDatasets([4]).then(function (data) {
						angular.forEach(data, function (dataset) {
							datasetService.removeVariables(dataset.datasetId, variableIds).then(function () {
								deferred.resolve(true);
								showSuccessfulMessage('', $.germplasmMessages.removeVariableSuccess);
								$rootScope.navigateToTab('germplasm', {reload: true});
							}, function (response) {
								if (response.errors && response.errors.length) {
									showErrorMessage('', response.errors[0].message);
								} else {
									showErrorMessage('', ajaxGenericErrorMsg);
								}
							});
						});
					});
					return deferred.promise;
				};

				$scope.onAddVariable = function(result, variableType) {
					var variable = undefined;
					angular.forEach(result, function (val) {
						variable = val.variable;
					});
					datasetService.getDatasets([4]).then(function (data) {
						angular.forEach(data, function (dataset) {
							var variableName = variable.alias ? variable.alias : variable.name;
							datasetService.addVariables(dataset.datasetId, {
								variableTypeId: variableType,
								variableId: variable.cvTermId,
								studyAlias: variableName
							}).then(function () {
								showSuccessfulMessage('', $.germplasmMessages.addVariableSuccess.replace("{0}", variableName));
							})
						});
					});
				};

				$scope.onHideCallback = function () {
					$rootScope.navigateToTab('germplasm', {reload: true});
				};

				function setNumberOfEntries() {
					studyEntryService.countStudyEntries().then(function(count) {
						$scope.numberOfEntries = count;
					});
				}

				$scope.showPopOverCheck = function(entryId, currentValue, studyEntryPropertyId) {
					$uibModal.open({
						templateUrl: '/Fieldbook/static/angular-templates/germplasm/changeStudyEntryEntryTypeModal.html',
						controller: "editEntryTypeCtrl",
						size: 'md',
						resolve: {
							entryId: function () {
								return entryId;
							},
							currentValue: function () {
								return currentValue;
							},
							studyEntryPropertyId: function () {
								return studyEntryPropertyId;
							}
						},
						controllerAs: 'ctrl'
					});
				};

				$scope.showManageEntryTypePopup = function() {
					$uibModal.open({
						templateUrl: '/Fieldbook/static/angular-templates/germplasm/addEntryTypeModal.html',
						controller: "addEntryTypeCtrl",
						size: 'md',
						resolve: {
						},
						controllerAs: 'ctrl'
					});
				}

				$scope.reloadStudyEntryTableData = function() {
					table().ajax.reload();
				}
			}]);

	manageTrialAppModule.controller('replaceGermplasmCtrl', ['$scope', '$rootScope', '$uibModalInstance', 'studyEntryService', 'entryId',
		function ($scope, $rootScope, $uibModalInstance, studyEntryService, entryId) {
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
					// if there are multiple entries selected, get only the first entry for replacement
					studyEntryService.replaceStudyGermplasm(entryId, newGid).then(function (response) {
						showSuccessfulMessage('', $.germplasmMessages.replaceGermplasmSuccessful);
						$rootScope.$emit("reloadStudyEntryTableData", {});
						$uibModalInstance.close();
					}, function(errResponse) {
						showErrorMessage($.fieldbookMessages.errorServerError,  errResponse.errors[0].message);
						$uibModalInstance.close();
					});
				}

			};
		}
	]);

	manageTrialAppModule.controller('editEntryTypeCtrl', ['$scope', '$rootScope', '$uibModalInstance', 'studyEntryService', 'entryId', 'currentValue',
		'studyEntryPropertyId',	function ($scope, $rootScope, $uibModalInstance, studyEntryService, entryId, currentValue, studyEntryPropertyId) {

			$scope.selected = {};
			$scope.entryTypes = [];
			$scope.init = function () {
				studyEntryService.getEntryTypes().then(function (entryTypes) {
					buildEntryTypes(entryTypes);
				})
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};


			$scope.editEntryType = function () {
				studyEntryService.updateStudyEntry(entryId, $scope.selected.entryType.id, studyEntryPropertyId).then(function () {
					$uibModalInstance.close();
					$rootScope.$emit("reloadStudyEntryTableData", {});
				});
			};

			function buildEntryTypes(entryTypes) {
				entryTypes.forEach(function (entryType) {
					$scope.entryTypes.push(entryType);
					if(entryType.id === parseInt(currentValue)) {
						$scope.selected.entryType = entryType;
					}
				});
			}

			$scope.init();
		}
	]);

	manageTrialAppModule.controller('addEntryTypeCtrl', ['$scope', '$rootScope', '$uibModalInstance', 'studyEntryService',
		function ($scope, $rootScope, $uibModalInstance, studyEntryService) {

			$scope.entryTypeValues = [];
			$scope.entryTypeCode = '';
			$scope.entryTypeValue = '';
			$scope.showAddButton = true;
			$scope.showUpdateButton = false;
			$scope.showDeleteButton = false;
			$scope.suggestions_obj = [];

			$scope.init = function () {
				studyEntryService.getEntryTypes().then(function (entryTypes) {
					populateEntryTypesSelect(entryTypes);
				});
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.addEntryType = function () {
				if(validateCheckFields()) {
					var entryType = {
						id: null,
						name: $('#comboCheckCode').select2('data').text,
						description: $scope.entryTypeValue,
						rank: 0
					};

					studyEntryService.addOrUpdateStudyEntryType(entryType).then(function () {
						showSuccessfulMessage('', getFormattedMessage($.studyEntryTypeMessages.addStudyEntryTypeSuccess, $('#comboCheckCode').select2('data').text));
						$uibModalInstance.dismiss();
					});
				}
			};

			$scope.updateEntryType = function () {
				if(validateCheckFields()) {
					var entryType = {
						id: $('#comboCheckCode').select2('data').id,
						name: $('#comboCheckCode').select2('data').text,
						description: $scope.entryTypeValue,
						rank: $('#comboCheckCode').select2('data').rank
					};

					studyEntryService.addOrUpdateStudyEntryType(entryType).then(function () {
						showSuccessfulMessage('', getFormattedMessage($.studyEntryTypeMessages.updateStudyEntryTypeSuccess, $('#comboCheckCode').select2('data').text));
						$uibModalInstance.dismiss();
					});
				}

			};

			$scope.deleteEntryType = function () {
				var studyEntryTypeId = $('#comboCheckCode').select2('data').id;
				studyEntryService.isStudyEntryTypeUsed(studyEntryTypeId).then(function (isUsed) {
					if(!isUsed) {
						studyEntryService.deleteStudyEntryType(studyEntryTypeId).then(function () {
							showSuccessfulMessage('', getFormattedMessage($.studyEntryTypeMessages.deleteStudyEntryTypeSuccess, $('#comboCheckCode').select2('data').text));
							$uibModalInstance.dismiss();
						});
					} else {
						showErrorMessage('', getFormattedMessage($.studyEntryTypeMessages.deleteStudyEntryTypeFail, $('#comboCheckCode').select2('data').text));
					}
				});
			};

			function validateCheckFields() {
				if (!$('#comboCheckCode').select2('data')) {
					showErrorMessage('', $.studyEntryTypeMessages.codeRequiredError);
					return false;
				} else if ($scope.entryTypeValue === '') {
					showErrorMessage('', $.studyEntryTypeMessages.valueRequiredError);
					return false;
				} else if (!isValueUnique()) {
					showErrorMessage('', $.studyEntryTypeMessages.valueNotUniqueError);
					return false;
				}

				return true;
			}

			function isValueUnique() {
				'use strict';
				return !$scope.entryTypeValues.includes($scope.entryTypeValue);
			}


			function populateEntryTypesSelect(entryTypes) {
				$.each(entryTypes, function (index, value) {
					var dataObj = {
						'id': value.id,
						'text': value.name,
						'description': value.description,
						'originalText': value.name,
						'rank': value.rank
					};
					$scope.suggestions_obj.push(dataObj);
					$scope.entryTypeValues.push(value.description);
				});
				// if combo to create is one of the ontology combos, add an onchange event
				// to populate the description based on the selected value
				$('#comboCheckCode')
					.select2(
						{
							query: function (query) {
								var data = {
									results: sortByKey($scope.suggestions_obj, 'text')
								};
								// return the array that matches
								data.results = $.grep(data.results, function (
									item, index) {
									if (item.text.toUpperCase().indexOf(query.term.toUpperCase()) === 0) {
										return true;
									}
									return false;
								});
								if (data.results.length === 0 || data.results[0].text.toUpperCase() != query.term.toUpperCase()) {
									data.results.unshift({
										id: query.term,
										text: query.term
									});
								}
								query.callback(data);
							},
							dropdownCssClass: 's2-nosearch-icon'
						})
					.on(
						'change',
						function () {
							if ($('#comboCheckCode').select2('data')) {
								if ($('#comboCheckCode').select2('data').id == $('#comboCheckCode').select2('data').text) {
									$scope.entryTypeValue = '';
									$scope.showAddButton = true;
									$scope.showUpdateButton = false;
									$scope.showDeleteButton = false;
								} else {
									$scope.entryTypeValue = $('#comboCheckCode').select2('data').description;
									$scope.showAddButton = false;
									$scope.showUpdateButton = true;
									$scope.showDeleteButton = true;
								}
								$scope.$apply();
							}
						});
			};

			function getFormattedMessage(message, entryTypeCode) {
				return message.replace("{0}", entryTypeCode);
			};

			$scope.init();
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
	};

})();
