(function () {
	'use strict';

	var datasetsApiModule = angular.module('datasets-api', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	datasetsApiModule.factory('datasetService', ['$http', '$q', 'studyContext', 'serviceUtilities', 'DATASET_TYPES', 'DATASET_TYPES_OBSERVATION_IDS',
		function ($http, $q, studyContext, serviceUtilities, DATASET_TYPES, DATASET_TYPES_OBSERVATION_IDS) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/studies/';

			var datasetTypeMap = {};
			var datasetTypes = [{
				id: DATASET_TYPES.PLANT_SUBOBSERVATIONS,
				tabTitlePrefix: 'Plants: ',
				tabNamePrefix: 'Plants: '
			}, {
				id: DATASET_TYPES.QUADRAT_SUBOBSERVATIONS,
				tabTitlePrefix: 'Quadrats: ',
				tabNamePrefix: 'Quadrats: '
			}, {
				id: DATASET_TYPES.TIME_SERIES_SUBOBSERVATIONS,
				tabTitlePrefix: 'Time Series: ',
				tabNamePrefix: 'Time Series: '
			}, {
				id: DATASET_TYPES.CUSTOM_SUBOBSERVATIONS,
				tabTitlePrefix: 'Sub-Observation Units: ',
				tabNamePrefix: 'SOUs: '
			}, {
				id: DATASET_TYPES.PLOT_OBSERVATIONS,
				tabTitlePrefix: '',
				tabNamePrefix: ''
			}];

			angular.forEach(datasetTypes, function (datasetType) {
				datasetTypeMap[datasetType.id] = datasetType;
			});


			var datasetService = {};
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			datasetService.observationCount = function (datasetId, variableIds) {

				if (datasetId && variableIds) {
					return $http.head(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/variables/observations?variableIds=' + variableIds.join(','));
				}

				return $q.reject('studyId, datasetId and variableIds are not defined.');
			};

			datasetService.observationCountByInstance = function (datasetId, instanceId) {

				if (datasetId && instanceId) {
					return $http.head(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' + instanceId);
				}

				return $q.reject('instanceId and datasetId are not defined.');
			};

			datasetService.addObservation = function (datasetId, observationUnitId, observation) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' + observationUnitId + '/observations/'
					, observation);
				return request.then(successHandler, failureHandler);
			};

			datasetService.updateObservation = function (datasetId, observationUnitId, observationId, observationValue) {
				var request = $http.patch(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' +
					observationUnitId + '/observations/' + observationId, observationValue);
				return request.then(successHandler, failureHandler);
			};

			datasetService.deleteObservation = function (datasetId, observationUnitId, observationId) {
				var request = $http.delete(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' +
					observationUnitId + '/observations/' + observationId);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDatasets = function (datasetTypeIds) {
				if (!studyContext.studyId) {
					return $q.resolve([]);
				}
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets', angular.merge({
					params: {
						datasetTypeIds: datasetTypeIds || DATASET_TYPES_OBSERVATION_IDS.join(",")
					}
				}));
				return request.then(successHandler, failureHandler);
			};

			datasetService.getColumns = function (datasetId, draftMode) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/table/columns', {
					params: {
						draftMode: Boolean(draftMode)
					}
				});
				return request.then(successHandler, failureHandler);
			};

			datasetService.getObservationTableUrl = function (datasetId) {
				return BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/table';
			};

			datasetService.generation = function (newDataset) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + studyContext.measurementDatasetId + '/generation', newDataset);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDataset = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId);
				return request.then(successHandler, failureHandler);
			};

			datasetService.addVariables = function (datasetId, newVariable) {
				var request = $http.put(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/variables', newVariable);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getVariables = function (datasetId, variableTypeId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/variables/' + variableTypeId);
				return request.then(successHandler, failureHandler);
			};

			datasetService.removeVariables = function (datasetId, variableIds) {
				var request = $http.delete(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/variables?', {
					params: {
						variableIds: variableIds.join(",")
					}
				});
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDatasetInstances = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/instances');
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDatasetType = function (datasetTypeId) {
				return datasetTypeMap[datasetTypeId];

			};

			datasetService.exportDataset = function (datasetId, instanceIds, collectionOrderId, singleFile, fileFormat) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/' + fileFormat , {
						params: {
							instanceIds: instanceIds.join(","),
							collectionOrderId: collectionOrderId,
							singleFile: singleFile
						},
						responseType: 'blob'
					});

				return request.then(function (response) {
					return response;
				}, failureHandler);
			};

			datasetService.importObservations = function (datasetId, observationList, processWarnings) {
				if (!studyContext.studyId) {
					return $q.resolve([]);
				}
				var request = $http.put(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/observations',
					{
						processWarnings: processWarnings,
						data: observationList,
                        draftMode: true
					});
				return request.then(successHandler, failureHandler);
			};

			datasetService.acceptDraftData = function (datasetId) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/acceptance');
				return request.then(successHandler, failureHandler);

			};

			datasetService.checkOutOfBoundDraftData = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/out-of-bounds');
				return request.then(successHandler, failureHandler);

			};

			datasetService.rejectDraftData = function (datasetId) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/rejection');
				return request.then(successHandler, failureHandler);

			};

			datasetService.setAsMissingDraftData = function (datasetId) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/set-as-missing');
				return request.then(successHandler, failureHandler);

			};

			datasetService.countFilteredPhenotypesAndInstances = function (datasetId, observationUnitsSearch) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/observations/filter/count', observationUnitsSearch);
				return request.then(successHandler, failureHandler);

			};

			datasetService.acceptDraftDataByVariable = function (datasetId, observationUnitsSearch) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/filter/acceptance', observationUnitsSearch);
				return request.then(successHandler, failureHandler);

			};

			datasetService.setValueToVariable = function (datasetId, observationUnitsSearch) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/filter/set-value', observationUnitsSearch);
				return request.then(successHandler, failureHandler);

			};

			datasetService.getAllVariables = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/variables');
				return request.then(successHandler, failureHandler);
			};

			return datasetService;

		}]);


	datasetsApiModule.factory('experimentDesignService', ['$http', '$q', 'studyContext', 'serviceUtilities', '$uibModal',
		function ($http, $q, studyContext, serviceUtilities, $uibModal) {

			var BASE_CROP_URL = '/bmsapi/crops/' + studyContext.cropName;
			var BASE_STUDY_URL = BASE_CROP_URL  +  '/studies/';

			var experimentDesignService = {};
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			experimentDesignService.openSelectEnvironmentToGenerateModal = function (experimentDesignInput) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/generateDesign/selectEnvironmentModal.html',
					controller: "generateDesignCtrl",
					size: 'md',
					resolve: {
						experimentDesignInput: function () {
							return experimentDesignInput;
						}
					},
					controllerAs: 'ctrl'
				});
			};

			experimentDesignService.generateDesign = function (experimentDesignInput) {
				var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/experimental-designs/generation', experimentDesignInput);
				return request.then(successHandler, failureHandler);
			}

			experimentDesignService.deleteDesign = function () {
				var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/experimental-designs');
				return request.then(successHandler, failureHandler);
			}

            experimentDesignService.getBVDesignLicense = function () {
               return $http.get('/bmsapi/breeding-view-licenses');
            }

			experimentDesignService.getDesignTypes = function () {
				var request = $http.get(BASE_CROP_URL + '/experimental-design-types');
				return request.then(successHandler, failureHandler);
			}

			experimentDesignService.getInsertionManners = function () {
				var request = $http.get(BASE_CROP_URL + '/check-insertion-manners');
				return request.then(successHandler, failureHandler);
			}

			return experimentDesignService;

		}]);

	datasetsApiModule.controller('generateDesignCtrl', ['experimentDesignInput', '$scope', '$rootScope', '$uibModalInstance',
		'environmentService', 'experimentDesignService', 'studyContext',
		function (experimentDesignInput, $scope, $rootScope, $uibModalInstance, environmentService, experimentDesignService, studyContext) {

			var generateDesignCtrl = this;

			$scope.instances = [];
			$scope.selectedInstances = {};
			$scope.isEmptySelection = false;

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.validateSelectedEnvironments = function () {
				experimentDesignInput.trialInstancesForDesignGeneration = generateDesignCtrl.getSelectedInstanceNumbers();

				var environmentsWithMeasurements = [];
				//Check if selected instances has measurement data
				$scope.instances.forEach(function (instance) {
					if($scope.selectedInstances[instance['instanceNumber']] && (instance['hasMeasurements'] || instance['hasFieldmap'])) {
						environmentsWithMeasurements.push(instance['instanceNumber']);
					}
				});
				if(environmentsWithMeasurements.length > 0 ) {
					generateDesignCtrl.showHasMeasurementsWarning(environmentsWithMeasurements);
				} else {
					var environmentsWithDesign = [];
					//Check if selected instances has measurement data
					$scope.instances.forEach(function (instance) {
						if($scope.selectedInstances[instance['instanceNumber']] && instance['hasExperimentalDesign']) {
							environmentsWithDesign.push(instance['instanceNumber']);
						}
					});
					if(environmentsWithDesign.length > 0) {
						generateDesignCtrl.showHasGeneratedDesignWarning(environmentsWithDesign);
					} else {
						generateDesignCtrl.generateDesign();
					}
				}
			};

			generateDesignCtrl.generateDesign = function() {
				experimentDesignService.generateDesign(experimentDesignInput).then(
					function(response) {
						showSuccessfulMessage('', $.experimentDesignMessages.experimentDesignGeneratedSuccessfully);
						window.location = '/Fieldbook/TrialManager/openTrial/' + studyContext.studyId;
					}, function(errResponse) {
						var errorMessage = errResponse.errors[0].message;
						showErrorMessage($.fieldbookMessages.errorServerError, $.fieldbookMessages.errorDesignGenerationFailed + ' ' + errorMessage);
					});
			}

			generateDesignCtrl.getSelectedInstanceNumbers = function () {

				var instanceNumbers = [];

				Object.keys($scope.selectedInstances).forEach(function (instanceNumber) {
					var isSelected = $scope.selectedInstances[instanceNumber];
					if (isSelected) {
						instanceNumbers.push(instanceNumber);
					}
				});

				return instanceNumbers;

			};

			generateDesignCtrl.showHasMeasurementsWarning = function(environmentsWithMeasurements) {
				var deleteMessage = "Measurements and fieldmaps of the following environment(s): " + environmentsWithMeasurements.toString()
						.replace(new RegExp(",", 'g'), ", ") + " will be deleted. Do you want to continue?";
				var modalConfirmDelete = $rootScope.openConfirmModal(deleteMessage, 'Yes','No');
				modalConfirmDelete.result.then(function (shouldContinue) {
					if (shouldContinue) {
						generateDesignCtrl.generateDesign();
					} else {
						$scope.cancel();
					}
				});
			};

			generateDesignCtrl.showHasGeneratedDesignWarning = function(environmentsWithDesign) {
				var deleteMessage = "The following environment(s): " + environmentsWithDesign.toString()
						.replace(new RegExp(",", 'g'), ", ") + " already has experimental design. Do you want to continue?";
				var modalConfirmDelete = $rootScope.openConfirmModal(deleteMessage, 'Yes','No');
				modalConfirmDelete.result.then(function (shouldContinue) {
					if (shouldContinue) {
						generateDesignCtrl.generateDesign();
					} else {
						$scope.cancel();
					}
				});
			};

			generateDesignCtrl.init = function () {
				environmentService.getEnvironments().then(function(instances) {
					$scope.instances = instances;
				});

			};

			generateDesignCtrl.init();


		}]);

})();
