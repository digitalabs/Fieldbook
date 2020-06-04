/*global angular, openStudyTree, showErrorMessage, operationMode, resetGermplasmList,
showAlertMessage,showMeasurementsPreview,createErrorNotification,errorMsgHeader,
stockListImportNotSaved, ImportDesign, isOpenStudy, displayAdvanceList, InventoryPage, ImportCrosses*/
//TODO move this messages under a namespace
/* global addEnvironmentsImportDesignMessage, importSaveDataWarningMessage*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp', ['designImportApp', 'leafnode-utils', 'fieldbook-utils', 'subObservation',
		'ui.router', 'ui.bootstrap', 'ngLodash', 'ngResource', 'ngStorage', 'datatables', 'datatables.buttons', 'datatables.colreorder',
		'showSettingFormElementNew', 'ngSanitize', 'ui.select', 'ngMessages', 'blockUI', 'datasets-api', 'auth', 'bmsAuth' , 'studyState',
		'export-study', 'import-study', 'create-sample', 'derived-variable', 'importObservationsApp']);

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

			.state('inventory', {
				url: '/inventory',
				views: {
					inventory: {
						controller: 'InventoryTabCtrl',
						templateUrl: '/Fieldbook/static/js/trialmanager/inventory/inventory-tab.html'
					}
				}
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
					subObservationTab: null,
					isPendingView: null
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
								subObservationSet: subObservationSet,
								isPendingView: trans.params().isPendingView
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
					subObservationSet: null,
					isPendingView: null
				},
			})
		;

	});

	manageTrialApp.config(['$provide', function ($provide) {
		$provide.decorator('$locale', function ($delegate) {
			var value = $delegate.DATETIME_FORMATS;
			value.SHORTDAY = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
			return $delegate;
		});
	}]);

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

	// do not switch tab if we have newly imported measurements or stock list is not saved
	function isTabChangeDisabled() {
		return stockListImportNotSaved || $('.import-study-data').data('data-import') === '1';
	}

	manageTrialApp.run(
		['$rootScope', '$state', '$stateParams', 'uiSelect2Config', 'VARIABLE_TYPES', '$transitions',
			function ($rootScope, $state, $stateParams, uiSelect2Config, VARIABLE_TYPES, $transitions) {
				$rootScope.VARIABLE_TYPES = VARIABLE_TYPES;

				$transitions.onStart({},
					function (transition) {
						if (isTabChangeDisabled()) {
							transition.abort();
						}
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
		'$timeout', '_', '$localStorage', '$state', '$location', 'HasAnyAuthorityService', 'derivedVariableService', 'exportStudyModalService',
		'importStudyModalService', 'createSampleModalService', 'derivedVariableModalService', '$uibModal', '$q', 'datasetService', 'InventoryService', 'studyContext', 'PERMISSIONS', 'LABEL_PRINTING_TYPE', 'HAS_LISTS_OR_SUB_OBS', 'HAS_GENERATED_DESIGN',
		function ($scope, $rootScope, studyStateService, TrialManagerDataService, $http, $timeout, _, $localStorage, $state, $location, HasAnyAuthorityService,
				  derivedVariableService, exportStudyModalService, importStudyModalService, createSampleModalService, derivedVariableModalService, $uibModal, $q, datasetService, InventoryService,
				  studyContext, PERMISSIONS, LABEL_PRINTING_TYPE, HAS_LISTS_OR_SUB_OBS, HAS_GENERATED_DESIGN) {

			$scope.trialTabs = [
				{
					name: 'Settings',
					state: 'trialSettings'
				},
				{
					name: 'Germplasm & Checks',
					state: 'germplasm'
				},
/*                {   name: 'Treatment Factors',
                    state: 'treatment'
                },*/
				{   name: 'Environments',
					state: 'environment'
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
			$scope.inventoryTab = {
				name: 'Inventory',
				state: 'inventory',
				hidden: true
			};

			$scope.isOpenStudy = TrialManagerDataService.isOpenStudy;
			$scope.isLockedStudy = TrialManagerDataService.isLockedStudy;
			$scope.studyTypes = [];
			$scope.studyTypeSelected = undefined;
			$scope.isChoosePreviousStudy = false;
			$scope.hasUnsavedData = studyStateService.hasUnsavedData;

			$scope.hasAnyAuthority = HasAnyAuthorityService.hasAnyAuthority;
			$scope.PERMISSIONS = PERMISSIONS;

			if ($scope.isOpenStudy()) {
				var environment = $scope.trialTabs.pop();
				$scope.trialTabs.push({
					name: 'Treatment Factors',
					state: 'treatment'
				});
				$scope.trialTabs.push(environment);
				$scope.trialTabs.push({
					name: 'Experimental Design',
					state: 'experimentalDesign'
				});

				$scope.trialTabs.push($scope.inventoryTab);
				InventoryService.searchStudyTransactions({
					sortedPageRequest: {pageNumber: 1, pageSize: 1}
				}).then((transactionsTable) => {
					if (transactionsTable.data.length) {
						$scope.inventoryTab.hidden = false;
					}
				});

				studyStateService.updateHasListsOrSubObs(HAS_LISTS_OR_SUB_OBS);
				studyStateService.updateGeneratedDesign(HAS_GENERATED_DESIGN);

			};

			$http.get('/bmsapi/crops/' + cropName + '/study-types/visible?programUUID=' + studyContext.programId).success(function (data) {
				$scope.studyTypes = data;

			}).error(function (data) {
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
				if ($localStorage.serviceBackup) {
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
				}

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

				if (typeof resetGermplasmList !== 'undefined') {
					resetGermplasmList();
				}
				TrialManagerDataService.resetServiceBackup();
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
				if (response && response.data.length > 0) {
					$uibModal.open({
						animation: true,
						templateUrl: '/Fieldbook/static/angular-templates/derivedTraitsValidationModal.html',
						size: 'md',
						controller: function ($scope, $uibModalInstance) {
							$scope.dependencyVariables = response.data;
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
				TrialManagerDataService.saveCurrentData();
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
						TrialManagerDataService.storeInitialValuesInServiceBackup();
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
						TrialManagerDataService.updateSettings('environments', environmentSettings);
						TrialManagerDataService.updateSettings('germplasm', TrialManagerDataService.extractSettings(data.germplasmData));
						TrialManagerDataService.updateSettings('treatmentFactors', TrialManagerDataService.extractTreatmentFactorSettings(
							data.treatmentFactorsData));
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

			$scope.hasGermplasmListSelected = function () {
				return TrialManagerDataService.applicationData.germplasmListSelected;
			};

			$scope.displayGermplasmOrMeasurmentOnlyActions = function () {
				return this.hasGermplasmListSelected() || studyStateService.hasGeneratedDesign();
			};

			$scope.displayExecuteCalculatedVariableOnlyActions = function () {
				return derivedVariableService.isStudyHasCalculatedVariables && studyStateService.hasGeneratedDesign();
			};

			$scope.reloadActionMenuConditions = function () {
				$scope.hasDesignGenerated = studyStateService.hasGeneratedDesign();
			};

			// Programatically navigate to specified tab state
			$rootScope.navigateToTab = function (targetState) {
				$state.go(targetState);
				$scope.performFunctionOnTabChange(targetState);

			};

			$rootScope.navigateToSubObsTab = function (datasetId, options) {
				var subObsTab = undefined;
				var subObsSet = undefined;
				angular.forEach($scope.subObservationTabs, function (subObservationTab) {
					angular.forEach(subObservationTab.subObservationSets, function (subObservationSet) {
						if (subObservationSet.id === datasetId) {
							subObsSet = subObservationSet;
							subObsTab = subObservationTab;
						}
					});
				});

				$scope.isSettingsTab = false;
				$scope.tabSelected = subObsTab.state;
				return $state.transitionTo('subObservationTabs.subObservationSets',  {
					subObservationTabId: subObsTab.id,
					subObservationTab: subObsTab,
					subObservationSetId: subObsSet.id,
					subObservationSet: subObsSet,
					isPendingView: options && options.isPendingView
				}, {
					reload: options && options.reload, inherit: false, notify: true
				});
			};

			$scope.hasAdvanceListCreated = function () {
				return $scope.advanceTabsData.length !== 0;
			};

			$scope.performFunctionOnTabChange = function (targetState) {
				if (isTabChangeDisabled()) {
					showAlertMessage('', importSaveDataWarningMessage);
					return;
				}

				$scope.isSettingsTab = true;
				$scope.tabSelected = targetState;

				// we need to redraw the columns of the table on tab change as they appear all to be squeezed to the left corner
				// of the table if we do not do that
				function adjustColumns($table) {
					if ($table.length !== 0 && $table.dataTable()) {
						$timeout(function () {
							$table.dataTable().fnAdjustColumnSizing();
						});
					}
				}

				if (targetState === 'germplasm') {
					adjustColumns($('#tableForGermplasm'));
				} else if (targetState === 'environment') {
					adjustColumns($('#environment-table .fbk-datatable-environments'));
				} else if (targetState.indexOf('/subObservationTabs/') === 0) {
					$rootScope.$broadcast('subObsTabSelected');
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
				studyStateService.updateHasListsOrSubObs(true);

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
				studyStateService.updateHasListsOrSubObs(true);

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
						$rootScope.$broadcast('sampleListCreated');
					}
				}
			};

			$scope.addCrossesTabData = function (tabId, tabData, listName, crossesType, isPageLoading) {
				studyStateService.updateHasListsOrSubObs(true);

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
				studyStateService.updateHasListsOrSubObs(true);

				var newSubObsTab = {
					id: id,
					name: name,
					datasetType: datasetType,
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

				var observationTabs = data.filter(function (dataset) {
					// those whose parent is not in the list are considered roots
					return !datasetById[dataset.parentDatasetId];
				});

				angular.forEach(observationTabs, function (datasetTab) {
					var datasetType = datasetService.getDatasetType(datasetTab.datasetTypeId);
					$scope.subObservationTabs.push({
						id: datasetTab.datasetId,
						name: datasetTab.name,
						datasetType: datasetType,
						hasPendingData: datasetTab.hasPendingData,
						state: '/subObservationTabs/' + datasetTab.datasetId, // arbitrary prefix to filter tab content
						subObservationSets: datasetByTabs[datasetTab.datasetId].map(function (dataset) {
							return {
								id: dataset.datasetId,
								name: dataset.name,
								hasPendingData: dataset.hasPendingData,
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

			$scope.listTabChange = function (selectedTab) {
				if (isTabChangeDisabled()) {
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

			$scope.isSaveDisabled = function () {
				return !$scope.isSaveEnabled() && !studyStateService.hasUnsavedData();
			};

			$scope.isSaveEnabled = function () {

				// Enable save button when Stock List tab is selected and only if there is an imported inventory.
				var enableSaveForStockList = $scope.tabSelected.indexOf('stock-list') >=0 && stockListImportNotSaved;

				return $scope.tabSelected && ([
					"trialSettings",
					"germplasm",
					"treatment",
					"environment",
				].indexOf($scope.tabSelected) >= 0 || enableSaveForStockList);


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
						$scope.confirmButtonLabel = confirmButtonLabel || okLabel;
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

			$scope.printLabels = function () {
				$uibModal.open({
					template: '<dataset-option-modal modal-title="modalTitle" message="message"' +
					' selected="selected" on-continue="forkPrintLabelFlows()"></dataset-option-modal>',
					size: 'md',
					controller: ['$scope', 'studyContext', function (scope, studyContext) {

						scope.modalTitle = 'Create planting labels';
						scope.message = 'Please choose the dataset you would like to print from:';
						scope.selected = {datasetId: studyContext.measurementDatasetId};

						scope.forkPrintLabelFlows = function () {
							if (studyContext.measurementDatasetId === scope.selected.datasetId) {
								// Old workflow for plot dataset. TODO migrate
								createLabelPrinting();
							} else {
								window.location.href = '/ibpworkbench/controller/jhipster#label-printing' +
									'?datasetId=' + scope.selected.datasetId +
									'&studyId=' + studyContext.studyId +
									'&programId=' + studyContext.programId +
									'&printingLabelType=' + LABEL_PRINTING_TYPE.SUBOBSERVATION_DATASET;
							}
						};
					}]
				});
			};

			$scope.showGeoJSONModal = function (isViewGeoJSON) {
				datasetService.getDatasetInstances(studyContext.measurementDatasetId).then((datasetInstances) => {
					let instances = datasetInstances.filter((instance) => instance.hasFieldLayout);
					if (!instances || !instances.length) {
						return showErrorMessage('', noLayoutError);
					}

					if (isViewGeoJSON) {
						instances = instances.filter((instance) => instance.hasGeoJSON)
						if (!instances.length) {
							return showErrorMessage('', geoReferenceViewNotAvailableError);
						}
					} else {
						instances = instances.filter((instance) => instance.hasFieldLayout && !instance.hasGeoJSON)
						if (!instances.length) {
							return showErrorMessage('', geoReferenceCreateNotAvailableError);
						}
					}

					$uibModal.open({
						template: '<single-instance-selector-modal instances="instances" ' +
							' instance-id-property="instanceDbId" ' +
							' selected="selected" ' +
							' on-select-instance="onSelectInstance" ' +
							' on-continue="onContinue" ' +
							' ></single-instance-selector-modal>',
						controller: function ($scope, $uibModalInstance) {
							$scope.selected = {};
							$scope.instances = instances;

							$scope.onContinue = function () {
								$uibModal.open({
									templateUrl: '/Fieldbook/static/angular-templates/geojson/geojson-modal.html',
									size: 'lg',
									controller: 'GeoJSONModalCtrl',
									resolve: {
										isViewGeoJSON: function () {
											return Boolean(isViewGeoJSON);
										},
										instanceId: function () {
											return $scope.selected.instanceDbId;
										}
									}
								});
							};
						}

					});
				});
			};

			$scope.preparePlanting = function () {
				$scope.navigateToSubObsTab(studyContext.measurementDatasetId).then(function () {
					$rootScope.$broadcast('startPlantingPreparation');
				});
			}

			$scope.showCreateSampleListModal = function() {
				createSampleModalService.openDatasetOptionModal();
			}

			$scope.showCalculatedVariableModal = function () {
				derivedVariableModalService.openDatasetOptionModal();
			}

			$scope.init = function () {
				derivedVariableService.displayExecuteCalculateVariableMenu();
			}

			$scope.init();

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
