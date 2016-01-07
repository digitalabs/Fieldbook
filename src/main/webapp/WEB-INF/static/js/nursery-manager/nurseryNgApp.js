/**
 * Created by cyrus on 01/07/2016.
 */
(function() {
	'use strict';

	var nurseryNgApp = angular.module('nurseryNgApp', ['designImportApp', 'leafnode-utils', 'ngLodash', 'ngResource']);

	nurseryNgApp.controller('experimentalDesignCtrl', ['$scope', '$http', '_', function($scope, $http, _) {
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
