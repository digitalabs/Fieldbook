/*global angular*/
'use strict';

(function() {
	var manageTrialApp = angular.module('manageTrialApp');
	manageTrialApp.service('variableService', ['$http', 'serviceUtilities', 'configService', function($http, serviceUtilities, configService) {

		var successHandler = serviceUtilities.restSuccessHandler,
			failureHandler = serviceUtilities.restFailureHandler;
		var xAuthToken = JSON.parse(localStorage['bms.xAuthToken']).token;
		var config = {
			headers: {
				'X-Auth-Token': xAuthToken
			}
		};

		return {

			getVariable: function (id) {
				var request = $http.get('/bmsapi/ontology/' + configService.getCropName() + '/variables/' + id + '?programId=' +
					configService.getProgramId(), config);
				return request.then(successHandler, failureHandler);
			},

			getVariables: function () {
				var request = $http.get('/bmsapi/ontology/' + configService.getCropName() + '/variables?programId=' +
					configService.getProgramId(), config);
				return request.then(successHandler, failureHandler);
			},

			getVariablesByFilter: function (id, propertyIds, methodIds, scaleIds, variableIds, exclusionVariableIds, dataTypeIds, variableTypeIds, propertyClasses) {

				var filter = '';
				if (propertyIds) {
					filter += '&propertyIds=' + propertyIds;
				}
				if (methodIds) {
					filter += '&methodIds=' + methodIds;
				}
				if (scaleIds) {
					filter += '&scaleIds=' + scaleIds;
				}
				if (variableIds) {
					filter += '&variableIds=' + variableIds;
				}
				if (exclusionVariableIds) {
					filter += '&exclusionVariableIds=' + exclusionVariableIds;
				}
				if (dataTypeIds) {
					filter += '&dataTypeIds=' + dataTypeIds;
				}
				if (variableTypeIds) {
					filter += '&variableTypeIds=' + variableTypeIds;
				}
				if (propertyClasses) {
					filter += '&propertyClasses=' + propertyClasses;
				}

				var request = $http.get('/bmsapi/ontology/' + configService.getCropName() + '/filtervariables?programId=' +
					configService.getProgramId() + filter, config);
				return request.then(successHandler, failureHandler);
			}
		};
	}]);

}());
