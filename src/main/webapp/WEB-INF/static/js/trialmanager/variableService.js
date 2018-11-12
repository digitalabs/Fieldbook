/*global angular*/
'use strict';

(function() {
	var manageTrialApp = angular.module('manageTrialApp');
	manageTrialApp.service('variableService', ['$http', 'serviceUtilities', 'studyContext', function($http, serviceUtilities, studyContext) {

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
				var request = $http.get('/bmsapi/ontology/' + studyContext.cropName + '/variables/' + id + '?programId=' +
					studyContext.programId, config);
				return request.then(successHandler, failureHandler);
			},

			getVariables: function () {
				var request = $http.get('/bmsapi/ontology/' + studyContext.cropName + '/variables?programId=' +
					studyContext.programId, config);
				return request.then(successHandler, failureHandler);
			},

			getVariablesByFilter: function (filter) {
				/**
				 * See org.ibp.api.rest.ontology.VariableFilterResource.listAllVariablesUsingFilter
				 */
				var request = $http.get('/bmsapi/ontology/' + studyContext.cropName + '/filtervariables', angular.merge({
					params: angular.merge({
						programId: studyContext.programId
					}, filter)
				}, config));
				return request.then(successHandler, failureHandler);
			}
		};
	}]);

}());
