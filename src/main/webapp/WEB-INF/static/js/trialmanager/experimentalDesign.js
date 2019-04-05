/* global angular, showErrorMessage, showAlertMessage, showSuccessfulMessage, expDesignMsgs */
(function() {
		'use strict';

		angular.module('manageTrialApp')
			.constant('EXP_DESIGN_MSGS', expDesignMsgs)
			.constant('EXPERIMENTAL_DESIGN_PARTIALS_LOC', '/Fieldbook/static/angular-templates/experimentalDesignPartials/')
			.controller('ExperimentalDesignCtrl', ['$scope', '$state', 'EXPERIMENTAL_DESIGN_PARTIALS_LOC','DESIGN_TYPE','SYSTEM_DEFINED_ENTRY_TYPE', 'TrialManagerDataService', '$http',
				'EXP_DESIGN_MSGS', '_', '$q', 'Messages', '$rootScope', function($scope, $state, EXPERIMENTAL_DESIGN_PARTIALS_LOC, DESIGN_TYPE, SYSTEM_DEFINED_ENTRY_TYPE, TrialManagerDataService, $http, EXP_DESIGN_MSGS, _, $q, Messages, $rootScope) {

					var ENTRY_TYPE_COLUMN_DATA_KEY = '8255-key';

					$scope.$on('$viewContentLoaded', function(){
						// This is to automatically refresh the design details for augmented design
						// whenever the Experimental tab is viewed
						if ($scope.data.designType === DESIGN_TYPE.AUGMENTED_RANDOMIZED_BLOCK) {
							$scope.refreshDesignDetailsForAugmentedDesign();
						}
						if ($scope.data.designType === DESIGN_TYPE.ENTRY_LIST_ORDER ) {
							$scope.refreshDesignDetailsForELODesign();
						}
						if ($scope.data.designType === DESIGN_TYPE.P_REP ) {
							$scope.refreshDesignDetailsForPRepDesign();
						}
					});

					$scope.applicationData = TrialManagerDataService.applicationData;
					$scope.studyID = TrialManagerDataService.currentData.basicDetails.studyID;

					$scope.designTypes = TrialManagerDataService.applicationData.designTypes;
					$scope.insertionManners = TrialManagerDataService.applicationData.insertionManners;
					$scope.designTypeView = [];
					$scope.insertionMannerView = [];

					$scope.generateDesignView = function() {

						// Add Breeding View Engine Designs except for for Custom Import Design
						$.each($scope.designTypes, function(index, designType){
							if(designType.name !== 'Custom Import Design'){
								$scope.designTypeView.push(designType);
							}
						});
					};

					$scope.generateInsertionMannerView = function() {
						$.each($scope.insertionManners, function(index, insertionManner){
							$scope.insertionMannerView.push(insertionManner);
						});
					};

					$scope.generateDesignView();
					$scope.generateInsertionMannerView();

					$scope.$watch(function() {
						return $scope.data.designType;
					}, function(newValue) {
						// If Design Type is Preset Design
						$scope.currentDesignType = TrialManagerDataService.getDesignTypeById(newValue, $scope.designTypes);
					});

					// TODO : re run computeLocalData after loading of previous study as template
					$scope.computeLocalData = function() {
						$scope.data.designType = TrialManagerDataService.currentData.experimentalDesign.designType;

						$scope.settings = TrialManagerDataService.specialSettings.experimentalDesign;
						$scope.settings.treatmentFactors = TrialManagerDataService.settings.treatmentFactors.details;

						// user has a treatment factor, if previous exp design is not RCBD, then set selection to RCBD
						// may need to clear non RCBD input
						if (TrialManagerDataService.settings.treatmentFactors.details.keys().length > 0) {
							$scope.data.designType = TrialManagerDataService.getDesignTypeById(DESIGN_TYPE.RANDOMIZED_COMPLETE_BLOCK, $scope.designTypes).id;
						}

						if ($scope.data.designType != null && $scope.data.designType !== '') {
							$scope.currentDesignType = TrialManagerDataService.getDesignTypeById($scope.data.designType, $scope.designTypes);

							if ($scope.currentDesignType.params !== null) {
								$scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;
							} else {
								$scope.currentParams = null;
							}

							if (!$scope.settings.showAdvancedOptions[$scope.data.designType]) {
								$scope.settings.showAdvancedOptions[$scope.data.designType] = $scope.data.useLatenized;
							}

							if ($scope.currentDesignType !== null && $scope.currentDesignType.name === 'Custom Import Design') {
								$http.get('/Fieldbook/DesignImport/getCustomImportDesignTypeDetails').then(function(result) {
									$scope.currentDesignType.templateName = result.data.templateName;
								});
							}
						}

						$scope.germplasmDescriptorSettings = TrialManagerDataService.settings.germplasm;
						$scope.measurementDetails = TrialManagerDataService.trialMeasurement;
						$scope.data.noOfEnvironments = TrialManagerDataService.currentData.environments.noOfEnvironments ?
							TrialManagerDataService.currentData.environments.noOfEnvironments : 0;
						$scope.data.treatmentFactors = TrialManagerDataService.settings.treatmentFactors.details;
						$scope.data.treatmentFactorsData = TrialManagerDataService.currentData.treatmentFactors.currentData;

						$scope.data.hasMeasurementData = TrialManagerDataService.trialMeasurement.hasMeasurement;

					};

					$scope.disableGenerateDesign = $scope.subObservationTabs.length > 0 || (TrialManagerDataService.trialMeasurement.hasMeasurement && !TrialManagerDataService.applicationData.unappliedChangesAvailable);

					//FIXME: cheating a bit for the meantime.
					var totalGermplasms = countGermplasms();
					if (!TrialManagerDataService.applicationData.germplasmListCleared) {
						$scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.experimentalDesign.
							germplasmTotalListCount = totalGermplasms ? totalGermplasms :
							TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount;
					} else {
						$scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.experimentalDesign.
							germplasmTotalListCount = totalGermplasms;
					}

					if (isNaN($scope.totalGermplasmEntryListCount)) {
						$scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.
							experimentalDesign.germplasmTotalListCount = 0;
					}

					$scope.data = TrialManagerDataService.currentData.experimentalDesign;

					// the property "startingEntryNo" is at least part of the data object here when the germplasm tab is loaded first
					if (!$scope.data || Object.keys($scope.data).length <= 1) {
						angular.copy({
							totalGermplasmListCount: $scope.totalGermplasmEntryListCount,
							designType: null,
							replicationsCount: null,
							replicationPercentage: null,
							blockSize: null,
							useLatenized: false,
							nblatin: null,
							replicationsArrangement: null,
							rowsPerReplications: null,
							colsPerReplications: null,
							nrlatin: null,
							nclatin: null,
							replatinGroups: '',
							startingPlotNo: 1,
							startingEntryNo: (typeof $scope.data.startingEntryNo !== 'undefined')? parseInt($scope.data.startingEntryNo,10) : 1 ,
							hasMeasurementData: TrialManagerDataService.trialMeasurement.hasMeasurement,
							numberOfBlocks: null
						}, $scope.data);
					}


					if ($scope.data.checkStartingPosition == null) {
						$scope.data.checkStartingPosition = 1;
						$scope.data.checkInsertionManner = '8414';
					}


					TrialManagerDataService.specialSettings.experimentalDesign.data = $scope.data;

					$scope.computeLocalData();

					$scope.replicationsArrangementGroupsOpts = {
						1: 'In a single column',
						2: 'In a single row',
						3: 'In adjacent columns'
					};
					
					$scope.disableDesignTypeSelect = ((TrialManagerDataService.trialMeasurement.hasMeasurement) || (TrialManagerDataService.trialMeasurement.count > 0 && TrialManagerDataService.applicationData.hasNewEnvironmentAdded));
					
					$scope.onSwitchDesignTypes = function(newId) {
						if (newId !== '') {

							$scope.currentDesignType = TrialManagerDataService.getDesignTypeById(newId, $scope.designTypes);
							$scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;
							$scope.data.designType = $scope.currentDesignType.id;
							TrialManagerDataService.currentData.experimentalDesign.designType = $scope.data.designType;
							$scope.applicationData.unappliedChangesAvailable = true;

							if (DESIGN_TYPE.ENTRY_LIST_ORDER === $scope.data.designType ) {
								$scope.refreshDesignDetailsForELODesign();
							} else if (DESIGN_TYPE.AUGMENTED_RANDOMIZED_BLOCK === $scope.data.designType ) {
								$scope.refreshDesignDetailsForAugmentedDesign();
							} else if (DESIGN_TYPE.P_REP === $scope.data.designType ) {
								$scope.refreshDesignDetailsForPRepDesign();
							}

						} else {
							$scope.currentDesignType = null;
							$scope.data.designType = '';
							$scope.currentParams = '';
						}

					};

					$scope.updateAfterGeneratingDesignSuccessfully = function() {
						//we show the preview
						showSuccessfulMessage('', $.experimentDesignMessages.experimentDesignGeneratedSuccessfully);
						TrialManagerDataService.clearUnappliedChangesFlag();
						TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
						$('#chooseGermplasmAndChecks').data('replace', '1');
						//if the design is generated but not saved, the measurements datatable is for preview only (edit is not allowed)
						$rootScope.$broadcast('previewMeasurements');
						$('body').addClass('preview-measurements-only');
					};

					// on click generate design button
					$scope.generateDesign = function() {

						if (!$scope.doValidate()) {
							return;
						}
                        TrialManagerDataService.performDataCleanup();
						var environmentData = angular.copy($scope.data);
						environmentData.startingEntryNo = TrialManagerDataService.currentData.experimentalDesign.startingEntryNo;

						// transform ordered has of treatment factors if existing to just the map
						if (environmentData && environmentData.treatmentFactors) {
							environmentData.treatmentFactors = $scope.data.treatmentFactors.vals();
						}

						TrialManagerDataService.generateExpDesign(environmentData).then(
							function(response) {
								if (response.valid === true) {
									if(response.message && response.message !== '') {
										if(response.userConfirmationRequired) {
											$scope.showConfirmDialog(response.message);
										} else {
											showSuccessfulMessage('', response.message);
										}
									}
									$scope.updateAfterGeneratingDesignSuccessfully();
								} else {
									if(response.message && response.message !== '') {
										if(response.userConfirmationRequired) {
											$scope.showConfirmDialog(response.message);
										} else {
											showErrorMessage('', response.message);
										}
									}
								}
							}, function(errResponse) {
								showErrorMessage($.fieldbookMessages.errorServerError, $.fieldbookMessages.errorDesignGenerationFailed);
							}
						);

					};

					$scope.showConfirmDialog = function(message) {

						var deferred = $q.defer();

						bootbox.dialog({
							message: message,
							closeButton: false,
							onEscape: false,
							buttons: {
								ok: {
									label: Messages.OK,
									className: 'btn-primary',
									callback: function() {
										deferred.resolve(true);
									}
								}
							}
						});

						return deferred.promise;
					};

					// Register designImportGenerated handler that will activate when an importDesign is generated
					$scope.$on('designImportGenerated', function() {
						var summaryPromise = $http.get('/Fieldbook/DesignImport/getMappingSummary');
						var designTypeDetailsPromise = $http.get('/Fieldbook/DesignImport/getCustomImportDesignTypeDetails');

						TrialManagerDataService.applicationData.unappliedChangesAvailable = false;

						$q.all([summaryPromise, designTypeDetailsPromise]).then(function(results) {
							$scope.applicationData.importDesignMappedData = results[0].data;
							$scope.currentDesignType.templateName = results[1].data.templateName;
						});
					});

					$scope.showConfirmResetDesign = function() {

						var deferred = $q.defer();

						bootbox.dialog({
							title: Messages.DESIGN_IMPORT_PRESET_DESIGN_CHANGE_DESIGN,
							message: Messages.DESIGN_IMPORT_CHANGE_DESIGN_DESCRIPTION_STUDY,
							closeButton: false,
							onEscape: false,
							buttons: {
								yes: {
									label: Messages.YES,
									className: 'btn-primary',
									callback: function() {
										deferred.resolve(true);
									}
								},
								no: {
									label: Messages.NO,
									className: 'btn-default',
									callback: function() {
										deferred.reject(false);
									}
								}
							}
						});

						return deferred.promise;
					};

					$scope.resetExperimentalDesign = function() {

						$scope.showConfirmResetDesign().then(function(result) {
							$scope.resetExperimentalDesignRelatedVariables();
						});
					};
					
					$scope.resetExperimentalDesignRelatedVariables = function() {
						// the following reset the data used for the experimental design, allowing the user to select another design again
						$scope.applicationData.isGeneratedOwnDesign = false;
						$scope.currentDesignType = null;
						$scope.applicationData.importDesignMappedData = null;
						$scope.data.designType = '';
						$scope.applicationData.unsavedGeneratedDesign = true;
					};
					
					$scope.$on('importedDesignReset', function() {
						$scope.resetExperimentalDesignRelatedVariables();
					});

					$scope.toggleDesignView = function() {
						var selectedDesignType = TrialManagerDataService.getDesignTypeById($scope.data.designType, $scope.designTypes);
						return !$scope.applicationData.unappliedChangesAvailable && ($scope.applicationData.isGeneratedOwnDesign
							|| ($scope.data.designType !== null
							&& $scope.data.designType !== ''
							&& selectedDesignType.name === 'Custom Import Design')
							);
					};

					$scope.isImportedDesign = function() {
						var selectedDesignType = TrialManagerDataService.getDesignTypeById($scope.data.designType, $scope.designTypes);
						return $scope.data.designType != null
							&& $scope.data.designType !== ''
							&& selectedDesignType.name === 'Custom Import Design';
					};

					$scope.isBVDesign = function() {
						var selectedDesignType = TrialManagerDataService.getDesignTypeById($scope.data.designType, $scope.designTypes);
						return $scope.data.designType != null
							&& $scope.data.designType !== ''
							&& selectedDesignType.name !== 'Custom Import Design';
					};
					
					$scope.showOrHideAdvancedOptions = function (isShown) {
						$scope.settings.showAdvancedOptions[$scope.currentDesignType.id] = isShown;
						$scope.data.useLatenized = isShown;
					};

					$scope.doValidate = function() {

						switch ($scope.currentDesignType.id) {
							case DESIGN_TYPE.RANDOMIZED_COMPLETE_BLOCK:
							{
								if (!$scope.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[4]);
									return false;
								}

								if (!$scope.settings.treatmentFactors || !TrialManagerDataService.currentData.treatmentFactors.currentData) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[18]);
									return false;
								}

								var isValidTreatmentVars = TrialManagerDataService.validateAllTreatmentFactorLabels({});
								if (!!isValidTreatmentVars && isValidTreatmentVars.hasError) {
									showErrorMessage(isValidTreatmentVars.customHeader, isValidTreatmentVars.customMessage);
									return false;
								}

								var errorCode = TrialManagerDataService.treatmentFactorDataInvalid();

								if (errorCode === 1) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[24]);
									return false;
								} else if (errorCode === 2) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[25]);
									return false;
								}

								break;
							}
							case DESIGN_TYPE.RESOLVABLE_INCOMPLETE_BLOCK:
							{

								if (!$scope.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[5]);
									return false;
								}

								if (!$scope.data.blockSize || $scope.expDesignForm.blockSize.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[8]);
									return false;
								}

								if ($scope.totalGermplasmEntryListCount % $scope.data.blockSize > 0) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[13]);
									return false;
								}

								// latinized
								if ($scope.data.useLatenized) {
									if ($scope.data.nblatin === null) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[27]);
										return false;
									}
									if (Number($scope.data.nblatin) >= ($scope.totalGermplasmEntryListCount / $scope.data.blockSize)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[11]);
										return false;
									}

									if (Number($scope.data.nblatin) >= Number($scope.data.replicationsCount)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[23]);
										return false;
									}

									if (Number($scope.data.replicationsArrangement) <= 0) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[21]);
										return false;

									}
									if (Number($scope.data.replicationsArrangement) === 3) {
										if (!$scope.data.replatinGroups || $scope.expDesignForm.replatinGroups.$invalid) {
											showErrorMessage('page-message', EXP_DESIGN_MSGS[22]);
											return false;
										}

										// validate sum of replatinGroups
										var sum = 0;
										var arrGroups = $scope.data.replatinGroups.split(',');

										for (var i = 0; i < arrGroups.length; i++) {
											sum += Number(arrGroups[i]);
										}

										if (sum !== Number($scope.data.replicationsCount)) {
											showErrorMessage('page-message', EXP_DESIGN_MSGS[12]);
											return false;
										}
									}
								}

								break;
							}
							case DESIGN_TYPE.ROW_COL:
							{
								if (!$scope.data.replicationsCount && $scope.expDesignForm.replicationsCount.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[5]);
									return false;
								}

								if ($scope.data.rowsPerReplications * $scope.data.colsPerReplications !== $scope.totalGermplasmEntryListCount) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[6]);
									return false;
								}

								if ($scope.data.useLatenized) {

									if (Number($scope.data.nrlatin) >= Number($scope.data.replicationsCount)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[15]);
										return false;
									}

									if (Number($scope.data.nclatin) >= Number($scope.data.replicationsCount)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[16]);
										return false;
									}

									if (Number($scope.data.nrlatin) <= 0 || Number($scope.data.nrlatin) >= Number($scope.data.rowsPerReplications)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[14]);
										return false;

									}

									if (Number($scope.data.nclatin) <= 0 || Number($scope.data.nclatin) >= Number($scope.data.colsPerReplications)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[17]);
										return false;

									}

									if (Number($scope.data.replicationsArrangement <= 0)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[21]);
										return false;
									}

									if (Number($scope.data.replicationsArrangement) === 3) {
										if (!$scope.data.replatinGroups || $scope.expDesignForm.replatinGroups.$invalid) {
											showErrorMessage('page-message', EXP_DESIGN_MSGS[22]);
											return false;
										}

										// validate sum of replatinGroups
										var _sum = 0;
										var _arrGroups = $scope.data.replatinGroups.split(',');

										for (var j = 0; j < _arrGroups.length; j++) {
											_sum += Number(_arrGroups[j]);
										}

										if (_sum !== Number($scope.data.replicationsCount)) {
											showErrorMessage('page-message', EXP_DESIGN_MSGS[12]);
											return false;
										}
									}

								}

								break;
							}
							case DESIGN_TYPE.AUGMENTED_RANDOMIZED_BLOCK: {

								if (!validateNumberOfBlocks()) {
									return false;
								}
								if (!$scope.checkIfTheNumberOfTestEntriesPerBlockIsWholeNumber()) {
									return false;
								}
								if (!validateNumberOfChecks()) {
									return false;
								}
								break;

							}
							case DESIGN_TYPE.ENTRY_LIST_ORDER: {

								if ($scope.germplasmTotalCheckEntriesCount > 0) {
									if ($scope.germplasmTotalTestEntriesCount === 0) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[33]);
										return false
									}
									if ($scope.data.checkStartingPosition > $scope.germplasmTotalTestEntriesCount) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[30]);
										return false;
									}
									if ($scope.data.checkStartingPosition < 1) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[32]);
										return false
									}
									if ($scope.data.checkSpacing > $scope.germplasmTotalTestEntriesCount) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[29]);
										return false
									}
									if ($scope.data.checkSpacing < 1) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[31]);
										return false
									}
								} else {
									$scope.data.checkSpacing = '';
								}
								break;
							}
							case DESIGN_TYPE.P_REP:  {

								if (!$scope.data.replicationPercentage || $scope.expDesignForm.replicationPercentage.$invalid) {
									showErrorMessage(MESSAGE_DIV_ID, '% of test entries to replicate should be an integer number ' +
										'greater or equal than 0 and less or equal than 100');
									return false;
								}

								if (!$scope.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
									showErrorMessage('page-message', 'Replication count should be greater than 1');
									return false;
								}

								if (!$scope.data.blockSize || $scope.expDesignForm.blockSize.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[8]);
								if ($scope.germplasmNumberOfPlotsPerBlock % 1 !== 0) {
									showErrorMessage(MESSAGE_DIV_ID, 'The block size must be a factor of the number of treatments.');
									return false;
								}

							}

						}

						if ($scope.totalGermplasmEntryListCount <= 0) {
							showErrorMessage('page-message', EXP_DESIGN_MSGS[26]);
							return false;
						}

						return true;
					};

					$scope.checkIfTheNumberOfTestEntriesPerBlockIsWholeNumber = function() {
						// Check if the Number of Test entries per block is a whole number
						if ($scope.germplasmNumberOfTestEntriesPerBlock % 1 !== 0) {
							showErrorMessage('page-message', 'The number of test entries must be divisible by number of blocks.');
							return false;
						}
						return true;
					};

					$scope.showOnlyIfNumberOfBlocksIsSpecified = function() {

						if ($scope.currentDesignType.id === DESIGN_TYPE.AUGMENTED_RANDOMIZED_BLOCK) {
							if (!$scope.data.numberOfBlocks && $scope.data.numberOfBlocks !== 0) {
								return false;
							}
							return true;
						}

					};

					$scope.showOnlyIfNumberOfBlockSizeIsSpecified = function() {

						if ($scope.currentDesignType.id === DESIGN_TYPE.P_REP) {
							if (!$scope.data.blockSize && $scope.data.blockSize !== 0) {
								return false;
							}
							return true;
						}

					};

					$scope.showOnlyIfNumberOfReplicationsCountIsSpecified = function() {

						if ($scope.currentDesignType.id === DESIGN_TYPE.P_REP) {
							if (!$scope.data.replicationsCount && $scope.data.replicationsCount !== 0) {
								return false;
							}
							return true;
						}
					};



					$scope.refreshDesignDetailsForAugmentedDesign = function() {

						$scope.germplasmTotalCheckEntriesCount = countCheckEntries();
						$scope.germplasmTotalTestEntriesCount = $scope.totalGermplasmEntryListCount - $scope.germplasmTotalCheckEntriesCount;
						$scope.germplasmNumberOfTestEntriesPerBlock = $scope.germplasmTotalTestEntriesCount / $scope.data.numberOfBlocks;
						$scope.germplasmNumberOfPlotsPerBlock = $scope.germplasmNumberOfTestEntriesPerBlock + $scope.germplasmTotalCheckEntriesCount;
						$scope.germplasmTotalNumberOfPlots = $scope.germplasmNumberOfPlotsPerBlock * $scope.data.numberOfBlocks;

					}

					$scope.refreshDesignDetailsForELODesign = function() {
						$scope.germplasmTotalTestEntriesCount = countNumberOfTestEntries();
						$scope.germplasmTotalCheckEntriesCount = $scope.totalGermplasmEntryListCount - $scope.germplasmTotalTestEntriesCount;
					}

					$scope.refreshDesignDetailsForPRepDesign = function() {
						$scope.germplasmTotalCheckEntriesCount = countCheckEntries();
						$scope.germplasmTotalTestEntriesCount = $scope.totalGermplasmEntryListCount - $scope.germplasmTotalCheckEntriesCount;
						var noOfTestEntriesToReplicate = Math.round($scope.germplasmTotalTestEntriesCount * ($scope.data.replicationPercentage / 100));
						$scope.germplasmTotalNumberOfPlots = (noOfTestEntriesToReplicate + $scope.germplasmTotalCheckEntriesCount) * $scope.data.replicationsCount;
						$scope.germplasmNumberOfPlotsPerBlock = $scope.germplasmTotalNumberOfPlots / $scope.data.blockSize;
					}


					function countNumberOfTestEntries() {
						var germplasmListDataTable = $('.germplasm-list-items').DataTable();

						if (germplasmListDataTable.rows().length !== 0) {

							var numberOfTestEntries = 0;

							$.each(germplasmListDataTable.rows().data(), function(index, obj) {
								var entryCheckType = parseInt(obj[ENTRY_TYPE_COLUMN_DATA_KEY]);
								if (entryCheckType === SYSTEM_DEFINED_ENTRY_TYPE.TEST_ENTRY) {
									numberOfTestEntries++;
								}
							});

							return numberOfTestEntries;

						} else if (TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalCheckCount != null) {
							// If the germplasmlistDataTable is not yet initialized, we should get the number of check entries of germplasm list in the database
							// when an existing study is opened / loaded, only if available. experimentalDesign.germplasmTotalCheckCount contains the count of checks stored in the database.
							return TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount -
								TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalCheckCount;
						}

						return 0;

					}

					function countCheckEntries() {

						// When the user changed the entry type of germplasm entries in Germplasm Tab, the changes are not yet saved in the database,
						// so we can only count the number of checks through DataTable.
						var germplasmListDataTable = $('.germplasm-list-items').DataTable();

						if (germplasmListDataTable.rows().length !== 0) {

							var numberOfChecksEntries = 0;

							$.each(germplasmListDataTable.rows().data(), function(index, obj) {
								if (parseInt(obj[ENTRY_TYPE_COLUMN_DATA_KEY]) === SYSTEM_DEFINED_ENTRY_TYPE.CHECK_ENTRY) {
									numberOfChecksEntries++;
								}
							});

							return numberOfChecksEntries;

						} else if (TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalCheckCount != null) {
							// If the germplasmlistDataTable is not yet initialized, we should get the number of check entries of germplasm list in the database
							// when an existing study is opened / loaded, only if available. experimentalDesign.germplasmTotalCheckCount contains the count of checks stored in the database.
							return TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalCheckCount;
						}

						return 0;

					}

					$scope.showParamsWhenChecksAreSelected = function(designTypeId) {
						return !(designTypeId === DESIGN_TYPE.ENTRY_LIST_ORDER &&
							($scope.germplasmTotalTestEntriesCount === $scope.totalGermplasmEntryListCount
								|| $scope.totalGermplasmEntryListCount === 0  || $scope.totalGermplasmEntryListCount === null));
					}

					function validateNumberOfBlocks() {
						if (!$scope.data.numberOfBlocks || $scope.expDesignForm.numberOfBlocks.$invalid) {
							showErrorMessage('page-message', 'Please specify the number of blocks.');
							return false;
						}
						return true;
					}

					function validateNumberOfChecks() {

						if ($scope.germplasmTotalCheckEntriesCount === 0) {
							showErrorMessage('page-message', 'Please specify checks in germplasm list before generating augmented design.');
							return false;
						}
						return true;
					}

				}])

			// FILTERS USED FOR EXP DESIGN

			.filter('filterFactors', ['_','DESIGN_TYPE', function(_, DESIGN_TYPE) {
				return function(factorList, designTypeId) {
					var excludeTermIds;

					if (designTypeId === DESIGN_TYPE.RANDOMIZED_COMPLETE_BLOCK) {
						excludeTermIds = [8230, 8220, 8581, 8582, 8842];
					} else if (designTypeId === DESIGN_TYPE.RESOLVABLE_INCOMPLETE_BLOCK) {
						excludeTermIds = [8581, 8582, 8842];
					} else if (designTypeId === DESIGN_TYPE.ROW_COL) {
						excludeTermIds = [8220, 8200, 8842];
					} else if (designTypeId === DESIGN_TYPE.AUGMENTED_RANDOMIZED_BLOCK) {
						excludeTermIds = [8210, 8581, 8582, 8842];
					} else if (designTypeId === DESIGN_TYPE.ENTRY_LIST_ORDER) {
						excludeTermIds = [8210, 8220, 8581, 8582, 8842];
					} else if (designTypeId === DESIGN_TYPE.P_REP) {
						excludeTermIds = [8210, 8581, 8582, 8842];
					}

					return _.filter(factorList, function(value) {
							return !_.contains(excludeTermIds, value);
					});


				};
			}]);

	}
)();
