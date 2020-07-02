(function () {
	'use strict';

	var studyGermplasmSourceModule = angular.module('study-germplasmm-source');

	studyGermplasmSourceModule.factory('studyGermplasmSourceService', ['$http', '$q', 'studyContext', 'serviceUtilities',
		function ($http, $q, studyContext, serviceUtilities) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var studyGermplasmSourceService = {};

			studyGermplasmSourceService.getStudyGermplasmSourceTableUrl = function () {
				return BASE_URL + studyContext.studyId + '/germplasm-sources/table';
			};

			studyGermplasmSourceService.searchStudyGermplasmSources = function (studyGermplasmSourceRequest) {
				studyGermplasmSourceRequest.studyId = studyContext.studyId;
				return $http.post(BASE_URL + studyContext.studyId + '/germplasm-sources/table', studyGermplasmSourceRequest)
					.then(successHandler, failureHandler);
			};

			return studyGermplasmSourceService;

		}]);

})();