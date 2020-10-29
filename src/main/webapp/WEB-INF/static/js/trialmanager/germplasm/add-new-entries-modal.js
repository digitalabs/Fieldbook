(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('AddNewEntriesController', ['$scope', '$rootScope', '$uibModal', '$uibModalInstance', 'studyEntryService',
		function ($scope, $rootScope, $uibModal, $uibModalInstance, studyEntryService) {

			$scope.selected = {};
			$scope.entryTypes = [];
			$scope.selectedGids = [];

			$scope.init = function () {
				studyEntryService.getEntryTypes().then(function (entryTypes) {
					buildEntryTypes(entryTypes);
				})
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.openSelectGermplasmForAddingEntries = function() {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/js/trialmanager/germplasm-selector/germplasm-selector-modal.html',
					controller: "GermplasmSelectorCtrl",
					windowClass: 'modal-very-huge',
				}).result.then((gids) => {
					if (gids != null) {
						$scope.selectedGids = gids;
					}
				});
			};

			function buildEntryTypes(entryTypes) {
				entryTypes.forEach(function (entryType) {
					$scope.entryTypes.push(entryType);
					if(entryType.id === parseInt(currentValue)) {
						$scope.selected.entryType = entryType;
					}
				});
			}

			$scope.init();
		}
	]);

})();