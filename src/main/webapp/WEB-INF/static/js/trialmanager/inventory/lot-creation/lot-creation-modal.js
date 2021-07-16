(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('LotCreationCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance', 'searchResultDbId',
		function ($scope, $q, studyContext, $uibModalInstance, searchResultDbId) {

			$scope.url = '/ibpworkbench/controller/jhipster#/lot-creation-dialog?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&searchRequestId=' + searchResultDbId +
				'&studyId=' + studyContext.studyId +
				'&searchOrigin=MANAGE_STUDY';
			
			$scope.cancel = function () {
				$uibModalInstance.close(null);
			};

		}]);

})();
