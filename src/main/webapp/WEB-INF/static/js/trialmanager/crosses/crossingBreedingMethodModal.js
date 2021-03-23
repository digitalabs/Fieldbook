/*global angular, showAlertMessage, showErrorMessage, selectContinueAdvancing*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('CrossingBreedingMethodModalCtrl', ['$scope', '$rootScope',	function ($scope, $rootScope) {

			$scope.isCrossesImport = true;
			$scope.hasHybridMethod = ImportCrosses.hasHybridMethod;
			$scope.selectedMethod;

			$scope.methodChanged = function () {
				console.log($scope.selectedMethod);
			}

			$scope.enableDisableMethodsDropdown = function () {
				$rootScope.$emit('enableDisableMethodsSelect', $('#selectMethodForAllCrosses').prop('checked'));

				if ($('#selectMethodInImportFile').prop('checked') && ImportCrosses.hasHybridMethod) {
					$("#applyGroupingOptionDiv").show();
				} else {
					$("#applyGroupingOptionDiv").hide();
				}
			}
		}]);
})();
