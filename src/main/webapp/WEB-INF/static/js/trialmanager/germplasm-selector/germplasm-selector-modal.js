(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('GermplasmSelectorCtrl', ['$scope', '$rootScope', '$q', 'studyContext', '$uibModalInstance', 'studyEntryService', 'entryId',
		function ($scope, $rootScope, $q, studyContext, $uibModalInstance, studyEntryService, entryId) {

			$scope.url = '/ibpworkbench/controller/jhipster#/germplasm-selector?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId;
			
			$scope.cancel = function () {
				$uibModalInstance.close(null);
			};

			window.onGidsSelected = function(gids) {
				// TODO the germplasm selector could be called from other screens, not just replace germplasm
				// if there are multiple entries selected, get only the first entry for replacement
				studyEntryService.replaceStudyGermplasm(entryId, gids[0]).then(function (response) {
					showSuccessfulMessage('', $.germplasmMessages.replaceGermplasmSuccessful);
					$rootScope.$emit("reloadStudyEntryTableData", {});
					$uibModalInstance.close();
				}, function(errResponse) {
					showErrorMessage($.fieldbookMessages.errorServerError,  errResponse.errors[0].message);
					$uibModalInstance.close();
				});
			}

		}]);

})();