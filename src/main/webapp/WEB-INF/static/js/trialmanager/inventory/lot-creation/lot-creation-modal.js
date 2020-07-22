(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('LotCreationCtrl', ['$scope', '$q', 'studyContext',
		function ($scope, $q, studyContext) {
			$scope.url = '/ibpworkbench/controller/jhipster#/lot-creation-dialog?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&searchRequestId=' + 1;
		}]);

})();