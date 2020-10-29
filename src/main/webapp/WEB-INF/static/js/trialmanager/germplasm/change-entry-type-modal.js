(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('changeStudyEntryTypeCtrl', ['$scope', '$rootScope', '$uibModalInstance', 'studyEntryService', 'entryId', 'currentValue',
		'studyEntryPropertyId',	function ($scope, $rootScope, $uibModalInstance, studyEntryService, entryId, currentValue, studyEntryPropertyId) {

			$scope.selected = {};
			$scope.entryTypes = [];
			$scope.init = function () {
				studyEntryService.getEntryTypes().then(function (entryTypes) {
					buildEntryTypes(entryTypes);
				})
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};


			$scope.editEntryType = function () {
				studyEntryService.updateStudyEntryProperty(entryId, $scope.selected.entryType.id, studyEntryPropertyId, 8255).then(function () {
					$uibModalInstance.close();
					$rootScope.$emit("reloadStudyEntryTableData", {});
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