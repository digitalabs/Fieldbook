/**
 * Main Entry point for Nursery's Angular code
 */
/*global angular*/
(function() {
	'use strict';

	var nurseryNgApp = angular.module('nurseryNgApp', ['designImportApp', 'leafnode-utils', 'ngLodash', 'ngResource']);

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

})();
