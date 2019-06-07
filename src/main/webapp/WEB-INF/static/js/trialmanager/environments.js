/*global angular, openManageLocations,
environmentModalConfirmationText, environmentConfirmLabel, showAlertMessage, showErrorMessage*/
// TODO: MARK CODE TO BE DELETE IBP-2789
(function () {
	'use strict';

	angular.module('manageTrialApp').controller('EnvironmentCtrl', ['$scope', 'TrialManagerDataService', '$uibModal', '$stateParams',
		'$http', 'DTOptionsBuilder', 'LOCATION_ID', '$timeout', 'environmentService', 'studyStateService', '$rootScope', 'studyContext', 'datasetService',
		function ($scope, TrialManagerDataService, $uibModal, $stateParams, $http, DTOptionsBuilder, LOCATION_ID, $timeout, environmentService,
				  studyStateService, $rootScope, studyContext, datasetService) {

			var ctrl = this;
			// preload the measurements tab, if the measurements tab is not yet loaded 
			// to make sure deleting environments will still works
			// since environments are directly correlated to their measurement rows
			// NOTE: $rootScope.stateSuccessfullyLoaded will only have value once the specific tab is successfully loaded
/*			if ($rootScope.stateSuccessfullyLoaded['createMeasurements'] === undefined
				&& $rootScope.stateSuccessfullyLoaded['editMeasurements'] === undefined) {
				$scope.loadMeasurementsTabInBackground();
			}*/

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
			$scope.shouldDisableEnvironmentCountUpdate = function() {
				return studyStateService.hasGeneratedDesign() || studyStateService.hasListOrSubObs();
			};

			$scope.updateEnvironmentCount = function () {
				if ($scope.temp.noOfEnvironments > $scope.data.environments.length) {
					$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
				} else if ($scope.temp.noOfEnvironments < $scope.data.environments.length) {
					//var modalInstance = $rootScope.openConfirmModal(environmentModalConfirmationText, environmentConfirmLabel);
					//modalInstance.result.then(function (shouldContinue) {
						//if (shouldContinue) {
							$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
						//}
					//});
				}
			};

			$scope.deleteEnvironment = function (index) {
				//if (!TrialManagerDataService.isOpenStudy()) {
					// For New Trial
					ctrl.confirmDeleteEnvironment(index);

				/*} else {
					// For Existing Trial
					ctrl.hasMeasurementDataOnEnvironment(index);
				}*/
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

					// Regenerate experimental design and measurement table when the study is not saved yet
					/*if (TrialManagerDataService.isOpenStudy() && TrialManagerDataService.currentData.experimentalDesign.noOfEnvironments !== undefined) {
						refreshMeasurementTableAfterDeletingEnvironment();
					}*/

					TrialManagerDataService.applicationData.hasNewEnvironmentAdded = false;
				} else if (Number(newVal) > Number(oldVal)) {
					addNewEnvironments(newVal - oldVal);
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

			/* Controller Utility functions */
			ctrl.confirmDeleteEnvironment = function (index) {
				// Existing Trial with measurement data
				/*var modalInstance = $rootScope.openConfirmModal(environmentModalConfirmationText, environmentConfirmLabel);
				modalInstance.result.then(function (shouldContinue) {
					if (shouldContinue) {*/
						updateDeletedEnvironment(index);
					/*}
				});*/
			}

			/*ctrl.hasMeasurementDataOnEnvironment = function (environmentNo) {
				var deferred = $.Deferred();

				$http.get('/Fieldbook/trial/measurements/instanceMetadata/' + studyContext.studyId, {cache: false}).success(function (environmentList) {
					if (environmentList[environmentNo] === undefined) {
						ctrl.hasAdvancedOrCrossesListOnStudy(environmentNo);
						deferred.resolve();
					}
					else {
						// FIXME - This validation should be removed once we fix the logic to delete environments without regenerating experiment units
						if ($scope.subObservationTabs.length > 1) {
							var warningMessage = 'Environments cannot be removed because the study has sub-observation units created.';
							ctrl.showAlertMessage('', warningMessage);
						} else {
							var instanceId = environmentList[environmentNo].instanceDbId;
							var datasetId = studyContext.measurementDatasetId;

							datasetService.getDataset(datasetId).then(function (dataset) {
								if (!dataset.instances.length) {
									updateDeletedEnvironment(environmentNo);
									deferred.resolve();
									return deferred.promise();
								}
								datasetService.observationCountByInstance(datasetId, instanceId).then(function (response) {
									var count = response.headers('X-Total-Count');
									if (count > 0) {
										var warningMessage = 'This environment cannot be removed because it contains measurement data.';
										ctrl.showAlertMessage('', warningMessage);
									} else {
										ctrl.hasAdvancedOrCrossesListOnStudy(environmentNo);
									}
									deferred.resolve();
								});
							});
						}
					}

				});

				return deferred.promise();
			}*/

			/*ctrl.hasAdvancedOrCrossesListOnStudy = function (environmentNo) {
				var deferred = $.Deferred();
				if(TrialManagerDataService.trialMeasurement.hasAdvancedOrCrossesList) {
					var warningMessage = 'This environment cannot be removed because the study has Advance/Cross List.';
					ctrl.showAlertMessage('', warningMessage);
				} else {
					ctrl.confirmDeleteEnvironment(environmentNo);
				}
				deferred.resolve();
				return deferred.promise();
			}*/

			// Wrap 'showAlertMessage' global function to a controller function so that
			// we can mock it in unit test.
			ctrl.showAlertMessage = function (title, message) {
				showAlertMessage(title, message);
			}

			// on click generate design button
			/*function refreshMeasurementTableAfterDeletingEnvironment() {
				// TODO MARK FOR DELETE IBP-2789
				/!*$rootScope.$broadcast('/!*previewMeasurements');
				$('body').addClass('preview-measurements-only');*!/
				// Make sure that the measurement table will only refresh if there is a selected design type for the current study
				var designTypeId = TrialManagerDataService.currentData.experimentalDesign.designType;
				var designTypes = TrialManagerDataService.applicationData.designTypes;
				var noOfEnvironments = TrialManagerDataService.currentData.environments.noOfEnvironments;
				var data = TrialManagerDataService.currentData.experimentalDesign;
				//update the no of environments in experimental design tab
				data.noOfEnvironments = noOfEnvironments;
				data.environments = $scope.data.environments;
				TrialManagerDataService.generateExpDesign(data).then(
					function (response) {
						if (response.valid === true) {
							TrialManagerDataService.clearUnappliedChangesFlag();
							TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
							$('#chooseGermplasmAndChecks').data('replace', '1');
						} else {
							showErrorMessage('', response.message);
							$body.removeClass('preview-measurements-only');
						}
					}, function (errResponse) {
						showErrorMessage($.fieldbookMessages.errorServerError, $.fieldbookMessages.errorDesignGenerationFailed);
						$body.removeClass('preview-measurements-only');
					}
				);
			}*/

			function addNewEnvironments(noOfEnvironments) {
				for (var ctr = 0; ctr < noOfEnvironments; ctr++) {
					$scope.data.environments.push({
						managementDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
							$scope.settings.managementDetails),
						trialDetailValues: TrialManagerDataService.constructDataStructureFromDetails($scope.settings.trialConditionDetails)
					});
				}
				// we need to assign the TrialInstanceNumber and set it equal to index when new environments were added to the list
				for (var i = 0; i < $scope.data.environments.length; i++) {
					var environment = $scope.data.environments[i];
					if (!environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX]) {
						environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = i + 1;
					}
				}
			}

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
			}

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
				addNewEnvironments(addtlNumOfEnvironments);
			}
		}]).factory('DTLoadingTemplate', function () {
		return {
			html: '<span class="throbber throbber-2x"></span>'
		};
	});
})();
