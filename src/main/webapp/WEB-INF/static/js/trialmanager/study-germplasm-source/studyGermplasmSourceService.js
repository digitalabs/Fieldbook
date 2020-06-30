(function () {
	'use strict';

	var datasetsApiModule = angular.module('study-germplasm-source', []);

	datasetsApiModule.factory('studyGermplasmSourceService', ['$http', '$q', 'studyContext',
		function ($http, $q, studyContext) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';

			var studyGermplasmSourceService = {};

			studyGermplasmSourceService.getStudyGermplasmSourceTable = function (studyGermplasmSourceRequest) {
				return $http.post(BASE_URL + studyContext.studyId + '/germplasm-sources/table');
			};

			return studyGermplasmSourceService;

		}]);

})();