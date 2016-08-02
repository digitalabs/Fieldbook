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
			$http.get('/Fieldbook/DesignImport/getMappingSummary').success(function(data) {
				$scope.applicationData.importDesignMappedData = data;
			});

			retrieveCustomDesignImportData();
		});

		function retrieveCustomDesignImportData() {
			// retrieve default values for custom design
			$http.get('/Fieldbook/DesignImport/getCustomImportDesignTypeDetails').success(function(data) {
				$scope.currentDesignType.name = data.name;
				$scope.currentDesignType.templateName = data.templateName;
			});
		}

		// init logic
		retrieveCustomDesignImportData();

	}]);

	nurseryNgApp.controller('selectUserCtrl', ['$scope', '$http', '_', function($scope, $http, _) {
		$scope.currentProgramMembers = [];

		$http.get('/Fieldbook/crosses/getCurrentUser').success(function(data) {
            $scope.selectedUserId = data;
        }).error(function() {
        	showErrorMessage('', 'Could not resolve current user, please try again or contact the support.');
        });

		$http.get('/Fieldbook/crosses/getCurrentProgramMembers').success(function(data) {
        	$scope.currentProgramMembers= data;
        	if (!$scope.selectedUserId) {
        		$scope.selectedUserId = _.keys($scope.currentProgramMembers[0]);
        	}
        }).error(function() {
        	showErrorMessage('', 'Could not resolve the list of members of the program, please try again or contact the support.');
        });

        $scope.saveSelectedUser = function(){
        	$http.post('/Fieldbook/crosses/submitListOwner', $scope.selectedUserId).success(function() {
            	//do nothing or success message?
            }).error(function() {
            	showErrorMessage('', 'Could not associate User id with the list, please try again or contact the support.');
            });
        };
    }]);

})();
