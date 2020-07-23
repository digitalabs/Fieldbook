(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('LotCreationCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance',
		function ($scope, $q, studyContext, $uibModalInstance) {
			$scope.url = '/ibpworkbench/controller/jhipster#/lot-creation-dialog?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&searchRequestId=' + 1 +
				'&studyId=' + studyContext.studyId;
			
			$scope.cancel = function () {
				$uibModalInstance.close(null);
			};
		}]);

})();