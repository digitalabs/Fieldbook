(function () {
	'use strict';

	var datasetsApiModule = angular.module('datasets-api', []);

	datasetsApiModule.factory('datasetService', ['$http', '$q', 'studyContext', 'DATASET_TYPES_SUBOBSERVATION_IDS', 'serviceUtilities', 'DATASET_TYPES',
		function ($http, $q, studyContext, DATASET_TYPES_SUBOBSERVATION_IDS, serviceUtilities, DATASET_TYPES) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/studies/';
			var xAuthToken = JSON.parse(localStorage['bms.xAuthToken']).token;
			var config = {
				headers: {
					'X-Auth-Token': xAuthToken
				},
				cache: false
			};

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

			angular.forEach(datasetTypes, function(datasetType) {
				datasetTypeMap[datasetType.id] = datasetType;
			});


			var datasetService = {};
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			datasetService.observationCount = function (studyId, datasetId, variableIds) {

				if (studyId && datasetId && variableIds) {
					return $http.head(BASE_URL + studyId + '/datasets/' + datasetId + '/variables/observations?variableIds=' + variableIds.join(','), config);
				}

				return $q.reject('studyId, datasetId and variableIds are not defined.');

			};

			datasetService.observationCountByInstance = function (studyId, datasetId, instanceId) {

				if (studyId && instanceId && datasetId) {
					return $http.head(BASE_URL + studyId + '/datasets/' + datasetId + '/observationUnits/' + instanceId, config);
				}

				return $q.reject('studyId, instanceId and datasetId are not defined.');

			};

			datasetService.getDatasets = function () {
				if (!studyContext.studyId) {
					return $q.resolve([]);
				}
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets', angular.merge({
					params: {
						datasetTypeIds: DATASET_TYPES_SUBOBSERVATION_IDS.join(",")
					}
				}, config));
				return request.then(successHandler, failureHandler);
			};

			datasetService.getColumns = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/table/columns', config);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getObservationTableUrl = function(datasetId, instanceId) {
				return BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/instances/' + instanceId + '/observationUnits/table';
			};

			datasetService.generation = function (newDataset) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + studyContext.measurementDatasetId + '/generation', newDataset, config);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDataset = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId, config);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDatasetType = function (datasetTypeId) {
				return datasetTypeMap[datasetTypeId];

			};

			datasetService.getPlotAndSubobservationDatasets = function () {
				if (!studyContext.studyId) {
					return $q.resolve([]);
				}
				var datasetTypeIds = DATASET_TYPES_SUBOBSERVATION_IDS.concat(DATASET_TYPES.PLOT_OBSERVATIONS);
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets', angular.merge({
					params: {
						datasetTypeIds: datasetTypeIds.join(",")
					}
				}, config));
				return request.then(successHandler, failureHandler);
			};

			return datasetService;
	}]);

})();
