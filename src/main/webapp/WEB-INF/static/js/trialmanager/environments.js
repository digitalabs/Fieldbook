/*global angular, openManageLocations,
environmentModalConfirmationText, environmentConfirmLabel, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	angular.module('manageTrialApp').controller('EnvironmentCtrl', ['$scope', '$q', 'TrialManagerDataService', '$uibModal', '$stateParams',
		'$http', 'DTOptionsBuilder', 'LOCATION_ID', 'UNSPECIFIED_LOCATION_ID', '$timeout', 'studyInstanceService', 'studyStateService', 'derivedVariableService', 'studyContext',
		'datasetService', '$compile',
		function ($scope, $q, TrialManagerDataService, $uibModal, $stateParams, $http, DTOptionsBuilder, LOCATION_ID, UNSPECIFIED_LOCATION_ID, $timeout, studyInstanceService,
				  studyStateService, derivedVariableService, studyContext, datasetService, $compile) {

			var ctrl = this;
			var tableId = '#environment-table';

			$scope.TRIAL_INSTANCE_NO_INDEX = 8170;
			$scope.addVariable = true;
			$scope.data = TrialManagerDataService.currentData.environments;
			$scope.nested = {};
			$scope.nested.dataTable = {};
			$scope.isDisableAddEnvironment = false;
			$scope.isHideDelete = false;
			$scope.temp = {
				settingMap: {},
				noOfEnvironments: $scope.data.noOfEnvironments
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

					if (settingVariable.variable.dataTypeId === 1130 || settingVariable.variable.dataTypeId === 1132) {
						return renderCategoricalValue(settingVariable, value);
					} else {
						return EscapeHTML.escape(value);
					}
				}

				function renderCategoricalValue(settingVariable, value) {
					var displayValue = '';
					if (settingVariable.possibleValuesById && settingVariable.possibleValuesById[value]) {
						displayValue = settingVariable.possibleValuesById[value].name;
					}
					return displayValue;
				}

			}

			$scope.initiateManageLocationModal = function () {
				//TODO $scope.variableDefinition.locationUpdated = false;
				openManageLocations();
			};

			TrialManagerDataService.onUpdateData('environments', function () {
				$scope.temp.noOfEnvironments = $scope.data.noOfEnvironments;
			});

			/* Scope Functions */
			$scope.isDesignAlreadyGenerated = function () {
				return studyStateService.hasGeneratedDesign() || studyStateService.hasListOrSubObs();
			};

			$scope.updateEnvironmentCount = function () {
				if ($scope.temp.noOfEnvironments > $scope.data.environments.length) {
					$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
					$scope.addEnvironments($scope.temp.noOfEnvironments - $scope.data.environments.length);
				} else if ($scope.temp.noOfEnvironments < $scope.data.environments.length) {
					$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
					var instanceIds = [];
					// if new environment count is less than previous value, splice array
					while ($scope.data.environments.length > $scope.temp.noOfEnvironments) {
						var environment = $scope.data.environments.pop();
						instanceIds.push(environment.experimentId);
					}
					studyInstanceService.deleteStudyInstances(instanceIds);
				}
			};

			$scope.deleteEnvironment = function (index, instanceId) {

				studyInstanceService.getStudyInstance(instanceId).then(function (studyInstance) {

					// Show error if environment cannot be deleted
					if (!studyInstance.canBeDeleted) {
						showErrorMessage('', $.environmentMessages.environmentCannotBeDeleted);
						return;

						// Show confirmation message for overwriting measurements and/or fieldmap
					} else if (studyInstance.hasMeasurements || studyInstance.hasFieldmap) {
						var modalConfirmDelete = $scope.openConfirmModal($.environmentMessages.environmentHasDataThatWillBeLost, 'Yes', 'No');
						modalConfirmDelete.result.then(function (shouldContinue) {
							if (shouldContinue) {
								$scope.continueEnvironmentDeletion(index, [instanceId]);
							}
						});
					} else {
						$scope.continueEnvironmentDeletion(index, [instanceId]);
					}
				}, function (errResponse) {
					showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
				});

			};

			// Proceed deleting existing environment
			$scope.continueEnvironmentDeletion = function (index, instanceIds) {
				studyInstanceService.deleteStudyInstances(instanceIds);
				updateDeletedEnvironment(index);
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
			$scope.$watch('data.noOfEnvironments', function (newVal, oldVal) {
				$scope.temp.noOfEnvironments = newVal;
				if (Number(newVal) < Number(oldVal)) {
					TrialManagerDataService.applicationData.hasNewEnvironmentAdded = false;
				} else if (Number(newVal) > Number(oldVal)) {
					TrialManagerDataService.applicationData.hasNewEnvironmentAdded = true;
					addCellClickHandler();
				}
			});

			$scope.$watch('settings.managementDetails', function (newVal, oldVal) {
				ctrl.updateEnvironmentVariables('managementDetails', newVal.keys().length > oldVal.keys().length);
			}, true);

			$scope.$watch('settings.trialConditionDetails', function (newVal, oldVal) {
				ctrl.updateEnvironmentVariables('trialConditionDetails', newVal.keys().length > oldVal.keys().length);
			}, true);

			$scope.addEnvironment = function () {

				$scope.isDisableAddEnvironment = true;

				// create and save the environment in the server
				studyInstanceService.createStudyInstances(1).then(function (studyInstances) {
					angular.forEach(studyInstances, function (studyInstance) {
						// update the environment table
						$scope.createEnvironment(studyInstance.instanceNumber, studyInstance.experimentId);
						$scope.data.noOfEnvironments++;
					});
					$scope.isDisableAddEnvironment = false;
				});

			};

			$scope.addEnvironments = function (numberOfEnvironments) {
				// create and save the environment in the server
				studyInstanceService.createStudyInstances(numberOfEnvironments).then(function (studyInstances) {
					angular.forEach(studyInstances, function (studyInstance) {
						// update the environment table
						$scope.createEnvironment(studyInstance.instanceNumber, studyInstance.experimentId);
					});
				});
			};

			$scope.createEnvironment = function (instanceNumber, experimentId, index) {
				var environment = {
					experimentId: experimentId,
					managementDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
						$scope.settings.managementDetails),
					trialDetailValues: TrialManagerDataService.constructDataStructureFromDetails($scope.settings.trialConditionDetails)
				};

				environment.managementDetailValues[LOCATION_ID] = UNSPECIFIED_LOCATION_ID;
				environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = instanceNumber;
				if (index != undefined) {
					$scope.data.environments.splice(index, 0, environment);
				} else {
					$scope.data.environments.push(environment);
				}
			};

			ctrl.updateEnvironmentVariables = function (type, entriesIncreased) {

				var settingDetailSource = null;
				var targetKey = null;

				if (type === 'managementDetails') {
					settingDetailSource = $scope.settings.managementDetails;
					targetKey = 'managementDetailValues';
				} else if (type === 'trialConditionDetails') {
					settingDetailSource = $scope.settings.trialConditionDetails;
					targetKey = 'trialDetailValues';
				}

				$.each($scope.data.environments, function (key, value) {
					var subList = value[targetKey];

					if (entriesIncreased) {
						$.each(settingDetailSource.keys(), function (key, value) {
							if (subList[value] === undefined) {
								subList[value] = null;
							}
						});
					} else {
						$.each(subList, function (idKey) {
							if (!settingDetailSource.vals().hasOwnProperty(idKey)) {
								delete subList[idKey];
							}
						});
					}
				});
			};

			ctrl.initializePossibleValuesMap = function initializePossibleValuesMap () {

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

			function updateDeletedEnvironment(index) {
				// remove 1 environment
				$scope.temp.noOfEnvironments -= 1;
				$scope.data.environments.splice(index, 1);
				if (!$scope.isDesignAlreadyGenerated()) {
					$scope.updateTrialInstanceNo($scope.data.environments, index);
				}
				$scope.data.noOfEnvironments -= 1;

				TrialManagerDataService.deleteEnvironment(index + 1);
			}

			function addCellClickHandler() {
				var $table = angular.element(tableId);

				addClickHandler();

				function addClickHandler() {
					$table.off('click').on('click', 'td.environmentVariable', clickHandler);
				}

				function clickHandler() {
					var cell = this;
					var table = $table.DataTable();
					var dtRow = table.row(cell.parentNode);
					var rowIndex = dtRow.index();
					var dtCell = table.cell(cell);
					var cellIndex = table.colReorder.transpose(table.column(cell).index(), 'toOriginal');

					var termId = $table.find('th:eq(' + cellIndex + ')').data('termid');
					var environment = $scope.data.environments[rowIndex];
					var isManagementDetailVariable = environment.managementDetailValues.hasOwnProperty(termId);

					if (isManagementDetailVariable) {
						createInlineEditor(dtCell, cell, $scope.settings.managementDetails, environment.managementDetailValues, termId);
					} else {
						createInlineEditor(dtCell, cell, $scope.settings.trialConditionDetails, environment.trialDetailValues, termId);
					}
					/**
					 * Remove handler to not interfere with inline editor
					 * will be restored after fnUpdate
					 */
					$table.off('click');

				}

				function createInlineEditor(dtCell, cell, settings, valueContainer, targetKey) {

					$scope.$apply(function () {

						var $inlineScope = $scope.$new(true);
						$inlineScope.settings = settings;
						$inlineScope.valueContainer = valueContainer;
						$inlineScope.targetKey = targetKey;
						$inlineScope.environment = {
							change: function () {
								updateInline();
							},
							// FIXME altenative to blur bug https://github.com/angular-ui/ui-select/issues/499
							onOpenClose: function (isOpen) {
								if (!isOpen) updateInline();
							},
							newInlineValue: function (newValue) {
								return {name: newValue};
							}
						};

						$(cell).html('');

						var editor = $compile(
							'<environment-inline-editor ' +
							'environment="environment" ' +
							'settings="settings" ' +
							'targetkey="targetKey"' +
							'settingkey="targetKey"' +
							'valuecontainer="valueContainer"' +
							'</environment-inline-editor>'
						)($inlineScope);

						$(cell).append(editor);

						function updateInline() {
							$inlineScope.$destroy();
							editor.remove();
							dtCell.data($scope.renderDisplayValue(settings.vals()[targetKey], valueContainer[targetKey]));
							// Restore handler
							addClickHandler();
						}

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

		}]).directive('environmentInlineEditor', ['_', function (_) {
		return {
			require: '?ngModel',
			restrict: 'E',
			scope: {
				environment: '=',
				settings: '=',
				targetkey: '=',
				settingkey: '=',
				valuecontainer: '=',
				changefunction: '&'
			},

			templateUrl: '/Fieldbook/static/angular-templates/environmentInlineEditor.html',
			controller: function ($scope, LOCATION_ID, UNSPECIFIED_LOCATION_ID, BREEDING_METHOD_ID, BREEDING_METHOD_CODE, $http) {

				var LOCATION_LOOKUP_BREEDING_LOCATION = 1;
				var LOCATION_LOOKUP_ALL_LOCATION = 2;

				if ($scope.settingkey === undefined) {
					$scope.settingkey = $scope.targetkey;
				}

				if (!$scope.changefunction) {
					$scope.changefunction = function () {
					};
				}

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

				$scope.variableDefinition = $scope.settings.val($scope.settingkey);
				$scope.widgetType = $scope.variableDefinition.variable.widgetType.$name ?
					$scope.variableDefinition.variable.widgetType.$name : $scope.variableDefinition.variable.widgetType;
				$scope.hasDropdownOptions = $scope.widgetType === 'DROPDOWN';

				$scope.isLocation = parseInt(LOCATION_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);
				$scope.isBreedingMethod = parseInt(BREEDING_METHOD_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10) ||
					parseInt(BREEDING_METHOD_CODE, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);

				$scope.localData = {};

				$scope.updateDropdownValuesFavorites = function () { // Change state for favorite checkbox
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

				$scope.updateDropdownValuesBreedingLocation = function () { // Change state for breeding
					// location radio
					$scope.localData.dropdownValues = $scope.localData.useFavorites ? $scope.variableDefinition.possibleValuesFavorite
						: $scope.variableDefinition.possibleValues;
					$scope.localData.locationLookup = LOCATION_LOOKUP_BREEDING_LOCATION;
				};

				$scope.updateDropdownValuesAllLocation = function () { // Change state for all locations radio
					$scope.localData.dropdownValues = $scope.localData.useFavorites ? $scope.variableDefinition.allFavoriteValues
						: $scope.variableDefinition.allValues;
					$scope.localData.locationLookup = LOCATION_LOOKUP_ALL_LOCATION;
				};

				if ($scope.hasDropdownOptions) {

					var currentVal = $scope.valuecontainer[$scope.targetkey];

					// lets fix current val if its an object so that valuecontainer only contains the id
					if (currentVal && currentVal.id) {
						currentVal = currentVal.id;
						$scope.valuecontainer[$scope.targetkey] = currentVal;
					}

					// If the currentVal is undefined then we assume that the environment is newly created and not yet saved,
					// so in this case, we need to set the set the location to the default value, which is UNSPECIFIED_LOCATION_ID (NOLOC)
					if (!currentVal && $scope.targetkey === LOCATION_ID) {
						$scope.valuecontainer[$scope.targetkey] = UNSPECIFIED_LOCATION_ID;
						$scope.localData.locationLookup = $scope.isBreedingLocation(UNSPECIFIED_LOCATION_ID) ?
							LOCATION_LOOKUP_BREEDING_LOCATION : LOCATION_LOOKUP_ALL_LOCATION;
						$scope.localData.useFavorites = $scope.isFavoriteLocation(UNSPECIFIED_LOCATION_ID);
					} else {
						$scope.localData.locationLookup = $scope.isBreedingLocation($scope.valuecontainer[LOCATION_ID]) ?
							LOCATION_LOOKUP_BREEDING_LOCATION : LOCATION_LOOKUP_ALL_LOCATION;
						$scope.localData.useFavorites = $scope.isFavoriteLocation($scope.valuecontainer[LOCATION_ID]);
					}

					$scope.updateDropdownValuesFavorites();

				}


				// TODO: add code that can handle display of favorite methods, as well as update of possible values in case of click
				// of manage methods
				if ($scope.isLocation) {
					$scope.clearArray = function (targetArray) {
						// current internet research suggests that this is the fastest way of clearing an array
						while (targetArray.length > 0) {
							targetArray.pop();
						}
					};

					$scope.updateLocationValues = function () {
						$http.get('/Fieldbook/locations/getLocations').then(function (returnVal) {
							if (returnVal.data.success === '1') {
								$scope.variableDefinition.locationUpdated = true;
								// clear and copy of array is performed so as to preserve previous reference
								// and have changes applied to all components with a copy of the previous reference
								$scope.clearArray($scope.variableDefinition.possibleValues);
								$scope.clearArray($scope.variableDefinition.possibleValuesFavorite);
								$scope.clearArray($scope.variableDefinition.allFavoriteValues);
								$scope.clearArray($scope.variableDefinition.allValues);

								$scope.variableDefinition.possibleValues.push.apply($scope.variableDefinition.possibleValues,
									$scope.convertLocationsToPossibleValues(returnVal.data.allBreedingLocations));
								$scope.variableDefinition.possibleValuesFavorite.push.apply(
									$scope.variableDefinition.possibleValuesFavorite,
									$scope.convertLocationsToPossibleValues(returnVal.data.allBreedingFavoritesLocations));
								$scope.variableDefinition.allFavoriteValues.push.apply(
									$scope.variableDefinition.allFavoriteValues,
									$scope.convertLocationsToPossibleValues(returnVal.data.favoriteLocations));
								$scope.variableDefinition.allValues.push.apply(
									$scope.variableDefinition.allValues,
									$scope.convertLocationsToPossibleValues(returnVal.data.allLocations));
								$scope.updateDropdownValuesFavorites();
							}
						});
					};

					$scope.convertLocationsToPossibleValues = function (locations) {
						var possibleValues = [];

						$.each(locations, function (key, value) {
							var locNameDisplay = value.lname;
							if (value.labbr != null && value.labbr !== '') {
								locNameDisplay += ' - (' + value.labbr + ')';
							}

							possibleValues.push({
								id: value.locid,
								name: locNameDisplay,
								description: value.lname
							});
						});

						return possibleValues;
					};

					$(document).off('location-update');
					$(document).on('location-update', $scope.updateLocationValues);
				}
			}
		};
	}]).factory('DTLoadingTemplate', function () {
		return {
			html: '<span class="throbber throbber-2x"></span>'
		};
	});
})();
