
/*global angular,openStudyTree, SpinnerManager, ajaxGenericErrorMsg, showErrorMessage, operationMode, resetGermplasmList,
showAlertMessage,importSaveDataWarningMessage,createErrorNotification,errorMsgHeader, ImportDesign*/

(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp', ['designImportApp', 'leafnode-utils', 'fieldbook-utils',
		'ct.ui.router.extras', 'ui.bootstrap', 'ngLodash', 'ngResource', 'ngStorage', 'datatables', 'datatables.buttons',
		'showSettingFormElementNew']);

	// HTTP INTERCEPTOR CONFIGURATION START
	// The following block defines an interceptor that hooks into AJAX operations initiated by Angular to start / stop the spinner operation
	manageTrialApp.factory('spinnerHttpInterceptor', function($q) {
		return {
			request: function(config) {
				SpinnerManager.addActive();

				return config || $q.when(config);
			},
			requestError: function(config) {
				SpinnerManager.resolveActive();
				showErrorMessage('', ajaxGenericErrorMsg);

				return config || $q.when(config);
			},
			response: function(config) {
				SpinnerManager.resolveActive();

				return config || $q.when(config);
			},
			responseError: function(config) {
				SpinnerManager.resolveActive();
				showErrorMessage('', ajaxGenericErrorMsg);

				return config || $q.when(config);
			}
		};
	});

	// Added to prevent Unsecured HTML error
	manageTrialApp.config(function($sceProvider) {
		$sceProvider.enabled(false);
	});

	manageTrialApp.config(['$httpProvider', function($httpProvider) {
		$httpProvider.interceptors.push('spinnerHttpInterceptor');
	}]);

	// HTTP INTERCEPTOR CONFIGURATION END

	// routing configuration
	// TODO: if possible, retrieve the template urls from the list of constants
	manageTrialApp.config(function($stateProvider, $urlRouterProvider, $stickyStateProvider) {

		$stickyStateProvider.enableDebug(false);

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
			});

	});

	// common filters
	manageTrialApp.filter('range', function() {
		return function(input, total) {
			total = parseInt(total);
			for (var i = 0; i < total; i++) {
				input.push(i);
			}

			return input;
		};
	});

	manageTrialApp.run(
		['$rootScope', '$state', '$stateParams', 'uiSelect2Config', 'VARIABLE_TYPES',
			function($rootScope, $state, $stateParams, uiSelect2Config, VARIABLE_TYPES) {
				$rootScope.VARIABLE_TYPES = VARIABLE_TYPES;

				$rootScope.$on('$stateChangeStart',
					function(event) {
						if ($('.import-study-data').data('data-import') === '1') {
							showAlertMessage('', importSaveDataWarningMessage);

							event.preventDefault();
						}
						if (stockListImportNotSaved) {
							showAlertMessage('', importSaveDataWarningMessage);
							e.preventDefault();
						}

						// a 'transition prevented' error
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
	manageTrialApp.controller('manageTrialCtrl', ['$scope', '$rootScope', 'TrialManagerDataService', '$http', '$timeout', '_',
		'$localStorage', '$state', '$location', function($scope, $rootScope, TrialManagerDataService, $http, $timeout, _, $localStorage,
			$state, $location) {
			$scope.trialTabs = [
				{   name: 'Settings',
					state: 'trialSettings'
				},
				{   name: 'Germplasm',
					state: 'germplasm'
				},
				{   name: 'Treatment Factors',
					state: 'treatment'
				},
				{   name: 'Environments',
					state: 'environment'
				},
				{   name: 'Experimental Design',
					state: 'experimentalDesign'
				},
				{   name: 'Measurements',
					state: 'createMeasurements'
				},
				{
					name: 'Measurements',
					state: 'editMeasurements'
				}
			];
			$scope.tabSelected = 'trialSettings';
			$scope.isSettingsTab = true;
			$location.path('/trialSettings');
			$scope.advanceTabsData = [];
			$scope.advanceTrialTabs = [];
			$scope.isOpenTrial = TrialManagerDataService.isOpenTrial;

			$scope.isChoosePreviousTrial = false;

			$scope.toggleChoosePreviousTrial = function() {
				$scope.isChoosePreviousTrial = !$scope.isChoosePreviousTrial;

				if (!$scope.isChoosePreviousTrial) {
					$scope.resetTabsData();
				}
			};

			$scope.resetTabsData = function() {
				// reset the service data to initial state (for untick of user previous trial)
				_.each(_.keys($localStorage.serviceBackup.settings), function(key) {
					if ('basicDetails' !== key) {
						TrialManagerDataService.updateSettings(key, angular.copy($localStorage.serviceBackup.settings[key]));
					}
				});

				_.each(_.keys($localStorage.serviceBackup.currentData), function(key) {
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
				}).then(function(response) {
					if (response.data !== 'success' || response.status !== 200) {
						showErrorMessage('', 'Your trial settings could not be cleared at the moment. Please try again later.');
					}
				});

				var measurementDiv = $('#measurementsDiv');
				if (measurementDiv.length !== 0) {
					measurementDiv.html('');
				}

				if (typeof resetGermplasmList !== 'undefined') {
					resetGermplasmList();
				}
			};

			// To apply scope safely
			$scope.safeApply = function(fn) {
				var phase = this.$root.$$phase;
				if (phase == '$apply' || phase == '$digest') {
					if (fn && (typeof(fn) === 'function')) {
						fn();
					}
				} else {
					this.$apply(fn);
				}
			};
			$scope.data = TrialManagerDataService.currentData.basicDetails;

			$scope.saveCurrentTrialData = TrialManagerDataService.saveCurrentData;

			$scope.selectPreviousTrial = function() {
				openStudyTree(3, $scope.useExistingTrial);
			};

			$scope.changeFolderLocation = function() {
				openStudyTree(2, TrialManagerDataService.updateSelectedFolder);
			};

			$scope.useExistingTrial = function(existingTrialID) {
				$http.get('/Fieldbook/TrialManager/createTrial/useExistingTrial?trialID=' + existingTrialID).success(function(data) {
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
						TrialManagerDataService.updateSettings('measurements',
							TrialManagerDataService.extractSettings(data.measurementsData));
					}
				});
			};
			$scope.refreshTabAfterImport = function() {
				$http.get('/Fieldbook/TrialManager/createTrial/refresh/settings/tab').success(function(data) {
					// update data and settings

					var environmentData = TrialManagerDataService.extractData(data.environmentData);
					TrialManagerDataService.updateCurrentData('trialSettings', TrialManagerDataService.extractData(data.trialSettingsData));
					TrialManagerDataService.updateCurrentData('environments', environmentData);
				});
			};
			$scope.temp = {
				noOfEnvironments: 0
			};
			$scope.refreshEnvironmentsAndExperimentalDesign = function() {
				var currentDesignType = TrialManagerDataService.currentData.experimentalDesign.designType;
				var showIndicateUnappliedChangesWarning = true;
				if (TrialManagerDataService.applicationData.designTypes[currentDesignType].name === 'Custom Import Design') {
					TrialManagerDataService.currentData.experimentalDesign.noOfEnvironmentsToAdd = $scope.temp.noOfEnvironments;
					showIndicateUnappliedChangesWarning = false;
					ImportDesign.showPopup(ImportDesign.hasGermplasmListSelected());
					showAlertMessage('', addEnvironmentsImportDesignMessage, 5000);
				}

				$state.go('environment', {addtlNumOfEnvironments:$scope.temp.noOfEnvironments, displayWarningMessage: showIndicateUnappliedChangesWarning, timestamp: new Date()});

				TrialManagerDataService.applicationData.hasNewEnvironmentAdded = true;

				//enable the user to regenerate preset design when the user adds new environment
				TrialManagerDataService.applicationData.hasGeneratedDesignPreset = false;

				$state.go('environment', {addtlNumOfEnvironments:$scope.temp.noOfEnvironments, timestamp: new Date()});
				$scope.performFunctionOnTabChange('environment');

			};

			$scope.loadMeasurementsTabInBackground = function() {
				if (isOpenTrial()) {
					$state.go('editMeasurements', {}, { location: false });
				}

			};
			$scope.displayMeasurementOnlyActions = function() {
				return TrialManagerDataService.trialMeasurement.count &&
					TrialManagerDataService.trialMeasurement.count > 0 && !TrialManagerDataService.applicationData.unsavedGeneratedDesign &&
					!TrialManagerDataService.applicationData.unsavedTraitsAvailable;
			};
			$scope.hasMeasurementData = function() {
				return TrialManagerDataService.trialMeasurement.count &&
					TrialManagerDataService.trialMeasurement.count > 0;
			};

			$scope.displayGermplasmOnlyActions = function() {
				return TrialManagerDataService.applicationData.germplasmListSelected;
			};

			// Programatically navigate to specified tab state
			$scope.navigateToTab = function(targetState) {
				$state.go(targetState);
				$scope.performFunctionOnTabChange(targetState);

			};

			$scope.performFunctionOnTabChange = function(targetState) {
				// do not switch tab if we have newly imported measurements or stock list is not saved
				if (stockListImportNotSaved || $('.import-study-data').data('data-import') === '1') {
					return;
				}

				$scope.isSettingsTab = true;
				$scope.tabSelected = targetState;
				if (targetState === 'editMeasurements') {
					if ($('#measurement-table').length !== 0 && $('#measurement-table').dataTable() !== null) {
						$timeout(function() {
							$('#measurement-table').dataTable().fnAdjustColumnSizing();
						}, 1);
					}
					if (TrialManagerDataService.applicationData.unappliedChangesAvailable) {
						showAlertMessage('', 'Changes have been made that may affect the experimental design of this trial.' +
							'Please regenerate the design on the Experimental Design tab', 10000);
					}
				} else if (targetState === 'experimentalDesign') {
					if (TrialManagerDataService.applicationData.unappliedChangesAvailable) {
						showAlertMessage('', 'Trial settings have been updated since the experimental design was generated. ' +
							'Please select a design type and specify the parameters for your trial again', 10000);
					}
				} else if (targetState === 'createMeasurements') {
					if (TrialManagerDataService.applicationData.unappliedChangesAvailable) {
						showAlertMessage('', 'Changes have been made that may affect the experimental design of this trial.' +
							'Please regenerate the design on the Experimental Design tab', 10000);
					}
				}

				if (targetState === 'createMeasurements' || targetState === 'editMeasurements') {
					//TODO Remove this global
					if ($('body').data('expDesignShowPreview') === '1') {
						$.ajax({
							url: '/Fieldbook/TrialManager/openTrial/load/preview/measurement',
							type: 'GET',
							data: '',
							cache: false,
							success: function(html) {
								setTimeout(function() {
									$('#measurementsDiv').html(html);
									//TODO Remove this global
									$('body').data('expDesignShowPreview', '0');
								}, 300);
							},
							error: function() {
								//TODO Localise this
								showErrorMessage('Server Error', 'Experimental design preview could not be generated.');
							}
						});
					}
				}
			};

			$scope.addAdvanceTabData = function(tabId, tabData, listName, isPageLoading) {
				isAdvanceListGeneratedForTrial = true;
				var isSwap = false;
				var isUpdate = false;
				if (isPageLoading === undefined) {
					isPageLoading = false;
				}
				angular.forEach($scope.advanceTrialTabs, function(value, index) {
					if (value.name == listName && value.id == tabId) {
						isUpdate = true;
						$scope.advanceTabsData[index].data = tabData;
						return;
					}
				}
				);

				$scope.stockListTabs = [];
				angular.forEach($scope.advanceTrialTabs, function(value, index) {
					if (!isSwap && !isUpdate) {
						if (value.id == tabId) {
							$scope.advanceTrialTabs.splice(index + 1, 0, {
								name: listName,
								state: 'stock-list' + tabId + '-li',
								id: tabId,
								displayName: 'Stock List:[' + $scope.advanceTrialTabs[index].name + ']'
							});

							$scope.advanceTabsData.splice(index + 1, 0, {
								name: 'stock-list' + tabId + '-li',
								data: tabData,
								id: 'stock-content-pane' + tabId
							});
							isSwap = true;
							if (isPageLoading !== true) {
								$scope.tabSelected = 'stock-list' + tabId + '-li';
							}
							$('#listActionButton' + tabId).addClass('disabled');
						}
					}
				});
				if (!isSwap && !isUpdate) {
					$scope.advanceTrialTabs.push({
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

			$scope.advancedTrialList = TrialManagerDataService.settings.advancedList;

			angular.forEach($scope.advancedTrialList, function(value) {
				displayAdvanceList('', value.id, value.name, false, '', true);
			});

			$scope.tabChange = function(selectedTab) {
				$scope.tabSelected = selectedTab;
				$scope.isSettingsTab = false;

				// Load selected stock list inventory page setup function single time
				if ($scope.stockListTabs.indexOf(selectedTab) === -1) {
					var isStock = selectedTab.split('-');
					if (isStock[0] === 'stock') {
						$scope.stockListTabs.push(selectedTab);
						setTimeout(InventoryPage.setupPage, 100);
					}
				}
			};

			$scope.closeAdvanceListTab = function(tab) {
				var index = $scope.findIndexByKeyValue($scope.advanceTrialTabs, 'state', tab);
				$scope.advanceTrialTabs.splice(index, 1);
				$scope.advanceTabsData.splice(index, 1);
				$scope.tabSelected = 'trialSettings';
				$scope.isSettingsTab = true;
			};

			$('body').on('DO_AUTO_SAVE', function() {
				TrialManagerDataService.saveCurrentData();
			});
			$('body').on('REFRESH_AFTER_IMPORT_SAVE', function() {
				$scope.refreshTabAfterImport();
			});
			$scope.findIndexByKeyValue = function(arraytosearch, key, valuetosearch) {
				for (var i = 0; i < arraytosearch.length; i++) {
					if (arraytosearch[i][key] == valuetosearch) {
						return i;
					}
				}
				return null;
			};
		}]);

	manageTrialApp.filter('filterMeasurementState', function() {
			return function(tabs, isOpenTrial) {
				var filtered = angular.copy(tabs);

				for (var i = 0; i < filtered.length; i++) {
					if (filtered[i].state === 'editMeasurements' && isOpenTrial) {
						filtered.splice(i, 1);

						break;
					} else if (filtered[i].state === 'openMeasurements' && !isOpenTrial) {
						filtered.splice(i, 1);

						break;
					}
				}

				return filtered;
			};
		});

	manageTrialApp.controller('ConfirmModalController', function($scope, $uibModalInstance, MODAL_TITLE, MODAL_TEXT, CONFIRM_BUTTON_LABEL) {
			$scope.title = MODAL_TITLE;
			$scope.text = MODAL_TEXT;
			$scope.confirmButtonLabel = CONFIRM_BUTTON_LABEL;

			$scope.confirm = function() {
				$uibModalInstance.close(true);
			};

			$scope.cancel = function() {
				$uibModalInstance.close(false);
			};
		});

	manageTrialApp.filter('orderObjectBy', function() {
		return function(items, field, reverse) {
			var filtered = [];
			angular.forEach(items, function(item) {
				filtered.push(item);
			});
			filtered.sort(function(a, b) {
				return (a[field] > b[field] ? 1 : -1);
			});
			if (reverse) {
				filtered.reverse();
			}
			return filtered;
		};
	});

	// README IMPORTANT: Code unmanaged by angular should go here
	document.onInitManageTrial = function() {
			// do nothing for now
			$('body').data('trialStatus', operationMode);
		};

})();
