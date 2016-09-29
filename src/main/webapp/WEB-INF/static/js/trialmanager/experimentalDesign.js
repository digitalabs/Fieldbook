/* global angular, showErrorMessage, showAlertMessage, showSuccessfulMessage, expDesignMsgs */

(function() {
		'use strict';

		angular.module('manageTrialApp')
			.constant('EXP_DESIGN_MSGS', expDesignMsgs)
			.constant('EXPERIMENTAL_DESIGN_PARTIALS_LOC', '/Fieldbook/static/angular-templates/experimentalDesignPartials/')
			.controller('ExperimentalDesignCtrl', ['$scope', '$state', 'EXPERIMENTAL_DESIGN_PARTIALS_LOC', 'TrialManagerDataService', '$http',
				'EXP_DESIGN_MSGS', '_', '$q', 'Messages', function($scope, $state, EXPERIMENTAL_DESIGN_PARTIALS_LOC, TrialManagerDataService, $http, EXP_DESIGN_MSGS, _, $q, Messages) {

					$scope.applicationData = TrialManagerDataService.applicationData;
					$scope.studyID = TrialManagerDataService.currentData.basicDetails.studyID;

					$scope.designTypes = TrialManagerDataService.applicationData.designTypes;
					$scope.designTypeView = [];

					$scope.generateDesignView = function() {

						// Add Breeding View Engine Designs except for for Custom Import Design
						$.each($scope.designTypes, function(index, designType){
							if(!designType.isPreset && designType.name !== 'Custom Import Design'){
								$scope.designTypeView.push(designType);
							}
						});

						// Add separator only if there are preset designs.
						if($scope.isTherePresetDesign($scope.designTypes)){
							$scope.designTypeView.push({id:	null, name: '----------------------------------------------', isDisabled: true });
						}

						// Add preset designs.
						$.each($scope.designTypes, function(index, designType){
							if(designType.isPreset && designType.name !== 'Custom Import Design'){
								$scope.designTypeView.push(designType);
							}
						});
					};

					$scope.isTherePresetDesign = function(designTypes) {
						var presetExists = false;
						$.each(designTypes, function(index, designType){
							if(designType.isPreset){
								presetExists = true;
							}
						});
						return presetExists;
					};

					$scope.generateDesignView();

					$scope.$watch(function() {
						return $scope.data.designType;
					}, function(newValue) {
						// If Design Type is Preset Design
						$scope.currentDesignType = TrialManagerDataService.getDesignTypeById(newValue, $scope.designTypes);
					});

					// TODO : re run computeLocalData after loading of previous trial as template
					$scope.computeLocalData = function() {
						$scope.data.designType = TrialManagerDataService.currentData.experimentalDesign.designType;

						$scope.settings = TrialManagerDataService.specialSettings.experimentalDesign;
						$scope.settings.treatmentFactors = TrialManagerDataService.settings.treatmentFactors.details;

						// user has a treatment factor, if previous exp design is not RCBD, then set selection to RCBD
						// may need to clear non RCBD input
						if (TrialManagerDataService.settings.treatmentFactors.details.keys().length > 0) {
							$scope.data.designType = TrialManagerDataService.getDesignTypeById(0, $scope.designTypes).id;
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

							// loading for existing trial
							if($scope.studyID != null && !TrialManagerDataService.applicationData.unappliedChangesAvailable){
								var selectedDesignType = TrialManagerDataService.getDesignTypeById($scope.data.designType, $scope.designTypes);
								$scope.applicationData.hasGeneratedDesignPreset = selectedDesignType.isPreset
									&& $scope.studyID != null && TrialManagerDataService.trialMeasurement.count > 0;
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

					$scope.disableGenerateDesign = TrialManagerDataService.trialMeasurement.hasMeasurement && !TrialManagerDataService.applicationData.unappliedChangesAvailable;

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

					TrialManagerDataService.specialSettings.experimentalDesign.data = $scope.data;

					$scope.computeLocalData();

					$scope.replicationsArrangementGroupsOpts = {
						1: 'In a single column',
						2: 'In a single row',
						3: 'In adjacent columns'
					};
					$scope.onSwitchDesignTypes = function(newId) {
						if (newId !== '') {

							$scope.currentDesignType = TrialManagerDataService.getDesignTypeById(newId, $scope.designTypes);
							$scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;
							$scope.data.designType = $scope.currentDesignType.id;
							TrialManagerDataService.currentData.experimentalDesign.designType = $scope.data.designType;
							$scope.applicationData.unappliedChangesAvailable = true;

							if ($scope.currentDesignType.isPreset) {
								showAlertMessage('', ImportDesign.getMessages().OWN_DESIGN_SELECT_WARNING, 5000);
							}
						} else {
							$scope.currentDesignType = null;
							$scope.data.designType = '';
							$scope.currentParams = '';
						}

						$scope.applicationData.hasGeneratedDesignPreset = false;
					};

					$scope.toggleIsPresetWithGeneratedDesign = function() {
						var selectedDesignType = TrialManagerDataService.getDesignTypeById($scope.data.designType, $scope.designTypes);
						$scope.applicationData.hasGeneratedDesignPreset =  $scope.applicationData.unsavedGeneratedDesign && selectedDesignType.isPreset;
					};

					$scope.updateAfterGeneratingDesignSuccessfully = function() {
						//we show the preview
						showSuccessfulMessage('', $.experimentDesignMessages.experimentDesignGeneratedSuccessfully);
						TrialManagerDataService.clearUnappliedChangesFlag();
						TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
						$('#chooseGermplasmAndChecks').data('replace', '1');
						$('body').data('expDesignShowPreview', '1');
						$scope.toggleIsPresetWithGeneratedDesign();
					};

					// on click generate design button
					$scope.generateDesign = function() {
						if (!$scope.doValidate()) {
							return;
						}

						var environmentData = angular.copy($scope.data);
						environmentData.startingEntryNo = TrialManagerDataService.currentData.experimentalDesign.startingEntryNo;

						// transform ordered has of treatment factors if existing to just the map
						if (environmentData && environmentData.treatmentFactors) {
							environmentData.treatmentFactors = $scope.data.treatmentFactors.vals();
						}
						
						// non-preset design type
						var selectedDesignType = TrialManagerDataService.getDesignTypeById($scope.data.designType, $scope.designTypes);
						if (!selectedDesignType.isPreset) {
							TrialManagerDataService.generateExpDesign(environmentData).then(
								function(response) {
									if (response.valid === true) {
										$scope.updateAfterGeneratingDesignSuccessfully();
									} else {
										showErrorMessage('', response.message);
									}
								}, function(errResponse) {
                                    showErrorMessage($.fieldbookMessages.errorServerError, $.fieldbookMessages.errorDesignGenerationFailed);
                                }
							);
						} else {
							TrialManagerDataService.generatePresetExpDesign($scope.data.designType).then(function() {
								$scope.updateAfterGeneratingDesignSuccessfully();
							}, function(data) {
								showErrorMessage('', data.error[0]);
							});
						}
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
							message: Messages.DESIGN_IMPORT_CHANGE_DESIGN_DESCRIPTION_TRIAL,
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
							// the following reset the data used for the experimental design, allowing the user to select another design again
							$scope.applicationData.hasGeneratedDesignPreset = false;
							$scope.applicationData.isGeneratedOwnDesign = false;
							$scope.currentDesignType = null;
							$scope.data.designType = '';

							// the following prevents the user from saving before re-generating the design, to avoid having invalid measurement data
							if (TrialManagerDataService.trialMeasurement.count > 0) {
								TrialManagerDataService.applicationData.unappliedChangesAvailable = true;
							}
						});
					};

					$scope.toggleDesignView = function() {
						var selectedDesignType = TrialManagerDataService.getDesignTypeById($scope.data.designType, $scope.designTypes);
						return !$scope.applicationData.unappliedChangesAvailable && ($scope.applicationData.isGeneratedOwnDesign
							|| ($scope.data.designType != null
							&& $scope.data.designType !== ''
							&& selectedDesignType.name === 'Custom Import Design')
							|| $scope.applicationData.hasGeneratedDesignPreset);
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
							&& !selectedDesignType.isPreset && selectedDesignType.name !== 'Custom Import Design';
					};

					$scope.doValidate = function() {

						// FIXME: Find a way to detect the design type by not using hard coded design ids, if the design type id changed in the backend, this will break.

						switch ($scope.currentDesignType.id) {
							case 0:
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
							case 1:
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
							case 2:
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
							case 4: {

								if (!$scope.data.numberOfBlocks || $scope.expDesignForm.numberOfBlocks.$invalid) {
									showErrorMessage('page-message', 'Please specify the number of blocks.');
									return false;
								}

								if ($scope.totalGermplasmEntryListCount % $scope.data.numberOfBlocks !== 0) {
									showAlertMessage('page-message', 'The entries in this trial cannot be divided into evenly sized blocks. Augmented designs are most efficient when block sizes are constant.', 10000);
								}

								break;

							}
							case 5:
							case 6:
							{
								var actualNoOfGermplasmListEntries = $scope.currentDesignType.totalNoOfEntries;
								if ($scope.totalGermplasmEntryListCount > 0 && $scope.totalGermplasmEntryListCount !== actualNoOfGermplasmListEntries) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[28]);
									return false;
								}
								break;
							}

						}

						if ($scope.totalGermplasmEntryListCount <= 0) {
							showErrorMessage('page-message', EXP_DESIGN_MSGS[26]);
							return false;
						}

						return true;
					};

				}])

			// FILTERS USED FOR EXP DESIGN

			.filter('filterFactors', ['_', function(_) {
				return function(factorList, designTypeId) {

					var excludeTermIds;

					// FIXME: Find a way to detect the design type by not using hard coded design ids, if the design type id changed in the backend, this will break.
					if (designTypeId === 0) {
						excludeTermIds = [8230, 8220, 8581, 8582];
					} else if (designTypeId === 1) {
						excludeTermIds = [8581, 8582];
					} else if (designTypeId === 2) {
						excludeTermIds = [8220, 8200];
					} else if (designTypeId === 4) {
						excludeTermIds = [8210, 8581, 8582];
					}

					return _.filter(factorList, function(value) {
							return !_.contains(excludeTermIds, value);
					});


				};
			}]);

	}
)();
