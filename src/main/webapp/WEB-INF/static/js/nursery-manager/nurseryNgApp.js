/**
 * Main Entry point for Nursery's Angular code
 */
/*global angular, showErrorMessage*/
(function() {
	'use strict';

	var nurseryNgApp = angular.module('nurseryNgApp', ['designImportApp', 'leafnode-utils', 'ngLodash', 'ngResource', 'ui.select2']);

	nurseryNgApp.controller('experimentalDesignCtrl', ['$scope', '$http',  'VARIABLE_TYPES', function($scope, $http, VARIABLE_TYPES) {
		$scope.VARIABLE_TYPES = VARIABLE_TYPES;

		$scope.applicationData = {
			importDesignMappedData: null
		};

		$scope.currentDesignType = {};

		// Register designImportGenerated handler that will activate when an importDesign is generated
		$scope.$on('designImportGenerated', function() {
			$http.get('/Fieldbook/DesignImport/getMappingSummary', {timeout: 3000}).success(function(data) {
				$scope.applicationData.importDesignMappedData = data;
			}).error(function() {
				showErrorMessage('', $.fieldbookMessages.errorNoMappingSummary);
			});

			retrieveCustomDesignImportData();
		});

		function retrieveCustomDesignImportData() {
			// retrieve default values for custom design
			$http.get('/Fieldbook/DesignImport/getCustomImportDesignTypeDetails', {timeout: 3000}).success(function(data) {
				$scope.currentDesignType.name = data.name;
				$scope.currentDesignType.templateName = data.templateName;
			}).error(function() {
				showErrorMessage('', $.fieldbookMessages.errorNoDefaultValuesForCustomDesign);
			});
		}

		// init logic
		retrieveCustomDesignImportData();

	}]);

	nurseryNgApp.controller('selectUserCtrl', ['$scope', '$http', '_', function($scope, $http, _) {
		$scope.currentProgramMembers = [];

		$http.get('/Fieldbook/crosses/getCurrentUser', {timeout: 3000}).success(function(data) {
			$scope.selectedUserId = data;
		}).error(function() {
			showErrorMessage('', $.fieldbookMessages.errorNoCurrentUser);
		});

		$http.get('/Fieldbook/crosses/getCurrentProgramMembers', {timeout: 3000}).success(function(data) {
			$scope.currentProgramMembers = data;
			if (!$scope.selectedUserId) {
				$scope.selectedUserId = _.keys($scope.currentProgramMembers[0]);
			}
		}).error(function() {
			showErrorMessage('', $.fieldbookMessages.errorNoProgramMembers);
		});

		$scope.saveSelectedUser = function() {
			$http.post('/Fieldbook/crosses/submitListOwner', $scope.selectedUserId, {timeout: 3000}).success(function(data) {
				if (data.isSuccess === 0) {
					showErrorMessage('', data.error);
				}
			}).error(function() {
				showErrorMessage('', $.fieldbookMessages.errorSubmittingListOwner);
			});
		};
	}]);

})();
