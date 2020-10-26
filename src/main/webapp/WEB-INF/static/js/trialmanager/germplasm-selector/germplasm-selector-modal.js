(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('GermplasmSelectorCtrl', ['$scope', '$rootScope', '$q', 'studyContext', '$uibModalInstance',
		function ($scope, $rootScope, $q, studyContext, $uibModalInstance) {

			$scope.url = '/ibpworkbench/controller/jhipster#/germplasm-selector?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&selectMultiple=false';

			window.closeModal = function() {
				$uibModalInstance.close(null);
			}

			window.onGidsSelected = function(gids) {
				$uibModalInstance.close(gids);
			}

		}]);

})();