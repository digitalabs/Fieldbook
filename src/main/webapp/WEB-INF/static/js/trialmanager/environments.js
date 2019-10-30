/*global angular, openManageLocations,
environmentModalConfirmationText, environmentConfirmLabel, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	angular.module('manageTrialApp').controller('EnvironmentCtrl', ['$scope', '$q', 'TrialManagerDataService', '$uibModal', '$stateParams',
		'$http', 'DTOptionsBuilder', 'LOCATION_ID', '$timeout', 'environmentService', 'studyStateService', 'derivedVariableService', 'studyInstanceService', 'studyContext',
		function ($scope, $q, TrialManagerDataService, $uibModal, $stateParams, $http, DTOptionsBuilder, LOCATION_ID, $timeout, environmentService,
				  studyStateService, derivedVariableService, studyInstanceService, studyContext) {

			var ctrl = this;

			// at least one environment should be in the datatable, so we are prepopulating the table with the first environment
			var populateDatatableWithDefaultValues = function () {
				$scope.data = TrialManagerDataService.currentData.environments;

				if (!$scope.data.environments) {
					$scope.data.environments = [];
				}
				if ($scope.data.environments.length === 0) {
					$scope.data.environments.push({});
				}
				if (!$scope.data.environments[0].managementDetailValues) {
					$scope.data.environments[0].managementDetailValues = {};
				}
				if (!$scope.data.environments[0].managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX]) {
					$scope.data.environments[0].managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = 1;
				}
			};

			$scope.TRIAL_INSTANCE_NO_INDEX = 8170;

			$scope.data = TrialManagerDataService.currentData.environments;
			$scope.nested = {};
			$scope.nested.dtInstance = {};
			$scope.isHideDelete = false;
			$scope.temp = {
				settingMap: {},
				noOfEnvironments: $scope.data.noOfEnvironments
			};

			$scope.settings = TrialManagerDataService.settings.environments;
			if (Object.keys($scope.settings).length === 0) {
				$scope.settings = {};
				$scope.settings.managementDetails = [];
				$scope.settings.trialConditionDetails = [];
			}

			$scope.ifLocationAddedToTheDataTable = function () {
				if (Object.keys($scope.settings.managementDetails).length !== 0) {
					return $scope.settings.managementDetails.keys().indexOf(parseInt(LOCATION_ID)) > -1;
				} else {
					return false;
				}

			};

			$scope.onRemoveVariable = function (variableType, variableIds) {
				return $scope.checkVariableIsUsedInCalculatedVariable(variableIds);
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
					} else {
						deferred.resolve(true);
					}
				});
				return deferred.promise;
			};

			//the flag to determine if we have a location variable in the datatable
			$scope.isLocation = $scope.ifLocationAddedToTheDataTable();

			$scope.onLocationChange = function (data) {
				environmentService.changeEnvironments(data);
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

			$scope.buttonsTop = [{
				extend: 'colvis',
				className: 'fbk-buttons-no-border fbk-colvis-button',
				text: '<i class="glyphicon glyphicon-th dropdown-toggle fbk-show-hide-grid-column"></i>',
				columns: ':gt(0):not(.ng-hide)'
			}];

			$scope.dtOptions = DTOptionsBuilder.newOptions().withDOM('<"fbk-datatable-panel-top"liB>rtp')
				.withButtons($scope.isLocation ? $scope.buttonsTopWithLocation.slice() : $scope.buttonsTop.slice())
				.withOption('scrollX', true)
				.withOption('scrollCollapse', true)
				.withOption('deferRender', true);

			$scope.dtOptions.drawCallback = function () {
				var api = $(this).DataTable();

				//temporary fix in buttons disappear bug,
				//see https://github.com/l-lin/angular-datatables/issues/502#issuecomment-161166246
				if (api) {
					// remove old set of buttons before recreating them
					if (api.buttons()) {
						api.buttons().remove();
					}
					new $.fn.dataTable.Buttons(api, {
						buttons: $scope.isLocation ? $scope.buttonsTopWithLocation.slice() : $scope.buttonsTop.slice()
					});

					$(this).parents('.dataTables_wrapper').find('.dt-buttons').replaceWith(api.buttons().container());
				}
			};

			$scope.onAddVariable = function () {
				$scope.nested.dtInstance.rerender();
				// update the location flag, as it could have been added
				$scope.isLocation = $scope.ifLocationAddedToTheDataTable();
			};

			$scope.$on('deleteOccurred', function () {
				$scope.nested.dtInstance.rerender();
				// update the location flag, as it could have been deleted
				$scope.isLocation = $scope.ifLocationAddedToTheDataTable();
			});

			$scope.$on('rerenderEnvironmentTable', function (event, args) {
				$scope.nested.dtInstance.rerender();
			});

			$scope.initiateManageLocationModal = function () {
				//TODO $scope.variableDefinition.locationUpdated = false;
				openManageLocations();
			};

			//prepopulate the datatable
			populateDatatableWithDefaultValues();

			TrialManagerDataService.onUpdateData('environments', function () {
				$scope.temp.noOfEnvironments = $scope.data.noOfEnvironments;
			});

			/* Scope Functions */
			$scope.shouldDisableEnvironmentCountUpdate = function () {
				return studyStateService.hasGeneratedDesign() || studyStateService.hasListOrSubObs();
			};

			$scope.updateEnvironmentCount = function () {
				if ($scope.temp.noOfEnvironments > $scope.data.environments.length) {
					$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
				} else if ($scope.temp.noOfEnvironments < $scope.data.environments.length) {
					$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
				}
			};

			$scope.deleteEnvironment = function (index) {
				updateDeletedEnvironment(index);
			};

			$scope.updateTrialInstanceNo = function (environments, index) {
				for (var i = 0; i < environments.length; i++) {
					var environment = environments[i];
					var trialInstanceNo = environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX];
					if (trialInstanceNo > index) {
						trialInstanceNo -= 1;
						environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = trialInstanceNo;
					}
				}
			};

			$scope.addVariable = true;
			$scope.findSetting = function (targetKey, type) {
				if (!$scope.temp.settingMap[targetKey]) {
					var targetSettingList = null;

					if (type === 'managementDetails') {
						targetSettingList = $scope.settings.managementDetails;
					} else if (type === 'trialConditionDetails') {
						targetSettingList = $scope.settings.trialConditionDetails;
					}

					$.each(targetSettingList, function (key, value) {
						if (value.variable.cvTermId === targetKey) {
							$scope.temp.settingMap[targetKey] = value;
							return false;
						}
					});
				}

				return $scope.temp.settingMap[targetKey];
			};

			/* Watchers */
			$scope.$watch('data.noOfEnvironments', function (newVal, oldVal) {
				$scope.temp.noOfEnvironments = newVal;
				if (Number(newVal) < Number(oldVal)) {
					// if new environment count is less than previous value, splice array
					while ($scope.data.environments.length > newVal) {
						$scope.data.environments.pop();
					}

					TrialManagerDataService.applicationData.hasNewEnvironmentAdded = false;
				} else if (Number(newVal) > Number(oldVal)) {
					$scope.addEnvironments(newVal);
					TrialManagerDataService.applicationData.hasNewEnvironmentAdded = true;
				}

				if (newVal !== oldVal) {
					studyStateService.updateOccurred();
				}
			});

			$scope.$watch('settings.managementDetails', function (newVal, oldVal) {
				ctrl.updateEnvironmentVariables('managementDetails', newVal.length > oldVal.length);
			}, true);

			$scope.$watch('settings.trialConditionDetails', function (newVal, oldVal) {
				ctrl.updateEnvironmentVariables('trialConditionDetails', newVal.length > oldVal.length);
			}, true);

			// Wrap 'showAlertMessage' global function to a controller function so that
			// we can mock it in unit test.
			ctrl.showAlertMessage = function (title, message) {
				showAlertMessage(title, message);
			};

			$scope.addEnvironment = function () {
				var startingInstanceNumber = $scope.data.environments.length + 1;
				studyInstanceService.createStudyInstance(startingInstanceNumber).then(function () {
					$scope.createEnvironment(startingInstanceNumber);
				});
			};

			$scope.createEnvironment = function (instanceNumber) {
				var environment = {
					managementDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
						$scope.settings.managementDetails),
					trialDetailValues: TrialManagerDataService.constructDataStructureFromDetails($scope.settings.trialConditionDetails)
				};
				environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = instanceNumber;
				$scope.data.environments.push(environment);
			};

			$scope.addEnvironments = function (numberOfEnvironments) {
				var startingInstanceNumber = $scope.data.environments.length + 1;
				for (var instanceNumber = startingInstanceNumber; instanceNumber <= numberOfEnvironments; instanceNumber++) {
					$scope.createEnvironment(instanceNumber);
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

			function updateDeletedEnvironment(index) {
				// remove 1 environment
				$scope.temp.noOfEnvironments -= 1;
				$scope.data.environments.splice(index, 1);
				$scope.updateTrialInstanceNo($scope.data.environments, index);
				$scope.data.noOfEnvironments -= 1;

				//update the no of environments in experimental design tab
				if (TrialManagerDataService.currentData.experimentalDesign.noOfEnvironments !== undefined) {
					TrialManagerDataService.currentData.experimentalDesign.noOfEnvironments = $scope.temp.noOfEnvironments;
				}

				TrialManagerDataService.deleteEnvironment(index + 1);
			}

			// init
			if ($stateParams && $stateParams.addtlNumOfEnvironments && !isNaN(parseInt($stateParams.addtlNumOfEnvironments))) {
				var addtlNumOfEnvironments = parseInt($stateParams.addtlNumOfEnvironments, 10);
				$scope.temp.noOfEnvironments = parseInt($scope.temp.noOfEnvironments, 10) + addtlNumOfEnvironments;
				$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
				$scope.addEnvironments(addtlNumOfEnvironments);
			}
		}]).factory('DTLoadingTemplate', function () {
		return {
			html: '<span class="throbber throbber-2x"></span>'
		};
	});
})();
