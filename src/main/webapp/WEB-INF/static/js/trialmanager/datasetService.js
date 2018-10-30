(function () {
	'use strict';

	var datasetsApiModule = angular.module('datasets-api', []);

	datasetsApiModule.factory('datasetService', ['$http', '$q', function ($http, $q) {

		var BASE_URL = '/bmsapi/crops/' + cropName + '/studies/';
		var xAuthToken = JSON.parse(localStorage['bms.xAuthToken']).token;
		var config = {
			headers: {
				'X-Auth-Token': xAuthToken
			},
			cache: false
		};

		var datasetService = {};

		datasetService.observationCount = function (studyId, datasetId, variableIds) {

			if (studyId && datasetId && variableIds) {
				return $http.head(BASE_URL + studyId + '/datasets/' + datasetId + '/variables/observations?variableIds=' + variableIds.join(','), config);
			}

			return $q.reject('studyId, datasetId and variableIds are not defined.');

		};

		return datasetService;

	}]);

})();