/*global angular, openManageLocations,
environmentModalConfirmationText, environmentConfirmLabel, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	angular.module('manageTrialApp').controller('EnvironmentCtrl', ['$scope', '$q', 'TrialManagerDataService', '$uibModal', '$stateParams',
		'$http', 'DTOptionsBuilder', 'LOCATION_ID', 'UNSPECIFIED_LOCATION_ID', '$timeout', 'studyInstanceService', 'studyStateService', 'derivedVariableService', 'studyContext',
		'datasetService', '$compile', '$document',
		function ($scope, $q, TrialManagerDataService, $uibModal, $stateParams, $http, DTOptionsBuilder, LOCATION_ID, UNSPECIFIED_LOCATION_ID, $timeout, studyInstanceService,
				  studyStateService, derivedVariableService, studyContext, datasetService, $compile, $document) {

			var ctrl = this;
			var tableId = '#environment-table';

			$scope.TRIAL_INSTANCE_NO_INDEX = 8170;
			$scope.addVariable = true;
			$scope.instanceInfo = studyInstanceService.instanceInfo;
			$scope.nested = {};
			$scope.nested.dataTable = {};
			$scope.isDisableAddInstance = false;
			$scope.isHideDelete = false;
			$scope.temp = {
				settingMap: {},
				numberOfInstances: $scope.instanceInfo.numberOfInstances
			};

			$scope.settings = TrialManagerDataService.settings.environments;

			$scope.onRemoveVariable = function (variableType, variableIds) {
				return $scope.checkVariableIsUsedInCalculatedVariable(variableIds);
			};

			$scope.onAddVariable = function (result, variableTypeId) {
				var variable = undefined;
				angular.forEach(result, function (val) {
					variable = val.variable;
				});

				datasetService.addVariables(studyContext.trialDatasetId, {
					variableTypeId: variableTypeId,
					variableId: variable.cvTermId,
					studyAlias: variable.alias ? variable.alias : variable.name
				}).then(function () {
					$scope.nested.dataTable.rerender();
					ctrl.initializePossibleValuesMap();
				});
			};

			$scope.checkVariableIsUsedInCalculatedVariable = function (deleteVariables) {
				var deferred = $q.defer();
				var variableIsUsedInOtherCalculatedVariable;

				// Retrieve all formula variables in study
				derivedVariableService.getFormulaVariables(studyContext.measurementDatasetId).then(function (response) {
					//response is null if study is not yet saved
					if (response) {
						var formulaVariables = response.data;
						// Check if any of the deleted variables are formula variables
						angular.forEach(formulaVariables, function (formulaVariable) {
							if (deleteVariables.indexOf(formulaVariable.id) > -1) {
								variableIsUsedInOtherCalculatedVariable = true;
							}
						});
					}

					if (variableIsUsedInOtherCalculatedVariable) {
						var modalInstance = $scope.openConfirmModal(removeVariableDependencyConfirmationText, environmentConfirmLabel);
						modalInstance.result.then(deferred.resolve);
						modalInstance.result.then((isOK) => {
							if (isOK) {
								datasetService.removeVariables(studyContext.trialDatasetId, deleteVariables).then(() => {
									$scope.nested.dataTable.rerender();
								});
							}
						});
					} else {
						deferred.resolve(true);
						datasetService.removeVariables(studyContext.trialDatasetId, deleteVariables).then(() => {
							$scope.nested.dataTable.rerender();
						});
					}
				});
				return deferred.promise;
			};

			$scope.onLocationChange = function (data) {
				studyInstanceService.changeEnvironments(data);
			}

			$scope.buttonsTopWithLocation = [{
				//TODO disable?
				text: $.fieldbookMessages.studyManageSettingsManageLocation,
				className: 'fbk-buttons-no-border fbk-buttons-link',
				action: function () {
					$scope.initiateManageLocationModal();
				}
			},
				{
					extend: 'colvis',
					className: 'fbk-buttons-no-border fbk-colvis-button',
					text: '<i class="glyphicon glyphicon-th dropdown-toggle fbk-show-hide-grid-column"></i>',
					columns: ':gt(0):not(.ng-hide)'
				}];

			$scope.dtOptions = DTOptionsBuilder.newOptions().withDOM('<"fbk-datatable-panel-top"liB>rtp')
				.withButtons($scope.buttonsTopWithLocation.slice())
				.withOption('scrollX', true)
				.withOption('scrollCollapse', true)
				.withOption('deferRender', true);

			$scope.dtOptions.drawCallback = function () {
				var api = $(this).DataTable();

				addCellClickHandler();

				//temporary fix in buttons disappear bug,
				//see https://github.com/l-lin/angular-datatables/issues/502#issuecomment-161166246
				if (api) {
					// remove old set of buttons before recreating them
					if (api.buttons()) {
						api.buttons().remove();
					}
					new $.fn.dataTable.Buttons(api, {
						buttons: $scope.buttonsTopWithLocation.slice()
					});

					$(this).parents('.dataTables_wrapper').find('.dt-buttons').replaceWith(api.buttons().container());
				}
			};

			$scope.renderDisplayValue = function (settingVariable, value) {

				return renderByDataType(settingVariable, value);

				function renderByDataType(settingVariable, value) {

					// If variable is LOCATION_ID variable or categorical, show the description of the selected possible value.
					if (settingVariable.variable.dataTypeId === 1130 || settingVariable.variable.cvTermId === parseInt(LOCATION_ID)) {
						return renderCategoricalValue(settingVariable, value);
					} else {
						return EscapeHTML.escape(value);
					}
				}

				function renderCategoricalValue(settingVariable, value) {
					var displayValue = '';
					if (settingVariable.possibleValuesById && settingVariable.possibleValuesById[value]) {
						displayValue = settingVariable.possibleValuesById[value].description;
					}
					return displayValue;
				}

			}

			$scope.initiateManageLocationModal = function () {
				//TODO $scope.variableDefinition.locationUpdated = false;
				openManageLocations();
			};

			TrialManagerDataService.onUpdateData('environments', function () {
				$scope.temp.numberOfInstances = $scope.instanceInfo.numberOfInstances;
			});

			/* Scope Functions */
			$scope.isDesignAlreadyGenerated = function () {
				return studyStateService.hasGeneratedDesign() || studyStateService.hasListOrSubObs();
			};

			$scope.updateInstanceCount = function () {
				if ($scope.temp.numberOfInstances > $scope.instanceInfo.instances.length) {
					$scope.instanceInfo.numberOfInstances = $scope.temp.numberOfInstances;
					$scope.addInstances($scope.temp.numberOfInstances - $scope.instanceInfo.instances.length);
				} else if ($scope.temp.numberOfInstances < $scope.instanceInfo.instances.length) {
					$scope.instanceInfo.numberOfInstances = $scope.temp.numberOfInstances;
					var instanceIds = [];
					// if new instance count is less than previous value, splice array
					while ($scope.instanceInfo.instances.length > $scope.temp.numberOfInstances) {
						var instance = $scope.instanceInfo.instances.pop();
						instanceIds.push(instance.instanceId);
					}
					studyInstanceService.deleteStudyInstances(instanceIds);
				}
			};

			$scope.deleteInstance = function (index, instanceId) {

				studyInstanceService.getStudyInstance(instanceId).then(function (studyInstance) {

					// Show error if instance cannot be deleted
					if (!studyInstance.canBeDeleted) {
						showErrorMessage('', $.environmentMessages.environmentCannotBeDeleted);
						return;

						// Show confirmation message for overwriting measurements and/or fieldmap
					} else if (studyInstance.hasMeasurements || studyInstance.hasFieldmap) {
						var modalConfirmDelete = $scope.openConfirmModal($.environmentMessages.environmentHasDataThatWillBeLost, 'Yes', 'No');
						modalConfirmDelete.result.then(function (shouldContinue) {
							if (shouldContinue) {
								$scope.continueInstanceDeletion(index, [instanceId]);
							}
						});
					} else {
						$scope.continueInstanceDeletion(index, [instanceId]);
					}
				}, function (errResponse) {
					showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
				});

			};

			// Proceed deleting existing instance
			$scope.continueInstanceDeletion = function (index, instanceIds) {
				studyInstanceService.deleteStudyInstances(instanceIds);
				updateDeletedInstances(index);
				showSuccessfulMessage('', $.environmentMessages.environmentDeletedSuccessfully);
			};


			$scope.updateTrialInstanceNo = function (environments, index) {
				for (var i = 0; i < environments.length; i++) {
					var environment = environments[i];
					var expectedTrialInstanceNo = i + 1;
					var trialInstanceNo = environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX];
					if (trialInstanceNo > expectedTrialInstanceNo) {
						environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = expectedTrialInstanceNo;
					}
				}
			};

			/* Watchers */
			$scope.$watch('instanceInfo.numberOfInstances', function (newVal, oldVal) {
				$scope.temp.numberOfInstances = newVal;
				if (Number(newVal) < Number(oldVal)) {
					TrialManagerDataService.applicationData.hasNewInstanceAdded = false;
				} else if (Number(newVal) > Number(oldVal)) {
					TrialManagerDataService.applicationData.hasNewInstanceAdded = true;
					addCellClickHandler();
				}
			});

			$scope.$watch('settings.managementDetails', function (newVal, oldVal) {
				ctrl.updateInstanceVariables('managementDetails', newVal.keys().length > oldVal.keys().length);
			}, true);

			$scope.$watch('settings.trialConditionDetails', function (newVal, oldVal) {
				ctrl.updateInstanceVariables('trialConditionDetails', newVal.keys().length > oldVal.keys().length);
			}, true);

			$scope.addInstance = function () {

				$scope.isDisableAddInstance = true;

				// create and save the environment in the server
				studyInstanceService.createStudyInstances(1).then(function (studyInstances) {
					angular.forEach(studyInstances, function (studyInstance) {
						// update the environment table
						$scope.createInstance(studyInstance);
						$scope.instanceInfo.numberOfInstances++;
					});
					$scope.isDisableAddInstance = false;
				});

			};

			$scope.addInstances = function (numberOfEnvironments) {
				// create and save the environment in the server
				studyInstanceService.createStudyInstances(numberOfEnvironments).then(function (studyInstances) {
					angular.forEach(studyInstances, function (studyInstance) {
						// update the environment table
						$scope.createInstance(studyInstance);
					});
				});
			};

			$scope.createInstance = function (studyInstance) {
				var instance = {
					instanceId: studyInstance.instanceId,
					managementDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
						$scope.settings.managementDetails),
					trialDetailValues: TrialManagerDataService.constructDataStructureFromDetails($scope.settings.trialConditionDetails),
					experimentPropertyIdMap: {8190: studyInstance.locationInstanceDataId},
					phenotypeIDMap: {}
				};
				instance.managementDetailValues[LOCATION_ID] = UNSPECIFIED_LOCATION_ID;
				instance.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = studyInstance.instanceNumber;
				$scope.instanceInfo.instances.push(instance);
			};

			ctrl.updateInstanceVariables = function (type, entriesIncreased) {

				var settingDetailSource = null;
				var valuesPropertyKey = null;
				var idsPropertyKey = null;

				if (type === 'managementDetails') {
					settingDetailSource = $scope.settings.managementDetails;
					valuesPropertyKey = 'managementDetailValues';
					idsPropertyKey = 'experimentPropertyIdMap';
				} else if (type === 'trialConditionDetails') {
					settingDetailSource = $scope.settings.trialConditionDetails;
					valuesPropertyKey = 'trialDetailValues';
					idsPropertyKey = 'phenotypeIDMap';
				}

				angular.forEach($scope.instanceInfo.instances, function (instance) {
					var valuesList = instance[valuesPropertyKey];
					var idList = instance[idsPropertyKey];

					if (entriesIncreased) {
						angular.forEach(settingDetailSource.keys(), function (settingDetailKey) {
							if (valuesList[settingDetailKey] === undefined) {
								valuesList[settingDetailKey] = null;
							}
						});
					} else {
						angular.forEach(valuesList, function (value, settingDetailKey) {
							if (!settingDetailSource.vals().hasOwnProperty(settingDetailKey)) {
								delete valuesList[settingDetailKey];
								delete idList[settingDetailKey];
							}
						});
					}
				});
			};

			ctrl.initializePossibleValuesMap = function initializePossibleValuesMap() {

				angular.forEach($scope.settings.managementDetails.vals(), function (settingVariable) {
					process(settingVariable);
				});
				angular.forEach($scope.settings.trialConditionDetails.vals(), function (settingVariable) {
					process(settingVariable);
				});

				function process(settingVariable) {
					if (settingVariable.allValues && settingVariable.allValues.length > 0 && !settingVariable.hasOwnProperty('possibleValuesById')) {
						settingVariable.possibleValuesById = {};
						angular.forEach(settingVariable.allValues, function (possibleValue) {
							settingVariable.possibleValuesById[possibleValue.id] = possibleValue;
						});
					}
				}
			};

			// Wrap 'showAlertMessage' global function to a controller function so that
			// we can mock it in unit test.
			ctrl.showAlertMessage = function (title, message) {
				showAlertMessage(title, message);
			};

			ctrl.initializePossibleValuesMap();

			function updateDeletedInstances(index) {
				// remove 1 environment
				$scope.temp.numberOfInstances -= 1;
				$scope.instanceInfo.instances.splice(index, 1);
				if (!$scope.isDesignAlreadyGenerated()) {
					$scope.updateTrialInstanceNo($scope.instanceInfo.instances, index);
				}
				$scope.instanceInfo.numberOfInstances -= 1;

				TrialManagerDataService.deleteInstance(index + 1);
			}

			function addCellClickHandler() {
				var $table = angular.element(tableId);

				addCellClickHandler();

				function addCellClickHandler() {
					$table.off('click').on('click', 'td.instance-editable-cell', cellClickHandler);
				}

				function cellClickHandler() {
					var cell = this;
					var table = $table.DataTable();
					var dtRow = table.row(cell.parentNode);
					var rowIndex = dtRow.index();
					var dtCell = table.cell(cell);
					var cellIndex = table.colReorder.transpose(table.column(cell).index(), 'toOriginal');

					var variableId = $table.find('th:eq(' + cellIndex + ')').data('termid');
					var instance = $scope.instanceInfo.instances[rowIndex];

					createInlineEditor($table, dtCell, cell, instance, variableId);
				}

				function createInlineEditor($table, dtCell, cell, instance, variableId) {

					var isManagementDetailVariable = instance.managementDetailValues.hasOwnProperty(variableId);
					var instanceId = instance.instanceId;
					var variableSettings;
					var valueContainer;
					var instanceDataIdMap;

					if (isManagementDetailVariable) {
						variableSettings = $scope.settings.managementDetails;
						valueContainer = instance.managementDetailValues;
						instanceDataIdMap = instance.experimentPropertyIdMap;
					} else {
						variableSettings = $scope.settings.trialConditionDetails;
						valueContainer = instance.trialDetailValues;
						instanceDataIdMap = instance.phenotypeIDMap;
					}

					var $inlineScope = $scope.$new(true);
					$inlineScope.settings = variableSettings;
					$inlineScope.valueContainer = valueContainer;
					$inlineScope.targetKey = variableId;
					$inlineScope.instance = {
						change: function () {
							updateInline();
						},
						// FIXME altenative to blur bug https://github.com/angular-ui/ui-select/issues/499
						onOpenClose: function (isOpen) {
							if (!isOpen) updateInline();
						}
					};

					$(cell).html('');

					var editor = $compile(
						'<instance-inline-editor ' +
						'instance="instance" ' +
						'settings="settings" ' +
						'targetkey="targetKey"' +
						'settingkey="targetKey"' +
						'valuecontainer="valueContainer"' +
						'</instance-inline-editor>'
					)($inlineScope);

					$(cell).append(editor);

					function updateInline() {
						if (!instanceDataIdMap[variableId]) {
							studyInstanceService.addInstanceData({
								instanceId: instanceId,
								variableId: variableId,
								value: valueContainer[variableId]
							}).then(function (instanceData) {
								instanceDataIdMap[variableId] = instanceData.instanceDataId;
								refreshDisplay();
							});
						} else {
							studyInstanceService.updateInstanceData({
								instanceId: instanceId,
								variableId: variableId,
								instanceDataId: instanceDataIdMap[variableId],
								value: valueContainer[variableId]
							}).then(function (instanceData) {
								// Restore handler
								refreshDisplay();
								addCellClickHandler();
							});
						}
					}

					function refreshDisplay() {
						$inlineScope.$destroy();
						editor.remove();
						dtCell.data($scope.renderDisplayValue(variableSettings.vals()[variableId], valueContainer[variableId]));
						// Restore handler
						$(cell).addClass('instance-editable-cell');
						addCellClickHandler();
					}

					$timeout(function () {
						if (variableId === parseInt(LOCATION_ID)) {
							/** Remove the inline editor for Location when the other part of the page clicked. We can't apply onblur event on location
							 * combobox because the user would need to use the location filter radio and checkbox. **/
							$document.off('click').on('click', () => {
								refreshDisplay();
								$document.off('click');
							});
						} else {
							/**
							 * Initiate interaction with the input so that clicks on other parts of the page
							 * will trigger blur immediately. Also necessary to initiate datepicker
							 * This also avoids temporary click handler on body
							 * FIXME is there a better way?
							 */
							$(cell).find('a.ui-select-match, input:not([type=radio], [type=checkbox])').click().focus();
						}
					}, 100);
				}
			}

		}]).directive('instanceInlineEditor', ['_', function (_) {
		return {
			require: [],
			restrict: 'E',
			scope: {
				instance: '=',
				settings: '=',
				targetkey: '=',
				settingkey: '=',
				valuecontainer: '='
			},
			templateUrl: '/Fieldbook/static/angular-templates/instanceInlineEditor.html',
			link: function ($scope, element, attrs) {
				if ($scope.hasDropdownOptions) {
					$scope.initializeDropdown();
					$scope.updateDropdownValuesFavorites();
				}
				// Stop bubbling of click event so to not interfere with
				// document body click event.
				$(element).click(function (event) {
					event.stopPropagation();
				});
			},
			controller: function ($scope, LOCATION_ID, UNSPECIFIED_LOCATION_ID, BREEDING_METHOD_ID, BREEDING_METHOD_CODE, $http) {

				var LOCATION_LOOKUP_BREEDING_LOCATION = '1';
				var LOCATION_LOOKUP_ALL_LOCATION = '2';

				$scope.variableDefinition = $scope.settings.val($scope.settingkey);
				$scope.widgetType = $scope.variableDefinition.variable.widgetType.$name ?
					$scope.variableDefinition.variable.widgetType.$name : $scope.variableDefinition.variable.widgetType;
				$scope.hasDropdownOptions = $scope.widgetType === 'DROPDOWN';

				$scope.isLocation = parseInt(LOCATION_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);
				$scope.isBreedingMethod = parseInt(BREEDING_METHOD_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10) ||
					parseInt(BREEDING_METHOD_CODE, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);

				$scope.localData = {locationLookup: null, useFavorites: null};

				$scope.$watch('localData.locationLookup', function (newValue, oldValue) {
					if (!angular.equals(newValue, oldValue)) {
						if (angular.equals(newValue, LOCATION_LOOKUP_BREEDING_LOCATION)) {
							$scope.localData.dropdownValues = $scope.localData.useFavorites ? $scope.variableDefinition.possibleValuesFavorite
								: $scope.variableDefinition.possibleValues;
						} else {
							$scope.localData.dropdownValues = $scope.localData.useFavorites ? $scope.variableDefinition.allFavoriteValues
								: $scope.variableDefinition.allValues;
						}
					}
				});

				$scope.$watch('localData.useFavorites', function (newValue, oldValue) {
					if (!angular.equals(newValue, oldValue)) {
						$scope.updateDropdownValuesFavorites();
					}
				});

				$scope.isFavoriteLocation = function (locationId) {
					return $scope.variableDefinition.possibleValuesFavorite.some(function (locationPossibleValue) {
						return parseInt(locationId) === locationPossibleValue.id;
					});
				};

				$scope.isBreedingLocation = function (locationId) {
					return $scope.variableDefinition.possibleValues.some(function (locationPossibleValue) {
						return parseInt(locationId) === locationPossibleValue.id;
					});
				};

				$scope.updateDropdownValuesFavorites = function () {
					if ($scope.localData.useFavorites) {
						if ($scope.localData.locationLookup === LOCATION_LOOKUP_BREEDING_LOCATION) {
							$scope.localData.dropdownValues = $scope.variableDefinition.possibleValuesFavorite;
						} else {
							$scope.localData.dropdownValues = $scope.variableDefinition.allFavoriteValues;
						}
					} else {
						if ($scope.localData.locationLookup === LOCATION_LOOKUP_BREEDING_LOCATION) {
							$scope.localData.dropdownValues = $scope.variableDefinition.possibleValues;
						} else {
							$scope.localData.dropdownValues = $scope.variableDefinition.allValues;
						}
					}
				};

				$scope.initializeDropdown = function () {
					$scope.localData.locationLookup = $scope.isBreedingLocation($scope.valuecontainer[LOCATION_ID]) ?
						LOCATION_LOOKUP_BREEDING_LOCATION : LOCATION_LOOKUP_ALL_LOCATION;
					$scope.localData.useFavorites = $scope.isFavoriteLocation($scope.valuecontainer[LOCATION_ID]);
				};

			}
		};
	}]).directive('instanceDatepicker', function () {
		return {
			require: '^?ngModel',
			scope: {
				instance: '='
			},
			link: function (scope, el, attr, ngModel) {
				$(el).datepicker({
					format: 'yyyymmdd',
					todayHighlight: true,
					todayBtn: true
				}).on('changeDate', function () {
					scope.$apply(function () {
						ngModel.$setViewValue(el.val());
					});
					$(this).datepicker('hide');
				}).on('hide', function () {
					scope.instance.change();
				});
				ngModel.$render = function () {
					var parsedDate;
					try {
						parsedDate = $.datepicker.formatDate("yy-mm-dd", $.datepicker.parseDate('yymmdd', ngModel.$viewValue));
					} catch (e) {
					}
					$(el).datepicker('setDate', parsedDate);
				};

				// Stop bubbling of click event on datepicker selector so to not interfere with
				// document body click event.
				$("#ui-datepicker-div").click(function (event) {
					event.stopPropagation();
				});
			}
		};
	}).factory('DTLoadingTemplate', function () {
		return {
			html: '<span class="throbber throbber-2x"></span>'
		};
	});
})();
