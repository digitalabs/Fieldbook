(function () {
	'use strict';

	var datasetsApiModule = angular.module('datasets-api', []);

	datasetsApiModule.factory('datasetService', ['$http', '$q', 'studyContext', 'DATASET_TYPES_SUBOBSERVATION_IDS', 'serviceUtilities',
		function ($http, $q, studyContext, DATASET_TYPES_SUBOBSERVATION_IDS, serviceUtilities) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/studies/';
			var xAuthToken = JSON.parse(localStorage['bms.xAuthToken']).token;
			var config = {
				headers: {
					'X-Auth-Token': xAuthToken
				},
				cache: false
			};

			var datasetService = {};
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			datasetService.observationCount = function (studyId, datasetId, variableIds) {

				if (studyId && datasetId && variableIds) {
					return $http.head(BASE_URL + studyId + '/datasets/' + datasetId + '/variables/observations?variableIds=' + variableIds.join(','), config);
				}

				return $q.reject('studyId, datasetId and variableIds are not defined.');

			};

			datasetService.getDatasets = function () {
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

			datasetService.generation = function (newDataset) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + studyContext.measurementDatasetId + '/generation', newDataset, config);
				return request.then(successHandler, failureHandler);
			};

			return datasetService;

	}]);

})();
