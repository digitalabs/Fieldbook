(function () {
	'use strict';

	var datasetsApiModule = angular.module('datasets-api', []);

	datasetsApiModule.factory('datasetService', ['$http', '$q', 'studyContext', 'DATASET_TYPES_SUBOBSERVATION_IDS', 'serviceUtilities', 'DATASET_TYPES',
		function ($http, $q, studyContext, DATASET_TYPES_SUBOBSERVATION_IDS, serviceUtilities, DATASET_TYPES) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/studies/';

			var datasetTypeMap = {};
			var datasetTypes = [{
				id: DATASET_TYPES.PLANT_SUBOBSERVATIONS,
				name: 'Plants',
				abbr: 'Plants'
			}, {
				id: DATASET_TYPES.QUADRAT_SUBOBSERVATIONS,
				name: 'Quadrats',
				abbr: 'Quadrats'
			}, {
				id: DATASET_TYPES.TIME_SERIES_SUBOBSERVATIONS,
				name: 'Time Series',
				abbr: 'Time Series'
			}, {
				id: DATASET_TYPES.CUSTOM_SUBOBSERVATIONS,
				name: 'Sub-Observation Units',
				abbr: 'SOUs'
			}, {
				id: DATASET_TYPES.PLOT_OBSERVATIONS,
				name: 'Plots',
				abbr: 'Plots'
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
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' +
					observationUnitId, observation);
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
						datasetTypeIds: datasetTypeIds || DATASET_TYPES_SUBOBSERVATION_IDS.join(",")
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
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/drafts/rejection');
				return request.then(successHandler, failureHandler);

			};

			return datasetService;
		}]);

})();
