/**
 * Main Entry point for Nursery's Angular code
 */
/*global angular*/
(function() {
	'use strict';

	var nurseryNgApp = angular.module('nurseryNgApp', ['designImportApp', 'leafnode-utils', 'ngLodash', 'ngResource']);

	nurseryNgApp.controller('experimentalDesignCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.applicationData = {
			importDesignMappedData: null
		};

		// Register designImportGenerated handler that will activate when an importDesign is generated
		$scope.$on('designImportGenerated', function() {
			$http.get('/Fieldbook/DesignImport/getMappingSummary').success(function(data) {
				$scope.applicationData.importDesignMappedData = data;
			});
		});

	}]);

})();
