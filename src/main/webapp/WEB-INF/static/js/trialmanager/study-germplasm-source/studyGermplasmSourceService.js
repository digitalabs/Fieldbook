(function () {
	'use strict';

	var studyGermplasmSourceModule = angular.module('study-germplasmm-source');

	studyGermplasmSourceModule.factory('studyGermplasmSourceService', ['$http', '$q', 'studyContext',
		function ($http, $q, studyContext) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';

			var studyGermplasmSourceService = {};

			studyGermplasmSourceService.getStudyGermplasmSourceTableUrl = function () {
				return BASE_URL + studyContext.studyId + '/germplasm-sources/table';
			};

			return studyGermplasmSourceService;

		}]);

})();