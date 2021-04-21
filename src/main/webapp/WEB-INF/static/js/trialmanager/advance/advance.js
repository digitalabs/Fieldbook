(function () {
	'use strict';

	const manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('advanceStudyModalService', ['$uibModal',
		function ($uibModal) {

			var advanceStudyModalService = {};

			advanceStudyModalService.openSelectEnvironmentModal = function (advanceType) {
				$uibModal.open({
					templateUrl: '/Fieldbook/StudyManager/advance/study/selectEnvironmentModal',
					//templateUrl: '/Fieldbook/static/angular-templates/advance/selectEnvironmentModal.html',
					controller: "selectEnvironmentModalCtrl",
					size: 'md',
					resolve: {
						advanceType: function () {
							return advanceType;
						}
					}
				});
			};

			advanceStudyModalService.openAdvanceStudyModal = function (studyId, trialInstances, noOfReplications, locationsSelected, advanceType) {

				var advanceStudyURL = '/Fieldbook/StudyManager/advance/study';
				advanceStudyURL = advanceStudyURL + '/' + encodeURIComponent(studyId);
				advanceStudyURL = advanceStudyURL + '?selectedInstances=' + encodeURIComponent(trialInstances.join(","));
				if (noOfReplications) {
					advanceStudyURL = advanceStudyURL + '&noOfReplications=' + encodeURIComponent(noOfReplications);
				}
				if (advanceType) {
					advanceStudyURL = advanceStudyURL + '&advanceType=' + encodeURIComponent(advanceType);
				}

				$uibModal.open({
					templateUrl: advanceStudyURL,
					controller: "advanceStudyModalController",
					size: 'lg',
					resolve: {
						advanceType: function () {
							return advanceType;
						},
						locationsSelected: function () {
							return locationsSelected;
						}
					}
				});
			};

			return advanceStudyModalService;

		}
	]);

	manageTrialApp.controller('advanceStudyModalController', ['$scope', '$uibModalInstance', 'advanceType', 'advanceStudyModalService', 'locationsSelected',
		function ($scope, $uibModalInstance, advanceType, advanceStudyModalService, locationsSelected) {


			$scope.valueContainer = {
				selectedBreedingMethod: null,
				methodChoice: true,
				lineChoice: true,
				allPlotsChoice: true,
				linesValue: 1,
				methodVariateId: 0,
				plotVariateId: 0,
				lineVariateId: 0
			};

			$scope.locationsSelected = locationsSelected;
			$scope.advanceType = advanceType;

			$scope.isBulkingMethod = function () {
				return $scope.valueContainer.selectedBreedingMethod && $scope.valueContainer.selectedBreedingMethod.bulkingMethod;
			};

			$scope.methodChanged = function (item, model) {
				$scope.valueContainer.selectedBreedingMethod = item;
			};

			$scope.advanceStudy = function () {
				var lines = $scope.valueContainer.linesValue + '';

				var repsSectionIsDisplayed = $('#reps-section').length;
				if (repsSectionIsDisplayed) {
					var selectedReps = [];
					$('#replications input:checked').each(function () {
						selectedReps.push($(this).val());
					});

					if (selectedReps.length == 0) {
						showErrorMessage('page-advance-modal-message', noReplicationSelectedError);
						return false;
					}
				}

				if (!$scope.valueContainer.selectedBreedingMethod) {
					showErrorMessage('page-advance-modal-message', msgMethodError);
					return false;
				} else if (lines && !lines.match(/^\s*(\+|-)?\d+\s*$/)) {
					showErrorMessage('page-advance-modal-message', linesNotWholeNumberError);
					return false;
				} else if ($scope.validatePlantsSelected()) {
					SaveAdvanceList.doAdvanceStudy()
					$scope.close();
				}
			};

			$scope.validatePlantsSelected = function () {
				var ids = '', isMixed = false,
					valid;

				if ($scope.isBulkingMethod()) {
					if (!$scope.valueContainer.allPlotsChoice) {
						ids = ids + $scope.valueContainer.plotVariateId;
					}
				}
				if (!$scope.isBulkingMethod()) {
					if (!$scope.valueContainer.lineChoice) {
						if (ids !== '') {
							ids = ids + ',';
						}
						ids = ids + $scope.valueContainer.lineVariateId;
					}
				}

				valid = true;
				if (valid) {
					valid = $scope.checkBreedingMethodVariate();
				}
				if (valid && ids !== '') {
					$.ajax({
						url: '/Fieldbook/StudyManager/advance/study/countPlots/' + ids,
						type: 'GET',
						cache: false,
						async: false,
						success: function (data) {
							var choice,
								lineSameForAll;

							if (isMixed) {
								if (data == 0) {
									var param = $scope.valueContainer.lineVariateId + ' and/or ' + $scope.valueContainer.plotVariateId;
									var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), param);
									showErrorMessage('page-advance-modal-message', newMessage);
									valid = false;
								}
							} else if ($scope.isBulkingMethod()) {
								if (data == '0') {
									var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), $scope.valueContainer.plotVariateId);
									showErrorMessage('page-advance-modal-message', newMessage);
									valid = false;
								}
							} else {
								if (!$scope.valueContainer.lineChoice && data == '0') {
									var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), $scope.valueContainer.lineVariateId);
									showErrorMessage('page-advance-modal-message', newMessage);
									valid = false;
								}
							}
						}.bind(this),
						error: function (jqXHR, textStatus, errorThrown) {
							console.log('The following error occured: ' + textStatus, errorThrown);
						}
					});
				}
				if (valid && isMixed) {
					return valid;
				}
				return valid;
			};

			$scope.checkBreedingMethodVariate = function () {
				var valid = true;

				if (!$scope.valueContainer.methodChoice && $scope.valueContainer.methodVariateId) {
					// TODO: Use $http
					$.ajax({
						url: '/Fieldbook/StudyManager/advance/study/countPlots/' + $scope.valueContainer.methodVariateId,
						type: 'GET',
						cache: false,
						async: false,
						success: function (data) {

							if (data == 0) {
								var newMessage = noMethodValueErrorTrial.replace(new RegExp(/\{0\}/g), $scope.valueContainer.methodVariateId);
								showErrorMessage('page-advance-modal-message', newMessage);

								valid = false;
							} else {
								valid = validateBreedingMethodValues(id);
							}

						}.bind(this),
						error: function (jqXHR, textStatus, errorThrown) {
							console.log('The following error occured: ' + textStatus, errorThrown);
						}
					});
				}
				return valid;
			};

			$scope.back = function () {
				advanceStudyModalService.openSelectEnvironmentModal(advanceType);
				$scope.close();
			};

			$scope.close = function () {
				$uibModalInstance.close();
			};

			$scope.validateMethodChoice = function () {
				// FIXME: Find a way to check to detect if there are available nethod variates that do not use jquery.
				if ($('#hasMethodVariates').val() === 'false') {
					$scope.valueContainer.methodChoice = true;
					showErrorMessage('', noMethodVariatesErrorTrial);
				}
			};

			$scope.validateLineChoice = function () {
				// FIXME: Find a way to check to detect if there are available line variates that do not use jquery.
				if ($('#hasLineVariates').val() === 'false') {
					$scope.valueContainer.lineChoice = true;
					showErrorMessage('', noLineVariatesErrorTrial);
				}
			};

			$scope.validatePlotChoice = function () {
				// FIXME: Find a way to check to detect if there are available plot variates that do not use jquery.
				if ($('#hasLineVariates').val() === 'false') {
					$scope.valueContainer.allPlotsChoice = true;
					showErrorMessage('', noLineVariatesErrorTrial);
				}
			};

			$scope.init = function () {
				checkMethod();
				lineMethod();
				plotMethod();
				changeSuffix();
			};
		}
	]);

	manageTrialApp.controller('selectEnvironmentModalCtrl', ['$scope', '$uibModalInstance', 'TrialManagerDataService', 'studyInstanceService',
		'$timeout', 'studyContext', 'datasetService', 'advanceStudyModalService', 'advanceType',
		function ($scope, $uibModalInstance, TrialManagerDataService, studyInstanceService, $timeout, studyContext, datasetService,
				  advanceStudyModalService, advanceType) {

			$scope.settings = TrialManagerDataService.settings.environments;
			if (Object.keys($scope.settings).length === 0) {
				$scope.settings = {};
				$scope.settings.managementDetails = [];
				$scope.settings.trialConditionDetails = [];
			}

			$scope.TRIAL_LOCATION_NAME_INDEX = 8180;
			$scope.TRIAL_LOCATION_ABBR_INDEX = 8189;
			$scope.LOCATION_NAME_ID = 8190;
			$scope.applicationData = TrialManagerDataService.applicationData;
			$scope.instanceInfo = studyInstanceService.instanceInfo;

			$scope.applicationData.advanceType = advanceType;

			$scope.$on('changeEnvironments', function () {
				$scope.instanceInfo = studyInstanceService.instanceInfo;

				//create a map for location dropdown values
				var locationMap = {};
				angular.forEach($scope.settings.managementDetails.vals()[$scope.LOCATION_NAME_ID].allValues, function (locationVariable) {
					locationMap[locationVariable.id] = locationVariable;
				});

				angular.forEach($scope.instanceInfo.instances, function (instance) {
					if (locationMap[instance.managementDetailValues[$scope.LOCATION_NAME_ID]]) {

						// Ensure that the location id and location name details of the $scope.instanceInfo.instances
						// are updated with values from Location json object
						instance.managementDetailValues[$scope.LOCATION_NAME_ID]
							= locationMap[instance.managementDetailValues[$scope.LOCATION_NAME_ID]].id;
						instance.managementDetailValues[$scope.TRIAL_LOCATION_NAME_INDEX]
							= locationMap[instance.managementDetailValues[$scope.LOCATION_NAME_ID]].name;
					}
				});
			});

			$scope.noOfReplications = TrialManagerDataService.currentData.experimentalDesign.replicationsCount;

			$scope.instances = [];
			$scope.selectedInstances = {};
			$scope.isEmptySelection = false;

			//NOTE: Continue action for navigate from Locations to Advance Study Modal
			$scope.selectInstanceContinue = function () {

				// Do not go ahead for Advancing unless study has experimental design & number of replications variables
				if (TrialManagerDataService.currentData.experimentalDesign.designType === null) {
					showAlertMessage('', $.fieldbookMessages.advanceListUnableToGenerateWarningMessage);
					return;
				}

				var selectedTrialInstances = [];
				var selectedLocationDetails = [];
				var locationAbbr = false;

				if ($scope.isEmptySelection) {
					showErrorMessage('', $.fieldbookMessages.errorNotSelectedInstance);
				} else {
					if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX)) {
						selectedLocationDetails
							.push($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX).variable.name);
						locationAbbr = true;
					} else {
						selectedLocationDetails
							.push($scope.settings.managementDetails.val($scope.LOCATION_NAME_ID).variable.name);
					}

					angular.forEach($scope.instances, function (instance) {
						var isSelected = $scope.selectedInstances[instance.instanceNumber];
						if (isSelected) {
							selectedTrialInstances.push(instance.instanceNumber);
							if (locationAbbr) {
								selectedLocationDetails.push(instance.customLocationAbbreviation);
							} else {
								selectedLocationDetails.push(instance.locationName);
							}
						}
					});

					advanceStudyModalService.openAdvanceStudyModal(studyContext.studyId, selectedTrialInstances, $scope.noOfReplications, selectedLocationDetails,
						$scope.applicationData.advanceType);
					$uibModalInstance.close();
				}

			};

			$scope.close = function () {
				$uibModalInstance.close();
			}

			$scope.init = function () {
				datasetService.getDatasetInstances(studyContext.measurementDatasetId).then(function (datasetInstances) {
					$scope.instances = datasetInstances;
				});
			};

			$scope.init();
		}]);

})();
