/*global angular, openStudyTree, showErrorMessage, operationMode, resetGermplasmList,
showAlertMessage,showMeasurementsPreview,createErrorNotification,errorMsgHeader,
stockListImportNotSaved, ImportDesign, isOpenStudy, displayAdvanceList, InventoryPage, ImportCrosses*/
//TODO move this messages under a namespace
/* global addEnvironmentsImportDesignMessage, importSaveDataWarningMessage*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp', ['designImportApp', 'leafnode-utils', 'fieldbook-utils',
		'ui.router', 'ui.bootstrap', 'ngLodash', 'ngResource', 'ngStorage', 'datatables', 'datatables.buttons',
		'showSettingFormElementNew', 'ngSanitize', 'ui.select', 'ngMessages', 'blockUI', 'datasets-api', 'bmsAuth','studyState', 'export-study', 'import-study']);

	manageTrialApp.config(['$httpProvider', function($httpProvider) {
		$httpProvider.interceptors.push('authInterceptor');
		$httpProvider.interceptors.push('authExpiredInterceptor');
	}]);

	manageTrialApp.config(['localStorageServiceProvider', function(localStorageServiceProvider){
		localStorageServiceProvider.setPrefix('bms');
	}]);

	manageTrialApp.config(['blockUIConfig', function(blockUIConfig) {
		blockUIConfig.templateUrl = '/Fieldbook/static/angular-templates/blockUiTemplate.html';
	}]);

	/*** Added to prevent Unsecured HTML error
	 It is used by ng-bind-html ***/
	manageTrialApp.config(function ($sceProvider) {
		$sceProvider.enabled(false);
	});

	// routing configuration
	// TODO: if possible, retrieve the template urls from the list of constants
	manageTrialApp.config(function ($uiRouterProvider, $stateProvider, $urlRouterProvider) {

		var StickyStates = window['@uirouter/sticky-states'];
		var DSRPlugin = window['@uirouter/dsr'].DSRPlugin;
		$uiRouterProvider.plugin(StickyStates.StickyStatesPlugin);
		$uiRouterProvider.plugin(DSRPlugin);

		$urlRouterProvider.otherwise('/trialSettings');
		$stateProvider

			.state('trialSettings', {
				url: '/trialSettings',
				templateUrl: '/Fieldbook/TrialManager/createTrial/trialSettings',
				controller: 'TrialSettingsCtrl'
			})

			.state('treatment', {
				url: '/treatment',
				templateUrl: '/Fieldbook/TrialManager/createTrial/treatment',
				controller: 'TreatmentCtrl'
			})

			.state('environment', {
				url: '/environment?addtlNumOfEnvironments&displayWarningMessage&timestamp',
				views: {
					environment: {
						controller: 'EnvironmentCtrl',
						templateUrl: '/Fieldbook/TrialManager/createTrial/environment'
					}
				},
				deepStateRedirect: true, sticky: true
			})

			.state('experimentalDesign', {
				url: '/experimentalDesign',
				templateUrl: '/Fieldbook/TrialManager/createTrial/experimentalDesign',
				controller: 'ExperimentalDesignCtrl'
			})

			.state('germplasm', {
				url: '/germplasm',
				views: {
					germplasm: {
						controller: 'GermplasmCtrl',
						templateUrl: '/Fieldbook/TrialManager/createTrial/germplasm'
					}
				},
				deepStateRedirect: true, sticky: true
			})

			.state('createMeasurements', {
				url: '/createMeasurements',
				views: {
					createMeasurements: {
						controller: 'MeasurementsCtrl',
						templateUrl: '/Fieldbook/TrialManager/createTrial/measurements'
					}
				},
				deepStateRedirect: true, sticky: true
			})

			.state('editMeasurements', {
				url: '/editMeasurements',
				views: {
					editMeasurements: {
						controller: 'MeasurementsCtrl',
						templateUrl: '/Fieldbook/TrialManager/openTrial/measurements'
					}
				},
				deepStateRedirect: true, sticky: true
			})

			.state('subObservationTabs', {
				url: '/subObservationTabs/:subObservationTabId',
				views: {
					subObservationTab: {
						controller: 'SubObservationTabCtrl',
						templateUrl: '/Fieldbook/TrialManager/openTrial/subObservationTab'
					}
				},
				params: {
					subObservationTab: null
				},
				redirectTo: function (trans) {
					var tab = trans.params().subObservationTab;
					if (tab && tab.subObservationSets.length) {
						var subObservationSet = tab.subObservationSets[0];
						return {
							state: 'subObservationTabs.subObservationSets',
							params: {
								subObservationTabId: tab.id,
								subObservationTab: tab,
								subObservationSetId: subObservationSet.id,
								subObservationSet: subObservationSet
							}
						}
					}
				}
				// , deepStateRedirect: { params: true } // TODO
			})
			.state('subObservationTabs.subObservationSets', {
				url: '/subObservationSets/:subObservationSetId',
				controller: 'SubObservationSetCtrl',
				templateUrl: '/Fieldbook/TrialManager/openTrial/subObservationSet',
				params: {
					subObservationSet: null
				},
			})
		;

	});

	// common filters
	manageTrialApp.filter('range', function () {
		return function (input, total) {
			total = parseInt(total);
			for (var i = 0; i < total; i++) {
				input.push(i);
			}

			return input;
		};
	});

	manageTrialApp.run(
		['$rootScope', '$state', '$stateParams', 'uiSelect2Config', 'VARIABLE_TYPES', '$transitions',
			function ($rootScope, $state, $stateParams, uiSelect2Config, VARIABLE_TYPES, $transitions) {
				$rootScope.VARIABLE_TYPES = VARIABLE_TYPES;

				$transitions.onEnter({},
					function (transition) {
						if ($('.import-study-data').data('data-import') === '1' || stockListImportNotSaved) {
							transition.abort();
						}
						// a 'transition prevented' error
					});

				$rootScope.stateSuccessfullyLoaded = {};
				$transitions.onSuccess({},
					function (transition) {
						$rootScope.stateSuccessfullyLoaded[transition.from().name] = true;
					});

				// It's very handy to add references to $state and $stateParams to the $rootScope
				// so that you can access them from any scope within your applications.For example,
				// <li ui-sref-active="active }"> will set the <li> // to active whenever
				// 'contacts.list' or one of its decendents is active.
				$rootScope.$state = $state;
				$rootScope.$stateParams = $stateParams;

				uiSelect2Config.placeholder = 'Please Choose';
				uiSelect2Config.minimumResultsForSearch = 20;
				uiSelect2Config.allowClear = false;
			}
		]
	);

	// THE parent controller for the manageTrial (create/edit) page
	manageTrialApp.controller('manageTrialCtrl', ['$scope', '$rootScope', 'studyStateService', 'TrialManagerDataService', '$http',
		'$timeout', '_', '$localStorage', '$state', '$location', 'derivedVariableService', 'exportStudyModalService', 'importStudyModalService', '$uibModal', '$q', 'datasetService',
		function ($scope, $rootScope, studyStateService, TrialManagerDataService, $http, $timeout, _, $localStorage, $state, $location,
				  derivedVariableService, exportStudyModalService, importStudyModalService, $uibModal, $q, datasetService) {

			$scope.trialTabs = [
				{
					name: 'Settings',
					state: 'trialSettings'
				},
				{
					name: 'Germplasm & Checks',
					state: 'germplasm'
				},
                {   name: 'Treatment Factors',
                    state: 'treatment'
                },
				{   name: 'Environments',
					state: 'environment'
				},
				{
					name: 'Experimental Design',
					state: 'experimentalDesign'
				},
				{
					name: 'Measurements',
					state: 'createMeasurements'
				},
				{
					name: 'Measurements',
					state: 'editMeasurements'
				}
			];
			$scope.subObservationTabs = [];
			$scope.tabSelected = 'trialSettings';
			$scope.isSettingsTab = true;
			$location.path('/trialSettings');
			$scope.advanceTabsData = [];
			$scope.advanceTabs = [];
			$scope.sampleTabsData = [];
			$scope.sampleTabs = [];
			$scope.crossesTabsData = [];
			$scope.crossesTabs = [];
			$scope.isOpenStudy = TrialManagerDataService.isOpenStudy;
			$scope.isLockedStudy = TrialManagerDataService.isLockedStudy;
			$scope.studyTypes = [];
			$scope.studyTypeSelected = undefined;
			$scope.isChoosePreviousStudy = false;
			$scope.hasUnsavedData = studyStateService.hasUnsavedData;

			var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

			var config = {
				headers: {
					'X-Auth-Token': xAuthToken
				}
			};

			$http.get('/bmsapi/studytype/' + cropName + '/allVisible').success(function (data) {
				$scope.studyTypes = data;

			}).error(function (data) {
				if (data.status == 401) {
					bmsAuth.handleReAuthentication();
				}
				showErrorMessage('', data.error.message);
			});

			$scope.changeSelectStudyType = function (studyTypeSelected) {
				angular.forEach($scope.studyTypes, function (studyType) {
					if (studyType.id == studyTypeSelected) {
						$scope.data.studyType = studyType.name;
						return;
					}
				});
			};

			$scope.toggleChoosePreviousStudy = function () {
				$scope.isChoosePreviousStudy = !$scope.isChoosePreviousStudy;
			};

			$scope.resetTabsData = function () {
				// reset the service data to initial state (for untick of user previous study)
				_.each(_.keys($localStorage.serviceBackup.settings), function (key) {
					if ('basicDetails' !== key) {
						TrialManagerDataService.updateSettings(key, angular.copy($localStorage.serviceBackup.settings[key]));
					}
				});

				_.each(_.keys($localStorage.serviceBackup.currentData), function (key) {
					if ('basicDetails' !== key) {
						TrialManagerDataService.updateCurrentData(key, angular.copy($localStorage.serviceBackup.currentData[key]));
					}
				});

				TrialManagerDataService.applicationData = angular.copy($localStorage.serviceBackup.applicationData);
				TrialManagerDataService.trialMeasurement = angular.copy($localStorage.serviceBackup.trialMeasurement);

				// perform other cleanup tasks
				$http({
					url: '/Fieldbook/TrialManager/createTrial/clearSettings',
					method: 'GET',
					transformResponse: undefined
				}).then(function (response) {
					if (response.data !== 'success' || response.status !== 200) {
						showErrorMessage('', 'Your study settings could not be cleared at the moment. Please try again later.');
					}
				});

				var measurementDiv = $('#measurementsDiv');
				if (measurementDiv.length !== 0) {
					//measurementDiv.html('');
				}
				if (typeof resetGermplasmList !== 'undefined') {
					resetGermplasmList();
				}
			};

			// To apply scope safely
			$scope.safeApply = function (fn) {
				var phase = this.$root.$$phase;
				if (phase === '$apply' || phase === '$digest') {
					if (fn && (typeof(fn) === 'function')) {
						fn();
					}
				} else {
					this.$apply(fn);
				}
			};
			$scope.data = TrialManagerDataService.currentData.basicDetails;

			$scope.warnMissingInputData = function (response) {
				var deferred = $q.defer();
				var dependencyVariables = response.data;
				if (dependencyVariables.length > 0) {
					$uibModal.open({
						animation: true,
						templateUrl: '/Fieldbook/static/angular-templates/derivedTraitsValidationModal.html',
						size: 'md',
						controller: function ($scope, $uibModalInstance) {
							$scope.dependencyVariables = dependencyVariables;
							$scope.continue = function () {
								$uibModalInstance.close();
								deferred.resolve();
							};
						}
					});
				} else {
					deferred.resolve();
				}
				return deferred.promise;
			};

			$scope.saveCurrentTrialData = function () {
				derivedVariableService.getDependencies().then(function (response) {
					return $scope.warnMissingInputData(response);
				}).then(function () {
					TrialManagerDataService.saveCurrentData();
				});
			};

			$scope.selectPreviousStudy = function () {
				openStudyTree(3, $scope.useExistingStudy);
			};

			$scope.changeFolderLocation = function () {
				openStudyTree(2, TrialManagerDataService.updateSelectedFolder);
			};

			$scope.useExistingStudy = function (existingStudyId) {
				$http.get('/Fieldbook/TrialManager/createTrial/useExistingStudy?studyId=' + existingStudyId).success(function (data) {
					// update data and settings
					if (data.createTrialForm !== null && data.createTrialForm.hasError === true) {
						$scope.resetTabsData();
						createErrorNotification(errorMsgHeader, data.createTrialForm.errorMessage);
					} else {
						var environmentData = TrialManagerDataService.extractData(data.environmentData);
						var environmentSettings = TrialManagerDataService.extractSettings(data.environmentData);

						if (environmentData.noOfEnvironments > 0 && environmentData.environments.length === 0) {
							while (environmentData.environments.length !== environmentData.noOfEnvironments) {
								environmentData.environments.push({
									managementDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
										environmentSettings.managementDetails),
									trialDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
										environmentSettings.trialConditionDetails)
								});
							}
						}

						// update Select StudyType.
						angular.forEach($scope.studyTypes, function (studyType) {
								if (studyType.label === data.createTrialForm.studyTypeName) {
									$scope.changeSelectStudyType(studyType.id);
									$('#studyTypeId').val("number:" + studyType.id.toString());
									return;
								}
							}
						);

						TrialManagerDataService.updateCurrentData('trialSettings',
							TrialManagerDataService.extractData(data.trialSettingsData));
						TrialManagerDataService.updateCurrentData('environments', environmentData);
						TrialManagerDataService.updateCurrentData('treatmentFactors', TrialManagerDataService.extractData(
							data.treatmentFactorsData));

						//Added-selectionVariates
						TrialManagerDataService.updateSettings('trialSettings', TrialManagerDataService.extractSettings(
							data.trialSettingsData));
						TrialManagerDataService.updateSettings('selectionVariables', TrialManagerDataService.extractSettings(
							data.selectionVariableData));
						TrialManagerDataService.updateSettings('environments', environmentSettings);
						TrialManagerDataService.updateSettings('germplasm', TrialManagerDataService.extractSettings(data.germplasmData));
						TrialManagerDataService.updateSettings('treatmentFactors', TrialManagerDataService.extractTreatmentFactorSettings(
							data.treatmentFactorsData));
						TrialManagerDataService.updateSettings('measurements',
							TrialManagerDataService.extractSettings(data.measurementsData));
					}
				});
			};
			$scope.refreshTabAfterImport = function () {
				$http.get('/Fieldbook/TrialManager/createTrial/refresh/settings/tab').success(function (data) {
					// update data and settings

					var environmentData = TrialManagerDataService.extractData(data.environmentData);
					TrialManagerDataService.updateCurrentData('trialSettings', TrialManagerDataService.extractData(data.trialSettingsData));
					TrialManagerDataService.updateCurrentData('environments', environmentData);
				});
			};
			$scope.temp = {
				noOfEnvironments: 0
			};
			$scope.refreshEnvironmentsAndExperimentalDesign = function () {
				var currentDesignType = TrialManagerDataService.currentData.experimentalDesign.designType;
				var showIndicateUnappliedChangesWarning = true;

				var designTypes = TrialManagerDataService.applicationData.designTypes;

				if (TrialManagerDataService.getDesignTypeById(currentDesignType, designTypes).name === 'Custom Import Design') {
					TrialManagerDataService.currentData.experimentalDesign.noOfEnvironmentsToAdd = $scope.temp.noOfEnvironments;
					showIndicateUnappliedChangesWarning = false;
					ImportDesign.showPopup(ImportDesign.hasGermplasmListSelected());
					showAlertMessage('', addEnvironmentsImportDesignMessage, 5000);
				}

				$state.go('environment', {
					addtlNumOfEnvironments: $scope.temp.noOfEnvironments,
					displayWarningMessage: showIndicateUnappliedChangesWarning, timestamp: new Date()
				});

				TrialManagerDataService.applicationData.hasNewEnvironmentAdded = true;

				$state.go('environment', {addtlNumOfEnvironments: $scope.temp.noOfEnvironments, timestamp: new Date()});
				$scope.performFunctionOnTabChange('environment');

			};

			$scope.loadMeasurementsTabInBackground = function () {
				if (isOpenStudy()) {
					$state.go('editMeasurements', {}, {location: false});
				}

			};
			$scope.displayMeasurementOnlyActions = function () {
				return TrialManagerDataService.trialMeasurement.count &&
					TrialManagerDataService.trialMeasurement.count > 0 && !TrialManagerDataService.applicationData.unsavedGeneratedDesign &&
					!TrialManagerDataService.applicationData.unsavedTraitsAvailable;
			};
			$scope.hasMeasurementData = function () {
				return TrialManagerDataService.trialMeasurement.count &&
					TrialManagerDataService.trialMeasurement.count > 0;
			};

			$scope.hasGermplasmListSelected = function () {
				return TrialManagerDataService.applicationData.germplasmListSelected;
			};

			$scope.displayGermplasmOrMeasurmentOnlyActions = function () {
				return this.hasGermplasmListSelected() || this.displayMeasurementOnlyActions();
			};

			$scope.displayExecuteCalculatedVariableOnlyActions = function () {
				return this.hasCalculatedVariable() && this.displayMeasurementOnlyActions();
			};

			$scope.hasCalculatedVariable = function () {
				return TrialManagerDataService.settings.measurements.m_keys.some(function (key) {
					return TrialManagerDataService.settings.measurements.m_vals[key].variable.formula;
				});
			};

			// Programatically navigate to specified tab state
			$scope.navigateToTab = function (targetState) {
				$state.go(targetState);
				$scope.performFunctionOnTabChange(targetState);

			};

			$scope.navigateToSubObsTab = function (datasetId) {
				var subObsTab = undefined;
				angular.forEach($scope.subObservationTabs, function (subObservationTab) {
					if (subObservationTab.id === datasetId) {
						subObsTab = subObservationTab
					}
				});

				var params = {subObservationTabId: subObsTab.id, subObservationTab: subObsTab};
				$scope.isSettingsTab = false;
				$scope.tabSelected = subObsTab.state;
				$state.go('subObservationTabs', params);
			};

			$scope.hasAdvanceListCreated = function () {
				return $scope.advanceTabsData.length !== 0;
			};

			$scope.performFunctionOnTabChange = function (targetState) {
				// do not switch tab if we have newly imported measurements or stock list is not saved
				if (stockListImportNotSaved || $('.import-study-data').data('data-import') === '1') {
					// Display warning if the user tries to navigate across tabs(except advance & stock-list tab) without saving imported inventory file
					showAlertMessage('', importSaveDataWarningMessage);
					return;
				}

				$scope.isSettingsTab = true;
				$scope.tabSelected = targetState;
				if (targetState === 'editMeasurements') {
					// we need to redraw the columns of the table on tab change as they appear all to be squeezed to the left corner
					// of the table if we do not do that
					if ($('body').hasClass('preview-measurements-only')) {
						if ($('#preview-measurement-table').length !== 0 && $('#preview-measurement-table').dataTable()) {
							$timeout(function () {
								$('#preview-measurement-table').dataTable().fnAdjustColumnSizing();
							}, 1);
						}
					} else {
						if ($('#measurement-table').length !== 0 && $('#measurement-table').dataTable() !== null) {
							$timeout(function () {
								$('#measurement-table').dataTable().fnAdjustColumnSizing();
							}, 1);
						}
					}

					if (TrialManagerDataService.applicationData.unappliedChangesAvailable) {
						showAlertMessage('', 'Changes have been made that may affect the experimental design of this study.' +
							'Please regenerate the design on the Experimental Design tab', 10000);
					}
				} else if (targetState === 'experimentalDesign') {
					if (TrialManagerDataService.applicationData.unappliedChangesAvailable) {
						showAlertMessage('', 'Study settings have been updated since the experimental design was generated. ' +
							'Please select a design type and specify the parameters for your study again', 10000);
					}
				} else if (targetState === 'createMeasurements') {
					if (TrialManagerDataService.applicationData.unsavedGeneratedDesign) {
						$rootScope.$broadcast('previewMeasurements');
					}
					if (TrialManagerDataService.applicationData.unappliedChangesAvailable) {
						showAlertMessage('', 'Changes have been made that may affect the experimental design of this study.' +
							'Please regenerate the design on the Experimental Design tab', 10000);
					}

					// we need to redraw the columns of the table on tab change as they appear all to be squeezed to the left corner
					// of the table if we do not do that
					if (!TrialManagerDataService.applicationData.unsavedGeneratedDesign && $('#preview-measurement-table').length !== 0 &&
						$('#preview-measurement-table').dataTable()) {
						$timeout(function () {
							$('#preview-measurement-table').dataTable().fnAdjustColumnSizing();
						}, 1);
					}
				} else if (targetState === 'germplasm') {
					// we need to redraw the columns of the table on tab change as they appear all to be squeezed to the left corner
					// of the table if we do not do that
					if ($('#tableForGermplasm').length !== 0 && $('#tableForGermplasm').dataTable() !== null) {
						$timeout(function () {
							$('#tableForGermplasm').dataTable().fnAdjustColumnSizing();
						}, 1);
					}
				} else if (targetState === 'environment') {
					// we need to redraw the columns of the table on tab change as they appear all to be squeezed to the left corner
					// of the table if we do not do that
					if ($('#environment-table .fbk-datatable-environments').length !== 0 && $('#environment-table .fbk-datatable-environments').DataTable() !== null) {
						$timeout(function () {
							$('#environment-table .fbk-datatable-environments').DataTable().columns.adjust().draw();
						}, 1);
					}
				}
			};

			$scope.addStockTabData = function (tabId, tabData, listName, isPageLoading) {
				var isAdvanceStock = false;
				var isCrossesStock = false;
				var isAdvance = false;

				if (isPageLoading === undefined) {
					isPageLoading = false;
				}

				if ($scope.stockListTabs === undefined) {
					$scope.stockListTabs = [];
				}

				angular.forEach($scope.advanceTabs, function (value, index) {
					if (!isAdvance && value.id === parseInt(tabId)) {
						isAdvance = true;
					}

					if (!isAdvanceStock && value.state === 'stock-list' + tabId + '-li') {
						isAdvanceStock = true;
					}

					if (isAdvance && isAdvanceStock) {
						$scope.advanceTabsData[index].data = tabData;
					}
				});

				angular.forEach($scope.crossesTabs, function (value, index) {
					if (!isCrossesStock && value.state === 'stock-list' + tabId + '-li') {
						$scope.crossesTabsData[index].data = tabData;
						isCrossesStock = true;
					}
				});

				if (!isAdvanceStock && isAdvance) {
					angular.forEach($scope.advanceTabs, function (value, index) {
						if (!isAdvanceStock) {
							if (parseInt(value.id) === parseInt(tabId)) {
								$scope.advanceTabs.splice(index + 1, 0, {
									name: listName,
									state: 'stock-list' + tabId + '-li',
									id: tabId,
									displayName: 'Stock List:[' + $scope.advanceTabs[index].name + ']'
								});

								$scope.advanceTabsData.splice(index + 1, 0, {
									name: 'stock-list' + tabId + '-li',
									data: tabData,
									id: 'stock-content-pane' + tabId
								});
								isAdvanceStock = true;
							}
						}
					});

				} else if (!isCrossesStock && !isAdvance) {
					angular.forEach($scope.crossesTabs, function (value, index) {
						if (!isCrossesStock) {
							if (parseInt(value.id) === parseInt(tabId)) {
								$scope.crossesTabs.splice(index + 1, 0, {
									name: listName,
									state: 'stock-list' + tabId + '-li',
									id: tabId,
									displayName: 'Stock List:[' + $scope.crossesTabs[index].name + ']'
								});

								$scope.crossesTabsData.splice(index + 1, 0, {
									name: 'stock-list' + tabId + '-li',
									data: tabData,
									id: 'stock-content-pane' + tabId
								});
								isCrossesStock = true;
							}
						}
					});
				}

				if (isPageLoading !== true) {
					$scope.tabSelected = 'stock-list' + tabId + '-li';
				}

				$('#listActionButton' + tabId).addClass('disabled');
			};

			$scope.addAdvanceTabData = function (tabId, tabData, listName, isPageLoading) {
				var isUpdate = false;
				if (isPageLoading === undefined) {
					isPageLoading = false;
				}

				angular.forEach($scope.advanceTabs, function (value, index) {
						if (!isUpdate && value.name === listName && parseInt(value.id) === parseInt(tabId)) {
							isUpdate = true;
							$scope.advanceTabsData[index].data = tabData;

						}
					}
				);

				if (!isUpdate) {
					$scope.advanceTabs.push({
						name: listName,
						state: 'advance-list' + tabId + '-li',
						id: tabId,
						displayName: 'Advance List: [' + listName + ']'
					});

					$scope.advanceTabsData.push({
						name: 'advance-list' + tabId + '-li',
						data: tabData,
						id: 'advance-list' + tabId + '-li'
					});

					if (isPageLoading !== true) {
						$scope.tabSelected = 'advance-list' + tabId + '-li';
						$scope.isSettingsTab = false;
					}
				}

			};

			$scope.addSampleTabData = function (tabId, tabData, listName, isPageLoading) {
				var isSwap = false;
				var isUpdate = false;
				if (isPageLoading === undefined) {
					isPageLoading = false;
				}
				angular.forEach($scope.sampleTabs, function (value, index) {
						if (!isUpdate && value.name === listName && parseInt(value.id) === parseInt(tabId)) {
							isUpdate = true;
							$scope.sampleTabsData[index].data = tabData;

						}
					}
				);

				if (!isSwap && !isUpdate) {
					$scope.sampleTabs.push({
						name: listName,
						state: 'sample-list' + tabId + '-li',
						id: tabId,
						displayName: 'Sample List: [' + listName + ']'
					});
					$scope.sampleTabsData.push({
						name: 'sample-list' + tabId + '-li',
						data: tabData,
						id: 'sample-list' + tabId + '-li'
					});
					if (isPageLoading !== true) {
						$scope.tabSelected = 'sample-list' + tabId + '-li';
						$scope.isSettingsTab = false;
					}
				}
			};

			$scope.addCrossesTabData = function (tabId, tabData, listName, crossesType, isPageLoading) {
				var isUpdate = false;
				if (isPageLoading === undefined) {
					isPageLoading = false;
				}
				angular.forEach($scope.crossesTabs, function (value, index) {
						if (!isUpdate && value.name === listName && parseInt(value.id) === parseInt(tabId)) {
							isUpdate = true;
							$scope.crossesTabsData[index].data = tabData;

						}
					}
				);

				if (!isUpdate) {
					$scope.crossesTabs.push({
						name: listName,
						state: 'crosses-list' + tabId + '-li',
						id: tabId,
						displayName: crossesType + ': [' + listName + ']'
					});
					$scope.crossesTabsData.push({
						name: 'crosses-list' + tabId + '-li',
						data: tabData,
						id: 'crosses-list' + tabId + '-li'
					});
					if (isPageLoading !== true) {
						$scope.tabSelected = 'crosses-list' + tabId + '-li';
						$scope.isSettingsTab = false;
					}
				}
			};

			$scope.addSubObservationTabData = function (id, name, datasetTypeId, parentDatasetId) {
				var datasetType = datasetService.getDatasetType(datasetTypeId);

				var newSubObsTab = {
					id: id,
					name: name,
					tabName: datasetType.abbr + ': ' + name,
					titleName: datasetType.name + ': ' + name,
					state: '/subObservationTabs/' + id, // arbitrary prefix to filter tab content
					subObservationSets: [{
						id: id,
						name: name,
						datasetTypeId: datasetTypeId,
						parentDatasetId: parentDatasetId
					}]
				};

				$scope.subObservationTabs.push(newSubObsTab);
				var params = {subObservationTabId: id, subObservationTab: newSubObsTab};

				$scope.isSettingsTab = false;
				$scope.tabSelected = newSubObsTab.state;
				$state.go('subObservationTabs', params);

			};

			datasetService.getDatasets().then(function (data) {
				/**
				 * Restructure list from server based on parentDatasetId (can be null)
				 * Example:
				 *
				 *         plotdata+--------------------+
				 *            +                         |
				 *            v                         v
				 *    plants-dataset+---+        timeseries-dataset
				 *            +         |
				 *            v         v
				 *  fruits-dataset    leafs-datasets
				 *
				 *                          +
				 *                          |   transform into tabs
				 *                          v
				 *
				 * +-------------+-----------------+---------------------+
				 * |   plotdata  |  plants-dataset | timeseries-dataset  |
				 * +-------------+----------+------+---------------------+
				 *                          |
				 *  +-----------------------+
				 *  |
				 * +v--------------+-----------------+----------------+
				 * |plants-dataset |  fruits-dataset | leafs-datasets |
				 * +---------------+-----------------+----------------+
				 *
				 */

				// utility maps to easily get what we want
				var datasetByParent = {};
				var datasetById = {};
				angular.forEach(data, function (dataset) {
					datasetByParent[dataset.parentDatasetId] = dataset;
					datasetById[dataset.datasetId] = dataset;
				});

				// restructure in tabs - a second iteration is needed once we have the full byParent map
				var datasetByTabs = {};
				angular.forEach(data, function (dataset) {
					var parent = dataset;
					// subobservation sets can be nested
					while (parent.parentDatasetId && datasetById[parent.parentDatasetId]) {
						parent = datasetById[parent.parentDatasetId];
					}
					datasetByTabs[parent.datasetId] = datasetByTabs[parent.datasetId] || [];
					datasetByTabs[parent.datasetId].push(dataset);
				});

				var subObservationTabs = data.filter(function (dataset) {
					// those whose parent is not in the list are considered roots
					return !datasetById[dataset.parentDatasetId];
				});

				angular.forEach(subObservationTabs, function (datasetTab) {
					var datasetType = datasetService.getDatasetType(datasetTab.datasetTypeId);
					$scope.subObservationTabs.push({
						id: datasetTab.datasetId,
						name: datasetTab.name,
						tabName: datasetType.abbr + ': ' + datasetTab.name,
						titleName: datasetType.name + ': ' + datasetTab.name,
						state: '/subObservationTabs/' + datasetTab.datasetId, // arbitrary prefix to filter tab content
						subObservationSets: datasetByTabs[datasetTab.datasetId].map(function (dataset) {
							return {
								id: dataset.datasetId,
								name: dataset.name,
								datasetTypeId: dataset.datasetTypeId,
								parentDatasetId: dataset.parentDatasetId
							}
						})
					});
				});
			}, function (response) {
				if (response.errors[0] && response.errors[0].message) {
					showErrorMessage('', response.errors[0].message);
				} else {
					showErrorMessage('', ajaxGenericErrorMsg);
				}
			});

			$scope.advancedTrialList = TrialManagerDataService.settings.advancedList;

			angular.forEach($scope.advancedTrialList, function (value) {
				displayAdvanceList(value.id, value.name, false, '', true);
			});

			$scope.sampleList = TrialManagerDataService.settings.sampleList;

			angular.forEach($scope.sampleList, function (value) {
				displaySampleList(value.listId, value.listName, true);
			});

			$scope.crossesList = TrialManagerDataService.settings.crossesList;

			angular.forEach($scope.crossesList, function (value) {
				displayCrossesList(value.id, value.name, value.crossesType, true, '', true);
			});

			$scope.tabChange = function (selectedTab) {

				// Display warning if the user tries to navigate across tabs(advance & stock-list tab) without saving imported inventory file
				if (stockListImportNotSaved) {
					showAlertMessage('', importSaveDataWarningMessage);
					return;
				}
				$scope.tabSelected = selectedTab;
				$scope.isSettingsTab = false;

				// Load selected stock list inventory page setup function single time
				if ($scope.stockListTabs && $scope.stockListTabs.indexOf(selectedTab) === -1) {
					var isStock = selectedTab.split('-');
					if (isStock[0] === 'stock') {
						$scope.stockListTabs.push(selectedTab);
						setTimeout(InventoryPage.setupPage, 100);
					}
				}
			};

			$scope.closeAdvanceListTab = function (tab) {
				var index = $scope.findIndexByKeyValue($scope.advanceTabs, 'state', tab);
				$scope.advanceTabs.splice(index, 1);
				$scope.advanceTabsData.splice(index, 1);
				$scope.tabSelected = 'trialSettings';
				$scope.isSettingsTab = true;
			};

			$scope.closeSampleListTab = function (tab) {
				var index = $scope.findIndexByKeyValue($scope.sampleTabs, 'state', tab);
				$scope.sampleTabs.splice(index, 1);
				$scope.sampleTabsData.splice(index, 1);
				$scope.tabSelected = 'trialSettings';
				$scope.isSettingsTab = true;
			};

			$scope.closeCrossesListTab = function (tab) {
				var index = $scope.findIndexByKeyValue($scope.crossesTabs, 'state', tab);
				$scope.crossesTabs.splice(index, 1);
				$scope.crossesTabsData.splice(index, 1);
				$scope.tabSelected = 'trialSettings';
				$scope.isSettingsTab = true;
			};

			$scope.initSampleTab = function (tab) {
				$timeout(function () {
					$('#sample-list-' + tab.id).dataTable().fnAdjustColumnSizing();
				}, 1);
			};

			$scope.userHasLockPermission = function () {
				return $scope.data.userID === currentCropUserId || isSuperAdmin;
			};

			$scope.changeLockedStatus = function (doLock) {
				TrialManagerDataService.changeLockedStatus(doLock);
			};

			$('body').on('DO_AUTO_SAVE', function () {
				TrialManagerDataService.saveCurrentData();
			});
			$('body').on('REFRESH_AFTER_IMPORT_SAVE', function () {
				$scope.refreshTabAfterImport();
			});
			$scope.findIndexByKeyValue = function (arraytosearch, key, valuetosearch) {
				for (var i = 0; i < arraytosearch.length; i++) {
					if (arraytosearch[i][key] === valuetosearch) {
						return i;
					}
				}
				return null;
			};

			$rootScope.openConfirmModal = function (message, confirmButtonLabel, cancelButtonLabel) {

				var modalInstance = $uibModal.open({
					animation: true,
					templateUrl: '/Fieldbook/static/angular-templates/confirmModal.html',
					controller: function ($scope, $uibModalInstance) {
						$scope.text = message;
						$scope.confirmButtonLabel = confirmButtonLabel;
						$scope.cancelButtonLabel = cancelButtonLabel || cancelLabel;

						$scope.confirm = function () {
							$uibModalInstance.close(true);
						};

						$scope.cancel = function () {
							$uibModalInstance.close(false);
						};
					}
				});
				return modalInstance;
			};

			$scope.showExportStudyModal = function() {
				exportStudyModalService.openDatasetOptionModal();
			}

			$scope.showImportStudyModal = function() {
				importStudyModalService.openDatasetOptionModal();
			}

		}]);

	manageTrialApp.filter('filterMeasurementState', function () {
		return function (tabs, isOpenStudy) {
			var filtered = angular.copy(tabs);

			for (var i = 0; i < filtered.length; i++) {
				if (filtered[i].state === 'editMeasurements' && isOpenStudy) {
					filtered.splice(i, 1);

					break;
				} else if (filtered[i].state === 'openMeasurements' && !isOpenStudy) {
					filtered.splice(i, 1);

					break;
				}
			}

			return filtered;
		};
	});

	manageTrialApp.filter('orderObjectBy', function () {
		return function (items, field, reverse) {
			var filtered = [];
			angular.forEach(items, function (item) {
				filtered.push(item);
			});
			filtered.sort(function (a, b) {
				return (a[field] > b[field] ? 1 : -1);
			});
			if (reverse) {
				filtered.reverse();
			}
			return filtered;
		};
	});

	// README IMPORTANT: Code unmanaged by angular should go here
	document.onInitManageTrial = function () {
		// do nothing for now
		$('body').data('trialStatus', operationMode);
	};

})();
