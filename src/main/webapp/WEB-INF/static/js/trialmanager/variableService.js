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

			getVariablesByFilter: function (filter) {
				/**
				 * See org.ibp.api.rest.ontology.VariableFilterResource.listAllVariablesUsingFilter
				 */
				var request = $http.get('/bmsapi/ontology/' + configService.getCropName() + '/filtervariables', angular.merge({
					params: angular.merge({
						programId: configService.getProgramId()
					}, filter)
				}, config));
				return request.then(successHandler, failureHandler);
			}
		};
	}]);

}());
