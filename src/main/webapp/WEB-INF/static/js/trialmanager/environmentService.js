/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('environmentService', ['$rootScope', 'TrialManagerDataService', '$http', function ($rootScope, TrialManagerDataService, $http) {

		var environmentService = {};
		var service = {
			environments: []
			, getEnvironments: function () {
				var xAuthToken = JSON.parse(localStorage['bms.xAuthToken']).token;
				var config = {
					headers: {
						'X-Auth-Token': xAuthToken
					}
				};

				$http.get('/bmsapi/study/' + cropName + '/' + TrialManagerDataService.currentData.basicDetails.studyID + '/instances', config).success(function (data) {
					service.environments = data;

				}).error(function (data) {

					if (data.status === 401) {
						bmsAuth.handleReAuthentication();
					}

					showErrorMessage('', data.errors[0].message);
					service.environments = [];
				});
			}
		};

		if(!!TrialManagerDataService.currentData.basicDetails.studyID){
			service.getEnvironments();
		}

		environmentService.environments = TrialManagerDataService.currentData.environments;

		environmentService.changeEnvironments = function () {
			this.broadcastEnvironments();
		};

		environmentService.broadcastEnvironments = function () {
			$rootScope.$broadcast('changeEnvironments');
		};

		environmentService.getEnvironmentDetails = function () {
			var environmentListView = [];

			angular.forEach(service.environments, function (environment) {
				environmentListView.push({
					name: environment.locationName + ' - (' + environment.locationAbbreviation + ')',
					abbrName: environment.locationAbbreviation,
					customAbbrName: environment.customLocationAbbreviation,
					trialInstanceNumber: environment.instanceNumber,
					instanceDbId: environment.instanceDbId

				});
			});
			return environmentListView;
		};

		environmentService.updateEnvironmentData = function(){
			service.getEnvironments();
		};

		return environmentService;

	}]);

})();
